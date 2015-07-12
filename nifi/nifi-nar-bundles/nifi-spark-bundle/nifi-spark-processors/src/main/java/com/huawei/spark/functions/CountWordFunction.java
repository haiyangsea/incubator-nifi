package com.huawei.spark.functions;

import org.apache.spark.api.java.function.Function2;

/**
 * Created by Allen on 2015/7/11.
 */
public class CountWordFunction implements Function2<Integer, Integer, Integer> {
    @Override
    public Integer call(Integer integer, Integer integer2) throws Exception {
        return Integer.valueOf(integer.intValue() + integer2.intValue());
    }
}
