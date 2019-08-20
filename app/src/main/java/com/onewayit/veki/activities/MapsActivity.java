package com.onewayit.veki.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.crystal.crystalrangeseekbar.interfaces.OnRangeSeekbarChangeListener;
import com.crystal.crystalrangeseekbar.interfaces.OnRangeSeekbarFinalValueListener;
import com.crystal.crystalrangeseekbar.widgets.CrystalRangeSeekbar;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.navigation.NavigationView;
import com.loopj.android.http.BaseJsonHttpResponseHandler;
import com.onewayit.veki.GetterSetter.RequestsData;
import com.onewayit.veki.Preferences.UserSessionPreferences;
import com.onewayit.veki.R;
import com.onewayit.veki.RestClient.RestClient;
import com.onewayit.veki.fragment.MyProposals;
import com.onewayit.veki.fragment.MyRequests;
import com.onewayit.veki.fragment.MyServices;
import com.onewayit.veki.fragment.NewRequestFragment;
import com.onewayit.veki.fragment.NotificationFragment;
import com.onewayit.veki.fragment.ProfileFragment;
import com.onewayit.veki.fragment.RequestDetails;
import com.onewayit.veki.utilities.GPSTracker;
import com.onewayit.veki.utilities.GlobalClass;
import com.onewayit.veki.utilities.Network;
import com.shashank.sony.fancygifdialoglib.FancyGifDialog;
import com.shashank.sony.fancygifdialoglib.FancyGifDialogListener;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.ByteArrayEntity;
import de.hdodenhof.circleimageview.CircleImageView;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener, View.OnClickListener, NavigationView.OnNavigationItemSelectedListener, AdapterView.OnItemClickListener {

    final Handler handler = new Handler();
    TextView tv_help, view_details, tv_apply;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ImageView iv_menu, filter, alert, iv_clear;
    CardView card1;
    RelativeLayout marker_details, rl_distance_bar, rl_searchLoc;
    boolean doubleBackToExitPressedOnce = false;
    AutoCompleteTextView ac_location;
    HandlerThread mHandlerThread;
    Handler mThreadHandler;
    LatLng ac_latlng;
    Marker ac_marker;
    Double lat;
    Double lon;
    ArrayList<RequestsData> requestsList;
    private GoogleMap mMap;
    private PlacesAutoCompleteAdapter mAdapter;

    public static Bitmap createDrawableFromView(Context context, View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    public void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(ac_location.getWindowToken(),
                InputMethodManager.RESULT_UNCHANGED_SHOWN);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        findViewIds();
        checkPermission();
        setOnClickListeners();
        setDistanceListener();
        setPlaceAdapter();
        checkExistingUser();
        checkIntent();
    }

    private void checkExistingUser() {
        UserSessionPreferences sessionPreferences = new UserSessionPreferences(this);

        if (sessionPreferences.getUserID() != null && !sessionPreferences.getUserID().equalsIgnoreCase("")) {
            navigationView.getMenu().findItem(R.id.signin).setTitle("Signout");
        } else {
            navigationView.getMenu().findItem(R.id.signin).setTitle("Signin");
        }
    }

    private void setPlaceAdapter() {
        mAdapter = new PlacesAutoCompleteAdapter(this, R.layout.auto_complete_listitem);
        ac_location.setAdapter(mAdapter);
        if (mThreadHandler == null) {
            // Initialize and start the HandlerThread
            // which is basically a Thread with a Looper
            // attached (hence a MessageQueue)
            mHandlerThread = new HandlerThread("", android.os.Process.THREAD_PRIORITY_BACKGROUND);
            mHandlerThread.start();

            // Initialize the Handler
            mThreadHandler = new Handler(mHandlerThread.getLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    if (msg.what == 1) {
                        ArrayList<String> results = mAdapter.resultList;

                        if (results != null && results.size() > 0) {
                            mAdapter.notifyDataSetChanged();
                        } else {
                            mAdapter.notifyDataSetInvalidated();
                        }
                    }
                }
            };
        }

    }

    private void setDistanceListener() {
        // get seekbar from view
        final CrystalRangeSeekbar rangeSeekbar = findViewById(R.id.rangeSeekbar3);

// get min and max text view
        final TextView tvMin = findViewById(R.id.tv_min1);
        final TextView tvMax = findViewById(R.id.tv_max1);
        final UserSessionPreferences userSessionPreferences = new UserSessionPreferences(this);

// set listener
        rangeSeekbar.setOnRangeSeekbarChangeListener(new OnRangeSeekbarChangeListener() {
            @Override
            public void valueChanged(Number minValue, Number maxValue) {
                tvMin.setText(minValue + "-");
                tvMax.setText(maxValue + " km");
                userSessionPreferences.setDistanceStart(String.valueOf(minValue));
                userSessionPreferences.setDistanceEnd(String.valueOf(maxValue));
            }
        });

// set final value listener
        rangeSeekbar.setOnRangeSeekbarFinalValueListener(new OnRangeSeekbarFinalValueListener() {
            @Override
            public void finalValue(Number minValue, Number maxValue) {
                tvMin.setText(minValue + "-");
                tvMax.setText(maxValue + " km");
            }
        });
    }

    private void setOnClickListeners() {
        card1.setOnClickListener(this);
        tv_help.setOnClickListener(this);
        filter.setOnClickListener(this);
        iv_menu.setOnClickListener(this);
        alert.setOnClickListener(this);
        tv_apply.setOnClickListener(this);
        iv_clear.setOnClickListener(this);
        navigationView.setNavigationItemSelectedListener(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            drawerLayout.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    drawerLayout.bringToFront();
                    drawerLayout.requestLayout();
                }
            });
        }
        requestsList = new ArrayList<>();
    }

    private void findViewIds() {
        navigationView = findViewById(R.id.nav_view);
        tv_help = findViewById(R.id.tv_help);
        drawerLayout = findViewById(R.id.drawer_layout);
        tv_help.setVisibility(View.VISIBLE);
        marker_details = findViewById(R.id.marker_details);
        rl_distance_bar = findViewById(R.id.rl_distance_bar);
        view_details = findViewById(R.id.view_details);
        filter = findViewById(R.id.filter);
        card1 = findViewById(R.id.card1);
        iv_menu = findViewById(R.id.menu);
        alert = findViewById(R.id.alert);
        tv_apply = findViewById(R.id.tv_apply);
        ac_location = findViewById(R.id.ac_location);
        ac_location.setOnItemClickListener(this);
        rl_searchLoc = findViewById(R.id.rl_searchLoc);
        iv_clear = findViewById(R.id.iv_clear);
    }

    public void checkPermission() {

        if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        } else {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapClickListener(this);
        UserSessionPreferences sessionPreferences = new UserSessionPreferences(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        View locationButton = ((View) findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
// position on right bottom
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        rlp.setMargins(0, 200, 200, 0);

        GPSTracker gpsTracker = new GPSTracker(this);
        gpsTracker.getLocation();
        if (gpsTracker.canGetLocation()) {
            lat = gpsTracker.getLatitude();
            lon = gpsTracker.getLongitude();
            sessionPreferences.setLat(String.valueOf(lat));
            sessionPreferences.setLon(String.valueOf(lon));

            LatLng latLng1 = new LatLng(lat, lon);
            View marker = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker_layout, null);
            CircleImageView iv_user = marker.findViewById(R.id.iv_user);
            iv_user.setImageDrawable(getResources().getDrawable(R.mipmap.ic_marker_app));
            Marker myLoc = mMap.addMarker(new MarkerOptions()
                    .position(latLng1)
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_app))
                    .title("My location"));
            myLoc.setTag("ac_marker");
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng1));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng1, 16.0f));
            getRequestsByTimer();
        }
    }

    private void getRequestsByTimer() {
        final int FIVE_SECONDS = 5000;
        handler.postDelayed(new Runnable() {
            public void run() {
                getNearByRequests();
                // this method will contain your almost-finished HTTP calls
                handler.postDelayed(this, FIVE_SECONDS);
            }
        }, FIVE_SECONDS);
    }

    private void addMarkers() {
        for (int i = 0; i < requestsList.size(); i++) {
            View marker = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker_layout, null);
            final CircleImageView iv_user = marker.findViewById(R.id.iv_user);
            Picasso.get().load(requestsList.get(i).getPhoto()).into(iv_user);
            LatLng latLng = new LatLng(Double.parseDouble(requestsList.get(i).getLatitude()), Double.parseDouble(requestsList.get(i).getLongitude()));
            String location = "Unnamed location";
            if (requestsList.get(i).getAddress() == null) {
                location = requestsList.get(i).getAddress();
            }
            Marker markerservices;
            markerservices = mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(this, marker))).title(location));
            markerservices.setTag(requestsList.get(i).getTag());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.map);
                    mapFragment.getMapAsync(this);
                } else {
                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.map);
                    mapFragment.getMapAsync(this);
                }
                return;
            }
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (marker.getTag() == "ac_marker") {
            marker_details.setVisibility(View.GONE);
            rl_distance_bar.setVisibility(View.GONE);
        } else if (String.valueOf(marker.getTag()).startsWith("request")) {

            setMarkerDetails(String.valueOf(marker.getTag()));
            marker_details.setVisibility(View.VISIBLE);
            rl_distance_bar.setVisibility(View.GONE);
        } else {
            setMarkerDetails(String.valueOf(marker.getTag()));
            marker_details.setVisibility(View.VISIBLE);
            rl_distance_bar.setVisibility(View.GONE);
        }
        return false;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        marker_details.setVisibility(View.GONE);
        rl_distance_bar.setVisibility(View.GONE);
        tv_help.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        Network network = new Network(this);
        if (!network.isConnectedToInternet()) {
            network.noInternetAlertBox(this, false);
            return;
        }
        marker_details.setVisibility(View.GONE);
        rl_searchLoc.setVisibility(View.GONE);
        tv_help.setVisibility(View.GONE);
        hideKeyboard(this);
        UserSessionPreferences sessionPreferences = new UserSessionPreferences(this);
        switch (v.getId()) {
            case R.id.tv_apply: {
                rl_distance_bar.setVisibility(View.GONE);
                rl_searchLoc.setVisibility(View.VISIBLE);
                tv_help.setVisibility(View.VISIBLE);
                getNearByRequests();
                break;
            }
            case R.id.tv_help: {
                if (sessionPreferences.getUserID()==null || sessionPreferences.getUserID().equalsIgnoreCase("null") || sessionPreferences.getUserID().equalsIgnoreCase("")) {
                    Intent intent = new Intent(this, LoginActivity.class);
                    startActivity(intent);
                }
                else if (sessionPreferences.getUserFirstName().equalsIgnoreCase("null") || sessionPreferences.getUserFirstName().equalsIgnoreCase("") || sessionPreferences.getMobile().equalsIgnoreCase("null") || sessionPreferences.getMobile().equalsIgnoreCase("")) {
                    openPopup();
                } else {
                    getSupportFragmentManager().beginTransaction().setCustomAnimations(android.R.anim.slide_out_right, android.R.anim.slide_out_right).replace(R.id.frame_layout, new NewRequestFragment(), "myProfile").addToBackStack(null).commit();
                    card1.setVisibility(View.GONE);
                    tv_help.setVisibility(View.GONE);
                    handler.removeCallbacksAndMessages(null);
                }
                break;
            }
            case R.id.menu: {

                drawerLayout.openDrawer(Gravity.START);
                break;
            }
            case R.id.alert: {
                if (sessionPreferences.getUserID()==null || sessionPreferences.getUserID().equalsIgnoreCase("") || sessionPreferences.getUserID().equalsIgnoreCase("null")) {
                    Intent intent = new Intent(this, LoginActivity.class);
                    startActivity(intent);
                }  else if (sessionPreferences.getUserFirstName().equalsIgnoreCase("") || sessionPreferences.getUserFirstName().equalsIgnoreCase("null")  || sessionPreferences.getMobile().equalsIgnoreCase("") || sessionPreferences.getMobile().equalsIgnoreCase("null")) {
                    openPopup();
                } else {
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new NotificationFragment(), "myProfile").addToBackStack(null).commit();
                    card1.setVisibility(View.GONE);
                    rl_distance_bar.setVisibility(View.GONE);
                    handler.removeCallbacksAndMessages(null);
                }
                break;
            }
            case R.id.filter: {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(ac_location.getWindowToken(),
                        InputMethodManager.RESULT_UNCHANGED_SHOWN);
                rl_distance_bar.setVisibility(View.VISIBLE);
                //marker_details.setVisibility(View.VISIBLE);
                rl_searchLoc.setVisibility(View.VISIBLE);
                tv_help.setVisibility(View.VISIBLE);
                break;
            }
            case R.id.iv_clear: {
                ac_location.setText("");
                ac_location.setVisibility(View.VISIBLE);
                ac_location.setFocusable(true);
                rl_searchLoc.setVisibility(View.VISIBLE);
                tv_help.setVisibility(View.VISIBLE);
                break;
            }
        }
    }

    private void openPopup() {
        new FancyGifDialog.Builder(MapsActivity.this)
                .setTitle("Update your profile")
                .setMessage("Do you want to continue with the profile update?")
                .setNegativeBtnText("Cancel")
                .setPositiveBtnBackground("#131E69")
                .setPositiveBtnText("Yes")
                .setNegativeBtnBackground("#FFA9A7A8")
                .setGifResource(R.drawable.think_gif)   //Pass your Gif here
                .isCancellable(false)
                .OnPositiveClicked(new FancyGifDialogListener() {
                    @Override
                    public void OnClick() {
                        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new ProfileFragment(), "myProfile").addToBackStack(null).commit();
                        card1.setVisibility(View.GONE);
                        tv_help.setVisibility(View.GONE);
                    }
                })
                .OnNegativeClicked(new FancyGifDialogListener() {
                    @Override
                    public void OnClick() {
                        rl_searchLoc.setVisibility(View.VISIBLE);
                        tv_help.setVisibility(View.VISIBLE);
                    }
                })
                .build();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        navigationView.getMenu().findItem(menuItem.getItemId()).setCheckable(true).setChecked(true);
        Intent intent;
        UserSessionPreferences sessionPreferences = new UserSessionPreferences(this);

        Network network = new Network(this);
        if (!network.isConnectedToInternet()) {
            network.noInternetAlertBox(this, false);
            return false;
        }

        hideFields();
        hideKeyboard();
        for (int i = 0; i < getSupportFragmentManager().getBackStackEntryCount(); ++i) {
            getSupportFragmentManager().popBackStack();
        }
        switch (menuItem.getItemId()) {
            case R.id.Home: {
                //do somthing
                intent = new Intent(this, MapsActivity.class);
                startActivity(intent);
                Objects.requireNonNull(this).finish();
                break;
            }
            case R.id.MyRequests: {
                hideKeyboard();
                if (sessionPreferences.getUserID() == "" || sessionPreferences.getUserID() == null) {
                    intent = new Intent(this, LoginActivity.class);
                    startActivity(intent);
                } else {
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new MyRequests(), "requestFragment").commit();
                    //do somthing
                }
                break;
            }
            case R.id.MyServices: {
                hideKeyboard();
                if (sessionPreferences.getUserID() == null || sessionPreferences.getUserID() == "") {
                    intent = new Intent(this, LoginActivity.class);
                    startActivity(intent);
                } else {
                    handler.removeCallbacksAndMessages(null);
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new MyServices(), "requestFragment").addToBackStack(null).commit();
                    //do somthing
                }
                break;
            }
            case R.id.Proposal: {
                hideKeyboard();
                if (sessionPreferences.getUserID() == "" || sessionPreferences.getUserID() == null) {
                    intent = new Intent(this, LoginActivity.class);
                    startActivity(intent);
                } else {
                    handler.removeCallbacksAndMessages(null);
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new MyProposals(), "requestFragment").addToBackStack(null).commit();
                    //do somthing
                }
                break;
            }
            case R.id.Profile: {
                hideKeyboard();
                if (sessionPreferences.getUserID() == "" || sessionPreferences.getUserID() == null) {
                    intent = new Intent(this, LoginActivity.class);
                    startActivity(intent);
                } else {
                    handler.removeCallbacksAndMessages(null);
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new ProfileFragment(), "myProfile").addToBackStack(null).commit();
                    //do somthing
                }
                break;
            }
            case R.id.Support: {
                hideKeyboard();
                showFields();
                if (sessionPreferences.getUserID() == "" || sessionPreferences.getUserID() == null) {
                    intent = new Intent(this, LoginActivity.class);
                    startActivity(intent);
                } else {
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                    emailIntent.setData(Uri.parse("mailto:dev@veki.app"));
                    startActivity(emailIntent);
                    //do somthing
                }
                break;
            }
            case R.id.signout: {
                //do somthing
                sessionPreferences.setUserID("");
                sessionPreferences.setMobile("");
                sessionPreferences.setUserFirstName("");
                sessionPreferences.setEmailId("");
                sessionPreferences.setAddress("");
                sessionPreferences.setUserFirstName("");
                sessionPreferences.setToken("");
                intent = new Intent(this, MapsActivity.class);
                startActivity(intent);
                Objects.requireNonNull(this).finish();
                break;
            }
            case R.id.signin: {
                //do somthing
                sessionPreferences = new UserSessionPreferences(this);
                sessionPreferences.setUserID("");
                sessionPreferences.setMobile("");
                sessionPreferences.setUserFirstName("");
                sessionPreferences.setEmailId("");
                sessionPreferences.setAddress("");
                sessionPreferences.setUserFirstName("");
                sessionPreferences.setToken("");
                intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                break;
            }
        }
        //close navigation drawer
        drawerLayout.closeDrawer(Gravity.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        GlobalClass globalClass = new GlobalClass();
        globalClass.cancelProgressBarInterection(false, this);
        getSupportFragmentManager().popBackStackImmediate();
        if (getSupportFragmentManager().getBackStackEntryCount() < 1) {
            showFields();
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }
    }

    public void hideKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {

        }
    }

    public void showFields() {
        card1.setVisibility(View.VISIBLE);
        tv_help.setVisibility(View.VISIBLE);
        rl_searchLoc.setVisibility(View.VISIBLE);
    }

    public void hideFields() {
        marker_details.setVisibility(View.GONE);
        rl_searchLoc.setVisibility(View.GONE);
        card1.setVisibility(View.GONE);
        tv_help.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Get rid of our Place API Handlers
        if (mThreadHandler != null) {
            mThreadHandler.removeCallbacksAndMessages(null);
            mHandlerThread.quit();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        hideKeyboard(this);
        if (i == 0) {
            Intent intent = new Intent(this, MarkerDragActivity.class);
            startActivityForResult(intent, 1);
        } else {
            hideKeyboard(this);
            getLatLongFromLocation(mAdapter.resultList.get(i));
        }
    }

    public void setLocationOnMap(String locName, double Lat, double Long) {
        if (ac_marker != null) {
            ac_marker.remove();
        }
        View marker = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker_layout, null);
        CircleImageView iv_user = marker.findViewById(R.id.iv_user);
        iv_user.setImageDrawable(getResources().getDrawable(R.drawable.veki_logo));
        ac_marker = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(Lat, Long))
                .icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(this, marker)))
                .title(locName));
        ac_marker.setTag("ac_marker");
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(Lat, Long)));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Lat, Long), 16.0f));
    }

    public void getLatLongFromLocation(String locName) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        String result = null;
        // Toast.makeText(context,"hereee",Toast.LENGTH_SHORT).show();
        try {
            List addressList = geocoder.getFromLocationName(locName, 1);
            if (addressList != null && addressList.size() > 0) {
                Address address = (Address) addressList.get(0);
                StringBuilder sb = new StringBuilder();
                sb.append(address.getLatitude()).append("\n");
                sb.append(address.getLongitude()).append("\n");
                lat = address.getLatitude();
                lon = address.getLongitude();
                setLocationOnMap(locName, address.getLatitude(), address.getLongitude());
                getNearByRequests();
            }
        } catch (IOException e) {
            Log.e("", "Unable to connect to Geocoder", e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            lat = Double.valueOf(data.getStringExtra("Lat"));
            lon = Double.valueOf(data.getStringExtra("Long"));
            ac_location.setText(data.getStringExtra("locAddress"));
            setLocationOnMap("My Location", lat, lon);
            getNearByRequests();
        }
    }

    private void getNearByRequests() {
        // startProgress();
        final GlobalClass globalClass = new GlobalClass();
        // globalClass.cancelProgressBarInterection(true,this);
        RestClient restClient = new RestClient();
        final String relativeUrl = "requests/nearby";
        ByteArrayEntity entity = null;
        //Log.e("password is=",encrptPass);
        try {
            entity = new ByteArrayEntity((getNearByRequestParameters().toString()).getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        restClient.getRequestJson(this, relativeUrl, entity, "application/json", new BaseJsonHttpResponseHandler<JSONArray>() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, JSONArray response) {
                Log.e("response", rawJsonResponse);
                requestsList.clear();
                mMap.clear();
                // globalClass.cancelProgressBarInterection(false,MapsActivity.this);
                try {
                    JSONObject object = new JSONObject(rawJsonResponse);
                    if (object.getString("status").equalsIgnoreCase("Success")) {
                        JSONArray requests_data = object.getJSONArray("data");
                        if (requests_data.length() == 0) {
                        } else if (requests_data.length() > 0) {
                            //  closeProgress(true);
                            for (int i = 0; i < requests_data.length(); i++) {
                                RequestsData requestsData = new RequestsData();
                                JSONObject request = new JSONObject(String.valueOf(requests_data.get(i)));
                                if (request.getString("request_status").equalsIgnoreCase("0")) {
                                    requestsData.setLatitude(request.getString("latitude"));
                                    requestsData.setLongitude(request.getString("longitude"));
                                    requestsData.setName(request.getString("name"));
                                    requestsData.setEmail(request.getString("email"));
                                    requestsData.setPhone_code(request.getString("phone_code"));
                                    requestsData.setPhone(request.getString("phone"));
                                    requestsData.setAddress(request.getString("address"));
                                    requestsData.setId(request.getString("id"));
                                    requestsData.setServiceName(request.getString("service_name"));
                                    requestsData.setMinServicePrice(request.getString("amount"));
                                    requestsData.setDistance(request.getString("distance"));
                                    requestsData.setPhoto(request.getString("photo"));
                                    requestsData.setNotes(request.getString("notes"));
                                    requestsData.setTimeAgo(String.valueOf(globalClass.getTimeDifference(request.getString("updated_at"))) + "");
                                    requestsData.setTag("request " + i);
                                    requestsList.add(requestsData);
                                }
                            }
                            addMarkers();
                        }
                    } else {
                        //  closeProgress(false);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, JSONArray errorResponse) {
                // Toast.makeText(MapsActivity.this,"Error while fetching requests...",Toast.LENGTH_SHORT).show();
                // globalClass.cancelProgressBarInterection(false,MapsActivity.this);
                // closeProgress(false);
            }

            @Override
            protected JSONArray parseResponse(String rawJsonData, boolean isFailure) throws Throwable {
                return null;
            }
        });
    }

    private JSONObject getNearByRequestParameters() {
        JSONObject jsonObject = new JSONObject();
        UserSessionPreferences userSessionPreferences = new UserSessionPreferences(this);
        try {
            jsonObject.put("latitude", lat);
            jsonObject.put("longitude", lon);
            jsonObject.put("user_id", userSessionPreferences.getUserID());
            jsonObject.put("distance_start", userSessionPreferences.getDistanceStart());
            jsonObject.put("distance_end", userSessionPreferences.getDistanceEnd());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    private void setMarkerDetails(String tag) {
        CircleImageView imageView = findViewById(R.id.profile_image);
        TextView name = findViewById(R.id.name);
        TextView time_ago = findViewById(R.id.time_ago);
        TextView acci_type = findViewById(R.id.acci_type);
        TextView mobile_number = findViewById(R.id.mobile_number);
        TextView distance = findViewById(R.id.distance);
        TextView rating = findViewById(R.id.rating);

        int position = 0;

        for (int i = 0; i < requestsList.size(); i++) {
            if (requestsList.get(i).getTag().equalsIgnoreCase(tag)) {
                position = i;
            }
        }
        if (tag.startsWith("request")) {
            name.setText(requestsList.get(position).getName());
            time_ago.setText(requestsList.get(position).getTimeAgo());
            acci_type.setText(requestsList.get(position).getServiceName());
            mobile_number.setText(requestsList.get(position).getPhone_code() + " " + requestsList.get(position).getPhone());
            if (requestsList.get(position).getDistance() != "") {
                distance.setText(String.format("%.2f", Double.parseDouble(requestsList.get(position).getDistance())) + " km");
            }
            if (requestsList.get(position).getPhoto() != null) {
                Picasso.get().load(requestsList.get(position).getPhoto()).into(imageView);
            }
            rating.setText("0.0 (0 reviews)");
        }
        final int finalPosition = position;
        view_details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Network network = new Network(MapsActivity.this);
                if (!network.isConnectedToInternet()) {
                    network.noInternetAlertBox(MapsActivity.this, false);
                    return;
                }
                UserSessionPreferences sessionPreferences = new UserSessionPreferences(MapsActivity.this);
                if (sessionPreferences.getMobile() != null && sessionPreferences.getMobile() != "") {
                    handler.removeCallbacksAndMessages(null);
                    card1.setVisibility(View.GONE);
                    hideFields();
                    RequestDetails fragment2 = new RequestDetails();
                    Bundle bundle = new Bundle();
                    bundle.putString("name", requestsList.get(finalPosition).getName());
                    bundle.putString("source", "marker");
                    bundle.putString("id", requestsList.get(finalPosition).getId());
                    bundle.putString("lat", requestsList.get(finalPosition).getLatitude());
                    bundle.putString("lon", requestsList.get(finalPosition).getLongitude());
                    bundle.putString("distance", requestsList.get(finalPosition).getDistance());
                    bundle.putString("phone_code", requestsList.get(finalPosition).getPhone_code());
                    bundle.putString("phone", requestsList.get(finalPosition).getPhone());
                    bundle.putString("notes", requestsList.get(finalPosition).getNotes());
                    bundle.putString("photo", requestsList.get(finalPosition).getPhoto());
                    bundle.putString("timeAgo", requestsList.get(finalPosition).getTimeAgo());
                    bundle.putString("servicetype", requestsList.get(finalPosition).getServiceName());
                    fragment2.setArguments(bundle);
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.replace(R.id.frame_layout, fragment2);
                    fragmentTransaction.commit();
                } else {
                    Intent intent = new Intent(MapsActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    public void openMyRequests() {

        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra("openFragment", "myRequests");
        startActivity(intent);
        this.finish();
    }

    public void checkIntent() {
        if (getIntent().getStringExtra("openFragment") != null && getIntent().getStringExtra("openFragment").equalsIgnoreCase("myRequests")) {
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new MyRequests(), "requestFragment").commit();
            hideFields();
        }
    }
}
