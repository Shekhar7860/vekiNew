package com.onewayit.veki.fragment;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.loopj.android.http.BaseJsonHttpResponseHandler;
import com.onewayit.veki.GetterSetter.ServiceData;
import com.onewayit.veki.Preferences.UserSessionPreferences;
import com.onewayit.veki.R;
import com.onewayit.veki.RestClient.RestClient;
import com.onewayit.veki.activities.MapsActivity;
import com.onewayit.veki.activities.MarkerDragActivity;
import com.onewayit.veki.activities.PlacesAutoCompleteAdapter;
import com.onewayit.veki.utilities.GlobalClass;
import com.onewayit.veki.utilities.MultiSelectionSpinner;
import com.onewayit.veki.utilities.Network;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.ByteArrayEntity;
import cz.msebera.android.httpclient.message.BasicHeader;

import static androidx.appcompat.app.AppCompatActivity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment implements View.OnClickListener, MultiSelectionSpinner.OnMultipleItemsSelectedListener {
    RelativeLayout rl_loading;
    private MultiSelectionSpinner multiSelectionSpinner;
    private ArrayList<ServiceData> serviceData;
    private ArrayList<ServiceData> deletedServices;
    private LinearLayout services_lay, ll_profile;
    private AutoCompleteTextView ac_location;
    private PlacesAutoCompleteAdapter mAdapter;
    private Handler mThreadHandler;
    private HandlerThread mHandlerThread;
    private String profileImage;
    private Double lat, lon;
    private String editActive = "false";
    private Dialog fbDialogue;
    private Spinner spinner_code;
    private String[] array = {"car crash", "car wash", "car repair", "bike repair", "car handle alignment", "car maintenance"};
    private View view;
    private Context context;
    private EditText et_mobile_number, name, email, et_address, et_about;
    private TextView submit, tv_etProfile, tv_cancel, tv_action;
    private ImageView image, back_button_home_activity, iv_add;
    private Button bt;

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
        view = inflater.inflate(R.layout.fragment_profile, container, false);
        context = getActivity();
        initializeVariables();
        findViewById();
        setOnClickListener();
        getUserData();
        addServices();
        disableEdit();
        getProfile();
        return view;
    }

    private void getUserData() {
        UserSessionPreferences userSessionPreferences = new UserSessionPreferences(context);
        et_mobile_number.setText(userSessionPreferences.getMobile());
    }

    private void initializeVariables() {
        //((MapsActivity) Objects.requireNonNull(getActivity())).setHeading("Profile");
        serviceData = new ArrayList<>();
        deletedServices = new ArrayList<>();
    }

    private void findViewById() {

        name = view.findViewById(R.id.name);
        image = view.findViewById(R.id.image);
        email = view.findViewById(R.id.email);
        tv_cancel = view.findViewById(R.id.tv_cancel);
        tv_etProfile = view.findViewById(R.id.tv_etProfile);
        et_mobile_number = view.findViewById(R.id.et_mobile_number);
        submit = view.findViewById(R.id.submit);
        iv_add = view.findViewById(R.id.iv_add);
        services_lay = view.findViewById(R.id.services_lay);
        back_button_home_activity = view.findViewById(R.id.back_button_home_activity);
        spinner_code = (Spinner) view.findViewById(R.id.spinner_code);
        multiSelectionSpinner = view.findViewById(R.id.mySpinner);
        ll_profile = view.findViewById(R.id.ll_profile);
        rl_loading = view.findViewById(R.id.rl_loading);
        multiSelectionSpinner.setItems(array);
        tv_action = view.findViewById(R.id.tv_action);
        multiSelectionSpinner.setSelection(new int[]{1});
        et_address = view.findViewById(R.id.et_address);
        et_about = view.findViewById(R.id.et_about);
    }

    private void addServices() {
        this.services_lay.removeAllViews();
        for (int i = 0; i < serviceData.size(); i++) {
            LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            final TextView tv = new TextView(context);
            lparams.setMargins(5, 5, 5, 5);
            tv.setTextColor(getResources().getColor(R.color.white));
            tv.setLayoutParams(lparams);
            tv.setSingleLine(true);
            tv.setBackgroundResource(R.drawable.circular_services_back);
            tv.setPadding(40, 40, 40, 40);
            tv.setText(serviceData.get(i).getService_name());
            tv.setTypeface(tv.getTypeface(), Typeface.BOLD);
            this.services_lay.addView(tv);
            final int finalI = i;
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (editActive == "true") {
                        openInPopup("update", String.valueOf(tv.getText()), finalI);
                    }
                }
            });
        }
    }

    private void setOnClickListener() {
        submit.setOnClickListener(this);

        image.setOnClickListener(this);
        iv_add.setOnClickListener(this);
        tv_etProfile.setOnClickListener(this);
        tv_cancel.setOnClickListener(this);
        // bt.setOnClickListener(this);
        multiSelectionSpinner.setListener(this);
        back_button_home_activity.setOnClickListener(this);
    }

    public void enableEdit() {
        submit.setOnClickListener(this);
        submit.setVisibility(View.VISIBLE);
        iv_add.setOnClickListener(this);
        multiSelectionSpinner.setListener(this);
        image.setOnClickListener(this);
        name.setEnabled(true);
        email.setEnabled(true);
        et_address.setEnabled(true);
        et_about.setEnabled(true);
        et_mobile_number.setEnabled(true);
        tv_etProfile.setVisibility(View.GONE);
        tv_cancel.setVisibility(View.VISIBLE);
        spinner_code.setEnabled(true);
    }

    public void disableEdit() {
        submit.setOnClickListener(null);
        submit.setVisibility(View.GONE);
        iv_add.setOnClickListener(null);
        multiSelectionSpinner.setListener(null);
        image.setOnClickListener(null);
        name.setEnabled(false);
        email.setEnabled(false);
        et_address.setEnabled(false);
        et_about.setEnabled(false);
        tv_etProfile.setVisibility(View.VISIBLE);
        tv_cancel.setVisibility(View.GONE);
        et_mobile_number.setEnabled(false);
        spinner_code.setEnabled(false);
    }

    @Override
    public void selectedIndices(List<Integer> indices) {

    }

    @Override
    public void selectedStrings(List<String> strings) {
        //    Toast.makeText(context, strings.toString(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.submit:
                Network network = new Network(context);
                if (network.isConnectedToInternet()) {
                    if (validation()) {
                        submit();
                    }
                } else {
                    network.noInternetAlertBox(getActivity(), false);
                }
                break;
            case R.id.back_button_home_activity: {
                Intent intent = new Intent(getActivity(), MapsActivity.class);
                startActivity(intent);
                Objects.requireNonNull(getActivity()).finish();
                break;
            }
            case R.id.tv_etProfile: {
                editActive = "true";
                enableEdit();
                break;
            }
            case R.id.tv_cancel: {
                editActive = "false";
                disableEdit();
                break;
            }
            case R.id.iv_add: {
                if (editActive == "true") {
                    openInPopup("add", "", -1);
                } else {
                    Snackbar.make(view, "Please select edit profile ", Snackbar.LENGTH_LONG).show();
                }
                break;
            }
            case R.id.iv_clear: {
                ac_location.setText("");
                break;
            }
            case R.id.image:
                final CharSequence[] items = {"Take Photo", "Choose from Library",
                        "Cancel"};
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (items[item].equals("Take Photo")) {
                            cameraIntent();
                        } else if (items[item].equals("Choose from Library")) {
                            galleryIntent();

                        } else if (items[item].equals("Cancel")) {
                            dialog.dismiss();
                        }

                    }
                });
                builder.show();
                break;

//                   String s = spinner.getSelectedItemsAsString();
        }

    }

    private void openInPopup(String action, String service_name, Integer index) {
        fbDialogue = new Dialog(context, android.R.style.Theme_Black_NoTitleBar);
        fbDialogue.getWindow().setBackgroundDrawable(new ColorDrawable(Color.argb(100, 0, 0, 0)));
        fbDialogue.setContentView(R.layout.add_services);

        Button btn_submit = fbDialogue.findViewById(R.id.btn_submit);
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, MarkerDragActivity.class);
                startActivityForResult(intent, 2);
            }
        });
        initializeViews(fbDialogue, action, service_name, index);
        fbDialogue.setCancelable(true);
        fbDialogue.show();
    }

    private void initializeViews(final Dialog dialog, final String action, final String service_name, final Integer index) {
        final Spinner service_spinner = dialog.findViewById(R.id.mySpinner);
        final CheckBox cb_mobile = dialog.findViewById(R.id.cb_mobile);
        final CheckBox cb_loc = dialog.findViewById(R.id.cb_loc);
        ImageView iv_clear = dialog.findViewById(R.id.iv_clear);
        ImageView iv_back = dialog.findViewById(R.id.iv_back);
        ac_location = dialog.findViewById(R.id.ac_location);
        final TextView tv_offeredPrice = dialog.findViewById(R.id.tv_offeredPrice);
        TextView tv_cancel = dialog.findViewById(R.id.tv_cancel);
        TextView tv_action = dialog.findViewById(R.id.tv_action);
        Button btn_submit = dialog.findViewById(R.id.btn_submit);
        Button btn_delete = dialog.findViewById(R.id.btn_delete);
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                android.R.layout.simple_spinner_dropdown_item, array);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        service_spinner.setAdapter(adapter);

        setPlaceAdapter();
        iv_clear.setOnClickListener(this);
        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        cb_loc.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                cb_loc.setChecked(b);
                if (b) {
                    cb_mobile.setChecked(false);
                    cb_mobile.setEnabled(false);
                } else {
                    cb_mobile.setEnabled(true);
                    cb_mobile.setChecked(true);
                    ac_location.setText("");
                    ac_location.setEnabled(false);
                }
            }
        });
        cb_mobile.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                cb_loc.setChecked(b);
                if (b) {
                    ac_location.setText("");
                    ac_location.setEnabled(false);
                    cb_loc.setChecked(false);
                    cb_loc.setEnabled(false);
                } else {
                    cb_loc.setEnabled(true);
                    cb_loc.setChecked(true);
                    ac_location.setEnabled(true);
                }
            }
        });
        if (action == "add") {
            tv_action.setText("Add service");
            btn_submit.setText("Add");
            btn_delete.setVisibility(View.GONE);
            service_spinner.setEnabled(true);
        } else if (action == "update") {
            tv_action.setText("Update service");
            service_spinner.setEnabled(false);
            service_spinner.setSelection(adapter.getPosition(service_name));
            tv_offeredPrice.setText(serviceData.get(index).getService_price());
            if (serviceData.get(index).getLocation_type().equalsIgnoreCase("mobile")) {
                cb_mobile.setChecked(true);
            } else {
                cb_loc.setChecked(true);
            }
            btn_submit.setText("Update");
            btn_delete.setVisibility(View.VISIBLE);
            service_spinner.setEnabled(false);
        }
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validService(adapter.getItem(service_spinner.getSelectedItemPosition()), cb_mobile, cb_loc, tv_offeredPrice.getText().toString())) {
                    if (action == "add" && checkIfExists(adapter.getItem(service_spinner.getSelectedItemPosition()))) {
                        ServiceData service = new ServiceData();
                        service.setService_name(adapter.getItem(service_spinner.getSelectedItemPosition()));
                        if (cb_mobile.isChecked()) {
                            service.setLocation_type("mobile");
                            UserSessionPreferences sessionPreferences = new UserSessionPreferences(context);
                            if (sessionPreferences.getLat() == null || sessionPreferences.getLat() == "") {
                                lat = null;
                                lon = null;
                            } else {
                                lat = Double.valueOf(sessionPreferences.getLat());
                                lon = Double.valueOf(sessionPreferences.getLon());
                            }
                        } else {
                            service.setLocation_type("manual");
                        }
                        service.setService_price(tv_offeredPrice.getText().toString());
                        service.setLocation_lat(String.valueOf(lat));
                        service.setLocation_long(String.valueOf(lon));
                        serviceData.add(service);
                        addServices();
                        dialog.dismiss();
                    } else if (action == "update") {
                        serviceData.get(index).setService_name(adapter.getItem(service_spinner.getSelectedItemPosition()));
                        if (cb_mobile.isChecked()) {
                            serviceData.get(index).setLocation_type("mobile");
                            UserSessionPreferences sessionPreferences = new UserSessionPreferences(context);
                            if (sessionPreferences.getLat() == null || sessionPreferences.getLat() == "") {
                                lat = null;
                                lon = null;
                            } else {
                                lat = Double.valueOf(sessionPreferences.getLat());
                                lon = Double.valueOf(sessionPreferences.getLon());
                            }
                        } else {
                            serviceData.get(index).setLocation_type("manual");
                        }
                        serviceData.get(index).setService_price(tv_offeredPrice.getText().toString());
                        serviceData.get(index).setLocation_lat(String.valueOf(lat));
                        serviceData.get(index).setLocation_long(String.valueOf(lon));
                        addServices();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(context, "Already exists", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (serviceData.get(index).getService_id() != "" || serviceData.get(index).getService_id() != null) {
                    deletedServices.add(serviceData.get(index));
                }
                serviceData.remove(Integer.parseInt(String.valueOf(index)));
                addServices();
                dialog.dismiss();
            }
        });
    }

    private boolean validService(String service, CheckBox isMobileChecked, CheckBox isManualLoc, String offeredPrice) {
        boolean result = true;
        if (service.equalsIgnoreCase("")) {
            result = false;
            Toast.makeText(context, "Please select service", Toast.LENGTH_SHORT).show();
        } else if (!isMobileChecked.isChecked() && !isManualLoc.isChecked()) {
            result = false;

            Toast.makeText(context, "Please select location", Toast.LENGTH_SHORT).show();
        } else if (offeredPrice.equalsIgnoreCase("")) {
            result = false;
            Toast.makeText(context, "Please enter offered price", Toast.LENGTH_SHORT).show();
        } else if (isManualLoc.isChecked() && ac_location.getText().toString().equalsIgnoreCase("")) {
            result = false;
            Toast.makeText(context, "Please enter location", Toast.LENGTH_SHORT).show();
        }
        return result;
    }

    private void cameraIntent() {
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePicture, 0);
    }

    private void galleryIntent() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, 1);
//        startActivityForResult(Intent.createChooser(intent, "Select File"),SELECT_FILE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 0:
                    profileImage = "";
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    image.setImageBitmap(imageBitmap);
                    profileImage = getEncoded64ImageStringFromBitmap(imageBitmap);
                    break;
                case 1:
                    Uri selectedImage2 = data.getData();
                    profileImage = "";
                    Bitmap bitmap;
                    try {
                        bitmap = BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(selectedImage2));
                        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 500, 500, false);
                        profileImage = getEncoded64ImageStringFromBitmap(resizedBitmap);
                        image.setImageURI(selectedImage2);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    break;
                case 2:
                    lat = Double.valueOf(data.getStringExtra("Lat"));
                    lon = Double.valueOf(data.getStringExtra("Long"));
                    ac_location.setText(data.getStringExtra("locAddress"));
                    break;
            }
        }
    }

    private void submit() {
        saveProfile();

    }

    ///////////////Screen Validation, it will return true if user has fielded all required details/////////////
    private boolean validation() {
        String phoneCode = (spinner_code.getSelectedItem().toString().substring(1, (spinner_code.getSelectedItem().toString()).indexOf("(")));
        if (name.getText().toString().isEmpty()) {
            Snackbar.make(view, "Please Enter Name", Snackbar.LENGTH_LONG).show();
            return false;
        } else if (email.getText().toString().isEmpty()) {
            Snackbar.make(view, "Please Enter Email Id", Snackbar.LENGTH_LONG).show();
            return false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()) {
            Snackbar.make(view, "Please Enter a Valid Email Id", Snackbar.LENGTH_LONG).show();
            return false;
        } else if (et_mobile_number.getText().toString().isEmpty()) {
            Snackbar.make(view, "Please Enter Mobile Number", Snackbar.LENGTH_LONG).show();
            return false;
        } else if (et_mobile_number.getText().toString().length() < 10 && phoneCode.equals("91")) {
            Snackbar.make(view, "Please enter a valid mobile number", Snackbar.LENGTH_LONG).show();
            return false;
        } else if (et_mobile_number.getText().toString().length() < 9 && phoneCode.equals("972")) {
            Snackbar.make(view, "Please enter a valid mobile number", Snackbar.LENGTH_LONG).show();
            return false;
        } else if (et_mobile_number.getText().toString().length() < 9 && phoneCode.equals("962")) {
            Snackbar.make(view, "Please enter a valid mobile number", Snackbar.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void setPlaceAdapter() {
        mAdapter = new PlacesAutoCompleteAdapter(context, R.layout.auto_complete_listitem);
        ac_location.setAdapter(mAdapter);
        if (mThreadHandler == null) {
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
        ac_location.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                hideKeyboard(getActivity());
                if (i == 0) {
                    Intent intent = new Intent(context, MarkerDragActivity.class);
                    startActivityForResult(intent, 2);
                } else {
                    getLatLongFromLocation(mAdapter.resultList.get(i));
                }
            }
        });

    }

    private boolean checkIfExists(String service_name) {
        for (int i = 0; i < serviceData.size(); i++) {
            if (serviceData.get(i).getService_name().equals(service_name)) {
                return false;
            }
        }
        return true;
    }

    private void getLatLongFromLocation(String locName) {
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
                lon = address.getLongitude();
            }
        } catch (IOException e) {
            Log.e("", "Unable to connect to Geocoder", e);
        }
    }

    private void saveProfile() {
        startProgress("update");
        final UserSessionPreferences sessionPreferences = new UserSessionPreferences(context);
        RestClient restClient = new RestClient();
        String relativeUrl = "users/profile/" + sessionPreferences.getUserID();
        Log.e("userID", sessionPreferences.getUserID());
        ByteArrayEntity entity = null;
        final GlobalClass globalClass = new GlobalClass();
        //Log.e("password is=",encrptPass);
        try {
            entity = new ByteArrayEntity((getProfileParameters().toString()).getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        List<Header> headers = new ArrayList<Header>();
        headers.add(new BasicHeader("Authorization", sessionPreferences.getToken()));
        Log.e("Token ", sessionPreferences.getToken());
        restClient.putWithHeader(context, relativeUrl, headers.toArray(new Header[headers.size()]), entity, "application/json", new BaseJsonHttpResponseHandler<JSONArray>() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, JSONArray response) {
                Log.e("response :", rawJsonResponse);
                try {
                    JSONObject object = new JSONObject(rawJsonResponse);
                    if (object.getString("status").equalsIgnoreCase("Success")) {
                        JSONObject data = object.getJSONObject("data");
                        sessionPreferences.setProfile_Status(data.getString("photo"));
                        callDelay();
                    } else if (object.getString("status").equalsIgnoreCase("Error")) {
                        closeProgress();
                        Snackbar.make(view, object.getString("message"), Snackbar.LENGTH_LONG).show();
                    } else {
                        closeProgress();
                        Snackbar.make(view, object.getString("Something went wrong"), Snackbar.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, JSONArray errorResponse) {
                if (rawJsonData != null) {
                    try {
                        Snackbar.make(view, new JSONObject(rawJsonData).getString("message"), Snackbar.LENGTH_LONG).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                //  Log.e("fail resposne",rawJsonData);
                closeProgress();
            }

            @Override
            protected JSONArray parseResponse(String rawJsonData, boolean isFailure) throws Throwable {
                return null;
            }
        });
    }

    private JSONObject getProfileParameters() {
        JSONObject jsonObject = new JSONObject();
        UserSessionPreferences sessionPreferences = new UserSessionPreferences(context);
        try {

            jsonObject.put("name", name.getText().toString());
            jsonObject.put("email", email.getText().toString());
            jsonObject.put("address", et_address.getText().toString());
            jsonObject.put("phone_code", (spinner_code.getSelectedItem().toString()).substring(1, (spinner_code.getSelectedItem().toString()).indexOf("(")));
            jsonObject.put("phone", et_mobile_number.getText().toString());
            jsonObject.put("latitude", sessionPreferences.getLat());
            jsonObject.put("longitude", sessionPreferences.getLon());
            if (profileImage != null) {
                jsonObject.put("photo", "data:image/png;base64," + profileImage);
            }
            JSONArray services = new JSONArray();
            JSONArray deletedServ = new JSONArray();
            for (int i = 0; i < serviceData.size(); i++) {
                JSONObject service = new JSONObject();
                service.put("location", serviceData.get(i).getLocation_type());
                if (serviceData.get(i).getLocation_lat().equalsIgnoreCase("") || serviceData.get(i).getLocation_lat().equalsIgnoreCase(null)) {
                    service.put("latitude", null);
                    service.put("longitude", null);
                } else {
                    service.put("latitude", serviceData.get(i).getLocation_lat());
                    service.put("longitude", serviceData.get(i).getLocation_long());
                }
                service.put("price", serviceData.get(i).getService_price());
                service.put("service_id", getServiceId(serviceData.get(i).getService_name()));
                service.put("user_id", sessionPreferences.getUserID());
                services.put(service);
            }
            for (int j = 0; j < deletedServices.size(); j++) {
                JSONObject object = new JSONObject();
                object.put("id", deletedServices.get(j).getService_id());
                deletedServ.put(deletedServices.get(j).getService_id());
            }

            jsonObject.put("services", services);
            jsonObject.put("delete_services", deletedServ);
            sessionPreferences.setUserFirstName(name.getText().toString());
            sessionPreferences.setEmailId(email.getText().toString());

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

    private String getEncoded64ImageStringFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        byte[] byteFormat = stream.toByteArray();
        // get the base 64 string
        String imgString = Base64.encodeToString(byteFormat, Base64.NO_WRAP);

        return imgString;
    }

    private void getProfile() {
        final GlobalClass globalClass = new GlobalClass();
        startProgress("load data");
        globalClass.cancelProgressBarInterection(true, getActivity());
        final UserSessionPreferences sessionPreferences = new UserSessionPreferences(context);
        RestClient restClient = new RestClient();
        String relativeUrl = "users/profile/" + sessionPreferences.getUserID();
        List<Header> headers = new ArrayList<Header>();
        headers.add(new BasicHeader("Authorization", sessionPreferences.getToken()));
        Log.e("Auth and url  ", sessionPreferences.getToken() + " " + relativeUrl);
        restClient.getWithheader(context, relativeUrl, headers.toArray(new Header[headers.size()]), null, new BaseJsonHttpResponseHandler<JSONArray>() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, JSONArray response) {
                globalClass.cancelProgressBarInterection(false, getActivity());
                closeProgress();
                Log.e("Response :", rawJsonResponse);
                try {
                    JSONObject object = new JSONObject(rawJsonResponse);
                    if (object.getString("status").equalsIgnoreCase("Success")) {
                        JSONObject userDetails = new JSONObject(object.getString("data"));
                        if (userDetails.getString("name") != "null") {
                            name.setText(userDetails.getString("name"));
                            sessionPreferences.setUserFirstName(userDetails.getString("name"));
                        } else {
                            if (!sessionPreferences.getUserFirstName().equalsIgnoreCase("null")) {
                                name.setText(sessionPreferences.getUserFirstName());
                            }
                        }
                        if (userDetails.getString("email") != "null") {
                            email.setText(userDetails.getString("email"));
                            sessionPreferences.setEmailId(userDetails.getString("email"));
                        } else {
                            email.setText(sessionPreferences.getEmailId());
                        }

                        if (userDetails.getString("address") != "null") {
                            et_address.setText(userDetails.getString("address"));
                            sessionPreferences.setAddress(userDetails.getString("address"));
                        }
//                        if (userDetails.getString("about") != "null") {
//                            et_about.setText(userDetails.getString("about"));
//                            sessionPreferences.setAbout(userDetails.getString("about"));
//                        }
//                        else{
//                            et_about.setText(sessionPreferences.getAbout());
//                        }
                        if (userDetails.getString("photo") != "" && userDetails.getString("photo") != null) {
                            //   Log.d("photores", userDetails.getString("photo") );
                            sessionPreferences.setProfile_Status(userDetails.getString("photo"));
                        }
                        if (userDetails.getString("phone") != "null") {
                            et_mobile_number.setText(userDetails.getString("phone"));
                            sessionPreferences.setMobile(userDetails.getString("phone"));
                        }
                        if (userDetails.getString("phone_code") != "null") {
                            if (userDetails.getString("phone_code").contains("91")) {
                                spinner_code.setSelection(0);
                            } else if (userDetails.getString("phone_code").contains("97")) {
                                spinner_code.setSelection(1);
                            } else if (userDetails.getString("phone_code").contains("96")) {
                                spinner_code.setSelection(2);
                            }
                            sessionPreferences.setCountryCode(userDetails.getString("phone_code"));
                        }

                        JSONArray serviceArray = userDetails.getJSONArray("user_services");
                        if (sessionPreferences.getProfile_Status() == null || sessionPreferences.getProfile_Status().equalsIgnoreCase("null")) {
                            image.setImageDrawable(getResources().getDrawable(R.drawable.image_placeholder));
                        } else {
                            Picasso.get().load(sessionPreferences.getProfile_Status()).into(image);
                        }
                        for (int i = 0; i < serviceArray.length(); i++) {
                            ServiceData servicesGetterSetter = new ServiceData();
                            JSONObject service_details = new JSONObject(String.valueOf(serviceArray.get(i)));
                            JSONObject service = new JSONObject(service_details.getString("service"));
                            Log.e("sevice check", service.toString());
                            servicesGetterSetter.setService_name(service.getString("name"));
                            servicesGetterSetter.setService_id(service_details.getString("id"));
                            servicesGetterSetter.setLocation_lat(service_details.getString("latitude"));
                            servicesGetterSetter.setLocation_long(service_details.getString("longitude"));
                            servicesGetterSetter.setLocation_type(service_details.getString("location"));
                            servicesGetterSetter.setService_price(service_details.getString("price"));
                            serviceData.add(servicesGetterSetter);
                        }
                        addServices();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, JSONArray errorResponse) {
                closeProgress();
                globalClass.cancelProgressBarInterection(false, getActivity());
            }

            @Override
            protected JSONArray parseResponse(String rawJsonData, boolean isFailure) throws Throwable {
                return null;
            }
        });
    }


    private void startProgress(String action) {
        if (action.equalsIgnoreCase("load data")) {
            rl_loading.setVisibility(View.VISIBLE);
            ll_profile.setVisibility(View.GONE);
            tv_action.setText("Loading...");
        } else {
            rl_loading.setVisibility(View.VISIBLE);
            ll_profile.setVisibility(View.GONE);
            tv_action.setText("Updating...");
        }
    }

    private void closeProgress() {
        rl_loading.setVisibility(View.GONE);
        ll_profile.setVisibility(View.VISIBLE);
    }

    private void callDelay() {
        closeProgress();
        Intent intent = new Intent(getContext(), MapsActivity.class);
        startActivity(intent);
        Objects.requireNonNull(getActivity()).finish();
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Objects.requireNonNull(getView()).setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        Intent intent = new Intent(getContext(), MapsActivity.class);
                        startActivity(intent);
                        Objects.requireNonNull(getActivity()).finish();
                        return true;
                    }
                }
                return false;
            }
        });
    }
}
