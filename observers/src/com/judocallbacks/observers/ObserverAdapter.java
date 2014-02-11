package com.judocallbacks.observers;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
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

    protected int resource;
    protected ObserverAdapterHelper adapterHelper;

    public ObserverAdapter(Context context, int resource) {
        super(context, resource);
        this.resource =resource;
        adapterHelper = new ObserverAdapterHelper(context);
    }

    public ObserverAdapter(Context context, int resource, T[] objects) {
        super(context, resource, objects);
        this.resource = resource;
        adapterHelper = new ObserverAdapterHelper(context);
    }

    public ObserverAdapter(Context context, int resource, List<T> objects) {
        super(context, resource, objects);
        this.resource = resource;
        adapterHelper = new ObserverAdapterHelper(context);
    }

    public ObserverAdapter(Context context, int resource, Map<?,T> objects) {
        super(context, resource, new ArrayList<T>(objects.values()));
        this.resource = resource;
        adapterHelper = new ObserverAdapterHelper(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return adapterHelper.getView(resource,getItem(position),convertView,parent);
    }

    public View getView(int position, View convertView, ViewGroup parent, Class<?> holderClass) {
        return adapterHelper.getView(resource,getItem(position),convertView,parent,holderClass);
    }

}
