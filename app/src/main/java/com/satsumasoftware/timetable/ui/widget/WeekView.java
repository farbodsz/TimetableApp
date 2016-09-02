package com.satsumasoftware.timetable.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.satsumasoftware.timetable.R;
import com.satsumasoftware.timetable.framework.Class;
import com.satsumasoftware.timetable.framework.ClassDetail;
import com.satsumasoftware.timetable.framework.ClassTime;
import com.satsumasoftware.timetable.framework.Color;
import com.satsumasoftware.timetable.framework.Subject;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalTime;

import java.util.ArrayList;
import java.util.Calendar;

public class WeekView extends View {

    private Rect mHeaderRowRect;
    private int mHeaderRowHeight = 36;
    private Paint mHeaderRowPaint;
    private int mHeaderRowBkgdColorRes = R.color.mdu_grey_200;

    private Paint mHeaderRowBorderPaint;
    private int mHeaderRowBorderColorRes = R.color.mdu_divider_black;

    private Rect mTimeColRect;
    private Paint mTimeColPaint;
    private int mTimeColWidth = 48;
    private int mTimeColBkgdColorRes = R.color.mdu_grey_200;

    private int mRowHeight = 72;

    private int mTextSize = 14;
    private int mTextColorRes = R.color.mdu_text_black;
    private Paint mTextPaint;

    private int mTodayTextColorRes = R.color.mdu_blue_700;
    private Paint mTodayTextPaint;

    private Paint mLinePaint;
    private int mLineColorRes = R.color.mdu_divider_black;

    private ArrayList<ClassTime> mClassTimes = new ArrayList<>();

    private RectF mEventRect;
    private Paint mEventPaint;

    private int mEventTextSize = 12;
    private int mEventTextColorRes = R.color.mdu_text_white;
    private Paint mEventTextPaint;

    private static final int NUMBER_OF_DAYS = 7;
    private DayOfWeek mStartOfWeek = DayOfWeek.MONDAY;

    private Paint mCurrentLinePaint;
    private int mCurrentLineColorRes = R.color.mdu_blue_700;
    private int mCurrentLineWidth = 4;


    public WeekView(Context context) {
        this(context, null);
    }

    public WeekView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WeekView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setClassTimes(ArrayList<ClassTime> classTimes) {
        mClassTimes = classTimes;
    }

    private void init() {
        mHeaderRowRect = new Rect();

        mHeaderRowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHeaderRowPaint.setStyle(Paint.Style.FILL);
        mHeaderRowPaint.setColor(ContextCompat.getColor(getContext(), mHeaderRowBkgdColorRes));

        mHeaderRowBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHeaderRowBorderPaint.setColor(ContextCompat.getColor(
                getContext(), mHeaderRowBorderColorRes));

        mTimeColRect = new Rect();

        mTimeColPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTimeColPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mTimeColPaint.setColor(ContextCompat.getColor(getContext(), mTimeColBkgdColorRes));

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(ContextCompat.getColor(getContext(), mTextColorRes));
        mTextPaint.setTextSize(dpToPx(mTextSize));

        mTodayTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTodayTextPaint.setColor(ContextCompat.getColor(getContext(), mTodayTextColorRes));
        mTodayTextPaint.setTextSize(dpToPx(mTextSize));
        mTodayTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setColor(ContextCompat.getColor(getContext(), mLineColorRes));

        mEventRect = new RectF();

        mEventPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mEventPaint.setStyle(Paint.Style.FILL);

        mEventTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mEventTextPaint.setColor(ContextCompat.getColor(getContext(), mEventTextColorRes));
        mEventTextPaint.setTextSize(dpToPx(mEventTextSize));

        mCurrentLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCurrentLinePaint.setColor(ContextCompat.getColor(getContext(), mCurrentLineColorRes));
        mCurrentLinePaint.setStrokeWidth(dpToPx(mCurrentLineWidth));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Calendar instance = Calendar.getInstance();

        Log.d("WKV", "getLeft() - " + getLeft());
        Log.d("WKV", "getTop() - " + getTop());
        Log.d("WKV", "getRight() - " + getRight());
        Log.d("WKV", "getBottom() - " + getBottom());

        Log.d("WKV", "getMeasuredHeight() - " + getMeasuredHeight());
        Log.d("WKV", "getHeight() - " + getHeight());

        mHeaderRowRect.set(0, 0, getWidth(), dpToPx(mHeaderRowHeight));
        canvas.drawRect(mHeaderRowRect, mHeaderRowPaint);

        mTimeColRect.set(getLeft(), dpToPx(mHeaderRowHeight), dpToPx(mTimeColWidth), getBottom());
        canvas.drawRect(mTimeColRect, mTimeColPaint);

        // Border/divider line for the header
        canvas.drawLine(getLeft(), dpToPx(mHeaderRowHeight), getRight(), dpToPx(mHeaderRowHeight), mHeaderRowBorderPaint);

        // Draw the text and lines for the time column and its rows
        for (int i = 0; i < 24; i++) {
            LocalTime time = LocalTime.of(i, 0);
            float lineY = getYForTime(time);
            canvas.drawLine(dpToPx(mTimeColWidth), lineY, getRight(), lineY, mLinePaint);

            String timeText = i < 10 ? "0" + i + ":00" : i + ":00";
            float textX = dpToPx(4);
            float textY = lineY + dpToPx(mTextSize / 2);
            canvas.drawText(timeText, textX, textY, mTextPaint);
        }

        // Draw text and lines for the day columns
        int dayWidth = (getWidth() - dpToPx(mTimeColWidth)) / NUMBER_OF_DAYS;
        for (int i = 0; i < NUMBER_OF_DAYS; i++) {
            int dowValue = mStartOfWeek.getValue() + i;
            if (dowValue > 7) dowValue = dowValue - 7;
            DayOfWeek thisDay = DayOfWeek.of(dowValue);

            int textX = dpToPx(mTimeColWidth) + (dayWidth * i);
            int textY = dpToPx(mHeaderRowHeight / 2) + dpToPx(mTextSize / 2);

            int instanceDowVal = instance.get(Calendar.DAY_OF_WEEK);  // Sunday is 1, Saturday is 7
            instanceDowVal--;
            if (instanceDowVal == 0) instanceDowVal = 7;  // now, Sunday is 7, Saturday is 6

            boolean isToday = instanceDowVal == dowValue;

            canvas.drawText(getDayText(thisDay),
                    textX,
                    textY,
                    isToday ? mTodayTextPaint : mTextPaint);

            int lineStartY = dpToPx(mHeaderRowHeight);
            int lineEndY = getHeight();
            canvas.drawLine(textX, lineStartY, textX, lineEndY, mLinePaint);
        }

        // Display events
        for (ClassTime classTime : mClassTimes) {
            ClassDetail classDetail =
                    ClassDetail.create(getContext(), classTime.getClassDetailId());
            Class cls = Class.create(getContext(), classDetail.getClassId());
            assert cls != null;
            Subject subject = Subject.create(getContext(), cls.getSubjectId());
            assert subject != null;

            Color color = new Color(subject.getColorId());
            mEventPaint.setColor(ContextCompat.getColor(
                    getContext(), color.getPrimaryColorResId(getContext())));

            int dayColumn = getColumnForDay(classTime.getDay());

            float startX = getStartXForColumn(dayColumn);
            float endX = getEndXForColumn(dayColumn);
            float startY = getYForTime(classTime.getStartTime());
            float endY = getYForTime(classTime.getEndTime());

            mEventRect.set(startX, startY, endX, endY);
            canvas.drawRect(mEventRect, mEventPaint);

            // Now for the text of the events
            canvas.drawText(subject.getName(),
                    startX + dpToPx(2),
                    startY + dpToPx(mTextSize),
                    mEventTextPaint);
        }

        // Draw current line
        LocalTime now = LocalTime.of(instance.get(Calendar.HOUR_OF_DAY),
                instance.get(Calendar.MINUTE));
        canvas.drawLine(dpToPx(mTimeColWidth),
                getYForTime(now),
                getRight(),
                getYForTime(now),
                mCurrentLinePaint);
    }

    private String getDayText(DayOfWeek dayOfWeek) {
        String text = dayOfWeek.toString().toLowerCase().substring(0, 3);
        return text.substring(0, 1).toUpperCase() + text.substring(1, text.length());
    }

    private int getColumnForDay(DayOfWeek dayOfWeek) {
        for (int i = 0; i < 7; i++) {
            int dowValue = mStartOfWeek.getValue() + i;
            if (dowValue > 7){
                dowValue = dowValue - 7;
            }

            if (dayOfWeek == DayOfWeek.of(dowValue)) {
                return i;
            }
        }
        throw new NullPointerException("unable to find column for day '" + dayOfWeek + "'");
    }

    private float getStartXForColumn(int columnIndex) {
        if (columnIndex > NUMBER_OF_DAYS - 1) {
            throw new IllegalArgumentException("invalid columnIndex - must start from 0 and be " +
                    "less than the number of columns - 1 (" + NUMBER_OF_DAYS + ")");
        }
        int dayWidth = (getWidth() - dpToPx(mTimeColWidth)) / NUMBER_OF_DAYS;

        return dpToPx(mTimeColWidth) + (dayWidth * columnIndex);
    }

    private float getEndXForColumn(int columnIndex) {
        if (columnIndex > NUMBER_OF_DAYS - 1) {
            throw new IllegalArgumentException("invalid columnIndex - must start from 0 and be " +
                    "less than the number of columns - 1 (" + NUMBER_OF_DAYS + ")");
        }
        int dayWidth = (getWidth() - dpToPx(mTimeColWidth)) / NUMBER_OF_DAYS;

        return dpToPx(mTimeColWidth) + (dayWidth * (columnIndex + 1));
    }

    private float getYForTime(LocalTime time) {
        int hour = time.getHour();
        int minute = time.getMinute();

        float hourY = dpToPx(mHeaderRowHeight) + (hour * dpToPx(mRowHeight));
        float minuteY = dpToPx(mRowHeight) / 60 * minute;

        return hourY + minuteY;
    }

    private int dpToPx(int dps) {
        float density = getContext().getResources().getDisplayMetrics().density;
        return (int) (dps * density + 0.5f);
    }

}
