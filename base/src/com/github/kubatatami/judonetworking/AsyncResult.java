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
    protected Method method;
    protected EndpointImplementation rpc;

    enum Type {
        RESULT, ERROR, PROGRESS, START
    }

    AsyncResult(EndpointImplementation rpc, BatchInterface<?> callback, Object results[]) {
        this.results = results;
        this.transaction = callback;
        this.rpc=rpc;
        this.type = Type.RESULT;
    }

    AsyncResult(EndpointImplementation rpc,BatchInterface<?> callback, int progress) {
        this.progress = progress;
        this.transaction = callback;
        this.rpc=rpc;
        this.type = Type.PROGRESS;
    }

    AsyncResult(EndpointImplementation rpc,BatchInterface<?> callback, Exception e) {
        this.e = e;
        this.transaction = callback;
        this.rpc=rpc;
        this.type = Type.ERROR;
    }

    AsyncResult(Request request) {
        this.callback = request.getCallback();
        this.rpc=request.getRpc();
        this.type = Type.START;
    }


    AsyncResult(Request request, Object result) {
        this.result = result;
        this.callback = request.getCallback();
        this.rpc=request.getRpc();
        this.method=request.getMethod();
        this.type = Type.RESULT;
    }

    AsyncResult(Request request, int progress) {
        this.progress = progress;
        this.callback = request.getCallback();
        this.rpc=request.getRpc();
        this.method=request.getMethod();
        this.type = Type.PROGRESS;
    }

    AsyncResult(Request request, Exception e) {
        this.e = e;
        this.callback = request.getCallback();
        this.rpc=request.getRpc();
        this.method=request.getMethod();
        this.type = Type.ERROR;
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
                case START:
                    callback.onStart();
                    break;
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
        } else if(transaction!=null){
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
        if(method!=null) {
            switch (type) {
                case ERROR:
                case RESULT:
                    synchronized (rpc.getSingleCallMethods()){
                        rpc.getSingleCallMethods().remove(method);
                        if ((rpc.getDebugFlags() & Endpoint.REQUEST_LINE_DEBUG) > 0) {
                            LoggerImpl.log("Request " + method.getName() + " removed - SingleCall.");
                        }
                    }
                    break;
            }
        }
    }

    protected void logError(Exception ex){
        if((rpc.getDebugFlags() & Endpoint.ERROR_DEBUG) > 0){
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