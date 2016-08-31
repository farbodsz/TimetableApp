package com.satsumasoftware.timetable.ui.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.satsumasoftware.timetable.R;
import com.satsumasoftware.timetable.framework.Assignment;
import com.satsumasoftware.timetable.framework.Class;
import com.satsumasoftware.timetable.framework.Color;
import com.satsumasoftware.timetable.framework.Subject;

import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;

public class AssignmentsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_HEADER = 1;
    private static final int VIEW_TYPE_ITEM = 2;

    private Context mContext;
    private ArrayList<String> mHeaders;
    private ArrayList<Assignment> mAssignments;

    public AssignmentsAdapter(Context context, ArrayList<String> headers, ArrayList<Assignment> assignments) {
        mContext = context;
        mHeaders = headers;
        mAssignments = assignments;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_HEADER:
                View header = LayoutInflater.from(parent.getContext()).inflate(R.layout.subheader, parent, false);
                return new HeaderViewHolder(header);
            case VIEW_TYPE_ITEM:
                View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_general, parent, false);
                return new AssignmentViewHolder(item);
            default:
                throw new IllegalArgumentException("invalid view type");
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case VIEW_TYPE_HEADER:
                HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
                setupHeaderLayout(headerViewHolder, position);
                break;
            case VIEW_TYPE_ITEM:
                AssignmentViewHolder assignmentViewHolder = (AssignmentViewHolder) holder;
                setupItemLayout(assignmentViewHolder, position);
                break;
        }
    }

    private void setupHeaderLayout(HeaderViewHolder holder, int position) {
        String text = mHeaders.get(position);
        holder.mText.setText(text);
    }

    private void setupItemLayout(AssignmentViewHolder holder, int position) {
        Assignment assignment = mAssignments.get(position);

        holder.mTitle.setText(assignment.getTitle());

        holder.mDueDate.setText(assignment.getDueDate().format(DateTimeFormatter.ofPattern("dd MMM uu")));
        holder.mCompletion.setText(assignment.getCompletionProgress() + " %");

        Class cls = Class.create(mContext, assignment.getClassId());
        assert cls != null;
        Subject subject = Subject.create(mContext, cls.getSubjectId());
        assert subject != null;

        holder.mSubject.setText(subject.getName());

        Color color = new Color(subject.getColorId());
        holder.mColorView.setBackgroundColor(
                ContextCompat.getColor(mContext, color.getPrimaryColorResId(mContext)));
    }

    @Override
    public int getItemCount() {
        return mAssignments.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mAssignments.get(position) == null ? VIEW_TYPE_HEADER : VIEW_TYPE_ITEM;
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder {

        TextView mText;

        HeaderViewHolder(View itemView) {
            super(itemView);
            mText = (TextView) itemView.findViewById(R.id.text);
        }
    }

    public class AssignmentViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        View mColorView;
        TextView mTitle, mSubject, mDueDate, mCompletion;

        AssignmentViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mColorView = itemView.findViewById(R.id.color);
            mTitle = (TextView) itemView.findViewById(R.id.text1);
            mSubject = (TextView) itemView.findViewById(R.id.text2);
            mDueDate = (TextView) itemView.findViewById(R.id.text3);
            mCompletion = (TextView) itemView.findViewById(R.id.text4);
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
