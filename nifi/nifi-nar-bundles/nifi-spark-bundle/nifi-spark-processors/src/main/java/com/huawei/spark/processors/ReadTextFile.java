package com.huawei.spark.processors;

import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.datamodel.DataModel;
import org.apache.nifi.datamodel.Model;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.util.StandardValidators;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Allen on 2015/7/11.
 */
public class ReadTextFile extends AbstractSparkProcessor {
    // properties
    public static final PropertyDescriptor PATH = new PropertyDescriptor.Builder()
            .name("Path")
            .description("The HDFS directory from which files should be read")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return Collections.unmodifiableList(Arrays.asList(PATH));
    }

    @Override
    protected Model process(ProcessContext context, ProcessSession session) {
        String path = context.getProperty(PATH).getValue();
        return new SparkModel(spark.textFile(path));
    }
}
