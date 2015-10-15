package com.github.kubatatami.judonetworking;

import java.util.List;
import java.util.Map;

/**
 * Created by Kuba on 05/04/14.
 */
public interface AsyncResult {

    boolean isDone();

    boolean isCancelled();

    boolean isRunning();

    void cancel();

    void await() throws InterruptedException;

    Map<String, List<String>> getHeaders();
}
