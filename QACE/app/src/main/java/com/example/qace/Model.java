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

import com.qualcomm.qti.snpe.FloatTensor;
import com.qualcomm.qti.snpe.NeuralNetwork;
import com.qualcomm.qti.snpe.SNPE;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

    public float[] zNormalise(float[] f)
    {
        double sum = 0.0f, standardDeviation = 0.0f;
        int length = f.length;
        for(float num : f) {
            sum += num;
        }
        double mean = sum/length;
        for(float num: f) {
            standardDeviation += Math.pow(num - mean, 2);
        }
        standardDeviation = Math.sqrt(standardDeviation/length);

        for (int i = 0; i < f.length; i++) {
            f[i] = (float)((f[i] - mean) / standardDeviation);
        }
        return f;
    }

    public FloatTensor createFloatTensor(float[] data) {
        data = zNormalise(data);
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < data.length; i++) {
            sb.append(data[i]);
            sb.append(", ");
        }
        sb.append("]");
        Log.i("qace-snpe", sb.toString());
        FloatTensor tensor = network.createFloatTensor(1, data.length, 1, 1);
        tensor.write(data, 0, data.length);
        return tensor;
    }

    public boolean detectCheat(FloatTensor tensor) {
        final Map<String, FloatTensor> inputsMap = new HashMap<>();
        Set<String> inputNames = network.getInputTensorsNames();
        if (inputNames.size() > 1) throw new AssertionError();
        inputsMap.put("input_1:0", tensor);
        boolean isCheat = false;
        final Map<String, FloatTensor> outputsMap = network.execute(inputsMap);
        int count = 0;
        for (Map.Entry<String, FloatTensor> output : outputsMap.entrySet()) {
            final FloatTensor outTensor = output.getValue();
            final float[] values = new float[outTensor.getSize()];
            outTensor.read(values, 0, values.length);
            isCheat = values[0] < values[1];
            if (values.length != 2) throw new AssertionError();
            count++;
        }
        if (count != 1) throw new AssertionError();
        for (FloatTensor t : inputsMap.values())
            t.release();
        for (FloatTensor t : outputsMap.values())
            t.release();
        return isCheat;
    }

    public void deInit() {
        if (network != null)
            network.release();
        Log.i("qace-snpe", "Release SNPE model");
    }
}
