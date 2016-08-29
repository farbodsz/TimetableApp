package com.satsumasoftware.timetable;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.satsumasoftware.timetable.db.util.ClassUtils;
import com.satsumasoftware.timetable.db.util.SubjectUtils;
import com.satsumasoftware.timetable.framework.Class;
import com.satsumasoftware.timetable.framework.ClassDetail;
import com.satsumasoftware.timetable.framework.ClassTime;
import com.satsumasoftware.timetable.framework.Subject;
import com.satsumasoftware.timetable.ui.MainActivity;

import java.util.Calendar;

public class AlarmReceiver extends WakefulBroadcastReceiver {

    private static final String LOG_TAG = "AlarmReceiver";

    private static final String EXTRA_NOTIFICATION_ID = "extra_notification_id";

    private AlarmManager mAlarmManager;
    private PendingIntent mPendingIntent;

    @Override
    public void onReceive(Context context, Intent data) {
        int classTimeId = data.getExtras().getInt(EXTRA_NOTIFICATION_ID);
        ClassTime classTime = ClassUtils.getClassTimeWithId(context, classTimeId);

        ClassDetail classDetail = ClassUtils.getClassDetailWithId(context, classTime.getClassDetailId());
        Class cls = ClassUtils.getClassWithId(context, classDetail.getClassId());
        assert cls != null;

        Subject subject = SubjectUtils.getSubjectWithId(context, cls.getSubjectId());
        assert subject != null;

        Intent intent = new Intent(context, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, classTimeId, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentTitle(subject.getName())
                .setSmallIcon(R.drawable.ic_class_black_24dp)
                .setContentText(makeDescriptionText(classDetail, classTime))
                .setContentIntent(pendingIntent);

        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(classTimeId, builder.build());
    }

    public void setRepeatingAlarm(Context context, Calendar startDateTime, int classTimeId,
                                  long repeatInterval) {
        Log.i(LOG_TAG, "Setting repeated alarm for calendar: " + startDateTime.toString());

        mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(EXTRA_NOTIFICATION_ID, classTimeId);

        mPendingIntent = PendingIntent.getBroadcast(context, classTimeId, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        // Calculate notification time
        long startTimeMs = startDateTime.getTimeInMillis();
        long currentTimeMs = Calendar.getInstance().getTimeInMillis();
        long diffTime = startTimeMs - currentTimeMs;

        // Start alarm(s) using notification time
        mAlarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + diffTime,
                repeatInterval,
                mPendingIntent);

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
