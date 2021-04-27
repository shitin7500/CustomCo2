package com.custom.co2.activity;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.custom.co2.R;
import com.custom.co2.utils.AnimationUtils;
import com.custom.co2.utils.CustomDirection;
import com.custom.co2.utils.CustomMapClusterRenderer;
import com.custom.co2.utils.CustomModel;
import com.custom.co2.utils.DirectionListener;
import com.custom.co2.utils.DirectionUtils;
import com.custom.co2.utils.DirectionsJSONParser;
import com.custom.co2.utils.MapUtils;
import com.custom.co2.utils.MyItem;
import com.custom.co2.utils.Place;
import com.custom.co2.utils.Placelistener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.ui.IconGenerator;
import com.logicbeanzs.uberpolylineanimation.MapAnimator;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.tmall.ultraviewpager.UltraViewPager;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.custom.co2.utils.Constant.clearShaedPref;
import static com.custom.co2.utils.Constant.getShaedPref;
import static com.custom.co2.utils.Constant.showProgressDialog;
import static com.custom.co2.utils.Constant.spEmail;
import static com.custom.co2.utils.Constant.spUserId;
import static com.custom.co2.utils.Constant.spUsername;
import static com.custom.co2.utils.MapUtils.getScreenWidth;


public class HomeActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener,
        Placelistener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        DirectionListener, GoogleMap.OnCameraIdleListener,
        NavigationView.OnNavigationItemSelectedListener, GoogleMap.OnMarkerClickListener {

    private static final long INTERVAL = 1000 * 5; //1 minute
    private static final long defaultInterval = 1500;
    private long lastTimeClick = 0;
    private static final long FASTEST_INTERVAL = 1000 * 3;
    float screenRatio = 3.3f;
    List<LatLng> polyLineList;
    List<String> instruct;
    LinearLayout linMode;
    String trpSource = "";
    String trpDestination = "";
    String trpDestance = "";
    ImageView btn_car, btn_Walking, btn_cycle, btn_Bus, btn_Flight, btn_Train;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    CardView destCard;
    ImageButton back;
    Button dest;
    SlidingUpPanelLayout sliding_layout;
    Button fetch, btn_proceed;
    double totleKM = 0.0;
    String originLatlong = "";
    String destinationLatlong = "";
    String originAddress = "";
    String destinationAddress = "", RideDistance = "";
    UltraViewPager mRecyclerView;
    ViewGroup.MarginLayoutParams marginLayoutParams;
    ViewGroup.MarginLayoutParams marginLayoutParams1;
    // LinearLayout detLin;
    ImageView img;
    TextView txt;
    LatLng latLngUpdate;
    LatLng currentLatlong;
    Dialog progressDialog;
    CircleImageView img_profile;
    TextView tvUsername, tvEmail;
    DirectionUtils util;
    Place placeGet;

    int FuleSelectedPos = 0;
    int VehicleelectedPos = 0;

    Animation.AnimationListener uptop = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
            destCard.setVisibility(View.GONE);
            back.setVisibility(View.GONE);
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            //googleMap.setPadding(0,0,0,0);
            //googleMap.setPadding(0, 90, 0, getScreenHeight()/3);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };
    Animation.AnimationListener bottomup = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
            dest.setEnabled(false);
            destCard.setVisibility(View.GONE);
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            destCard.setVisibility(View.GONE);
            destCard.clearAnimation();
            MapUtils.createTopDownAnimation(HomeActivity.this, back, uptop);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };
    Animation.AnimationListener backdest = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
            dest.setEnabled(true);
            destCard.setVisibility(View.VISIBLE);
            linMode.setVisibility(View.GONE);
        }

        @Override
        public void onAnimationEnd(Animation animation) {

        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }
    };
    Animation.AnimationListener backtop = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            back.setVisibility(View.GONE);
            MapUtils.createTopDownAnimation(HomeActivity.this, destCard, backdest);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };
    CameraPosition position;
    TextView tv_timing_right;
    private GoogleMap googleMap;
    private SupportMapFragment mapFragment;
    private Handler handler;
    private Marker carMarker, carMarker1, desMarker, carMarker2, carMarker3, desMarker2, desMarker3;
    private String TAG = "HomeActivity";

    private Location mLastLoc;

    private ClusterManager<MyItem> mClusterManager;
    private Marker originMarker;
    private Marker destinationMarker;
    private Marker movingCabMarker = null;
    private LatLng previousLatLng = null;
    private LatLng currentLatLng = null;
    private Runnable runnable;
    private int index = 0;
    private String markerFlag = "BUS";

    /**
     * Method for manage location change requwst
     */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
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
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout1);
        NavigationView navigationView = findViewById(R.id.nav_view1);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        View hView = navigationView.getHeaderView(0);
        img_profile = hView.findViewById(R.id.img_profile);
        tvUsername = hView.findViewById(R.id.tvUsername);
        tvEmail = hView.findViewById(R.id.tvEmail);
        progressDialog = showProgressDialog(HomeActivity.this);

        tvUsername.setText(getShaedPref(HomeActivity.this, spUsername));
        tvEmail.setText(getShaedPref(HomeActivity.this, spEmail));

        CustomModel.getInstance().setListener(this);
        CustomDirection.getInstance().setListener(this);
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        handler = new Handler();
        dest = findViewById(R.id.destBtn);
        destCard = findViewById(R.id.destCard);
        img = findViewById(R.id.img);
        txt = findViewById(R.id.txt);
        fetch = findViewById(R.id.fetch);
        btn_proceed = findViewById(R.id.btn_proceed);
        btn_car = findViewById(R.id.btn_car);
        btn_Walking = findViewById(R.id.btn_Walking);
        btn_cycle = findViewById(R.id.btn_cycle);
        btn_Bus = findViewById(R.id.btn_bus);
        btn_Flight = findViewById(R.id.btn_flight);
        btn_Train = findViewById(R.id.btn_train);
        btn_Bus.setSelected(true);
        linMode = findViewById(R.id.lin_mode);
        sliding_layout = findViewById(R.id.sliding_layout);
        sliding_layout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        MapUtils.createTopDownAnimation(this, destCard, null);
        back = findViewById(R.id.back);
        mRecyclerView = findViewById(R.id.recycle);
        marginLayoutParams = (ViewGroup.MarginLayoutParams) mRecyclerView.getLayoutParams();
        mRecyclerView.setScrollMode(UltraViewPager.ScrollMode.HORIZONTAL);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int usableHeight = metrics.heightPixels;
        back.setOnClickListener(v -> {
            if (googleMap != null) {
                googleMap.clear();
                polyLineList = null;
                instruct = null;
            }
            sliding_layout.setPanelHeight(0);
            sliding_layout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
            MapUtils.createBottomUpAnimation(HomeActivity.this, back, backtop);
            position = new CameraPosition.Builder()
                    .target(new LatLng(mLastLoc.getLatitude(), mLastLoc.getLongitude()))
                    .zoom(16f)
                    .build();

            CameraUpdate update = CameraUpdateFactory
                    .newCameraPosition
                            (position);
            googleMap.animateCamera(update);
            if (carMarker != null) {
                carMarker.remove();
            }

            carMarker = googleMap.addMarker(new MarkerOptions().position(new LatLng(mLastLoc.getLatitude(), mLastLoc.getLongitude())).anchor(0.5f, 0.5f).
                    flat(true).icon(BitmapDescriptorFactory.fromResource(R.mipmap.destination)));
        });
        createLocationRequest();
        dest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLastLoc != null) {
                    Intent intent = new Intent(HomeActivity.this, SearchActivity.class);
                    intent.putExtra("lat", mLastLoc.getLatitude());
                    intent.putExtra("lon", mLastLoc.getLongitude());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(HomeActivity.this, destCard, destCard.getTransitionName());
                        startActivity(intent, optionsCompat.toBundle());
                    } else
                        startActivity(intent);
                }
            }
        });
        btn_Bus.setOnClickListener(view -> {
            markerFlag = "BUS";
            btn_Bus.setSelected(true);
            btn_car.setSelected(false);
            btn_Train.setSelected(false);
            btn_Flight.setSelected(false);
            btn_Walking.setSelected(false);
            btn_cycle.setSelected(false);
            if (placeGet.getSrcLatLng() != null) {
                String url = getDirectionsUrl(placeGet.getSrcLatLng(), placeGet.getDesrLatLng(), "driving");
                DownloadTask downloadTask = new DownloadTask();
                downloadTask.execute(url);
            }
        });
        btn_car.setOnClickListener(view -> {
            markerFlag = "CAR";
            btn_Bus.setSelected(false);
            btn_car.setSelected(true);
            btn_Train.setSelected(false);
            btn_Flight.setSelected(false);
            btn_Walking.setSelected(false);
            btn_cycle.setSelected(false);
            if (placeGet.getSrcLatLng() != null) {
                String url = getDirectionsUrl(placeGet.getSrcLatLng(), placeGet.getDesrLatLng(), "driving");
                DownloadTask downloadTask = new DownloadTask();
                downloadTask.execute(url);
            }
        });
        btn_Train.setOnClickListener(view -> {
            markerFlag = "TRAIN";
            btn_Bus.setSelected(false);
            btn_car.setSelected(false);
            btn_Train.setSelected(true);
            btn_Flight.setSelected(false);
            btn_Walking.setSelected(false);
            btn_cycle.setSelected(false);
            if (placeGet.getSrcLatLng() != null) {
                String url = getDirectionsUrl(placeGet.getSrcLatLng(), placeGet.getDesrLatLng(), "driving");
                DownloadTask downloadTask = new DownloadTask();
                downloadTask.execute(url);
            }
        });
        btn_Flight.setOnClickListener(view -> {
            markerFlag = "FLIGHT";
            btn_Bus.setSelected(false);
            btn_car.setSelected(false);
            btn_Train.setSelected(false);
            btn_Flight.setSelected(true);
            btn_Walking.setSelected(false);
            btn_cycle.setSelected(false);
            if (placeGet.getSrcLatLng() != null) {
                String url = getDirectionsUrl(placeGet.getSrcLatLng(), placeGet.getDesrLatLng(), "driving");
                DownloadTask downloadTask = new DownloadTask();
                downloadTask.execute(url);
            }
        });
        btn_Walking.setOnClickListener(view -> {
            markerFlag = "WALK";
            btn_Bus.setSelected(false);
            btn_car.setSelected(false);
            btn_Train.setSelected(false);
            btn_Flight.setSelected(false);
            btn_Walking.setSelected(true);
            btn_cycle.setSelected(false);
            if (placeGet.getSrcLatLng() != null) {
                String url = getDirectionsUrl(placeGet.getSrcLatLng(), placeGet.getDesrLatLng(), "walking");
                DownloadTask downloadTask = new DownloadTask();
                downloadTask.execute(url);
            }
        });
        btn_cycle.setOnClickListener(view -> {
            markerFlag = "CYCLE";
            btn_Bus.setSelected(false);
            btn_car.setSelected(false);
            btn_Train.setSelected(false);
            btn_Flight.setSelected(false);
            btn_Walking.setSelected(false);
            btn_cycle.setSelected(true);
            if (placeGet.getSrcLatLng() != null) {
                String url = getDirectionsUrl(placeGet.getSrcLatLng(), placeGet.getDesrLatLng(), "walking");
                DownloadTask downloadTask = new DownloadTask();
                downloadTask.execute(url);
            }
        });


        FrameLayout frameLayout = findViewById(R.id.sliderLin);
        marginLayoutParams1 = (ViewGroup.MarginLayoutParams) frameLayout.getLayoutParams();

        marginLayoutParams1.height = (int) (usableHeight / screenRatio);
        frameLayout.setLayoutParams(marginLayoutParams1);

    }

    /**
     * Override method for get place data
     */
    @Override
    public void onPlace(Place place) {
        this.placeGet = place;
        MapUtils.createBottomUpAnimation(this, destCard, bottomup);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        btn_proceed.setVisibility(View.VISIBLE);

        btn_proceed.setOnClickListener(view -> {
            if(SystemClock.elapsedRealtime() - lastTimeClick < defaultInterval){
                return;
            }
            lastTimeClick = SystemClock.elapsedRealtime();
            if (btn_proceed.getText().toString().equals("Start")) {
                new Handler().postDelayed(() -> showMovingCab(polyLineList), 100);
                linMode.setVisibility(View.GONE);
                btn_proceed.setText("Stop");
            } else {
                TripDialog();
            }
        });

        if (place.getSrcLatLng() != null) {
            String url = getDirectionsUrl(place.getSrcLatLng(), place.getDesrLatLng(), "driving");
            DownloadTask downloadTask = new DownloadTask();
            downloadTask.execute(url);
        }
    }

    /**
     * Override Method for get direction data
     */
    @Override
    public void onDirectionsData(DirectionUtils utils) {
        this.util = utils;
        Log.e("distac", util.getDistance());
    }

    /**
     * Method called when activity resumed
     */
    @Override
    public void onResume() {
        super.onResume();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            }
        }
        if (tvUsername != null && tvEmail != null) {
            tvUsername.setText(getShaedPref(HomeActivity.this, spUsername));
            tvEmail.setText(getShaedPref(HomeActivity.this, spEmail));
        }

    }

    /**
     * Method called when google api client connected
     */
    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (mGoogleApiClient.isConnected()) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            }
        }
    }

    /**
     * Method for handle back press
     */
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout1);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (btn_car.getVisibility() == View.VISIBLE && btn_proceed.getVisibility() == View.VISIBLE) {
            googleMap.clear();
            btn_proceed.setVisibility(View.GONE);
            destCard.setVisibility(View.VISIBLE);
            linMode.setVisibility(View.GONE);

            handler.removeCallbacks(runnable);
            dest.setEnabled(true);

        } else {
            super.onBackPressed();
            finish();
        }
    }

    /**
     * Method for callback when google api client connection suspended
     */
    @Override
    public void onConnectionSuspended(int i) {

    }

    /**
     * Method for build google api client
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    /**
     * Activity lifecycle method when activity starts
     */
    @Override
    protected void onStart() {
        super.onStart();
        buildGoogleApiClient();
    }

    /**
     * Activity lifecycle method when activity pause for long time
     */
    @Override
    protected void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    /**
     * Method for request the permission for location above android-6
     */
    private void makeLocationPermissionRequest() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
    }

    /**
     * Method for callback when permission accepted or denied
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        if (mGoogleApiClient.isConnected()) {
                            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
                        }
                        googleMap.setOnCameraIdleListener(this);
                    }
                } else {
                    makeLocationPermissionRequest();
                }
            }
        }
    }

    /**
     * Method for prepare direction url
     */
    private String getDirectionsUrl(LatLng origin, LatLng dest, String mapType) {
        String url = "";
        String strOrigin = "origin=" + origin.latitude + "," + origin.longitude;
        String strDest = "destination=" + dest.latitude + "," + dest.longitude;
        String strMode = "";
        String strKey = "key=" + getResources().getString(R.string.Map_Api_Key);

        if (mapType.equals("driving")) {
            strMode = "mode=driving";
            url = "https://maps.googleapis.com/maps/api/directions/json?" + strOrigin + "&" + strDest + "&" + strMode + "&" + strKey;
        } else {
            strMode = "mode=walking";
            url = "https://maps.googleapis.com/maps/api/directions/json?" + strOrigin + "&" + strDest + "&" + strMode + "&" + strKey;
        }
        return url;
    }

    /**
     * Async Method for grab data from enpoint from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();
            // Connecting to url
            urlConnection.connect();
            // Reading data from url
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuilder sb = new StringBuilder();
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
            br.close();
        } catch (Exception e) {
            Log.d("Exceptiondownloading", e.toString());
        } finally {
            if (iStream != null) {
                iStream.close();
            }
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return data;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    /**
     * Method for callback when side drawer item clicked
     */
    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        int id = menuItem.getItemId();


        if (id == R.id.nav_home) {
            startActivity(new Intent(getApplicationContext(), HomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        } else if (id == R.id.nav_Profile) {
            startActivity(new Intent(getApplicationContext(), UpdateProfileActivity.class));
        } else if (id == R.id.nav_Score_Board) {
            startActivity(new Intent(getApplicationContext(), StatisticsActivity.class));
        } else if (id == R.id.nav_Change_Password) {
            startActivity(new Intent(getApplicationContext(), changepasswordactivity.class));
        } else if (id == R.id.nav_Logout) {
            new AlertDialog.Builder(HomeActivity.this)
                    .setMessage("You want to Logout?")
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        clearShaedPref(HomeActivity.this);
                        startActivity(new Intent(getApplicationContext(), LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));

                    })
                    .setNegativeButton(android.R.string.no, null)
                    .show();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout1);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Method for callback when market clicked
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    /**
     * Method for start getting polyline
     */
    void staticPolyLine() {
        googleMap.clear();
        Log.e("Size", String.valueOf(polyLineList.get(0)));
        final int MAP_BOUND_PADDING = 180;
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng latLng : polyLineList) {
            builder.include(latLng);
        }
        final LatLngBounds bounds = builder.build();

        CameraUpdate mCameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, MAP_BOUND_PADDING);
        googleMap.animateCamera(mCameraUpdate);

        MapAnimator.getInstance().animateRoute(googleMap, polyLineList);
        startCarAnimation(polyLineList.get(0).latitude, polyLineList.get(0).longitude);
    }

    /**
     * Override method called when map is ready
     */
    @SuppressLint("ResourceType")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        this.googleMap.getUiSettings().setCompassEnabled(false);
        this.googleMap.getUiSettings().setMapToolbarEnabled(false);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        this.googleMap.setMyLocationEnabled(true);

        View mapView = mapFragment.getView();
        if (mapView != null && mapView.findViewById(1) != null) {
            View locationButton = ((View) mapView.findViewById(1).getParent()).findViewById(2);
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                    locationButton.getLayoutParams();
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 0, 30, 30);
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int locationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
            int locationPermission2 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
            if (locationPermission != PackageManager.PERMISSION_GRANTED && locationPermission2 != PackageManager.PERMISSION_GRANTED) {
                makeLocationPermissionRequest();
            } else {
                if (mGoogleApiClient.isConnected()) {
                    LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
                }
                googleMap.setOnCameraIdleListener(this);
            }
        }

        setUpClusterer();
    }

    /**
     * Method for callback when location changed
     */
    @Override
    public void onLocationChanged(final Location location) {
        if (polyLineList == null || polyLineList.size() == 0) {
            if (mLastLoc == null) {
                position = new CameraPosition.Builder()
                        .target(new LatLng(location.getLatitude(), location.getLongitude()))
                        .zoom(18f)
                        .build();

                CameraUpdate update = CameraUpdateFactory
                        .newCameraPosition(position);

                googleMap.animateCamera(update);
            }
        }
        mLastLoc = location;
    }

    /**
     * Method for set up map cluster
     */
    private void setUpClusterer() {
        mClusterManager = new ClusterManager<>(this, googleMap);
        googleMap.setOnCameraIdleListener(mClusterManager);
        googleMap.setOnMarkerClickListener(mClusterManager);
        mClusterManager.setAnimation(false);
        mClusterManager.setRenderer(new CustomMapClusterRenderer<>(this, googleMap, mClusterManager));
    }

    /**
     * Method for prepare polyline markers and ready for start car animation
     */
    @SuppressLint("SetTextI18n")
    private void startCarAnimation(Double latitude, Double longitude) {
        LatLng latLng = new LatLng(latitude, longitude);
        currentLatlong = latLng;
        if (carMarker != null) {
            carMarker.remove();
        }

        originMarker = addOriginDestinationMarkerAndGet(latLng);
        originMarker.setAnchor(1f, 1f);
        destinationMarker = addOriginDestinationMarkerAndGet(polyLineList.get(polyLineList.size() - 1));
        destinationMarker.setAnchor(1f, 1f);

        IconGenerator iconFactory_left = new IconGenerator(this);
        iconFactory_left.setBackground(null);
        View view_left = View.inflate(this, R.layout.custom_marker_infowindow_left, null);
        iconFactory_left.setContentView(view_left);
        LinearLayout ll_marker_left = view_left.findViewById(R.id.ll_marker_right);
        TextView tv_address_left = view_left.findViewById(R.id.tv_address_right);
        tv_address_left.setText(placeGet.getSrcAddress());


        originAddress = tv_address_left.getText().toString();
        ll_marker_left.setPadding((int) (getScreenWidth() / 1.9), 0, 0, 0);
        latLngUpdate = latLng;
        carMarker2 = googleMap.addMarker(new MarkerOptions().position(latLng)
                .anchor(0.5f, 0.5f)
                .icon(BitmapDescriptorFactory.fromBitmap(iconFactory_left.makeIcon("current"))));

        IconGenerator iconFactory_lef2 = new IconGenerator(this);
        iconFactory_lef2.setBackground(null);
        View view_lef2 = View.inflate(this, R.layout.custom_marker_infowindow_left, null);
        iconFactory_lef2.setContentView(view_lef2);
        LinearLayout ll_marker_lef2 = view_lef2.findViewById(R.id.ll_marker_right);
        TextView tv_timing_lef2 = view_lef2.findViewById(R.id.tv_timing_right);
        tv_timing_lef2.setText("5 mins");
        TextView tv_address_lef2 = view_lef2.findViewById(R.id.tv_address_right);
        tv_address_lef2.setText(placeGet.getSrcAddress());
        ll_marker_lef2.setPadding(0, 0, (int) (getScreenWidth() / 1.9), 0);
        carMarker3 = googleMap.addMarker(new MarkerOptions().position(latLng)
                .anchor(0.5f, 0.5f)
                .icon(BitmapDescriptorFactory.fromBitmap(iconFactory_lef2.makeIcon("current2"))));
        carMarker3.setVisible(false);


        IconGenerator iconFactory_right = new IconGenerator(this);
        iconFactory_right.setBackground(null);
        View view_right = View.inflate(this, R.layout.custom_marker_infowindow_right, null);
        iconFactory_right.setContentView(view_right);
        tv_timing_right = view_right.findViewById(R.id.tv_timing_right);
        if (util != null) {
            tv_timing_right.setText(util.getDuration());

            RideDistance = util.getDuration();
            trpDestance = util.getDistance();
            String[] separated = util.getDistance().replace(",", "").split(" ");
            totleKM = Double.parseDouble(separated[0]);
            originLatlong = placeGet.getSrcLat() + "," + placeGet.getSrcLon();
            destinationLatlong = placeGet.getDestLat() + "," + placeGet.getDestLon();
            Log.e("ddddddd", originLatlong + ":" + destinationLatlong);
        }
        TextView tv_address_right = view_right.findViewById(R.id.tv_address_right);
        tv_address_right.setText(placeGet.getDestAddress());
        destinationAddress = tv_address_right.getText().toString();
        Log.e("latLng", originAddress + "\n\n" + destinationAddress);

        LinearLayout ll_marker_right = view_right.findViewById(R.id.ll_marker_right);
        ll_marker_right.setPadding((int) (getScreenWidth() / 1.9), 0, 0, 0);
        desMarker2 = googleMap.addMarker(new MarkerOptions().position(polyLineList.get(polyLineList.size() - 1))
                .anchor(0.5f, 0.5f)
                .icon(BitmapDescriptorFactory.fromBitmap(iconFactory_right.makeIcon("destination"))));

        IconGenerator iconFactory_righ2 = new IconGenerator(this);
        iconFactory_righ2.setBackground(null);
        View view_righ2 = View.inflate(this, R.layout.custom_marker_infowindow_right, null);
        iconFactory_righ2.setContentView(view_righ2);
        tv_timing_right = view_righ2.findViewById(R.id.tv_timing_right);
        if (util != null) {
            tv_timing_right.setText(util.getDuration());

            RideDistance = util.getDistance();
            trpDestance = util.getDistance();
        }
        TextView tv_address_righ2 = view_righ2.findViewById(R.id.tv_address_right);
        tv_address_righ2.setText(placeGet.getDestAddress());
        trpSource = placeGet.getSrcAddress().toString();
        trpDestination = placeGet.getDestAddress().toString();
        trpDestance = "";
        LinearLayout ll_marker_righ2 = view_righ2.findViewById(R.id.ll_marker_right);
        ll_marker_righ2.setPadding(0, 0, (int) (getScreenWidth() / 1.9), 0);
        desMarker3 = googleMap.addMarker(new MarkerOptions().position(polyLineList.get(polyLineList.size() - 1))
                .anchor(0.5f, 0.5f)
                .icon(BitmapDescriptorFactory.fromBitmap(iconFactory_righ2.makeIcon("destination2"))));
        desMarker3.setVisible(false);

        double tempDistance = 0.0;
        tempDistance = Double.parseDouble(RideDistance.replace(" km", "").replace(" m", ""));

        if (tempDistance > 500) {
            btn_Flight.setVisibility(View.VISIBLE);
            btn_Walking.setVisibility(View.GONE);
            btn_cycle.setVisibility(View.GONE);
        } else if (tempDistance > 50 && tempDistance <= 500) {
            btn_Flight.setVisibility(View.GONE);
            btn_Walking.setVisibility(View.GONE);
            btn_cycle.setVisibility(View.GONE);
        } else {
            btn_Flight.setVisibility(View.GONE);
            btn_Walking.setVisibility(View.VISIBLE);
            btn_cycle.setVisibility(View.VISIBLE);
        }

        linMode.setVisibility(View.VISIBLE);
    }

    /**
     * Method for callback when camera is Idle
     */
    @Override
    public void onCameraIdle() {
        Projection projection = googleMap.getProjection();
        LatLng markerLocation = null;
        LatLng dropMarkerLocation = null;
        try {
            if (carMarker != null) {
                markerLocation = carMarker.getPosition();
            } else {
                markerLocation = new LatLng(0.0f, 0.0f);
            }
            if (desMarker != null) {
                dropMarkerLocation = desMarker.getPosition();
            } else {
                dropMarkerLocation = new LatLng(0.0f, 0.0f);
            }
            Point screenPosition = projection.toScreenLocation(markerLocation);
            Point screenPositiondrop = projection.toScreenLocation(dropMarkerLocation);

            final View mapview = mapFragment.getView();
            float maxX = 0;
            if (mapview != null) {
                maxX = mapview.getMeasuredWidth();
            }

            if (carMarker2 != null) {

                if (screenPosition.x > 100 && screenPosition.x > maxX / 2) {
                    Log.e("Side", "CarLeft");
                    carMarker3.setVisible(true);
                    carMarker2.setVisible(false);
                } else {
                    carMarker2.setVisible(true);
                    carMarker3.setVisible(false);
                    Log.e("Side", "CarRight");
                }
            }
            if (desMarker != null) {
                if (screenPositiondrop.x > 100 && screenPositiondrop.x > maxX / 2) {
                    desMarker3.setVisible(true);
                    desMarker2.setVisible(false);
                } else {
                    desMarker2.setVisible(true);
                    desMarker3.setVisible(false);
                    Log.e("Side", "DesRight");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method for activity lifecycle when activity is in backgound
     */
    @Override
    protected void onPause() {
        super.onPause();
    }

    /**
     * Method for show car animation & moving cab
     */
    private void showMovingCab(final List<LatLng> cabLatLngList) {
        handler = new Handler();
        runnable = () -> {
            if (cabLatLngList != null && index < cabLatLngList.size()) {
                updateCarLocation(cabLatLngList.get(index));
                handler.postDelayed(runnable, 900);
                ++index;
            } else {
                handler.removeCallbacks(runnable);
                TripDialog();
            }
        };
        handler.postDelayed(runnable, 1500);
    }

    /**
     * Method for update pinpoint location to show animation
     */
    private void updateCarLocation(LatLng latLng) {
        if (movingCabMarker == null) {
            movingCabMarker = addCarMarkerAndGet(latLng);
        }
        if (previousLatLng == null) {
            currentLatLng = latLng;
            previousLatLng = currentLatLng;
            movingCabMarker.setPosition(currentLatLng);
            movingCabMarker.setAnchor(0.5f, 0.5f);
        } else {
            previousLatLng = currentLatLng;
            currentLatLng = latLng;
            ValueAnimator valueAnimator = AnimationUtils.carAnimator();
            valueAnimator.addUpdateListener(animation -> {
                if (currentLatLng != null && previousLatLng != null) {
                    float multiplier = animation.getAnimatedFraction();
                    LatLng nextLocation = new LatLng(
                            multiplier * currentLatLng.latitude + (1 - multiplier) * previousLatLng.latitude,
                            multiplier * currentLatLng.longitude + (1 - multiplier) * previousLatLng.longitude);
                    movingCabMarker.setPosition(nextLocation);
                    float rotation = MapUtils.getRotation(previousLatLng, nextLocation);
                    movingCabMarker.setRotation(rotation);
                    movingCabMarker.setAnchor(0.5f, 0.5f);
                }
            });
            valueAnimator.start();
        }
        animateCamera(latLng);
    }

    /**
     * Method for show dialog when no route is found
     */
    private void NoRouteDialog() {
        final Dialog dialog = new Dialog(HomeActivity.this);
        dialog.setContentView(R.layout.dialog_no_route);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(false);
        DisplayMetrics displaymetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = (int) (displaymetrics.widthPixels * 0.90);
        dialog.getWindow().setAttributes(lp);
        TextView btnSubmit = dialog.findViewById(R.id.btn_submit);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                startActivity(new Intent(getApplicationContext(), HomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));

            }
        });
        dialog.show();
    }

    /**
     * Method for show trip dialog
     */
    private void TripDialog() {
        Boolean isCo2 = false;

        final Dialog dialog = new Dialog(HomeActivity.this);
        dialog.setContentView(R.layout.dialog_trip_complate);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(false);
        DisplayMetrics displaymetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = (int) (displaymetrics.widthPixels * 0.90);
        dialog.getWindow().setAttributes(lp);
        TextView txtSource = dialog.findViewById(R.id.txtsourcAddress);
        TextView txtDestination = dialog.findViewById(R.id.txtDestinationAddress);
        TextView txtDistance = dialog.findViewById(R.id.txtDestence);
        TextView txtCo2 = dialog.findViewById(R.id.txtCO2);
        TextView txtCo2Title = dialog.findViewById(R.id.txtCO2Title);
        TextView txtdec1 = dialog.findViewById(R.id.txtdec1);
        TextView txtdec2 = dialog.findViewById(R.id.txtdec2);

        Spinner spFuleType = dialog.findViewById(R.id.spFuelType);
        Spinner spVehicleType = dialog.findViewById(R.id.spVehicleType);

        if (markerFlag.equals("CAR")) {
            isCo2 = true;
            spFuleType.setVisibility(View.VISIBLE);
            spVehicleType.setVisibility(View.VISIBLE);
        } else if (markerFlag.equals("BUS") || markerFlag.equals("TRAIN") || markerFlag.equals("FLIGHT")) {
            isCo2 = true;
            spFuleType.setVisibility(View.GONE);
            spVehicleType.setVisibility(View.GONE);
            double BasicCo2 = 0.0;
            double co2value;


            if (markerFlag.equals("BUS")) {
                BasicCo2 = 0.089;
            } else if (markerFlag.equals("TRAIN")) {
                BasicCo2 = 0.06;
            } else {
                BasicCo2 = 0.1753;
            }

            if (RideDistance.contains(" km")) {
                co2value = BasicCo2 * Double.parseDouble(RideDistance.replace(" km", ""));
            } else {
                co2value = BasicCo2 * (Double.parseDouble(RideDistance.replace(" m", "")) / 1000);
            }
            txtCo2.setText(String.format("%.2f", co2value));
        } else {
            isCo2 = false;
            spFuleType.setVisibility(View.GONE);
            spVehicleType.setVisibility(View.GONE);
            txtdec2.setVisibility(View.GONE);
            txtCo2Title.setText("Total Calories");
            double basiccalories;
            double co2value;
            if (markerFlag.equals("WALK")) {
                basiccalories = 47;
            } else {
                basiccalories = 22;
            }

            if (RideDistance.contains(" km")) {
                co2value = basiccalories * Double.parseDouble(RideDistance.replace(" km", ""));
            } else {
                co2value = basiccalories * (Double.parseDouble(RideDistance.replace(" m", "")) / 1000);
            }
            txtCo2.setText(String.format("%.2f", co2value));
        }

        txtSource.setText(trpSource);
        txtDestination.setText(trpDestination);
        txtDistance.setText(RideDistance);
        spFuleType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 3 || position == 4) {
                    txtdec1.setVisibility(View.VISIBLE);
                } else {
                    txtdec1.setVisibility(View.GONE);
                }
                FuleSelectedPos = position;
                txtCo2.setText(String.format("%.2f",
                        calculateCO2(RideDistance)));
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spVehicleType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                VehicleelectedPos = position;
                txtCo2.setText(String.format("%.2f", calculateCO2(RideDistance)));
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        TextView btnSubmit = dialog.findViewById(R.id.btn_submit);

        Boolean finalIsCo = isCo2;
        btnSubmit.setOnClickListener(view -> {
            progressDialog.show();
            AddRecored(txtSource.getText().toString(),
                    txtDestination.getText().toString(),
                    txtDistance.getText().toString(),
                    txtCo2.getText().toString(),
                    dialog,
                    finalIsCo,
                    spFuleType.getSelectedItem().toString(),
                    spVehicleType.getSelectedItem().toString(), markerFlag

            );

        });
        if (!this.isFinishing())
            dialog.show();
    }

    /**
     * Method for calculate how much co2 consumed
     */
    private double calculateCO2(String distance) {

        double basicValue = 0;
        double co2value = 0;
        if (FuleSelectedPos == 0 && VehicleelectedPos == 0) {
            basicValue = 0.28;
        } else if (FuleSelectedPos == 0 && VehicleelectedPos == 1) {
            basicValue = 0.34;
        } else if (FuleSelectedPos == 0 && VehicleelectedPos == 2) {
            basicValue = 0.41;
        } else if (FuleSelectedPos == 1 && VehicleelectedPos == 0) {
            basicValue = 0.24;
        } else if (FuleSelectedPos == 1 && VehicleelectedPos == 1) {
            basicValue = 0.31;
        } else if (FuleSelectedPos == 1 && VehicleelectedPos == 2) {
            basicValue = 0.39;
        } else if (FuleSelectedPos == 2 && VehicleelectedPos == 0) {
            basicValue = 0.21;
        } else if (FuleSelectedPos == 2 && VehicleelectedPos == 1) {
            basicValue = 0.26;
        } else if (FuleSelectedPos == 2 && VehicleelectedPos == 2) {
            basicValue = 0.30;
        } else if (FuleSelectedPos == 3 && VehicleelectedPos == 0) {
            basicValue = 0.19;
        } else if (FuleSelectedPos == 3 && VehicleelectedPos == 1) {
            basicValue = 0.24;
        } else if (FuleSelectedPos == 3 && VehicleelectedPos == 2) {
            basicValue = 0.28;
        } else if (FuleSelectedPos == 4 && VehicleelectedPos == 0) {
            basicValue = 0.07;
        } else if (FuleSelectedPos == 4 && VehicleelectedPos == 1) {
            basicValue = 0.08;
        } else if (FuleSelectedPos == 4 && VehicleelectedPos == 2) {
            basicValue = 0.10;
        }

        if (distance.contains(" km")) {
            co2value = basicValue * Double.parseDouble(distance.replace(" km", ""));
        } else {
            co2value = basicValue * (Double.parseDouble(distance.replace(" m", "")) / 1000);
        }


        return co2value;
    }

    /**
     * Method for add record in firebase
     */
    private void AddRecored(String Source, String Destination, String Distance, String Co2, Dialog dialog, Boolean isCo2, String FuleType, String VehicleType, String rideType) {

        String date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        Map<String, Object> user = new HashMap<>();
        user.put("UserID", getShaedPref(HomeActivity.this, spUserId));
        user.put("Source", Source);
        user.put("Destination", Destination);
        user.put("Distance", Distance);
        user.put("Co2Type", isCo2);
        user.put("Co2", Co2);
        user.put("RideType", rideType);
        if (markerFlag.equals("CAR")) {
            user.put("VehicleType", VehicleType);
            user.put("FuleType", FuleType);
        } else {
            user.put("VehicleType", "");
            user.put("FuleType", "");
        }
        user.put("Date", date);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Co2-Report")
                .document("Co2-Report" + System.currentTimeMillis() / 1000).set(user)
                .addOnSuccessListener(aVoid -> {
                    dialog.dismiss();
                    startActivity(new Intent(getApplicationContext(), HomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));

                }).addOnFailureListener(e -> {
            Log.e(TAG, "registerDataToCloud: " + e.getMessage());
            progressDialog.dismiss();
        });
    }

    /**
     * Method for animation camera on map
     */
    private void animateCamera(LatLng latLng) {
        CameraPosition cameraPosition = CameraPosition.builder().target(latLng).zoom(16.5f).build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    /**
     * Method for get marker on basis of selection
     */
    private Marker addCarMarkerAndGet(LatLng latLng) {

        if (markerFlag.equals("CAR")) {
            return googleMap.addMarker(new MarkerOptions().position(latLng).anchor(0.5f, 0.5f).flat(true).icon(BitmapDescriptorFactory.fromResource(R.mipmap.new_car_small)));

        } else if (markerFlag.equals("BUS")) {
            return googleMap.addMarker(new MarkerOptions().position(latLng).anchor(0.5f, 0.5f).flat(true).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_bus)));
        } else if (markerFlag.equals("TRAIN")) {
            return googleMap.addMarker(new MarkerOptions().position(latLng).anchor(0.5f, 0.5f).flat(true).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_train)));
        } else if (markerFlag.equals("FLIGHT")) {
            return googleMap.addMarker(new MarkerOptions().position(latLng).anchor(0.5f, 0.5f).flat(true).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_air_plan)));
        } else if (markerFlag.equals("WALK")) {
            return googleMap.addMarker(new MarkerOptions().position(latLng).anchor(0.5f, 0.5f).flat(true).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_man)));
        } else
            return googleMap.addMarker(new MarkerOptions().position(latLng).anchor(0.5f, 0.5f).flat(true).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_bike)));
    }

    /**
     * Method for add source and destination marker
     */
    private Marker addOriginDestinationMarkerAndGet(LatLng latLng) {
        BitmapDescriptor bitmapDescriptor =
                BitmapDescriptorFactory.fromBitmap(MapUtils.getOriginDestinationMarkerBitmap());
        return googleMap.addMarker(
                new MarkerOptions().position(latLng).flat(true).icon(bitmapDescriptor));
    }

    /**
     * Method for Aynctask to get direction data
     */
    @SuppressLint("StaticFieldLeak")
    private class DownloadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);
        }
    }

    /**
     * Method for Asyntask to parse direction data
     */
    @SuppressLint("StaticFieldLeak")
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                if (!jObject.getString("status").equals("ZERO_RESULTS")) {
                    DirectionsJSONParser parser = new DirectionsJSONParser();
                    Log.e("JsonObject", jObject.toString());
                    routes = parser.parse(jObject);
                } else {
                    new Thread() {
                        public void run() {
                            HomeActivity.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    NoRouteDialog();
                                }
                            });
                        }
                    }.start();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            if (result != null && result.size() > 0) {
                polyLineList = new ArrayList<>();
                instruct = new ArrayList<>();
                for (int i = 0; i < result.size(); i++) {
                    List<HashMap<String, String>> path = result.get(i);
                    for (int j = 0; j < path.size(); j++) {
                        HashMap<String, String> point = path.get(j);
                        double lat = Double.parseDouble(point.get("lat"));
                        double lng = Double.parseDouble(point.get("lng"));
                        LatLng position = new LatLng(lat, lng);
                        polyLineList.add(position);
                        instruct.add(point.get("instructions"));
                    }
                }
                staticPolyLine();
            }
        }
    }


}