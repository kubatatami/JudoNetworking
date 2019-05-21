package com.github.kubatatami.judonetworking.adapters;

import com.github.kubatatami.judonetworking.callbacks.DefaultCallback;
import com.github.kubatatami.judonetworking.exceptions.JudoException;
import com.github.kubatatami.judonetworking.internals.MethodInfo;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.functions.Cancellable;

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
        Type resultType = ((ParameterizedType) returnType).getActualTypeArguments()[0];
        if (resultType instanceof ParameterizedType && ((ParameterizedType) resultType).getRawType().equals(RxRequestStatus.class)) {
            RxCallback callback = new RxCallback(true);
            return new MethodInfo(callback, ((ParameterizedType) resultType).getActualTypeArguments()[0],
                    args, prepareReturnObject(callback, returnType));
        } else {
            RxCallback callback = new RxCallback(false);
            return new MethodInfo(callback, resultType, args, prepareReturnObject(callback, returnType));
        }

    }

    private Object prepareReturnObject(RxCallback source, Type observableType) {
        if (observableType.equals(Observable.class)) {
            return Observable.create(source);
        } else {
            return Single.create(source);
        }
    }

    private static class RxCallback extends DefaultCallback implements ObservableOnSubscribe, SingleOnSubscribe {

        private ObservableEmitter emitter;
        private SingleEmitter singleEmitter;
        private boolean fullRequest;

        private RxCallback(boolean fullRequest) {
            this.fullRequest = fullRequest;
        }

        @Override
        public void onSuccess(Object result) {
            if (fullRequest) result = new RxRequestStatus(getAsyncResult(), result);
            if (emitter != null) emitter.onNext(result);
            else if (singleEmitter != null) singleEmitter.onSuccess(result);
        }

        @Override
        public void onError(JudoException e) {
            if (emitter != null) emitter.tryOnError(e);
            else if (singleEmitter != null) singleEmitter.tryOnError(e);
        }

        @Override
        public void onProgress(int progress) {
            if (emitter != null && fullRequest) emitter.onNext(new RxRequestStatus(getAsyncResult(), progress));
        }

        @Override
        public void onFinish() {
            if (emitter != null) emitter.onComplete();
        }

        @Override
        public void subscribe(ObservableEmitter emitter) {
            this.emitter = emitter;
            emitter.setCancellable(new Cancellable() {
                @Override
                public void cancel() {
                    if (getAsyncResult() != null) getAsyncResult().cancel();
                }
            });
        }

        @Override
        public void subscribe(SingleEmitter emitter) {
            this.singleEmitter = emitter;
            emitter.setCancellable(new Cancellable() {
                @Override
                public void cancel() {
                    if (getAsyncResult() != null) getAsyncResult().cancel();
                }
            });
        }
    }
}
