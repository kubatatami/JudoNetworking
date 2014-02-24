package com.github.kubatatami.judonetworking;

import java.util.Comparator;

/**
 * Created by Kuba on 19/02/14.
 */
public class RequestComparator implements Comparator<RequestInterface> {
    @Override
    public int compare(RequestInterface lhs, RequestInterface rhs) {
        return lhs.getId().compareTo(rhs.getId());
    }
}
