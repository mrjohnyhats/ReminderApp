package jinnycorp.reminderapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ServiceStart extends BroadcastReceiver {
    public ServiceStart() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("CUSTOML", "starting checker from broadcast reciever");
        Intent startService = new Intent(context, rchecker.class);
        context.startService(startService);
    }
}
