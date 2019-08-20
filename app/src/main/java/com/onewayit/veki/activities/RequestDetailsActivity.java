package com.onewayit.veki.activities;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.JsonObject;
import com.loopj.android.http.BaseJsonHttpResponseHandler;
import com.onewayit.veki.Preferences.UserSessionPreferences;
import com.onewayit.veki.R;
import com.onewayit.veki.RestClient.RestClient;
import com.onewayit.veki.fragment.PaymentFragment;
import com.onewayit.veki.fragment.ProposalDetails;
import com.onewayit.veki.utilities.GlobalClass;
import com.shashank.sony.fancygifdialoglib.FancyGifDialog;
import com.shashank.sony.fancygifdialoglib.FancyGifDialogListener;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.ByteArrayEntity;
import cz.msebera.android.httpclient.message.BasicHeader;
import de.hdodenhof.circleimageview.CircleImageView;

public class RequestDetailsActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {

    Context context;
    ImageView back_button_home_activity;
    CircleImageView imageView;
    TextView name, time_ago, acci_type, mobile_number, distance, rating, tv_note, tv_name;
    Double lat, lng;
    ImageView iv1, iv2, iv3;
    ArrayList<String> images;
    String id;
    Button btn_navigate, btn_complete, btn_accept, btn_reject, btn_hire;
    CardView cv_acceptreject;
    RelativeLayout rl_loading, rl_parent;
    RatingBar simpleRatingBar;
    EditText et_comment;
    String accepted_user_id, photo, request_id, user_from_id, request_title,price;
    View parentLayout;
    private GoogleMap mMap;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_detail);
        parentLayout = findViewById(android.R.id.content);
        context = this;
        findViewByID();
        setClickListeners();
        setMapView();
        getNotificationDataFromIntent();
        fetchRequestData();
    }

    private void getNotificationDataFromIntent() {
        //if notification received when app is closed


        if (getIntent().hasExtra("push")) {
            if (getIntent().getExtras().getString("status").equalsIgnoreCase("background")) {

                Bundle bundle = getIntent().getBundleExtra("data");
                request_title=bundle.getString("notification_title");
                if (bundle.getString("notification_title").equalsIgnoreCase("New Service Request")) {
                    Log.e("back notification_title",bundle.getString("notification_title"));
                    cv_acceptreject.setVisibility(View.VISIBLE);
                    btn_complete.setVisibility(View.GONE);
                    try {
                        JSONObject data = new JSONObject(bundle.getString("data"));
                        if (data.has("id")) {
                            request_id = data.getString("id");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (bundle.getString("notification_title").equalsIgnoreCase("Request Accepted") || bundle.getString("notification_title").equalsIgnoreCase("Request Completed") || bundle.getString("notification_title").equalsIgnoreCase("New Review")) {
                    try {
                        JSONObject data = new JSONObject(bundle.getString("data"));
                        if (data.has("id")) {
                            request_id = data.getString("id");
                            user_from_id = bundle.getString("user_from_id");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    cv_acceptreject.setVisibility(View.GONE);
                    btn_complete.setVisibility(View.VISIBLE);
                }
            }


            //if notification received when app is open
            else if (getIntent().getExtras().getString("status").equalsIgnoreCase("foreground")) {
                RemoteMessage message = (RemoteMessage) getIntent().getExtras().get("data");
                request_title=message.getData().get("notification_title");
                if (message.getData().get("notification_title").equalsIgnoreCase("New Service Request")) {

                    Log.e("fore notification_title",message.getData().get("notification_title"));
                    cv_acceptreject.setVisibility(View.VISIBLE);
                    btn_complete.setVisibility(View.GONE);
                    try {
                        JSONObject data = new JSONObject(message.getData().get("data"));
                        if (data.has("id")) {
                            request_id = data.getString("id");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (message.getData().get("notification_title").equalsIgnoreCase("Request Accepted") || message.getData().get("notification_title").equalsIgnoreCase("Request Completed") || message.getData().get("notification_title").equalsIgnoreCase("New Review")) {
                    try {
                        JSONObject data = new JSONObject(message.getData().get("data"));
                        if (data.has("id")) {
                            request_id = data.getString("id");
                            user_from_id = message.getData().get("user_from_id");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    cv_acceptreject.setVisibility(View.GONE);
                    btn_complete.setVisibility(View.VISIBLE);
                }
                else {
                    try {
                        JSONObject data = new JSONObject(message.getData().get("data"));
                        if (data.has("id")) {
                            request_id = data.getString("id");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        //If notifications opened through notification screen
        else {
            request_id = getIntent().getExtras().getString("request_id");
            user_from_id = getIntent().getExtras().getString("user_from_id");
            request_title=getIntent().getExtras().getString("title");
            if (getIntent().hasExtra("title")) {
                if (getIntent().getExtras().getString("title").equalsIgnoreCase("Request Accepted")) {
                    cv_acceptreject.setVisibility(View.GONE);
                    btn_complete.setVisibility(View.VISIBLE);
                } else if (getIntent().getExtras().getString("title").equalsIgnoreCase("New Service Request")) {
                    cv_acceptreject.setVisibility(View.VISIBLE);
                    btn_complete.setVisibility(View.GONE);
                } else {

                }
            }
        }
    }

    private void setImages() {
        for (int i = 0; i < images.size(); i++) {
            if (i == 0) {
                if (images.get(i) != null) {
                    Picasso.get().load(images.get(i)).into(iv1);
                }
            }
            if (i == 1) {
                if (images.get(i) != null) {
                    Picasso.get().load(images.get(i)).into(iv2);
                }
            }

            if (i == 2) {
                if (images.get(i) != null) {
                    Picasso.get().load(images.get(i)).into(iv3);
                }
            }
        }
    }

    private void setLocationMap() {
        LatLng latLng = new LatLng(lat, lng);
        View marker = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker_layout, null);
        CircleImageView iv_user = marker.findViewById(R.id.iv_user);
        if (photo != null && photo != "") {
            Picasso.get().load(photo).into(iv_user);
        } else {
            iv_user.setImageDrawable(getResources().getDrawable(R.drawable.img_placehol));
        }
        Marker m1 = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(context, marker)))
                .title("Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12.0f));

    }

    private void setClickListeners() {
        back_button_home_activity.setOnClickListener(this);
        btn_navigate.setOnClickListener(this);
        btn_complete.setOnClickListener(this);
        btn_accept.setOnClickListener(this);
        btn_reject.setOnClickListener(this);
        btn_hire.setOnClickListener(this);
    }
    private void findViewByID() {
        back_button_home_activity = findViewById(R.id.back_button_home_activity);
        imageView = findViewById(R.id.profile_image);
        name = findViewById(R.id.name);
        time_ago = findViewById(R.id.time_ago);
        acci_type = findViewById(R.id.acci_type);
        mobile_number = findViewById(R.id.mobile_number);
        distance = findViewById(R.id.distance);
        rating = findViewById(R.id.rating);
        iv1 = findViewById(R.id.iv1);
        iv2 = findViewById(R.id.iv2);
        iv3 = findViewById(R.id.iv3);
        tv_name = findViewById(R.id.tv_name);
        tv_note = findViewById(R.id.tv_note);
        btn_navigate = findViewById(R.id.btn_navigate);
        btn_complete = findViewById(R.id.btn_complete);
        btn_hire = findViewById(R.id.btn_hire);
        cv_acceptreject = findViewById(R.id.cv_acceptreject);
        btn_accept = findViewById(R.id.btn_accept);
        btn_reject = findViewById(R.id.btn_reject);
        rl_loading = findViewById(R.id.rl_loading);
        rl_parent = findViewById(R.id.rl_parent);
        images = new ArrayList<>();
    }

    private void setMapView() {

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        googleMap.getUiSettings().setScrollGesturesEnabled(false);
        // setLocationMap();
    }

    @Override
    public void onClick(View v) {
        UserSessionPreferences sessionPreferences = new UserSessionPreferences(context);
        switch (v.getId()) {
            case R.id.back_button_home_activity: {
                Intent intent = new Intent(this, MapsActivity.class);
                startActivity(intent);
                finish();
                break;
            }
            case R.id.btn_navigate: {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://maps.google.com/maps?saddr=" + sessionPreferences.getLat() + "," + sessionPreferences.getLon() + "&daddr=" + lat + "," + lng));
                startActivity(intent);
                break;
            }
            case R.id.btn_complete: {
                openInPopup();
                break;
            }
            case R.id.btn_accept: {
                showAcceptDialog();
                break;
            }
            case R.id.btn_reject: {
                showRejecttDialog();
                break;
            }
            case R.id.btn_hire:{
                Bundle bundle = new Bundle();
                bundle.putString("price", price);
                bundle.putString("lat", String.valueOf(lat));
                bundle.putString("lon", String.valueOf(lng));
                bundle.putString("request_id", request_id);
                bundle.putString("service_user_id", user_from_id);
                ProposalDetails fragment2 = new ProposalDetails();
                fragment2.setArguments(bundle);
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.frame_layout, fragment2);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                break;
            }
        }
    }

    private void fetchRequestData() {
        startProgress();
        final GlobalClass globalClass = new GlobalClass();
        globalClass.cancelProgressBarInterection(true, this);
        RestClient restClient = new RestClient();
        Log.e("request Id", request_id);
        final UserSessionPreferences sessionPreferences = new UserSessionPreferences(context);
        final String relativeUrl = "requests/" + request_id;
        ByteArrayEntity entity = null;
        //Log.e("password is=",encrptPass);
        List<Header> headers = new ArrayList<Header>();
        headers.add(new BasicHeader("Authorization", sessionPreferences.getToken()));
        Log.e("Token and url", relativeUrl + " " + headers);
        restClient.getWithheader(this, relativeUrl, headers.toArray(new Header[headers.size()]), null, new BaseJsonHttpResponseHandler<JSONArray>() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, JSONArray response) {
                Log.e("response", rawJsonResponse);
                closeProgress(true);
                globalClass.cancelProgressBarInterection(false, RequestDetailsActivity.this);
                try {
                    JSONObject object = new JSONObject(rawJsonResponse);
                    if (object.getString("status").equalsIgnoreCase("Success")) {
                        JSONObject request = object.getJSONObject("data");
                        JSONArray serviceUserArray = request.getJSONArray("service_users");
                        JSONObject user = request.getJSONObject("user");
                        JSONObject service = request.getJSONObject("service");
                        lat = request.getDouble("latitude");
                        lng = request.getDouble("longitude");
                        name.setText(user.getString("name"));
                        tv_name.setText(user.getString("name"));
                        if (user.getString("photo") != "" && user.getString("photo") != "null") {
                            Picasso.get().load(user.getString("photo")).into(imageView);
                            photo = user.getString("photo");
                        }
                        acci_type.setText(service.getString("name"));
                        tv_note.setText(request.getString("notes"));
                        time_ago.setText((globalClass.getTimeDifference(request.getString("updated_at")) + ""));
                        JSONArray imagesArray = request.getJSONArray("images");
                        if (imagesArray.length() == 0) {
                            //   closeProgress(true);
                            Toast.makeText(RequestDetailsActivity.this, "No images found", Toast.LENGTH_SHORT).show();
                        } else if (imagesArray.length() > 0) {
                            //   closeProgress(true);
                            for (int i = 0; i < imagesArray.length(); i++) {
                                images.add(new JSONObject(String.valueOf(imagesArray.get(i))).getString("image"));
                            }
                            setImages();
                            setLocationMap();
                            if(user_from_id!=null && user_from_id!="") {
                                for (int i = 0; i < serviceUserArray.length(); i++) {
                                    JSONObject serviceProvider = new JSONObject(String.valueOf(serviceUserArray.get(i)));
                                    if (user_from_id.equalsIgnoreCase(serviceProvider.getString("user_id"))) {
                                        JSONObject serviceDetails = serviceProvider.getJSONObject("service_details");
                                        JSONObject userDetails=serviceProvider.getJSONObject("user_details");
                                        price = serviceDetails.getString("price");
                                        checkRequestUser(user.getString("id"),userDetails.getString("name"));
                                    }
                                }
                            }
                        }
                    } else {
                        closeProgress(true);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, JSONArray errorResponse) {
                Toast.makeText(context, "Error while fetching requests...", Toast.LENGTH_SHORT).show();
                globalClass.cancelProgressBarInterection(false, RequestDetailsActivity.this);
                closeProgress(false);
            }
            @Override
            protected JSONArray parseResponse(String rawJsonData, boolean isFailure) throws Throwable {
                return null;
            }
        });
    }
    private void checkRequestUser(String userId, String name) {
        UserSessionPreferences sessionPreferences=new UserSessionPreferences(context);

        if(sessionPreferences.getUserID().equalsIgnoreCase(userId) && request_title.equalsIgnoreCase("Request Accepted")){
            btn_hire.setVisibility(View.VISIBLE);
            btn_complete.setVisibility(View.GONE);
            cv_acceptreject.setVisibility(View.GONE);
            btn_hire.setText("Link to "+name+"'s profile");
        }
    }
    public void startProgress() {
        rl_parent.setVisibility(View.GONE);
        rl_loading.setVisibility(View.VISIBLE);
    }

    public void closeProgress(boolean result) {
        rl_parent.setVisibility(View.VISIBLE);
        rl_loading.setVisibility(View.GONE);
        // progressBarGIFDialog.
    }

    private void openInPopup() {
        getProfile();

    }

    private void updateRequest(String status, String userId) {
        UserSessionPreferences sessionPreferences = new UserSessionPreferences(context);
        RestClient restClient = new RestClient();
        rl_loading.setVisibility(View.VISIBLE);
        String relativeUrl = "requests/" + request_id;
        ByteArrayEntity entity = null;
        Log.e("Params: ", String.valueOf(getRequestParameters(status, userId)));
        try {
            entity = new ByteArrayEntity((getRequestParameters(status, userId).toString()).getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        List<Header> headers = new ArrayList<Header>();
        headers.add(new BasicHeader("Authorization", sessionPreferences.getToken()));
        restClient.patchtWithHeader(context, relativeUrl, headers.toArray(new Header[headers.size()]), entity, "application/json", new BaseJsonHttpResponseHandler<JSONArray>() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, JSONArray response) {
                Log.e("response", rawJsonResponse);
                rl_loading.setVisibility(View.GONE);
                if (statusCode == 200) {
                    try {
                        JSONObject jsonObject = new JSONObject(rawJsonResponse);
                        if (jsonObject.getString("status").equalsIgnoreCase("Success")) {
                            Intent intent = new Intent(context, MapsActivity.class);
                            startActivity(intent);
                            finish();
                        } else {

                            Snackbar.make(parentLayout, jsonObject.getString("message"), Snackbar.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else if (statusCode == 400) {

                    try {
                        JSONObject jsonObject = new JSONObject(rawJsonResponse);
                        Snackbar.make(parentLayout, jsonObject.getString("message"), Snackbar.LENGTH_LONG).show();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {

                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, JSONArray errorResponse) {
                rl_loading.setVisibility(View.GONE);
                if (statusCode == 400) {
                    try {
                        JSONObject jsonObject = new JSONObject(rawJsonData);
                        Snackbar.make(parentLayout, jsonObject.getString("message"), Snackbar.LENGTH_LONG).show();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    parentLayout.setClickable(false);
                }
            }

            @Override
            protected JSONArray parseResponse(String rawJsonData, boolean isFailure) throws Throwable {
                return null;
            }
        });
    }

    private JsonObject getRequestParameters(String status, String userID) {

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("status", status);
        jsonObject.addProperty("user_id", userID);
        return jsonObject;
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() <1) {
            super.onBackPressed();
            Intent intent = new Intent(context, MapsActivity.class);
            startActivity(intent);
            finish();
        }
        getSupportFragmentManager().popBackStackImmediate();
    }

    private void callFeedbackApi() {
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.show();
        progressDialog.setMessage("Saving your feedback");
        final UserSessionPreferences sessionPreferences = new UserSessionPreferences(context);
        RestClient restClient = new RestClient();
        String relativeUrl = "reviews";
        ByteArrayEntity entity = null;
        try {
            entity = new ByteArrayEntity((getReviewParamters("", "").toString()).getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        List<Header> headers = new ArrayList<Header>();
        headers.add(new BasicHeader("Authorization", sessionPreferences.getToken()));
        restClient.postWithHeader(context, relativeUrl, headers.toArray(new Header[headers.size()]), entity, "application/json", new BaseJsonHttpResponseHandler<JSONArray>() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, JSONArray response) {
                Log.e("response", rawJsonResponse);
                progressDialog.dismiss();
                if (statusCode == 200) {
                    try {
                        JSONObject jsonObject = new JSONObject(rawJsonResponse);
                        if (jsonObject.getString("status").equalsIgnoreCase("Success")) {
                            updateRequest("2", sessionPreferences.getUserID());
                        } else {
                            Snackbar.make(parentLayout, jsonObject.getString("message"), Snackbar.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else if (statusCode == 400) {

                    try {
                        JSONObject jsonObject = new JSONObject(rawJsonResponse);
                        Snackbar.make(parentLayout, jsonObject.getString("message"), Snackbar.LENGTH_LONG).show();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {

                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, JSONArray errorResponse) {
                progressDialog.dismiss();
                if (statusCode == 400) {
                    try {
                        JSONObject jsonObject = new JSONObject(rawJsonData);
                        Snackbar.make(parentLayout, jsonObject.getString("message"), Snackbar.LENGTH_LONG).show();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    parentLayout.setClickable(false);
                }
            }

            @Override
            protected JSONArray parseResponse(String rawJsonData, boolean isFailure) throws Throwable {
                return null;
            }
        });
    }

    private JsonObject getReviewParamters(String status, String userID) {

        UserSessionPreferences sessionPreferences = new UserSessionPreferences(context);
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("service_request_id", request_id);
        jsonObject.addProperty("user_from_id", sessionPreferences.getUserID());
        jsonObject.addProperty("user_to_id", user_from_id);
        jsonObject.addProperty("rating", simpleRatingBar.getRating());
        jsonObject.addProperty("comment", et_comment.getText().toString());
        return jsonObject;
    }

    public void getProfile() {
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.show();
        progressDialog.setMessage("Loading user data");
        final GlobalClass globalClass = new GlobalClass();
        globalClass.cancelProgressBarInterection(true, RequestDetailsActivity.this);
        final UserSessionPreferences sessionPreferences = new UserSessionPreferences(context);
        RestClient restClient = new RestClient();
        String relativeUrl = "users/profile/" + user_from_id;
        List<Header> headers = new ArrayList<Header>();
        headers.add(new BasicHeader("Authorization", sessionPreferences.getToken()));
        Log.e("Auth and url  ", sessionPreferences.getToken() + " " + relativeUrl);
        restClient.getWithheader(context, relativeUrl, headers.toArray(new Header[headers.size()]), null, new BaseJsonHttpResponseHandler<JSONArray>() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, JSONArray response) {
                globalClass.cancelProgressBarInterection(false, RequestDetailsActivity.this);
                progressDialog.dismiss();
                Log.e("Response :", rawJsonResponse);
                try {
                    JSONObject object = new JSONObject(rawJsonResponse);
                    if (object.getString("status").equalsIgnoreCase("Success")) {
                        JSONObject userDetails = new JSONObject(object.getString("data"));
                        final Dialog fbDialogue = new Dialog(RequestDetailsActivity.this);
                        fbDialogue.setContentView(R.layout.fragment_feedback);
                        TextView btn_submit = fbDialogue.findViewById(R.id.tv_feedback);
                        TextView name = fbDialogue.findViewById(R.id.name);
                        et_comment = fbDialogue.findViewById(R.id.et_comment);
                        simpleRatingBar = fbDialogue.findViewById(R.id.simpleRatingBar);
                        ImageView iv_close = fbDialogue.findViewById(R.id.iv_close);
                        CircleImageView profile_image = fbDialogue.findViewById(R.id.profile_image);
                        name.setText(userDetails.getString("name"));
                        Picasso.get().load(userDetails.getString("photo")).into(profile_image);
                        iv_close.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                fbDialogue.dismiss();
                            }
                        });
                        btn_submit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (validateFeedback()) {
                                    callFeedbackApi();
                                }
                                fbDialogue.dismiss();
                            }
                        });
                        fbDialogue.setCancelable(false);
                        fbDialogue.show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, JSONArray errorResponse) {
                globalClass.cancelProgressBarInterection(false, RequestDetailsActivity.this);
                progressDialog.dismiss();
            }

            @Override
            protected JSONArray parseResponse(String rawJsonData, boolean isFailure) throws Throwable {
                return null;
            }
        });
    }

    private boolean validateFeedback() {
        boolean result = true;
        if (simpleRatingBar.getRating() == 0) {
            Toast.makeText(context, "Please fill the required fields", Toast.LENGTH_SHORT).show();
            result = false;
        }
        return result;
    }

    public void showAcceptDialog() {
        final UserSessionPreferences sessionPreferences = new UserSessionPreferences(context);
        new FancyGifDialog.Builder(RequestDetailsActivity.this)
                .setTitle("New request received")
                .setMessage("Are you sure you want to accept the request?")
                .setNegativeBtnText("Cancel")
                .setPositiveBtnBackground("#131E69")
                .setPositiveBtnText("Yes")
                .setNegativeBtnBackground("#FFA9A7A8")
                .setGifResource(R.drawable.load)   //Pass your Gif here
                .isCancellable(true)
                .OnPositiveClicked(new FancyGifDialogListener() {
                    @Override
                    public void OnClick() {
                        updateRequest("1", sessionPreferences.getUserID());
                    }
                })
                .OnNegativeClicked(new FancyGifDialogListener() {
                    @Override
                    public void OnClick() {
                    }
                })
                .build();
    }

    public void showRejecttDialog() {
        final UserSessionPreferences sessionPreferences = new UserSessionPreferences(context);
        new FancyGifDialog.Builder(RequestDetailsActivity.this)
                .setTitle("New request received")
                .setMessage("Are you sure you want to reject the request?")
                .setNegativeBtnText("Cancel")
                .setPositiveBtnBackground("#FF0000")
                .setPositiveBtnText("Yes, Reject")
                .setNegativeBtnBackground("#FFA9A7A8")
                .setGifResource(R.drawable.load)   //Pass your Gif here
                .isCancellable(true)
                .OnPositiveClicked(new FancyGifDialogListener() {
                    @Override
                    public void OnClick() {
                        updateRequest("5", sessionPreferences.getUserID());
                    }
                })
                .OnNegativeClicked(new FancyGifDialogListener() {
                    @Override
                    public void OnClick() {
                    }
                })
                .build();
    }

}
