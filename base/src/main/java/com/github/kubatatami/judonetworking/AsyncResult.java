package com.github.kubatatami.judonetworking;

/**
 * Created by Kuba on 05/04/14.
 */
public interface AsyncResult {

    boolean isDone();

    boolean isCancelled();

    boolean isRunning();

    void cancel();


}
