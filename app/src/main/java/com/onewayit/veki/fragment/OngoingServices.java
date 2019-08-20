package com.onewayit.veki.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.loopj.android.http.BaseJsonHttpResponseHandler;
import com.onewayit.veki.Adapters.ServicesListAdapter;
import com.onewayit.veki.GetterSetter.ServiceData;
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

public class OngoingServices extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    ArrayList<ServiceData> servicesList;
    View view;
    ViewPager viewPager;
    TabLayout tabLayout;
    ListView lv_requests;
    RelativeLayout rl_loading, rl_popup;
    GifImageView gifnoresult;
    TextView tv_action, tv_feedback;
    private GlobalClass globalClass;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private PopupWindow mPopupWindow;

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
        view = inflater.inflate(R.layout.fragment_ongoing_services, container, false);
        findViewById();
        getMyServices();
        return view;
    }

    private void findViewById() {
        lv_requests = view.findViewById(R.id.lv_requests);
        rl_loading = view.findViewById(R.id.rl_loading);
        rl_popup = view.findViewById(R.id.rl_top);
        gifnoresult = view.findViewById(R.id.gifnoresult);
        tv_action = view.findViewById(R.id.tv_action);
        servicesList = new ArrayList<>();
    }


    private void getMyServices() {
        startProgress();
        final GlobalClass globalClass = new GlobalClass();
        globalClass.cancelProgressBarInterection(true, getActivity());
        RestClient restClient = new RestClient();
        final UserSessionPreferences sessionPreferences = new UserSessionPreferences(getActivity());
        final String relativeUrl = "requests/receive?user_id=" + sessionPreferences.getUserID();
        ByteArrayEntity entity = null;
        //Log.e("password is=",encrptPass);
        List<Header> headers = new ArrayList<Header>();
        headers.add(new BasicHeader("Authorization", sessionPreferences.getToken()));
        Log.e("Token and url", relativeUrl + " " + headers);
        restClient.getWithheader(getActivity(), relativeUrl, headers.toArray(new Header[headers.size()]), null, new BaseJsonHttpResponseHandler<JSONArray>() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, JSONArray response) {
                globalClass.cancelProgressBarInterection(false, getActivity());
                try {
                    JSONObject object = new JSONObject(rawJsonResponse);
                    if (object.getString("status").equalsIgnoreCase("Success")) {
                        JSONArray requests_data = object.getJSONArray("data");
                        if (requests_data.length() == 0) {
                            closeProgressNoResult();
                        } else if (requests_data.length() > 0) {
                            closeProgress();
                            for (int i = 0; i < requests_data.length(); i++) {
                                ServiceData servicesData = new ServiceData();
                                JSONObject request = new JSONObject(String.valueOf(requests_data.get(i)));
                                if (request.getString("status").equalsIgnoreCase("1")) { //ongoing services
                                    JSONObject serviceData = new JSONObject(request.getString("service"));
                                    JSONObject userData = new JSONObject(request.getString("user"));
                                    servicesData.setService_name(serviceData.getString("name"));
                                    servicesData.setService_price(request.getString("amount"));
                                    servicesData.setNotes(request.getString("notes"));
                                    servicesData.setName(userData.getString("name"));
                                    servicesData.setPhoto(userData.getString("photo"));
                                    servicesData.setPhone(userData.getString("phone_code") + userData.getString("phone"));
                                    servicesData.setDistance(String.format("%.2f", globalClass.distance(Double.parseDouble(sessionPreferences.getLat()), Double.parseDouble(sessionPreferences.getLon()), Double.parseDouble(request.getString("latitude")), Double.parseDouble(request.getString("longitude")))) + " Km");
                                    servicesData.setTimeAgo(String.valueOf(globalClass.getTimeDifference(request.getString("updated_at"))) + "");
                                    ArrayList<String> images = new ArrayList<>();
                                    if (request.has("images")) {
                                        JSONArray imagesArray = request.getJSONArray("images");

                                        for (int j = 0; j < imagesArray.length(); j++) {
                                            images.add(new JSONObject(String.valueOf(imagesArray.get(j))).getString("image"));
                                        }
                                    }
                                    servicesData.setImages(images);
                                    servicesList.add(servicesData);
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

    private void setAdapter() {
        if (servicesList.size() > 0) {
            ServicesListAdapter requestsListAdapter = new ServicesListAdapter(getActivity(), servicesList, getFragmentManager());
            lv_requests.setAdapter(requestsListAdapter);
        } else {
            closeProgressNoResult();
        }
    }

    public void startProgress() {
        lv_requests.setVisibility(View.GONE);
        rl_loading.setVisibility(View.VISIBLE);
        tv_action.setText("Loading ongoing services..");
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
}
