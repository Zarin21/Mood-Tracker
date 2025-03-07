package com.example.unemployedavengers.arrayadapters;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    // 定义点击事件接口，用于在点击列表项时传递当前 Event 对象
    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    // 存放要显示的 Event 数据列表
    private List<Event> eventList = new ArrayList<>();

    // 外部传入的点击监听器
    private final OnEventClickListener listener;

    // 构造方法，要求传入 OnEventClickListener 实例
    public EventAdapter(OnEventClickListener listener) {
        this.listener = listener;
    }

    // 设置数据并刷新列表
    public void setEvents(List<Event> events) {
        this.eventList = events;
        notifyDataSetChanged();
    }

    // 创建 ViewHolder：加载列表项布局文件 item_event.xml
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    // 将数据绑定到 ViewHolder
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);
        if (event != null) {
            holder.bind(event, listener);
        }
    }

    // 返回列表项的总数
    @Override
    public int getItemCount() {
        return eventList.size();
    }

    // 内部 ViewHolder 类，用于管理每个列表项视图及数据绑定
    static class EventViewHolder extends RecyclerView.ViewHolder {
        // 用于显示“emoji + emoji文字描述 + 时间”
        private final TextView textViewCombined;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewCombined = itemView.findViewById(R.id.textViewEventCombined);
        }

        // 绑定数据到列表项视图，并设置点击事件
        public void bind(Event event, OnEventClickListener listener) {

            String emoji = event.getMoodEmoji();         // eg "🙂"
            String moodText = event.getMoodDescription();  // eg "happy"
            String time = event.getTime();                 // eg "2025/3/2
            String fullText = emoji + " " + moodText + " " + time;
            SpannableString spannableString = new SpannableString(fullText);

            // count moodText place at fullText
            int start = emoji.length() + 1;
            int end = start + moodText.length();

            // set moodText color
            spannableString.setSpan(new ForegroundColorSpan(Color.BLUE),
                    start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            // TextView
            textViewCombined.setText(spannableString);
            // click event
            itemView.setOnClickListener(v -> listener.onEventClick(event));
        }
    }

    // ge event data
    public static List<Event> getAllEvents(Context context) {
        List<Event> events = new ArrayList<>();
        EventDatabase dbHelper = new EventDatabase(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                EventDatabase.COLUMN_ID,
                EventDatabase.COLUMN_MOOD_EMOJI,
                EventDatabase.COLUMN_MOOD_DESCRIPTION,
                EventDatabase.COLUMN_TRIGGER,
                EventDatabase.COLUMN_TIME,
                EventDatabase.COLUMN_SITUATION
        };

        Cursor cursor = db.query(
                EventDatabase.TABLE_EVENTS,
                projection,
                null,
                null,
                null,
                null,
                null
        );

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(EventDatabase.COLUMN_ID));
            String moodEmoji = cursor.getString(cursor.getColumnIndexOrThrow(EventDatabase.COLUMN_MOOD_EMOJI));
            String moodDescription = cursor.getString(cursor.getColumnIndexOrThrow(EventDatabase.COLUMN_MOOD_DESCRIPTION));
            String trigger = cursor.getString(cursor.getColumnIndexOrThrow(EventDatabase.COLUMN_TRIGGER));
            String time = cursor.getString(cursor.getColumnIndexOrThrow(EventDatabase.COLUMN_TIME));
            String situation = cursor.getString(cursor.getColumnIndexOrThrow(EventDatabase.COLUMN_SITUATION));
            events.add(new Event(id, moodEmoji, moodDescription, trigger, time, situation));
        }
        cursor.close();
        db.close();
        return events;
    }
}

