package com.satsumasoftware.timetable.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.satsumasoftware.timetable.R;
import com.satsumasoftware.timetable.db.util.ClassUtils;
import com.satsumasoftware.timetable.db.util.SubjectUtils;
import com.satsumasoftware.timetable.framework.Class;
import com.satsumasoftware.timetable.framework.ClassDetail;
import com.satsumasoftware.timetable.framework.ClassTime;
import com.satsumasoftware.timetable.framework.Color;
import com.satsumasoftware.timetable.framework.Subject;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeClassesAdapter extends RecyclerView.Adapter<HomeClassesAdapter.ClassesViewHolder> {

    private Context mContext;
    private ArrayList<ClassTime> mClassTimes;

    public HomeClassesAdapter(Context context, ArrayList<ClassTime> classTimes) {
        mContext = context;
        mClassTimes = classTimes;
    }

    @Override
    public ClassesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_card_detail, parent, false);
        return new ClassesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ClassesViewHolder holder, int position) {
        ClassTime classTime = mClassTimes.get(position);
        ClassDetail classDetail = ClassUtils.getClassDetailWithId(mContext, classTime.getClassDetailId());
        Class cls = ClassUtils.getClassWithId(mContext, classDetail.getClassId());
        assert cls != null;

        Subject subject = SubjectUtils.getSubjectWithId(mContext, cls.getSubjectId());
        assert subject != null;
        holder.mSubject.setText(subject.getName());

        Color color = new Color(subject.getColorId());
        holder.mColorCircle.setImageResource(color.getPrimaryColorResId(mContext));

        StringBuilder detailBuilder = new StringBuilder();

        detailBuilder.append(classTime.getStartTime().toString())
                .append(" - ")
                .append(classTime.getEndTime().toString());

        if (classDetail.hasRoom() || classDetail.hasBuilding()) {
            detailBuilder.append(" \u2022 ");

            if (classDetail.hasRoom()) {
                detailBuilder.append(classDetail.getRoom());
                if (classDetail.hasBuilding()) detailBuilder.append(", ");
            }
            if (classDetail.hasBuilding()) {
                detailBuilder.append(classDetail.getBuilding());
            }
        }

        holder.mDetails.setText(detailBuilder.toString());
    }

    @Override
    public int getItemCount() {
        return mClassTimes.size();
    }

    public class ClassesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        CircleImageView mColorCircle;
        TextView mSubject, mDetails;

        ClassesViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mColorCircle = (CircleImageView) itemView.findViewById(R.id.imageView);
            mSubject = (TextView) itemView.findViewById(R.id.text1);
            mDetails = (TextView) itemView.findViewById(R.id.text2);
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
