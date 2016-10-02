package com.github.kubatatami.judonetworking.internals.stats;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 23.03.2013
 * Time: 16:59
 */
public class MethodStat implements Serializable {

    private static final long serialVersionUID = -8716566790880974135L;

    public long requestCount = 0;

    public long methodTime = 0;

    public long allTime = 0;

    public long errors = 0;

    @Override
    public String toString() {
        return "requestCount=" + requestCount +
                ", methodTime=" + methodTime +
                ", allTime=" + allTime +
                ", errors=" + errors;
    }
}
