package com.jnu_alarm.android.ui.notifications;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jnu_alarm.android.R;
import com.jnu_alarm.android.data.NotificationData;

import java.util.ArrayList;

public class NotificationsListAdapter extends RecyclerView.Adapter<NotificationsListAdapter.ViewHolder> {
    private ArrayList<NotificationData> mData = null;
    class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView subTitleTextView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.textTitle);
            subTitleTextView = itemView.findViewById(R.id.textSubTitle);
        }
    }

    NotificationsListAdapter(ArrayList list) {
        mData = list;
    }

    @NonNull
    @Override
    public NotificationsListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext() ;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ;

        View view = inflater.inflate(R.layout.notification_item, parent, false) ;
        NotificationsListAdapter.ViewHolder vh = new NotificationsListAdapter.ViewHolder(view) ;

        return vh ;
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationsListAdapter.ViewHolder holder, int position) {
        String title = mData.get(position).getTitle();
        String subTitle = mData.get(position).getBody();
        holder.titleTextView.setText(title);
        holder.subTitleTextView.setText(subTitle);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }
}
