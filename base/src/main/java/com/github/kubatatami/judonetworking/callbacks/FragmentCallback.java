package com.github.kubatatami.judonetworking.callbacks;

import com.github.kubatatami.judonetworking.AsyncResult;
import com.github.kubatatami.judonetworking.CacheInfo;
import com.github.kubatatami.judonetworking.builders.DefaultCallbackBuilder;
import com.github.kubatatami.judonetworking.exceptions.JudoException;
import com.github.kubatatami.judonetworking.fragments.ViewStateFragment;

import java.lang.ref.WeakReference;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 23.04.2013
 * Time: 11:40
 */
public class FragmentCallback<T> extends DefaultCallbackBuilder.LambdaCallback<T> {

    private final WeakReference<ViewStateFragment> fragment;

    public FragmentCallback(ViewStateFragment fragment) {
        this.fragment = new WeakReference<>(fragment);
    }

    public FragmentCallback(Builder<T> builder) {
        super(builder);
        this.fragment = new WeakReference<>(builder.fragment);
    }

    @Override
    public final void onStart(CacheInfo cacheInfo, AsyncResult asyncResult) {
        if (isFragmentActive()) {
            super.onStart(cacheInfo, asyncResult);
        } else {
            tryCancel();
        }
    }

    private boolean isFragmentActive() {
        return fragment.get() != null && fragment.get().getActivity() != null && !fragment.get().isViewDestroyed();
    }

    @Override
    public final void onSuccess(T result) {
        if (isFragmentActive()) {
            super.onSuccess(result);
        } else {
            tryCancel();
        }
    }

    @Override
    public final void onError(JudoException e) {
        if (isFragmentActive()) {
            super.onError(e);
        } else {
            tryCancel();
        }
    }

    @Override
    public final void onProgress(int progress) {
        if (isFragmentActive()) {
            super.onProgress(progress);
        } else {
            tryCancel();
        }
    }

    @Override
    public final void onFinish() {
        if (isFragmentActive()) {
            super.onFinish();
        } else {
            tryCancel();
        }
    }

    protected void tryCancel() {
        if (getAsyncResult() != null) {
            getAsyncResult().cancel();
        }
    }

    public static <T> Builder<T> builder(ViewStateFragment fragment) {
        return new Builder<>(fragment);
    }

    public static class Builder<T> extends DefaultCallbackBuilder<T, Builder<T>> {

        private ViewStateFragment fragment;

        public Builder(ViewStateFragment fragment) {
            this.fragment = fragment;
        }

        @Override
        public FragmentCallback<T> build() {
            return new FragmentCallback<>(this);
        }
    }

}
