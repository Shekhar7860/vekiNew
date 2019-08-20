package com.onewayit.veki.api.apiResponse.otp;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.json.JSONObject;

import java.io.Serializable;

public class OtpResponse implements Serializable {

    private final static long serialVersionUID = 3908831457763202275L;
    @SerializedName("otp")
    @Expose
    private String otp;
    @SerializedName("status_code")
    @Expose
    private String statusCode;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("data")
    @Expose
    private JSONObject data;

    public JSONObject getData() {
        return data;
    }

    public String getOtpNumber() {
        return otp;
    }

    public void setOtpNumber(String otpNumber) {
        this.otp = otpNumber;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
