package org.judonetworking.observers;

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
import org.judonetworking.ErrorLogger;

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
    private List<Pair<Adapter, DataSetObserver>> dataAdapters = new ArrayList<Pair<Adapter, DataSetObserver>>();
    private Map<View, Pair<ObservableWrapper, WrapObserver>> viewObservers = new HashMap<View, Pair<ObservableWrapper, WrapObserver>>();

    private static final String splitter = "\\.";
    private static final Pattern pattern = Pattern.compile("\\[[^\\]]*\\]");
    private static final String convention = "Changed";
    private Context context;

    private static ErrorLogger errorLogger;

    private static Object dataObject;
    private static Class<?> dataClass;

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

    public static void setErrorLogger(ErrorLogger errorLogger) {
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

    @SuppressWarnings("unchecked")
    protected void startInternal(final Object object, View view) {
        dataObservers.clear();
        viewObservers.clear();
        dataAdapters.clear();
        if (ObserverHelper.dataClass != null) {
            findDataObserver(object);
        }
        if (view != null) {
            findViewObserver(view, object);
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
                    String result = buildResult(tag.substring(1, tag.length() - 1), object);
                    textView.setText(result);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
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
                Object field = getFieldValue(key, object);
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
        List<ObservableWrapper> list = new ArrayList<ObservableWrapper>();
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
                    list.add((ObservableWrapper) getField(fields[1]).get(ObserverHelper.dataObject));
                }
            }

        }
        return list;
    }

    @SuppressWarnings("unchecked")
    private void findDataObserver(final Object object) {
        for (final Method method : object.getClass().getMethods()) {
            final DataObserver ann = method.getAnnotation(DataObserver.class);
            if (ann != null) {

                final Object wrapperOrAdapter = getObservableOrAdapter(method);
                if (wrapperOrAdapter != null) {

                    if (wrapperOrAdapter instanceof ObservableWrapper) {
                        final ObservableWrapper wrapper = (ObservableWrapper) wrapperOrAdapter;
                        WrapObserver observer = new WrapObserver() {
                            @Override
                            public void update(Object data) {
                                try {
                                    if (data != null || wrapper.isAllowNull()) {
                                        method.invoke(object, data);
                                    }
                                } catch (Exception e) {
                                    RuntimeException ex;
                                    if (e.getCause() != null) {
                                        ex = new RuntimeException(e.getCause());
                                        ex.setStackTrace(e.getCause().getStackTrace());
                                    } else {
                                        ex = new RuntimeException(e);
                                    }
                                    if (ann.crashable()) {
                                        throw ex;
                                    } else if(errorLogger!=null){
                                        errorLogger.onError(ex);
                                    }
                                }
                            }
                        };

                        wrapper.addObserver(observer, ann.onStartup());
                        dataObservers.add(new Pair<ObservableWrapper, WrapObserver>(wrapper, observer));
                    } else if (wrapperOrAdapter instanceof Adapter) {
                        final Adapter adapter = (Adapter) wrapperOrAdapter;
                        DataSetObserver dataSetObserver = new DataSetObserver() {
                            @Override
                            public void onChanged() {
                                Object param;
                                if (method.getParameterTypes()[0].isAssignableFrom(List.class)) {
                                    List<Object> list = new ArrayList<Object>();
                                    for (int i = 0; i < adapter.getCount(); i++) {
                                        list.add(adapter.getItem(i));
                                    }
                                    param = list;
                                } else {
                                    param = adapter;
                                }
                                try {
                                    method.invoke(object, param);
                                } catch (Exception e) {
                                    RuntimeException ex;
                                    if (e.getCause() != null) {
                                        ex = new RuntimeException(e.getCause());
                                        ex.setStackTrace(e.getCause().getStackTrace());
                                    } else {
                                        ex = new RuntimeException(e);
                                    }
                                    if (ann.crashable()) {
                                        throw ex;
                                    } else if(errorLogger!=null){
                                        errorLogger.onError(ex);
                                    }
                                }
                            }
                        };
                        adapter.registerDataSetObserver(dataSetObserver);
                        if (ann.onStartup()) {
                            dataSetObserver.onChanged();
                        }
                        dataAdapters.add(new Pair<Adapter, DataSetObserver>(adapter, dataSetObserver));
                    }
                }


            }
        }
    }

    private static Object getFieldFromObserver(String fieldName, Object object) throws IllegalAccessException {
        String fields[] = fieldName.split(splitter);
        ObservableWrapper observableWrapper = (ObservableWrapper) getField(fields[1]).get(object);
        Object data = observableWrapper.get();
        if (data == null) {
            return "";
        } else {
            return getFieldValue(fieldName.substring(fields[1].length() + 2), data);
        }
    }

    private static Object getFieldValue(String fieldName, Object object) throws IllegalAccessException {
        String parts[] = fieldName.split(splitter);
        Class<?> clazz;
        for (String part : parts) {
            clazz = object.getClass();
            object = getField(part, clazz).get(object);
        }
        return object;
    }

    private static Field getField(String fieldName) {
        return getField(fieldName, ObserverHelper.dataClass);
    }

    private static Field getField(String fieldName, Class<?> objectClass) {
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

    private static Object getObservableOrAdapter(Method method) {
        try {
            DataObserver ann = method.getAnnotation(DataObserver.class);

            if (!ann.fieldName().equals("")) {
                return getField(ann.fieldName()).get(ObserverHelper.dataObject);
            }

            String methodName = method.getName();
            if (methodName.length() > convention.length() + 1) {
                String fieldName = methodName.substring(0, methodName.length() - convention.length());

                return getField(fieldName).get(ObserverHelper.dataObject);

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
