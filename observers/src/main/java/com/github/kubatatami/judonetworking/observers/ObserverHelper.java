package com.github.kubatatami.judonetworking.observers;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 08.04.2013
 * Time: 22:23
 */
public class ObserverHelper {

    private Map<ObservableWrapper, WrapperObserver> observersToDeleteOnDestroy = new HashMap<>();

    public void addObserverToDelete(ObservableWrapper<?> observableWrapper, WrapperObserver<?> observer) {
        observersToDeleteOnDestroy.put(observableWrapper, observer);
    }

    public void onDestroy() {
        for (Map.Entry<ObservableWrapper, WrapperObserver> entry : observersToDeleteOnDestroy.entrySet()) {
            //noinspection unchecked
            entry.getKey().deleteObserver(entry.getValue());
        }
    }

}
