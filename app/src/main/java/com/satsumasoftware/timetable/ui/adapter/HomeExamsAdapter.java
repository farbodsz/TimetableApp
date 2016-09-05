package com.satsumasoftware.timetable.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.satsumasoftware.timetable.R;
import com.satsumasoftware.timetable.framework.Color;
import com.satsumasoftware.timetable.framework.Exam;
import com.satsumasoftware.timetable.framework.Subject;

import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeExamsAdapter extends RecyclerView.Adapter<HomeExamsAdapter.ExamsViewHolder> {

    private Context mContext;
    private ArrayList<Exam> mExams;

    public HomeExamsAdapter(Context context, ArrayList<Exam> exams) {
        mContext = context;
        mExams = exams;
    }

    @Override
    public ExamsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_general_small, parent, false);
        return new ExamsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ExamsViewHolder holder, int position) {
        Exam exam = mExams.get(position);

        Subject subject = Subject.create(mContext, exam.getSubjectId());
        assert subject != null;

        holder.mTitle.setText(Exam.makeName(exam, subject));

        Color color = new Color(subject.getColorId());
        holder.mColorCircle.setImageResource(color.getPrimaryColorResId(mContext));

        StringBuilder details = new StringBuilder();
        details.append(exam.getStartTime().toString())
                .append(", ")
                .append(exam.getDate().format(DateTimeFormatter.ofPattern("d MMM")));
        if (exam.hasSeat()) {
            details.append(" \u2022 ")
                    .append(exam.getSeat());
        }
        if (exam.hasRoom()) {
            details.append(" \u2022 ")
                    .append(exam.getRoom());
        }

        holder.mDetails.setText(details.toString());
    }

    @Override
    public int getItemCount() {
        return mExams.size();
    }

    public class ExamsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        CircleImageView mColorCircle;
        TextView mTitle, mDetails;

        ExamsViewHolder(View itemView) {
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
