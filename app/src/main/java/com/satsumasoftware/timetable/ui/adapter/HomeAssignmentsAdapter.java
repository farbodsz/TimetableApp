package com.satsumasoftware.timetable.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.satsumasoftware.timetable.R;
import com.satsumasoftware.timetable.db.util.SubjectUtils;
import com.satsumasoftware.timetable.framework.Assignment;
import com.satsumasoftware.timetable.framework.Class;
import com.satsumasoftware.timetable.framework.Color;
import com.satsumasoftware.timetable.framework.Subject;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeAssignmentsAdapter extends RecyclerView.Adapter<HomeAssignmentsAdapter.AssignmentsViewHolder> {

    private Context mContext;
    private ArrayList<Assignment> mAssignments;

    public HomeAssignmentsAdapter(Context context, ArrayList<Assignment> assignments) {
        mContext = context;
        mAssignments = assignments;
    }

    @Override
    public AssignmentsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_general_small, parent, false);
        return new AssignmentsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AssignmentsViewHolder holder, int position) {
        Assignment assignment = mAssignments.get(position);

        holder.mTitle.setText(assignment.getTitle());

        Class cls = Class.create(mContext, assignment.getClassId());
        assert cls != null;

        Subject subject = SubjectUtils.getSubjectWithId(mContext, cls.getSubjectId());
        assert subject != null;

        Color color = new Color(subject.getColorId());
        holder.mColorCircle.setImageResource(color.getPrimaryColorResId(mContext));

        boolean isOverdue = assignment.getDueDate().isBefore(LocalDate.now());

        StringBuilder details = new StringBuilder();

        if (isOverdue) {
            details.append("<font color=\"#F44336\"><b>");
            details.append(mContext.getString(R.string.due_overdue));
            details.append("</b> \u2022 ");
        }

        details.append(subject.getName())
                .append(" \u2022 ");
        details.append(assignment.getDueDate().format(DateTimeFormatter.ofPattern("d MMM")));
        details.append(" \u2022 ")
                .append(mContext.getString(R.string.property_progress,
                        assignment.getCompletionProgress()));

        if (isOverdue) details.append("</font>");

        holder.mDetails.setText(Html.fromHtml(details.toString()));
    }

    @Override
    public int getItemCount() {
        return mAssignments.size();
    }

    public class AssignmentsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        CircleImageView mColorCircle;
        TextView mTitle, mDetails;

        AssignmentsViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mColorCircle = (CircleImageView) itemView.findViewById(R.id.imageView);
            mTitle = (TextView) itemView.findViewById(R.id.text1);
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
