package com.github.kubatatami.judonetworking.utils;

import android.support.annotation.NonNull;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MoveToHeadThreadPoolExecutor extends ThreadPoolExecutor {

    public MoveToHeadThreadPoolExecutor() {
        this(0, Runtime.getRuntime().availableProcessors());
    }

    public MoveToHeadThreadPoolExecutor(int corePoolSize, int maximumPoolSize) {
        super(corePoolSize, maximumPoolSize, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<Runnable>(), new ThreadFactory() {
            @Override
            public Thread newThread(@NonNull Runnable r) {
                Thread thread = new Thread(r);
                thread.setPriority(Thread.MIN_PRIORITY);
                return thread;
            }
        });
    }

    public boolean moveToHead(Runnable runnable) {
        LinkedBlockingDeque<Runnable> deque = (LinkedBlockingDeque<Runnable>) getQueue();
        if (deque.contains(runnable)) {
            deque.removeFirstOccurrence(runnable);
            deque.addFirst(runnable);
            return true;
        }
        return false;
    }
}