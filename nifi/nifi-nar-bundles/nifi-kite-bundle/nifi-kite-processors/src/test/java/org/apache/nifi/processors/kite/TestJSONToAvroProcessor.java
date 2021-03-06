/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.nifi.processors.kite;

import java.io.IOException;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Assert;
import org.junit.Test;

import static org.apache.nifi.processors.kite.TestUtil.streamFor;

public class TestJSONToAvroProcessor {

    public static final Schema SCHEMA = SchemaBuilder.record("Test").fields()
            .requiredLong("id")
            .requiredString("color")
            .optionalDouble("price")
            .endRecord();

    public static final String JSON_CONTENT = ""
            + "{\"id\": 1,\"color\": \"green\"}"
            + "{\"id\": \"120V\", \"color\": \"blue\"}\n" // invalid, ID is a string
            + "{\"id\": 10, \"color\": 15.23}\n" + // invalid, color as double
            "{\"id\": 2, \"color\": \"grey\", \"price\": 12.95 }";

    public static final String FAILURE_CONTENT = "Cannot convert field id [Cannot convert to long: \"120V\"]\n"
            + "Cannot convert field color [Cannot convert to string: 15.23]\n";

    @Test
    public void testBasicConversion() throws IOException {
        TestRunner runner = TestRunners.newTestRunner(ConvertJSONToAvro.class);
        runner.assertNotValid();
        runner.setProperty(ConvertJSONToAvro.SCHEMA, SCHEMA.toString());
        runner.assertValid();

        runner.enqueue(streamFor(JSON_CONTENT));
        runner.run();

        long converted = runner.getCounterValue("Converted records");
        long errors = runner.getCounterValue("Conversion errors");
        Assert.assertEquals("Should convert 2 rows", 2, converted);
        Assert.assertEquals("Should reject 2 rows", 2, errors);

        runner.assertTransferCount("success", 1);
        runner.assertTransferCount("failure", 1);

        String failureContent = Bytes.toString(runner.getContentAsByteArray(
                runner.getFlowFilesForRelationship("failure").get(0)));
        Assert.assertEquals("Should reject an invalid string and double", FAILURE_CONTENT, failureContent);
    }
}
