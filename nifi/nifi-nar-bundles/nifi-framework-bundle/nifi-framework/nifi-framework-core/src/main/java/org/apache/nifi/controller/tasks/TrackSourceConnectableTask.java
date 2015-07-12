package org.apache.nifi.controller.tasks;

import org.apache.nifi.annotation.lifecycle.OnStopped;
import org.apache.nifi.connectable.Connectable;
import org.apache.nifi.connectable.ConnectableType;
import org.apache.nifi.connectable.Connection;
import org.apache.nifi.controller.repository.StandardProcessSessionFactory;
import org.apache.nifi.controller.scheduling.ProcessContextFactory;
import org.apache.nifi.controller.scheduling.ScheduleState;
import org.apache.nifi.nar.NarCloseable;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSessionFactory;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.util.Connectables;
import org.apache.nifi.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Allen on 2015/7/12.
 */
public class TrackSourceConnectableTask  implements Callable<Boolean> {

    private static final Logger logger = LoggerFactory.getLogger(ContinuallyRunConnectableTask.class);

    private final Connectable connectable;
    private final ScheduleState scheduleState;
    private final ProcessSessionFactory sessionFactory;
    private final ProcessContext processContext;

    public TrackSourceConnectableTask(final ProcessContextFactory contextFactory,
                                      final Connectable connectable,
                                      final ScheduleState scheduleState,
                                      final ProcessContext processContext) {
        this.connectable = connectable;
        this.scheduleState = scheduleState;
        this.sessionFactory = new StandardProcessSessionFactory(contextFactory.newProcessContext(connectable, new AtomicLong(0L)));
        this.processContext = processContext;
    }

    @Override
    @SuppressWarnings("deprecation")
    public Boolean call() {
        if (!scheduleState.isScheduled()) {
            return false;
        }

        // Connectable should run if the following conditions are met:
        // 1. It is not yielded.
        // 2. It has incoming connections with FlowFiles queued or doesn't expect incoming connections
        // 3. If it is a funnel, it has an outgoing connection (this is needed because funnels are "always on"; other
        //    connectable components cannot even be started if they need an outbound connection and don't have one)
        // 4. There is a connection for each relationship.
        final boolean triggerWhenEmpty = connectable.isTriggerWhenEmpty();
        boolean flowFilesQueued = true;
        final boolean shouldRun = (connectable.getYieldExpiration() < System.currentTimeMillis())
                && (triggerWhenEmpty || (flowFilesQueued = Connectables.flowFilesQueued(connectable)))
                && (connectable.getConnectableType() != ConnectableType.FUNNEL || !connectable.getConnections().isEmpty())
                && (connectable.getRelationships().isEmpty() || Connectables.anyRelationshipAvailable(connectable));

        if (shouldRun) {
            scheduleState.incrementActiveThreadCount();
            try {


                try (final AutoCloseable ncl = NarCloseable.withNarLoader()) {
                    run(connectable, processContext, sessionFactory);
                } catch (final ProcessException pe) {
                    logger.error("{} failed to process session due to {}", connectable, pe.toString());
                } catch (final Throwable t) {
                    logger.error("{} failed to process session due to {}", connectable, t.toString());
                    logger.error("", t);

                    logger.warn("{} Administratively Pausing for 10 seconds due to processing failure: {}", connectable, t.toString(), t);
                    try {
                        Thread.sleep(10000L);
                    } catch (final InterruptedException e) {
                    }

                }
            } finally {
                if (!scheduleState.isScheduled() && scheduleState.getActiveThreadCount() == 1 && scheduleState.mustCallOnStoppedMethods()) {
                    try (final NarCloseable x = NarCloseable.withNarLoader()) {
                        ReflectionUtils.quietlyInvokeMethodsWithAnnotations(OnStopped.class, org.apache.nifi.processor.annotation.OnStopped.class, connectable, processContext);
                    }
                }

                scheduleState.decrementActiveThreadCount();
            }
        } else if (!flowFilesQueued) {
            // FlowFiles must be queued in order to run but there are none queued;
            // yield for just a bit.
            return true;
        }

        return false; // do not yield
    }

    private void run(Connectable connectable, ProcessContext context, ProcessSessionFactory contextFactory) {
        List<Connection> connections = connectable.getIncomingConnections();
        for (Connection connection : connections) {
            if (connection.getDataModel() == null) {
                run(connection.getSource(), context, contextFactory);
            }
        }

        connectable.onTrigger(context, contextFactory);
    }
}
