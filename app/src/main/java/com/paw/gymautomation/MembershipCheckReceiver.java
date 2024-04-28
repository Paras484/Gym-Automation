package com.paw.gymautomation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MembershipCheckReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Create an instance of MainActivity
        MainActivity mainActivity = new MainActivity();

        // Trigger membership expiration check and message sending
        mainActivity.sendMembershipExpirationSMS(context);
    }
}
