package com.onewayit.veki.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.onewayit.veki.Adapters.RequestsTabAdapter;
import com.onewayit.veki.R;
import com.onewayit.veki.activities.MapsActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MyRequests extends Fragment implements View.OnClickListener {

    RelativeLayout rl_loading;
    View view;
    ImageView back_button_home_activity;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    Context context;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_my_requests, container, false);
        context = getActivity();
        findViewById();
        setOnClickListener();
        setupViewPager(viewPager);
        return view;
    }

    private void setOnClickListener() {
        back_button_home_activity.setOnClickListener(this);

    }

    private void findViewById() {
        viewPager = view.findViewById(R.id.viewpager);
        tabLayout = view.findViewById(R.id.tabs);
        back_button_home_activity = view.findViewById(R.id.back_button_home_activity);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.back_button_home_activity) {
            Intent intent = new Intent(getActivity(), MapsActivity.class);
            startActivity(intent);
            Objects.requireNonNull(getActivity()).finish();
        }
    }

    private void setupViewPager(final ViewPager viewPager) {
        tabLayout.addTab(tabLayout.newTab().setText("Ongoing"));
        tabLayout.addTab(tabLayout.newTab().setText("Completed"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        RequestsTabAdapter tabAdapter = new RequestsTabAdapter(Objects.requireNonNull(getActivity()).getSupportFragmentManager(), 2);
        viewPager.setAdapter(tabAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Objects.requireNonNull(getView()).setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        Intent intent = new Intent(getContext(), MapsActivity.class);
                        startActivity(intent);
                        Objects.requireNonNull(getActivity()).finish();
                        return true;
                    }
                }
                return false;
            }
        });
    }


}
