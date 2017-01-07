package com.github.kubatatami.judonetworking.activities;

import android.os.Bundle;
import android.view.ViewGroup;

import com.github.kubatatami.judonetworking.observers.ObservableController;
import com.github.kubatatami.judonetworking.observers.ObservableWrapper;
import com.github.kubatatami.judonetworking.observers.ObserverHelper;
import com.github.kubatatami.judonetworking.observers.WrapperObserver;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 08.04.2013
 * Time: 22:27
 */
public class ObserverActivity extends JudoActivity implements ObservableController {

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        observerHelper.onDestroy();
    }

    @Override
    public void addObserverToDelete(ObservableWrapper<?> observableWrapper, WrapperObserver<?> observer) {
        observerHelper.addObserverToDelete(observableWrapper, observer);
    }
}
