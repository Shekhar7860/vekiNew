package com.onewayit.veki.fragment;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
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
import com.onewayit.veki.api.ApiClient;
import com.onewayit.veki.api.ApiInterface;
import com.onewayit.veki.api.apiResponse.forgot.ForgotPasswordVerifyResponse;
import com.onewayit.veki.api.apiResponse.otp.VerifyOtpResponse;
import com.onewayit.veki.utilities.AppConstant;
import com.onewayit.veki.utilities.GlobalClass;
import com.onewayit.veki.utilities.Network;
import com.onewayit.veki.utilities.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.ByteArrayEntity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// import com.onewayit.veki.activities.AppConstant;

/**
 * A simple {@link Fragment} subclass.
 */
public class VerificationCodeFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    public Button emailbutton, mobilebutton;
    Spinner spinner_code;
    private View view;
    private EditText etDigit1, etDigit2, etDigit3, etDigit4, etDigit5, etDigit6;
    private Button btnContinue, btnResendCode;
    private ProgressBar progress_bar;
    private TextView tvToolbarTitle, tvCountDownTimer;
    private Context context;
    private TextView tvToolbarBack, mobile_number;
    private GlobalClass globalClass;
    private EditText password, confirm_password, et_email;
    private TextView submit, login, phone, tv_sentTo;
    private CheckBox service_provider, customer;
    private FirebaseAuth mAuth;
    private String mVerificationId, strPhoneCode, strPhoneNumber, emailCode, OtpCode, regPhone, userId, regEmail, refreshedToken, userData;
    private CountDownTimer countDownTimer;
    private LinearLayout llContinue;
    private RelativeLayout rlResend, rl_loading;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private FrameLayout mobileLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_verification_code, container, false);
        initializeVariables();
        findViewById();
        setOnClickListener();
        setUpUI();
        refreshedToken = FirebaseInstanceId.getInstance().getToken();
        emailCode = getArguments().getString("email");
        strPhoneCode = getArguments().getString(AppConstant.PhoneCode);
        getDataFromPreviousScreen();
        return view;
    }

    private void getDataFromPreviousScreen() {
        if (getArguments().getString(AppConstant.PhoneNumber) != null) {
            strPhoneNumber = getArguments().getString(AppConstant.PhoneNumber);
            if (!strPhoneNumber.equalsIgnoreCase("")) {
                sendOtp();
            }
            tvToolbarBack.setText("< Edit Number");
            tvToolbarTitle.setText(AppConstant.PLUS + strPhoneCode + "" + strPhoneNumber + "");
            mobile_number.setText(("One Time Password(OTP) has been sent to your mobile +" + strPhoneCode + strPhoneNumber)+", please enter the same here to login.");
        }
        if (getArguments().getString("userData") != null) {
            userData = getArguments().getString("userData");
            try {
                JSONObject jsonObj = new JSONObject(userData);
                String data = jsonObj.getString("data");
                JSONObject data2 = new JSONObject(data);
                userId = data2.getString("id");
                regEmail = data2.getString("email");
                regPhone = data2.getString("phone");
                sendOtp();
            } catch (JSONException e) {
                // TODO Auto-generated catch blfock
                Log.e("erroe", e.toString());
            }
        }
    }

    private void sendOtp() {
        mAuth = FirebaseAuth.getInstance();
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                Utility.log("onVerificationCompleted: " + credential);
                OtpCode = credential.getSmsCode();
                if (getArguments().getString("page").equals("register")) {
                    callRegisterVerifyAPI();
                } else {
                    callForgotPasswordAPI();
                }
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
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
                mVerificationId = verificationId;
                mResendToken = token;
            }
        };
        startPhoneNumberVerification(AppConstant.PLUS + strPhoneCode + strPhoneNumber + "");
    }

    private void startPhoneNumberVerification(String phoneNumber) {
        // [START start_phone_auth]
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                30,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                getActivity(),               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
    }

    private void startCounter() {
        if (countDownTimer != null)
            countDownTimer.cancel();
        countDownTimer = new CountDownTimer(30000, 1000) {
            public void onTick(long millisUntilFinished) {
                tvCountDownTimer.setText("" + millisUntilFinished / 1000);
                //here you can have your logic to set text to edittext
            }

            public void onFinish() {
                tvCountDownTimer.setText("");
                btnResendCode.setEnabled(true);
                setResendButtonEnableDisable();
            }
        };
        countDownTimer.start();
    }


    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        Log.d("mycode", credential.toString());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Utility.log("signInWithCredential:success");
                            final FirebaseUser user = task.getResult().getUser();
                            Utility.showToast(getActivity(), user.getPhoneNumber() + " verified successfully");
                            if (getArguments().getString("page").equals("register")) {
                                callRegisterVerifyAPI();
                            } else {
                                callForgotPasswordAPI();
                            }
                        } else {
                            Utility.log("signInWithCredential:failure " + task.getException());
                            Utility.showToast(getActivity(), " Verification failed");
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                            }
                        }
                    }
                });
    }

    private void initializeVariables() {
        context = getActivity();
        globalClass = new GlobalClass();
        ((LoginActivity) Objects.requireNonNull(getActivity())).setHeading("Verify OTP");
    }
    private void findViewById() {
        mobileLayout = (FrameLayout) view.findViewById(R.id.mobileLayout);
        emailbutton = view.findViewById(R.id.emailbutton);
        mobilebutton = view.findViewById(R.id.mobilebutton);
        //   et_email = view.findViewById(R.id.et_email);
        emailbutton.setTextColor(Color.parseColor("#1E339E"));
        mobileLayout.setVisibility(view.GONE);
        mobile_number = view.findViewById(R.id.mobile_number);
        password = view.findViewById(R.id.password);
        confirm_password = view.findViewById(R.id.confirm_password);
        submit = view.findViewById(R.id.submit);
        login = view.findViewById(R.id.login);
        tv_sentTo = view.findViewById(R.id.tv_sentTo);
        phone = view.findViewById(R.id.mobile);
        service_provider = view.findViewById(R.id.service_provider);
        customer = view.findViewById(R.id.customer);
        rl_loading = view.findViewById(R.id.rl_loading);
        spinner_code = view.findViewById(R.id.spinner_code);
        tv_sentTo.setText("Sent to " + getArguments().getString(AppConstant.PhoneCode + AppConstant.PhoneNumber));
    }

    private void setOnClickListener() {
        emailbutton.setOnClickListener(this);
        mobilebutton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.login:
                Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new EmailLoginFragment(), "EmailLoginFragment").addToBackStack(null).commit();
                break;
            case R.id.phone:
                Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new LoginFragment(), "LoginFragment").addToBackStack(null).commit();
                break;
        }
    }

    ///////////////Screen Validation, it will return true if user has fielded all required details/////////////

    private void setResendButtonEnableDisable() {
        if (btnResendCode.isEnabled()) {
            rlResend.setBackgroundResource(R.drawable.rectangle_circular_ends);
            btnResendCode.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
        } else {
            rlResend.setBackgroundResource(R.drawable.rectangle_circular_ends);
            btnResendCode.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
        }
    }

    private void setUpUI() {
        rlResend = view.findViewById(R.id.rlResend);
        llContinue = view.findViewById(R.id.llContinue);
        llContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnContinue.isClickable())
                    btnContinue.performClick();
            }
        });
        // pbVerify = findViewById(R.id.pbVerify);

        btnContinue = view.findViewById(R.id.btnContinue);
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getArguments().getString(AppConstant.PhoneNumber) != "") {
                    //Utility.hideKeyBoardFromView(getActivity());
                    if (validate()) {
                        if (!TextUtils.isEmpty(mVerificationId)) {
                            verifyPhoneNumberWithCode(mVerificationId,
                                    etDigit1.getText().toString().trim() +
                                            etDigit2.getText().toString().trim() +
                                            etDigit3.getText().toString().trim() +
                                            etDigit4.getText().toString().trim() +
                                            etDigit5.getText().toString().trim() +
                                            etDigit6.getText().toString().trim());
                        } else {
                            Utility.showToast(getActivity(), "Verification id not received");
                        }
                    }
                    Log.d("verifiedemail", "this is mobile ");
                } else {
                    Log.d("verifiedemail", "this is email");
                    callForgotVerifyAPIEmail();
                }

            }
        });

        btnResendCode = view.findViewById(R.id.btnResendCode);
        btnResendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //  Utility.hideKeyBoardFromView(getActivity()));
                if (!strPhoneNumber.equalsIgnoreCase("") )
                    resendVerificationCode(AppConstant.PLUS + strPhoneCode + strPhoneNumber, mResendToken);
                else {
                    callResendPasswordAPIMail();
                    //  onBackPressed();
                }
            }
        });


        tvToolbarBack = view.findViewById(R.id.tvToolbarBack);
        tvToolbarTitle = view.findViewById(R.id.tvToolbarTitle);
        tvCountDownTimer = view.findViewById(R.id.tvCountDownTimer);

        etDigit1 = view.findViewById(R.id.etDigit1);
        etDigit2 = view.findViewById(R.id.etDigit2);
        etDigit3 = view.findViewById(R.id.etDigit3);
        etDigit4 = view.findViewById(R.id.etDigit4);
        etDigit5 = view.findViewById(R.id.etDigit5);
        etDigit6 = view.findViewById(R.id.etDigit6);

        setButtonContinueClickbleOrNot();
        tvToolbarBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //  onBackPressed();

            }
        });
        etDigit1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                setButtonContinueClickbleOrNot();
                if (editable.toString().length() == 1) {
                    etDigit2.requestFocus();
                }
            }
        });
        etDigit2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                setButtonContinueClickbleOrNot();
                if (editable.toString().length() == 1) {
                    etDigit3.requestFocus();
                } else {
                    etDigit1.requestFocus();
                }
            }
        });
        etDigit3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                setButtonContinueClickbleOrNot();
                if (editable.toString().length() == 1) {
                    etDigit4.requestFocus();
                } else {
                    etDigit2.requestFocus();
                }
            }
        });
        etDigit4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                setButtonContinueClickbleOrNot();
                if (editable.toString().length() == 1) {
                    etDigit5.requestFocus();
                } else {
                    etDigit3.requestFocus();
                }
            }
        });
        etDigit5.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                setButtonContinueClickbleOrNot();
                if (editable.toString().length() == 1) {
                    etDigit6.requestFocus();
                } else {
                    etDigit4.requestFocus();
                }
            }
        });
        etDigit6.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                setButtonContinueClickbleOrNot();
                if (editable.toString().length() == 1) {
                } else {
                    etDigit5.requestFocus();
                }
            }
        });

        etDigit1.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                } else {
                    if (etDigit1.getText().toString().trim().length() == 1) {
                        etDigit2.requestFocus();
                    }
                }
                return false;
            }
        });
        etDigit2.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    if (etDigit2.getText().toString().trim().length() == 0)
                        etDigit1.requestFocus();
                } else {
                    if (etDigit2.getText().toString().trim().length() == 1) {
                        etDigit3.requestFocus();
                    }
                }
                return false;
            }
        });
        etDigit3.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    if (etDigit3.getText().toString().trim().length() == 0)
                        etDigit2.requestFocus();
                } else {
                    if (etDigit3.getText().toString().trim().length() == 1) {
                        etDigit4.requestFocus();
                    }
                }
                return false;
            }
        });
        etDigit4.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    if (etDigit4.getText().toString().trim().length() == 0)
                        etDigit3.requestFocus();
                } else {
                    if (etDigit4.getText().toString().trim().length() == 1) {
                        etDigit5.requestFocus();
                    }
                }
                return false;
            }
        });
        etDigit5.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    if (etDigit5.getText().toString().trim().length() == 0)
                        etDigit4.requestFocus();
                } else {
                    if (etDigit5.getText().toString().trim().length() == 1) {
                        etDigit6.requestFocus();
                    }
                }
                return false;
            }
        });
        etDigit6.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    if (etDigit6.getText().toString().trim().length() == 0)
                        etDigit5.requestFocus();
                }
                return false;
            }
        });

    }

    private boolean validate() {
        if (TextUtils.isEmpty(etDigit1.getText().toString().trim())) {
            return false;
        } else if (TextUtils.isEmpty(etDigit2.getText().toString().trim())) {
            return false;
        } else if (TextUtils.isEmpty(etDigit3.getText().toString().trim())) {
            return false;
        } else if (TextUtils.isEmpty(etDigit4.getText().toString().trim())) {
            return false;
        } else if (TextUtils.isEmpty(etDigit5.getText().toString().trim())) {
            return false;
        } else if (TextUtils.isEmpty(etDigit6.getText().toString().trim())) {
            return false;
        }
        return true;
    }

    private void setButtonContinueClickbleOrNot() {
        if (!validate()) {
            llContinue.setAlpha(.5f);
            btnContinue.setClickable(false);
        } else {
            llContinue.setAlpha(1.0f);
            btnContinue.setClickable(true);
        }
    }


    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void callResendPasswordAPIMail() {
        //   globalClass.cancelProgressBarInterection(true, this);
        // progress_bar.setVisibility(View.VISIBLE);
        RestClient restClient = new RestClient();
        String relativeUrl = "users/reset/password";
        ByteArrayEntity entity = null;
        try {
            entity = new ByteArrayEntity((getForgotPasswordParametersEmail2().toString()).getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Log.e("Params", getForgotPasswordParametersEmail2().toString());
        restClient.postRequestJson(context, relativeUrl, entity, "application/json", new BaseJsonHttpResponseHandler<JSONArray>() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, JSONArray response) {
                Log.e("Params", rawJsonResponse);
                //   progress_bar.setVisibility(View.GONE);
                globalClass.cancelProgressBarInterection(false, getActivity());
                try {
                    JSONObject object = new JSONObject(rawJsonResponse);
                    if (statusCode == 200) {
                        if (object.getString("status").equalsIgnoreCase("Success")) {
                            Snackbar.make(view, "OTP Sent Successfully", Snackbar.LENGTH_LONG).show();
                            //  Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new ResetPasswordFragment(), "LoginFragment").addToBackStack(null).commit();
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
                    //  progress_bar.setVisibility(View.GONE);
                    globalClass.cancelProgressBarInterection(false, getActivity());
                }
            }

            @Override
            protected JSONArray parseResponse(String rawJsonData, boolean isFailure) throws Throwable {
                return null;
            }
        });
    }

    private void resendVerificationCode(String phoneNumber, PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                30,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                getActivity(),               // Activity (for callback binding)
                mCallbacks,         // OnVerificationStateChangedCallbacks
                token);             // ForceResendingToken from callbacks
        startCounter();
        btnResendCode.setEnabled(false);
        setResendButtonEnableDisable();
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

    // Verify OTP API
    private void callRegisterVerifyAPI() {
        //   globalClass.cancelProgressBarInterection(true, this);
        rl_loading.setVisibility(View.VISIBLE);
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<VerifyOtpResponse> call = apiService.verifyOtp(getRegisterVerfifyParameters());
        Log.e(" Registration url", "" + call.request().url().toString());
        call.enqueue(new Callback<VerifyOtpResponse>() {
            @Override
            public void onResponse(Call<VerifyOtpResponse> call, Response<VerifyOtpResponse> response) {
                //  globalClass.cancelProgressBarInterection(false, this);
                if (response.code() == 200) {
                    rl_loading.setVisibility(View.GONE);
                    Log.e("RegistrationOTP", "" + globalClass.getJsonString(response.body()));
                    UserSessionPreferences sessionPreferences = new UserSessionPreferences(context);
                    sessionPreferences.setUserID(userId);
                    sessionPreferences.setEmailId(regEmail);
                    sessionPreferences.setMobile(regPhone);
                    JSONObject jsonObj = null;
                    try {
                        jsonObj = new JSONObject(globalClass.getJsonString(response.body()));
                        String userData = jsonObj.getString("data");
                        JSONObject sessionData = new JSONObject(userData);
                        sessionPreferences.setToken("Bearer " + sessionData.getString("token"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Snackbar.make(view, "OTP Verified Successfully", Snackbar.LENGTH_LONG).show();
                    callDelay();
                } else {
                    Log.e("errorret", response.toString());
                    rl_loading.setVisibility(View.GONE);
                    Snackbar.make(view, response.toString(), Snackbar.LENGTH_LONG).show();
                    globalClass.retrofitNetworkErrorHandler(response.code(), view, getActivity());
                }
            }

            @Override
            public void onFailure(Call<VerifyOtpResponse> call, Throwable t) {
                rl_loading.setVisibility(View.GONE);
                globalClass.cancelProgressBarInterection(false, getActivity());
                Log.e("retrofit error", "" + t.getMessage());
                if (t instanceof IOException) {
                    try {

                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    }

                }
            }
        });

    }

    @SuppressLint("HardwareIds")
    private JsonObject getRegisterVerfifyParameters() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", userId);
        jsonObject.addProperty("otp", "546654");
        jsonObject.addProperty("device_id", Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID));
        jsonObject.addProperty("device_token", "12345");
        jsonObject.addProperty("device_type", "android");
        Log.e("verify parameters", jsonObject.toString());

        return jsonObject;

    }

    private void callForgotPasswordAPI() {
        //   globalClass.cancelProgressBarInterection(true, this);
        rl_loading.setVisibility(View.VISIBLE);
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<ForgotPasswordVerifyResponse> call = apiService.forgotPasswordVerify(getForgotPasswordParameters());
        Log.e(" Registration url", "" + call.request().url().toString());
        call.enqueue(new Callback<ForgotPasswordVerifyResponse>() {
            @Override
            public void onResponse(Call<ForgotPasswordVerifyResponse> call, Response<ForgotPasswordVerifyResponse> response) {
                //  globalClass.cancelProgressBarInterection(false, this);
                if (response.code() == 200) {
                    rl_loading.setVisibility(View.GONE);
                    Log.e("forgotPasswordAPI", "" + globalClass.getJsonString(response.body()));
                    Snackbar.make(view, "OTP Verified Successfully", Snackbar.LENGTH_LONG).show();

                    ResetPasswordFragment resetPasswordFragment = new ResetPasswordFragment();
                    Bundle args = new Bundle();
                    if (getArguments().getString(AppConstant.PhoneNumber) == "") {
                        args.putString("type", "email");
                        args.putString("email", getArguments().getString("email"));
                        args.putString("code", "");
                        args.putString("phone", "");
                    } else {
                        args.putString("type", "phone");
                        args.putString("email", "");
                        args.putString("code", getArguments().getString(AppConstant.PhoneCode));
                        args.putString("phone", getArguments().getString(AppConstant.PhoneNumber));
                    }
                    args.putString(AppConstant.PhoneCode, "");
                    args.putString("page", "forgot");
                    args.putInt("otp", getArguments().getInt("otp"));
                    //  args.putString("userData",  globalClass.getJsonString(response.body()));
                    resetPasswordFragment.setArguments(args);
                    Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, resetPasswordFragment, "ResetPassword").commit();
                    // Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new ProfileFragment(), "ProfileFragment").addToBackStack(null).commit();
                } else {
                    rl_loading.setVisibility(View.GONE);
                    Log.e("errorret", response.toString());
                    Snackbar.make(view, response.toString(), Snackbar.LENGTH_LONG).show();
                    globalClass.retrofitNetworkErrorHandler(response.code(), view, getActivity());
                }
            }

            @Override
            public void onFailure(Call<ForgotPasswordVerifyResponse> call, Throwable t) {
                // progress_bar.setVisibility(View.GONE);
                globalClass.cancelProgressBarInterection(false, getActivity());
                Log.e("retrofit error", "" + t.getMessage());
                if (t instanceof IOException) {
                    try {

                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    }

                }
            }
        });

    }

    @SuppressLint("HardwareIds")
    private JsonObject getForgotPasswordParameters() {
        SharedPreferences pref = getActivity().getSharedPreferences("MY_PREFERENCES", Activity.MODE_PRIVATE);
        Integer otp2 = pref.getInt("otp", 0);
        Log.d("MYINT", "value: " + otp2);
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("phone", strPhoneNumber);
        jsonObject.addProperty("otp", getArguments().getInt("otp"));
        jsonObject.addProperty("phone_code", strPhoneCode);
        return jsonObject;

    }

    private void callForgotVerifyAPIEmail() {
        Log.e("emailCheck", "this is email");
        //   globalClass.cancelProgressBarInterection(true, this);
        rl_loading.setVisibility(View.VISIBLE);
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<ForgotPasswordVerifyResponse> call = apiService.forgotPasswordVerify(getForgotPasswordParametersEmail());
        call.enqueue(new Callback<ForgotPasswordVerifyResponse>() {
            @Override
            public void onResponse(Call<ForgotPasswordVerifyResponse> call, Response<ForgotPasswordVerifyResponse> response) {
                //  globalClass.cancelProgressBarInterection(false, this);
                if (response.code() == 200) {
                    rl_loading.setVisibility(View.GONE);
                    Log.e("forgotPasswordAPI", "" + globalClass.getJsonString(response.body()));
                    Snackbar.make(view, "OTP Verified Successfully", Snackbar.LENGTH_LONG).show();
                    ResetPasswordFragment resetPasswordFragment = new ResetPasswordFragment();
                    Bundle args = new Bundle();
                    if (getArguments().getString(AppConstant.PhoneNumber) == "") {
                        args.putString("type", "email");
                        args.putString("email", getArguments().getString("email"));
                        args.putString("code", "");
                        args.putString("phone", "");
                    } else {
                        args.putString("type", "phone");
                        args.putString("email", "");
                        args.putString("code", getArguments().getString(AppConstant.PhoneCode));
                        args.putString("phone", getArguments().getString(AppConstant.PhoneNumber));
                    }
                    args.putString(AppConstant.PhoneCode, "");
                    args.putString("page", "forgot");
                    args.putInt("otp", getArguments().getInt("otp"));
                    //  args.putString("userData",  globalClass.getJsonString(response.body()));
                    resetPasswordFragment.setArguments(args);
                    Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, resetPasswordFragment, "ResetPassword").addToBackStack(null).commit();
                } else {
                    rl_loading.setVisibility(View.GONE);
                    Log.e("errorret", globalClass.getJsonString(response.body()));
                    Snackbar.make(view, response.toString(), Snackbar.LENGTH_LONG).show();
                    globalClass.retrofitNetworkErrorHandler(response.code(), view, getActivity());
                }
            }

            @Override
            public void onFailure(Call<ForgotPasswordVerifyResponse> call, Throwable t) {
                // progress_bar.setVisibility(View.GONE);
                globalClass.cancelProgressBarInterection(false, getActivity());
                Log.e("retrofit error", "" + t.getMessage());
                if (t instanceof IOException) {
                    try {

                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @SuppressLint("HardwareIds")
    private JsonObject getForgotPasswordParametersEmail2() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("email", emailCode);
        jsonObject.addProperty("otp", getArguments().getInt("otp"));
        Log.d("checkParams", jsonObject.toString());
        return jsonObject;
    }

    @SuppressLint("HardwareIds")
    private JsonObject getForgotPasswordParametersEmail() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("email", emailCode);
        jsonObject.addProperty("otp", getArguments().getInt("otp"));
        Log.d("checkParams", jsonObject.toString());
        return jsonObject;
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

