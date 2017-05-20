package com.github.kubatatami.judonetworking.adapters;

import com.github.kubatatami.judonetworking.AsyncResult;
import com.github.kubatatami.judonetworking.CacheInfo;
import com.github.kubatatami.judonetworking.callbacks.DefaultCallback;
import com.github.kubatatami.judonetworking.exceptions.JudoException;
import com.github.kubatatami.judonetworking.internals.MethodInfo;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

@SuppressWarnings("unchecked")
public class RxAdapter implements JudoAdapter {

    @Override
    public boolean canHandle(Type type) {
        return type.equals(Observable.class);
    }

    @Override
    public MethodInfo getMethodInfo(Type returnType, Object[] args, Type[] types) {
        final PublishSubject<RxRequestStatus> subject = PublishSubject.create();
        ParameterizedType observableType = (ParameterizedType) ((ParameterizedType)returnType).getActualTypeArguments()[0];
        Type resultType = observableType.getActualTypeArguments()[0];
        return new MethodInfo(new SubjectCallback(subject), resultType, args, subject);
    }

    private static class SubjectCallback extends DefaultCallback {

        private final PublishSubject<RxRequestStatus> subject;

        private SubjectCallback(PublishSubject<RxRequestStatus> subject) {
            this.subject = subject;
        }

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
}
