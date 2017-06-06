package com.github.kubatatami.judonetworking.fragments;

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
public class ObserverSupportFragment extends JudoSupportFragment implements ObservableController {

    private ObserverHelper observerHelper = new ObserverHelper();

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        observerHelper.onDestroy();
    }

    @Override
    public void addObserverToDelete(ObservableWrapper<?> observableWrapper, WrapperObserver<?> observer) {
        observerHelper.addObserverToDelete(observableWrapper, observer);
    }
}
