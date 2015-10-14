package com.github.kubatatami.judonetworking.internals;

import com.github.kubatatami.judonetworking.Request;
import com.github.kubatatami.judonetworking.controllers.ProtocolController;
import com.github.kubatatami.judonetworking.logs.ErrorLogger;

/**
 * Created by Kuba on 09/04/14.
 */
public interface EndpointBase {

    /**
     * @param connectionTimeout
     * @param methodTimeout
     */
    void setTimeouts(int connectionTimeout, int methodTimeout);

    /**
     * @param alwaysMainThread
     */
    void setCallbackThread(boolean alwaysMainThread);

    /**
     * @param flags
     */
    void setDebugFlags(int flags);

    int getDebugFlags();

    /**
     * @param delay
     */
    void setDelay(int delay);

    void addErrorLogger(ErrorLogger logger);

    void removeErrorLogger(ErrorLogger logger);

    void setOnRequestEventListener(OnRequestEventListener listener);


    void setPercentLoss(float percentLoss);

    int getThreadPriority();

    void setThreadPriority(int threadPriority);

    /**
     * @param onlyInDebugMode
     */
    void startTest(boolean onlyInDebugMode, String name, int revision);

    /**
     *
     */
    void stopTest();

    ProtocolController getProtocolController();

    void setVerifyResultModel(boolean enabled);

    boolean isProcessingMethod();

    void setProcessingMethod(boolean enabled);

    void setUrlModifier(UrlModifier urlModifier);

    /**
     * No log.
     */
    int NO_DEBUG = 0;

    /**
     * Log time of requests.
     */
    int TIME_DEBUG = 1;

    /**
     * Log request content.
     */
    int REQUEST_DEBUG = 2;

    /**
     * Log response content.
     */
    int RESPONSE_DEBUG = 4;

    /**
     * Log cache behavior.
     */
    int CACHE_DEBUG = 8;

    /**
     * Log request code line.
     */
    int REQUEST_LINE_DEBUG = 16;

    /**
     * Log request and response headers.
     */
    int HEADERS_DEBUG = 32;

    /**
     * Log request errors
     */
    int ERROR_DEBUG = 64;

    /**
     * Log cancellations
     */
    int CANCEL_DEBUG = 128;

    /**
     * Log cancellations
     */
    int THREAD_DEBUG = 256;

    /**
     * Log everything.
     */
    int FULL_DEBUG = TIME_DEBUG | REQUEST_DEBUG | RESPONSE_DEBUG | CACHE_DEBUG | REQUEST_LINE_DEBUG | HEADERS_DEBUG | ERROR_DEBUG | CANCEL_DEBUG;

    int INTERNAL_DEBUG = FULL_DEBUG | THREAD_DEBUG;

    /**
     * Created by Kuba on 17/07/14.
     */
    interface UrlModifier {

        String createUrl(String url);

    }

    interface OnRequestEventListener {

        void onStart(Request request, int requestsCount);

        void onStop(Request request, int requestsCount);

    }
}
