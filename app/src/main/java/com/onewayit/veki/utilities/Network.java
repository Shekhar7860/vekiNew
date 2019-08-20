package com.onewayit.veki.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.snackbar.Snackbar;
import com.onewayit.veki.R;

public class Network {


    private Context mContext;

    public Network(Context mContext) {
        this.mContext = mContext;
    }

    /////////////////Check Internet Connection////////////////
    public boolean isConnectedToInternet() {
        ConnectivityManager connectivity = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
        }
        return false;
    }

    /////////////////////It will show no Internet Alert Box, On Refresh it will refresh fragment//////////////
    public void noInternetAlertBox(final String fragmentTag, final FragmentManager fragmentManager, final AppCompatActivity activity) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.DialogTheme);
                LayoutInflater layoutInflater = LayoutInflater.from(mContext);
                final View dialogView = layoutInflater.inflate(R.layout.fragment_network, null);
                //find id from dialog view..
                builder.setView(dialogView);
                builder.setCancelable(false);
                final AlertDialog alertDialog = builder.create();
                ImageView refresh = dialogView.findViewById(R.id.refresh);
                refresh.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isConnectedToInternet()) {

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                fragmentManager.beginTransaction().detach(fragmentManager.findFragmentByTag(fragmentTag)).attach(fragmentManager.findFragmentByTag(fragmentTag)).setReorderingAllowed(false).commit();
                            } else {
                                fragmentManager.beginTransaction().detach(fragmentManager.findFragmentByTag(fragmentTag)).attach(fragmentManager.findFragmentByTag(fragmentTag)).commit();
                            }
                            alertDialog.dismiss();
                        } else {
//                          Toast.makeText(mContext,"No Internet Connection Found",Toast.LENGTH_SHORT).show();
                            try {
                                Snackbar.make(dialogView, "No Internet Connection Found", Snackbar.LENGTH_LONG).show();
                            } catch (IllegalArgumentException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                alertDialog.show();

            }
        });
    }

    /////////////////////It will Show no Internet Alert Box, On Refresh it can refresh activity or it will just dismiss//////////////
    public void noInternetAlertBox(final Activity activity, final boolean refreshActivity) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ImageView refresh;
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.DialogTheme);
                LayoutInflater layoutInflater = LayoutInflater.from(mContext);
                final View dialogView = layoutInflater.inflate(R.layout.fragment_network, null);
                //find id from dialog view..
                builder.setView(dialogView);
                builder.setCancelable(false);
                final AlertDialog alertDialog = builder.create();
                refresh = dialogView.findViewById(R.id.refresh);
                refresh.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isConnectedToInternet()) {
                            if (refreshActivity) {
                                Intent intent = activity.getIntent();
                                activity.finish();
                                activity.startActivity(intent);
                            }
                            alertDialog.dismiss();
                        } else {
                            try {
                                Snackbar.make(dialogView, "No Internet Connection Found", Snackbar.LENGTH_LONG).show();
                            } catch (IllegalArgumentException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                alertDialog.show();
            }
        });
    }

    public void showProgress(final Activity activity, final boolean refreshActivity) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ImageView refresh;
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.DialogTheme);
                LayoutInflater layoutInflater = LayoutInflater.from(mContext);
                final View dialogView = layoutInflater.inflate(R.layout.fragment_network, null);
                //find id from dialog view..
                builder.setView(dialogView);
                builder.setCancelable(false);
                final AlertDialog alertDialog = builder.create();
                refresh = dialogView.findViewById(R.id.refresh);
                refresh.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isConnectedToInternet()) {
                            if (refreshActivity) {
                                Intent intent = activity.getIntent();
                                activity.finish();
                                activity.startActivity(intent);
                            }
                            alertDialog.dismiss();
                        } else {
                            try {
                                Snackbar.make(dialogView, "No Internet Connection Found", Snackbar.LENGTH_LONG).show();
                            } catch (IllegalArgumentException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                alertDialog.show();
            }
        });
    }

}
