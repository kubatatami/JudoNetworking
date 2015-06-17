package com.github.kubatatami.judonetworking.observers;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.OutputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Created by Kuba on 22/04/15.
 */
public class ObservableParcelableWrapper<T extends Parcelable> extends ObservablePersistentWrapper<T> {


    public ObservableParcelableWrapper(Context context, String persistentKey) {
        super(context, persistentKey);
    }

    public ObservableParcelableWrapper(Context context, String persistentKey, Level level) {
        super(context, persistentKey, level);
    }

    @Override
    protected PersistentData<T> loadObject(byte[] array) throws Exception {
        Parcel parcel = Parcel.obtain();
        parcel.unmarshall(array, 0, array.length);
        Type sooper = getClass().getGenericSuperclass();
        Class<?> t = (Class<?>) ((ParameterizedType) sooper).getActualTypeArguments()[0];
        PersistentData<T> result = (PersistentData<T>) new PersistentData<>(parcel.readLong(), parcel.readParcelable(t.getClassLoader()));
        parcel.recycle();
        return result;
    }

    @Override
    protected void saveObject(OutputStream fileStream, PersistentData<T> data) throws Exception {
        Parcel parcel = Parcel.obtain();
        parcel.writeLong(data.dataSetTime);
        parcel.writeParcelable(data.object, 0);
        byte[] byteArray = parcel.marshall();
        fileStream.write(byteArray);
        fileStream.flush();
        parcel.recycle();
    }
}
