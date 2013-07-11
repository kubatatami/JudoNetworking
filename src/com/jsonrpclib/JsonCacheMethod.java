package com.jsonrpclib;

import java.lang.reflect.Method;

public class JsonCacheMethod
    {
        private String test;
        private int testRevision;
        private String url;
        private Method method;

        public JsonCacheMethod(String url, Method method) {
            this.url = url;
            this.method = method;
        }

        public JsonCacheMethod(String test, int testRevision, String url, Method method) {
            this.test = test;
            this.testRevision = testRevision;
            this.url = url;
            this.method = method;
        }


        public String getTest() {
            return test;
        }

        public int getTestRevision() {
            return testRevision;
        }

        public String getUrl() {
            return url;
        }

        public Method getMethod() {
            return method;
        }
    }