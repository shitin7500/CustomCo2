package com.custom.co2.utils;

public class CustomProgress {
    private static CustomProgress mInstance;
    private ProgressListener mListener;
    private CustomProgress() {}

    public static CustomProgress getInstance() {
        if(mInstance == null) {
            mInstance = new CustomProgress();
        }
        return mInstance;
    }

    public void setListener(ProgressListener listener) {
        mListener = listener;
    }

    public void changeState(boolean state) {
        if(mListener != null) {
            notifyStateChange(state);
        }
    }

    private void notifyStateChange(boolean state) {
        mListener.onProgressChanged(state);
    }
}
