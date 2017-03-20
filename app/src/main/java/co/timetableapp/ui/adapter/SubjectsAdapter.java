package co.timetableapp.ui.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import co.timetableapp.R;
import co.timetableapp.framework.Color;
import co.timetableapp.framework.Subject;

public class SubjectsAdapter extends RecyclerView.Adapter<SubjectsAdapter.SubjectViewHolder> {

    private Context mContext;
    private ArrayList<Subject> mSubjects;

    public SubjectsAdapter(Context context, ArrayList<Subject> subjects) {
        mContext = context;
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
        Color color = new Color(subject.getColorId());

        holder.mColor.setBackgroundColor(ContextCompat.getColor(
                mContext, color.getPrimaryColorResId(mContext)));
        holder.mName.setText(subject.getName());
    }

    @Override
    public int getItemCount() {
        return mSubjects.size();
    }

    public class SubjectViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        View mColor;
        TextView mName;

        SubjectViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mColor = itemView.findViewById(R.id.color);
            mName = (TextView) itemView.findViewById(R.id.name);
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
