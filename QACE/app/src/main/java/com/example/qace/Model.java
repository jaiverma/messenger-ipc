package com.example.qace;

import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.ApplicationInfo;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import com.qualcomm.qti.snpe.NeuralNetwork;
import com.qualcomm.qti.snpe.SNPE;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;

public class Model extends ContextWrapper {
    public enum MODEL {
        PONG,
        FPS
    };
    EnumMap<MODEL, Integer> modelMap;
    private SNPE.NeuralNetworkBuilder builder;
    private NeuralNetwork network;

    private void initModelMap() {
        modelMap = new EnumMap<MODEL, Integer>(MODEL.class);
        modelMap.put(MODEL.PONG, R.raw.pong);
        modelMap.put(MODEL.FPS, R.raw.fps);
    }

    public Model(Context base, MODEL m) {
        super(base);
        Log.i("hathi", "" + this.getExternalFilesDir(null));
        initModelMap();

//        AssetFileDescriptor fd = this.getResources().openRawResourceFd(R.raw.pong);
        try {
//            InputStream dlc = fd.createInputStream();
//            long size = fd.getLength();
            InputStream dlc = this.getResources().openRawResource(R.raw.pong);
            int size = dlc.available();
            builder = new SNPE.NeuralNetworkBuilder((Application)this.getApplicationContext())
                    .setRuntimeOrder(NeuralNetwork.Runtime.DSP, NeuralNetwork.Runtime.GPU, NeuralNetwork.Runtime.CPU)
                    .setModel(dlc, size);
            network = builder.build();
            Log.i("hathi", "Created SNPE model");
            Toast.makeText(this, "Created SNPE model", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e("hathi", e.toString());
        }
    }
}
