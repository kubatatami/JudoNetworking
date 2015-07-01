package com.github.kubatatami.judonetworking.loaders;

import android.support.v4.app.LoaderManager;

/**
 * Created by Kuba on 01/07/15.
 */
public class LoaderHelper {

    public static void connect(LoaderManager manager, int id,  LoaderManager.LoaderCallbacks<?> callback){
        if(manager.getLoader(id)!=null){
            manager.initLoader(id, null,callback);
        }
    }

    public static void start(LoaderManager manager, int id,  LoaderManager.LoaderCallbacks<?> callback){
        manager.restartLoader(id, null,callback);
    }

}
