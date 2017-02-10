package com.satsumasoftware.timetable.ui.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.satsumasoftware.timetable.R;
import com.satsumasoftware.timetable.db.handler.ClassDetailHandler;
import com.satsumasoftware.timetable.framework.Class;
import com.satsumasoftware.timetable.framework.ClassDetail;
import com.satsumasoftware.timetable.framework.Color;
import com.satsumasoftware.timetable.framework.Subject;

import java.util.ArrayList;

public class ClassesAdapter extends RecyclerView.Adapter<ClassesAdapter.ClassesViewHolder> {

    private Context mContext;
    private ArrayList<Class> mClasses;

    public ClassesAdapter(Context context, ArrayList<Class> classes) {
        mContext = context;
        mClasses = classes;
    }

    @Override
    public ClassesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_classes, parent, false);
        return new ClassesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ClassesViewHolder holder, int position) {
        Class cls = mClasses.get(position);

        Subject subject = Subject.create(mContext, cls.getSubjectId());
        assert subject != null;

        holder.mSubject.setText(Class.makeName(cls, subject));

        Color color = new Color(subject.getColorId());
        holder.mColorView.setBackgroundColor(ContextCompat.getColor(
                mContext, color.getPrimaryColorResId(mContext)));

        ArrayList<ClassDetail> classDetails =
                ClassDetailHandler.getClassDetailsForClass(mContext, cls.getId());

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < classDetails.size(); i++) {
            ClassDetail classDetail = classDetails.get(i);

            if (classDetail.hasRoom()) {
                builder.append(classDetail.getRoom());
                if (classDetail.hasTeacher()) builder.append(" \u2022 ");
            }

            if (classDetail.hasTeacher()) {
                builder.append(classDetail.getTeacher());
            }

            if (i != classDetails.size() - 1) {
                builder.append("\n");
            }
        }
        holder.mClassInfo.setText(builder.toString());
    }

    @Override
    public int getItemCount() {
        return mClasses.size();
    }

    public class ClassesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        View mColorView;
        TextView mSubject, mClassInfo;

        ClassesViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mColorView = itemView.findViewById(R.id.color);
            mSubject = (TextView) itemView.findViewById(R.id.subject);
            mClassInfo = (TextView) itemView.findViewById(R.id.class_details);
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
