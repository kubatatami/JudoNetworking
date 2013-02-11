package com.implix.jsonrpc;

import android.widget.ArrayAdapter;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 07.01.2013
 * Time: 10:55
 * To change this template use File | Settings | File Templates.
 */
public class JsonArrayAdapterCallback<T extends List<Z>,Z> extends  JsonCallback<T> {

    ArrayAdapter<Z> adapter;

    public JsonArrayAdapterCallback(ArrayAdapter<Z> adapter)
    {
        this.adapter = adapter;
    }

    @Override
    public void onFinish(T result)
    {
        adapter.clear();

        for(Z obj : result)
        {
            adapter.add(obj);
        }

        adapter.notifyDataSetChanged();
    }


}
