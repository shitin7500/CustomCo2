package com.custom.co2.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;

import androidx.recyclerview.widget.RecyclerView;

import com.custom.co2.R;
import com.google.android.gms.maps.model.LatLng;

import static java.lang.Math.atan;

public class MapUtils {

    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static void createBottomUpAnimation(Context context, View view, Animation.AnimationListener listener) {
        Animation slideUpAnimation = AnimationUtils.loadAnimation(context,
                R.anim.slide_up_animation);
        if (listener != null) {
            slideUpAnimation.setAnimationListener(listener);
        }
        view.startAnimation(slideUpAnimation);
    }

    public static void createTopDownAnimation(Context context, View view, Animation.AnimationListener listener) {
        Animation slideDownAnimation = AnimationUtils.loadAnimation(context,
                R.anim.slide_down_animation);
        if (listener != null) {
            slideDownAnimation.setAnimationListener(listener);
        }
        view.startAnimation(slideDownAnimation);
    }

    public static void SlideToAbove(final RecyclerView rl_footer, Animation.AnimationListener listener) {
        Animation slide = null;
        slide = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, -5.0f);

        slide.setDuration(600);
        slide.setFillAfter(true);
        slide.setFillEnabled(true);
        rl_footer.startAnimation(slide);
        if (listener != null) {
            slide.setAnimationListener(listener);
        }
    }

    public static void slideUp(Context context, View view, Animation.AnimationListener listener) {
        view.setVisibility(View.VISIBLE);
        Animation animate = AnimationUtils.loadAnimation(context, R.anim.slide_down);
        animate.setFillAfter(true);
        if (listener != null) {
            animate.setAnimationListener(listener);
        }
        view.startAnimation(animate);
    }

    public static Bitmap getOriginDestinationMarkerBitmap() {
        int height = 20;
        int width = 20;
        Bitmap bitmap = Bitmap.createBitmap(height, width, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        canvas.drawRect(0F, 0F, width, height, paint);
        return  bitmap;
    }

    public static Float getRotation(LatLng start, LatLng end) {
        Double latDifference = Math.abs(start.latitude - end.latitude);
        Double lngDifference = Math.abs(start.longitude - end.longitude);
        float rotation = -1F;
        if(start.latitude < end.latitude && start.longitude < end.longitude) {
            rotation = (float) Math.toDegrees(atan(lngDifference / latDifference));
        } else if(start.latitude >= end.latitude && start.longitude < end.longitude) {
            rotation = (float) (90 - Math.toDegrees(atan(lngDifference / latDifference)) + 90);
        } else if(start.latitude >= end.latitude && start.longitude >= end.longitude) {
            rotation = (float) (Math.toDegrees(atan(lngDifference / latDifference)) + 180);
        } else if(start.latitude < end.latitude && start.longitude >= end.longitude) {
            rotation = (float) (90 - Math.toDegrees(atan(lngDifference / latDifference)) + 270);
        }
        return rotation;
    }

}
