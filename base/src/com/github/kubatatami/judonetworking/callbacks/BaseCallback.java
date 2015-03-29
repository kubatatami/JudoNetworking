package com.github.kubatatami.judonetworking.callbacks;

import com.github.kubatatami.judonetworking.exceptions.JudoException;

/**
 * Created by Kuba on 28/03/15.
 */
public interface BaseCallback<T> {

    public void onProgress(int progress);

    public void onSuccess(T result);

    public void onError(JudoException e);

    public void onFinish();

}
