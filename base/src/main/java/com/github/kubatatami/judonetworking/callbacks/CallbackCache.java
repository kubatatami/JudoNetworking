package com.github.kubatatami.judonetworking.callbacks;

import java.util.Map;
import java.util.WeakHashMap;

public class CallbackCache {

    private final int hash;

    private AsyncResultCallback callback;

    private static final Map<Integer, AsyncResultCallback> itemCache = new WeakHashMap<>();

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
        if (itemCache.containsKey(hash) && !validCallback()) {
            callback.getAsyncResult().cancel();
            return true;
        }
        return false;
    }

    public static void cancelRequest(Object item) {
        int itemHash = item.hashCode();
        if (itemCache.containsKey(itemHash)) {
            if (itemCache.get(itemHash).getAsyncResult() != null) {
                itemCache.get(itemHash).getAsyncResult().cancel();
            }
            itemCache.remove(itemHash);
        }
    }

    private boolean validCallback() {
        return callback.equals(itemCache.get(hash));
    }
}
