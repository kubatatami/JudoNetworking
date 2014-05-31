package com.github.kubatatami.judonetworking;

import android.support.v4.util.LruCache;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * Created by Kuba on 23/05/14.
 */
public class ReflectionCache {

    protected final static LruCache<Class<?>, Method[]> interfaceMethodCache = new LruCache<Class<?>, Method[]> (100);
    protected final static LruCache<Class<?>, Annotation[]> interfaceAnnotationCache = new LruCache<Class<?>, Annotation[]> (100);
    protected final static LruCache<Method, Annotation[]> methodAnnotationCache = new LruCache<Method, Annotation[]>(100);
    protected final static LruCache<Method, Annotation[][]> methodParamAnnotationCache = new LruCache<Method, Annotation[][]>(100);
    protected final static LruCache<Field, Annotation[]> fieldAnnotationCache = new LruCache<Field, Annotation[]>(100);
    protected final static LruCache<Method, Type[]> methodParamsTypeCache = new LruCache<Method, Type[]>(100);

    public static Annotation[] getAnnotations(Class<?> apiInterface){
        Annotation[] result = interfaceAnnotationCache.get(apiInterface);
        if(result==null){
            result=apiInterface.getAnnotations();
            interfaceAnnotationCache.put(apiInterface,result);
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
        Annotation[] result = fieldAnnotationCache.get(field);
        if(result==null){
            result=field.getAnnotations();
            fieldAnnotationCache.put(field,result);
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
                getAnnotations(apiInterface);
                for(Method method: apiInterface.getMethods()){
                    getAnnotations(method);
                    getGenericParameterTypes(method);
                }
            }
        }).start();
    }
}
