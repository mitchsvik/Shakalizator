package com.mitchsvik.shakalizator;

import android.app.Application;

public final class App extends Application {
    private static App sInstance;
    private byte[] mCapturedPhotoData;

    public byte[] getCapturedPhotoData() {
        return mCapturedPhotoData;
    }

    public void setCapturedPhotoData(byte[] capturedPhotoData) {
        mCapturedPhotoData = capturedPhotoData;
    }

    public static App getInstance() { return sInstance; }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
    }
}