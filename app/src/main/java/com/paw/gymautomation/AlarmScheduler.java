package com.paw.gymautomation;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class AlarmScheduler {

    private static final long INTERVAL_DAY = AlarmManager.INTERVAL_DAY;

    public static void scheduleAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, MembershipCheckReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        long currentTimeMillis = System.currentTimeMillis();
        long midnightMillis = currentTimeMillis - (currentTimeMillis % INTERVAL_DAY);
        long triggerTimeMillis = midnightMillis + INTERVAL_DAY;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent);
        } else {
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, triggerTimeMillis, INTERVAL_DAY, pendingIntent);
        }
    }
}
