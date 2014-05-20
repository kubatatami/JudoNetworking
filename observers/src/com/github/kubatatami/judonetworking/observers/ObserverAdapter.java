package com.github.kubatatami.judonetworking.observers;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 25.10.2013
 * Time: 10:15
 * To change this template use File | Settings | File Templates.
 */
public class ObserverAdapter<T> extends ArrayAdapter<T> {

    protected int[] resource;
    protected ObserverAdapterHelper adapterHelper;
    protected FilterInterface<T> filterInterface;
    protected Field mObjectsField, mOriginalField;

    public ObserverAdapter(Context context, int... resource) {
        super(context, 0);
        this.resource = resource;
        init(context);
    }

    public ObserverAdapter(Context context, T[] objects, int... resource) {
        super(context, 0, objects);
        this.resource = resource;
        init(context);
    }

    public ObserverAdapter(Context context, List<T> objects, int... resource) {
        super(context, 0, objects);
        this.resource = resource;
        init(context);
    }

    public ObserverAdapter(Context context, Map<?, T> objects, int... resource) {
        super(context, 0, new ArrayList<T>(objects.values()));
        this.resource = resource;
        init(context);
    }

    protected void init(Context context) {
        try {
            adapterHelper = new ObserverAdapterHelper(context);
            mObjectsField = ArrayAdapter.class.getDeclaredField("mObjects");
            mObjectsField.setAccessible(true);
            mOriginalField = ArrayAdapter.class.getDeclaredField("mOriginalValues");
            mOriginalField.setAccessible(true);
        } catch (Exception e) {
            ExceptionHandler.throwRuntimeException(e);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return adapterHelper.getView(resource[getItemViewType(position)], getItem(position), convertView, parent);
    }

    public View getView(int position, View convertView, ViewGroup parent, Class<?> holderClass) {
        return adapterHelper.getView(resource[getItemViewType(position)], getItem(position), convertView, parent, holderClass);
    }

    @Override
    public int getItemViewType(int position) {
        return position % getViewTypeCount();
    }

    @Override
    public int getViewTypeCount() {
        return resource.length;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                try {
                    List<T> mOriginals = (List<T>) mOriginalField.get(ObserverAdapter.this);
                    if (mOriginals == null) {
                        mOriginals = (List<T>) mObjectsField.get(ObserverAdapter.this);
                        mOriginalField.set(ObserverAdapter.this, mOriginals);
                    }
                    // We implement here the filter logic
                    if (constraint == null || constraint.length() == 0) {
                        // No filter implemented we return all the list
                        results.values = mOriginals;
                        results.count = mOriginals.size();
                    } else {
                        List<T> newValues = new ArrayList<T>();
                        for (T value : mOriginals) {
                            if (filterInterface.filter(constraint, value)) {
                                newValues.add(value);
                            }
                        }
                        results.values = newValues;
                        results.count = newValues.size();
                    }

                } catch (Exception e) {
                    ExceptionHandler.throwRuntimeException(e);
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                try {
                    mObjectsField.set(ObserverAdapter.this, results.values);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                if (results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
    }

    public void setFilterInterface(FilterInterface<T> filterInterface) {
        this.filterInterface = filterInterface;
    }

    public interface FilterInterface<T> {

        public boolean filter(CharSequence constraint, T item);

    }

    public void preHoneycombAddAll(Collection<? extends T> collection) {
        setNotifyOnChange(false);
        for(T item : collection){
            add(item);
        }
        notifyDataSetChanged();
    }

    public void preHoneycombAddAll(T... items) {
        setNotifyOnChange(false);
        for(T item : items){
            add(item);
        }
        notifyDataSetChanged();
    }

}
