package com.onewayit.veki.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.onewayit.veki.R;
import com.onewayit.veki.utilities.GPSTracker;
import com.onewayit.veki.utilities.LocationAddress;

public class MarkerDragActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {

    RelativeLayout marker_details, rl_distance_bar;
    Marker location;
    Button btn_submit;
    ImageView back_button_home_activity;
    LatLng selectedLoc;
    String locAddress = "";
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drag_map);
        findViewIds();
        checkPermission();
        setOnClickListeners();
    }

    private void setOnClickListeners() {
        btn_submit.setOnClickListener(this);
        back_button_home_activity.setOnClickListener(this);

    }

    private void findViewIds() {
        btn_submit = findViewById(R.id.btn_submit);
        back_button_home_activity = findViewById(R.id.back_button_home_activity);
    }

    public void checkPermission() {
        if (ActivityCompat.checkSelfPermission(MarkerDragActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MarkerDragActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MarkerDragActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        addMarkers();
    }

    private void addMarkers() {

        GPSTracker gpsTracker = new GPSTracker(this);
        if (gpsTracker.canGetLocation()) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(gpsTracker.getLatitude(), gpsTracker.getLongitude()), 18.0f));
            mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                @Override
                public void onCameraIdle() {
                    selectedLoc = mMap.getCameraPosition().target;

                }
            });
        } else {
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gpsTracker.showSettingsAlert();
        }
        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                selectedLoc = mMap.getCameraPosition().target;
                LocationAddress locationAddress = new LocationAddress();
                locationAddress.getAddressFromLocation(selectedLoc.latitude, selectedLoc.longitude,
                        getApplicationContext(), new GeocoderHandler());
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //   SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    //        .findFragmentById(R.id.map);
                    //mapFragment.getMapAsync(this);
                } else {
                    this.finish();
                }
                return;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_submit: {
                Intent intent = getIntent();
                intent.putExtra("Lat", String.valueOf(selectedLoc.latitude));
                intent.putExtra("Long", String.valueOf(selectedLoc.longitude));
                intent.putExtra("locAddress", locAddress);
                setResult(RESULT_OK, intent);
                finish();
                break;
            }
            case R.id.back_button_home_activity: {
                finish();
                break;
            }
        }
    }

    private class GeocoderHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            String locationAddress;
            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    locationAddress = bundle.getString("address");
                    break;
                default:
                    locationAddress = null;
            }
            locAddress = locationAddress;
        }
    }
}
