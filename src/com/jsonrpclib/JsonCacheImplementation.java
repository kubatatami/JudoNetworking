package com.jsonrpclib;

import android.content.Context;
import android.support.v4.util.LruCache;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 07.03.2013
 * Time: 08:05
 */
class JsonCacheImplementation extends JsonCache {
    private int debugFlags;
    private Map<String, LruCache<Integer, JsonCacheObject>> cache = Collections.synchronizedMap(new HashMap<String, LruCache<Integer, JsonCacheObject>>());


    public JsonCacheImplementation(Context context) {
        super(context);
    }

    @Override
    public Object get(String method, Object params[], int cacheLifeTime, boolean persist) {
        if (cache.containsKey(method)) {
            Integer hash = Arrays.deepHashCode(params);
            JsonCacheObject cacheObject = cache.get(method).get(hash);
            if (cacheObject != null) {
                if (cacheLifeTime == 0 || System.currentTimeMillis() - cacheObject.createTime < cacheLifeTime) {
                    if ((debugFlags & JsonRpc.CACHE_DEBUG) > 0) {
                        JsonLoggerImpl.log("Cache(" + method + "): Get from cache.");
                    }
                    return cacheObject.getObject();
                }
            } else if (persist) {
                return loadObject(hash.toString(), cacheLifeTime);
            }
        }
        return null;
    }

    @Override
    public void put(String method, Object params[], Object object, int cacheSize, boolean persist) {
        if (!cache.containsKey(method)) {
            cache.put(method, new LruCache<Integer, JsonCacheObject>(cacheSize));
        }
        Integer hash = Arrays.deepHashCode(params);
        cache.get(method).put(hash, new JsonCacheObject(System.currentTimeMillis(), object));
        if ((debugFlags & JsonRpc.CACHE_DEBUG) > 0) {
            JsonLoggerImpl.log("Cache(" + method + "): Saved in cache.");
        }

        if (persist) {
            saveObject(hash.toString(), object);
        }
    }

    public Object loadObject(String hash, int cacheLifeTime) {
        ObjectInputStream os = null;
        FileInputStream fileStream = null;
        File file = new File(context.getCacheDir(), hash);
        if (file.exists()) {
            if (cacheLifeTime == 0 || System.currentTimeMillis() - file.lastModified() < cacheLifeTime) {
                try {
                    fileStream = new FileInputStream(file);
                    os = new ObjectInputStream(fileStream);
                    return os.readObject();
                } catch (Exception e) {
                    JsonLoggerImpl.log(e);
                } finally {
                    if (os != null) {
                        try {
                            os.close();
                        } catch (IOException e) {
                            JsonLoggerImpl.log(e);
                        }
                    }
                }
            } else {
                file.delete();
            }
        }
        return null;
    }


    public void saveObject(String hash, Object object) {
        try {
            ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(new File(context.getCacheDir(), hash)));
            os.writeObject(object);
            os.flush();
            os.close();
        } catch (IOException e) {
            JsonLoggerImpl.log(e);
        }

    }

    @Override
    public void clearCache() {
        cache = Collections.synchronizedMap(new HashMap<String, LruCache<Integer, JsonCacheObject>>());
    }

    @Override
    public void clearCache(String method) {
        if (cache.containsKey(method)) {
            cache.remove(method);
        }
    }

    @Override
    public void clearCache(String method, Object... params) {
        if (cache.containsKey(method)) {
            Integer hash = Arrays.deepHashCode(params);
            if (cache.get(method).get(hash) != null) {
                cache.get(method).remove(hash);
            }
        }
    }

    @Override
    public int getDebugFlags() {
        return debugFlags;
    }

    @Override
    public void setDebugFlags(int debugFlags) {
        this.debugFlags = debugFlags;
    }
}
