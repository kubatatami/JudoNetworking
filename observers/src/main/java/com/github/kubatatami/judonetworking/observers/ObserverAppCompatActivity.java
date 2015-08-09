package com.github.kubatatami.judonetworking.observers;

import android.os.Bundle;
import android.view.ViewGroup;

import com.github.kubatatami.judonetworking.activity.JudoAppCompatActivity;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 08.04.2013
 * Time: 22:27
 */
public class ObserverAppCompatActivity extends JudoAppCompatActivity {

    private ObserverHelper observerHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        observerHelper = new ObserverHelper(this);
    }


    @Override
    public void onStop() {
        super.onStop();
        observerHelper.stop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        observerHelper.start(this, ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0));
    }

}
