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
    public JsonCacheResult get(String method, Object params[], int cacheLifeTime, int cacheSize, boolean persist) {
        JsonCacheResult result = new JsonCacheResult();
        Integer hash = Arrays.deepHashCode(params);
        if (cache.containsKey(method)) {
            JsonCacheObject cacheObject = cache.get(method).get(hash);
            if (cacheObject != null) {
                if (cacheLifeTime == 0 || System.currentTimeMillis() - cacheObject.createTime < cacheLifeTime) {
                    if ((debugFlags & JsonRpc.CACHE_DEBUG) > 0) {
                        JsonLoggerImpl.log("Cache(" + method + "): Get from memory cache.");
                    }
                    result.object=cacheObject.getObject();
                    result.result=true;
                    return result;
                }
            }
        }

        if (persist) {
            result = loadObject(method + hash, cacheLifeTime);
            if (result.result) {
                JsonLoggerImpl.log("Cache(" + method + "): Get from disc cache.");
                put(method, params, result.object, cacheSize, false);
            }
        }
        return result;
    }

    @Override
    public void put(String method, Object params[], Object object, int cacheSize, boolean persist) {
        if (!cache.containsKey(method)) {
            cache.put(method, new LruCache<Integer, JsonCacheObject>(cacheSize));
        }
        Integer hash = Arrays.deepHashCode(params);
        cache.get(method).put(hash, new JsonCacheObject(System.currentTimeMillis(), object));
        if ((debugFlags & JsonRpc.CACHE_DEBUG) > 0) {
            JsonLoggerImpl.log("Cache(" + method + "): Saved in memory cache.");
        }

        if (persist) {
            JsonLoggerImpl.log("Cache(" + method + "): Saved in disc cache.");
            saveObject(method + hash, object);
        }
    }

    public JsonCacheResult loadObject(String hash, int cacheLifeTime) {
        JsonCacheResult result = new JsonCacheResult();
        ObjectInputStream os = null;
        FileInputStream fileStream = null;
        File file = new File(context.getCacheDir(), hash);
        if (file.exists()) {
            if (cacheLifeTime == 0 || System.currentTimeMillis() - file.lastModified() < cacheLifeTime) {
                try {
                    fileStream = new FileInputStream(file);
                    os = new ObjectInputStream(fileStream);
                    result.object=os.readObject();
                    result.result=true;
                    return result;
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
        result.result=false;
        return result;
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

        for (File file : context.getCacheDir().listFiles()) {
            file.delete();
        }
    }

    @Override
    public void clearCache(String method) {
        if (cache.containsKey(method)) {
            cache.remove(method);
        }

        for (File file : context.getCacheDir().listFiles()) {
            if(file.getName().contains(method))
            {
                file.delete();
            }
        }

    }

    @Override
    public void clearCache(String method, Object... params) {
        if (cache.containsKey(method)) {
            Integer hash = Arrays.deepHashCode(params);
            if (cache.get(method).get(hash) != null) {
                cache.get(method).remove(hash);
            }

            File file = new File(context.getCacheDir(), method + hash);
            if(file.exists())
            {
                file.delete();
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
