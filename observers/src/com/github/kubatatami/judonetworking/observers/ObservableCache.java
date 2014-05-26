package com.github.kubatatami.judonetworking.observers;

import android.content.Context;
import android.support.v4.util.LruCache;

import com.github.kubatatami.judonetworking.ReflectionCache;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import dalvik.system.DexFile;

/**
 * Created by Kuba on 16/02/14.
 */
public class ObservableCache {
    protected final static LruCache<Class<?>, List<DataObserverMethod>> dataObserverMethodsCache = new LruCache<Class<?>, List<DataObserverMethod>>(100);
    protected final static LruCache<Class<?>, LruCache<String, Field>> fieldsCache = new LruCache<Class<?>, LruCache<String, Field>>(100);

    protected static class PreloadRunnable implements Runnable {

        Context context;
        String packageName;

        public PreloadRunnable(Context context, String packageName) {
            this.context = context;
            this.packageName = packageName;
        }

        @Override
        public void run() {
            try {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                DexFile df = new DexFile(context.getPackageCodePath());
                for (Enumeration<String> iter = df.entries(); iter.hasMoreElements(); ) {
                    String className = iter.nextElement();
                    if (className.contains(packageName)) {
                        Class<?> clazz = Class.forName(className);
                        try {
                            dataObserverMethodsCache.put(clazz, getMethods(clazz));
                        }catch (NoClassDefFoundError ex){}
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected static Field getDataObserverField(Method method, DataObserver ann) {


        if (!ann.fieldName().equals("")) {
            return getField(ann.fieldName());
        }

        String methodName = method.getName();
        if (methodName.length() > ObserverHelper.convention.length() + 1) {
            String fieldName = methodName.substring(0, methodName.length() - ObserverHelper.convention.length());

            return getField(fieldName);

        }
        return null;
    }

    protected static List<DataObserverMethod> getMethods(Class<?> clazz) {
        List<DataObserverMethod> methodList = new ArrayList<DataObserverMethod>();
        for (final Method method : ReflectionCache.getMethods(clazz)) {
            DataObserver ann = ReflectionCache.getAnnotation(method, DataObserver.class);
            if (ann != null) {
                Field field = getDataObserverField(method, ann);
                if (field != null) {
                    methodList.add(new DataObserverMethod(method, field, ann));
                }
            }
        }
        return methodList;
    }

    public static List<DataObserverMethod> getDataObserverMethods(Class<?> clazz) {
        List<DataObserverMethod> methods = dataObserverMethodsCache.get(clazz);
        if (methods == null) {
            methods = getMethods(clazz);
            dataObserverMethodsCache.put(clazz, methods);
        }
        return methods;
    }

    public static void clearCache() {
        dataObserverMethodsCache.evictAll();
    }

    public static void preLoad(Context context, String packageName) {
        Thread thread = new Thread(new PreloadRunnable(context, packageName));
        thread.start();
    }

    public static class DataObserverMethod {
        public Method method;
        public Field field;
        public DataObserver dataObserver;

        public DataObserverMethod(Method method, Field field, DataObserver dataObserver) {
            this.method = method;
            this.field = field;
            this.dataObserver = dataObserver;
        }
    }

    protected static Field getField(String fieldName) {
        return getField(fieldName, ObserverHelper.dataClass);
    }

    protected static Field getField(String fieldName, Class<?> objectClass) {
        LruCache<String, Field> fields = fieldsCache.get(objectClass);
        if (fields == null) {
            fields = new LruCache<String, Field>(10);
            fieldsCache.put(objectClass, fields);
        }
        Field field = fields.get(fieldName);
        if (field == null) {
            field = getFieldImplementation(fieldName, objectClass);
            fields.put(fieldName, field);
        }
        return field;
    }

    protected static Field getFieldImplementation(String fieldName, Class<?> objectClass) {
        Field field = null;
        while (objectClass != null && field == null) {
            try {
                field = objectClass.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                objectClass = objectClass.getSuperclass();
            }
        }
        if (field != null) {
            field.setAccessible(true);
        } else {
            throw new RuntimeException(new NoSuchFieldException(fieldName));
        }
        return field;
    }

}
