package com.implix.jsonrpc;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 23.03.2013
 * Time: 16:59
 * To change this template use File | Settings | File Templates.
 */
public class JsonStat implements Serializable{
    public long requestCount=0;
    public long avgTime=0;
    public long errors=0;


    @Override
    public String toString() {
        return "requestCount=" + requestCount +
                ", avgTime=" + avgTime +
                ", errors=" + errors;
    }
}
