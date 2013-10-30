package com.jsonrpclib.observers;

import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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

    protected static class DataSource {
        protected Context context;
        protected Field field;
        protected Method method;

        public DataSource(Context context, Field field) {
            this.context = context;
            this.field = field;
        }

        public DataSource(Context context, Method method) {
            this.context = context;
            this.method = method;
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
    }

    public View getView(int layout, Object item, View convertView, ViewGroup parent) {
        return getView(layout, item, convertView, parent, null);
    }

    @SuppressWarnings("unchecked")
    public View getView(int layout, Object item, View convertView, ViewGroup parent, Class<?> holderClass) {
        List<Pair<TextView, DataSource>> data;
        if (convertView == null) {
            convertView = layoutInflater.inflate(layout, parent, false);
            data = new ArrayList<Pair<TextView, DataSource>>();
            findViewTag(convertView, data, item.getClass());
            convertView.setTag(layout, data);
            if (holderClass != null) {
                try {
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
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            }

        } else {
            data = (List<Pair<TextView, DataSource>>) convertView.getTag(layout);
        }
        for (Pair<TextView, DataSource> pair : data) {
            pair.first.setText(pair.second.getValue(item));
        }

        return convertView;
    }


    private void findViewTag(View view, List<Pair<TextView, DataSource>> data, Class<?> itemClass) {
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                View viewElem = group.getChildAt(i);
                findViewTag(viewElem, data, itemClass);
            }
        } else if (view instanceof TextView) {
            linkViewTag((TextView) view, data, itemClass);
        }
    }

    @SuppressWarnings("unchecked")
    private void linkViewTag(final TextView view, List<Pair<TextView, DataSource>> data, Class<?> itemClass) {

        if (view.getTag() != null && view.getTag() instanceof String) {
            String tag = (String) view.getTag();
            if (tag.matches("\\[.*\\]")) {
                tag = tag.substring(2, tag.length() - 1);
                DataSource dataSource = getDataSource(tag, itemClass);
                data.add(new Pair<TextView, DataSource>(view, dataSource));
            }
        }

    }

    private DataSource getDataSource(String fieldName, Class<?> clazz) {
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
                    return new DataSource(context, field);
                } catch (NoSuchFieldException e) {
                    try {
                        Method method = getMethod(part, clazz);
                        return new DataSource(context, method);
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
        Method method = null;
        while (objectClass != null && method == null) {
            try {
                method = objectClass.getDeclaredMethod(fieldName);
            } catch (NoSuchMethodException e) {
                try {
                    method = objectClass.getDeclaredMethod(fieldName, Context.class);
                } catch (NoSuchMethodException e1) {
                    objectClass = objectClass.getSuperclass();
                }
            }
        }
        if (method == null) {
            throw new NoSuchFieldException(fieldName);
        }
        return method;
    }
}
