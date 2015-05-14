package com.github.kubatatami.judonetworking.internals.executors;

import android.os.Build;
import android.os.Process;

import com.github.kubatatami.judonetworking.Endpoint;
import com.github.kubatatami.judonetworking.logs.JudoLogger;
import com.github.kubatatami.judonetworking.threads.DefaultThreadPoolSizer;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Kuba on 19/05/14.
 */
public class JudoExecutor extends ThreadPoolExecutor {

    protected int threadPriority = Process.THREAD_PRIORITY_BACKGROUND;
    protected Endpoint endpoint;
    protected int count;

    protected ThreadFactory threadFactory = new ThreadFactory() {
        @Override
        public Thread newThread(final Runnable runnable) {
            count++;
            if ((endpoint.getDebugFlags() & Endpoint.THREAD_DEBUG) > 0) {
                JudoLogger.log("Create thread " + count);
            }

            return new ConnectionThread(runnable, threadPriority, count, endpoint);
        }
    };

    public JudoExecutor(Endpoint endpoint) {
        super(DefaultThreadPoolSizer.DEFAULT_CONNECTIONS, Integer.MAX_VALUE, 30, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
        this.endpoint = endpoint;
        setThreadFactory(threadFactory);
        prestartAllCoreThreads();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            allowCoreThreadTimeOut(true);
        }
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);
        JudoExecutor.ConnectionThread connectionThread = (JudoExecutor.ConnectionThread) t;
        connectionThread.resetCanceled();
        if ((endpoint.getDebugFlags() & Endpoint.THREAD_DEBUG) > 0) {
            JudoLogger.log("Before execute thread " + t.getName() + ":" + toString());
        }
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        if ((endpoint.getDebugFlags() & Endpoint.THREAD_DEBUG) > 0) {
            JudoLogger.log("After thread execute:" + toString());
        }
    }

    @Override
    public void execute(Runnable command) {
        super.execute(command);
        if ((endpoint.getDebugFlags() & Endpoint.THREAD_DEBUG) > 0) {
            JudoLogger.log("Execute runnable" + toString());
        }
    }

    @Override
    public void setCorePoolSize(int corePoolSize) {
        super.setCorePoolSize(corePoolSize);
        if ((endpoint.getDebugFlags() & Endpoint.THREAD_DEBUG) > 0) {
            JudoLogger.log("Core thread pool size:" + corePoolSize);
        }
    }

    @Override
    public void setMaximumPoolSize(int maximumPoolSize) {
        super.setMaximumPoolSize(maximumPoolSize);
        if ((endpoint.getDebugFlags() & Endpoint.THREAD_DEBUG) > 0) {
            JudoLogger.log("Maximum thread pool size:" + maximumPoolSize);
        }
    }

    public void setThreadPriority(int threadPriority) {
        this.threadPriority = threadPriority;
    }

    public int getThreadPriority() {
        return threadPriority;
    }


    public static class ConnectionThread extends Thread {

        Runnable runnable;
        int threadPriority;
        Canceller canceller;
        boolean canceled;
        Endpoint endpoint;

        public ConnectionThread(Runnable runnable, int threadPriority, int count, Endpoint endpoint) {
            super("JudoNetworking ConnectionPool " + count);
            this.runnable = runnable;
            this.endpoint = endpoint;
            this.threadPriority = threadPriority;

        }

        @Override
        public void run() {
            android.os.Process.setThreadPriority(threadPriority);
            runnable.run();
        }


        @Override
        public void interrupt() {
            if ((endpoint.getDebugFlags() & Endpoint.THREAD_DEBUG) > 0) {
                JudoLogger.log("Interrupt task on: " + getName());
            }
            canceled = true;
            super.interrupt();
            if (canceller != null) {
                canceller.cancel();
                canceller = null;
            }
        }

        public void resetCanceled() {
            this.canceled = false;
        }

        public void setCanceller(Canceller canceller) {
            this.canceller = canceller;
        }

        public interface Canceller {
            void cancel();
        }

        public boolean isCanceled() {
            return canceled;
        }
    }
}
