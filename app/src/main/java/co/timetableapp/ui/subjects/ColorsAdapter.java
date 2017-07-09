/*
 * Copyright 2017 Farbod Salamat-Zadeh
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package co.timetableapp.ui.subjects;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import co.timetableapp.R;
import co.timetableapp.model.Color;
import de.hdodenhof.circleimageview.CircleImageView;

class ColorsAdapter extends RecyclerView.Adapter<ColorsAdapter.ColorViewHolder> {

    private Context mContext;
    private ArrayList<Color> mColors;

    ColorsAdapter(Context context, ArrayList<Color> colors) {
        mContext = context;
        mColors = colors;
    }

    static ArrayList<Color> getAllColors() {
        ArrayList<Color> colors = new ArrayList<>();
        for (int i = 1; i <= 19; i++) colors.add(new Color(i));
        return colors;
    }

    @Override
    public ColorViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_color, parent, false);
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

    class ColorViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        CircleImageView mImageView;

        ColorViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mImageView = (CircleImageView) itemView.findViewById(R.id.imageView);
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
