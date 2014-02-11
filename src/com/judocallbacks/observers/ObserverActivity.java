package com.judocallbacks.observers;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 08.04.2013
 * Time: 22:27
 */
public class ObserverActivity extends Activity {

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
