package com.github.kubatatami.judonetworking.utils;

import android.os.*;
import android.os.Process;
import android.support.v4.util.LruCache;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * Created by Kuba on 23/05/14.
 */
public class ReflectionCache {

    protected final static LruCache<Class<?>, Method[]> interfaceMethodCache = new LruCache<>(100);
    protected final static LruCache<Class<?>, Field[]> fieldCache = new LruCache<>(100);
    protected final static LruCache<Class<?>, Annotation[]> interfaceAnnotationCache = new LruCache<>(100);
    protected final static LruCache<Method, Annotation[]> methodAnnotationCache = new LruCache<>(100);
    protected final static LruCache<Method, Annotation[][]> methodParamAnnotationCache = new LruCache<>(100);
    protected final static LruCache<String, Annotation[]> fieldAnnotationCache = new LruCache<>(100);
    protected final static LruCache<Method, Type[]> methodParamsTypeCache = new LruCache<>(100);

    public static void clearCache(){
        interfaceAnnotationCache.evictAll();
        fieldCache.evictAll();
        methodAnnotationCache.evictAll();
        methodParamAnnotationCache.evictAll();
        fieldAnnotationCache.evictAll();
        methodParamsTypeCache.evictAll();
    }

    public static Annotation[] getAnnotations(Class<?> apiInterface){
        Annotation[] result = interfaceAnnotationCache.get(apiInterface);
        if(result==null){
            result=apiInterface.getAnnotations();
            interfaceAnnotationCache.put(apiInterface,result);
        }
        return result;
    }

    public static Field[] getDeclaredFields(Class<?> apiInterface){
        Field[] result = fieldCache.get(apiInterface);
        if(result==null){
            result=apiInterface.getDeclaredFields();
            fieldCache.put(apiInterface,result);
        }
        return result;
    }

    public static Method[] getMethods(Class<?> apiInterface){
        Method[] result = interfaceMethodCache.get(apiInterface);
        if(result==null){
            result=apiInterface.getMethods();
            interfaceMethodCache.put(apiInterface,result);
        }
        return result;
    }

    public static Annotation[] getAnnotations(Method method){
        Annotation[] result = methodAnnotationCache.get(method);
        if(result==null){
            result=method.getAnnotations();
            methodAnnotationCache.put(method,result);
        }
        return result;
    }

    public static Annotation[] getAnnotations(Field field){
        String value=field.getDeclaringClass().getName().concat(field.getName());
        Annotation[] result = fieldAnnotationCache.get(value);
        if(result==null){
            result=field.getAnnotations();
            fieldAnnotationCache.put(value,result);
        }
        return result;
    }


    public static Annotation[][] getParameterAnnotations(Method method){
        Annotation[][] result = methodParamAnnotationCache.get(method);
        if(result==null){
            result=method.getParameterAnnotations();
            methodParamAnnotationCache.put(method,result);
        }
        return result;
    }

    public static <T extends Annotation> T getAnnotation(Class<?> apiInterface, Class<T> annotationClass) {
        Annotation[] annotations=getAnnotations(apiInterface);
        for(Annotation annotation : annotations){
            if(annotationClass.isInstance(annotation)){
                return (T) annotation;
            }
        }
        return null;
    }

    public static <T extends Annotation> T findAnnotation(Annotation[] annotations, Class<T> annotationClass){
        for(Annotation annotation : annotations){
            if(annotationClass.isInstance(annotation)){
                return (T) annotation;
            }
        }
        return null;
    }

    public static <T extends Annotation> T getAnnotation(Method method, Class<T> annotationClass) {
        Annotation[] annotations=getAnnotations(method);
        for(Annotation annotation : annotations){
            if(annotationClass.isInstance(annotation)){
                return (T) annotation;
            }
        }
        return null;
    }

    public static <T extends Annotation> T getAnnotation(Field field, Class<T> annotationClass) {
        Annotation[] annotations=getAnnotations(field);
        for(Annotation annotation : annotations){
            if(annotationClass.isInstance(annotation)){
                return (T) annotation;
            }
        }
        return null;
    }

    public static <T extends Annotation> T getAnnotationInherited(Method method, Class<T> annotationClass) {
        T ann = ReflectionCache.getAnnotation(method,annotationClass);
        if (ann == null) {
            ann = ReflectionCache.getAnnotation(method.getDeclaringClass(),annotationClass);
        }
        return ann;
    }


    public static Type[] getGenericParameterTypes(Method method){
        Type[] result = methodParamsTypeCache.get(method);
        if(result==null){
            result=method.getGenericParameterTypes();
            methodParamsTypeCache.put(method,result);
        }
        return result;
    }

    public static void preLoad(final Class<?> apiInterface){
        new Thread(new Runnable() {
            @Override
            public void run() {
                android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                getAnnotations(apiInterface);
                for(Method method: apiInterface.getMethods()){
                    getAnnotations(method);
                    getGenericParameterTypes(method);
                }
            }
        }).start();
    }
}
