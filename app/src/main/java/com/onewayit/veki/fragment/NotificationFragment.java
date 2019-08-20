package com.onewayit.veki.fragment;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.loopj.android.http.BaseJsonHttpResponseHandler;
import com.onewayit.veki.Adapters.NotificationAdapter;
import com.onewayit.veki.GetterSetter.NotificationData;
import com.onewayit.veki.Preferences.UserSessionPreferences;
import com.onewayit.veki.R;
import com.onewayit.veki.RestClient.RestClient;
import com.onewayit.veki.activities.MapsActivity;
import com.onewayit.veki.utilities.GlobalClass;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.message.BasicHeader;
import pl.droidsonroids.gif.GifImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class NotificationFragment extends Fragment implements View.OnClickListener {

    ImageView back_button_home_activity;
    ArrayList<NotificationData> notifications;
    RelativeLayout rl_loading;
    GifImageView gifnoresult;
    RecyclerView recyclerView;
    private View view;
    private Context context;
    private TextView submit;

    private GlobalClass globalClass;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_notification, container, false);
        context = getActivity();
        initializeVariables();
        getNotifications();
        return view;
    }

    private void setAdapter() {
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        NotificationAdapter adapter = new NotificationAdapter(context, notifications);
        recyclerView.setAdapter(adapter);
    }

    private void initializeVariables() {
        context = getActivity();
        globalClass = new GlobalClass();
        recyclerView = view.findViewById(R.id.card_recycler_view);
        back_button_home_activity = view.findViewById(R.id.back_button_home_activity);
        back_button_home_activity.setOnClickListener(this);
        notifications = new ArrayList<>();
        rl_loading = view.findViewById(R.id.rl_loading);
        gifnoresult = view.findViewById(R.id.gifnoresult);
        gifnoresult.setImageResource(R.drawable.no_result_found);
    }


    private void setOnClickListener() {
        submit.setOnClickListener(this);

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.submit: {
                Toast.makeText(getActivity(), "test working",
                        Toast.LENGTH_LONG).show();
                break;
            }
            case R.id.back_button_home_activity: {
                Intent intent = new Intent(getActivity(), MapsActivity.class);
                startActivity(intent);
                Objects.requireNonNull(getActivity()).finish();
                break;
            }
        }
    }

    private void getNotifications() {
        final GlobalClass globalClass = new GlobalClass();
        rl_loading.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        gifnoresult.setVisibility(View.GONE);
        globalClass.cancelProgressBarInterection(true, getActivity());
        RestClient restClient = new RestClient();
        final UserSessionPreferences sessionPreferences = new UserSessionPreferences(getActivity());
        final String relativeUrl = "notifications?user_id=" + sessionPreferences.getUserID();
        List<Header> headers = new ArrayList<Header>();
        headers.add(new BasicHeader("Authorization", sessionPreferences.getToken()));
        Log.e("Token and url", relativeUrl + " " + headers);
        restClient.getWithheader(getActivity(), relativeUrl, headers.toArray(new Header[headers.size()]), null, new BaseJsonHttpResponseHandler<JSONArray>() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, JSONArray response) {
                Log.e("response", rawJsonResponse);
                rl_loading.setVisibility(View.GONE);
                globalClass.cancelProgressBarInterection(false, getActivity());
                try {
                    JSONObject object = new JSONObject(rawJsonResponse);
                    if (object.getString("status").equalsIgnoreCase("Success")) {
                        JSONArray data = object.getJSONArray("data");
                        if (data.length() > 0) {
                            recyclerView.setVisibility(View.VISIBLE);
                            gifnoresult.setVisibility(View.GONE);
                            for (int i = 0; i < data.length(); i++) {
                                NotificationData notificationData = new NotificationData();
                                JSONObject notification = new JSONObject(String.valueOf(data.get(i)));
                                JSONObject requestData = new JSONObject(notification.getString("data"));
                                JSONObject user_from = notification.getJSONObject("user_from");
                                notificationData.setDescription(notification.getString("notification_message"));
                                notificationData.setNotification_title(notification.getString("notification_title"));
                                notificationData.setId(notification.getString("id"));
                                notificationData.setImage(user_from.getString("photo"));
                                notificationData.setUser_from_id(user_from.getString("id"));
                                notificationData.setName(user_from.getString("name"));
                                notificationData.setStatus(notification.getString("status"));
                                notificationData.setTime(globalClass.getTimeDifference(notification.getString("created_at")));
                                Log.e("notification " + i, notification.toString());
                                if (requestData.has("service_request_id")) {
                                    notificationData.setService_request_id(requestData.getString("service_request_id"));
                                } else if (requestData.has("id")) {
                                    notificationData.setService_request_id(requestData.getString("id"));
                                }
                                notifications.add(notificationData);
                            }
                            setAdapter();
                        } else {
                            gifnoresult.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, JSONArray errorResponse) {
                rl_loading.setVisibility(View.GONE);
                gifnoresult.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                Toast.makeText(getActivity(), "Error while fetching requests...", Toast.LENGTH_SHORT).show();
                globalClass.cancelProgressBarInterection(false, getActivity());
            }

            @Override
            protected JSONArray parseResponse(String rawJsonData, boolean isFailure) throws Throwable {
                return null;
            }
        });
    }
}
