package com.satsumasoftware.timetable;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.satsumasoftware.timetable.ui.MainActivity;

import java.util.Calendar;

public class AlarmReceiver extends WakefulBroadcastReceiver {

    private static final String EXTRA_NOTIFICATION_ID = "extra_notification_id";

    private AlarmManager mAlarmManager;
    private PendingIntent mPendingIntent;

    @Override
    public void onReceive(Context context, Intent data) {
        int classTimeId = data.getExtras().getInt(EXTRA_NOTIFICATION_ID);

        //ClassTime classTime = ClassUtils.getClassTimeWithId();

        Intent intent = new Intent(context, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, classTimeId, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentTitle("ClassTime id: " + classTimeId)
                .setSmallIcon(R.drawable.ic_class_black_24dp)
                .setContentText("Test description")
                .setContentIntent(pendingIntent);

        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(classTimeId, builder.build());
    }

    public void setAlarm(Context context, Calendar startDate, int classTimeId) {
        mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(EXTRA_NOTIFICATION_ID, classTimeId);

        mPendingIntent = PendingIntent.getBroadcast(context, classTimeId, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        // Calculate notification time
        long startTime = startDate.getTimeInMillis();
        long currentTime = Calendar.getInstance().getTimeInMillis();
        long diffTime = startTime - currentTime;

        // Start alarm using notification time
        mAlarmManager.set(AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + diffTime,
                mPendingIntent);

        // Restart alarm if device is rebooted
        // TODO
    }

    public void cancelAlarm(Context context, int classTimeId) {
        mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Cancel alarm using ClassTime id
        Intent intent = new Intent(context, AlarmReceiver.class);
        mPendingIntent = PendingIntent.getBroadcast(context, classTimeId, intent, 0);
        mAlarmManager.cancel(mPendingIntent);

        // Disable alarm
        // TODO
    }
}
