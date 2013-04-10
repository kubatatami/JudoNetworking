package com.implix.jsonrpc.observers;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Pair;
import android.view.View;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 27.02.2013
 * Time: 16:39
 * To change this template use File | Settings | File Templates.
 */
public class ObserverSupportFragment extends Fragment {

    private ObserverHelper observerHelper = new ObserverHelper();

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        observerHelper.start(this, view);
    }

    @Override
    public void onDestroyView() {
        observerHelper.stop();
        super.onDestroyView();
    }
}
