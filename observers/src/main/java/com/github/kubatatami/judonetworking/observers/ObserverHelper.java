package com.github.kubatatami.judonetworking.observers;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ListView;
import android.widget.TextView;

import com.github.kubatatami.judonetworking.exceptions.JudoException;

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
    private List<Pair<ObservableWrapper, WrapObserver>> dataObservers = new ArrayList<>();
    private List<Pair<Adapter, DataSetObserver>> dataAdapters = new ArrayList<>();
    private Map<View, Pair<ObservableWrapper, WrapObserver>> viewObservers = new HashMap<>();

    private static final String splitter = "\\.";
    private static final Pattern pattern = Pattern.compile("\\[[^\\]]*\\]");
    static final String convention = "Changed";
    private Context context;

    private static ObserverErrorLogger errorLogger;

    private static Object dataObject;
    static Class<?> dataClass;

    public static void setDataObject(Object data) {
        dataObject = data;
        dataClass = data.getClass();
    }

    public static void setDataClass(Class<?> dataClass) {
        dataObject = null;
        ObserverHelper.dataClass = dataClass;
    }

    public ObserverHelper(Context context) {
        this.context = context;
    }

    public static void setErrorLogger(ObserverErrorLogger errorLogger) {
        ObserverHelper.errorLogger = errorLogger;
    }

    public void start(final android.support.v4.app.Fragment fragment, View view) {
        startInternal(fragment, view);
    }

    public void start(final android.app.Fragment fragment, View view) {
        startInternal(fragment, view);
    }

    public void start(final Activity activity, View view) {
        startInternal(activity, view);
    }

    public void start(final FragmentActivity activity, View view) {
        startInternal(activity, view);
    }

    protected ObserverSettings getSettingsAnnotation(Class<?> clazz) {
        for (; clazz != null; clazz = clazz.getSuperclass()) {
            ObserverSettings settings = clazz.getAnnotation(ObserverSettings.class);
            if (settings != null) {
                return settings;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    protected void startInternal(final Object object, View view) {
        ObserverSettings settings = getSettingsAnnotation(object.getClass());
        if (settings != null) {
            dataObservers.clear();
            viewObservers.clear();
            dataAdapters.clear();
            if (settings.dataObservers() && ObserverHelper.dataClass != null) {
                findDataObserver(object);
            }
            if (settings.tags() && view != null) {
                findViewObserver(view, object);
            }
        }
    }

    private void findViewObserver(View view, Object object) {
        if (view instanceof ViewGroup && !(view instanceof ListView) && !isFromFragment(view, object)) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                View viewElem = group.getChildAt(i);
                findViewObserver(viewElem, object);
            }
        } else {
            linkViewObserver(view, object);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private boolean isFromFragment(View view, Object object) {
        if (view.getId() == -1) {
            return false;
        }
        if (object instanceof FragmentActivity) {
            return ((FragmentActivity) object).getSupportFragmentManager().findFragmentById(view.getId()) != null;
        } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB && object instanceof Activity) {
            return ((Activity) object).getFragmentManager().findFragmentById(view.getId()) != null;
        } else {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private void linkViewObserver(final View view, final Object object) {
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

                                String result = buildResult(tag.substring(1, tag.length() - 1), object);
                                textView.setText(result);

                            }
                        } catch (Exception e) {
                            ExceptionHandler.throwRuntimeException(e);
                        }
                    }
                };

                if (results.size() > 0) {
                    for (ObservableWrapper result : results) {
                        result.addObserver(observer);
                        viewObservers.put(view, new Pair<>(result, observer));
                    }
                } else if (view instanceof TextView && tag.matches("\\[.*\\]")) {
                    TextView textView = (TextView) view;
                    String result = buildResult(tag.substring(1, tag.length() - 1), object);
                    textView.setText(result);
                }
            }
        } catch (Exception e) {
            ExceptionHandler.throwRuntimeException(e);
        }
    }

    private String buildResult(String tag, Object object) throws IllegalAccessException, NoSuchFieldException {
        Matcher matcher = pattern.matcher(tag);

        while (matcher.find()) {
            String res = matcher.group(0);
            String key = res.substring(1, res.length() - 1);
            if (key.substring(0, 1).equals(".")) {
                Object field = getFieldFromObserver(key, ObserverHelper.dataObject);
                tag = tag.replace(res, field != null ? field.toString() : "");
            } else if (key.substring(0, 8).equals("@string/")) {
                tag = tag.replace(res, getStringResource(key.substring(8)));

            } else {
                Object field = getFieldOrMethodValue(key, object);
                tag = tag.replace(res, field != null ? field.toString() : "");
            }
        }

        return tag;
    }

    private String getStringResource(String stringName) {
        int resId = context.getResources().getIdentifier(stringName, "string", context.getApplicationContext().getPackageName());
        return context.getString(resId);
    }

    private List<ObservableWrapper> findObservablesByTag(String tag) throws Exception {
        List<ObservableWrapper> list = new ArrayList<>();
        if (tag.matches("\\[.*\\]")) {

            if (ObserverHelper.dataClass == null) {
                throw new RuntimeException("No data object set. Use JsonObserver.setDataObject() method.");
            }

            tag = tag.substring(1, tag.length() - 1);
            Matcher matcher = pattern.matcher(tag);

            while (matcher.find()) {
                String res = matcher.group(0);
                res = res.substring(1, res.length() - 1);
                if (res.substring(0, 1).equals(".")) {
                    String fields[] = res.split(splitter);
                    list.add((ObservableWrapper) ObservableCache.getField(fields[1]).get(ObserverHelper.dataObject));
                }
            }

        }
        return list;
    }

    @SuppressWarnings("unchecked")
    private void findDataObserver(final Object object) {
        for (final ObservableCache.DataObserverMethod observerMethod : ObservableCache.getDataObserverMethods(object.getClass())) {
            try {
                final Object wrapperOrAdapter = observerMethod.field.get(ObserverHelper.dataObject);
                if (wrapperOrAdapter instanceof ObservableWrapper) {
                    final ObservableWrapper wrapper = (ObservableWrapper) wrapperOrAdapter;
                    WrapObserver observer = new WrapObserver() {
                        @Override
                        public void update(Object data) {
                            try {
                                if (data != null || wrapper.isNotifyOnNull()) {
                                    observerMethod.method.invoke(object, data);
                                }
                            } catch (Exception e) {
                                JudoException ex;
                                if (e.getCause() != null) {
                                    ex = new JudoException(e.getCause());
                                    ex.setStackTrace(e.getCause().getStackTrace());
                                } else {
                                    ex = new JudoException(e);
                                }
                                if (observerMethod.dataObserver.crashable()) {
                                    throw ex;
                                } else if (errorLogger != null) {
                                    errorLogger.onError(ex);
                                }
                            }
                        }
                    };

                    wrapper.addObserver(observer, observerMethod.dataObserver.onStartup() && wrapper.isLoaded());
                    dataObservers.add(new Pair<>(wrapper, observer));
                } else if (wrapperOrAdapter instanceof Adapter) {
                    final Adapter adapter = (Adapter) wrapperOrAdapter;
                    DataSetObserver dataSetObserver = new DataSetObserver() {
                        @Override
                        public void onChanged() {
                            Object param;
                            if (observerMethod.method.getParameterTypes()[0].isAssignableFrom(List.class)) {
                                List<Object> list = new ArrayList<>();
                                for (int i = 0; i < adapter.getCount(); i++) {
                                    list.add(adapter.getItem(i));
                                }
                                param = list;
                            } else {
                                param = adapter;
                            }
                            try {
                                observerMethod.method.invoke(object, param);
                            } catch (Exception e) {
                                JudoException ex;
                                if (e.getCause() != null) {
                                    ex = new JudoException(e.getCause());
                                    ex.setStackTrace(e.getCause().getStackTrace());
                                } else {
                                    ex = new JudoException(e);
                                }
                                if (observerMethod.dataObserver.crashable()) {
                                    throw ex;
                                } else if (errorLogger != null) {
                                    errorLogger.onError(ex);
                                }
                            }
                        }
                    };
                    adapter.registerDataSetObserver(dataSetObserver);
                    if (observerMethod.dataObserver.onStartup()) {
                        dataSetObserver.onChanged();
                    }
                    dataAdapters.add(new Pair<>(adapter, dataSetObserver));
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private Object getFieldFromObserver(String fieldName, Object object) throws IllegalAccessException {
        String fields[] = fieldName.split(splitter);
        ObservableWrapper observableWrapper = (ObservableWrapper) ObservableCache.getField(fields[1]).get(object);
        Object data = observableWrapper.get();
        if (data == null) {
            return "";
        } else {
            return getFieldOrMethodValue(fieldName.substring(fields[1].length() + 2), data);
        }
    }

    private Object getFieldOrMethodValue(String fieldName, Object object) throws IllegalAccessException {
        if (object == null) {
            return null;
        }
        String parts[] = fieldName.split(splitter);
        Class<?> clazz;
        int i = 0;
        for (String part : parts) {
            clazz = object.getClass();
            try {
                object = ObservableCache.getField(part, clazz).get(object);
            } catch (Exception ex) {
                if (i == parts.length - 1) {
                    try {
                        Method method = ObserverAdapterHelper.getMethod(part, clazz);
                        if (method.getParameterTypes().length == 1) {
                            return method.invoke(object, context).toString();
                        } else {
                            return method.invoke(object).toString();
                        }
                    } catch (Exception e) {
                        ExceptionHandler.throwRuntimeException(e);
                    }
                }
            }
            i++;
        }
        return object;
    }


    @SuppressWarnings("unchecked")
    public void stop() {
        for (Pair<ObservableWrapper, WrapObserver> pair : dataObservers) {
            pair.first.deleteObserver(pair.second);
        }
        for (Pair<Adapter, DataSetObserver> pair : dataAdapters) {
            pair.first.unregisterDataSetObserver(pair.second);
        }
        for (Pair<ObservableWrapper, WrapObserver> pair : viewObservers.values()) {
            pair.first.deleteObserver(pair.second);
        }

        dataObservers.clear();
        dataAdapters.clear();
        viewObservers.clear();
    }


}
