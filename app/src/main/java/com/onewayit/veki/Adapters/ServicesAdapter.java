package com.onewayit.veki.Adapters;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.onewayit.veki.GetterSetter.ServiceProviderData;
import com.onewayit.veki.R;
import com.onewayit.veki.fragment.ProposalDetails;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ServicesAdapter extends RecyclerView.Adapter<ServicesAdapter.MyViewHolder> {

    final FragmentManager fragmentManager;
    Context context;
    private ArrayList<ServiceProviderData> ItemList;


    public ServicesAdapter(ArrayList<ServiceProviderData> ItemList, FragmentManager fragmentManager) {
        this.ItemList = ItemList;
        this.fragmentManager = fragmentManager;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.my_proposal, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        holder.mobile_number.setText(ItemList.get(position).getPhone());
        holder.tv_price.setText(ItemList.get(position).getPrice());
        holder.tv_name.setText(ItemList.get(position).getName());
        holder.rating.setText(ItemList.get(position).getReviews());
        Picasso.get().load(ItemList.get(position).getImage()).into(holder.profile_image);
        holder.tv_viewDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("service_user_id", ItemList.get(position).getUser_id());
                bundle.putString("price", ItemList.get(position).getPrice());
                bundle.putString("lat", ItemList.get(position).getRequestLat());
                bundle.putString("lon", ItemList.get(position).getRequestLon());
                bundle.putString("request_id", ItemList.get(position).getRequest_id());
                ProposalDetails fragment2 = new ProposalDetails();
                fragment2.setArguments(bundle);
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.replace(R.id.frame_layout, fragment2);
                fragmentTransaction.commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return ItemList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tv_viewDetails, mobile_number, tv_price, rating, service, tv_name;
        ImageView profile_image;

        public MyViewHolder(View view) {
            super(view);
            tv_viewDetails = view.findViewById(R.id.tv_viewDetails);
            mobile_number = view.findViewById(R.id.mobile_number);
            tv_price = view.findViewById(R.id.tv_price);
            rating = view.findViewById(R.id.rating);
            service = view.findViewById(R.id.service);
            tv_name = view.findViewById(R.id.tv_name);
            profile_image = view.findViewById(R.id.profile_image);
        }
    }
}