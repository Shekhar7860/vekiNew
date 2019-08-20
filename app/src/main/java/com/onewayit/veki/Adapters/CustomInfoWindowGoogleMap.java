package com.onewayit.veki.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.onewayit.veki.GetterSetter.ServiceProviderData;
import com.onewayit.veki.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class CustomInfoWindowGoogleMap implements GoogleMap.InfoWindowAdapter {

    TextView tv_hire, name, distance, rating, mobile_number, tv_price;
    ImageView profile_image;
    FragmentManager fragmentManager;
    ArrayList<ServiceProviderData> items;
    private Context context;
    private AppCompatActivity activity;

    public CustomInfoWindowGoogleMap(Context ctx, FragmentManager fragmentManager, ArrayList<ServiceProviderData> items) {
        context = ctx;
        this.items = items;
        this.fragmentManager = fragmentManager;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        View view = ((Activity) context).getLayoutInflater()
                .inflate(R.layout.marker_short_desc, null);
        view.setLayoutParams(new LinearLayout.LayoutParams(550, 450));
        tv_hire = view.findViewById(R.id.tv_hire);
        name = view.findViewById(R.id.name);
        distance = view.findViewById(R.id.distance);
        rating = view.findViewById(R.id.rating);
        mobile_number = view.findViewById(R.id.mobile_number);
        profile_image = view.findViewById(R.id.profile_image);
        tv_price = view.findViewById(R.id.tv_price);

        name.setText(items.get((Integer) marker.getTag()).getName());
        if (items.get((Integer) marker.getTag()).getReviews() != "" && items.get((Integer) marker.getTag()).getReviews() != null) {
            rating.setText(items.get((Integer) marker.getTag()).getReviews());
        }
        mobile_number.setText(items.get((Integer) marker.getTag()).getPhone());
        tv_price.setText(items.get((Integer) marker.getTag()).getPrice());
        if (items.get((Integer) marker.getTag()).getDistance() != "" && items.get((Integer) marker.getTag()).getDistance() != null) {
            distance.setText(items.get((Integer) marker.getTag()).getDistance());
        }
        Picasso.get().load(items.get((Integer) marker.getTag()).getImage()).into(profile_image);
        return view;
    }
}