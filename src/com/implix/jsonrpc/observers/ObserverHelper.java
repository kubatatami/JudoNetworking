package com.implix.jsonrpc.observers;

import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 08.04.2013
 * Time: 22:23
 * To change this template use File | Settings | File Templates.
 */
public class ObserverHelper {
    private List<Pair<ObservableWrapper, WrapObserver>> regObservers = new ArrayList<Pair<ObservableWrapper, WrapObserver>>();
    private Object observableObject;

    @SuppressWarnings("unchecked")
    public void start(final Object object, View view)
    {
        regObservers.clear();
        if (JsonObserver.dataObject != null) {
            observableObject = JsonObserver.dataObject;
            findViewObserver(view);
            findDataObserver(object);
        }
    }

    private void findViewObserver(View view)
    {
       if(view instanceof ViewGroup)
       {
           ViewGroup group = (ViewGroup) view;
           for(int i=0; i< group.getChildCount(); i++)
           {
               View viewElem=group.getChildAt(i);
               findViewObserver(viewElem);
           }
       }
       else
       {
           linkViewObserver(view);
       }
    }

    private void linkViewObserver(View view)
    {

    }

    @SuppressWarnings("unchecked")
    private void findDataObserver(final Object object)
    {
        for (final Method method : object.getClass().getMethods()) {
            if (method.isAnnotationPresent(DataObserver.class)) {

                ObservableWrapper wrapper = getObservable(method);
                WrapObserver observer = new WrapObserver() {
                    @Override
                    public void update(Object data) {
                        try {
                            method.invoke(object, data);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                };

                wrapper.addObserver(observer);
                regObservers.add(new Pair<ObservableWrapper, WrapObserver>(wrapper, observer));
            }
        }
    }

    private Field getField(String fieldName) {
        Class<?> objectClass = observableObject.getClass();
        Field field = null;
        while(objectClass !=null && field==null)
        {
            try {
                field = objectClass.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                objectClass=objectClass.getSuperclass();
            }
        }
        if(field!=null)
        {
            field.setAccessible(true);
        }
        else
        {
            throw new RuntimeException(new NoSuchFieldException(fieldName));
        }
        return field;
    }

    private ObservableWrapper getObservable(Method method) {
        final String convention = "Changed";
        DataObserver ann = method.getAnnotation(DataObserver.class);
        try {
            if (!ann.fieldName().equals("")) {
                return (ObservableWrapper) getField(ann.fieldName()).get(observableObject);
            }

            String methodName = method.getName();
            if (methodName.length() > convention.length() + 1) {
                String fieldName = methodName.substring(0, methodName.length() - convention.length());
                return (ObservableWrapper) getField(fieldName).get(observableObject);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public void stop() {
        for (Pair<ObservableWrapper, WrapObserver> pair : regObservers) {
            pair.first.deleteObserver(pair.second);
        }
    }
}
