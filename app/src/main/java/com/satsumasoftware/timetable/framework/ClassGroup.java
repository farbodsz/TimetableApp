package com.satsumasoftware.timetable.framework;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class ClassGroup implements Parcelable {

    private int mSubjectId;
    private ArrayList<Class> mClasses;

    public ClassGroup(int subjectId) {
        mSubjectId = subjectId;
        mClasses = new ArrayList<>();
    }

    public void addClass(Class cls) {
        if (cls.getSubjectId() != mSubjectId) {
            throw new IllegalArgumentException("the subject id of the Class must match that of " +
                    "this ClassGroup");
        }

        mClasses.add(cls);
    }


    public static final Creator<ClassGroup> CREATOR = new Creator<ClassGroup>() {
        @Override
        public ClassGroup createFromParcel(Parcel in) {
            return new ClassGroup(in);
        }

        @Override
        public ClassGroup[] newArray(int size) {
            return new ClassGroup[size];
        }
    };

    protected ClassGroup(Parcel source) {
        source.readInt();
        source.readTypedList(mClasses, Class.CREATOR);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mSubjectId);
        dest.writeTypedList(mClasses);
    }

}
