package com.huawei.spark.processors;

import java.io.InputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 * Created by Allen on 2015/7/4.
 */
public interface Serializer {
    <T extends Serializable> T deserialize(InputStream in);
    <T extends Serializable> T deserialize(ByteBuffer data);
    <T extends Serializable>  ByteBuffer serialize(T obj);
    <T extends Serializable> InputStream serializeAsStream(T obj);
}
