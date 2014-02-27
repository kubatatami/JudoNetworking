package com.github.kubatatami.judonetworking.observers;

import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.github.kubatatami.judonetworking.exceptions.JudoException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

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
    private static final String splitter = "\\.";

    public ObserverAdapterHelper(Context context) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
    }

    protected static class DataSourceOrTarget {
        protected Context context;
        protected Field field;
        protected Method method;

        public DataSourceOrTarget(Context context, Field field) {
            this.context = context;
            this.field = field;
        }

        public DataSourceOrTarget(Context context, Method method) {
            this.context = context;
            this.method = method;
        }

        public boolean isSource() {
            return field != null || !method.getReturnType().equals(Void.TYPE);
        }

        public String getValue(Object item) {
            try {
                String result;
                if (field != null) {
                    result = field.get(item).toString();
                } else if (method.getParameterTypes().length == 1) {
                    result = method.invoke(item, context).toString();
                } else {
                    result = method.invoke(item).toString();
                }

                return result;

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public void setValue(Object item, View view) {
            try {
                method.invoke(item, view);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }

    public View getView(int layout, Object item, View convertView, ViewGroup parent) {
        return getView(layout, item, convertView, parent, null);
    }

    public static boolean isInnerClass(Class<?> clazz) {
        return clazz.isMemberClass() && !Modifier.isStatic(clazz.getModifiers());
    }

    public View getView(int layout, View convertView, ViewGroup parent, Class<?> holderClass) {
        return getView(layout, null, convertView, parent, holderClass);
    }

    public View getView(int layout, View convertView, ViewGroup parent) {
        return getView(layout, null, convertView, parent, null);
    }

    @SuppressWarnings("unchecked")
    public View getView(int layout, Object item, View convertView, ViewGroup parent, Class<?> holderClass) {
        try {
            List<Pair<View, DataSourceOrTarget>> dataSources;

            if (convertView == null) {
                convertView = layoutInflater.inflate(layout, parent, false);
                dataSources = new ArrayList<Pair<View, DataSourceOrTarget>>();
                if (item != null) {
                    findViewTag(convertView, dataSources, item.getClass());
                }
                convertView.setTag(layout, dataSources);
                if (holderClass != null) {
                    if (isInnerClass(holderClass)) {
                        throw new JudoException("Inner holder class must be static!");
                    }
                    Constructor<?> constructor = holderClass.getDeclaredConstructors()[0];
                    constructor.setAccessible(true);
                    Object holder = constructor.newInstance();
                    for (Field field : holderClass.getDeclaredFields()) {
                        HolderView viewById = field.getAnnotation(HolderView.class);
                        if (viewById != null) {
                            field.setAccessible(true);
                            field.set(holder, convertView.findViewById(viewById.value()));
                        }
                    }
                    convertView.setTag(holder);
                }

            } else {
                dataSources = (List<Pair<View, DataSourceOrTarget>>) convertView.getTag(layout);
            }
            for (Pair<View, DataSourceOrTarget> pair : dataSources) {
                if (pair.second.isSource()) {
                    ((TextView) pair.first).setText(pair.second.getValue(item));
                } else {
                    pair.second.setValue(item, pair.first);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return convertView;
    }


    private void findViewTag(View view, List<Pair<View, DataSourceOrTarget>> data, Class<?> itemClass) throws JudoException {
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                View viewElem = group.getChildAt(i);
                findViewTag(viewElem, data, itemClass);
            }
        } else {
            linkViewTag(view, data, itemClass);
        }
    }

    @SuppressWarnings("unchecked")
    private void linkViewTag(final View view, List<Pair<View, DataSourceOrTarget>> data, Class<?> itemClass) throws JudoException {

        if (view.getTag() != null && view.getTag() instanceof String) {
            String tag = (String) view.getTag();
            if (tag.matches("\\[.*\\]")) {
                tag = tag.substring(2, tag.length() - 1);
                DataSourceOrTarget dataSourceOrTarget = getDataSource(tag, itemClass);
                if (dataSourceOrTarget.isSource() && !(view instanceof TextView)) {
                    throw new JudoException("Method which returns value must be link with TextView");
                }
                data.add(new Pair<View, DataSourceOrTarget>(view, dataSourceOrTarget));
            }
        }

    }

    private DataSourceOrTarget getDataSource(String fieldName, Class<?> clazz) {
        int i = 0;
        Field field;
        String parts[] = fieldName.split(splitter);
        for (String part : parts) {
            i++;
            if (i != parts.length) {
                try {
                    field = getField(part, clazz);
                    clazz = field.getClass();
                } catch (NoSuchFieldException e) {
                    throw new RuntimeException(e);
                }
            } else {
                try {
                    field = getField(part, clazz);
                    return new DataSourceOrTarget(context, field);
                } catch (NoSuchFieldException e) {
                    try {
                        Method method = getMethod(part, clazz);
                        return new DataSourceOrTarget(context, method);
                    } catch (NoSuchFieldException e1) {
                        throw new RuntimeException(e1);
                    }


                }
            }

        }
        return null;
    }

    private static Field getField(String fieldName, Class<?> objectClass) throws NoSuchFieldException {
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
            throw new NoSuchFieldException(fieldName);
        }
        return field;
    }


    private static Method getMethod(String fieldName, Class<?> objectClass) throws NoSuchFieldException {
        Method finalMethod = null;
        while (objectClass != null && finalMethod == null) {
            for (Method method : objectClass.getDeclaredMethods()) {
                if (method.getName().equals(fieldName)) {
                    Class<?>[] paramsType = method.getParameterTypes();
                    if (paramsType.length == 0) {
                        finalMethod = method;
                        break;
                    } else if (paramsType.length == 1) {
                        if (paramsType[0].equals(Context.class) || View.class.isAssignableFrom(paramsType[0])) {
                            finalMethod = method;
                            break;
                        }
                    }

                }
            }
            if (finalMethod == null) {
                objectClass = objectClass.getSuperclass();
            }
        }
        if (finalMethod == null) {
            throw new NoSuchFieldException(fieldName);
        }
        return finalMethod;
    }
}
