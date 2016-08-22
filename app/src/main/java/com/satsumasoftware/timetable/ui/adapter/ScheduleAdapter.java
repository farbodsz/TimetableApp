package com.satsumasoftware.timetable.ui.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.satsumasoftware.timetable.R;
import com.satsumasoftware.timetable.db.util.ClassUtilsKt;
import com.satsumasoftware.timetable.db.util.SubjectUtilsKt;
import com.satsumasoftware.timetable.framework.Class;
import com.satsumasoftware.timetable.framework.ClassDetail;
import com.satsumasoftware.timetable.framework.ClassTime;
import com.satsumasoftware.timetable.framework.Color;
import com.satsumasoftware.timetable.framework.Subject;

import java.util.ArrayList;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder> {

    private Context mContext;
    private ArrayList<ClassTime> mClassTimes;

    public ScheduleAdapter(Context context, ArrayList<ClassTime> classTimes) {
        mContext = context;
        mClassTimes = classTimes;
    }

    @Override
    public ScheduleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_assignment, parent, false);
        return new ScheduleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ScheduleViewHolder holder, int position) {
        ClassTime classTime = mClassTimes.get(position);

        ClassDetail classDetail = ClassUtilsKt.getClassDetailWithId(mContext, classTime.getClassDetailId());
        Class cls = ClassUtilsKt.getClassWithId(mContext, classDetail.getClassId());
        assert cls != null;

        Subject subject = SubjectUtilsKt.getSubjectWithId(mContext, cls.getSubjectId());
        assert subject != null;

        Color color = new Color(subject.getColorId());
        holder.mColorView.setBackgroundColor(ContextCompat.getColor(
                mContext, color.getPrimaryColorResId(mContext)));

        holder.mSubject.setText(subject.getName());

        StringBuilder builder = new StringBuilder();
        if (classDetail.hasRoom()) {
            builder.append(classDetail.getRoom());
        }
        if (classDetail.hasBuilding()) {
            if (classDetail.hasRoom()) builder.append(", ");
            builder.append(classDetail.getBuilding());
        }
        if (classDetail.hasTeacher()) {
            if (classDetail.hasRoom() || classDetail.hasBuilding()) builder.append(" \u2022 ");
            builder.append(classDetail.getTeacher());
        }
        holder.mDetails.setText(builder.toString());

        holder.mTimes.setText(classTime.getStartTime().toString() + " - " +
                classTime.getEndTime().toString());
    }

    @Override
    public int getItemCount() {
        return mClassTimes.size();
    }

    public class ScheduleViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        View mColorView;
        TextView mSubject, mDetails, mTimes;

        ScheduleViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mColorView = itemView.findViewById(R.id.color);
            mSubject = (TextView) itemView.findViewById(R.id.title);
            mDetails = (TextView) itemView.findViewById(R.id.subject);
            mTimes = (TextView) itemView.findViewById(R.id.due_date);
            itemView.findViewById(R.id.completion_progress).setVisibility(View.GONE);
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
