package com.github.kubatatami.judonetworking.fragments;

import android.support.v4.app.Fragment;

import com.github.kubatatami.judonetworking.callbacks.Callback;
import com.github.kubatatami.judonetworking.callbacks.SupportFragmentCallback;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Kuba on 01/07/15.
 */
public class JudoSupportFragment extends Fragment {

    private static final Map<String, Map<Integer, SupportFragmentCallback<?>>> callbacksMap = new HashMap<>();

    private String mWho;

    public String getWho() {
        if (mWho == null) {
            try {
                Field whoFiled = Fragment.class.getDeclaredField("mWho");
                whoFiled.setAccessible(true);
                mWho = (String) whoFiled.get(this);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return mWho;
    }

    protected boolean connectCallback(int id, Callback<?> callback) {
        if (callbacksMap.containsKey(getWho())) {
            Map<Integer, SupportFragmentCallback<?>> fragmentCallbackMap = callbacksMap.get(getWho());
            if (fragmentCallbackMap.containsKey(id)) {
                fragmentCallbackMap.get(id).setCallback(callback);
                return true;
            }
        }
        return false;
    }

    public static void addCallback(String who, int id, SupportFragmentCallback<?> supportFragmentCallback) {
        if (!callbacksMap.containsKey(who)) {
            callbacksMap.put(who, new HashMap<Integer, SupportFragmentCallback<?>>());
        }
        Map<Integer, SupportFragmentCallback<?>> fragmentCallbackMap = callbacksMap.get(who);
        if (fragmentCallbackMap.containsKey(id)) {
            fragmentCallbackMap.get(id).tryCancel();
        }
        fragmentCallbackMap.put(id, supportFragmentCallback);
    }

    public static void removeCallback(String who, int id) {
        if (callbacksMap.containsKey(who)) {
            Map<Integer, SupportFragmentCallback<?>> fragmentCallbackMap = callbacksMap.get(who);
            if (fragmentCallbackMap.containsKey(id)) {
                fragmentCallbackMap.remove(id);
            }
        }
    }


}