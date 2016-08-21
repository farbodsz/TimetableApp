package com.satsumasoftware.timetable.ui.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.satsumasoftware.timetable.R;
import com.satsumasoftware.timetable.db.util.SubjectUtilsKt;
import com.satsumasoftware.timetable.framework.Color;
import com.satsumasoftware.timetable.framework.Exam;
import com.satsumasoftware.timetable.framework.Subject;

import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;

public class ExamsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_HEADER = 1;
    private static final int VIEW_TYPE_ITEM = 2;

    private Context mContext;
    private ArrayList<String> mHeaders;
    private ArrayList<Exam> mExams;

    public ExamsAdapter(Context context, ArrayList<String> headers, ArrayList<Exam> exams) {
        mContext = context;
        mHeaders = headers;
        mExams = exams;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_HEADER:
                View header = LayoutInflater.from(parent.getContext()).inflate(R.layout.subheader, parent, false);
                return new HeaderViewHolder(header);
            case VIEW_TYPE_ITEM:
                View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_assignment, parent, false);
                return new ExamViewHolder(item);
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
                ExamViewHolder examViewHolder = (ExamViewHolder) holder;
                setupItemLayout(examViewHolder, position);
                break;
        }
    }

    private void setupHeaderLayout(HeaderViewHolder holder, int position) {
        String text = mHeaders.get(position);
        holder.mText.setText(text);
    }

    private void setupItemLayout(ExamViewHolder holder, int position) {
        Exam exam = mExams.get(position);

        Subject subject = SubjectUtilsKt.getSubjectWithId(mContext, exam.getSubjectId());
        assert subject != null;

        StringBuilder titleBuilder = new StringBuilder();
        titleBuilder.append(subject.getName());
        if (exam.hasModuleName()) {
            titleBuilder.append(": ")
                    .append(exam.getModuleName());
        }
        holder.mTitle.setText(titleBuilder.toString());

        StringBuilder details = new StringBuilder();
        if (exam.hasSeat()) {
            details.append(exam.getSeat())
                    .append(" \u2022 ");
        }
        if (exam.hasRoom()) {
            details.append(exam.getRoom());
        }
        holder.mDetail.setText(details.toString());

        holder.mDate.setText(exam.getDate().format(DateTimeFormatter.ofPattern("dd MMM uu")));
        holder.mDuration.setText(exam.getDuration() + " mins");

        Color color = new Color(subject.getColorId());
        holder.mColorView.setBackgroundColor(
                ContextCompat.getColor(mContext, color.getPrimaryColorResId(mContext)));
    }

    @Override
    public int getItemCount() {
        return mExams.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mExams.get(position) == null ? VIEW_TYPE_HEADER : VIEW_TYPE_ITEM;
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder {

        TextView mText;

        HeaderViewHolder(View itemView) {
            super(itemView);
            mText = (TextView) itemView.findViewById(R.id.text);
        }
    }

    public class ExamViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        View mColorView;
        TextView mTitle, mDetail, mDate, mDuration;

        ExamViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mColorView = itemView.findViewById(R.id.color);
            mTitle = (TextView) itemView.findViewById(R.id.title);
            mDetail = (TextView) itemView.findViewById(R.id.subject);
            mDate = (TextView) itemView.findViewById(R.id.due_date);
            mDuration = (TextView) itemView.findViewById(R.id.completion_progress);
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
