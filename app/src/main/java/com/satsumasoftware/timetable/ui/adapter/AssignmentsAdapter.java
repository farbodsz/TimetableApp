package com.satsumasoftware.timetable.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.satsumasoftware.timetable.R;
import com.satsumasoftware.timetable.framework.Assignment;

import java.util.ArrayList;

public class AssignmentsAdapter extends RecyclerView.Adapter<AssignmentsAdapter.AssignmentViewHolder> {

    private ArrayList<Assignment> mAssignments;

    public AssignmentsAdapter(ArrayList<Assignment> assignments) {
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
    }

    @Override
    public int getItemCount() {
        return mAssignments.size();
    }

    public class AssignmentViewHolder extends RecyclerView.ViewHolder {

        TextView mTitle, mDetail;

        AssignmentViewHolder(View itemView) {
            super(itemView);
            mTitle = (TextView) itemView.findViewById(R.id.title);
            mDetail = (TextView) itemView.findViewById(R.id.detail);
        }
    }

}
