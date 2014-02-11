package com.judocallbacks.observers;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.ViewGroup;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 08.04.2013
 * Time: 22:27
 */
public class ObserverFragmentActivity extends FragmentActivity {

    private ObserverHelper observerHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        observerHelper = new ObserverHelper(this);
    }


    @Override
    protected void onStop() {
        super.onStop();
        observerHelper.stop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        observerHelper.start(this, ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0));
    }

}
