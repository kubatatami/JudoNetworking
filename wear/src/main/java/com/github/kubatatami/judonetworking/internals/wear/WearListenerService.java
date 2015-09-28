package com.github.kubatatami.judonetworking.internals.wear;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

public class WearListenerService extends WearableListenerService {

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().contains(MessageUtils.MSG_PATH)) {
            String id = messageEvent.getPath().substring(MessageUtils.MSG_PATH.length());
            MessageUtils.resultObjects.put(id, messageEvent.getData());
            MessageUtils.waitObjects.get(id).notifyAll();
        } else {
            super.onMessageReceived(messageEvent);
        }
    }
}