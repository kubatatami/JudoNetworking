package com.jsonrpclib.observers;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 27.02.2013
 * Time: 16:37
 */
public class JsonObserver {
    static Object dataObject;
    static Class<?> dataClass;

    public static void setDataObject(Object data) {
        dataObject = data;
        dataClass = data.getClass();
    }

    public static void setDataClass(Class<?> dataClass) {
        dataObject = null;
        JsonObserver.dataClass = dataClass;
    }
}
