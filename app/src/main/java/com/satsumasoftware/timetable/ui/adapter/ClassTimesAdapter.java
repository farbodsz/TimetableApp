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
import com.satsumasoftware.timetable.framework.ClassTimeGroup;
import com.satsumasoftware.timetable.framework.Timetable;
import com.satsumasoftware.timetable.util.TextUtilsKt;

import java.util.ArrayList;

public class ClassTimesAdapter extends RecyclerView.Adapter<ClassTimesAdapter.ClassTimesViewHolder> {

    private Activity mActivity;
    private ArrayList<ClassTimeGroup> mClassTimeGroups;

    public ClassTimesAdapter(Activity activity, ArrayList<ClassTimeGroup> classTimeGroups) {
        mActivity = activity;
        mClassTimeGroups = classTimeGroups;
    }

    @Override
    public ClassTimesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_class_time, parent, false);
        return new ClassTimesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ClassTimesViewHolder holder, int position) {
        ClassTimeGroup group = mClassTimeGroups.get(position);

        String timeText = group.getStartTime().toString() + " - " + group.getEndTime().toString();
        holder.mTime.setText(timeText);

        Timetable timetable = ((TimetableApplication) mActivity.getApplication()).getCurrentTimetable();
        assert timetable != null;

        StringBuilder dayTextBuilder = new StringBuilder();

        ArrayList<ClassTime> classTimes = group.getClassTimes();
        for (int i = 0; i < classTimes.size(); i++) {
            ClassTime classTime = classTimes.get(i);

            dayTextBuilder.append(TextUtilsKt.title(classTime.getDay().toString().toLowerCase()));

            if (!timetable.hasFixedScheduling()) {
                String weekItem = classTime.getWeekText(mActivity);
                dayTextBuilder.append(" ").append(weekItem);
            }

            if (i != classTimes.size() - 1) dayTextBuilder.append("\n");
        }

        String dayText = dayTextBuilder.toString();
        holder.mDay.setText(dayText);
    }

    @Override
    public int getItemCount() {
        return mClassTimeGroups.size();
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
