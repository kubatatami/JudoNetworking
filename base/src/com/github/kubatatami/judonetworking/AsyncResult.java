package com.github.kubatatami.judonetworking;

import java.lang.reflect.Method;
import java.util.*;

class AsyncResult implements Runnable {
    private CallbackInterface<Object> callback;
    private BatchInterface<?> transaction;
    private Object result = null;
    private Object[] results = null;
    private Exception e = null;
    private int progress = 0;
    private final Type type;

    enum Type {
        RESULT, ERROR, PROGRESS
    }

    AsyncResult(BatchInterface<?> callback, Object results[]) {
        this.results = results;
        this.transaction = callback;
        type = Type.RESULT;
    }

    AsyncResult(BatchInterface<?> callback, int progress) {
        this.progress = progress;
        this.transaction = callback;
        type = Type.PROGRESS;
    }

    AsyncResult(BatchInterface<?> callback, Exception e) {
        this.e = e;
        this.transaction = callback;
        type = Type.ERROR;
    }

    AsyncResult(CallbackInterface<Object> callback, Object result) {
        this.result = result;
        this.callback = callback;
        type = Type.RESULT;
    }

    AsyncResult(CallbackInterface<Object> callback, int progress) {
        this.progress = progress;
        this.callback = callback;
        type = Type.PROGRESS;
    }

    AsyncResult(CallbackInterface<Object> callback, Exception e) {
        this.e = e;
        this.callback = callback;
        type = Type.ERROR;
    }

    protected Method findHandleMethod(Class<?> callbackClass, Class<?> exceptionClass) {
        Method handleMethod=null;
        for (; callbackClass != null; callbackClass = callbackClass.getSuperclass()) {
            for (Method method : callbackClass.getMethods()) {
                if (method.isAnnotationPresent(HandleException.class)){
                    if(method.getParameterTypes().length!=1){
                        throw new RuntimeException("Method " + method.getName() + " annotated HandleException must have one parameter.");
                    }
                    Class<?> handleExceptionClass = method.getParameterTypes()[0];
                    if (handleExceptionClass.isAssignableFrom(exceptionClass)) {
                        if(handleMethod==null || handleMethod.getParameterTypes()[0].isAssignableFrom(handleExceptionClass)){
                            handleMethod=method;
                        }
                    }
                }
            }
        }

        return handleMethod;
    }

    @Override
    public void run() {
        if (callback != null) {
            switch (type) {
                case RESULT:
                    callback.onFinish(result);
                    break;
                case ERROR:
                    Method handleMethod = findHandleMethod(callback.getClass(), e.getClass());
                    if (handleMethod != null) {
                        try {
                            handleMethod.invoke(callback, e);
                        } catch (Exception invokeException) {
                            throw new RuntimeException(invokeException);
                        }
                    } else {
                        callback.onError(e);
                    }
                    break;
                case PROGRESS:
                    callback.onProgress(progress);
                    break;
            }
        } else {
            switch (type) {
                case RESULT:
                    transaction.onFinish(results);
                    break;
                case ERROR:
                    Method handleMethod = findHandleMethod(transaction.getClass(), e.getClass());
                    if (handleMethod != null) {
                        try {
                            handleMethod.invoke(transaction, e);
                        } catch (Exception invokeException) {
                            throw new RuntimeException(invokeException);
                        }
                    } else {
                        transaction.onError(e);
                    }
                    break;
                case PROGRESS:
                    transaction.onProgress(progress);
                    break;
            }
        }
    }
}