package com.github.kubatatami.judonetworking.callbacks;

import com.github.kubatatami.judonetworking.AsyncResult;

import java.util.HashMap;
import java.util.Map;

public class CallbackCache {

    private final int hash;

    private AsyncResultCallback callback;

    private static final Map<Integer, AsyncResultCallback> itemCache = new HashMap<>();

    public CallbackCache(Object item, AsyncResultCallback callback) {
        cancelRequest(item);
        this.callback = callback;
        this.hash = item.hashCode();
        itemCache.put(hash, callback);
    }

    public boolean consume() {
        if (validCallback()) {
            itemCache.remove(hash);
            return true;
        }
        return false;
    }

    public boolean cancel() {
        if (!validCallback()) {
            callback.getAsyncResult().cancel();
            return true;
        }
        return false;
    }

    public static void cancelRequest(Object item) {
        int itemHash = item.hashCode();
        if (itemCache.containsKey(itemHash)) {
            AsyncResult asyncResult = itemCache.get(itemHash).getAsyncResult();
            if (asyncResult != null) {
                asyncResult.cancel();
            }
            itemCache.remove(itemHash);
        }
    }

    private boolean validCallback() {
        return callback.equals(itemCache.get(hash));
    }
}
