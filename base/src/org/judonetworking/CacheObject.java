package org.judonetworking;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 07.03.2013
 * Time: 07:55
 */
class CacheObject {
    final long createTime;
    private final Object object;

    CacheObject(long createTime, Object object) {
        this.createTime = createTime;
        this.object = object;
    }

    public long getCreateTime() {
        return createTime;
    }

    public Object getObject() {
        return object;
    }
}
