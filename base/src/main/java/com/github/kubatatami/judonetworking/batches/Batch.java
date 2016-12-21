package com.github.kubatatami.judonetworking.batches;

import com.github.kubatatami.judonetworking.AsyncResult;
import com.github.kubatatami.judonetworking.builders.DefaultBatchBuilder;
import com.github.kubatatami.judonetworking.callbacks.BaseCallback;
import com.github.kubatatami.judonetworking.callbacks.MergeCallback;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 11.02.2013
 * Time: 22:48
 */
public interface Batch<T> extends BaseCallback<Object[]> {

    void onStart(AsyncResult asyncResult);

    void run(final T api);

    void runNonFatal(final T api);

    class Builder<T> extends DefaultBatchBuilder<T, Builder<T>> {

        public Builder() {
        }

        public Builder(MergeCallback<?> mergeCallback) {
            super(mergeCallback);
        }
    }
}
