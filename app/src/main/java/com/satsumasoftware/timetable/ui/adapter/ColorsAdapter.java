package com.satsumasoftware.timetable.ui.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.satsumasoftware.timetable.R;
import com.satsumasoftware.timetable.framework.Assignment;
import com.satsumasoftware.timetable.framework.Color;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ColorsAdapter extends RecyclerView.Adapter<ColorsAdapter.ColorViewHolder> {

    private Context mContext;
    private ArrayList<Color> mColors;

    public ColorsAdapter(Context context, ArrayList<Color> colors) {
        mContext = context;
        mColors = colors;
    }

    public static ArrayList<Color> getAllColors() {
        ArrayList<Color> colors = new ArrayList<>();
        for (int i = 1; i <= 19; i++) colors.add(new Color(i));
        return colors;
    }

    @Override
    public ColorViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_color, parent, false);
        return new ColorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ColorViewHolder holder, int position) {
        Color color = mColors.get(position);
        holder.mImageView.setImageResource(color.getPrimaryColorResId(mContext));
    }

    @Override
    public int getItemCount() {
        return mColors.size();
    }

    public class ColorViewHolder extends RecyclerView.ViewHolder {

        CircleImageView mImageView;

        ColorViewHolder(View itemView) {
            super(itemView);
            mImageView = (CircleImageView) itemView.findViewById(R.id.imageView);
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
