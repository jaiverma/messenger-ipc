package com.example.qace;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Random;

public class QACEService extends Service {
    public static final int MSG_SAY_HELLO = 1;
    NotificationManager mNM;
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    final int mNotificationId = 0xdeadbeef;
    Model model;
    int randNum;
    Random randGen;
    public QACEService() {
    }

    @Override
    public void onCreate() {
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        showNotification("onCreate");
        model = new Model(this, Model.MODEL.PONG);
        randGen = new Random();
    }

    @Override
    public void onDestroy() {
        mNM.cancel(mNotificationId);
        model.deInit();
        Toast.makeText(this, "onDestroy", Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Toast.makeText(QACEService.this, "onBind", Toast.LENGTH_SHORT).show();
        return mMessenger.getBinder();
    }

    private void showNotification(CharSequence title) {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = "QACE";

        NotificationChannel nChannel = new NotificationChannel("QACE", "QACE_CHANNEL", NotificationManager.IMPORTANCE_HIGH);
        nChannel.setDescription("Q-ACE Service");
        nChannel.enableLights(true);
        nChannel.setLightColor(Color.RED);
        nChannel.enableVibration(true);
        nChannel.setShowBadge(true);
        mNM.createNotificationChannel(nChannel);

        // Set the info for the views that show in the notification panel.
        Notification notification = new NotificationCompat.Builder(this, "QACE")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setTicker(text)  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle(title)  // the label of the entry
                .setContentText(text)  // the contents of the entry
                .build();
        // Send the notification.
        mNM.notify(mNotificationId, notification);
    }

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SAY_HELLO:
                    Messenger replyTo = msg.replyTo;
                    Bundle b = (Bundle)msg.obj;
                    float[] f = b.getFloatArray("data-from-qace");
                    Message reply = Message.obtain(null, MSG_SAY_HELLO, f.length, 0);
                    Toast.makeText(QACEService.this, String.valueOf(f[2]), Toast.LENGTH_SHORT).show();
                    try {
                        replyTo.send(reply);
                    } catch (RemoteException e) {
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
}
