package com.huawei.spark.processors;

import com.huawei.spark.functions.CountWordFunction;
import com.huawei.spark.functions.MapPairFunction;
import com.huawei.spark.functions.WordSplitFunction;
import org.apache.nifi.datamodel.Model;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;

/**
 * Created by Allen on 2015/7/11.
 */
public class WordCount extends AbstractSparkProcessor {
    @Override
    protected Model process(ProcessContext context, ProcessSession session) {
        Model model = session.getDataModel().getModel();
        JavaRDD<String> lines = (JavaRDD<String>)model.getData();
        JavaPairRDD<String, Integer> words = lines.flatMap(new WordSplitFunction())
                .mapToPair(new MapPairFunction()).reduceByKey(new CountWordFunction());

        return new SparkModel(words);
    }
}
