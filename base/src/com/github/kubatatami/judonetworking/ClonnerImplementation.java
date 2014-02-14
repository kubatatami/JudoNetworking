package com.github.kubatatami.judonetworking;

import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 29.05.2013
 * Time: 10:08
 * To change this template use File | Settings | File Templates.
 */
public class ClonnerImplementation implements Clonner {

    @Override
    public <T> T clone(T object) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(object);
        out.flush();
        out.close();
        ObjectInputStream in = new ObjectInputStream(
                new ByteArrayInputStream(bos.toByteArray()));
        object = (T) in.readObject();

        return object;
    }

}
