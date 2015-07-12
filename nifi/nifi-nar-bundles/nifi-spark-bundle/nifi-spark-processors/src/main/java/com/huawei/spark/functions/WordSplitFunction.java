package com.huawei.spark.functions;

import org.apache.spark.api.java.function.FlatMapFunction;

import java.util.Arrays;

/**
 * Created by Allen on 2015/7/11.
 */
public class WordSplitFunction implements FlatMapFunction<String, String> {
    @Override
    public Iterable<String> call(String s) throws Exception {
        return Arrays.asList(s.split(","));
    }
}
