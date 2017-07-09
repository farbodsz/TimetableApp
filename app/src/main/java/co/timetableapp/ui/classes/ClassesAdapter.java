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

package co.timetableapp.ui.classes;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import co.timetableapp.R;
import co.timetableapp.data.handler.ClassDetailHandler;
import co.timetableapp.data.handler.DataNotFoundException;
import co.timetableapp.model.Class;
import co.timetableapp.model.ClassDetail;
import co.timetableapp.model.Color;
import co.timetableapp.model.Subject;

public class ClassesAdapter extends RecyclerView.Adapter<ClassesAdapter.ClassesViewHolder> {

    private Context mContext;
    private ArrayList<Class> mClasses;

    public ClassesAdapter(Context context, ArrayList<Class> classes) {
        mContext = context;
        mClasses = classes;
    }

    @Override
    public ClassesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_classes, parent, false);
        return new ClassesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ClassesViewHolder holder, int position) {
        Class cls = mClasses.get(position);

        Subject subject = null;
        try {
            subject = Subject.create(mContext, cls.getSubjectId());
        } catch (DataNotFoundException e) {
            e.printStackTrace();
        }
        assert subject != null;

        holder.mSubject.setText(cls.makeName(subject));

        Color color = new Color(subject.getColorId());
        holder.mColorView.setBackgroundColor(ContextCompat.getColor(
                mContext, color.getPrimaryColorResId(mContext)));

        ArrayList<ClassDetail> classDetails =
                ClassDetailHandler.getClassDetailsForClass(mContext, cls.getId());

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < classDetails.size(); i++) {
            ClassDetail classDetail = classDetails.get(i);

            if (classDetail.hasRoom()) {
                builder.append(classDetail.getRoom());
                if (classDetail.hasTeacher()) builder.append(" \u2022 ");
            }

            if (classDetail.hasTeacher()) {
                builder.append(classDetail.getTeacher());
            }

            if (i != classDetails.size() - 1) {
                builder.append("\n");
            }
        }
        holder.mClassInfo.setText(builder.toString());
    }

    @Override
    public int getItemCount() {
        return mClasses.size();
    }

    class ClassesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        View mColorView;
        TextView mSubject, mClassInfo;

        ClassesViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mColorView = itemView.findViewById(R.id.color);
            mSubject = (TextView) itemView.findViewById(R.id.subject);
            mClassInfo = (TextView) itemView.findViewById(R.id.class_details);
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
