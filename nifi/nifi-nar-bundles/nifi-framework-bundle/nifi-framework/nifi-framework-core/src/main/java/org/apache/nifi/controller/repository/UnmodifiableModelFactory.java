package org.apache.nifi.controller.repository;

import org.apache.nifi.datamodel.Model;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by Allen on 2015/7/11.
 */
public class UnmodifiableModelFactory {

    private static class Handler implements InvocationHandler {
        private final Model modelProxy;
        Handler(Model modelProxy) {
            this.modelProxy = modelProxy;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return method.invoke(this.modelProxy, args);
        }
    }

    public static Model getUnmodifiableModel(Model proxy) {
        Object instance = Proxy.newProxyInstance(UnmodifiableModelFactory.class.getClassLoader(),
                new Class[] {Model.class},
                new Handler(proxy));

        return (Model)instance;
    }
}
