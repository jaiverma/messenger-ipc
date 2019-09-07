package com.example.qace;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

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
//        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        showNotification("onCreate");
        model = new Model(this, Model.MODEL.PONG);
        randGen = new Random();
    }

    @Override
    public void onDestroy() {
//        mNM.cancel(mNotificationId);
        Log.i("hathi", "onDestroy");
        Toast.makeText(this, "onDestroy", Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i("hathi", "onBind");
        Toast.makeText(QACEService.this, "onBind", Toast.LENGTH_SHORT).show();
        return mMessenger.getBinder();
    }

    private void showNotification(CharSequence title) {
        Log.i("hathi", title.toString());
//        // In this sample, we'll use the same text for the ticker and the expanded notification
//        CharSequence text = "QACE";
//
//        // Set the info for the views that show in the notification panel.
//        Notification notification = new Notification.Builder(this)
//                .setTicker(text)  // the status text
//                .setWhen(System.currentTimeMillis())  // the time stamp
//                .setContentTitle(title)  // the label of the entry
//                .setContentText(text)  // the contents of the entry
//                .build();
//        // Send the notification.
//        mNM.notify(mNotificationId, notification);
    }

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SAY_HELLO:
                    Log.i("hathi", "MSG_SAY_HELLO");
                    Toast.makeText(QACEService.this, "Hello World", Toast.LENGTH_SHORT).show();
                    Messenger replyTo = msg.replyTo;
                    randNum = randGen.nextInt(1000);
                    Message reply = Message.obtain(null, MSG_SAY_HELLO, randNum, 0);
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
