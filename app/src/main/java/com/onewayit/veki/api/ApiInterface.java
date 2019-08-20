package com.onewayit.veki.api;

import com.google.gson.JsonObject;
import com.onewayit.veki.api.apiResponse.emailLogin.LoginResponse;
import com.onewayit.veki.api.apiResponse.feedback.feedbackResponse;
import com.onewayit.veki.api.apiResponse.forgot.ForgotPasswordResponse;
import com.onewayit.veki.api.apiResponse.forgot.ForgotPasswordVerifyResponse;
import com.onewayit.veki.api.apiResponse.forgot.ForgotUpdatePasswordResponse;
import com.onewayit.veki.api.apiResponse.nearByRequests.NearByRequestsResponse;
import com.onewayit.veki.api.apiResponse.otp.OtpResponse;
import com.onewayit.veki.api.apiResponse.otp.VerifyOtpResponse;
import com.onewayit.veki.api.apiResponse.registration.FbRegistrationResponse;
import com.onewayit.veki.api.apiResponse.registration.RegistrationResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiInterface {

    @POST("users/login")
    Call<LoginResponse> loginUser(@Body JsonObject jsonObject);

    @POST("users/register")
    Call<RegistrationResponse> registerUser(@Body JsonObject jsonObject);

    @POST("users/send/otp")
    Call<OtpResponse> generateOtp(@Body JsonObject jsonObject);

    @POST("users/register/verify")
    Call<VerifyOtpResponse> verifyOtp(@Body JsonObject jsonObject);

    @POST("users/reset/password")
    Call<ForgotPasswordResponse> forgotPassword(@Body JsonObject jsonObject);

    @POST("users/reset/password/verify")
    Call<ForgotPasswordVerifyResponse> forgotPasswordVerify(@Body JsonObject jsonObject);

    @POST("users/reset/password/update")
    Call<ForgotUpdatePasswordResponse> forgotPasswordUpdate(@Body JsonObject jsonObject);

    @GET("services/request/nearby")
    Call<NearByRequestsResponse> nearByRequests(@Query("latitude") String latitude, @Query("longitude") String longitude);

    @POST("users/login/facebook")
    Call<FbRegistrationResponse> fbRegisteration(@Body JsonObject jsonObject);

    @POST("reviews")
    Call<feedbackResponse> giveReviews(@Body JsonObject jsonObject);

}
