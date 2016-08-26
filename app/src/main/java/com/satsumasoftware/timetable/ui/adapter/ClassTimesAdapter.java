package com.satsumasoftware.timetable.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.satsumasoftware.timetable.R;
import com.satsumasoftware.timetable.framework.ClassTime;

import java.util.ArrayList;

public class ClassTimesAdapter extends RecyclerView.Adapter<ClassTimesAdapter.ClassTimesViewHolder> {

    private ArrayList<ClassTime> mClassTimes;

    public ClassTimesAdapter(ArrayList<ClassTime> classTimes) {
        mClassTimes = classTimes;
    }

    @Override
    public ClassTimesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_class_time, parent, false);
        return new ClassTimesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ClassTimesViewHolder holder, int position) {
        ClassTime classTime = mClassTimes.get(position);

        String timeText = classTime.getStartTime().toString() + " - " +
                classTime.getEndTime().toString();
        holder.mTime.setText(timeText);

        String dayText = classTime.getDay().toString() + " " + classTime.getWeekNumber();
        holder.mDay.setText(dayText);
    }

    @Override
    public int getItemCount() {
        return mClassTimes.size();
    }

    public class ClassTimesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView mTime, mDay;

        ClassTimesViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mTime = (TextView) itemView.findViewById(R.id.time);
            mDay = (TextView) itemView.findViewById(R.id.day);
        }

        @Override
        public void onClick(View view) {
            if (mOnEntryClickListener != null) {
                mOnEntryClickListener.onEntryClick(view, getLayoutPosition());
            }
        }
    }

    private OnEntryClickListener mOnEntryClickListener;

    public interface OnEntryClickListener {
        void onEntryClick(View view, int position);
    }

    public void setOnEntryClickListener(OnEntryClickListener onEntryClickListener) {
        mOnEntryClickListener = onEntryClickListener;
    }

}
