package com.onewayit.veki.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.onewayit.veki.R;
import com.onewayit.veki.activities.MapsActivity;

import java.util.Objects;

public class MyProposals extends Fragment implements View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    RelativeLayout my_request_item;
    // TODO: Rename and change types of parameters
    View view;
    ImageView back_button_home_activity;
    CardView card1, card2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_my_proposals, container, false);
        findViewById();
        setOnClickListener();
        // Inflate the layout for this fragment
        return view;
    }

    private void setOnClickListener() {
        back_button_home_activity.setOnClickListener(this);
        card1.setOnClickListener(this);
        card2.setOnClickListener(this);
    }

    private void findViewById() {
        my_request_item = view.findViewById(R.id.my_request_item);
        back_button_home_activity = view.findViewById(R.id.back_button_home_activity);
        card1 = view.findViewById(R.id.card1);
        card2 = view.findViewById(R.id.card2);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.my_request_item: {
                ProposalDetails fragment2 = new ProposalDetails();
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.replace(R.id.frame_layout, fragment2);
                fragmentTransaction.commit();
                break;
            }
            case R.id.back_button_home_activity: {
                Intent intent = new Intent(getActivity(), MapsActivity.class);
                startActivity(intent);
                Objects.requireNonNull(getActivity()).finish();
                break;
            }
            case R.id.card1: {
                ProposalDetails fragment2 = new ProposalDetails();
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.replace(R.id.frame_layout, fragment2);
                fragmentTransaction.commit();
                break;
            }
            case R.id.card2: {
                ProposalDetails fragment2 = new ProposalDetails();
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.replace(R.id.frame_layout, fragment2);
                fragmentTransaction.commit();
                break;
            }
        }
    }

}
