package com.jsonrpclib.observers;

import android.content.Context;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private static final Pattern pattern = Pattern.compile("\\[[^\\]]*\\]");
    private static final String convention = "Changed";
    private final Class<?> resources;
    private Context context;

    public ObserverHelper(Context context) {
        this.context = context;
        String packageName = context.getApplicationContext().getPackageName();
        try {
            resources = Class.forName(packageName+".R");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

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
            final String tag = (String) view.getTag();

            final List<ObservableWrapper> results = findObservablesByTag(tag);

            WrapObserver observer = new WrapObserver() {
                @Override
                public void update(Object data) {
                    try {
                        if (view instanceof TextView) {
                            TextView textView = (TextView) view;

                            String result = buildResult(tag.substring(1, tag.length() - 1));
                            textView.setText(result);

                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            };

            for (ObservableWrapper result : results) {
                result.addObserver(observer);
                regObservers.add(new Pair<ObservableWrapper, WrapObserver>(result, observer));
            }

        }
    }

    private String buildResult(String tag) throws IllegalAccessException, NoSuchFieldException {
        Matcher matcher = pattern.matcher(tag);

        while (matcher.find()) {
            String res = matcher.group(0);
            String key = res.substring(1, res.length() - 1);
            if (key.substring(0, 1).equals(".")) {
                tag = tag.replace(res, getFieldFromObserver(key, observableObject).toString());
            } else if (key.substring(0, 8).equals("@string/")) {
                tag = tag.replace(res, getStringResource(key.substring(8)));

            }
        }

        return tag;
    }

    private String getStringResource(String stringName) throws NoSuchFieldException, IllegalAccessException {

        Field f = resources.getField("string");
        f=f.getClass().getField(stringName);
        return context.getString(f.getInt(null));
    }

    private List<ObservableWrapper> findObservablesByTag(String tag) throws Exception {
        List<ObservableWrapper> list = new ArrayList<ObservableWrapper>();
        if (tag.matches("\\[.*\\]")) {
            tag = tag.substring(1, tag.length() - 1);
            Matcher matcher = pattern.matcher(tag);

            while (matcher.find()) {
                String res = matcher.group(0);
                res = res.substring(1, res.length() - 1);
                if (res.substring(0, 1).equals(".")) {
                    String fields[] = res.split(splitter);
                    list.add((ObservableWrapper) getField(fields[1]).get(observableObject));
                }
            }

        }
        return list;
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

    private Object getFieldFromObserver(String fieldName, Object object) throws IllegalAccessException {
        String fields[] = fieldName.split(splitter);
        ObservableWrapper observableWrapper = (ObservableWrapper) getField(fields[1]).get(object);
        Object data = observableWrapper.get();
        if(data==null)
        {
            return "";
        }
        else
        {
            return getFieldValue(fieldName.substring(fields[1].length()+2), data);
        }
    }

    private Object getFieldValue(String fieldName, Object object) throws IllegalAccessException {
        String parts[] = fieldName.split(splitter);
        Class<?> clazz;
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
