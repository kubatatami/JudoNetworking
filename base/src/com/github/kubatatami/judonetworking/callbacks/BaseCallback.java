package com.github.kubatatami.judonetworking.callbacks;

import com.github.kubatatami.judonetworking.exceptions.JudoException;

/**
 * Created by Kuba on 28/03/15.
 */
public interface BaseCallback<T> {

    void onProgress(int progress);

    void onSuccess(T result);

    void onError(JudoException e);

    void onFinish();

}
