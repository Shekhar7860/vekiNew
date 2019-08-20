package com.onewayit.veki.utilities;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import okhttp3.ResponseBody;

public class GlobalClass {

    private int exit = 0;

    public void exitApp(AppCompatActivity activity) {
        exit = exit + 1;
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                exit = 0;
            }
        }, 2000);
        if (exit == 2) {
            activity.finish();
        } else {
            Toast.makeText(activity, "Press again! to exit", Toast.LENGTH_SHORT).show();
        }
    }

    //////////////Disable Screen Interection/////////////////////
    public void cancelProgressBarInterection(boolean cancellable, Activity activity) {
        if (activity != null) {
            if (cancellable) {
                activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            } else {
                activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        }
    }

    public String getJsonString(Object object) {
        Gson gson = new GsonBuilder().setLenient().create();
        return gson.toJson(object);
    }

    ////////////Get String value from retrofit error body////////////
    public String getErrorResponseBody(ResponseBody errorBody) {
        BufferedReader reader = null;
        StringBuilder sb = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(errorBody.byteStream()));
            String line;

            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.e("error body", "" + sb.toString());
        return sb.toString();
    }

    ///retrofit network error handling and message printing/////////////////
    public void retrofitNetworkErrorHandler(int errorCode, View view, Context mContext) {
        try {
            switch (errorCode) {
                case 204:
                    Snackbar.make(view, "No Content", Snackbar.LENGTH_LONG).show();
                    break;
                case 400:
                    Snackbar.make(view, "Bad Request", Snackbar.LENGTH_LONG).show();
                    break;
                case 401:
                    Snackbar.make(view, "Unauthorized", Snackbar.LENGTH_LONG).show();
                    break;
                case 402:
                    Snackbar.make(view, "Payment Required", Snackbar.LENGTH_LONG).show();
                    break;
                case 403:
//                    alertForSessionExpired(mContext);
                    Snackbar.make(view, "Session Expired Please Login Again", Snackbar.LENGTH_LONG).show();
                    break;
                case 404:
                    Snackbar.make(view, "Not Found", Snackbar.LENGTH_LONG).show();
                    break;
                case 405:
                    Snackbar.make(view, "Method Not Allowed", Snackbar.LENGTH_LONG).show();
                    break;
                case 406:
                    Snackbar.make(view, "Not Acceptable", Snackbar.LENGTH_LONG).show();
                    break;
                case 407:
                    Snackbar.make(view, "Proxy Authentication Required", Snackbar.LENGTH_LONG).show();
                    break;
                case 408:
                    Snackbar.make(view, "Request Timeout", Snackbar.LENGTH_LONG).show();
                    break;
                case 409:
                    Snackbar.make(view, "Conflict", Snackbar.LENGTH_LONG).show();
                    break;
                case 410:
                    Snackbar.make(view, "Gone", Snackbar.LENGTH_LONG).show();
                    break;
                case 429:
                    Snackbar.make(view, "Too Many Requests", Snackbar.LENGTH_LONG).show();
                    break;
                case 451:
                    Snackbar.make(view, "Unavailable For Legal Reasons ", Snackbar.LENGTH_LONG).show();
                    break;
                case 500:
                    Snackbar.make(view, "Internal Server Error", Snackbar.LENGTH_LONG).show();
                    break;
                case 501:
                    Snackbar.make(view, "Not Implemented", Snackbar.LENGTH_LONG).show();
                    break;
                case 502:
                    Snackbar.make(view, "Bad Gateway", Snackbar.LENGTH_LONG).show();
                    break;
                case 503:
                    Snackbar.make(view, "Service Unavailable", Snackbar.LENGTH_LONG).show();
                    break;
                case 504:
                    Snackbar.make(view, "Gateway Timeout", Snackbar.LENGTH_LONG).show();
                    break;
                case 511:
                    Snackbar.make(view, "Network Authentication Required", Snackbar.LENGTH_LONG).show();
                    break;

            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

    }

    public void closeKeyBoard(Activity activity) {
        View view = activity.getCurrentFocus();    //for fragment
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    public String getTimeDifference(String dateStop) {
        String timeDiff = "";
        //HH converts hour in 24 hours format (0-23), day calculation
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Date d1 = null;
        Date d2 = null;

        try {
            d1 = format.parse(dateStop);
            d2 = Calendar.getInstance().getTime();
            //in milliseconds
            long diff = d2.getTime() - d1.getTime();
            long diffSeconds = diff / 1000 % 60;
            long diffMinutes = diff / (60 * 1000) % 60;
            long diffHours = diff / (60 * 60 * 1000) % 24;
            long diffDays = diff / (24 * 60 * 60 * 1000);
            timeDiff = diffDays + "d " + diffHours + "h " + diffMinutes + "m";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return timeDiff;
    }
}
