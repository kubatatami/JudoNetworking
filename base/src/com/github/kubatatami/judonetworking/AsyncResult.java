package com.github.kubatatami.judonetworking;

/**
 * Created by Kuba on 05/04/14.
 */
public interface AsyncResult {

    public boolean isDone();

    public boolean isCancelled();

    public boolean isRunning();

    public void cancel();


}
