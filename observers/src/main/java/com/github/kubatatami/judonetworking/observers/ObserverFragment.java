package com.github.kubatatami.judonetworking.observers;

import android.app.Activity;
import android.app.Fragment;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 27.02.2013
 * Time: 16:39
 */
public class ObserverFragment extends Fragment {

    private ObserverHelper observerHelper;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        observerHelper = new ObserverHelper(activity);
    }

    @Override
    public void onStart() {
        super.onStart();
        observerHelper.start(this, getView());
    }

    @Override
    public void onStop() {
        super.onStop();
        observerHelper.stop();
    }
}
