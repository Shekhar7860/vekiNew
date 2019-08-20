package com.onewayit.veki.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.JsonObject;
import com.onewayit.veki.Preferences.UserSessionPreferences;
import com.onewayit.veki.R;
import com.onewayit.veki.activities.LoginActivity;
import com.onewayit.veki.activities.MapsActivity;
import com.onewayit.veki.api.ApiClient;
import com.onewayit.veki.api.ApiInterface;
import com.onewayit.veki.api.apiResponse.registration.FbRegistrationResponse;
import com.onewayit.veki.utilities.AppConstant;
import com.onewayit.veki.utilities.GlobalClass;
import com.onewayit.veki.utilities.Network;
import com.onewayit.veki.utilities.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment implements View.OnClickListener {

    public String SmsCode;
    RelativeLayout rl_loading;
    Spinner spinner_code;
    ProgressBar my_progressBar;
    CallbackManager callbackManager = CallbackManager.Factory.create();
    private View view;
    private Context context;
    private TextView login, register, request_otp, facebook, tv_loading;
    private EditText mobile_number;
    private GlobalClass globalClass;
    private FirebaseAuth mAuth;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private String refreshedToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_login, container, false);
        refreshedToken = FirebaseInstanceId.getInstance().getToken();
        initializeVariables();
        findViewById();
        setOnClickListener();
        return view;

    }

    @Override
    public void onResume() {
        super.onResume();
        onBackPressed();
    }

    private void initializeVariables() {

        context = getActivity();
        globalClass = new GlobalClass();
        ((LoginActivity) Objects.requireNonNull(getActivity())).setHeading("");
        FacebookSdk.sdkInitialize(getActivity());
    }

    private void findViewById() {
        login = view.findViewById(R.id.login);
        register = view.findViewById(R.id.register);
        request_otp = view.findViewById(R.id.request_otp);
        mobile_number = view.findViewById(R.id.mobile_number);
        facebook = view.findViewById(R.id.facebook);
        spinner_code = view.findViewById(R.id.spinner_code);
        rl_loading = view.findViewById(R.id.rl_loading);
        tv_loading = view.findViewById(R.id.tv_loading);
        my_progressBar = view.findViewById(R.id.my_progressBar);
    }

    private void setOnClickListener() {
        login.setOnClickListener(this);
        facebook.setOnClickListener(this);
        register.setOnClickListener(this);
        request_otp.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.facebook:
                LoginManager.getInstance().logInWithReadPermissions(
                        this,
                        Arrays.asList("user_photos", "email", "user_birthday", "public_profile")
                );

                LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        //   Toast.makeText(getActivity(), "its working",
                        //       Toast.LENGTH_LONG).show();
                        GraphRequest request = GraphRequest.newMeRequest(
                                loginResult.getAccessToken(),
                                new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(JSONObject object, GraphResponse response) {
                                        Log.v("LoginActivity", response.toString());
                                        Log.e("fb", object.toString());

                                        try {
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
            case R.id.login:
                Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new EmailLoginFragment(), "EmailLoginFragment").addToBackStack(null).commit();
                break;
            case R.id.register:
                goToRegisterationFragment();
                break;
            case R.id.request_otp:

                Network network = new Network(context);
                if (network.isConnectedToInternet()) {
                    if (validation()) {
                        rl_loading.setVisibility(View.VISIBLE);
                        globalClass.cancelProgressBarInterection(true, getActivity());
                        sendVerificationCode();
                    }
                } else {
                    network.noInternetAlertBox(getActivity(), false);
                }
                break;

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void goToRegisterationFragment() {
        Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new RegistrationFragment(), "RegistrationFragment").addToBackStack(null).commit();
    }

    private void onBackPressed() {
        Objects.requireNonNull(getView()).setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {

                    Objects.requireNonNull(getActivity()).onBackPressed();
                    return true;
                }
                return false;
            }
        });
    }

    ///////////////Screen Validation, it will return true if user has fielded all required details/////////////
    private boolean validation() {
        String phoneCode = (spinner_code.getSelectedItem().toString().substring(1, (spinner_code.getSelectedItem().toString()).indexOf("(")));
        if (mobile_number.getText().toString().isEmpty()) {
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
        }
        return true;
    }

    ///////////Parameters for login API//////////////
    @SuppressLint("HardwareIds")
    private JSONObject getOtpParameters() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("phone", mobile_number.getText().toString().trim());
            jsonObject.put("phone_code", (spinner_code.getSelectedItem().toString()).substring(1, (spinner_code.getSelectedItem().toString()).indexOf("(")));
            jsonObject.put("otp", SmsCode);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public void nextScreen(String approvalType) {
        rl_loading.setVisibility(View.GONE);
        Bundle bundle = new Bundle();
        bundle.putString("mobile_number", mobile_number.getText().toString());
        bundle.putString("otp", SmsCode);
        bundle.putString("approvalType", approvalType);
        bundle.putString("phone_code", (spinner_code.getSelectedItem().toString()).substring(1, (spinner_code.getSelectedItem().toString()).indexOf("(")));
        bundle.putString("verificationid", "" + mVerificationId);
        VerifyOtpFragment verifyOtpFragment = new VerifyOtpFragment();
        verifyOtpFragment.setArguments(bundle);
        Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, verifyOtpFragment, "VerifyOtpFragment").addToBackStack(null).commit();
    }

    public void sendVerificationCode() {
        mAuth = FirebaseAuth.getInstance();
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                Utility.log("onVerificationCompleted: " + credential);
                SmsCode = credential.getSmsCode();
                globalClass.cancelProgressBarInterection(false, getActivity());
                nextScreen("auto verified");
                // generateOtp();
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                rl_loading.setVisibility(View.GONE);
                globalClass.cancelProgressBarInterection(false, getActivity());
                Utility.log("onVerificationFailed" + e);
                Snackbar.make(view, String.valueOf(e), Snackbar.LENGTH_LONG).show();
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                } else if (e instanceof FirebaseTooManyRequestsException) {
                }//   pbVerify.setVisibility(View.GONE);
            }

            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                Utility.log("onCodeSent: " + verificationId);
                Utility.log("token: " + token);
                globalClass.cancelProgressBarInterection(false, getActivity());
                mVerificationId = verificationId;
                SmsCode = SmsCode;
                mResendToken = token;
                nextScreen("Manual");
                //generateOtp();

            }
        };
        startPhoneNumberVerification(AppConstant.PLUS + (spinner_code.getSelectedItem().toString()).substring(1, (spinner_code.getSelectedItem().toString()).indexOf("(")) + mobile_number.getText().toString() + "");

    }

    private void startPhoneNumberVerification(String phoneNumber) {
        // [START start_phone_auth]
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                2,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                getActivity(),               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
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
                globalClass.cancelProgressBarInterection(false, getActivity());
                if (response.code() == 200) {
                    try {
                        JSONObject jsonObj = new JSONObject(globalClass.getJsonString(response.body()));
                        String data = jsonObj.getString("data");
                        JSONObject data2 = new JSONObject(data);
                        String id = data2.getString("id");
                        sessionPreferences.setUserID(id);
                        sessionPreferences.setUserFirstName(name);
                        sessionPreferences.setEmailId(email);
                        sessionPreferences.setToken("Bearer " + data2.getString("token"));
                    } catch (JSONException e) {
                        Log.e("exception", e.toString());
                    }
                    Log.e("RegistrationFb", "" + globalClass.getJsonString(response.body()));
                    Snackbar.make(view, "Logged in Successfully", Snackbar.LENGTH_LONG).show();
                    rl_loading.setVisibility(View.GONE);
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

    @SuppressLint("HardwareIds")
    private JsonObject getRegistrationFbParameters(String id) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("facebook_id", id);
        jsonObject.addProperty("device_id", refreshedToken);
        jsonObject.addProperty("device_token", refreshedToken);
        jsonObject.addProperty("device_type", "android");
        Log.e("registration parameters", jsonObject.toString());
        return jsonObject;
    }

}

