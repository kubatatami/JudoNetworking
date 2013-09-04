package com.jsonrpclib.observers;

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
        findViewById(android.R.id.content).post(new Runnable() {
            @Override
            public void run() {
                observerHelper.start(ObserverActivity.this, ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0));
            }
        });

    }

    @Override
    protected void onDestroy() {
        observerHelper.stop();
        super.onDestroy();
    }
}
