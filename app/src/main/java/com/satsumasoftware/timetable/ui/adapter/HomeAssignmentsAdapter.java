package com.satsumasoftware.timetable.ui.adapter;

import android.content.Context;
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_card_detail, parent, false);
        return new AssignmentsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AssignmentsViewHolder holder, int position) {
        Assignment assignment = mAssignments.get(position);

        holder.mTitle.setText(assignment.getTitle());

        Class cls = ClassUtilsKt.getClassWithId(mContext, assignment.getClassId());
        assert cls != null;

        Subject subject = SubjectUtilsKt.getSubjectWithId(mContext, cls.getSubjectId());
        assert subject != null;

        Color color = new Color(subject.getColorId());
        holder.mColorCircle.setImageResource(color.getPrimaryColorResId(mContext));

        String details = subject.getName() +
                " \u2022 " +
                assignment.getDueDate().format(DateTimeFormatter.ofPattern("d MMM")) +
                " \u2022 " +
                mContext.getString(R.string.property_progress, assignment.getCompletionProgress());

        holder.mDetails.setText(details);
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
            mTitle = (TextView) itemView.findViewById(R.id.subject);
            mDetails = (TextView) itemView.findViewById(R.id.details);
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
