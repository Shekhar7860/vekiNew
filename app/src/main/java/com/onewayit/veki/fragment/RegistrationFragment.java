package com.onewayit.veki.fragment;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.JsonObject;
import com.loopj.android.http.BaseJsonHttpResponseHandler;
import com.onewayit.veki.Preferences.UserSessionPreferences;
import com.onewayit.veki.R;
import com.onewayit.veki.RestClient.RestClient;
import com.onewayit.veki.activities.LoginActivity;
import com.onewayit.veki.activities.MapsActivity;
import com.onewayit.veki.api.ApiClient;
import com.onewayit.veki.api.ApiInterface;
import com.onewayit.veki.api.apiResponse.registration.FbRegistrationResponse;
import com.onewayit.veki.utilities.AppConstant;
import com.onewayit.veki.utilities.GlobalClass;
import com.onewayit.veki.utilities.Network;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.ByteArrayEntity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// import com.onewayit.veki.activities.AppConstant;

/**
 * A simple {@link Fragment} subclass.
 */
public class RegistrationFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {


    Spinner spinner_code;
    CallbackManager callbackManager = CallbackManager.Factory.create();
    RelativeLayout rl_loading;
    private View view;
    private Context context;
    private GlobalClass globalClass;
    private EditText name, email, mobile_number, password, confirm_password;
    private TextView submit, login, phone, facebook, tv_loading;
    private CheckBox service_provider, customer;
    private Integer number;
    private String refreshedToken;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_registeration, container, false);
        initializeVariables();
        findViewById();
        setOnClickListener();
        // setOnCheckedChangeListener();
        refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Random rnd = new Random();
        number = rnd.nextInt(99999) + 99999;
        return view;
    }

    private void initializeVariables() {
        context = getActivity();
        globalClass = new GlobalClass();
        ((LoginActivity) Objects.requireNonNull(getActivity())).setHeading("Register");
    }

    private void setOnCheckedChangeListener() {
        service_provider.setOnCheckedChangeListener(this);
        customer.setOnCheckedChangeListener(this);
    }

    private void findViewById() {
        name = view.findViewById(R.id.name);
        email = view.findViewById(R.id.email);
        mobile_number = view.findViewById(R.id.mobile_number);
        password = view.findViewById(R.id.password);
        confirm_password = view.findViewById(R.id.confirm_password);
        submit = view.findViewById(R.id.submit);
        login = view.findViewById(R.id.login);
        facebook = view.findViewById(R.id.facebook);
        phone = view.findViewById(R.id.phone);
        service_provider = view.findViewById(R.id.service_provider);
        customer = view.findViewById(R.id.customer);
        spinner_code = view.findViewById(R.id.spinner_code);
        rl_loading = view.findViewById(R.id.rl_loading);
        tv_loading = view.findViewById(R.id.tv_loading);
    }

    private void setOnClickListener() {
        submit.setOnClickListener(this);
        login.setOnClickListener(this);
        phone.setOnClickListener(this);
        facebook.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.submit:
                Network network = new Network(context);
                if (network.isConnectedToInternet()) {
                    if (validation()) {
                        registration();
                    }
                } else {
                    network.noInternetAlertBox(getActivity(), false);
                }
                break;
            case R.id.login:
                Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new EmailLoginFragment(), "EmailLoginFragment").addToBackStack(null).commit();
                break;
            case R.id.phone:
                Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new LoginFragment(), "LoginFragment").addToBackStack(null).commit();
                break;

            case R.id.facebook:
                LoginManager.getInstance().logInWithReadPermissions(getActivity(), Arrays.asList("public_profile", "email"));
                LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.d("result", loginResult.toString());
                        GraphRequest request = GraphRequest.newMeRequest(
                                loginResult.getAccessToken(),
                                new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(JSONObject object, GraphResponse response) {
                                        Log.v("LoginActivity", response.toString());

                                        try {
                                            Log.v("Loginresponse", object.toString());
                                            registerLoginFb(object.getString("id"), object.getString("name"), object.getString("email"));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "id, name, email,gender, birthday, location"); // Parámetros que pedimos a facebook
                        request.setParameters(parameters);
                        request.executeAsync();
                    }


                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onError(FacebookException e) {
                        Toast.makeText(getActivity(), e.toString(),
                                Toast.LENGTH_LONG).show();
                    }
                });
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    ///////////Registration API//////////////
    private void registration() {

        rl_loading.setVisibility(View.VISIBLE);
        tv_loading.setText("Registering..");
        globalClass.cancelProgressBarInterection(true, getActivity());
        RestClient restClient = new RestClient();
        String relativeUrl = "users/register";
        ByteArrayEntity entity = null;
        final GlobalClass globalClass = new GlobalClass();
        //Log.e("password is=",encrptPass);
        try {
            entity = new ByteArrayEntity((getRegistrationParameters().toString()).getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        restClient.postRequestJson(context, relativeUrl, entity, "application/json", new BaseJsonHttpResponseHandler<JSONArray>() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, JSONArray response) {
                Log.e("Params", rawJsonResponse);

                globalClass.cancelProgressBarInterection(false, getActivity());
                try {

                    JSONObject object = new JSONObject(rawJsonResponse);
                    Log.e("Registration", "" + rawJsonResponse);
                    if (object.getString("status").equalsIgnoreCase("Success")) {
                        rl_loading.setVisibility(View.GONE);
                        VerificationCodeFragment ldf = new VerificationCodeFragment();
                        Bundle args = new Bundle();
                        args.putString(AppConstant.PhoneNumber, mobile_number.getText().toString().trim());
                        args.putString(AppConstant.PhoneCode, (spinner_code.getSelectedItem().toString()).substring(1, (spinner_code.getSelectedItem().toString()).indexOf("(")));
                        args.putString("page", "register");
                        args.putString("userData", rawJsonResponse);
                        ldf.setArguments(args);
                        Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, ldf).addToBackStack(null).commit();
                    } else if (object.getString("status").equalsIgnoreCase("Error")) {
                        rl_loading.setVisibility(View.GONE);
                        Snackbar.make(view, object.getString("message"), Snackbar.LENGTH_LONG).show();
                    } else {
                        rl_loading.setVisibility(View.GONE);
                    }
                } catch (JSONException e) {
                    Log.e("exception", e.toString());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, JSONArray errorResponse) {
                rl_loading.setVisibility(View.GONE);
                globalClass.cancelProgressBarInterection(false, getActivity());
                JSONObject object = null;
                try {
                    object = new JSONObject(rawJsonData);
                    if (object.getString("status").equalsIgnoreCase("Error")) {
                        rl_loading.setVisibility(View.GONE);
                        Snackbar.make(view, object.getString("message"), Snackbar.LENGTH_LONG).show();
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

    ///////////Parameters for login API//////////////
    @SuppressLint("HardwareIds")
    private JsonObject getRegistrationParameters() {

        Log.d("testagaincode", "value: " + number);
        SharedPreferences pref = getActivity().getSharedPreferences("MY_PREFERENCES", Activity.MODE_PRIVATE);
        pref.edit().putInt("otp", number).apply();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", name.getText().toString().trim());
        jsonObject.addProperty("email", email.getText().toString().trim());
        jsonObject.addProperty("phone", mobile_number.getText().toString().trim());
        jsonObject.addProperty("phone_code", (spinner_code.getSelectedItem().toString()).substring(1, (spinner_code.getSelectedItem().toString()).indexOf("(")));
        jsonObject.addProperty("otp", number);
        jsonObject.addProperty("password", password.getText().toString().trim());
        Log.e("registration parameters", jsonObject.toString());
        return jsonObject;
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
        } else if (mobile_number.getText().toString().isEmpty()) {
            Snackbar.make(view, "Please Enter Mobile Number", Snackbar.LENGTH_LONG).show();
            return false;
        } else if (mobile_number.getText().toString().length() < 10 && phoneCode.equals("91")) {
            Snackbar.make(view, "Please enter a valid mobile number", Snackbar.LENGTH_LONG).show();
            return false;
        } else if (mobile_number.getText().toString().length() < 9 && phoneCode.equals("972")) {
            Snackbar.make(view, "Please enter a valid mobile number", Snackbar.LENGTH_LONG).show();
            return false;
        } else if (mobile_number.getText().toString().length() < 9 && phoneCode.equals("962")) {
            Snackbar.make(view, "Please enter a valid mobile number", Snackbar.LENGTH_LONG).show();
            return false;
        } else if (password.getText().toString().isEmpty()) {
            Snackbar.make(view, "Please enter password", Snackbar.LENGTH_LONG).show();
            return false;
        }
        if (password.getText().length() < 8) {
            Snackbar.make(view, "Password must be at least 8 characters long", Snackbar.LENGTH_LONG).show();
            return false;
        } else if (confirm_password.getText().toString().isEmpty()) {
            Snackbar.make(view, "Please enter confirm password", Snackbar.LENGTH_LONG).show();
            return false;
        } else if (!password.getText().toString().equalsIgnoreCase(confirm_password.getText().toString())) {
            Snackbar.make(view, "Passwords doesn't match", Snackbar.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()) {
            case R.id.service_provider:
                if (b) {
                    customer.setChecked(false);
                }
                break;
            case R.id.customer:
                if (b) {
                    service_provider.setChecked(false);
                }
                break;

        }
    }

    private void registerLoginFb(String id, final String name, final String email) {
        final UserSessionPreferences sessionPreferences = new UserSessionPreferences(context);
        rl_loading.setVisibility(View.VISIBLE);
        tv_loading.setText("Logging in with facebook..");
        globalClass.cancelProgressBarInterection(true, getActivity());
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<FbRegistrationResponse> call = apiService.fbRegisteration(getRegistrationFbParameters(id));
        Log.e("Registration parameters", "" + getRegistrationFbParameters(id));
        Log.e(" Registration url", "" + call.request().url().toString());
        call.enqueue(new Callback<FbRegistrationResponse>() {
            @Override
            public void onResponse(Call<FbRegistrationResponse> call, Response<FbRegistrationResponse> response) {
                //progress_bar.setVisibility(View.GONE);
                globalClass.cancelProgressBarInterection(false, getActivity());

                if (response.code() == 200) {
                    rl_loading.setVisibility(View.GONE);
                    Log.e("RegistrationFb", "" + globalClass.getJsonString(response.body()));

                    Snackbar.make(view, "Registered Successfully", Snackbar.LENGTH_LONG).show();
                    JSONObject jsonObj = null;
                    try {
                        jsonObj = new JSONObject(globalClass.getJsonString(response.body()));
                        String data = jsonObj.getString("data");
                        JSONObject data2 = new JSONObject(data);
                        String id = data2.getString("id");
                        sessionPreferences.setUserID(id);
                        sessionPreferences.setUserFirstName(name);
                        sessionPreferences.setEmailId(email);
                        sessionPreferences.setToken("Bearer " + data2.getString("token"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Intent intent = new Intent(getContext(), MapsActivity.class);
                    startActivity(intent);
                    Objects.requireNonNull(getActivity()).finish();
                } else {
                    rl_loading.setVisibility(View.GONE);
                    globalClass.retrofitNetworkErrorHandler(response.code(), view, context);
                }
            }

            @Override
            public void onFailure(Call<FbRegistrationResponse> call, Throwable t) {
                //progress_bar.setVisibility(View.GONE);
                globalClass.cancelProgressBarInterection(false, getActivity());
                rl_loading.setVisibility(View.GONE);
                Log.e("retrofit error", "" + t.getMessage());
                if (t instanceof IOException) {
                    try {
                        Snackbar.make(view, "Network Failure! Please Check Internet Connection", Snackbar.LENGTH_LONG).show();
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    }

                }
            }
        });

    }

    @SuppressLint("HardwareIds")
    private JsonObject getRegistrationFbParameters(String id) {
        Log.d("testagaincode", "value: " + number);
        SharedPreferences pref = getActivity().getSharedPreferences("MY_PREFERENCES", Activity.MODE_PRIVATE);
        pref.edit().putInt("otp", number).apply();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("facebook_id", id);
        jsonObject.addProperty("email", email.getText().toString().trim());
        jsonObject.addProperty("phone", mobile_number.getText().toString().trim());
        jsonObject.addProperty("phone_code", AppConstant.PLUS + (spinner_code.getSelectedItem().toString()).substring(1, (spinner_code.getSelectedItem().toString()).indexOf("(")));
        jsonObject.addProperty("device_id", Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID));
        jsonObject.addProperty("device_token", refreshedToken);
        jsonObject.addProperty("device_type", "android");
        jsonObject.addProperty("password", password.getText().toString().trim());
        Log.e("registration parameters", jsonObject.toString());
        return jsonObject;
    }
}
