package com.onewayit.veki.Adapters;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.onewayit.veki.GetterSetter.NotificationData;
import com.onewayit.veki.R;
import com.onewayit.veki.activities.RequestDetailsActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

// import com.squareup.picasso.Picasso;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    private ArrayList<NotificationData> notifications;
    private Context context;


    public NotificationAdapter(Context context, ArrayList<NotificationData> notifications) {
        this.context = context;
        this.notifications = notifications;
    }

    @Override
    public NotificationAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, final int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.notifications_row_layout, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int i) {

        viewHolder.tv_android.setText(notifications.get(i).getDescription());
        if (notifications.get(i).getImage() != "null" && notifications.get(i).getImage() != "") {
            Picasso.get().load(notifications.get(i).getImage()).into(viewHolder.img_android);
        } else {
            viewHolder.img_android.setImageDrawable(context.getResources().getDrawable(R.drawable.img_placehol));
        }
        viewHolder.time.setText(notifications.get(i).getTime());
        viewHolder.cardview1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, RequestDetailsActivity.class);
                intent.putExtra("request_id", notifications.get(i).getService_request_id());
                intent.putExtra("title", notifications.get(i).getNotification_title());
                intent.putExtra("user_from_id", notifications.get(i).getUser_from_id());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_android, time;
        ImageView img_android;
        CardView cardview1;

        public ViewHolder(View view) {
            super(view);
            tv_android = view.findViewById(R.id.tv_android);
            img_android = view.findViewById(R.id.img_android);
            cardview1 = view.findViewById(R.id.cardview1);
            time = view.findViewById(R.id.time);

        }
    }


}