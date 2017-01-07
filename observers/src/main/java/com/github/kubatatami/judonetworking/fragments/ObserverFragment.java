package com.github.kubatatami.judonetworking.fragments;

import android.app.Activity;

import com.github.kubatatami.judonetworking.observers.ObservableController;
import com.github.kubatatami.judonetworking.observers.ObservableWrapper;
import com.github.kubatatami.judonetworking.observers.ObserverHelper;
import com.github.kubatatami.judonetworking.observers.WrapperObserver;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 27.02.2013
 * Time: 16:39
 */
public class ObserverFragment extends JudoFragment implements ObservableController {

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

    @Override
    public void onDestroy() {
        super.onDestroy();
        observerHelper.onDestroy();
    }

    @Override
    public void addObserverToDelete(ObservableWrapper<?> observableWrapper, WrapperObserver<?> observer) {
        observerHelper.addObserverToDelete(observableWrapper, observer);
    }
}
