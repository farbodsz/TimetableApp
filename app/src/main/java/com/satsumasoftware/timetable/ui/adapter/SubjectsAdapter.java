package com.satsumasoftware.timetable.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.satsumasoftware.timetable.R;
import com.satsumasoftware.timetable.framework.Subject;

import java.util.ArrayList;

public class SubjectsAdapter extends RecyclerView.Adapter<SubjectsAdapter.SubjectViewHolder> {

    private ArrayList<Subject> mSubjects;

    public SubjectsAdapter(ArrayList<Subject> subjects) {
        mSubjects = subjects;
    }

    @Override
    public SubjectViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subject, parent, false);
        return new SubjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SubjectViewHolder holder, int position) {
        Subject subject = mSubjects.get(position);

        holder.mName.setText(subject.getName());
    }

    @Override
    public int getItemCount() {
        return mSubjects.size();
    }

    public class SubjectViewHolder extends RecyclerView.ViewHolder {

        TextView mName;

        SubjectViewHolder(View itemView) {
            super(itemView);
            mName = (TextView) itemView.findViewById(R.id.name);
        }
    }

}
