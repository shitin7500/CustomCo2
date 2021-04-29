package com.custom.co2.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import com.custom.co2.R;
import com.custom.co2.adapter.PlaceAutocompleteAdapter;
import com.custom.co2.utils.CustomModel;
import com.custom.co2.utils.CustomPickUp;
import com.custom.co2.utils.CustomProgress;
import com.custom.co2.utils.MapUtils;
import com.custom.co2.utils.Place;
import com.custom.co2.utils.Placelistener;
import com.custom.co2.utils.ProgressListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class SearchActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,
        PlaceAutocompleteAdapter.PlaceAutoCompleteInterface, OnMapReadyCallback, LocationListener, Placelistener,
        ProgressListener {
    private RecyclerView mRecyclerView;
    PlaceAutocompleteAdapter mAdapter;
    private static final LatLngBounds BOUNDS_INDIA = new LatLngBounds(
            new LatLng(6.4626999, 68.1097),
            new LatLng(35.513327, 97.39535869999999));
    EditText mSearchEdittext;
    LocationRequest mLocationRequest;
    private static final long INTERVAL = 1000 * 5; //1 minute
    private static final long FASTEST_INTERVAL = 1000 * 3;
    private GoogleApiClient mClient;
    private GoogleMap googleMap;
    LatLng loc;
    private Marker carMarker;
    ImageView pin;
    FrameLayout frame;
    Button done;
    Place placeCust;
    String srcAdd = "";
    Button current;
    ImageView iv_line;
    LinearLayout linSetLocation;
    SupportMapFragment mapFragment;
    Handler handler = new Handler();
    private AnimatedVectorDrawableCompat avd;

    /**
     * Method for callback of place selected
     */
    @Override
    public void onPlace(Place place) {
        this.placeCust = place;
        if (current != null) {
            current.setText(placeCust.getSrcAddress());
        }
    }

    /**
     * Method for callback when activity resumed
     */
    @Override
    protected void onResume() {
        super.onResume();

    }

    /**
     * Method for start animation of view
     */
    private void startAnimation(boolean isStart) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (isStart) {
                repeatAnimation();
            } else {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        iv_line.removeCallbacks(action);
                        avd.stop();
                        iv_line.setVisibility(View.GONE);
                        iv_line.invalidate();
                    }
                }, 800);
//
            }
        }
    }

    private Runnable action = () -> repeatAnimation();

    /**
     * Method for repeat animation
     */
    private void repeatAnimation() {
        if (avd != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            iv_line.setVisibility(View.VISIBLE);
                            avd.start();
                            iv_line.postDelayed(action, 1000);
                            iv_line.invalidate();
                        }
                    });
                }
            }).start();
        }
    }

    /**
     * Method for destroy activity
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * Method for initialize layout
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            getWindow().setSharedElementEnterTransition(TransitionInflater.from(this)
                    .inflateTransition(R.transition.changebounds));
        }
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            loc = new LatLng(bundle.getDouble("lat"), bundle.getDouble("lon"));
        }

        mClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addApi(LocationServices.API)
                .build();
        setContentView(R.layout.activity_search);


        CustomPickUp.getInstance().setListener(this);
        CustomProgress.getInstance().setListener(this);
        findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBack();
            }
        });
        current = findViewById(R.id.cuurent);
        linSetLocation = findViewById(R.id.lin_set_location);
        iv_line = findViewById(R.id.iv_line);
        current.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SearchActivity.this, PickLocation.class);
                intent.putExtra("lat", loc.latitude);
                intent.putExtra("lon", loc.longitude);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    current.setTransitionName("current");
                    ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(SearchActivity.this, current, current.getTransitionName());
                    startActivity(intent, optionsCompat.toBundle());
                } else
                    startActivity(intent);
            }
        });
        Animation animation = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        findViewById(R.id.close).startAnimation(animation);
        pin = findViewById(R.id.pin);
        frame = findViewById(R.id.frame);
        done = findViewById(R.id.done);


        mSearchEdittext = findViewById(R.id.mSearchText);

        mSearchEdittext.setEnabled(false);
        mRecyclerView = findViewById(R.id.list_search);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(llm);
        avd = AnimatedVectorDrawableCompat.create(this, R.drawable.avd_line);
        iv_line.setBackground(avd);
        iv_line.setVisibility(View.GONE);

        mRecyclerView.setAdapter(mAdapter);
        final AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setCountry("IN")
                .build();
        mAdapter = new PlaceAutocompleteAdapter(this, mClient, BOUNDS_INDIA, typeFilter);
        mAdapter.setProgress(iv_line);


        MapUtils.slideUp(this, mRecyclerView, new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mRecyclerView.setAdapter(mAdapter);
                mSearchEdittext.setEnabled(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mSearchEdittext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                linSetLocation.setVisibility(View.VISIBLE);
                if (mRecyclerView.getVisibility() == View.GONE) {
                    MapUtils.slideUp(SearchActivity.this, mRecyclerView, new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            MapUtils.hideKeyboard(SearchActivity.this);
                            mSearchEdittext.setText(null);
                            mSearchEdittext.setInputType(InputType.TYPE_CLASS_TEXT);
                            mRecyclerView.clearAnimation();
                            mRecyclerView.setVisibility(View.VISIBLE);
                            frame.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                } else {
                    mSearchEdittext.setFocusable(true);
                    mSearchEdittext.setCursorVisible(true);
                    mSearchEdittext.requestFocus();
                    mSearchEdittext.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            if (inputMethodManager != null) {
                                inputMethodManager.showSoftInput(mSearchEdittext, InputMethodManager.SHOW_IMPLICIT);
                            }
                            mSearchEdittext.setFocusable(true);
                            mSearchEdittext.setCursorVisible(true);
                            mSearchEdittext.requestFocus();
                        }
                    }, 200);
                }
            }
        });
        LatLng ladw = new LatLng(loc.latitude, loc.longitude);
        try {
            Geocoder selected_place_geocoder = new Geocoder(SearchActivity.this);
            List<Address> address;
            address = selected_place_geocoder.getFromLocation(ladw.latitude, ladw.longitude, 1);
            Address location = address.get(0);
            if (location != null) {
                if (location.getLocality() != null) {
                    srcAdd = location.getAddressLine(0) + "," + location.getLocality() + "," + location.getCountryName();
                } else {
                    srcAdd = location.getAddressLine(0) + "," + location.getCountryName();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        mSearchEdittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count > 0) {
                    if (mAdapter != null) {
                        mRecyclerView.setAdapter(mAdapter);
                        Log.e("mAdapterSize", mAdapter.getItemCount() + "");
                    }
                } else {
                    if (mAdapter != null) {
                        mAdapter.clearList();
                        mAdapter.notifyDataSetChanged();
                        mRecyclerView.setAdapter(mAdapter);
                        Log.e("mAdapterSize1", mAdapter.getItemCount() + "");
                    }
                }
                if (!s.toString().equals("") && mClient.isConnected()) {
                    mAdapter.getFilter().filter(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        linSetLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                linSetLocation.setVisibility(View.GONE);
                MapUtils.hideKeyboard(SearchActivity.this);
                MapUtils.SlideToAbove(mRecyclerView, new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mRecyclerView.clearAnimation();
                        mRecyclerView.setVisibility(View.GONE);
                        frame.setVisibility(View.VISIBLE);
                        mSearchEdittext.setInputType(InputType.TYPE_NULL);
                        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                        mapFragment.getMapAsync(SearchActivity.this);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            }
        });
        com.google.android.libraries.places.api.Places.initialize(SearchActivity.this, getResources().getString(R.string.Map_Api_Key));

    }

    /**
     * Method for handle on back press
     */
    private void onBack() {
        MapUtils.SlideToAbove(mRecyclerView, new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                MapUtils.hideKeyboard(SearchActivity.this);
            }

            @Override
            public void onAnimationEnd(Animation anim) {
                mRecyclerView.clearAnimation();
                mRecyclerView.setVisibility(View.GONE);
                Animation animation = AnimationUtils.loadAnimation(SearchActivity.this, R.anim.slide_out_left);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        findViewById(R.id.close).setVisibility(View.GONE);
                        SearchActivity.super.onBackPressed();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                findViewById(R.id.close).startAnimation(animation);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    /**
     * Method for create location request
     */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (mClient.isConnected()) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mClient, mLocationRequest, this);
            }
        }
    }

    /**
     * Method for callback when activity start
     */
    @Override
    protected void onStart() {
        super.onStart();
        mClient.connect();
    }

    /**
     * Method for callback when activity is in background for long time
     */
    @Override
    protected void onStop() {
        mClient.disconnect();
        super.onStop();
    }

    /**
     * Method for callback when google api client connection failed
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    /**
     * Method for get callback when place clicked
     */
    @Override
    public void onPlaceClick(ArrayList<PlaceAutocompleteAdapter.PlaceAutocomplete> mResultList, int position) {
        onProgressChanged(false);
        if (mResultList != null) {
            try {
                final String placeId = String.valueOf(mResultList.get(position).placeId);
                Log.d("SelectPlaceId", placeId);

                PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mClient, placeId);
                placeResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
                    @Override
                    public void onResult(@NonNull PlaceBuffer places) {
                        try {
                            if (placeCust != null) {
                                Place place = new Place(new LatLng(placeCust.srcLat, placeCust.srcLon), getLocationFromAddress(SearchActivity.this, String.valueOf(mResultList.get(position).description)));
                                place.setSrcAddress(placeCust.getSrcAddress());
                                place.setDestAddress(String.valueOf(mResultList.get(position).description));
                                CustomModel.getInstance().changeState(place);
                            } else {
                                Place place = new Place(new LatLng(loc.latitude, loc.longitude), getLocationFromAddress(SearchActivity.this, String.valueOf(mResultList.get(position).description)));
                                place.setSrcAddress(srcAdd);
                                place.setDestAddress(String.valueOf(mResultList.get(position).description));
                                CustomModel.getInstance().changeState(place);
                            }
                            mSearchEdittext.setText(String.valueOf(mResultList.get(position).description));
                            onBack();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            MapUtils.hideKeyboard(SearchActivity.this);
            MapUtils.SlideToAbove(mRecyclerView, new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mRecyclerView.clearAnimation();
                    mRecyclerView.setVisibility(View.GONE);
                    frame.setVisibility(View.VISIBLE);
                    mSearchEdittext.setInputType(InputType.TYPE_NULL);
                    final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.map);
                    mapFragment.getMapAsync(SearchActivity.this);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }
    }

    /**
     * Method for get lat lng from address
     */
    public LatLng getLocationFromAddress(Context context, String strAddress) {
        Geocoder coder = new Geocoder(context);
        List<Address> address;
        LatLng p1 = null;
        try {
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }
            Address location = address.get(0);
            p1 = new LatLng(location.getLatitude(), location.getLongitude());
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return p1;
    }

    /**
     * Method for get callback when map is ready
     */
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMap = googleMap;
        this.googleMap.getUiSettings().setCompassEnabled(false);
        this.googleMap.getUiSettings().setMapToolbarEnabled(false);
        try {
            googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.custom_map));
        } catch (Resources.NotFoundException e) {
            Log.e("error", "Can't find style. Error: ", e);
        }
        createLocationRequest();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        googleMap.setMyLocationEnabled(true);
        googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onCameraMove() {
                done.setEnabled(false);
                done.setText("Fetching...");
            }
        });

        View mapView = mapFragment.getView();
        if (mapView != null && mapView.findViewById(0) != null) {
            View locationButton = ((View) mapView.findViewById(0).getParent()).findViewById(0);
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                    locationButton.getLayoutParams();
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 0, 30, 30);
        }
        googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onCameraIdle() {
                LatLng ladw = googleMap.getCameraPosition().target;
                try {
                    Geocoder selected_place_geocoder = new Geocoder(SearchActivity.this);
                    List<Address> address;
                    address = selected_place_geocoder.getFromLocation(ladw.latitude, ladw.longitude, 1);
                    Address location = address.get(0);
                    if (location != null) {
                        if (location.getLocality() != null) {
                            String awq = location.getAddressLine(0) + "," + location.getLocality() + "," + location.getCountryName();
                            mSearchEdittext.setText(awq);
                        } else {
                            String gst = location.getAddressLine(0) + "," + location.getCountryName();
                            mSearchEdittext.setText(gst);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                done.setText("Done");
                done.setEnabled(true);
            }
        });
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (done.getText().toString().equalsIgnoreCase("Done")) {
                    if (placeCust != null) {
                        Place place = new Place(new LatLng(placeCust.srcLat, placeCust.srcLon), googleMap.getCameraPosition().target);
                        place.setSrcAddress(placeCust.getSrcAddress());
                        place.setDestAddress(mSearchEdittext.getText().toString());
                        CustomModel.getInstance().changeState(place);
                    } else {
                        Place place = new Place(new LatLng(loc.latitude, loc.longitude), googleMap.getCameraPosition().target);
                        place.setSrcAddress(srcAdd);
                        place.setDestAddress(mSearchEdittext.getText().toString());
                        CustomModel.getInstance().changeState(place);
                    }
                    onBack();
                } else {
                    Toast.makeText(getApplicationContext(), "Wait for Location to be fetched.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Method for callback when location changed
     */
    @Override
    public void onLocationChanged(Location location) {
        if (carMarker == null) {
            carMarker = googleMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).flat(true).icon(BitmapDescriptorFactory.fromResource(R.mipmap.source)));
            carMarker.remove();
            googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                @Override
                public void onMapLoaded() {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 18), new GoogleMap.CancelableCallback() {
                        @Override
                        public void onFinish() {
                            pin.setVisibility(View.VISIBLE);
                            done.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onCancel() {
                            pin.setVisibility(View.VISIBLE);
                            done.setVisibility(View.VISIBLE);
                        }
                    });
                }
            });
        } else {
            carMarker.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
        }
    }

    /**
     * Method for get callback when progress changed
     */
    @Override
    public void onProgressChanged(boolean state) {
        startAnimation(state);
    }


}
