package com.implix.jsonrpc.observers;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Pair;
import android.view.View;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 27.02.2013
 * Time: 16:39
 * To change this template use File | Settings | File Templates.
 */
public class ObserverFragment extends Fragment {

    private List<Pair<ObservableWrapper, WrapObserver>> regObservers = new ArrayList<Pair<ObservableWrapper, WrapObserver>>();
    private Object observableObject;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        regObservers.clear();


        if (JsonObserver.dataObject != null) {
            observableObject = JsonObserver.dataObject;
            for (final Method method : getClass().getMethods()) {
                if (method.isAnnotationPresent(DataObserver.class)) {

                    ObservableWrapper wrapper = getObservable(method);
                    WrapObserver observer = new WrapObserver() {
                        @Override
                        public void update(Object data) {
                            try {
                                method.invoke(ObserverFragment.this, data);
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

    @Override
    public void onDestroyView() {
        for (Pair<ObservableWrapper, WrapObserver> pair : regObservers) {
            pair.first.deleteObserver(pair.second);
        }
        super.onDestroyView();
    }
}
