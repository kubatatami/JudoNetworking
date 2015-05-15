package com.github.kubatatami.judonetworking.observers;

import com.github.kubatatami.judonetworking.exceptions.JudoException;

/**
 * Created by Kuba on 01/07/14.
 */
public interface ObserverErrorLogger {

    void onError(JudoException e);

}
