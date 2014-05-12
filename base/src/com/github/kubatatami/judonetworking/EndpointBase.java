package com.github.kubatatami.judonetworking;

/**
 * Created by Kuba on 09/04/14.
 */
public interface EndpointBase {

    /**
     * @param connectionTimeout
     * @param methodTimeout
     * @param reconnectionAttempts
     */
    public void setTimeouts(int connectionTimeout, int methodTimeout, int reconnectionAttempts);

    /**
     * @param alwaysMainThread
     */
    public void setCallbackThread(boolean alwaysMainThread);

    /**
     * @param flags
     */
    public void setDebugFlags(int flags);

    /**
     * @param delay
     */
    public void setDelay(int delay);

    public void setErrorLogger(ErrorLogger logger);

    public void setPercentLoss(float percentLoss);

    public int getThreadPriority();

    public void setThreadPriority(int threadPriority);

    /**
     * @param onlyInDebugMode
     */
    public void startTest(boolean onlyInDebugMode, String name, int revision);

    public void setIgnoreNullParams(boolean ignoreNullParams);

    /**
     *
     */
    public void stopTest();

    public ProtocolController getProtocolController();

    public void setVerifyResultModel(boolean enabled);

    public boolean isProcessingMethod();

    public void setProcessingMethod(boolean enabled);

    /**
     * No log.
     */
    public static final int NO_DEBUG = 0;
    /**
     * Log time of requests.
     */
    public static final int TIME_DEBUG = 1;
    /**
     * Log request content.
     */
    public static final int REQUEST_DEBUG = 2;
    /**
     * Log response content.
     */
    public static final int RESPONSE_DEBUG = 4;
    /**
     * Log cache behavior.
     */
    public static final int CACHE_DEBUG = 8;
    /**
     * Log request code line.
     */
    public static final int REQUEST_LINE_DEBUG = 16;

    /**
     * Log request and response headers.
     */
    public static final int HEADERS_DEBUG = 32;

    /**
     * Log token behavior
     */
    public static final int TOKEN_DEBUG = 64;

    /**
     * Log request errors
     */
    public static final int ERROR_DEBUG = 128;

    /**
     * Log cancellations
     */
    public static final int CANCEL_DEBUG = 256;

    /**
     * Log everything.
     */
    public static final int FULL_DEBUG = TIME_DEBUG | REQUEST_DEBUG | RESPONSE_DEBUG | CACHE_DEBUG | REQUEST_LINE_DEBUG | HEADERS_DEBUG | TOKEN_DEBUG | ERROR_DEBUG | CANCEL_DEBUG;
}
