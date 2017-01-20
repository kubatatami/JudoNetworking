package com.github.kubatatami.judonetworking.observers;

import android.support.v4.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 08.04.2013
 * Time: 22:23
 */
public class ObserverHelper {

    private List<Pair<ObservableWrapper, WrapperObserver>> observersToDeleteOnDestroy = new ArrayList<>();

    public void addObserverToDelete(ObservableWrapper<?> observableWrapper, WrapperObserver<?> observer) {
        observersToDeleteOnDestroy.add(new Pair<ObservableWrapper, WrapperObserver>(observableWrapper, observer));
    }

    public void onDestroy() {
        for (Pair<ObservableWrapper, WrapperObserver> pair : observersToDeleteOnDestroy) {
            //noinspection unchecked
            pair.first.deleteObserver(pair.second);
        }
    }

}
