package com.github.kubatatami.judonetworking.adapters;

import com.github.kubatatami.judonetworking.callbacks.DefaultCallback;
import com.github.kubatatami.judonetworking.exceptions.JudoException;
import com.github.kubatatami.judonetworking.internals.MethodInfo;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
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
        return type.equals(Observable.class) || type.equals(Single.class) || type.equals(Completable.class);
    }

    @Override
    public MethodInfo getMethodInfo(Type returnType, Object[] args, Type[] types, Runnable run) {
        Type resultType = ((ParameterizedType) returnType).getActualTypeArguments()[0];
        if (resultType instanceof ParameterizedType && ((ParameterizedType) resultType).getRawType().equals(RxRequestStatus.class)) {
            RxCallback callback = new RxCallback(true, run);
            return new MethodInfo(callback, ((ParameterizedType) resultType).getActualTypeArguments()[0],
                    args, prepareReturnObject(callback, returnType), false);
        } else {
            RxCallback callback = new RxCallback(false, run);
            return new MethodInfo(callback, resultType, args, prepareReturnObject(callback, returnType), false);
        }

    }

    private Object prepareReturnObject(RxCallback source, Type observableType) {
        if (observableType.equals(Observable.class)) {
            return Observable.create(source);
        } else if (observableType.equals(Single.class)) {
            return Single.create(source);
        } else {
            return Completable.create(source);
        }
    }

    private static class RxCallback extends DefaultCallback implements ObservableOnSubscribe,
            SingleOnSubscribe, CompletableOnSubscribe {

        private ObservableEmitter emitter;
        private SingleEmitter singleEmitter;
        private CompletableEmitter completableEmitter;
        private boolean fullRequest;
        private Runnable run;

        private RxCallback(boolean fullRequest, Runnable run) {
            this.fullRequest = fullRequest;
            this.run = run;
        }

        @Override
        public void onSuccess(Object result) {
            if (fullRequest) result = new RxRequestStatus(getAsyncResult(), result);
            if (emitter != null) emitter.onNext(result);
            else if (singleEmitter != null) singleEmitter.onSuccess(result);
            else if (completableEmitter != null) completableEmitter.onComplete();
        }

        @Override
        public void onError(JudoException e) {
            if (emitter != null) emitter.tryOnError(e);
            else if (singleEmitter != null) singleEmitter.tryOnError(e);
            else if (completableEmitter != null) completableEmitter.tryOnError(e);
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
            run.run();
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
            run.run();
        }

        @Override
        public void subscribe(CompletableEmitter emitter) {
            this.completableEmitter = emitter;
            emitter.setCancellable(new Cancellable() {
                @Override
                public void cancel() {
                    if (getAsyncResult() != null) getAsyncResult().cancel();
                }
            });
            run.run();
        }
    }
}
