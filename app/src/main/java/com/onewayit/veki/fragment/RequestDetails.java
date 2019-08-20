package com.onewayit.veki.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.view.KeyEvent;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonObject;
import com.loopj.android.http.BaseJsonHttpResponseHandler;
import com.onewayit.veki.Preferences.UserSessionPreferences;
import com.onewayit.veki.R;
import com.onewayit.veki.RestClient.RestClient;
import com.onewayit.veki.activities.MapsActivity;
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
import java.util.Objects;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.ByteArrayEntity;
import cz.msebera.android.httpclient.message.BasicHeader;
import de.hdodenhof.circleimageview.CircleImageView;

public class RequestDetails extends Fragment implements OnMapReadyCallback, View.OnClickListener {

    Context context;
    View view;
    ImageView back_button_home_activity;
    CircleImageView imageView;
    TextView name, time_ago, acci_type, mobile_number, distance, rating, tv_note, tv_name;
    Double lat, lng;
    ImageView iv1, iv2, iv3;
    ArrayList<String> images;
    String id;
    Button btn_navigate, btn_complete, btn_accept, btn_reject;
    CardView cv_acceptreject;
    RelativeLayout rl_loading;
    RatingBar simpleRatingBar;
    EditText et_comment;
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_request_detail, container, false);
        findViewByID();
        setClickListeners();
        setMapView();
        getRequestDetails();
        fetchImages();
        return view;
    }

    private void getRequestDetails() {
        UserSessionPreferences sessionPreferences = new UserSessionPreferences(context);
        imageView.setImageDrawable(getResources().getDrawable(R.drawable.img_placehol));
        name.setText(getArguments().getString("name"));
        id = getArguments().getString("id");
        tv_name.setText(getArguments().getString("name"));
        time_ago.setText(getArguments().getString("timeAgo"));
        acci_type.setText(getArguments().getString("servicetype"));
        mobile_number.setText(sessionPreferences.getCountryCode() + " " + getArguments().getString("phone"));
        if (getArguments().getString("distance") == "") {
            distance.setText(String.format("%.2f", Double.parseDouble(getArguments().getString("distance"))));
        }
        if (getArguments().getString("photo") != null) {
            Picasso.get().load(getArguments().getString("photo")).into(imageView);
        }
        rating.setText("0.0 (0 reviews)");
        lat = Double.parseDouble(getArguments().getString("lat"));
        lng = Double.parseDouble(getArguments().getString("lon"));
        tv_note.setText(getArguments().getString("notes"));
        if (getArguments().getString("source").equalsIgnoreCase("marker")) {
            cv_acceptreject.setVisibility(View.VISIBLE);
            btn_complete.setVisibility(View.GONE);
        } else if (getArguments().getString("source").equalsIgnoreCase("complete request")) {
            btn_complete.setVisibility(View.GONE);
            cv_acceptreject.setVisibility(View.GONE);
        } else {
            btn_complete.setVisibility(View.VISIBLE);
            cv_acceptreject.setVisibility(View.GONE);
        }
        setImages();
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
        if (getArguments().getString("photo") != null) {
            Picasso.get().load(getArguments().getString("photo")).into(iv_user);
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
    }

    private void findViewByID() {
        back_button_home_activity = view.findViewById(R.id.back_button_home_activity);
        imageView = view.findViewById(R.id.profile_image);
        name = view.findViewById(R.id.name);
        time_ago = view.findViewById(R.id.time_ago);
        acci_type = view.findViewById(R.id.acci_type);
        mobile_number = view.findViewById(R.id.mobile_number);
        distance = view.findViewById(R.id.distance);
        rating = view.findViewById(R.id.rating);
        iv1 = view.findViewById(R.id.iv1);
        iv2 = view.findViewById(R.id.iv2);
        iv3 = view.findViewById(R.id.iv3);
        tv_name = view.findViewById(R.id.tv_name);
        tv_note = view.findViewById(R.id.tv_note);
        btn_navigate = view.findViewById(R.id.btn_navigate);
        btn_complete = view.findViewById(R.id.btn_complete);
        cv_acceptreject = view.findViewById(R.id.cv_acceptreject);
        btn_accept = view.findViewById(R.id.btn_accept);
        btn_reject = view.findViewById(R.id.btn_reject);
        rl_loading = view.findViewById(R.id.rl_loading);
        images = new ArrayList<>();
    }

    private void setMapView() {

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        googleMap.getUiSettings().setScrollGesturesEnabled(false);
        setLocationMap();
    }

    @Override
    public void onClick(View v) {
        UserSessionPreferences sessionPreferences = new UserSessionPreferences(context);
        switch (v.getId()) {
            case R.id.back_button_home_activity: {
                ((MapsActivity) getActivity()).openMyRequests();
                break;
            }
            case R.id.btn_navigate: {
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
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

        }
    }

    private void fetchImages() {
        startProgress();
        final GlobalClass globalClass = new GlobalClass();
        globalClass.cancelProgressBarInterection(true, getActivity());
        RestClient restClient = new RestClient();
        final UserSessionPreferences sessionPreferences = new UserSessionPreferences(context);
        final String relativeUrl = "requests/" + id;
        ByteArrayEntity entity = null;
        //Log.e("password is=",encrptPass);
        List<Header> headers = new ArrayList<Header>();
        headers.add(new BasicHeader("Authorization", sessionPreferences.getToken()));
        Log.e("Token and url", relativeUrl + " " + headers);
        restClient.getWithheader(getActivity(), relativeUrl, headers.toArray(new Header[headers.size()]), null, new BaseJsonHttpResponseHandler<JSONArray>() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, JSONArray response) {
                Log.e("response", rawJsonResponse);
                closeProgress(true);
                globalClass.cancelProgressBarInterection(false, getActivity());
                try {
                    JSONObject object = new JSONObject(rawJsonResponse);
                    if (object.getString("status").equalsIgnoreCase("Success")) {
                        JSONObject request = object.getJSONObject("data");
                        JSONArray imagesArray = request.getJSONArray("images");
                        if (imagesArray.length() == 0) {
                            //   closeProgress(true);
                            Toast.makeText(getActivity(), "No images found", Toast.LENGTH_SHORT).show();
                        } else if (imagesArray.length() > 0) {
                            //   closeProgress(true);
                            for (int i = 0; i < imagesArray.length(); i++) {
                                images.add(new JSONObject(String.valueOf(imagesArray.get(i))).getString("image"));
                            }
                            setImages();
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
                globalClass.cancelProgressBarInterection(false, getActivity());
                closeProgress(false);
            }

            @Override
            protected JSONArray parseResponse(String rawJsonData, boolean isFailure) throws Throwable {
                return null;
            }
        });
    }

    public void startProgress() {
    }

    public void closeProgress(boolean result) {

        // progressBarGIFDialog.
    }

    private void openInPopup() {
        getProfile();

    }

    private void updateRequest(String status, String userId) {
        UserSessionPreferences sessionPreferences = new UserSessionPreferences(context);
        RestClient restClient = new RestClient();
        rl_loading.setVisibility(View.VISIBLE);
        String relativeUrl = "requests/" + id;
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
                            Intent intent = new Intent(getContext(), MapsActivity.class);
                            startActivity(intent);
                            Objects.requireNonNull(getActivity()).finish();
                        } else {

                            Snackbar.make(view, jsonObject.getString("message"), Snackbar.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else if (statusCode == 400) {

                    try {
                        JSONObject jsonObject = new JSONObject(rawJsonResponse);
                        Snackbar.make(view, jsonObject.getString("message"), Snackbar.LENGTH_LONG).show();

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
                        Snackbar.make(view, jsonObject.getString("message"), Snackbar.LENGTH_LONG).show();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    view.setClickable(false);
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        //go to previous fragemnt
                        //perform your fragment transaction here
                        //pass data as arguments
                        ((MapsActivity) getActivity()).openMyRequests();
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private void callFeedbackApi() {
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.show();
        progressDialog.setMessage("Saving your feedback");
        final UserSessionPreferences sessionPreferences = new UserSessionPreferences(context);
        RestClient restClient = new RestClient();
        String relativeUrl = "reviews";
        ByteArrayEntity entity = null;
        Log.e("Params: ", String.valueOf(getReviewParamters()));
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

                            Snackbar.make(view, jsonObject.getString("message"), Snackbar.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else if (statusCode == 400) {

                    try {
                        JSONObject jsonObject = new JSONObject(rawJsonResponse);
                        Snackbar.make(view, jsonObject.getString("message"), Snackbar.LENGTH_LONG).show();

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
                        Snackbar.make(view, jsonObject.getString("message"), Snackbar.LENGTH_LONG).show();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    view.setClickable(false);
                }
            }

            @Override
            protected JSONArray parseResponse(String rawJsonData, boolean isFailure) throws Throwable {
                return null;
            }
        });
    }

    @SuppressLint("HardwareIds")
    private JsonObject getReviewParamters() {
        UserSessionPreferences sessionPreferences = new UserSessionPreferences(context);
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("service_request_id", id);
        jsonObject.addProperty("user_from_id", sessionPreferences.getUserID());
        jsonObject.addProperty("user_to_id", getArguments().getString("accepted_user_id"));
        jsonObject.addProperty("rating", simpleRatingBar.getRating());
        jsonObject.addProperty("comment", et_comment.getText().toString());
        return jsonObject;
    }

    private JsonObject getReviewParamters(String status, String userID) {

        UserSessionPreferences sessionPreferences = new UserSessionPreferences(context);
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("service_request_id", id);
        jsonObject.addProperty("user_from_id", sessionPreferences.getUserID());
        jsonObject.addProperty("user_to_id", getArguments().getString("accepted_user_id"));
        jsonObject.addProperty("rating", simpleRatingBar.getRating());
        jsonObject.addProperty("comment", et_comment.getText().toString());
        return jsonObject;
    }

    public void getProfile() {
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.show();
        progressDialog.setMessage("Loading user data");
        final GlobalClass globalClass = new GlobalClass();
        globalClass.cancelProgressBarInterection(true, getActivity());
        final UserSessionPreferences sessionPreferences = new UserSessionPreferences(context);
        RestClient restClient = new RestClient();
        String relativeUrl = "users/profile/" + getArguments().getString("accepted_user_id");
        List<Header> headers = new ArrayList<Header>();
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
                        final Dialog fbDialogue = new Dialog(getActivity());
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
                globalClass.cancelProgressBarInterection(false, getActivity());
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
        new FancyGifDialog.Builder(getActivity())
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
        new FancyGifDialog.Builder(getActivity())
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
