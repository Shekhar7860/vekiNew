package com.onewayit.veki.fragment;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.JsonObject;
import com.loopj.android.http.BaseJsonHttpResponseHandler;
import com.onewayit.veki.Preferences.UserSessionPreferences;
import com.onewayit.veki.R;
import com.onewayit.veki.RestClient.RestClient;
import com.onewayit.veki.activities.LoginActivity;
import com.onewayit.veki.activities.MapsActivity;
import com.onewayit.veki.utilities.AppConstant;
import com.onewayit.veki.utilities.GlobalClass;
import com.onewayit.veki.utilities.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.ByteArrayEntity;

/**
 * A simple {@link Fragment} subclass.
 */
public class VerifyOtpFragment extends Fragment implements View.OnClickListener {

    RelativeLayout rl_loading;
    private View view;
    private Context context;
    private EditText et_otp;
    private TextView mobile_number, submit, tv_resend, tvCountDownTimer;
    private GlobalClass globalClass;
    private String mobileNumber = "";
    private FirebaseAuth mAuth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String mVerificationId;
    private String OtpCode, phone_code;
    private CountDownTimer countDownTimer;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    public static void hideKeyboard(Activity activity) {
        View view = activity.findViewById(android.R.id.content);
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_verify_otp, container, false);
        context = getActivity();
        initializeVariables();
        findViewById();
        setClickListeners();
        getOtpData();
        textWatcher();
        return view;
    }

    private void setClickListeners() {
        submit.setOnClickListener(this);
        tv_resend.setOnClickListener(this);
    }

    private void getOtpData() {
        mobileNumber = getArguments().getString("mobile_number");
        mVerificationId = getArguments().getString("verificationid");
        phone_code = getArguments().getString("phone_code");
        mobile_number.setText(("One Time Password(OTP) has been sent to your mobile +" + phone_code + mobileNumber)+", please enter the same here to login.");
        if (getArguments().getString("approvalType").equalsIgnoreCase("auto verified")) {
            rl_loading.setVisibility(View.VISIBLE);
            Toast.makeText(context, "Your device has been verified automatically ", Toast.LENGTH_LONG).show();
            verifyOtp();
        }
    }
    private void initializeVariables() {
        globalClass = new GlobalClass();
        mobileNumber = Objects.requireNonNull(getArguments()).getString("mobile_number");
        ((LoginActivity) Objects.requireNonNull(getActivity())).setHeading("Enter verification code");
    }

    private void findViewById() {
        et_otp = view.findViewById(R.id.et_otp);
        submit = view.findViewById(R.id.submit);
        mobile_number = view.findViewById(R.id.mobile_number);
        tv_resend = view.findViewById(R.id.tv_resend);
        rl_loading = view.findViewById(R.id.rl_loading);
        tvCountDownTimer=view.findViewById(R.id.tvCountDownTimer);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.submit: {
                if (et_otp.getText().length() == 6) {
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, et_otp.getText().toString());
                    signInWithPhoneAuthCredential(credential);
                } else {
                    Snackbar.make(view, "Please enter valid OTP", Snackbar.LENGTH_LONG).show();
                }
                break;
            }
            case R.id.tv_resend:
                sendVerificationCode();
                break;
        }
    }

    private void textWatcher() {
        et_otp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int length) {
                if (et_otp.getText().length() == 6) {
                    hideKeyboard(getActivity());
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, et_otp.getText().toString());
                    rl_loading.setVisibility(View.VISIBLE);
                    signInWithPhoneAuthCredential(credential);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Utility.log("signInWithCredential:success");
                            final FirebaseUser user = task.getResult().getUser();
                            verifyOtp();

                        } else {
                            Utility.log("signInWithCredential:failure " + task.getException());
                            rl_loading.setVisibility(View.GONE);
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            }
                        }
                    }
                });
    }

    ///////////OTP API//////////////
    private void verifyOtp() {
        globalClass.cancelProgressBarInterection(true, getActivity());
        RestClient restClient = new RestClient();
        String relativeUrl = "users/otp/verify";
        ByteArrayEntity entity = null;
        final GlobalClass globalClass = new GlobalClass();
        try {
            entity = new ByteArrayEntity((getVerifyOtpParameters().toString()).getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Log.e("Params", getVerifyOtpParameters().toString());
        restClient.postRequestJson(context, relativeUrl, entity, "application/json", new BaseJsonHttpResponseHandler<JSONArray>() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, JSONArray response) {
                Log.e("Params", rawJsonResponse);
                rl_loading.setVisibility(View.GONE);
                globalClass.cancelProgressBarInterection(false, getActivity());
                try {
                    JSONObject object = new JSONObject(rawJsonResponse);
                    if (object.getString("status").equalsIgnoreCase("Success")) {
                        UserSessionPreferences userSessionPreferences = new UserSessionPreferences(context);
                        JSONObject jsonObject = new JSONObject(object.getString("data"));
                        userSessionPreferences.setCountryCode(jsonObject.getString("phone_code"));
                        userSessionPreferences.setUserID(jsonObject.getString("id"));
                        if (jsonObject.has("name")) {
                            userSessionPreferences.setUserFirstName(jsonObject.getString("name"));
                        }
                        userSessionPreferences.setMobile(jsonObject.getString("phone"));
                        userSessionPreferences.setToken("Bearer " + jsonObject.getString("token"));
                        callDelay();
                    }
                } catch (JSONException e) {
                    Log.e("Exception", e.toString());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, JSONArray errorResponse) {
                Toast.makeText(context, rawJsonData, Toast.LENGTH_SHORT).show();
                rl_loading.setVisibility(View.GONE);
                globalClass.cancelProgressBarInterection(false, getActivity());
            }

            @Override
            protected JSONArray parseResponse(String rawJsonData, boolean isFailure) throws Throwable {
                return null;
            }
        });
    }

    ///////////Parameters for login API//////////////
    @SuppressLint("HardwareIds")
    private JsonObject getVerifyOtpParameters() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("phone", "" + mobileNumber);
        jsonObject.addProperty("phone_code", phone_code);
        jsonObject.addProperty("otp", "456434");
        jsonObject.addProperty("device_id", Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID));
        jsonObject.addProperty("device_token", FirebaseInstanceId.getInstance().getToken());
        jsonObject.addProperty("device_type", "android");
        Log.e("verify Otp parameters", jsonObject.toString());
        return jsonObject;
    }

    public void sendVerificationCode() {
        // startProgress2("Sending OTP...","OTP sent");
        mAuth = FirebaseAuth.getInstance();
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                Utility.log("onVerificationCompleted: " + credential);
                OtpCode = credential.getSmsCode();
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Utility.log("onVerificationFailed" + e);
                Snackbar.make(view, String.valueOf(e), Snackbar.LENGTH_LONG).show();
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                } else if (e instanceof FirebaseTooManyRequestsException) {
                }
                //   pbVerify.setVisibility(View.GONE);
            }

            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                Utility.log("onCodeSent: " + verificationId);
                Utility.log("token: " + token);
                //  closeProgress2(true);
                mVerificationId = verificationId;
                mResendToken = token;
            }
        };
        startPhoneNumberVerification(AppConstant.PLUS + phone_code + mobileNumber + "");
    }

    private void startPhoneNumberVerification(String phoneNumber) {
        // [START start_phone_auth]
        Snackbar.make(view, "OTP sent", Snackbar.LENGTH_LONG).show();
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                30,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                getActivity(),               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
        //    tv_resend.setEnabled(false);
        tv_resend.setEnabled(false);
        tv_resend.setVisibility(View.GONE);
        tvCountDownTimer.setVisibility(View.VISIBLE);
        startCounter();
    }

    private void startCounter() {
        if (countDownTimer != null)
            countDownTimer.cancel();

        countDownTimer = new CountDownTimer(30000, 1000) {
            public void onTick(long millisUntilFinished) {
                //here you can have your logic to set text to edittext
                tvCountDownTimer.setText("00."+millisUntilFinished / 1000);
            }
            public void onFinish() {
                tvCountDownTimer.setText("");
                tv_resend.setEnabled(true);
                tv_resend.setVisibility(View.VISIBLE);
                tvCountDownTimer.setVisibility(View.GONE);
            }
        };
        countDownTimer.start();
    }
    private void callDelay() {
        Intent intent = new Intent(getContext(), MapsActivity.class);
        startActivity(intent);
        Objects.requireNonNull(getActivity()).finish();
//        final Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//
//            }
//        }, 3000);
    }
}
