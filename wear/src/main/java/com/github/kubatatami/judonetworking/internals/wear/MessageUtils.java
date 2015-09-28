package com.github.kubatatami.judonetworking.internals.wear;

import android.content.Context;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kubatatami.judonetworking.exceptions.ConnectionException;
import com.github.kubatatami.judonetworking.wear.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by Kuba on 28/09/15.
 */
public class MessageUtils {

    protected Random random = new Random();

    protected ObjectMapper objectMapper = new ObjectMapper();

    protected Context context;

    protected final static String MSG_PATH = "/jj/requestProxy/";

    static Map<String, Object> waitObjects = new HashMap<>();

    static Map<String, byte[]> resultObjects = new HashMap<>();

    protected GoogleApiClient googleClient;

    protected long connectionTimeout = 10000;

    protected long sendTimeout = 15000;

    protected long readTimeout = 15000;

    public MessageUtils(Context context) {
        this.context = context.getApplicationContext();
        googleClient = new GoogleApiClient.Builder(context.getApplicationContext())
                .addApiIfAvailable(Wearable.API)
                .build();
        googleClient.connect();
    }

    public <T> T sendMessageAndReceive(Object msg, int operationTimeout, Class<T> clazz) throws IOException {
        makeSureIsConnected();
        Set<Node> nodes = Wearable.CapabilityApi.getCapability(
                googleClient, context.getString(R.string.jj_request_proxy),
                CapabilityApi.FILTER_REACHABLE).await().getCapability().getNodes();
        String id = generateUniqId();
        Object waitObject = new Object();
        waitObjects.put(id, waitObject);
        sendMessage(MSG_PATH + id, pickBestNodeId(nodes), msg);
        try {
            waitObject.wait(readTimeout + operationTimeout);
        } catch (InterruptedException e) {
            throw new ConnectionException("GoogleApiClient timeout");
        } finally {
            waitObjects.remove(id);
        }
        return readObject(resultObjects.remove(id), clazz);
    }

    public <T> T readObject(byte[] msg, Class<T> clazz) throws IOException {
        return objectMapper.readValue(msg, clazz);
    }

    public void sendMessage(String msgId, String nodeId, Object msg) throws IOException {
        byte[] message = objectMapper.writeValueAsBytes(msg);
        MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(googleClient,
                nodeId, msgId, message).await(sendTimeout, TimeUnit.MILLISECONDS);
        Status status = result.getStatus();
        if (!status.isSuccess()) {
            throw new ConnectionException("GoogleApiClient failed to send message: "
                    + status.getStatusMessage() + "(" + status.getStatusCode() + ")");
        }
    }

    private String pickBestNodeId(Set<Node> nodes) {
        String bestNodeId = null;
        for (Node node : nodes) {
            if (node.isNearby()) {
                return node.getId();
            }
            bestNodeId = node.getId();
        }
        return bestNodeId;
    }

    protected void makeSureIsConnected() {
        ConnectionResult connectionResult = googleClient.blockingConnect(connectionTimeout, TimeUnit.MILLISECONDS);
        if (!connectionResult.isSuccess()) {
            throw new ConnectionException("GoogleApiClient error" + connectionResult.getErrorCode());
        }
    }

    protected String generateUniqId() {
        return System.currentTimeMillis() + "" + random.nextInt(10000);
    }

    public long getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(long connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public long getSendTimeout() {
        return sendTimeout;
    }

    public void setSendTimeout(long sendTimeout) {
        this.sendTimeout = sendTimeout;
    }

    public long getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(long readTimeout) {
        this.readTimeout = readTimeout;
    }
}
