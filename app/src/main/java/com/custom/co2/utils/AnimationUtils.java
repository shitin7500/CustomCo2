package com.custom.co2.utils;


import android.animation.ValueAnimator;
import android.view.animation.LinearInterpolator;

public class AnimationUtils {

    public static ValueAnimator polylineAnimator() {
        ValueAnimator valueAnimator = ValueAnimator.ofInt(0, 100);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.setDuration(4000);
        return valueAnimator;
    }

    public static ValueAnimator carAnimator() {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0f, 1f);
        valueAnimator.setDuration(900);
        valueAnimator.setInterpolator(new LinearInterpolator());
        return valueAnimator;
    }

}
