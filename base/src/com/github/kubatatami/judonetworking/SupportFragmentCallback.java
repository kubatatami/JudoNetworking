package com.github.kubatatami.judonetworking;

import android.support.v4.app.Fragment;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 23.04.2013
 * Time: 11:40
 */
public class SupportFragmentCallback<T> extends Callback<T> {

    private final Fragment fragment;

    public SupportFragmentCallback(Fragment fragment) {
        this.fragment = fragment;
    }

    @Override
    public final void onStart() {
        if (fragment.getActivity() != null) {
            onSafeStart();
        }
    }

    public final void onFinish(T result) {
        if (fragment.getActivity() != null) {
            onSafeFinish(result);
        }
    }

    public final void onError(Exception e) {
        if (fragment.getActivity() != null) {
            onSafeError(e);
        }
    }

    @Override
    public final void onProgress(int progress) {
        if (fragment.getActivity() != null) {
            onSafeProgress(progress);
        }
    }

    public void onSafeStart() {

    }

    public void onSafeProgress(int progress) {

    }

    public void onSafeFinish(T result) {

    }

    public void onSafeError(Exception e) {
        if (e != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            LoggerImpl.log(sw.toString());
        } else {
            LoggerImpl.log("Null exception");
        }
    }


}
