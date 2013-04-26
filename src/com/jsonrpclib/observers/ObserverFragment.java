package com.jsonrpclib.observers;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.View;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 27.02.2013
 * Time: 16:39
 * To change this template use File | Settings | File Templates.
 */
class ObserverFragment extends Fragment {

    private ObserverHelper observerHelper;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        observerHelper = new ObserverHelper(activity);
    }

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
