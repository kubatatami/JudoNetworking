package com.github.kubatatami.judonetworking.adapters;

import com.github.kubatatami.judonetworking.AsyncResult;
import com.github.kubatatami.judonetworking.CacheInfo;
import com.github.kubatatami.judonetworking.callbacks.DefaultCallback;
import com.github.kubatatami.judonetworking.exceptions.JudoException;
import com.github.kubatatami.judonetworking.internals.MethodInfo;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Action;
import io.reactivex.subjects.PublishSubject;

@SuppressWarnings("unchecked")
public class RxJava2Adapter implements JudoAdapter {

    @Override
    public boolean canHandle(Type type) {
        return type instanceof ParameterizedType && isSupportedType(((ParameterizedType) type).getRawType());
    }

    private boolean isSupportedType(Type type) {
        return type.equals(Observable.class) || type.equals(Single.class);
    }

    @Override
    public MethodInfo getMethodInfo(Type returnType, Object[] args, Type[] types) {
        ParameterizedType observableType = (ParameterizedType) ((ParameterizedType)returnType).getActualTypeArguments()[0];
        if (observableType.getRawType().equals(RxRequestStatus.class)) {
            Type resultType = observableType.getActualTypeArguments()[0];
            RxRequestStatusSubjectCallback callback = new RxRequestStatusSubjectCallback();
            return new MethodInfo(callback, resultType, args, prepareReturnObject(callback.observable, returnType));
        } else {
            Type resultType = observableType.getRawType();
            SimpleSubjectCallback callback = new SimpleSubjectCallback();
            return new MethodInfo(callback, resultType, args, prepareReturnObject(callback.observable, returnType));
        }

    }

    private Object prepareReturnObject(Observable subject, Type observableType) {
        if (observableType.equals(Observable.class)) {
            return subject;
        } else {
            return subject.firstOrError();
        }
    }

    private static class RxRequestStatusSubjectCallback extends DefaultCallback {

        private final PublishSubject<RxRequestStatus> subject = PublishSubject.create();
        final Observable<RxRequestStatus> observable = subject.doOnDispose(new Action() {
            @Override
            public void run() {
                if (getAsyncResult() != null) getAsyncResult().cancel();
            }
        });

        @Override
        public void onStart(CacheInfo cacheInfo, AsyncResult asyncResult) {
            super.onStart(cacheInfo, asyncResult);
            subject.onNext(new RxRequestStatus(asyncResult, 0));
        }

        @Override
        public void onSuccess(Object result) {
            subject.onNext(new RxRequestStatus(getAsyncResult(), result));
        }

        @Override
        public void onError(JudoException e) {
            subject.onError(e);
        }

        @Override
        public void onFinish() {
            subject.onComplete();
        }

        @Override
        public void onProgress(int progress) {
            subject.onNext(new RxRequestStatus(getAsyncResult(), progress));
        }
    }

    private static class SimpleSubjectCallback extends DefaultCallback {

        private final PublishSubject<Object> subject = PublishSubject.create();
        final Observable<Object> observable = subject.doOnDispose(new Action() {
            @Override
            public void run() {
                if (getAsyncResult() != null) getAsyncResult().cancel();
            }
        });

        @Override
        public void onSuccess(Object result) {
            subject.onNext(result);
        }

        @Override
        public void onError(JudoException e) {
            subject.onError(e);
        }

        @Override
        public void onFinish() {
            subject.onComplete();
        }
    }
}