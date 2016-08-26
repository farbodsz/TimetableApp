package com.satsumasoftware.timetable.ui.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.satsumasoftware.timetable.R;
import com.satsumasoftware.timetable.TimetableApplication;
import com.satsumasoftware.timetable.framework.ClassTime;
import com.satsumasoftware.timetable.framework.Timetable;

import java.util.ArrayList;

public class ClassTimesAdapter extends RecyclerView.Adapter<ClassTimesAdapter.ClassTimesViewHolder> {

    private Activity mActivity;
    private ArrayList<ClassTime> mClassTimes;

    public ClassTimesAdapter(Activity activity, ArrayList<ClassTime> classTimes) {
        mActivity = activity;
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

        Timetable timetable = ((TimetableApplication) mActivity.getApplication()).getCurrentTimetable();
        assert timetable != null;

        StringBuilder dayTextBuilder = new StringBuilder();
        dayTextBuilder.append(classTime.getDay().toString());
        if (!timetable.hasFixedScheduling()) {
            dayTextBuilder.append(" ")
                    .append(classTime.getWeekNumber());
        }
        String dayText = dayTextBuilder.toString();
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
