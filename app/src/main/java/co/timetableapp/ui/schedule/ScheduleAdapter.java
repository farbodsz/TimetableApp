package co.timetableapp.ui.schedule;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import co.timetableapp.R;
import co.timetableapp.data.handler.DataNotFoundException;
import co.timetableapp.model.Class;
import co.timetableapp.model.ClassDetail;
import co.timetableapp.model.ClassTime;
import co.timetableapp.model.Color;
import co.timetableapp.model.Subject;

class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder> {

    private Context mContext;
    private ArrayList<ClassTime> mClassTimes;

    ScheduleAdapter(Context context, ArrayList<ClassTime> classTimes) {
        mContext = context;
        mClassTimes = classTimes;
    }

    @Override
    public ScheduleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_general, parent, false);
        return new ScheduleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ScheduleViewHolder holder, int position) {
        ClassTime classTime = mClassTimes.get(position);

        ClassDetail classDetail = null;
        Class cls = null;
        try {
            classDetail = ClassDetail.create(mContext, classTime.getClassDetailId());
            cls = Class.create(mContext, classDetail.getClassId());
        } catch (DataNotFoundException e) {
            e.printStackTrace();
        }
        assert classDetail != null;
        assert cls != null;

        Subject subject = null;
        try {
            subject = Subject.create(mContext, cls.getSubjectId());
        } catch (DataNotFoundException e) {
            e.printStackTrace();
        }
        assert subject != null;

        Color color = new Color(subject.getColorId());
        holder.mColorView.setBackgroundColor(ContextCompat.getColor(
                mContext, color.getPrimaryColorResId(mContext)));

        holder.mSubject.setText(cls.makeName(subject));

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

    class ScheduleViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        View mColorView;
        TextView mSubject, mDetails, mTimes;

        ScheduleViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mColorView = itemView.findViewById(R.id.color);
            mSubject = (TextView) itemView.findViewById(R.id.text1);
            mDetails = (TextView) itemView.findViewById(R.id.text2);
            mTimes = (TextView) itemView.findViewById(R.id.text3);
            itemView.findViewById(R.id.text4).setVisibility(View.GONE);
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
