package com.github.kubatatami.judonetworking;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Kuba on 19/05/14.
 */
public class JudoExecutor extends ThreadPoolExecutor{

    protected int threadPriority = Thread.NORM_PRIORITY - 1;
    protected ThreadFactory threadFactory =  new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r,"JudoNetworking ConnectionPool");
            thread.setPriority(threadPriority);
            return thread;
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
