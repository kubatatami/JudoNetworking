package com.github.kubatatami.judonetworking.observers;

import android.content.Context;

import com.github.kubatatami.judonetworking.logs.JudoLogger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * Created by Kuba on 22/04/15.
 */
public class ObservableSerializableWrapper<T extends Serializable> extends ObservablePersistentWrapper<T> {
    public ObservableSerializableWrapper(Context context, String persistentKey) {
        super(context, persistentKey);
    }

    public ObservableSerializableWrapper(Context context, String persistentKey, Level level) {
        super(context, persistentKey, level);
    }

    @Override
    protected PersistentData<T> loadObject(byte[] array) throws Exception {
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(array);
        ObjectInputStream os = new ObjectInputStream(byteArrayInputStream);
        PersistentData<T> result = (PersistentData<T>) os.readObject();
        os.close();
        byteArrayInputStream.close();
        return result;
    }

    @Override
    protected void saveObject(OutputStream fileStream, PersistentData<T> data) throws Exception {
        ObjectOutputStream os = null;
        try {
            os = new ObjectOutputStream(fileStream);
            os.writeObject(data);
            os.flush();
        } catch (IOException e) {
            JudoLogger.log(e);
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException ex) {
                JudoLogger.log(ex);
            }
        }
    }
}
