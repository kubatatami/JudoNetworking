package com.github.kubatatami.judonetworking;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.*;

class AsyncResult implements Runnable {
    protected CallbackInterface<Object> callback;
    protected BatchInterface<?> transaction;
    protected Object result = null;
    protected Object[] results = null;
    protected Exception e = null;
    protected int progress = 0;
    protected final Type type;
    protected int debugFlags;

    enum Type {
        RESULT, ERROR, PROGRESS
    }

    AsyncResult(BatchInterface<?> callback, Object results[],int debugFlags) {
        this.results = results;
        this.transaction = callback;
        this.debugFlags=debugFlags;
        type = Type.RESULT;
    }

    AsyncResult(BatchInterface<?> callback, int progress,int debugFlags) {
        this.progress = progress;
        this.transaction = callback;
        this.debugFlags=debugFlags;
        type = Type.PROGRESS;
    }

    AsyncResult(BatchInterface<?> callback, Exception e,int debugFlags) {
        this.e = e;
        this.transaction = callback;
        this.debugFlags=debugFlags;
        type = Type.ERROR;
    }

    AsyncResult(CallbackInterface<Object> callback, Object result,int debugFlags) {
        this.result = result;
        this.callback = callback;
        this.debugFlags=debugFlags;
        type = Type.RESULT;
    }

    AsyncResult(CallbackInterface<Object> callback, int progress,int debugFlags) {
        this.progress = progress;
        this.callback = callback;
        this.debugFlags=debugFlags;
        type = Type.PROGRESS;
    }

    AsyncResult(CallbackInterface<Object> callback, Exception e,int debugFlags) {
        this.e = e;
        this.callback = callback;
        this.debugFlags=debugFlags;
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
                    logError(e);
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
                    logError(e);
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

    protected void logError(Exception ex){
        if((debugFlags & Endpoint.ERROR_DEBUG) > 0){
            if (ex != null) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                ex.printStackTrace(pw);
                LoggerImpl.log(sw.toString());
            } else {
                LoggerImpl.log("Null exception");
            }
        }
    }
}