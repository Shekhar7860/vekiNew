package com.onewayit.veki.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.gson.JsonObject;
import com.loopj.android.http.BaseJsonHttpResponseHandler;
import com.onewayit.veki.Preferences.UserSessionPreferences;
import com.onewayit.veki.R;
import com.onewayit.veki.RestClient.RestClient;
import com.onewayit.veki.activities.LoginActivity;
import com.onewayit.veki.activities.MapsActivity;
import com.onewayit.veki.utilities.GlobalClass;
import com.onewayit.veki.utilities.Network;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Objects;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.ByteArrayEntity;


public class EmailLoginFragment extends Fragment implements View.OnClickListener {
    RelativeLayout rl_loading;
    private View view;
    private Context context;
    private TextView sign_up, submit, forgot;
    private EditText email, password;
    private GlobalClass globalClass;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_email_login, container, false);
        initializeVariables();
        findViewById();
        setOnClickListener();
        return view;
    }

    private void initializeVariables() {
        context = getActivity();
        globalClass = new GlobalClass();
        ((LoginActivity) Objects.requireNonNull(getActivity())).setHeading("Log In");
    }

    private void findViewById() {
        sign_up = view.findViewById(R.id.sign_up);
        email = view.findViewById(R.id.email);
        password = view.findViewById(R.id.password);
        forgot = view.findViewById(R.id.forgot);
        submit = view.findViewById(R.id.submit);
        rl_loading = view.findViewById(R.id.rl_loading);
    }

    private void setOnClickListener() {
        sign_up.setOnClickListener(this);
        forgot.setOnClickListener(this);
        submit.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sign_up:
                goToRegisterationFragment();
                break;
            case R.id.forgot:
                gotoForgotPassword();
                break;
            case R.id.submit:
                Network network = new Network(context);
                if (network.isConnectedToInternet()) {
                    if (validation()) {
                        login();
                    }
                } else {
                    network.noInternetAlertBox(getActivity(), false);
                }
                break;
        }
    }

    private void goToRegisterationFragment() {
        Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new RegistrationFragment(), "RegistrationFragment").addToBackStack(null).commit();
    }

    private void gotoForgotPassword() {
        Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new ForgotPasswordFragment(), "ForgotPasswordFragment").addToBackStack(null).commit();
    }

    ///////////login API//////////////
    private void login() {
        rl_loading.setVisibility(View.VISIBLE);
        globalClass.cancelProgressBarInterection(true, getActivity());

        RestClient restClient = new RestClient();
        String relativeUrl = "users/login";
        ByteArrayEntity entity = null;
        Log.e("Params: ", String.valueOf(getLoginParameters()));
        try {
            entity = new ByteArrayEntity((getLoginParameters().toString()).getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        restClient.postRequestJson(context, relativeUrl, entity, "application/json", new BaseJsonHttpResponseHandler<JSONArray>() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, JSONArray response) {
                globalClass.cancelProgressBarInterection(false, getActivity());
                Log.e("response", rawJsonResponse);
                if (statusCode == 200) {
                    try {
                        JSONObject jsonObject = new JSONObject(rawJsonResponse);
                        if (jsonObject.getString("status").equalsIgnoreCase("Success")) {
                            rl_loading.setVisibility(View.GONE);
                            JSONObject data = new JSONObject(jsonObject.getString("data"));
                            UserSessionPreferences sessionPreferences = new UserSessionPreferences(context);
                            sessionPreferences.setEmailId(data.getString("email"));
                            sessionPreferences.setUserFirstName(data.getString("name"));
                            sessionPreferences.setMobile(data.getString("phone"));
                            sessionPreferences.setUserID(data.getString("id"));
                            sessionPreferences.setCountryCode(data.getString("phone_code"));
                            sessionPreferences.setMobile(data.getString("phone"));
                            sessionPreferences.setToken("Bearer " + data.getString("token"));
                            callDelay();
                        } else {
                            rl_loading.setVisibility(View.GONE);
                            Snackbar.make(view, jsonObject.getString("message"), Snackbar.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else if (statusCode == 400) {
                    rl_loading.setVisibility(View.GONE);
                    try {
                        JSONObject jsonObject = new JSONObject(rawJsonResponse);
                        Snackbar.make(view, jsonObject.getString("message"), Snackbar.LENGTH_LONG).show();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    rl_loading.setVisibility(View.GONE);
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, JSONArray errorResponse) {
                if (statusCode == 400) {
                    try {
                        JSONObject jsonObject = new JSONObject(rawJsonData);
                        Snackbar.make(view, jsonObject.getString("message"), Snackbar.LENGTH_LONG).show();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    rl_loading.setVisibility(View.GONE);
                    globalClass.cancelProgressBarInterection(false, getActivity());
                    view.setClickable(false);
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
    private JsonObject getLoginParameters() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("email", email.getText().toString().trim());
        jsonObject.addProperty("password", password.getText().toString().trim());
        jsonObject.addProperty("device_id", Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID));
        jsonObject.addProperty("device_token", FirebaseInstanceId.getInstance().getToken());
        jsonObject.addProperty("device_type", "android");
        return jsonObject;
    }

    public String getToken() {
        String token = null;
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("", "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();

                        // Log and toast
                    }
                });
        return token;
    }

    ///////////////Screen Validation, it will return true if user has fielded all required details/////////////
    private boolean validation() {
        if (email.getText().toString().isEmpty()) {
            Snackbar.make(view, "Please Enter Email Id", Snackbar.LENGTH_LONG).show();
            return false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()) {
            Snackbar.make(view, "Please Enter a Valid Email Id", Snackbar.LENGTH_LONG).show();
            return false;
        } else if (password.getText().toString().isEmpty()) {
            Snackbar.make(view, "Please Enter Password", Snackbar.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void callDelay() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getContext(), MapsActivity.class);
                startActivity(intent);
                Objects.requireNonNull(getActivity()).finish();
            }
        }, 3000);
    }

}
