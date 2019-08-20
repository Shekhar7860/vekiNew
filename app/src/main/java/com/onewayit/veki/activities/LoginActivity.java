package com.onewayit.veki.activities;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.onewayit.veki.R;
import com.onewayit.veki.fragment.LoginFragment;
import com.onewayit.veki.utilities.GlobalClass;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private GlobalClass globalClass;
    private LinearLayout header;
    private TextView heading;
    private ImageView back_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initializeVariable();
        findViewById();
        setOnClickListener();
        goToLoginFragment();


    }

    private void goToLoginFragment() {
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new LoginFragment(), "LoginFragment").addToBackStack(null).commit();
    }

    private void initializeVariable() {
        globalClass = new GlobalClass();
    }

    private void findViewById() {
        heading = findViewById(R.id.heading);
        back_button = findViewById(R.id.back_button);
        header = findViewById(R.id.header);
        header.setVisibility(View.GONE);
    }

    private void setOnClickListener() {
        back_button.setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        if ((getSupportFragmentManager().findFragmentByTag("LoginFragment") != null && getSupportFragmentManager().findFragmentByTag("LoginFragment").isVisible()) || (getSupportFragmentManager().findFragmentByTag("RoleFragment") != null && getSupportFragmentManager().findFragmentByTag("RoleFragment").isVisible())) {
            Intent intent = new Intent(this, MapsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } else {
            super.onBackPressed();
        }

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
        try {
            for (Fragment fragment : getSupportFragmentManager().getFragments()) {
                fragment.onActivityResult(requestCode, resultCode, data);
                Log.d("Activity", "ON RESULT CALLED");
            }
        } catch (Exception e) {
            Log.d("ERROR", e.toString());
        }
    }

    public void setHeading(String string) {
        heading.setText(string);
        if (string.isEmpty()) {
            header.setVisibility(View.GONE);
        } else {
            header.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back_button:
                onBackPressed();
                break;
        }
    }
}
