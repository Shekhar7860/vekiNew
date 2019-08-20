package com.onewayit.veki.fragment;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.loopj.android.http.BaseJsonHttpResponseHandler;
import com.onewayit.veki.Preferences.UserSessionPreferences;
import com.onewayit.veki.R;
import com.onewayit.veki.RestClient.RestClient;
import com.onewayit.veki.utilities.GlobalClass;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.message.BasicHeader;
import de.hdodenhof.circleimageview.CircleImageView;

public class ProposalDetails extends Fragment implements OnMapReadyCallback, View.OnClickListener {

    Context context;
    View view;
    ImageView back_button_home_activity;
    TextView tv_hire, name, tv_distance, tv_address, tv_desc, tv_offeredPrice, tv_feedback, tv_viewAll, tv_mobile_number;
    ListView lv_feedback;
    CircleImageView profile_image;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_request_details, container, false);
        findViewByID();
        setClickListener();
        setMapView();
        getProfile();
        return view;
    }

    private void setClickListener() {
        tv_hire.setOnClickListener(this);
        tv_viewAll.setOnClickListener(this);
        back_button_home_activity.setOnClickListener(this);

    }

    private void findViewByID() {
        tv_hire = view.findViewById(R.id.tv_hire);
        back_button_home_activity = view.findViewById(R.id.back_button_home_activity);
        tv_address = view.findViewById(R.id.tv_address);
        tv_desc = view.findViewById(R.id.tv_desc);
        tv_distance = view.findViewById(R.id.tv_distance);
        tv_feedback = view.findViewById(R.id.tv_feedback);
        tv_mobile_number = view.findViewById(R.id.tv_mobile_number);
        tv_offeredPrice = view.findViewById(R.id.tv_offeredPrice);
        tv_viewAll = view.findViewById(R.id.tv_viewAll);
        lv_feedback = view.findViewById(R.id.lv_feedback);
        profile_image = view.findViewById(R.id.profile_image);
        name = view.findViewById(R.id.name);
    }

    private void setMapView() {

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        googleMap.getUiSettings().setScrollGesturesEnabled(false);
        LatLng myloc = new LatLng(30.7352102, 76.6934882);
        googleMap.addMarker(new MarkerOptions()
                .position(myloc)
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker))
                .title("Mohali 7 Phase"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(myloc));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(30.7352102, 76.6934882), 12.0f));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_hire: {
                Bundle bundle = new Bundle();
                bundle.putString("price", getArguments().getString("price"));
                bundle.putString("requestId", getArguments().getString("request_id"));
                bundle.putString("user_id", getArguments().getString("service_user_id"));

                PaymentFragment fragment2 = new PaymentFragment();
                fragment2.setArguments(bundle);
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.replace(R.id.frame_layout, fragment2);
                fragmentTransaction.commit();
                break;
            }
            case R.id.back_button_home_activity: {
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.popBackStackImmediate();
                break;
            }
        }
    }

    public void getProfile() {
        final GlobalClass globalClass = new GlobalClass();
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Loading user data");
        progressDialog.show();
        globalClass.cancelProgressBarInterection(true, getActivity());
        RestClient restClient = new RestClient();
        String relativeUrl = "users/profile/" + getArguments().getString("service_user_id");
        List<Header> headers = new ArrayList<Header>();
        UserSessionPreferences sessionPreferences = new UserSessionPreferences(context);
        headers.add(new BasicHeader("Authorization", sessionPreferences.getToken()));
        Log.e("Auth and url  ", sessionPreferences.getToken() + " " + relativeUrl);
        restClient.getWithheader(context, relativeUrl, headers.toArray(new Header[headers.size()]), null, new BaseJsonHttpResponseHandler<JSONArray>() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, JSONArray response) {
                globalClass.cancelProgressBarInterection(false, getActivity());
                progressDialog.dismiss();
                Log.e("Response :", rawJsonResponse);
                try {
                    JSONObject object = new JSONObject(rawJsonResponse);
                    if (object.getString("status").equalsIgnoreCase("Success")) {
                        JSONObject userDetails = new JSONObject(object.getString("data"));
                        if (userDetails.getString("name") != "null") {
                            name.setText(userDetails.getString("name"));
                        }
                        if (userDetails.getString("photo") != "" && userDetails.getString("photo") != null) {
                            //   Log.d("photores", userDetails.getString("photo") );
                            Picasso.get().load(userDetails.getString("photo")).into(profile_image);
                        }
                        if (userDetails.getString("phone") != "null") {
                            tv_mobile_number.setText(userDetails.getString("phone_code") + userDetails.getString("phone"));
                        }
                        tv_offeredPrice.setText("$" + getArguments().getString("price"));
                        GlobalClass globalClass1 = new GlobalClass();
                        tv_distance.setText(String.format("%.2f", globalClass.distance(Double.parseDouble(getArguments().getString("lat")), Double.parseDouble(getArguments().getString("lon")), Double.parseDouble(userDetails.getString("latitude")), Double.parseDouble(userDetails.getString("longitude")))) + " Km");
//
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, JSONArray errorResponse) {
                progressDialog.dismiss();
                globalClass.cancelProgressBarInterection(false, getActivity());
            }

            @Override
            protected JSONArray parseResponse(String rawJsonData, boolean isFailure) throws Throwable {
                return null;
            }
        });
    }
}
