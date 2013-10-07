package com.jsonrpclib;

import android.content.Context;

import java.io.*;
import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 07.03.2013
 * Time: 08:05
 */
class JsonDiscCacheImplementation implements JsonDiscCache {
    private int debugFlags;

    protected Context context;

    public JsonDiscCacheImplementation(Context context) {
        this.context = context;
    }


    @Override
    public JsonCacheResult get(JsonCacheMethod method, String hash, int cacheLifeTime) {
        return loadObject(method, hash, cacheLifeTime);
    }

    @Override
    public void put(JsonCacheMethod method, String hash, Object object) {
        saveObject(method, hash, object);

    }

    @Override
    public void clearTests() {
        File file = getTestDir();
        delete(file);
    }

    @Override
    public void clearTest(String name) {
        File file = getTestDir(name);
        delete(file);
    }

    @Override
    public void clearCache() {
        File file = getLocalCacheDir();
        delete(file);
        file = getDynamicCacheDir();
        delete(file);
    }

    @Override
    public void clearCache(JsonCacheMethod method) {
        File file = getCacheDir(method);
        delete(file);
    }

    @Override
    public void clearCache(JsonCacheMethod method, Object... params) {
        Integer hash = Arrays.deepHashCode(params);
        File file = getCacheDir(method);
        delete(new File(file, hash.toString()));
    }


    private void delete(File f) {
        if (f.isDirectory()) {
            for (File c : f.listFiles())
                delete(c);
        }
        if (!f.delete())
            throw new RuntimeException(new FileNotFoundException("Failed to delete file: " + f));
    }

    @Override
    public int getDebugFlags() {
        return debugFlags;
    }

    @Override
    public void setDebugFlags(int debugFlags) {
        this.debugFlags = debugFlags;
    }


    private JsonCacheResult loadObject(JsonCacheMethod method, String hash, int cacheLifeTime) {
        JsonCacheResult result;
        ObjectInputStream os = null;
        FileInputStream fileStream;
        File file = new File(getCacheDir(method), hash + "");

        if ((debugFlags & JsonRpc.CACHE_DEBUG) > 0) {
            JsonLoggerImpl.log("Cache(" + method + "): Search in disc cache " + file.getAbsolutePath() + ".");
        }

        if (file.exists()) {
            if (cacheLifeTime == 0 || System.currentTimeMillis() - file.lastModified() < cacheLifeTime) {
                try {
                    fileStream = new FileInputStream(file);
                    os = new ObjectInputStream(fileStream);
                    result = (JsonCacheResult) os.readObject();
                    if ((debugFlags & JsonRpc.CACHE_DEBUG) > 0) {
                        JsonLoggerImpl.log("Cache(" + method + "): Get from disc cache " + file.getAbsolutePath() + ".");
                    }
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
        result = new JsonCacheResult();
        result.result = false;
        return result;
    }


    private void saveObject(JsonCacheMethod method, String hash, Object object) {
        try {
            File file = new File(getCacheDir(method), hash + "");
            ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(file));
            os.writeObject(new JsonCacheResult(object, true, method.getTime(), method.getHash()));
            os.flush();
            os.close();
            if ((debugFlags & JsonRpc.CACHE_DEBUG) > 0) {
                JsonLoggerImpl.log("Cache(" + method + "): Saved in disc cache " + file.getAbsolutePath() + ".");
            }
        } catch (IOException e) {
            JsonLoggerImpl.log(e);
        }

    }

    private File getLocalCacheDir() {
        File file = new File(context.getCacheDir() + "/cache/local/");
        file.mkdirs();
        return file;
    }

    private File getDynamicCacheDir() {
        File file = new File(context.getCacheDir() + "/cache/dynamic/");
        file.mkdirs();
        return file;
    }

    private File getTestDir() {
        File file = new File(context.getCacheDir() + "/cache/tests/");
        file.mkdirs();
        return file;
    }

    private File getTestDir(String name) {
        File file = new File(context.getCacheDir() + "/cache/tests/" + name + "/");
        file.mkdirs();
        return file;
    }

    private File getCacheDir(JsonCacheMethod method) {
        String name = context.getCacheDir() + "/cache/";
        if (method.isDynamic()) {
            name += "dynamic/";
        } else {
            if (method.getTest() != null) {
                name += "tests/" + method.getTest() + "/" + method.getTestRevision() + "/";
            } else {
                name += "local/";
            }
        }
        name += method.getMethod().getDeclaringClass().getSimpleName() + "/";
        name += method.getUrl().hashCode() + "/";
        name += method.getMethod().getName() + "/";

        File file = new File(name);
        file.mkdirs();
        return file;
    }

}
