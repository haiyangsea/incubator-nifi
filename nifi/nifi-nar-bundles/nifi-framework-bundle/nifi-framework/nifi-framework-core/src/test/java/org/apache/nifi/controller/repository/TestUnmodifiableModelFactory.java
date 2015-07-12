package org.apache.nifi.controller.repository;

import org.apache.nifi.datamodel.Model;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Allen on 2015/7/11.
 */
public class TestUnmodifiableModelFactory {
    static class TestModel implements Model {
        private final Object data;
        public TestModel(Object data) {
            this.data = data;
        }

        public Object getData() {
            return this.data;
        }
    }

    @Test
    public void testUnmodifiableModel() {
        TestModel model = new TestModel("Hello");
        Model unmodifieableModel = UnmodifiableModelFactory.getUnmodifiableModel(model);

        Object hello = unmodifieableModel.getData();

        Assert.assertFalse(unmodifieableModel instanceof TestModel);
        Assert.assertEquals("Hello", hello.toString());
    }
}
