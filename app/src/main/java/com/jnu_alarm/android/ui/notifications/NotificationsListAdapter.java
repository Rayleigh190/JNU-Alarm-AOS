package com.jnu_alarm.android.ui.notifications;

import android.content.Context;
import android.util.Log;
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
    private OnItemClickListener onItemClickListener; // 클릭 리스너 인터페이스

    // 인터페이스 정의
    // 인터페이스 정의: 클릭 이벤트 리스너
    public interface OnItemClickListener {
        void onItemClick(NotificationData data);
    }

    // 클릭 리스너 설정 메서드
    public void setOnItemClickListener(OnItemClickListener listener) {
        onItemClickListener = listener;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView subTitleTextView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.textTitle);
            subTitleTextView = itemView.findViewById(R.id.textSubTitle);

            // 아이템 클릭 리스너 설정
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && onItemClickListener != null) {
                        onItemClickListener.onItemClick(mData.get(position));
                    }
                }
            });
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
