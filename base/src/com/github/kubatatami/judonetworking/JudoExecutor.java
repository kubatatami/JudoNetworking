package com.github.kubatatami.judonetworking;

import android.os.*;
import android.os.Process;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Kuba on 19/05/14.
 */
public class JudoExecutor extends ThreadPoolExecutor{

    protected int threadPriority = Process.THREAD_PRIORITY_BACKGROUND;
    protected ThreadFactory threadFactory =  new ThreadFactory() {
        @Override
        public Thread newThread(final Runnable runnable) {
            return new Thread(new Runnable() {
                @Override
                public void run() {
                    android.os.Process.setThreadPriority(threadPriority);
                    runnable.run();
                }
            },"JudoNetworking ConnectionPool");
        }
    };

    public JudoExecutor() {
        super(DefaultThreadPoolSizer.DEFAULT_THREAD_COUNT, DefaultThreadPoolSizer.DEFAULT_THREAD_COUNT,
                0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
        setThreadFactory(threadFactory);
    }

    public void setThreadPriority(int threadPriority) {
        this.threadPriority = threadPriority;
    }

    public int getThreadPriority() {
        return threadPriority;
    }
}
