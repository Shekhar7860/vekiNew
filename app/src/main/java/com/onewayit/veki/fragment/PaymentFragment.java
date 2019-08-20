package com.onewayit.veki.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonObject;
import com.loopj.android.http.BaseJsonHttpResponseHandler;
import com.onewayit.veki.Preferences.UserSessionPreferences;
import com.onewayit.veki.R;
import com.onewayit.veki.RestClient.RestClient;
import com.onewayit.veki.activities.MapsActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.ByteArrayEntity;
import cz.msebera.android.httpclient.message.BasicHeader;

public class PaymentFragment extends Fragment implements View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    Button btn_submit;
    View view;
    ImageView back_button_home_activity;
    TextView tv_price;
    Context context;
    RelativeLayout rl_loading;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        context = getActivity();
        view = inflater.inflate(R.layout.fragment_payment, container, false);
        findViewById();
        setClickListener();
        getData();
        return view;
    }

    private void getData() {
        tv_price.setText("$" + getArguments().getString("price"));
    }

    private void setClickListener() {
        btn_submit.setOnClickListener(this);
        back_button_home_activity.setOnClickListener(this);
    }

    private void findViewById() {
        btn_submit = view.findViewById(R.id.btn_submit);
        tv_price = view.findViewById(R.id.tv_price);
        back_button_home_activity = view.findViewById(R.id.back_button_home_activity);
        rl_loading = view.findViewById(R.id.rl_loading);
    }

    @Override
    public void onClick(View v) {
        UserSessionPreferences sessionPreferences = new UserSessionPreferences(context);
        switch (v.getId()) {
            case R.id.btn_submit: {
                updateRequest("1", sessionPreferences.getUserID());
                break;
            }
            case R.id.back_button_home_activity: {
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.popBackStackImmediate();
                break;
            }
        }

    }

    // TODO: Rename method, update argument and hook method into UI event

    private void updateRequest(String status, String userId) {
        UserSessionPreferences sessionPreferences = new UserSessionPreferences(context);
        RestClient restClient = new RestClient();
        rl_loading.setVisibility(View.VISIBLE);
        String relativeUrl = "requests/" + getArguments().getString("requestId");
        ByteArrayEntity entity = null;
        Log.e("Params: request ", String.valueOf(getRequestParameters(status, userId)) + getArguments().getString("requestId"));
        try {
            entity = new ByteArrayEntity((getRequestParameters(status, userId).toString()).getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        List<Header> headers = new ArrayList<Header>();
        headers.add(new BasicHeader("Authorization", sessionPreferences.getToken()));
        restClient.patchtWithHeader(context, relativeUrl, headers.toArray(new Header[headers.size()]), entity, "application/json", new BaseJsonHttpResponseHandler<JSONArray>() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, JSONArray response) {
                Log.e("response", rawJsonResponse);
                rl_loading.setVisibility(View.GONE);
                if (statusCode == 200) {
                    try {
                        JSONObject jsonObject = new JSONObject(rawJsonResponse);
                        if (jsonObject.getString("status").equalsIgnoreCase("Success")) {
                            Intent intent = new Intent(getContext(), MapsActivity.class);
                            startActivity(intent);
                            Objects.requireNonNull(getActivity()).finish();
                        } else {
                            Snackbar.make(view, jsonObject.getString("message"), Snackbar.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else if (statusCode == 400) {

                    try {
                        JSONObject jsonObject = new JSONObject(rawJsonResponse);
                        Snackbar.make(view, jsonObject.getString("message"), Snackbar.LENGTH_LONG).show();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {

                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, JSONArray errorResponse) {
                rl_loading.setVisibility(View.GONE);
                if (statusCode == 400) {
                    try {
                        JSONObject jsonObject = new JSONObject(rawJsonData);
                        Snackbar.make(view, jsonObject.getString("message"), Snackbar.LENGTH_LONG).show();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    view.setClickable(false);
                }
            }
            @Override
            protected JSONArray parseResponse(String rawJsonData, boolean isFailure) throws Throwable {
                return null;
            }
        });
    }
    private JsonObject getRequestParameters(String status, String userID) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("status", status);
        jsonObject.addProperty("user_id", userID);
        jsonObject.addProperty("accepted_user_id", getArguments().getString("user_id"));
        return jsonObject;
    }
}
