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
    private NeuralNetwork network = null;

    private void initModelMap() {
        modelMap = new EnumMap<MODEL, Integer>(MODEL.class);
        modelMap.put(MODEL.PONG, R.raw.pong);
        modelMap.put(MODEL.FPS, R.raw.fps);
    }

    public Model(Context base, MODEL m) {
        super(base);
        initModelMap();

        try {
            InputStream dlc = this.getResources().openRawResource(R.raw.pong);
            int size = dlc.available();
            builder = new SNPE.NeuralNetworkBuilder((Application)this.getApplicationContext())
                    .setRuntimeOrder(NeuralNetwork.Runtime.DSP, NeuralNetwork.Runtime.GPU, NeuralNetwork.Runtime.CPU)
                    .setModel(dlc, size);
            network = builder.build();
            Log.i("qace-snpe", "Created SNPE model");
            Toast.makeText(this, "Created SNPE model", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e("qace-snpe", e.toString());
        }
    }

    public void deInit() {
        if (network != null)
            network.release();
        Log.i("qace-snpe", "Release SNPE model");
    }
}
