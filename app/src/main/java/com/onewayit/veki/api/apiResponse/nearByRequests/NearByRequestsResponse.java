package com.onewayit.veki.api.apiResponse.nearByRequests;

import com.google.gson.JsonArray;

public class NearByRequestsResponse {
    String status, message;
    JsonArray data;

    public JsonArray getData() {
        return data;
    }

    public void setData(JsonArray data) {
        this.data = data;
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
