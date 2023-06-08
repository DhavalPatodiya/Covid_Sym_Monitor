package com.example.covid_sym_monitor;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.DoubleSummaryStatistics;
import java.util.List;


public class AccelerometerSensorHandlerService extends Service implements SensorEventListener {
    private static final String TAG = "abc";
    private SensorManager sensorManager;
    private Sensor senseAccel;

    List<Float> accelValuesZ = new ArrayList<>();
    Bundle b;


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        //new data
        Sensor mySensor = sensorEvent.sensor;
        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            accelValuesZ.add(sensorEvent.values[2]);

            Log.d(TAG, "Z: " + sensorEvent.values[2]);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //manages sensors
    }



    @Override
    public void onCreate() {
        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senseAccel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, senseAccel, SensorManager.SENSOR_DELAY_NORMAL);
        final SensorEventListener listener = this;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(AccelerometerSensorHandlerService.this ,"Service Stopped", Toast.LENGTH_LONG).show();
                sensorManager.unregisterListener(listener);
                callMeasureRespRate();
                stopSelf();
            }
        }, 45000);
    }

    @SuppressLint("NewApi")
    private void callMeasureRespRate() {

        accelValuesZ.removeIf(val -> val < 0);
        DoubleSummaryStatistics summaryStatistics = accelValuesZ.stream().mapToDouble((a)->a).summaryStatistics();
        double Baseline =   summaryStatistics.getAverage();
        //double Baseline =  sum/accelValuesZ.size();
        double DynamicRangeUp = Collections.max(accelValuesZ,null) - Baseline;
        double DynamicRangeDown = Baseline - Collections.min(accelValuesZ, null);
        double thresholdUp = 0.002 * DynamicRangeUp;
        double thresholdR = 0.05*DynamicRangeUp;
        double thresholdDown = 0.0002*DynamicRangeDown;
        //double thresholdQ = 0.1*DynamicRangeDown;

        int up =1;
        double previousPeak = accelValuesZ.get(1);
        int k = -1;
        float maximum = -1000.0f;
        float minimum = 1000.0f;
        int possiblePeak = 0;
        float Rpeak = 0;
        ArrayList<Integer> Rpeak_index = new ArrayList<>();
        ArrayList<Integer> peak_values = new ArrayList<>();

        List<Integer> peak_index = new ArrayList<>();
        int peak = 0;
        int i =1;
        float peakType = 0;

        while (i<accelValuesZ.size()){
            if(accelValuesZ.get(i)>maximum){
                maximum = accelValuesZ.get(i);
            }
            if (accelValuesZ.get(i)<minimum){
                minimum = accelValuesZ.get(i);
            }

            if(up==1){
                if(accelValuesZ.get(i)<maximum){
                    if(possiblePeak ==0){
                        possiblePeak = 1;
                    }
                    if (accelValuesZ.get(i) < (maximum-thresholdUp)){
                        k = k +1;
                        peak_index.add(  (possiblePeak-1));
                        minimum = accelValuesZ.get(i);
                        up = 0;
                        possiblePeak = 0;
                        if(peakType==0){
                            if(accelValuesZ.get(peak_index.get(k)) > Baseline + thresholdR){
                                peak = peak + 1;
                                // Rpeak_index= [Rpeak_index peak_index(k)];
                                Collections.addAll(Rpeak_index, peak_index.get(k));
                                previousPeak = accelValuesZ.get(peak_index.get(k));

                            }
                            else if(( Math.abs((accelValuesZ.get(peak_index.get(k))) - previousPeak)/previousPeak) > 1.5 && (accelValuesZ.get(peak_index.get(k))>Baseline+thresholdR)) {
                                Rpeak = Rpeak + 1;
                                Collections.addAll(Rpeak_index, peak_index.get(k));
                                previousPeak = accelValuesZ.get(peak_index.get(k));
                                peakType = 2;
                            }
                        }
                    }
                }

            }
            else if(accelValuesZ.get(i) > minimum) {
                if (possiblePeak == 0)
                    possiblePeak = i;
            }
            if(accelValuesZ.get(i) > (minimum + thresholdDown)) {
                k = k + 1;
                peak_index.add( possiblePeak - 1);
                maximum = accelValuesZ.get(i);
                up = 1;
                possiblePeak = 0;
            }
            i = i + 1;
        }
        float ans = peak_index.size()/50;
        Log.d("sfsdf",String.valueOf(ans) );
        sendBroadcast(ans);

    }

    private void sendBroadcast(float ans) {
        Intent intent = new Intent("message");
        intent.putExtra("success", ans);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }


}
