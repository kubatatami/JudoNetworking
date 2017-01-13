package com.github.kubatatami.judonetworking.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.kubatatami.judonetworking.exceptions.JudoException;
import com.github.kubatatami.judonetworking.observers.ExceptionHandler;
import com.github.kubatatami.judonetworking.observers.HolderCallback;
import com.github.kubatatami.judonetworking.observers.HolderView;
import com.github.kubatatami.judonetworking.utils.ReflectionCache;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 25.10.2013
 * Time: 10:15
 * To change this template use File | Settings | File Templates.
 */
public class ObserverAdapterHelper {

    protected Context context;
    protected LayoutInflater layoutInflater;

    public ObserverAdapterHelper(Context context) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
    }

    public View getView(int layout, View convertView, ViewGroup parent) {
        return getView(layout, convertView, parent, null);
    }

    public static boolean isInnerClass(Class<?> clazz) {
        return clazz.isMemberClass() && !Modifier.isStatic(clazz.getModifiers());
    }

    @SuppressWarnings("unchecked")
    public View getView(int layout, View convertView, ViewGroup parent, Class<?> holderClass) {
        try {
            if (convertView == null) {
                convertView = layoutInflater.inflate(layout, parent, false);
                if (holderClass != null) {
                    if (isInnerClass(holderClass)) {
                        throw new JudoException("Inner holder class must be static!");
                    }
                    Constructor<?> constructor = holderClass.getDeclaredConstructors()[0];
                    constructor.setAccessible(true);
                    Object holder = constructor.newInstance();
                    for (Field field : holderClass.getDeclaredFields()) {
                        HolderView viewById = ReflectionCache.getAnnotation(field, HolderView.class);
                        if (viewById != null) {
                            field.setAccessible(true);
                            int res;
                            if (viewById.value() != 0) {
                                res = viewById.value();
                            } else if (!viewById.resName().equals("")) {
                                res = context.getResources().getIdentifier(viewById.resName(), "id", context.getPackageName());
                            } else {
                                res = context.getResources().getIdentifier(field.getName(), "id", context.getPackageName());
                            }
                            field.set(holder, convertView.findViewById(res));
                        }
                        HolderCallback holderCallback = ReflectionCache.getAnnotation(field, HolderCallback.class);
                        if (holderCallback != null) {
                            field.setAccessible(true);
                            View view = convertView.findViewById(holderCallback.value());
                            Object callback = field.getType().getConstructor(View.class).newInstance(view);
                            field.set(holder, callback);
                        }
                    }
                    convertView.setTag(holder);
                }
            }

        } catch (Exception e) {
            ExceptionHandler.throwRuntimeException(e);
        }
        return convertView;
    }
}
