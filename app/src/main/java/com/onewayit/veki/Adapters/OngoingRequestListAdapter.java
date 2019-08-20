package com.onewayit.veki.Adapters;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.onewayit.veki.GetterSetter.RequestsData;
import com.onewayit.veki.R;
import com.onewayit.veki.fragment.RequestDetails;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class OngoingRequestListAdapter extends BaseAdapter {

    final FragmentManager fragmentManager;
    Context mContext;
    Activity activity;
    ArrayList<RequestsData> ItemArrayList;

    public OngoingRequestListAdapter(Activity activity, ArrayList<RequestsData> itemArrayListData, FragmentManager fragmentManager) {
        mContext = activity;
        this.fragmentManager = fragmentManager;
        this.activity = activity;
        ItemArrayList = itemArrayListData;

    }

    @Override
    public int getCount() {
        return ItemArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.my_request, parent, false);
            ImageView imageView = convertView.findViewById(R.id.profile_image);
            TextView name = convertView.findViewById(R.id.name);
            TextView time_ago = convertView.findViewById(R.id.time_ago);
            TextView acci_type = convertView.findViewById(R.id.acci_type);
            TextView mobile_number = convertView.findViewById(R.id.mobile_number);
            TextView distance = convertView.findViewById(R.id.distance);
            TextView rating = convertView.findViewById(R.id.rating);
            imageView.setImageDrawable(activity.getResources().getDrawable(R.drawable.img_placehol));
            name.setText(ItemArrayList.get(position).getName());
            time_ago.setText(ItemArrayList.get(position).getTimeAgo());
            acci_type.setText(ItemArrayList.get(position).getServiceName());
            distance.setText(ItemArrayList.get(position).getDistance());
            mobile_number.setText(ItemArrayList.get(position).getPhone_code() + " " + ItemArrayList.get(position).getPhone());
            for (int i = 0; i < ItemArrayList.get(position).getImages().size(); i++) {
                if (i == 0) {
                    Picasso.get().load(ItemArrayList.get(position).getImages().get(i)).into(imageView);
                }
            }
            rating.setText("0.0 (0 reviews)");
        } else {

        }
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                RequestDetails fragment2 = new RequestDetails();
                Bundle bundle = new Bundle();
                Log.e("array", ItemArrayList.get(position).getId());
                bundle.putString("source", "myrequest");
                bundle.putString("id", ItemArrayList.get(position).getId());
                bundle.putString("name", ItemArrayList.get(position).getName());
                bundle.putString("lat", ItemArrayList.get(position).getLatitude());
                bundle.putString("lon", ItemArrayList.get(position).getLongitude());
                bundle.putString("accepted_user_id", ItemArrayList.get(position).getAccepted_user_id());
                bundle.putString("distance", ItemArrayList.get(position).getDistance());
                bundle.putString("phone_code", ItemArrayList.get(position).getPhone_code());
                bundle.putString("phone", ItemArrayList.get(position).getPhone());
                bundle.putString("photo", ItemArrayList.get(position).getPhoto());
                bundle.putString("timeAgo", ItemArrayList.get(position).getTimeAgo());
                bundle.putString("notes", ItemArrayList.get(position).getNotes());
                bundle.putString("servicetype", ItemArrayList.get(position).getServiceName());
                fragment2.setArguments(bundle);
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.replace(R.id.frame_layout, fragment2);
                fragmentTransaction.commit();
            }
        });
        return convertView;
    }
}