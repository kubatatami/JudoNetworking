package com.github.kubatatami.judonetworking.activities;

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
public class ObserverAppCompatActivity extends JudoAppCompatActivity implements ObservableController {

    private ObserverHelper observerHelper = new ObserverHelper();

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
