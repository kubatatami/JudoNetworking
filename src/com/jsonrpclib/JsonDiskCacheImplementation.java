package com.jsonrpclib;

import android.content.Context;

import java.io.*;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 07.03.2013
 * Time: 08:05
 */
class JsonDiskCacheImplementation implements JsonDiskCache {
    private int debugFlags;

    protected Context context;

    public JsonDiskCacheImplementation(Context context) {
        this.context = context;
    }


    @Override
    public JsonCacheResult get(JsonCacheMethod method, String hash, int cacheLifeTime) {
        return loadObject(method, hash, cacheLifeTime);
    }

    @Override
    public void put(JsonCacheMethod method, String hash, Object object, int cacheSize) {


        try {
            File dir = getCacheDir(method);
            File file = new File(getCacheDir(method), hash + "");
            if (cacheSize > 0) {
                trimToSize(dir, cacheSize);
            }
            ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(file));
            os.writeObject(new JsonCacheResult(object, true, method.getTime(), method.getHash()));
            os.flush();
            os.close();
            if ((debugFlags & JsonRpc.CACHE_DEBUG) > 0) {
                JsonLoggerImpl.log("Cache(" + method + "): Saved in disk cache " + file.getAbsolutePath() + ".");
            }
        } catch (IOException e) {
            JsonLoggerImpl.log(e);
        }

    }

    private void trimToSize(File dir, int cacheSize) {
        File[] files = dir.listFiles();
        if (files != null && files.length > cacheSize) {
            Arrays.sort(files, new Comparator<File>() {
                public int compare(File f1, File f2) {
                    return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
                }
            });
            for (int i = 0; i < files.length - cacheSize; i++) {
                files[i].delete();
            }
        }
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
        File file = getLocalCacheDir(JsonLocalCacheLevel.DISK_CACHE);
        delete(file);
        file = getDynamicCacheDir(JsonLocalCacheLevel.DISK_CACHE);
        delete(file);
        file = getLocalCacheDir(JsonLocalCacheLevel.DISK_DATA);
        delete(file);
        file = getDynamicCacheDir(JsonLocalCacheLevel.DISK_DATA);
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
            JsonLoggerImpl.log("Cache(" + method + "): Search in disk cache " + file.getAbsolutePath() + ".");
        }

        if (file.exists()) {
            if (cacheLifeTime == 0 || System.currentTimeMillis() - file.lastModified() < cacheLifeTime) {
                try {
                    fileStream = new FileInputStream(file);
                    os = new ObjectInputStream(fileStream);
                    result = (JsonCacheResult) os.readObject();
                    if ((debugFlags & JsonRpc.CACHE_DEBUG) > 0) {
                        JsonLoggerImpl.log("Cache(" + method + "): Get from disk cache " + file.getAbsolutePath() + ".");
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


    private File getLocalCacheDir(JsonLocalCacheLevel cacheLevel) {
        File file = new File(getRootDir(cacheLevel) + "/cache/local/");
        file.mkdirs();
        return file;
    }

    private File getDynamicCacheDir(JsonLocalCacheLevel cacheLevel) {
        File file = new File(getRootDir(cacheLevel) + "/cache/dynamic/");
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


    private String getRootDir(JsonLocalCacheLevel cacheLevel) {
        return ((cacheLevel == JsonLocalCacheLevel.DISK_CACHE) ? context.getCacheDir() : context.getFilesDir()) + "";
    }

    private File getCacheDir(JsonCacheMethod method) {
        String name = getRootDir(method.getCacheLevel()) + "/cache/";
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
