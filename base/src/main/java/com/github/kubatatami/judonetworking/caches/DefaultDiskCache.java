package com.github.kubatatami.judonetworking.caches;

import android.content.Context;

import com.github.kubatatami.judonetworking.Endpoint;
import com.github.kubatatami.judonetworking.annotations.LocalCache;
import com.github.kubatatami.judonetworking.internals.cache.CacheMethod;
import com.github.kubatatami.judonetworking.internals.results.CacheResult;
import com.github.kubatatami.judonetworking.logs.JudoLogger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 07.03.2013
 * Time: 08:05
 */
public class DefaultDiskCache implements DiskCache {

    private int debugFlags;

    protected Context context;

    public DefaultDiskCache(Context context) {
        this.context = context;
    }


    @Override
    public CacheResult get(CacheMethod method, String hash, int cacheLifeTime) {
        return loadObject(method, hash, cacheLifeTime);
    }

    @Override
    public void put(CacheMethod method, String hash, Object object, int cacheSize) {


        try {
            File dir = getCacheDir(method);
            File file = new File(getCacheDir(method), hash + "");
            if (cacheSize > 0) {
                trimToSize(dir, cacheSize);
            }
            ObjectOutputStream os = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
            os.writeObject(new CacheResult(object, true, method.getTime(), method.getHash()));
            os.flush();
            os.close();
            if ((debugFlags & Endpoint.CACHE_DEBUG) > 0) {
                JudoLogger.log("Cache(" + method + "): Saved in disk cache " + file.getAbsolutePath() + ".", JudoLogger.LogLevel.DEBUG);
            }
        } catch (IOException e) {
            JudoLogger.log(e);
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
        File file = getLocalCacheDir(LocalCache.CacheLevel.DISK_CACHE);
        delete(file);
        file = getDynamicCacheDir(LocalCache.CacheLevel.DISK_CACHE);
        delete(file);
        file = getLocalCacheDir(LocalCache.CacheLevel.DISK_DATA);
        delete(file);
        file = getDynamicCacheDir(LocalCache.CacheLevel.DISK_DATA);
        delete(file);
    }

    @Override
    public void clearCache(CacheMethod method) {
        File file = getCacheDir(method);
        delete(file);
    }

    @Override
    public void clearCache(CacheMethod method, Object... params) {
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


    private CacheResult loadObject(CacheMethod method, String hash, int cacheLifeTime) {
        CacheResult result;
        ObjectInputStream os = null;
        InputStream fileStream;
        File file = new File(getCacheDir(method), hash + "");

        if ((debugFlags & Endpoint.CACHE_DEBUG) > 0) {
            JudoLogger.log("Cache(" + method + "): Search in disk cache " + file.getAbsolutePath() + ".", JudoLogger.LogLevel.DEBUG);
        }

        if (file.exists()) {
            if (cacheLifeTime == 0 || System.currentTimeMillis() - file.lastModified() < cacheLifeTime) {
                try {
                    fileStream = new BufferedInputStream(new FileInputStream(file));
                    os = new ObjectInputStream(fileStream);
                    result = (CacheResult) os.readObject();
                    if ((debugFlags & Endpoint.CACHE_DEBUG) > 0) {
                        JudoLogger.log("Cache(" + method + "): Get from disk cache " + file.getAbsolutePath() + ".", JudoLogger.LogLevel.DEBUG);
                    }
                    return result;
                } catch (Exception e) {
                    JudoLogger.log(e);
                } finally {
                    if (os != null) {
                        try {
                            os.close();
                        } catch (IOException e) {
                            JudoLogger.log(e);
                        }
                    }
                }
            } else {
                file.delete();
            }
        }
        result = new CacheResult();
        result.result = false;
        return result;
    }


    private File getLocalCacheDir(LocalCache.CacheLevel cacheLevel) {
        File file = new File(getRootDir(cacheLevel) + "/cache/local/");
        file.mkdirs();
        return file;
    }

    private File getDynamicCacheDir(LocalCache.CacheLevel cacheLevel) {
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


    private String getRootDir(LocalCache.CacheLevel cacheLevel) {
        return ((cacheLevel == LocalCache.CacheLevel.DISK_CACHE) ? context.getCacheDir() : context.getFilesDir()) + "";
    }

    private File getCacheDir(CacheMethod method) {
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
        name += method.getInterfaceName() + "/";
        name += method.getUrl().hashCode() + "/";
        name += method.getMethodId() + "/";

        File file = new File(name);
        file.mkdirs();
        return file;
    }

}
