package com.onewayit.veki.RestClient;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpEntity;

/**
 * Created by owITsol2 on 06-11-2015.
 */
public class RestClient {
    private static final String BASE_URL = "http://139.59.11.192/veki/api/v1/";

    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void put(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.put(getAbsoluteUrl(url), params, responseHandler);
    }

    //    public static void delete(String url, RequestParams params, AsyncHttpResponseHandler responseHandler){
//        client.delete(getAbsoluteUrl(url),params,responseHandler);
//    }
    public static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }

    public void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public void getWithheader(Context context, String url, Header[] header, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(context, getAbsoluteUrl(url), header, params, responseHandler);
    }

    public void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    public void postWithHeader(Context context, String url, Header[] header, HttpEntity entity, String contentType, AsyncHttpResponseHandler responseHandler) {
        client.post(context, getAbsoluteUrl(url), header, entity, contentType, responseHandler);
    }

    public void putWithHeader(Context context, String url, Header[] header, HttpEntity entity, String contentType, AsyncHttpResponseHandler responseHandler) {
        client.put(context, getAbsoluteUrl(url), header, entity, contentType, responseHandler);
    }

    public void postRequestJson(Context context, String url, HttpEntity entity, String contentType, AsyncHttpResponseHandler responseHandler) {
        client.post(context, getAbsoluteUrl(url), entity, contentType, responseHandler);
    }

    public void getRequestJson(Context context, String url, HttpEntity entity, String contentType, AsyncHttpResponseHandler responseHandler) {
        client.get(context, getAbsoluteUrl(url), entity, contentType, responseHandler);
    }

    public void getNearByLocation(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(url, params, responseHandler);
    }

    public void cancelRequest(Context context, boolean result) {
        client.cancelRequests(context, result);
    }

    public void patchtRequestJson(Context context, String url, HttpEntity entity, String contentType, AsyncHttpResponseHandler responseHandler) {
        client.patch(context, getAbsoluteUrl(url), entity, contentType, responseHandler);
    }

    public void patchtWithHeader(Context context, String url, Header[] header, HttpEntity entity, String contentType, AsyncHttpResponseHandler responseHandler) {
        client.patch(context, getAbsoluteUrl(url), header, entity, contentType, responseHandler);
    }
}
