package com.onewayit.veki.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;

import com.onewayit.veki.GetterSetter.ServiceData;
import com.onewayit.veki.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ServicesListAdapter extends BaseAdapter {
    final FragmentManager fragmentManager;
    Context mContext;
    Activity activity;
    ArrayList<ServiceData> ItemArrayList;

    public ServicesListAdapter(Activity activity, ArrayList<ServiceData> itemArrayListData, FragmentManager fragmentManager) {
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
            convertView = inflater.inflate(R.layout.my_services, parent, false);
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
            acci_type.setText(ItemArrayList.get(position).getService_name());
            distance.setText(ItemArrayList.get(position).getDistance());
            mobile_number.setText(ItemArrayList.get(position).getPhone());
            if (ItemArrayList.get(position).getPhoto() != "null") {
                Picasso.get().load(ItemArrayList.get(position).getPhoto()).into(imageView);
            }
            rating.setText("0.0 (0 reviews)");
        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        return convertView;
    }
}
