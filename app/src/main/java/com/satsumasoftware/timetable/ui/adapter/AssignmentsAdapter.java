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
import com.satsumasoftware.timetable.framework.Assignment;
import com.satsumasoftware.timetable.framework.Class;
import com.satsumasoftware.timetable.framework.Color;
import com.satsumasoftware.timetable.framework.Subject;

import java.util.ArrayList;

public class AssignmentsAdapter extends RecyclerView.Adapter<AssignmentsAdapter.AssignmentViewHolder> {

    private Context mContext;
    private ArrayList<Assignment> mAssignments;

    public AssignmentsAdapter(Context context, ArrayList<Assignment> assignments) {
        mContext = context;
        mAssignments = assignments;
    }

    @Override
    public AssignmentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_assignment, parent, false);
        return new AssignmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AssignmentViewHolder holder, int position) {
        Assignment assignment = mAssignments.get(position);

        holder.mTitle.setText(assignment.getTitle());
        holder.mDetail.setText(assignment.getDetail());

        Class cls = ClassUtilsKt.getClassWithId(mContext, assignment.getClassId());
        assert cls != null;
        Subject subject = SubjectUtilsKt.getSubjectWithId(mContext, cls.getSubjectId());
        assert subject != null;

        Color color = new Color(subject.getColorId());
        holder.mColorView.setBackgroundColor(
                ContextCompat.getColor(mContext, color.getPrimaryColorResId(mContext)));
    }

    @Override
    public int getItemCount() {
        return mAssignments.size();
    }

    public class AssignmentViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        View mColorView;
        TextView mTitle, mDetail;

        AssignmentViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mColorView = itemView.findViewById(R.id.color);
            mTitle = (TextView) itemView.findViewById(R.id.title);
            mDetail = (TextView) itemView.findViewById(R.id.detail);
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
