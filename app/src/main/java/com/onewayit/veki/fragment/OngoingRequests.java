package com.onewayit.veki.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.loopj.android.http.BaseJsonHttpResponseHandler;
import com.onewayit.veki.Adapters.OngoingRequestListAdapter;
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

public class OngoingRequests extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    View view;
    ViewPager viewPager;
    TabLayout tabLayout;
    ListView lv_requests;
    RelativeLayout rl_loading;
    GifImageView gifnoresult;
    TextView tv_action;
    Context context;
    ArrayList<RequestsData> servicesList;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_pending_requests, container, false);
        context = getActivity();
        findViewById();
        getMyServices();
        return view;
    }

    private void findViewById() {
        lv_requests = view.findViewById(R.id.lv_requests);
        rl_loading = view.findViewById(R.id.rl_loading);
        gifnoresult = view.findViewById(R.id.gifnoresult);
        tv_action = view.findViewById(R.id.tv_action);
        servicesList = new ArrayList<>();
    }

    public void startProgress() {
        lv_requests.setVisibility(View.GONE);
        rl_loading.setVisibility(View.VISIBLE);
        tv_action.setText("Loading pending requests..");
    }

    public void closeProgress() {
        lv_requests.setVisibility(View.VISIBLE);
        rl_loading.setVisibility(View.GONE);
        // progressBarGIFDialog.
    }

    public void closeProgressNoResult() {
        lv_requests.setVisibility(View.GONE);
        rl_loading.setVisibility(View.GONE);
        gifnoresult.setImageResource(R.drawable.no_result_found);
        // progressBarGIFDialog.
    }

    private void getMyServices() {
        startProgress();
        final GlobalClass globalClass = new GlobalClass();
        globalClass.cancelProgressBarInterection(true, getActivity());
        RestClient restClient = new RestClient();
        final UserSessionPreferences sessionPreferences = new UserSessionPreferences(getActivity());
        final String relativeUrl = "requests?user_id=" + sessionPreferences.getUserID();
        ByteArrayEntity entity = null;
        //Log.e("password is=",encrptPass);
        List<Header> headers = new ArrayList<Header>();
        headers.add(new BasicHeader("Authorization", sessionPreferences.getToken()));
        Log.e("Token and url", relativeUrl + " " + headers);
        restClient.getWithheader(getActivity(), relativeUrl, headers.toArray(new Header[headers.size()]), null, new BaseJsonHttpResponseHandler<JSONArray>() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, JSONArray response) {
                Log.e("response dekh", rawJsonResponse);
                globalClass.cancelProgressBarInterection(false, getActivity());
                try {
                    JSONObject object = new JSONObject(rawJsonResponse);
                    if (object.getString("status").equalsIgnoreCase("Success")) {
                        JSONArray requests_data = object.getJSONArray("data");
                        if (requests_data.length() == 0) {
                            closeProgressNoResult();
                        } else if (requests_data.length() > 0) {
                            for (int i = 0; i < requests_data.length(); i++) {

                                JSONObject request = new JSONObject(String.valueOf(requests_data.get(i)));
                                JSONObject serviceData = new JSONObject(request.getString("service"));
                                if (request.getString("status").equalsIgnoreCase("1")) {
                                    RequestsData requestsData = new RequestsData();
                                    requestsData.setLatitude(request.getString("latitude"));
                                    requestsData.setLongitude(request.getString("longitude"));
                                    requestsData.setAccepted_user_id(request.getString("accepted_user_id"));
                                    requestsData.setName(sessionPreferences.getUserFirstName());
                                    requestsData.setEmail(sessionPreferences.getEmailId());
                                    requestsData.setPhone(sessionPreferences.getMobile());
                                    requestsData.setPhone(sessionPreferences.getMobile());
                                    //   requestsData.setAddress(request.getString("address"));
                                    requestsData.setId(request.getString("id"));
                                    requestsData.setServiceName(serviceData.getString("name"));
                                    requestsData.setMinServicePrice(request.getString("amount"));
                                    requestsData.setTimeAgo(String.valueOf(globalClass.getTimeDifference(request.getString("updated_at"))) + "");
                                    requestsData.setNotes(request.getString("notes"));
                                    requestsData.setDistance(String.format("%.2f", globalClass.distance(Double.parseDouble(sessionPreferences.getLat()), Double.parseDouble(sessionPreferences.getLon()), Double.parseDouble(request.getString("latitude")), Double.parseDouble(request.getString("longitude")))) + " Km");
                                    // requestsData.setDistance(request.getString("distance"));
                                    requestsData.setPhoto(sessionPreferences.getProfile_Status());
                                    requestsData.setTag("request " + i);
                                    ArrayList<String> images = new ArrayList<>();
                                    if (request.has("images")) {
                                        JSONArray imagesArray = request.getJSONArray("images");

                                        for (int j = 0; j < imagesArray.length(); j++) {
                                            images.add(new JSONObject(String.valueOf(imagesArray.get(j))).getString("image"));
                                        }

                                    }
                                    requestsData.setImages(images);
                                    servicesList.add(requestsData);
                                }
                            }
                            setAdapter();

                        }
                    } else {
                        closeProgressNoResult();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            private void setAdapter() {
                if (servicesList.size() > 0) {
                    OngoingRequestListAdapter requestsListAdapter = new OngoingRequestListAdapter(getActivity(), servicesList, getFragmentManager());
                    lv_requests.setAdapter(requestsListAdapter);
                    closeProgress();
                } else {
                    closeProgressNoResult();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, JSONArray errorResponse) {
                Toast.makeText(getActivity(), "Error while fetching requests...", Toast.LENGTH_SHORT).show();
                globalClass.cancelProgressBarInterection(false, getActivity());
                closeProgressNoResult();
            }

            @Override
            protected JSONArray parseResponse(String rawJsonData, boolean isFailure) throws Throwable {
                return null;
            }
        });
    }


}
