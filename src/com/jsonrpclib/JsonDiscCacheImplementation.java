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
    public JsonCacheResult get(JsonCacheMethod method, Object params[], int cacheLifeTime, int cacheSize) {
        Integer hash = Arrays.deepHashCode(params);
        return loadObject(method, hash, cacheLifeTime);
    }

    @Override
    public void put(JsonCacheMethod method, Object params[], Object object, int cacheSize) {

        Integer hash = Arrays.deepHashCode(params);
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
        File file = getCacheDir();
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


    private JsonCacheResult loadObject(JsonCacheMethod method, int hash, int cacheLifeTime) {
        JsonCacheResult result = new JsonCacheResult();
        ObjectInputStream os = null;
        FileInputStream fileStream = null;
        File file = new File(getCacheDir(method), hash + "");

        if ((debugFlags & JsonRpc.CACHE_DEBUG) > 0) {
            JsonLoggerImpl.log("Cache(" + method + "): Search in disc cache " + file.getAbsolutePath() + ".");
        }

        if (file.exists()) {
            if (cacheLifeTime == 0 || System.currentTimeMillis() - file.lastModified() < cacheLifeTime) {
                try {
                    fileStream = new FileInputStream(file);
                    os = new ObjectInputStream(fileStream);
                    result.object = os.readObject();
                    result.result = true;
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
        result.result = false;
        return result;
    }


    private void saveObject(JsonCacheMethod method, int hash, Object object) {
        try {
            File file = new File(getCacheDir(method), hash + "");
            ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(file));
            os.writeObject(object);
            os.flush();
            os.close();
            if ((debugFlags & JsonRpc.CACHE_DEBUG) > 0) {
                JsonLoggerImpl.log("Cache(" + method + "): Saved in disc cache " + file.getAbsolutePath() + ".");
            }
        } catch (IOException e) {
            JsonLoggerImpl.log(e);
        }

    }

    private File getCacheDir() {
        File file = new File(context.getCacheDir() + "/cache/production/");
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
        if (method.getTest() != null) {
            name += "tests/" + method.getTest() + "/" + method.getTestRevision() + "/";
        } else {
            name += "production/";
        }
        name += method.getMethod().getDeclaringClass().getSimpleName() + "/";
        name += method.getUrl().hashCode() + "/";
        name += method.getMethod().getName() + "/";

        File file = new File(name);
        file.mkdirs();
        return file;
    }

}
