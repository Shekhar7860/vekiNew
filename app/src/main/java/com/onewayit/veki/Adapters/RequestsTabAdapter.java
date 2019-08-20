package com.onewayit.veki.Adapters;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.onewayit.veki.fragment.CompletedRequests;
import com.onewayit.veki.fragment.OngoingRequests;

public class RequestsTabAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;

    public RequestsTabAdapter(FragmentManager fm, int NoofTabs) {
        super(fm);
        this.mNumOfTabs = NoofTabs;
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                OngoingRequests home = new OngoingRequests();
                return home;
            case 1:
                CompletedRequests about = new CompletedRequests();
                return about;
            default:
                return null;
        }
    }
}
