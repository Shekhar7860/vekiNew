package com.onewayit.veki.fragment;


import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.snackbar.Snackbar;
import com.loopj.android.http.BaseJsonHttpResponseHandler;
import com.onewayit.veki.Preferences.UserSessionPreferences;
import com.onewayit.veki.R;
import com.onewayit.veki.RestClient.RestClient;
import com.onewayit.veki.activities.MapsActivity;
import com.onewayit.veki.activities.MarkerDragActivity;
import com.onewayit.veki.activities.PlacesAutoCompleteAdapter;
import com.onewayit.veki.utilities.GPSTracker;
import com.onewayit.veki.utilities.GlobalClass;
import com.onewayit.veki.utilities.Network;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.ByteArrayEntity;
import cz.msebera.android.httpclient.message.BasicHeader;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class NewRequestFragment extends Fragment implements View.OnClickListener, LocationListener, AdapterView.OnItemClickListener {
    View view;
    Context context;
    AutoCompleteTextView ac_location;
    HandlerThread mHandlerThread;
    Handler mThreadHandler;
    double lat = 0, lng = 0;
    EditText et_note;
    ArrayList<String> images = new ArrayList<>();
    ProgressDialog progressDialog;
    Button btn_help;
    private Spinner spinnerDropDown;
    private RelativeLayout footer;
    private ImageView camera_image, camera_image2, camera_image3, cross, cross2, cross3, iv_gps, back_button_home_activity;
    private TextView tv_setOnMap;
    private String[] servicesList = {"car crash", "car wash", "car repair", "bike repair", "car handle alignment", "car maintenance"};
    private PlacesAutoCompleteAdapter mAdapter;

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_new_request, container, false);
        context = getActivity();
        initViews();
        setOnClickListener();
        // findViewById();
        //setOnClickListener();
        setPlaceAdapter();
        return view;
    }

    private void initViews() {
        cross = view.findViewById(R.id.cross);
        iv_gps = view.findViewById(R.id.iv_gps);
        cross2 = view.findViewById(R.id.cross2);
        cross3 = view.findViewById(R.id.cross3);
        back_button_home_activity = view.findViewById(R.id.back_button_home_activity);
        tv_setOnMap = view.findViewById(R.id.tv_setOnMap);
        camera_image = view.findViewById(R.id.camera_image);
        camera_image2 = view.findViewById(R.id.camera_image2);
        camera_image3 = view.findViewById(R.id.camera_image3);
        btn_help = view.findViewById(R.id.btn_help);
        et_note = view.findViewById(R.id.et_note);
        spinnerDropDown = view.findViewById(R.id.spinner);
        ac_location = (AutoCompleteTextView) view.findViewById(R.id.ac_location);
        footer = view.findViewById(R.id.footer);
        progressDialog = new ProgressDialog(context);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.
                R.layout.simple_spinner_dropdown_item, servicesList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDropDown.setAdapter(adapter);
    }

    private void setOnClickListener() {
        footer.setOnClickListener(this);
        btn_help.setOnClickListener(this);
        camera_image.setOnClickListener(this);
        camera_image2.setOnClickListener(this);
        camera_image3.setOnClickListener(this);
        cross.setOnClickListener(this);
        ac_location.setOnItemClickListener(this);
        cross2.setOnClickListener(this);
        cross3.setOnClickListener(this);
        back_button_home_activity.setOnClickListener(this);
        iv_gps.setOnClickListener(this);
        tv_setOnMap.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.footer:
                if (validate()) {
                    Network network = new Network(context);
                    if (!network.isConnectedToInternet()) {
                        network.noInternetAlertBox(getActivity(), false);
                        return;
                    }
                    saveRequest();
                }
                break;
            case R.id.btn_help:
                if (validate()) {
                    Network network = new Network(context);
                    if (!network.isConnectedToInternet()) {
                        network.noInternetAlertBox(getActivity(), false);
                        return;
                    }
                    saveRequest();
                }
                break;
            case R.id.camera_image:
                cameraIntent(0);
                break;
            case R.id.camera_image2:
                cameraIntent(1);
                break;
            case R.id.camera_image3:
                cameraIntent(2);
                break;
            case R.id.cross:
                setcamera(3);
                break;
            case R.id.cross2:
                setcamera(2);
                break;
            case R.id.cross3:
                setcamera(1);
                break;

            case R.id.iv_gps: {
                getMyLocation();
                break;
            }
            case R.id.back_button_home_activity:
                Intent homeIntent = new Intent(getActivity(), MapsActivity.class);
                startActivity(homeIntent);
                Objects.requireNonNull(getActivity()).finish();
                break;
            case R.id.tv_setOnMap: {
                Intent intent = new Intent(context, MarkerDragActivity.class);
                startActivityForResult(intent, 5);
                break;
            }
        }
    }

    private boolean validate() {
        boolean result = true;
        if (lat == 0 || lng == 0) {
            result = false;
            Snackbar.make(view, "Please select the location", Snackbar.LENGTH_LONG).show();
        } else if (et_note.getText().toString().equalsIgnoreCase("")) {
            result = false;
            Snackbar.make(view, "Please enter note", Snackbar.LENGTH_LONG).show();
        } else if (images.size() == 0) {
            result = false;
            Snackbar.make(view, "Please add images", Snackbar.LENGTH_LONG).show();
        }
        return result;
    }

    private void getMyLocation() {
        checkPermission();
    }

    public void checkPermission() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        } else {
            GPSTracker gpsTracker = new GPSTracker(context);
            progressDialog.setMessage("Fetching your location");
            progressDialog.show();
            if (gpsTracker.canGetLocation()) {
                lat = gpsTracker.getLatitude();
                lng = gpsTracker.getLongitude();
                getAddress(lat, lng);
            } else {
                progressDialog.dismiss();
                gpsTracker.showSettingsAlert();
            }
        }
    }

    private void setPlaceAdapter() {
        mAdapter = new PlacesAutoCompleteAdapter(context, R.layout.auto_complete_listitem);
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

    public void getAddress(double lat, double lng) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            Address obj = addresses.get(0);
            String add = "";
            if (obj.getLocality() != null && obj.getLocality() != "null") {
                add = add + obj.getLocality();
            }
            if (obj.getAdminArea() != null && obj.getAdminArea() != "null") {
                add = add + "," + obj.getAdminArea();
            }
            if (obj.getCountryName() != null && obj.getCountryName() != "null") {
                add = add + "," + obj.getCountryName();
            }
            ac_location.setText(add);
            progressDialog.dismiss();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            progressDialog.dismiss();
            Snackbar.make(view, "Error while fetching your location", Snackbar.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        //You had this as int. It is advised to have Lat/Loing as double.
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkPermission();
                } else {
                    Toast.makeText(context, "Please turn on GPS", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    private void setcamera(Integer a) {
        final float scale;
        switch (a) {
            case 3:
                String uri = "@drawable/camera";
                int imageResource = getResources().getIdentifier(uri, null, context.getPackageName());
                // imageview= (ImageView)findViewById(R.id.imageView);
                Drawable res = getResources().getDrawable(imageResource);
                camera_image3.setImageDrawable(res);
                camera_image3.setScaleType(ImageView.ScaleType.CENTER);
                images.remove(0);
                cross.setVisibility(View.GONE);
                break;
            case 2:
                String uri2 = "@drawable/camera";
                int imageResource2 = getResources().getIdentifier(uri2, null, context.getPackageName());
                // imageview= (ImageView)findViewById(R.id.imageView);
                Drawable res2 = getResources().getDrawable(imageResource2);
                camera_image.setImageDrawable(res2);
                camera_image.setScaleType(ImageView.ScaleType.CENTER);
                if (images.size() == 3) {
                    images.remove(1);
                } else if (images.size() == 2) {
                    images.remove(1);
                } else {
                    images.remove(0);
                }
                cross2.setVisibility(View.GONE);
                break;
            case 1:
                String uri3 = "@drawable/camera";
                int imageResource3 = getResources().getIdentifier(uri3, null, context.getPackageName());
                // imageview= (ImageView)findViewById(R.id.imageView);
                Drawable res3 = getResources().getDrawable(imageResource3);
                camera_image2.setImageDrawable(res3);
                camera_image2.setScaleType(ImageView.ScaleType.CENTER);
                if (images.size() == 3) {
                    images.remove(2);
                } else if (images.size() == 2) {
                    images.remove(1);
                } else {
                    images.remove(0);
                }
                cross3.setVisibility(View.GONE);
                break;
        }
    }

    private void cameraIntent(Integer a) {
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePicture, a);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent dataReturned) {
        super.onActivityResult(requestCode, resultCode, dataReturned);
        if (dataReturned != null) {
            if (resultCode == RESULT_OK) {
                switch (requestCode) {
                    case 0:
                        if (dataReturned.getExtras() != null) {
                            Bundle extras = dataReturned.getExtras();
                            Bitmap imageBitmap = (Bitmap) extras.get("data");
                            camera_image.setImageBitmap(imageBitmap);
                            camera_image.setScaleType(ImageView.ScaleType.FIT_XY);
                            cross2.setVisibility(View.VISIBLE);
                            if (images.size() == 3) {
                                images.add(2, "data:image/png;base64," + getEncoded64ImageStringFromBitmap(imageBitmap));
                            } else if (images.size() == 2) {
                                images.add(1, "data:image/png;base64," + getEncoded64ImageStringFromBitmap(imageBitmap));
                            } else if (images.size() == 1) {
                                images.add(0, "data:image/png;base64," + getEncoded64ImageStringFromBitmap(imageBitmap));
                            } else {
                                images.add("data:image/png;base64," + getEncoded64ImageStringFromBitmap(imageBitmap));

                            }
                        }
                        break;
                    case 1:
                        if (dataReturned.getExtras() != null) {
                            Bundle extras2 = dataReturned.getExtras();
                            Bitmap imageBitmap2 = (Bitmap) extras2.get("data");
                            camera_image2.setImageBitmap(imageBitmap2);
                            camera_image2.setScaleType(ImageView.ScaleType.FIT_XY);
                            cross3.setVisibility(View.VISIBLE);
                            if (images.size() == 3) {
                                images.add(2, "data:image/png;base64," + getEncoded64ImageStringFromBitmap(imageBitmap2));
                            } else if (images.size() == 2) {
                                images.add(1, "data:image/png;base64," + getEncoded64ImageStringFromBitmap(imageBitmap2));
                            } else if (images.size() == 1) {
                                images.add(0, "data:image/png;base64," + getEncoded64ImageStringFromBitmap(imageBitmap2));
                            } else {
                                images.add("data:image/png;base64," + getEncoded64ImageStringFromBitmap(imageBitmap2));

                            }
                        }
                        break;
                    case 2:
                        if (dataReturned.getExtras() != null) {
                            Bundle extras3 = dataReturned.getExtras();
                            Bitmap imageBitmap3 = (Bitmap) extras3.get("data");
                            camera_image3.setImageBitmap(imageBitmap3);
                            camera_image3.setScaleType(ImageView.ScaleType.FIT_XY);
                            cross.setVisibility(View.VISIBLE);
                            if (images.size() == 3) {
                                images.add(2, "data:image/png;base64," + getEncoded64ImageStringFromBitmap(imageBitmap3));
                            } else if (images.size() == 2) {
                                images.add(1, "data:image/png;base64," + getEncoded64ImageStringFromBitmap(imageBitmap3));
                            } else if (images.size() == 1) {
                                images.add(0, "data:image/png;base64," + getEncoded64ImageStringFromBitmap(imageBitmap3));
                            } else {
                                images.add("data:image/png;base64," + getEncoded64ImageStringFromBitmap(imageBitmap3));

                            }
                        }
                        break;
                    case 5:
                        lat = Double.valueOf(dataReturned.getStringExtra("Lat"));
                        lng = Double.valueOf(dataReturned.getStringExtra("Long"));
                        ac_location.setText(dataReturned.getStringExtra("locAddress"));
                }
            }
        }
    }

    public String getEncoded64ImageStringFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        byte[] byteFormat = stream.toByteArray();
        // get the base 64 string
        String imgString = Base64.encodeToString(byteFormat, Base64.NO_WRAP);

        return imgString;
    }

    private JSONObject getRequestParams() {
        JSONObject jsonObject = new JSONObject();
        UserSessionPreferences userSessionPreferences = new UserSessionPreferences(context);
        try {
            jsonObject.put("user_id", userSessionPreferences.getUserID());
            jsonObject.put("service_id", getServiceId(spinnerDropDown.getSelectedItem().toString()));
            jsonObject.put("latitude", lat);
            jsonObject.put("longitude", lng);
            jsonObject.put("notes", et_note.getText().toString());
            JSONObject body = new JSONObject();
            JSONArray requestImages = new JSONArray();
            for (int i = 0; i < images.size(); i++) {
                requestImages.put(images.get(i));
            }
            jsonObject.put("images", requestImages);
            //jsonObject.put("body",body);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    private String getServiceId(String serviceName) {
        String id = null;
        if (serviceName.equalsIgnoreCase("car crash")) {
            id = "1";
        }
        if (serviceName.equalsIgnoreCase("car wash")) {
            id = "2";
        }

        if (serviceName.equalsIgnoreCase("car repair")) {
            id = "3";
        }

        if (serviceName.equalsIgnoreCase("bike repair") || serviceName.equalsIgnoreCase("byke repair")) {
            id = "4";
        }

        if (serviceName.equalsIgnoreCase("car handle alignment")) {
            id = "5";
        }

        if (serviceName.equalsIgnoreCase("car maintenance")) {
            id = "6";
        }

        return id;
    }

    public void saveRequest() {
        progressDialog.setMessage("Sending your request..");
        progressDialog.show();
        final UserSessionPreferences sessionPreferences = new UserSessionPreferences(context);
        RestClient restClient = new RestClient();
        String relativeUrl = "requests";
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

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try {
                    if (statusCode == 200 && object.getString("status").equalsIgnoreCase("Success")) {
                        JSONObject request = object.getJSONObject("data");
                        JSONArray service_providers = request.getJSONArray("service_users");
                        progressDialog.dismiss();
                        Bundle bundle = new Bundle();
                        bundle.putString("lat", String.valueOf(lat));
                        bundle.putString("lon", String.valueOf(lng));
                        if (service_providers.length() > 0) {
                            bundle.putString("request_sent", "success");
                        } else {
                            bundle.putString("request_sent", "fail");
                        }
                        bundle.putString("requestId", request.getString("id"));
                        ProposalReceivedFragment fragment2 = new ProposalReceivedFragment();
                        fragment2.setArguments(bundle);
                        FragmentManager fragmentManager = getFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.frame_layout, fragment2);
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.commit();
                    } else if (statusCode == 200 && object.getString("status").equalsIgnoreCase("Error")) {
                        progressDialog.dismiss();
                        openPopup();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, JSONArray errorResponse) {
                JSONObject object = null;
                Log.e("response", rawJsonData);
                progressDialog.dismiss();
                try {
                    object = new JSONObject(rawJsonData);
                    if (object.getString("status").equalsIgnoreCase("Error")) {
                        Snackbar.make(view, object.getString("message"), Snackbar.LENGTH_LONG).show();
                    } else {
                        Snackbar.make(view, object.getString("message"), Snackbar.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Snackbar.make(view, "Something went wrong", Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            protected JSONArray parseResponse(String rawJsonData, boolean isFailure) throws Throwable {
                return null;
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        hideKeyboard(getActivity());
        if (i == 0) {
            Intent intent = new Intent(context, MarkerDragActivity.class);
            startActivityForResult(intent, 5);
        } else {
            hideKeyboard(getActivity());
            getLatLongFromLocation(mAdapter.resultList.get(i));
        }
    }

    public void getLatLongFromLocation(String locName) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
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
                lng = address.getLongitude();
            }
        } catch (IOException e) {
            Log.e("", "Unable to connect to Geocoder", e);
        }
    }

    private void openPopup() {
        Snackbar.make(view, "No service provider found.", Snackbar.LENGTH_LONG).show();
        Bundle bundle = new Bundle();
        bundle.putString("lat", String.valueOf(lat));
        bundle.putString("lon", String.valueOf(lng));
        bundle.putString("request_sent", "fail");
        bundle.putString("requestId", "112");
        ProposalReceivedFragment fragment2 = new ProposalReceivedFragment();
        fragment2.setArguments(bundle);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment2);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
//        final Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                Intent intent = new Intent(getContext(), MapsActivity.class);
//                startActivity(intent);
//                Objects.requireNonNull(getActivity()).finish();
//            }
//        }, 2000);

    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
