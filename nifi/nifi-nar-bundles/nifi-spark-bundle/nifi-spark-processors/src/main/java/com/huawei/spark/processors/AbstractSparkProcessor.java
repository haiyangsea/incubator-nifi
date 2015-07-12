package com.huawei.spark.processors;

import org.apache.nifi.datamodel.DataModel;
import org.apache.nifi.datamodel.Model;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.flowfile.attributes.CoreAttributes;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.io.InputStreamCallback;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.DataFrame;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Allen on 2015/7/4.
 */
public abstract class AbstractSparkProcessor extends AbstractProcessor {
    protected static final JavaSparkContext spark;

    static {
        spark = new JavaSparkContext("local[4]", "NiFi App");
    }

    // relationships
    public final Relationship REL_SUCCESS = new Relationship.Builder()
            .name("success")
            .description("All files retrieved from HDFS are transferred to this relationship")
            .build();

    @Override
    public Set<Relationship> getRelationships() {
        Set<Relationship> others = getOtherRelationships();
        Set<Relationship> relationships = new HashSet<>(others.size() + 1);

        relationships.add(REL_SUCCESS);
        relationships.addAll(others);

        return relationships;
    }

    protected Set<Relationship> getOtherRelationships() {
        return Collections.emptySet();
    }

    @Override
    public final void onTrigger(ProcessContext context, ProcessSession session) throws ProcessException {
        FlowFile flowFile = session.get();

        if (flowFile == null) {
            flowFile = session.create();
            flowFile = session.putAttribute(flowFile, CoreAttributes.PATH.key(), "Invalid path");
        }

        Model model = process(context, session);

        session.setModelData(model);

        session.transfer(flowFile, REL_SUCCESS);
    }

    protected abstract Model process(ProcessContext context, ProcessSession session);
}
