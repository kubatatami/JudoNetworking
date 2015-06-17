package com.github.kubatatami.judonetworking.clonners;

import com.github.kubatatami.judonetworking.exceptions.JudoException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 29.05.2013
 * Time: 10:08
 * To change this template use File | Settings | File Templates.
 */
public class DefaultClonner implements Clonner {

    @Override
    public <T> T clone(T object) throws JudoException {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(object);
            out.flush();
            out.close();
            ObjectInputStream in = new ObjectInputStream(
                    new ByteArrayInputStream(bos.toByteArray()));
            object = (T) in.readObject();
        } catch (Exception e) {
            throw new JudoException("Can't clone object " + object.getClass().getName(), e);
        }
        return object;
    }

}
