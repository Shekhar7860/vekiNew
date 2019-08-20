package com.onewayit.veki.fragment;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.onewayit.veki.Adapters.CustomInfoWindowGoogleMap;
import com.onewayit.veki.Adapters.ServicesAdapter;
import com.onewayit.veki.GetterSetter.ServiceProviderData;
import com.onewayit.veki.Preferences.UserSessionPreferences;
import com.onewayit.veki.R;
import com.onewayit.veki.RestClient.RestClient;
import com.onewayit.veki.activities.MapsActivity;
import com.onewayit.veki.utilities.GlobalClass;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.ByteArrayEntity;
import cz.msebera.android.httpclient.message.BasicHeader;
import pl.droidsonroids.gif.GifImageView;

public class ProposalReceivedFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener, View.OnClickListener, SlidingUpPanelLayout.PanelSlideListener {
    Context context;
    View view;
    SlidingUpPanelLayout slidingUpPanelLayout;
    ImageView iv_downup, filter, back_button_home_activity;
    RecyclerView recyclerView;
    ArrayList<ServiceProviderData> items;
    LinearLayout dragView;
    TextView tv_noServiceProvider, tv_apply;
    RelativeLayout rl_loading, rl_distance_bar;
    GifImageView gifnoresult;
    int count = 0;
    String distanceStart = "0";
    String distanceEnd = "20";
    String requestSent;
    private GoogleMap mMap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_proposal_received, container, false);
        context = getActivity();
        findViewIds();
        // checkPermission();
        setOnClickListener();
        loadMap();
        setDistanceListener();
        fetchByTimer(false);
        return view;
    }

    private void fetchByTimer(final boolean retry) {
        Log.e("request_sent 3", getArguments().getString("request_sent"));
        requestSent = getArguments().getString("request_sent");
        if (requestSent.equalsIgnoreCase("success") || retry) {
            rl_loading.setVisibility(View.VISIBLE);
            tv_noServiceProvider.setVisibility(View.VISIBLE);
            tv_noServiceProvider.setText("Searching for service providers...");
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
            // gifnoresult.setGifImageResource(R.drawable.loading_providers);
            gifnoresult.setVisibility(View.VISIBLE);
            final int FIVE_SECONDS = 5000;
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    count++;
                    if (count < 25) {
                        if (requestSent.equalsIgnoreCase("fail")) {
                            resendRequest();
                            handler.postDelayed(this, FIVE_SECONDS);
                        } else {
                            fetchServiceProviders();          // this method will contain your almost-finished HTTP calls
                            handler.postDelayed(this, FIVE_SECONDS);
                        }
                    } else {
                        if (items.size() > 0) {
                            addMarkers();
                            setAdapter();
                            rl_loading.setVisibility(View.GONE);
                            gifnoresult.setVisibility(View.GONE);
                            tv_noServiceProvider.setVisibility(View.GONE);
                            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                            handler.removeCallbacksAndMessages(null);
                        } else {
                            gifnoresult.setVisibility(View.GONE);
                            tv_noServiceProvider.setVisibility(View.VISIBLE);
                            tv_noServiceProvider.setText("No service providers found nearby");

                        }
                    }
                }
            }, FIVE_SECONDS);
        } else {
            rl_loading.setVisibility(View.VISIBLE);
            gifnoresult.setVisibility(View.GONE);
            tv_noServiceProvider.setVisibility(View.VISIBLE);
            tv_noServiceProvider.setText("No service providers found nearby. Please try adjusting your distance");
        }
    }

    private void setAdapter() {
        ServicesAdapter servicesAdapter = new ServicesAdapter(items, getFragmentManager());
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(servicesAdapter);
    }

    private void setOnClickListener() {
        slidingUpPanelLayout.addPanelSlideListener(this);
        tv_apply.setOnClickListener(this);
        filter.setOnClickListener(this);
        rl_loading.setOnClickListener(this);
        back_button_home_activity.setOnClickListener(this);
    }


    private void findViewIds() {
        slidingUpPanelLayout = view.findViewById(R.id.sliding_layout);
        //slidingUpPanelLayout.setAnchorPoint(0.5f);
        iv_downup = view.findViewById(R.id.iv_downup);
        recyclerView = view.findViewById(R.id.recycler_view);
        gifnoresult = view.findViewById(R.id.gifnoresult);
        dragView = view.findViewById(R.id.dragView);
        tv_noServiceProvider = view.findViewById(R.id.tv_noServiceProvider);
        rl_loading = view.findViewById(R.id.rl_loading);
        tv_apply = view.findViewById(R.id.tv_apply);
        filter = view.findViewById(R.id.filter);
        back_button_home_activity=view.findViewById(R.id.back_button_home_activity);
        rl_distance_bar = view.findViewById(R.id.rl_distance_bar);
        items = new ArrayList<>();
    }

    public void loadMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapClickListener(this);
        LatLng latLng1 = new LatLng(Double.parseDouble(getArguments().getString("lat")), Double.parseDouble(getArguments().getString("lon")));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng1));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng1, 12.0f));
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

    }

    private void addMarkers() {
        LatLng latLng1 = null;
        for (int i = 0; i < items.size(); i++) {
            latLng1 = new LatLng(items.get(i).getLat(), items.get(i).getLon());
            Marker m1 = mMap.addMarker(new MarkerOptions()
                    .position(latLng1)
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_app))
                    .title("Mohali 7 Phase"));
            m1.setTag(i);
            CustomInfoWindowGoogleMap customInfoWindow = new CustomInfoWindowGoogleMap(context, getFragmentManager(), items);
            mMap.setInfoWindowAdapter(customInfoWindow);
            final int finalI = i;
            mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    Bundle bundle = new Bundle();
                    bundle.putString("price", items.get(finalI).getPrice());
                    bundle.putString("requestId", getArguments().getString("requestId"));
                    bundle.putString("user_id", items.get(finalI).getUser_id());
                    PaymentFragment fragment2 = new PaymentFragment();
                    fragment2.setArguments(bundle);
                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.frame_layout, fragment2);
                    fragmentTransaction.commit();
                }
            });
        }
        if (latLng1 != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng1));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng1, 12.0f));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                            .findFragmentById(R.id.map);
                    mapFragment.getMapAsync(this);
                } else {
                    getActivity().finish();
                }
                return;
            }
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();

        return false;
    }

    @Override
    public void onMapClick(LatLng latLng) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_apply: {
                rl_distance_bar.setVisibility(View.GONE);
                fetchByTimer(true);
                break;
            }
            case R.id.filter: {
                rl_distance_bar.setVisibility(View.VISIBLE);
                //marker_details.setVisibility(View.VISIBLE);
                break;
            }
            case R.id.rl_loading: {
                rl_distance_bar.setVisibility(View.GONE);
                //marker_details.setVisibility(View.VISIBLE);
                break;
            }
            case R.id.back_button_home_activity: {
                ((MapsActivity)context).onBackPressed();
                //marker_details.setVisibility(View.VISIBLE);
                break;
            }
        }
    }

    @Override
    public void onPanelSlide(View panel, float slideOffset) {

    }

    @Override
    public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {

        if (newState == SlidingUpPanelLayout.PanelState.EXPANDED || newState == SlidingUpPanelLayout.PanelState.HIDDEN) {
            iv_downup.setRotation((float) 0);
            recyclerView.setNestedScrollingEnabled(false);
        }
        if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED || newState == SlidingUpPanelLayout.PanelState.DRAGGING) {
            iv_downup.setRotation((float) 180);
            recyclerView.setNestedScrollingEnabled(true);
        }
    }

    private void fetchServiceProviders() {
        RestClient restClient = new RestClient();
        final UserSessionPreferences sessionPreferences = new UserSessionPreferences(context);
        final String relativeUrl = "requests/" + getArguments().getString("requestId");
        ByteArrayEntity entity = null;
        //Log.e("password is=",encrptPass);
        List<Header> headers = new ArrayList<Header>();
        headers.add(new BasicHeader("Authorization", sessionPreferences.getToken()));
        Log.e("Token and url", relativeUrl + " " + headers);
        restClient.getWithheader(getActivity(), relativeUrl, headers.toArray(new Header[headers.size()]), null, new BaseJsonHttpResponseHandler<JSONArray>() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, JSONArray response) {
                Log.e("response", rawJsonResponse);
                try {
                    JSONObject object = new JSONObject(rawJsonResponse);
                    if (object.getString("status").equalsIgnoreCase("Success")) {
                        items.clear();
                        JSONObject request = object.getJSONObject("data");
                        JSONArray imagesArray = request.getJSONArray("service_users");
                        if (imagesArray.length() > 0) {
                            for (int i = 0; i < imagesArray.length(); i++) {
                                ServiceProviderData serviceProviderData = new ServiceProviderData();
                                JSONObject serviceProvider = new JSONObject(String.valueOf(imagesArray.get(i)));
                                JSONObject providerDetails = serviceProvider.getJSONObject("user_details");
                                JSONObject statusDetails = serviceProvider.getJSONObject("status_details");
                                if (statusDetails.getString("status").equalsIgnoreCase("1")) {
                                    serviceProviderData.setName(providerDetails.getString("name"));
                                    serviceProviderData.setPhone(providerDetails.getString("phone_code") + providerDetails.getString("phone"));
                                    serviceProviderData.setLat(Double.valueOf(providerDetails.getString("latitude")));
                                    serviceProviderData.setLon(Double.valueOf(providerDetails.getString("longitude")));
                                    serviceProviderData.setImage(providerDetails.getString("photo"));
                                    serviceProviderData.setUser_id(providerDetails.getString("id"));
                                    serviceProviderData.setRequest_id(getArguments().getString("requestId"));
                                    serviceProviderData.setRequestLat(getArguments().getString("lat"));
                                    serviceProviderData.setRequestLon(getArguments().getString("lon"));
                                    JSONObject serviceDetails = serviceProvider.getJSONObject("service_details");
                                    serviceProviderData.setPrice(serviceDetails.getString("price"));
                                    items.add(serviceProviderData);
                                }
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, JSONArray errorResponse) {
            }

            @Override
            protected JSONArray parseResponse(String rawJsonData, boolean isFailure) throws Throwable {
                return null;
            }
        });
    }

    private void setDistanceListener() {
        // get seekbar from view
        final CrystalRangeSeekbar rangeSeekbar = view.findViewById(R.id.rangeSeekbar3);

// get min and max text view
        final TextView tvMin = view.findViewById(R.id.tv_min1);
        final TextView tvMax = view.findViewById(R.id.tv_max1);
        final UserSessionPreferences userSessionPreferences = new UserSessionPreferences(context);

// set listener
        rangeSeekbar.setOnRangeSeekbarChangeListener(new OnRangeSeekbarChangeListener() {
            @Override
            public void valueChanged(Number minValue, Number maxValue) {
                tvMin.setText(minValue + "-");
                tvMax.setText(maxValue + " km");
                distanceStart = String.valueOf(minValue);
                distanceEnd = String.valueOf(maxValue);
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

    public void resendRequest() {
        final UserSessionPreferences sessionPreferences = new UserSessionPreferences(context);
        RestClient restClient = new RestClient();
        String relativeUrl = "requests/" + getArguments().getString("requestId") + "/resend";
        Log.e("userID", sessionPreferences.getUserID());
        ByteArrayEntity entity = null;
        final GlobalClass globalClass = new GlobalClass();
        //Log.e("password is=",encrptPass);
        try {
            entity = new ByteArrayEntity((getRequestParams().toString()).getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        List<Header> headers = new ArrayList<Header>();
        headers.add(new BasicHeader("Authorization", sessionPreferences.getToken()));
        Log.e("Token ", sessionPreferences.getToken());
        Log.e("Params", getRequestParams().toString());
        restClient.postWithHeader(context, relativeUrl, headers.toArray(new Header[headers.size()]), entity, "application/json", new BaseJsonHttpResponseHandler<JSONArray>() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, JSONArray response) {
                Log.e("response", rawJsonResponse);
                JSONObject object = new JSONObject();
                try {
                    object = new JSONObject(rawJsonResponse);
                    if (statusCode == 200 && object.getString("status").equalsIgnoreCase("Success")) {
                        JSONObject request = object.getJSONObject("data");
                        JSONArray service_providers = request.getJSONArray("service_users");
                        if (service_providers.length() > 0) {
                            requestSent = "success";
                        } else {
                            requestSent = "fail";
                        }
                    } else if (statusCode == 200 && object.getString("status").equalsIgnoreCase("Error")) {
                        requestSent = "fail";
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, JSONArray errorResponse) {
                JSONObject object = null;
                try {
                    object = new JSONObject(rawJsonData);
                    if (statusCode == 400 && object.getString("status").equalsIgnoreCase("Error")) {
                        requestSent = "fail";
                    } else {
                        requestSent = "fail";
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected JSONArray parseResponse(String rawJsonData, boolean isFailure) throws Throwable {
                return null;
            }
        });
    }

    private JSONObject getRequestParams() {
        JSONObject jsonObject = new JSONObject();
        UserSessionPreferences userSessionPreferences = new UserSessionPreferences(context);
        try {
            jsonObject.put("id", getArguments().getString("requestId"));
            jsonObject.put("distance_start", distanceStart);
            jsonObject.put("distance_end", distanceEnd);
            //jsonObject.put("body",body);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
}
