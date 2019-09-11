package com.example.qaceclient;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    Messenger mService = null;
    boolean mIsBound ;
    TextView mCallbackText;
    public static final int MSG_SAY_HELLO = 1;
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SAY_HELLO:
                    mCallbackText.setText("Received from service: " + msg.arg1);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private Bundle makeMessage() {
        ArrayList<Float> data = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
            data.add(i, (float)i / 3);
        }
        float[] d = new float[data.size()];
        int i = 0;
        for (Float f : data) {
            d[i++] = f;
        }
        Bundle b = new Bundle();
        b.putFloatArray("data-from-qace", d);
        return b;
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            mCallbackText.setText("Attached");
            Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mService = null;
            mCallbackText.setText("Disconnected");
            Toast.makeText(MainActivity.this, "Disconnected", Toast.LENGTH_SHORT).show();
        }
    };

    void doBindService() {
        Intent i = new Intent();
        i.setComponent(new ComponentName("com.example.qace", "com.example.qace.QACEService"));
        bindService(i, mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
        mCallbackText.setText("Binding");
    }

    void doUnbindService() {
        if (mIsBound) {
            Log.i("hathi", "doUnbindService");
            unbindService(mConnection);
            mIsBound = false;
            mCallbackText.setText("Unbinding");
        }
    }

    void doSendMsg() {
        if (mIsBound) {
            Log.i("hathi", "doSendMsg");
            try {
                Message msg = Message.obtain(null, MSG_SAY_HELLO);
                msg.replyTo = mMessenger;
                msg.obj = makeMessage();
                mService.send(msg);
            } catch (RemoteException e) {
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = (Button)findViewById(R.id.bind);
        button.setOnClickListener(mBindListener);
        button = (Button)findViewById(R.id.unbind);
        button.setOnClickListener(mUnbindListener);
        button = (Button)findViewById(R.id.sendMsg);
        button.setOnClickListener(mSendMsgListener);

        mCallbackText = (TextView)findViewById(R.id.text);
        mCallbackText.setText("Not attached");
    }

    private View.OnClickListener mBindListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            doBindService();
        }
    };

    private View.OnClickListener mUnbindListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            doUnbindService();
        }
    };

    private View.OnClickListener mSendMsgListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            doSendMsg();
        }
    };
}
