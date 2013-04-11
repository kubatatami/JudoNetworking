package com.implix.jsonrpc.observers;

import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
    private static final String splitter = "\\.";

    @SuppressWarnings("unchecked")
    public void start(final Object object, View view) {
        try {
            regObservers.clear();
            if (JsonObserver.dataObject != null) {
                observableObject = JsonObserver.dataObject;
                findViewObserver(view);
                findDataObserver(object);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void findViewObserver(View view) throws Exception {
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                View viewElem = group.getChildAt(i);
                findViewObserver(viewElem);
            }
        } else {
            linkViewObserver(view);
        }
    }

    @SuppressWarnings("unchecked")
    private void linkViewObserver(final View view) throws Exception {
        if (view.getTag() != null && view.getTag() instanceof String) {
            String tag = (String) view.getTag();

            final Pair<ObservableWrapper, String> result = findObservableByTag(tag);
            if (result != null) {
                WrapObserver observer = new WrapObserver() {
                    @Override
                    public void update(Object data) {
                        try {
                            if (view instanceof TextView) {
                                TextView textView = (TextView) view;
                                if (result.second == null) {
                                    textView.setText(data.toString());
                                } else {
                                    textView.setText(getFieldValue(result.second,data).toString());
                                }
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                };

                result.first.addObserver(observer);
                regObservers.add(new Pair<ObservableWrapper, WrapObserver>(result.first, observer));
            }
        }
    }


    private Pair<ObservableWrapper, String> findObservableByTag(String tag) throws Exception {
        String[] parts = tag.split(splitter);
        if (parts.length > 1 && parts[0].length()==0) {
            ObservableWrapper observableWrapper = (ObservableWrapper) getField(parts[1]).get(observableObject);
            return new Pair<ObservableWrapper, String>(observableWrapper, tag.substring(parts[1].length()+2));
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private void findDataObserver(final Object object) throws Exception {
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

    private Object getFieldValue(String fieldName, Object object) throws IllegalAccessException {
        String parts[] = fieldName.split(splitter);
        Class<?> clazz = null;
        for (String part : parts) {
            clazz = object.getClass();
            object = getField(part, clazz).get(object);
        }
        return object;
    }

    private Field getField(String fieldName) {
        return getField(fieldName, observableObject.getClass());
    }

    private Field getField(String fieldName, Class<?> objectClass) {
        Field field = null;
        while (objectClass != null && field == null) {
            try {
                field = objectClass.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                objectClass = objectClass.getSuperclass();
            }
        }
        if (field != null) {
            field.setAccessible(true);
        } else {
            throw new RuntimeException(new NoSuchFieldException(fieldName));
        }
        return field;
    }

    private ObservableWrapper getObservable(Method method) throws Exception {
        final String convention = "Changed";
        DataObserver ann = method.getAnnotation(DataObserver.class);

        if (!ann.fieldName().equals("")) {
            return (ObservableWrapper) getField(ann.fieldName()).get(observableObject);
        }

        String methodName = method.getName();
        if (methodName.length() > convention.length() + 1) {
            String fieldName = methodName.substring(0, methodName.length() - convention.length());
            return (ObservableWrapper) getField(fieldName).get(observableObject);
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
