package com.github.kubatatami.judonetworking.stateful;

import com.github.kubatatami.judonetworking.callbacks.BaseCallback;
import com.github.kubatatami.judonetworking.callbacks.Identifiable;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Kuba on 11/10/15.
 */
public class StatefulCache {

    private static final Map<String, Map<Integer, Stateful>> callbacksMap = new HashMap<>();

    public static void addStatefulCallback(String who, int id, Stateful statefulCallback) {
        if (!callbacksMap.containsKey(who)) {
            callbacksMap.put(who, new HashMap<Integer, Stateful>());
        }
        Map<Integer, Stateful> fragmentCallbackMap = callbacksMap.get(who);
        if (fragmentCallbackMap.containsKey(id)) {
            fragmentCallbackMap.get(id).tryCancel();
        }
        fragmentCallbackMap.put(id, statefulCallback);
    }

    public static void endStatefulCallback(String who, int id) {
        if (callbacksMap.containsKey(who)) {
            Map<Integer, Stateful> fragmentCallbackMap = callbacksMap.get(who);
            if (fragmentCallbackMap.containsKey(id)) {
                fragmentCallbackMap.remove(id);
            }
        }
    }


    public static void removeAllControllersCallbacks(String who) {
        if (callbacksMap.containsKey(who)) {
            Map<Integer, Stateful> fragmentCallbackMap = callbacksMap.get(who);
            for (Map.Entry<Integer, Stateful> entry : fragmentCallbackMap.entrySet()) {
                entry.getValue().setCallback(null);
            }
        }
    }

    public static void removeAllStatefulCallbacks(String who) {
        if (callbacksMap.containsKey(who)) {
            Map<Integer, Stateful> fragmentCallbackMap = callbacksMap.get(who);
            for (Map.Entry<Integer, Stateful> entry : fragmentCallbackMap.entrySet()) {
                entry.getValue().tryCancel();
            }
            callbacksMap.remove(who);
        }
    }

    public static boolean connectControllerCallbacks(StatefulController controller, BaseCallback<?>... callbacks) {
        boolean result = false;
        for (BaseCallback<?> callback : callbacks) {
            result |= connectControllerCallback(controller, StatefulCache.getCallbackId(callback), callback);
        }
        return result;
    }

    public static boolean connectControllerCallback(StatefulController controller, int id, BaseCallback<?> callback) {
        if (callbacksMap.containsKey(controller.getWho())) {
            Map<Integer, Stateful> fragmentCallbackMap = callbacksMap.get(controller.getWho());
            if (fragmentCallbackMap.containsKey(id)) {
                fragmentCallbackMap.get(id).setCallback(callback);
                return true;
            }
        }
        return false;
    }

    public static void cancelRequest(StatefulController controller, int id) {
        if (callbacksMap.containsKey(controller.getWho())) {
            Map<Integer, Stateful> fragmentCallbackMap = callbacksMap.get(controller.getWho());
            if (fragmentCallbackMap.containsKey(id)) {
                fragmentCallbackMap.get(id).tryCancel();
            }
        }
    }

    public static int getCallbackId(Object callback) {
        if (callback instanceof Identifiable) {
            return ((Identifiable) callback).getId();
        } else {
            return callback.getClass().hashCode();
        }
    }

    public static int calcHashCode(Object... objects) {
        int result = 0;
        for (Object obj : objects) {
            if (obj != null) {
                result += obj.getClass().hashCode();
            }
        }
        return result;
    }

}
