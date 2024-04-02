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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class NotificationsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_ITEM = 1;

    private ArrayList<NotificationData> mData = null;
    private OnItemClickListener onItemClickListener; // 클릭 리스너 인터페이스

    // 인터페이스 정의
    public interface OnItemClickListener {
        void onItemClick(NotificationData data);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        onItemClickListener = listener;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView subTitleTextView;
        TextView dateTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.textTitle);
            subTitleTextView = itemView.findViewById(R.id.textSubTitle);
            dateTextView = itemView.findViewById(R.id.date_text);

            // 아이템 클릭 리스너 설정
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onItemClickListener != null) {
                    onItemClickListener.onItemClick(mData.get(position - 1)); // Subtract 1 for header
                }
            });
        }
    }

    class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView headerTextView;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            headerTextView = itemView.findViewById(R.id.header_text);
        }
    }

    NotificationsListAdapter(ArrayList<NotificationData> list) {
        mData = list;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        if (viewType == VIEW_TYPE_HEADER) {
            View headerView = inflater.inflate(R.layout.notification_header_item, parent, false);
            return new HeaderViewHolder(headerView);
        } else {
            View itemView = inflater.inflate(R.layout.notification_item, parent, false);
            return new ViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ViewHolder) {
            ViewHolder itemViewHolder = (ViewHolder) holder;
            String title = mData.get(position - 1).getTitle(); // Subtract 1 for header
            String subTitle = mData.get(position - 1).getBody();

            // Parse date string
            String dateString = mData.get(position - 1).getCreatedAt();
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());
            try {
                Date date = inputFormat.parse(dateString);
                String formattedDate = outputFormat.format(date);
                itemViewHolder.titleTextView.setText(title);
                itemViewHolder.subTitleTextView.setText(subTitle);
                itemViewHolder.dateTextView.setText(formattedDate);
            } catch (ParseException e) {
                e.printStackTrace();
                // Handle parsing exception
            }
        } else if (holder instanceof HeaderViewHolder) {
            HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
            headerViewHolder.headerTextView.setText("최대 20개의 알림 내역이 제공됩니다.");
        }
    }


    @Override
    public int getItemCount() {
        // Add 1 for the header view
        return mData.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? VIEW_TYPE_HEADER : VIEW_TYPE_ITEM;
    }
}
