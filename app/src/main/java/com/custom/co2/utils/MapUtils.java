package com.custom.co2.utils;

import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;

import androidx.recyclerview.widget.RecyclerView;

import com.custom.co2.R;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;


import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.atan;


public class MapUtils {

    public static List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
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

    // // TODO: 1/22/2017 please check
    void fixZoomProblem(LatLng source, LatLng distance, List<LatLng> points, GoogleMap googleMap) {

        LatLngBounds.Builder bc = new LatLngBounds.Builder();

        bc.include(source);
        bc.include(distance);

    /*  for (LatLng item : points) {
            bc.include(item);
        }
*/
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bc.build(), 50));
    }

    public interface LatLngInterpolatorNew {
        LatLng interpolate(float fraction, LatLng a, LatLng b);

        class LinearFixed implements LatLngInterpolatorNew {
            @Override
            public LatLng interpolate(float fraction, LatLng a, LatLng b) {
                double lat = (b.latitude - a.latitude) * fraction + a.latitude;
                double lngDelta = b.longitude - a.longitude;
                // Take the shortest path across the 180th meridian.
                if (Math.abs(lngDelta) > 180) {
                    lngDelta -= Math.signum(lngDelta) * 360;
                }
                double lng = lngDelta * fraction + a.longitude;
                return new LatLng(lat, lng);
            }
        }
    }

    public static void fitZoomWithScreen(Location source, Location distance, GoogleMap googleMap, Context context) {

        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        //the include method will calculate the min and max bound.
        builder.include(new LatLng(source.getLatitude(), source.getLongitude()));
        builder.include(new LatLng(distance.getLatitude(), distance.getLongitude()));

        //        builder.include(marker3.getPosition());
        //        builder.include(marker4.getPosition());
        LatLngBounds bounds = builder.build();

        int width = context.getResources().getDisplayMetrics().widthPixels;
        int height = context.getResources().getDisplayMetrics().heightPixels - 250;
        int padding = (int) (width * 0.20);
        // offset from edges of the map 12% of screen

        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);

        googleMap.animateCamera(cu);

    }

    public static ObjectAnimator BottomUpAnimation(View view,
                                                   AnimatorListenerAdapter listener, float distance) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationY", -distance);
        animator.setDuration(3200);
        animator.removeAllListeners();
        if (listener != null) {
            animator.addListener(listener);
        }
        return animator;
    }

    public static ObjectAnimator TopDownAnimation(View view, AnimatorListenerAdapter listener,
                                                  float distance) {
        view.setTranslationY(-distance);
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationY", 0);
        animator.removeAllListeners();
        if (listener != null) {
            animator.addListener(listener);
        }
        return animator;
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
    // https://stackoverflow.com/questions/24812483/how-to-create-bounds-of-a-android-polyline-in-order-to-fit-the-screen

    public static CameraUpdate moveToBounds(Context context, Polyline p) {

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        List<LatLng> arr = p.getPoints();
        for (int i = 0; i < arr.size(); i++) {
            builder.include(arr.get(i));
        }
        LatLngBounds bounds = builder.build();
        int padding = 100; // offset from edges of the map in pixels

        int width = context.getResources().getDisplayMetrics().widthPixels;
        int height = context.getResources().getDisplayMetrics().heightPixels / 2;

//        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);


        return cu;
//        mMap.animateCamera(cu);
    }

    public static void animateMarker(final LatLng destination, final Marker marker) {
        if (marker != null) {
            final LatLng startPosition = marker.getPosition();
            final LatLng endPosition = destination;

            final float startRotation = marker.getRotation();

            final LatLngInterpolator latLngInterpolator = new LatLngInterpolator.LinearFixed();
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
            valueAnimator.setDuration(1000); // duration 1 second
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    try {
                        float v = animation.getAnimatedFraction();
                        LatLng newPosition = latLngInterpolator.interpolate(v, startPosition, endPosition);
                        marker.setPosition(newPosition);

                        Location temp = null;
                        temp.setLatitude(destination.latitude);
                        temp.setLongitude(destination.longitude);

                        marker.setRotation(computeRotation(v, startRotation, temp.getBearing()));
                    } catch (Exception ex) {
                        // I don't care atm..
                    }
                }
            });

            valueAnimator.start();
        }
    }

    private static float computeRotation(float fraction, float start, float end) {
        float normalizeEnd = end - start; // rotate start to 0
        float normalizedEndAbs = (normalizeEnd + 360) % 360;

        float direction = (normalizedEndAbs > 180) ? -1 : 1; // -1 = anticlockwise, 1 = clockwise
        float rotation;
        if (direction > 0) {
            rotation = normalizedEndAbs;
        } else {
            rotation = normalizedEndAbs - 360;
        }

        float result = fraction * rotation + start;
        return (result + 360) % 360;
    }

    private interface LatLngInterpolator {
        LatLng interpolate(float fraction, LatLng a, LatLng b);

        class LinearFixed implements LatLngInterpolator {
            @Override
            public LatLng interpolate(float fraction, LatLng a, LatLng b) {
                double lat = (b.latitude - a.latitude) * fraction + a.latitude;
                double lngDelta = b.longitude - a.longitude;
                // Take the shortest path across the 180th meridian.
                if (Math.abs(lngDelta) > 180) {
                    lngDelta -= Math.signum(lngDelta) * 360;
                }
                double lng = lngDelta * fraction + a.longitude;
                return new LatLng(lat, lng);
            }
        }
    }

    public static float getBearing(LatLng begin, LatLng end) {
        double lat = Math.abs(begin.latitude - end.latitude);
        double lng = Math.abs(begin.longitude - end.longitude);

        if (begin.latitude < end.latitude && begin.longitude < end.longitude)
            return (float) (Math.toDegrees(atan(lng / lat)));
        else if (begin.latitude >= end.latitude && begin.longitude < end.longitude)
            return (float) ((90 - Math.toDegrees(atan(lng / lat))) + 90);
        else if (begin.latitude >= end.latitude && begin.longitude >= end.longitude)
            return (float) (Math.toDegrees(atan(lng / lat)) + 180);
        else if (begin.latitude < end.latitude && begin.longitude >= end.longitude)
            return (float) ((90 - Math.toDegrees(atan(lng / lat))) + 270);
        return -1;
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

    public static void SlideToDown(final View rl_footer, Animation.AnimationListener listener) {
        Animation slide = null;
        slide = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, 5.2f);

        slide.setDuration(800);
        slide.setFillAfter(true);
        slide.setFillEnabled(true);
        rl_footer.startAnimation(slide);
        if (listener != null) {
            slide.setAnimationListener(listener);
        }
        slide.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {

                rl_footer.clearAnimation();

            }

        });

    }

    public static void expand(final View v) {
        v.measure(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        final int targetHeight = v.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? WindowManager.LayoutParams.MATCH_PARENT
                        : (int) (targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int) (targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
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



    public static void collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    v.setVisibility(View.GONE);
                } else {
                    v.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int) (initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
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

    public static Bitmap getCarBitmap(Context context) {
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.new_car_small);
        return Bitmap.createScaledBitmap(bitmap, 50, 100, false);
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
