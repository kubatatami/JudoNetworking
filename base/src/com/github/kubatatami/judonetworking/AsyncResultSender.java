package com.github.kubatatami.judonetworking;

import com.github.kubatatami.judonetworking.exceptions.JudoException;

import java.lang.reflect.Method;

class AsyncResultSender implements Runnable {
    protected CallbackInterface<Object> callback;
    protected BatchInterface<?> transaction;
    protected Object result = null;
    protected Object[] results = null;
    protected JudoException e = null;
    protected int progress = 0;
    protected final Type type;
    protected Method method;
    protected EndpointImplementation rpc;
    protected Request request;

    enum Type {
        RESULT, ERROR, PROGRESS, START
    }

    AsyncResultSender(EndpointImplementation rpc, BatchInterface<?> callback, Object results[]) {
        this.results = results;
        this.transaction = callback;
        this.rpc = rpc;
        this.type = Type.RESULT;
    }

    AsyncResultSender(EndpointImplementation rpc, BatchInterface<?> callback, int progress) {
        this.progress = progress;
        this.transaction = callback;
        this.rpc = rpc;
        this.type = Type.PROGRESS;
    }

    AsyncResultSender(EndpointImplementation rpc, BatchInterface<?> callback, JudoException e) {
        this.e = e;
        this.transaction = callback;
        this.rpc = rpc;
        this.type = Type.ERROR;
    }

    AsyncResultSender(Request request) {
        this.callback = request.getCallback();
        this.request = request;
        this.rpc = request.getRpc();
        this.type = Type.START;
    }


    AsyncResultSender(Request request, Object result) {
        this.result = result;
        this.callback = request.getCallback();
        this.request = request;
        this.rpc = request.getRpc();
        this.method = request.getMethod();
        this.type = Type.RESULT;
    }

    AsyncResultSender(Request request, int progress) {
        this.progress = progress;
        this.callback = request.getCallback();
        this.request = request;
        this.rpc = request.getRpc();
        this.method = request.getMethod();
        this.type = Type.PROGRESS;
    }

    AsyncResultSender(Request request, JudoException e) {
        this.e = e;
        this.callback = request.getCallback();
        this.request = request;
        this.rpc = request.getRpc();
        this.method = request.getMethod();
        this.type = Type.ERROR;
    }

    protected Method findHandleMethod(Class<?> callbackClass, Class<?> exceptionClass) {
        Method handleMethod = null;
        for (; callbackClass != null; callbackClass = callbackClass.getSuperclass()) {
            for (Method method : callbackClass.getMethods()) {
                if (method.isAnnotationPresent(HandleException.class)) {
                    if (method.getParameterTypes().length != 1) {
                        throw new RuntimeException("Method " + method.getName() + " annotated HandleException must have one parameter.");
                    }
                    Class<?> handleExceptionClass = method.getParameterTypes()[0];
                    if (handleExceptionClass.isAssignableFrom(exceptionClass)) {
                        if (handleMethod == null || handleMethod.getParameterTypes()[0].isAssignableFrom(handleExceptionClass)) {
                            handleMethod = method;
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
            if (request.isCancelled()) {
                return;
            }
            switch (type) {
                case START:
                    callback.onStart();
                    break;
                case RESULT:
                    callback.onSuccess(result);
                    request.done();
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
                    request.done();
                    break;
                case PROGRESS:
                    callback.onProgress(progress);
                    break;
            }
            if (type == Type.RESULT || type == Type.ERROR) {
                callback.onFinish();
            }
        } else if (transaction != null) {
            switch (type) {
                case RESULT:
                    transaction.onSuccess(results);
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
            if (type == Type.RESULT || type == Type.ERROR) {
                transaction.onFinish();
            }
        }
        if (method != null) {
            switch (type) {
                case ERROR:
                case RESULT:
                    synchronized (rpc.getSingleCallMethods()) {
                        boolean result = rpc.getSingleCallMethods().remove(method);
                        if (result && (rpc.getDebugFlags() & Endpoint.REQUEST_LINE_DEBUG) > 0) {
                            LoggerImpl.log("Request " + method.getName() + " removed from SingleCall queue.");
                        }
                    }
                    break;
            }
        }
    }

    protected void logError(Exception ex) {
        if ((rpc.getDebugFlags() & Endpoint.ERROR_DEBUG) > 0) {
            LoggerImpl.log(ex);
        }
    }
}