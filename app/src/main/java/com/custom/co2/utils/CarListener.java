package com.custom.co2.utils;

import android.widget.ImageView;

import org.json.JSONObject;

public interface CarListener {
    public void onCarSelect(JSONObject ob, int position, ImageView im);
}
