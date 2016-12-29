package com.github.kubatatami.judonetworking.builders;

import com.github.kubatatami.judonetworking.batches.Batch;

public interface BatchBuilder<T> {

    Batch<T> build();

}
