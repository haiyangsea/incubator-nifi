package com.huawei.spark.functions;

import org.apache.spark.api.java.function.PairFunction;
import scala.Tuple2;

/**
 * Created by Allen on 2015/7/11.
 */
public class MapPairFunction implements PairFunction<String, String, Integer> {
    @Override
    public Tuple2<String, Integer> call(String s) throws Exception {
        return new Tuple2<>(s, 1);
    }
}
