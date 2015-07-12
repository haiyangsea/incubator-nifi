package com.huawei.spark.processors;

import org.apache.nifi.datamodel.Model;

/**
 * Created by Allen on 2015/7/11.
 */
public class SparkModel implements Model {
    private final Object data;

    public SparkModel(Object data) {
        this.data = data;
    }

    @Override
    public Object getData() {
        return this.data;
    }
}
