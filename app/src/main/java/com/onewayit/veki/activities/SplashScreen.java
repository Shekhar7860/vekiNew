package com.onewayit.veki.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.onewayit.veki.R;

public class SplashScreen extends AppCompatActivity {
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        initializeVariables();
        callDelay();
    }

    private void initializeVariables() {
        context = SplashScreen.this;
    }

    private void goToNextActivity() {
        Log.e("Extras", getIntent().getExtras().toString());
        if (getIntent().hasExtra("request_id") || getIntent().hasExtra("notification")) {
            Intent intent = new Intent(context, RequestDetailsActivity.class);
            intent.putExtra("data", getIntent().getExtras());
            intent.putExtra("push", "yes");
            intent.putExtra("status", "background");
            if (getIntent().hasExtra("")) {
                intent.putExtra("request_id", getIntent().getExtras().getString("request_id"));
            }
            startActivity(intent);
            finish();
        } else {
            Intent intent = new Intent(context, MapsActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void callDelay() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                goToNextActivity();
            }
        }, 2000);
    }


}
