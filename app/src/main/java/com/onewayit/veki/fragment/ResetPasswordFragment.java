package com.onewayit.veki.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.JsonObject;
import com.onewayit.veki.R;
import com.onewayit.veki.activities.LoginActivity;
import com.onewayit.veki.api.ApiClient;
import com.onewayit.veki.api.ApiInterface;
import com.onewayit.veki.api.apiResponse.forgot.ForgotUpdatePasswordResponse;
import com.onewayit.veki.utilities.GlobalClass;
import com.onewayit.veki.utilities.Network;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ResetPasswordFragment extends Fragment implements View.OnClickListener {
    private View view;
    private Context context;
    private TextView sign_up, submit;
    private EditText password, confirm_password;
    private GlobalClass globalClass;
    private String refreshedToken;
    private RelativeLayout rl_loading;

    private String countryCode = "91";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_resetpassword, container, false);
        initializeVariables();
        findViewById();
        refreshedToken = FirebaseInstanceId.getInstance().getToken();
        setOnClickListener();
        return view;
    }

    private void initializeVariables() {
        context = getActivity();
        globalClass = new GlobalClass();
        ((LoginActivity) Objects.requireNonNull(getActivity())).setHeading("Reset Password");
    }

    private void findViewById() {
        sign_up = view.findViewById(R.id.sign_up);
        password = view.findViewById(R.id.password);
        submit = view.findViewById(R.id.submit);
        confirm_password = view.findViewById(R.id.confirm_password);
        rl_loading = view.findViewById(R.id.rl_loading);
    }

    private void setOnClickListener() {
        sign_up.setOnClickListener(this);
        submit.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sign_up:
                goToRegisterationFragment();
                break;
            case R.id.submit:
                Network network = new Network(context);
                if (network.isConnectedToInternet()) {
                    if (validation()) {
                        resetPassword();
                    }
                } else {
                    network.noInternetAlertBox(getActivity(), false);
                }
                break;
            case R.id.emailbutton:

                break;
            case R.id.mobilebutton:
                break;
        }
    }

    private boolean validation() {
        boolean result = true;
        if (password.getText().length() < 8) {
            Snackbar.make(view, "Password must be at least 8 characters long", Snackbar.LENGTH_LONG).show();
            result = false;
        } else if (!password.getText().toString().equalsIgnoreCase(confirm_password.getText().toString())) {
            Snackbar.make(view, "Passwords doesn't match", Snackbar.LENGTH_LONG).show();
            result = false;
        }
        return result;
    }


    private void goToRegisterationFragment() {
        Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new LoginFragment(), "LoginFragment").addToBackStack(null).commit();
    }


    ///////////login API//////////////
    private void resetPassword() {
        rl_loading.setVisibility(View.VISIBLE);
        globalClass.cancelProgressBarInterection(true, getActivity());
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<ForgotUpdatePasswordResponse> call = apiService.forgotPasswordUpdate(getResetPasswordParameters());
        Log.e(" Login url", "" + call.request().url().toString());
        call.enqueue(new Callback<ForgotUpdatePasswordResponse>() {
            @Override
            public void onResponse(Call<ForgotUpdatePasswordResponse> call, Response<ForgotUpdatePasswordResponse> response) {
                rl_loading.setVisibility(View.GONE);
                globalClass.cancelProgressBarInterection(false, getActivity());
                if (response.code() == 200) {
                    Log.e("ForgotPasswordResponse", "" + globalClass.getJsonString(response.body()));
                    FragmentManager fm = getActivity().getSupportFragmentManager();
                    for (int i = 0; i < fm.getBackStackEntryCount(); ++i) {
                        fm.popBackStack();
                    }
                    Snackbar.make(view, "Password Updated Successfully", Snackbar.LENGTH_LONG).show();
                    Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new LoginFragment(), "LoginFragment").addToBackStack(null).commit();
                } else if (response.code() == 409 || response.code() == 404) {
                    try {
                        JSONObject jsonObject = new JSONObject(globalClass.getErrorResponseBody(response.errorBody()));
                        if (jsonObject.has("error_message") && !jsonObject.getString("error_message").isEmpty()) {
                            try {
                                Snackbar.make(view, jsonObject.getString("error_message"), Snackbar.LENGTH_LONG).show();
                            } catch (IllegalArgumentException e) {
                                e.printStackTrace();
                            }

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    globalClass.retrofitNetworkErrorHandler(response.code(), view, context);
                }
            }

            @Override
            public void onFailure(Call<ForgotUpdatePasswordResponse> call, Throwable t) {
                rl_loading.setVisibility(View.GONE);
                globalClass.cancelProgressBarInterection(false, getActivity());
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

    ///////////Parameters for login API//////////////
    @SuppressLint("HardwareIds")
    private JsonObject getResetPasswordParameters() {

        JsonObject jsonObject = new JsonObject();
        if (getArguments().getString("type") == "phone") {
            jsonObject.addProperty("phone", getArguments().getString("phone"));
            jsonObject.addProperty("password", password.getText().toString().trim());
            jsonObject.addProperty("device_id", refreshedToken);
            jsonObject.addProperty("device_token", refreshedToken);
            jsonObject.addProperty("device_type", "android");
            jsonObject.addProperty("phone_code", countryCode);
            Log.e("login parameters", jsonObject.toString());
        } else {
            jsonObject.addProperty("email", getArguments().getString("email"));
            jsonObject.addProperty("password", password.getText().toString().trim());
            jsonObject.addProperty("device_id", refreshedToken);
            jsonObject.addProperty("device_token", refreshedToken);
            jsonObject.addProperty("device_type", "android");
            Log.e("reset parameters", jsonObject.toString());
            //  jsonObject.addProperty("phone_code", AppConstant.PLUS + countryCode);
        }
        return jsonObject;
    }
}

