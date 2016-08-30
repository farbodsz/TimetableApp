package com.satsumasoftware.timetable;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.satsumasoftware.timetable.db.util.ClassUtils;
import com.satsumasoftware.timetable.db.util.SubjectUtils;
import com.satsumasoftware.timetable.framework.Class;
import com.satsumasoftware.timetable.framework.ClassDetail;
import com.satsumasoftware.timetable.framework.ClassTime;
import com.satsumasoftware.timetable.framework.Color;
import com.satsumasoftware.timetable.framework.Subject;
import com.satsumasoftware.timetable.ui.MainActivity;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Calendar;

public class AlarmReceiver extends WakefulBroadcastReceiver {

    private static final String LOG_TAG = "AlarmReceiver";
    private static final long NO_REPEAT_INTERVAL = -1;

    private static final String EXTRA_ITEM_ID = "extra_item_id";
    private static final String EXTRA_NOTIFICATION_TYPE = "extra_notification_type";

    @IntDef({Type.CLASS})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {
        int CLASS = 1;
    }

    private AlarmManager mAlarmManager;
    private PendingIntent mPendingIntent;

    @Override
    public void onReceive(Context context, Intent data) {
        Bundle extras = data.getExtras();
        int id = extras.getInt(EXTRA_ITEM_ID);
        @Type int notificationType = extras.getInt(EXTRA_NOTIFICATION_TYPE);

        int notificationId = (notificationType * 100000) + id;

        Subject subject;
        Intent intent;

        String contentTitle, contentText, tickerText;
        @DrawableRes int drawableRes;

        switch (notificationType) {
            case Type.CLASS:
                ClassTime classTime = ClassUtils.getClassTimeWithId(context, id);
                ClassDetail classDetail = ClassUtils.getClassDetailWithId(
                        context, classTime.getClassDetailId());
                Class cls = ClassUtils.getClassWithId(context, classDetail.getClassId());
                assert cls != null;

                subject = SubjectUtils.getSubjectWithId(context, cls.getSubjectId());
                assert subject != null;

                intent = new Intent(context, MainActivity.class);

                contentTitle = subject.getName();
                drawableRes = R.drawable.ic_class_white_24dp;
                contentText = makeDescriptionText(classDetail, classTime);
                tickerText = subject.getName() + " class starting in 5 minutes";
                break;

            default:
                throw new IllegalArgumentException("invalid notification type");
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Color color = new Color(subject.getColorId());
        String hexString = Integer.toHexString(
                ContextCompat.getColor(context, color.getPrimaryColorResId(context)));
        int colorArgb = android.graphics.Color.parseColor("#" + hexString);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentTitle(contentTitle)
                .setSmallIcon(drawableRes)
                .setContentText(contentText)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setColor(colorArgb)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setTicker(tickerText);

        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(notificationId, builder.build());
    }

    public void setAlarm(Context context, @Type int notificationType, Calendar dateTime,
                         int itemId) {
        setRepeatingAlarm(context, notificationType, dateTime, itemId, NO_REPEAT_INTERVAL);
    }

    public void setRepeatingAlarm(Context context, @Type int notificationType,
                                  Calendar startDateTime, int itemId, long repeatInterval) {
        boolean isRepeat = repeatInterval != NO_REPEAT_INTERVAL;
        Log.i(LOG_TAG, isRepeat ?
                "Setting repeating alarm for calendar: " + startDateTime.toString() :
                "Setting alarm for calendar: " + startDateTime.toString());

        mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(EXTRA_ITEM_ID, itemId);
        intent.putExtra(EXTRA_NOTIFICATION_TYPE, notificationType);

        mPendingIntent = PendingIntent.getBroadcast(context, itemId, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        // Calculate notification time
        long startTimeMs = startDateTime.getTimeInMillis();
        long currentTimeMs = Calendar.getInstance().getTimeInMillis();
        long diffTime = startTimeMs - currentTimeMs;

        // Start alarm(s) using notification time
        if (isRepeat) {
            mAlarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime() + diffTime,
                    repeatInterval,
                    mPendingIntent);
        } else {
            mAlarmManager.set(AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime() + diffTime,
                    mPendingIntent);
        }

        // Restart alarm if device is rebooted
        // TODO
    }

    public void cancelAlarm(Context context, int classTimeId) {
        Log.i(LOG_TAG, "Cancelling repeated alarm for classTimeId: " + classTimeId);

        mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Cancel alarm using ClassTime id
        Intent intent = new Intent(context, AlarmReceiver.class);
        mPendingIntent = PendingIntent.getBroadcast(context, classTimeId, intent, 0);
        mAlarmManager.cancel(mPendingIntent);

        // Disable alarm
        // TODO
    }

    private String makeDescriptionText(ClassDetail classDetail, ClassTime classTime) {
        StringBuilder builder = new StringBuilder();

        builder.append(classTime.getStartTime().toString())
                .append(" - ")
                .append(classTime.getEndTime().toString());

        if (classDetail.hasRoom() || classDetail.hasBuilding()) {
            builder.append(" \u2022 ");

            if (classDetail.hasRoom()) {
                builder.append(classDetail.getRoom());
                if (classDetail.hasBuilding()) builder.append(", ");
            }
            if (classDetail.hasBuilding()) {
                builder.append(classDetail.getBuilding());
            }
        }

        return builder.toString();
    }
}
