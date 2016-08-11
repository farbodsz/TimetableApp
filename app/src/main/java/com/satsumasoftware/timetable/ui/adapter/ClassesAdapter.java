package com.satsumasoftware.timetable.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.satsumasoftware.timetable.R;
import com.satsumasoftware.timetable.db.DatabaseUtils;
import com.satsumasoftware.timetable.framework.Class;
import com.satsumasoftware.timetable.framework.ClassGroup;
import com.satsumasoftware.timetable.framework.Subject;

import java.util.ArrayList;

public class ClassesAdapter extends RecyclerView.Adapter<ClassesAdapter.ClassesViewHolder> {

    private Context mContext;
    private ArrayList<ClassGroup> mClassGroups;

    public ClassesAdapter(Context context, ArrayList<ClassGroup> classGroups) {
        mContext = context;
        mClassGroups = classGroups;
    }

    @Override
    public ClassesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_classes, parent, false);
        return new ClassesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ClassesViewHolder holder, int position) {
        ClassGroup classGroup = mClassGroups.get(position);

        Subject subject = DatabaseUtils.getSubjectFromId(mContext, classGroup.getSubjectId());
        holder.mSubject.setText(subject.getName());

        StringBuilder builder = new StringBuilder();
        ArrayList<Class> classes = classGroup.getClasses();
        for (int i = 0; i < classes.size(); i++) {
            Class cls = classes.get(i);
            builder.append(cls.getStartTime().toString())
                    .append(" - ")
                    .append(cls.getEndTime().toString())
                    .append(" ")
                    .append(cls.getDay().toString());
            if (i != classes.size() - 1) {
                builder.append("\n");
            }
        }
        holder.mClassInfo.setText(builder.toString());
    }

    @Override
    public int getItemCount() {
        return mClassGroups.size();
    }

    public class ClassesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView mSubject, mClassInfo;

        ClassesViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
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
