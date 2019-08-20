package com.onewayit.veki.fragment;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonObject;
import com.loopj.android.http.BaseJsonHttpResponseHandler;
import com.onewayit.veki.R;
import com.onewayit.veki.RestClient.RestClient;
import com.onewayit.veki.activities.LoginActivity;
import com.onewayit.veki.utilities.AppConstant;
import com.onewayit.veki.utilities.GlobalClass;
import com.onewayit.veki.utilities.Network;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Objects;
import java.util.Random;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.ByteArrayEntity;

// import com.onewayit.veki.activities.AppConstant;

/**
 * A simple {@link Fragment} subclass.
 */
public class ForgotPasswordFragment extends Fragment implements View.OnClickListener {
    public EditText email, mobile, otp;
    public TextView submit;
    RelativeLayout rl_loading;
    private View view;
    private Context context;
    private Button emailbutton, mobilebutton;
    private GlobalClass globalClass;
    private Integer number;
    private FrameLayout mobileLayout;
    private Spinner spinner_code;
    private String selectedType="email";
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_forgotpassword, container, false);
        initializeVariables();
        findViewById();
        setOnClickListener();
        Random rnd = new Random();
        number = rnd.nextInt(99999) + 99999;
        // setOnCheckedChangeListener();
        return view;
    }

    private void initializeVariables() {
        context = getActivity();
        globalClass = new GlobalClass();

        ((LoginActivity) Objects.requireNonNull(getActivity())).setHeading("Forgot Password");
    }


    private void findViewById() {
        mobileLayout = view.findViewById(R.id.mobileLayout);
        email = view.findViewById(R.id.email);
        mobile = view.findViewById(R.id.mobile);
        emailbutton = view.findViewById(R.id.emailbutton);
        mobilebutton = view.findViewById(R.id.mobilebutton);
        email.setVisibility(View.VISIBLE);
        mobileLayout.setVisibility(View.GONE);
        emailbutton.setTextColor(getResources().getColor(R.color.white));
        submit = view.findViewById(R.id.submit);
        spinner_code = view.findViewById(R.id.spinner_code);
        rl_loading = view.findViewById(R.id.rl_loading);
    }

    private void setOnClickListener() {
        submit.setOnClickListener(this);
        emailbutton.setOnClickListener(this);
        mobilebutton.setOnClickListener(this);

        //   login.setOnClickListener(this);
        //  phone.setOnClickListener(this);
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
            case R.id.emailbutton:
                selectedType="email";
                email.setVisibility(View.VISIBLE);
                mobileLayout.setVisibility(View.GONE);
                emailbutton.setTextColor(getResources().getColor(R.color.white));
                mobilebutton.setTextColor(getResources().getColor(R.color.dark_grey));
                break;
            case R.id.mobilebutton:
                selectedType="mobile";
                email.setVisibility(View.GONE);
                mobileLayout.setVisibility(View.VISIBLE);
                emailbutton.setTextColor(getResources().getColor(R.color.dark_grey));
                mobilebutton.setTextColor(getResources().getColor(R.color.white));
                break;

            case R.id.phone:
                Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new LoginFragment(), "LoginFragment").addToBackStack(null).commit();
                break;
        }
    }

    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isValidMobile() {
        String phoneCode = (spinner_code.getSelectedItem().toString().substring(1, (spinner_code.getSelectedItem().toString()).indexOf("(")));
        if (mobile.getText().toString().isEmpty()) {
            Snackbar.make(view, "Please Enter Mobile Number", Snackbar.LENGTH_LONG).show();
            return false;
        } else if (mobile.getText().toString().length() < 10 && phoneCode.equals("91")) {
            Snackbar.make(view, "Please enter a valid mobile number", Snackbar.LENGTH_LONG).show();
            return false;
        } else if (mobile.getText().toString().length() < 9 && phoneCode.equals("972")) {
            Snackbar.make(view, "Please enter a valid mobile number", Snackbar.LENGTH_LONG).show();
            return false;
        } else if (mobile.getText().toString().length() < 9 && phoneCode.equals("962")) {
            Snackbar.make(view, "Please enter a valid mobile number", Snackbar.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void registration() {
        callforgotPasswordAPI();
    }

    ///////////Parameters for login API//////////////
    private void callforgotPasswordAPI() {
        //   globalClass.cancelProgressBarInterection(true, this);
        rl_loading.setVisibility(View.VISIBLE);
        RestClient restClient = new RestClient();
        String relativeUrl = "users/reset/password";
        ByteArrayEntity entity = null;
        try {
            entity = new ByteArrayEntity((getForgotPasswordParameters().toString()).getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Log.e("Params", getForgotPasswordParameters().toString());
        restClient.postRequestJson(context, relativeUrl, entity, "application/json", new BaseJsonHttpResponseHandler<JSONArray>() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, JSONArray response) {
                Log.e("Params", rawJsonResponse);
                rl_loading.setVisibility(View.GONE);
                globalClass.cancelProgressBarInterection(false, getActivity());
                try {
                    JSONObject object = new JSONObject(rawJsonResponse);
                    if (statusCode == 200) {
                        if (object.getString("status").equalsIgnoreCase("Success")) {
                            VerificationCodeFragment ldf = new VerificationCodeFragment();
                            if (selectedType.equalsIgnoreCase("mobile")) {
                                Bundle args = new Bundle();
                                args.putString(AppConstant.PhoneNumber, mobile.getText().toString().trim());
                                args.putString("email", "");
                                args.putString(AppConstant.PhoneCode, (spinner_code.getSelectedItem().toString()).substring(1, (spinner_code.getSelectedItem().toString()).indexOf("(")));
                                args.putString("page", "forgot");
                                args.putInt("otp", number);
                                //  args.putString("userData",  globalClass.getJsonString(response.body()));
                                ldf.setArguments(args);
                            } else if(selectedType.equalsIgnoreCase("email")){
                                Bundle args = new Bundle();
                                args.putString(AppConstant.PhoneNumber, "");
                                args.putString(AppConstant.PhoneCode, "");
                                args.putString("email", email.getText().toString());
                                args.putString("page", "forgot");
                                args.putInt("otp", number);
                                //  args.putString("userData",  globalClass.getJsonString(response.body()));
                                ldf.setArguments(args);
                            }
                            Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, ldf).commit();
                            Snackbar.make(view, "OTP Sent Successfully", Snackbar.LENGTH_LONG).show();
                        } else if (object.getString("status").equalsIgnoreCase("Error")) {
                            Snackbar.make(view, object.getString("message"), Snackbar.LENGTH_LONG).show();
                        }
                    } else {
                        Snackbar.make(view, object.getString("message"), Snackbar.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
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
                }
            }

            @Override
            protected JSONArray parseResponse(String rawJsonData, boolean isFailure) throws Throwable {
                return null;
            }
        });
    }

    @SuppressLint("HardwareIds")
    private JsonObject getForgotPasswordParameters() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("otp", String.valueOf(number));

        if (selectedType.equalsIgnoreCase("mobile")) {
            jsonObject.addProperty("phone_code", (spinner_code.getSelectedItem().toString()).substring(1, (spinner_code.getSelectedItem().toString()).indexOf("(")));
            jsonObject.addProperty("phone", mobile.getText().toString().trim());
        } else {
            jsonObject.addProperty("phone_code", "");
            jsonObject.addProperty("phone", "");
            jsonObject.addProperty("email", email.getText().toString().trim());
        }
        return jsonObject;

    }
    ///////////////Screen Validation, it will return true if user has fielded all required details/////////////
    private boolean validation() {
        if(selectedType.equalsIgnoreCase("email") && !isValidEmail(email.getText().toString())){
            Snackbar.make(view, "Invalid Email", Snackbar.LENGTH_LONG).show();
            return false;
        }
        else return !selectedType.equalsIgnoreCase("mobile") || isValidMobile();
    }
}

