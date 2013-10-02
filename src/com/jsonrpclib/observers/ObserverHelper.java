package com.jsonrpclib.observers;

import android.content.Context;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 08.04.2013
 * Time: 22:23
 */
public class ObserverHelper {
    private List<Pair<ObservableWrapper, WrapObserver>> dataObservers = new ArrayList<Pair<ObservableWrapper, WrapObserver>>();
    private Map<View, Pair<ObservableWrapper, WrapObserver>> viewObservers = new HashMap<View, Pair<ObservableWrapper, WrapObserver>>();
    private Object observableObject;
    private static final String splitter = "\\.";
    private static final Pattern pattern = Pattern.compile("\\[[^\\]]*\\]");
    private static final String convention = "Changed";
    private Context context;


    public ObserverHelper(Context context) {
        this.context = context;
    }

    @SuppressWarnings("unchecked")
    public void start(final Object object, View view) {
        dataObservers.clear();
        viewObservers.clear();
        if (JsonObserver.dataObject != null) {
            observableObject = JsonObserver.dataObject;
            findDataObserver(object);
        }
        if (view != null) {
            findViewObserver(view);
        }
    }

    private void findViewObserver(View view) {
        try {
            if (view instanceof ViewGroup) {
                ViewGroup group = (ViewGroup) view;
                //group.setOnHierarchyChangeListener(onHierarchyChangeListener);         //need test
                for (int i = 0; i < group.getChildCount(); i++) {
                    View viewElem = group.getChildAt(i);
                    findViewObserver(viewElem);
                }
            } else {
                linkViewObserver(view);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private void linkViewObserver(final View view) {
        try {
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

                if (results.size() > 0) {
                    for (ObservableWrapper result : results) {
                        result.addObserver(observer);
                        viewObservers.put(view, new Pair<ObservableWrapper, WrapObserver>(result, observer));
                    }
                } else if (view instanceof TextView && tag.matches("\\[.*\\]")) {
                    TextView textView = (TextView) view;
                    String result = buildResult(tag.substring(1, tag.length() - 1));
                    textView.setText(result);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String buildResult(String tag) throws IllegalAccessException, NoSuchFieldException {
        Matcher matcher = pattern.matcher(tag);

        while (matcher.find()) {
            String res = matcher.group(0);
            String key = res.substring(1, res.length() - 1);
            if (key.substring(0, 1).equals(".")) {
                Object field = getFieldFromObserver(key, observableObject);
                tag = tag.replace(res, field != null ? field.toString() : "");
            } else if (key.substring(0, 8).equals("@string/")) {
                tag = tag.replace(res, getStringResource(key.substring(8)));

            }
        }

        return tag;
    }

    private String getStringResource(String stringName) {
        int resId = context.getResources().getIdentifier(stringName, "string", context.getApplicationContext().getPackageName());
        return context.getString(resId);
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
    private void findDataObserver(final Object object) {
        for (final Method method : object.getClass().getMethods()) {
            DataObserver ann = method.getAnnotation(DataObserver.class);
            if (ann!=null) {

                final ObservableWrapper wrapper = getObservable(method);
                WrapObserver observer = new WrapObserver() {
                    @Override
                    public void update(Object data) {
                        try {
                            if (data != null || wrapper.isAllowNull()) {
                                method.invoke(object, data);
                            }
                        } catch (Exception e) {
                            RuntimeException ex = null;
                            if (e.getCause() != null) {
                                ex = new RuntimeException(e.getCause());
                                ex.setStackTrace(e.getCause().getStackTrace());
                                throw ex;
                            } else {
                                ex = new RuntimeException(e);
                            }
                            throw ex;
                        }
                    }
                };

                wrapper.addObserver(observer,ann.onStartup());
                dataObservers.add(new Pair<ObservableWrapper, WrapObserver>(wrapper, observer));
            }
        }
    }

    private Object getFieldFromObserver(String fieldName, Object object) throws IllegalAccessException {
        String fields[] = fieldName.split(splitter);
        ObservableWrapper observableWrapper = (ObservableWrapper) getField(fields[1]).get(object);
        Object data = observableWrapper.get();
        if (data == null) {
            return "";
        } else {
            return getFieldValue(fieldName.substring(fields[1].length() + 2), data);
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

    private ObservableWrapper getObservable(Method method) {
        try {
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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public void stop() {
        for (Pair<ObservableWrapper, WrapObserver> pair : dataObservers) {
            pair.first.deleteObserver(pair.second);
        }
        for (Pair<ObservableWrapper, WrapObserver> pair : viewObservers.values()) {
            pair.first.deleteObserver(pair.second);
        }

        dataObservers.clear();
        viewObservers.clear();
    }


    private ViewGroup.OnHierarchyChangeListener onHierarchyChangeListener = new ViewGroup.OnHierarchyChangeListener() {

        @Override
        public void onChildViewAdded(View parent, View child) {
            findViewObserver(child);
        }

        @Override
        public void onChildViewRemoved(View parent, View child) {
            removeViewObserver(parent);
        }

        private void removeViewObserver(View view) {
            if (view instanceof ViewGroup) {
                ViewGroup group = (ViewGroup) view;
                for (int i = 0; i < group.getChildCount(); i++) {
                    View viewElem = group.getChildAt(i);
                    removeViewObserver(viewElem);
                }
            } else if (viewObservers.containsKey(view)) {
                viewObservers.remove(view);
            }
        }

    };
}
