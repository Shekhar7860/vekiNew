package com.onewayit.veki.fragment;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.onewayit.veki.R;
import com.onewayit.veki.activities.LoginActivity;
import com.onewayit.veki.utilities.Network;

import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class RoleFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private View view;
    private Context context;
    private TextView submit;
    private CheckBox service_provider, customer;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_role, container, false);
        context = getActivity();
        initializeVariables();
        findViewById();
        setOnClickListener();
        setOnCheckedChangeListener();

        return view;
    }

    private void initializeVariables() {
        ((LoginActivity) Objects.requireNonNull(getActivity())).setHeading("Role");
    }

    private void findViewById() {
        submit = view.findViewById(R.id.submit);
        service_provider = view.findViewById(R.id.service_provider);
        customer = view.findViewById(R.id.customer);
    }

    private void setOnClickListener() {
        submit.setOnClickListener(this);
    }

    private void setOnCheckedChangeListener() {
        service_provider.setOnCheckedChangeListener(this);
        customer.setOnCheckedChangeListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.submit:
                Network network = new Network(context);
                if (network.isConnectedToInternet()) {
                    if (validation()) {
                        submit();
                    }
                } else {
                    network.noInternetAlertBox(getActivity(), false);
                }

                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()) {
            case R.id.service_provider:
                if (b) {
                    customer.setChecked(false);
                }
                break;
            case R.id.customer:
                if (b) {
                    service_provider.setChecked(false);
                }
                break;

        }
    }

    ///////////////Screen Validation, it will return true if user has fielded all required details/////////////
    private boolean validation() {
        if (!service_provider.isChecked() && !customer.isChecked()) {
            Snackbar.make(view, "Please Select Role", Snackbar.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    private void submit() {
        Bundle bundle = new Bundle();
        bundle.putString("mobile_number", "" + Objects.requireNonNull(getArguments()).getString("mobile_number"));
        ProfileFragment profileFragment = new ProfileFragment();
        profileFragment.setArguments(bundle);
        Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, profileFragment, "ProfileFragment").addToBackStack(null).commit();
    }

}
