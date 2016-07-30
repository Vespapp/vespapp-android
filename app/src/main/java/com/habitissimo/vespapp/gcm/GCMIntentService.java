package com.habitissimo.vespapp.gcm;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

//import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.habitissimo.vespapp.MainActivity;
import com.habitissimo.vespapp.R;

/**
 * Created by Simó on 22/07/2016.
 */
public class GCMIntentService extends IntentService {

    private static final int NOTIF_ALERTA_ID = 1;

    public GCMIntentService() {
        super("GCMIntentService");
    }


    //Para Google Cloud Message en futuras actualizaciones
    @Override
    protected void onHandleIntent(Intent intent) {
//        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
//
//        String messageType = gcm.getMessageType(intent);
//        Bundle extras = intent.getExtras();
//
//        if (!extras.isEmpty()) {
//
//            if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
//                showNotification(extras.getString("msg"));
//            }
//        }
//
//        GCMBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void showNotification(String msg) {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.iconapp)
                        .setContentTitle("Estado avispamiento")
                        .setContentText(msg);

        Intent notIntent =  new Intent(this, MainActivity.class);
        PendingIntent contIntent = PendingIntent.getActivity(
                this, 0, notIntent, 0);

        mBuilder.setContentIntent(contIntent);

        mNotificationManager.notify(NOTIF_ALERTA_ID, mBuilder.build());
    }
}
