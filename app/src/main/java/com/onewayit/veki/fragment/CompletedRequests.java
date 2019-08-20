package com.onewayit.veki.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.loopj.android.http.BaseJsonHttpResponseHandler;
import com.onewayit.veki.Adapters.CompletedRequestsListAdapter;
import com.onewayit.veki.GetterSetter.RequestsData;
import com.onewayit.veki.Preferences.UserSessionPreferences;
import com.onewayit.veki.R;
import com.onewayit.veki.RestClient.RestClient;
import com.onewayit.veki.utilities.GlobalClass;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.ByteArrayEntity;
import cz.msebera.android.httpclient.message.BasicHeader;
import pl.droidsonroids.gif.GifImageView;

public class CompletedRequests extends Fragment implements View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    RelativeLayout my_request_item, rl_loading;
    // TODO: Rename and change types of parameters
    View view;


    ListView lv_requests;
    ArrayList<RequestsData> requestsList;
    Context context;
    TextView tv_action;
    GifImageView gifnoresult;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_requests_posted, container, false);
        context = getActivity();
        findViewById();
        setOnClickListener();
        // Inflate the layout for this fragment
        getMyRequests();
        return view;
    }

    private void setOnClickListener() {
    }
    private void findViewById() {
        my_request_item = view.findViewById(R.id.my_request_item);
        tv_action = view.findViewById(R.id.tv_action);
        lv_requests = view.findViewById(R.id.lv_requests);
        rl_loading = view.findViewById(R.id.rl_loading);
        gifnoresult = view.findViewById(R.id.gifnoresult);
        requestsList = new ArrayList<>();


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        }
    }

    private void getMyRequests() {
        startProgress();
        final GlobalClass globalClass = new GlobalClass();
        globalClass.cancelProgressBarInterection(true, getActivity());
        RestClient restClient = new RestClient();
        final UserSessionPreferences sessionPreferences = new UserSessionPreferences(context);
        final String relativeUrl = "requests?user_id=" + sessionPreferences.getUserID();
        ByteArrayEntity entity = null;
        //Log.e("password is=",encrptPass);
        List<Header> headers = new ArrayList<Header>();
        headers.add(new BasicHeader("Authorization", sessionPreferences.getToken()));
        Log.e("Token and url", relativeUrl + " " + headers);
        restClient.getWithheader(getActivity(), relativeUrl, headers.toArray(new Header[headers.size()]), null, new BaseJsonHttpResponseHandler<JSONArray>() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, final String rawJsonResponse, JSONArray response) {
                Log.e("response", rawJsonResponse);
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        closeProgress();
                        globalClass.cancelProgressBarInterection(false, getActivity());
                        try {
                            JSONObject object = new JSONObject(rawJsonResponse);
                            if (object.getString("status").equalsIgnoreCase("Success")) {
                                JSONArray requests_data = object.getJSONArray("data");
                                if (requests_data.length() == 0) {
                                    closeProgress();
                                    gifnoresult.setVisibility(View.VISIBLE);
                                    gifnoresult.setImageResource(R.drawable.no_result_found);
                                } else if (requests_data.length() > 0) {
                                    for (int i = 0; i < requests_data.length(); i++) {
                                        JSONObject request = new JSONObject(String.valueOf(requests_data.get(i)));
                                        JSONObject serviceData = new JSONObject(request.getString("service"));
                                        if (request.getString("status").equalsIgnoreCase("2") || request.getString("status").equalsIgnoreCase("6")) {
                                            RequestsData requestsData = new RequestsData();
                                            requestsData.setLatitude(request.getString("latitude"));
                                            requestsData.setLongitude(request.getString("longitude"));
                                            requestsData.setStatus(request.getString("status"));
                                            requestsData.setAccepted_user_id(request.getString("accepted_user_id"));
                                            requestsData.setName(sessionPreferences.getUserFirstName());
                                            requestsData.setEmail(sessionPreferences.getEmailId());
                                            requestsData.setPhone(sessionPreferences.getMobile());
                                            requestsData.setPhone(sessionPreferences.getMobile());
                                            //   requestsData.setAddress(request.getString("address"));
                                            requestsData.setId(request.getString("id"));
                                            requestsData.setServiceName(serviceData.getString("name"));
                                            requestsData.setMinServicePrice(request.getString("amount"));
                                            requestsData.setNotes(request.getString("notes"));
                                            // requestsData.setDistance(request.getString("distance"));
                                            requestsData.setPhoto(sessionPreferences.getProfile_Status());
                                            requestsData.setTimeAgo(String.valueOf(globalClass.getTimeDifference(request.getString("updated_at"))) + "");
                                            requestsData.setDistance(String.format("%.2f", globalClass.distance(Double.parseDouble(sessionPreferences.getLat()), Double.parseDouble(sessionPreferences.getLon()), Double.parseDouble(request.getString("latitude")), Double.parseDouble(request.getString("longitude")))) + " Km");
                                            requestsData.setTag("request " + i);
                                            ArrayList<String> images = new ArrayList<>();
                                            if (request.has("images")) {
                                                JSONArray imagesArray = request.getJSONArray("images");

                                                for (int j = 0; j < imagesArray.length(); j++) {
                                                    images.add(new JSONObject(String.valueOf(imagesArray.get(j))).getString("image"));
                                                }

                                            }
                                            requestsData.setImages(images);
                                            requestsList.add(requestsData);
                                        }
                                    }
                                    setAdapter();

                                }

                            } else {
                                gifnoresult.setVisibility(View.VISIBLE);
                                gifnoresult.setImageResource(R.drawable.no_result_found);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, 600);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, JSONArray errorResponse) {
                Toast.makeText(context, "Error while fetching requests...", Toast.LENGTH_SHORT).show();
                globalClass.cancelProgressBarInterection(false, getActivity());
                closeProgress();
            }

            @Override
            protected JSONArray parseResponse(String rawJsonData, boolean isFailure) throws Throwable {
                return null;
            }
        });
    }

    private void setAdapter() {
        if (requestsList.size() > 0) {
            CompletedRequestsListAdapter requestsListAdapter = new CompletedRequestsListAdapter(getActivity(), requestsList, getActivity().getSupportFragmentManager());
            lv_requests.setAdapter(requestsListAdapter);
            lv_requests.setVisibility(View.VISIBLE);
            gifnoresult.setVisibility(View.GONE);
            rl_loading.setVisibility(View.GONE);
        } else {
            lv_requests.setVisibility(View.GONE);
            gifnoresult.setVisibility(View.VISIBLE);
            gifnoresult.setImageResource(R.drawable.no_result_found);
            rl_loading.setVisibility(View.GONE);
        }
    }

    public void startProgress() {
        rl_loading.setVisibility(View.VISIBLE);
        tv_action.setText("Loading requests..");
    }

    public void closeProgress() {
        lv_requests.setVisibility(View.VISIBLE);
        rl_loading.setVisibility(View.GONE);
        // progressBarGIFDialog.
    }
}
