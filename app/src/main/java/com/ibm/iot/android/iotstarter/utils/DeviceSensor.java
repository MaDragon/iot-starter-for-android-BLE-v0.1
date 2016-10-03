/*******************************************************************************
 * Copyright (c) 2014-2015 IBM Corp.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *   http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *    Mike Robertson - initial contribution
 *******************************************************************************/
package com.ibm.iot.android.iotstarter.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.Toast;

import com.ibm.iot.android.iotstarter.IoTStarterApplication;
import com.ibm.iot.android.iotstarter.iot.IoTClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.util.Strings;

import java.util.Timer;
import java.util.TimerTask;

import static com.ibm.iot.android.iotstarter.IoTStarterApplication.amplitudeDb;

/**
 * This class implements the SensorEventListener interface. When the application creates the MQTT
 * connection, it registers listeners for the accelerometer and magnetometer sensors.
 * Output from these sensors is used to publish accel event messages.
 */
public class DeviceSensor implements SensorEventListener {
    private final String TAG = DeviceSensor.class.getName();
    private static DeviceSensor instance;
    private final IoTStarterApplication app;
    private final SensorManager sensorManager;
    private final Sensor accelerometer;
    private final Sensor linearAccelerometer;
    private final Sensor magnetometer;

        private String noiseState;

    private final Sensor stepCounter;
    private final Context context;
    private Timer timer;
    private boolean isEnabled = false;



    private float lastStep=0.0f;

    private DeviceSensor(Context context) {
        this.context = context;
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        linearAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        stepCounter=sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        app = (IoTStarterApplication) context.getApplicationContext();

    }

    /**
     * @param context The application context for the object.
     * @return The MqttHandler object for the application.
     */
    public static DeviceSensor getInstance(Context context) {
        if (instance == null) {
            Log.i(DeviceSensor.class.getName(), "Creating new DeviceSensor");
            instance = new DeviceSensor(context);
        }
        return instance;
    }

    /**
     * Register the listeners for the sensors the application is interested in.
     */
    public void enableSensor() {
        Log.i(TAG, ".enableSensor() entered");
        if (!isEnabled) {
//            app.setFfe(3);
            if(app.getFfe()==0){

//                Toast.makeText(app,"111",10000);
//                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
//                sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
            }
            else if(app.getFfe()==1){
//                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
//                sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME);
            }
            else if(app.getFfe()==2){
//                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
//                sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
            }
            else if(app.getFfe()==3){
//                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
//                sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_FASTEST);
            }
            app.setColor(Color.argb(10,10,10,10));
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
            sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
            sensorManager.registerListener(this, linearAccelerometer, SensorManager.SENSOR_DELAY_UI);

            sensorManager.registerListener(this, stepCounter, SensorManager.SENSOR_DELAY_FASTEST);


            timer = new Timer();
            timer.scheduleAtFixedRate(new SendTimerTask(), 1000, 1000);
            isEnabled = true;
        }
    }

    /**
     * Disable the listeners.
     */
    public void disableSensor() {
        Log.d(TAG, ".disableSensor() entered");
        if (timer != null && isEnabled) {
            timer.cancel();
            sensorManager.unregisterListener(this);
            isEnabled = false;
        }
    }

    // Values used for accelerometer, magnetometer, orientation sensor data
    private float[] G = new float[3]; // gravity x,y,z
    private float[] M = new float[3]; // geomagnetic field x,y,z
    private float[] L = new float[3];
    private float step;
    private final float[] R = new float[9]; // rotation matrix
    private final float[] I = new float[9]; // inclination matrix
    private float[] O = new float[3]; // orientation azimuth, pitch, roll
    private float yaw;

    /**
     * Callback for processing data from the registered sensors. Accelerometer and magnetometer
     * data are used together to get orientation data.
     *
     * @param sensorEvent The event containing the sensor data values.
     */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Log.v(TAG, "onSensorChanged() entered");
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            Log.v(TAG, "Accelerometer -- x: " + sensorEvent.values[0] + " y: "
                    + sensorEvent.values[1] + " z: " + sensorEvent.values[2]);
            G = sensorEvent.values;

        } else if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            Log.v(TAG, "Magnetometer -- x: " + sensorEvent.values[0] + " y: "
                    + sensorEvent.values[1] + " z: " + sensorEvent.values[2]);
            M = sensorEvent.values;
        }
        else if (sensorEvent.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            Log.v(TAG, "StepCounter -- x: " + sensorEvent.values[0]);

            step += sensorEvent.values[0];




        }else if (sensorEvent.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            Log.v(TAG, "Linear -- x: " + sensorEvent.values[0]);
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];
            L = sensorEvent.values;

            float diff = (float) Math.sqrt(x * x + y * y + z * z);
            if (diff > 0.5) // 0.5 is a threshold, you can test it and change it
            {
                app.setStateDeviceMovement("MOVING");
            }
            else{
                app.setStateDeviceMovement("STILL");
            }
            Log.d("statemoving","Device motion detected!!!!"+app.getStateDeviceMovement());

        }
        if(app.getStateDeviceMovement()!=null){
            Log.d("ssd","device;+"+app.getStateDeviceMovement());
            float currentStep=step;
            if(app.getStateDeviceMovement()=="MOVING"&&lastStep!=currentStep){
                app.setStateUserMovement("UserMoving");
                Log.d("ssd","device;111+"+app.getStateDeviceMovement()+"!!!"+lastStep+"222"+currentStep+" 33 "+app.getStateUserMovement());
                lastStep=step;

            }else{
                app.setStateUserMovement("UserStill");
                Log.d("ssd","device;222+"+app.getStateDeviceMovement()+"!!!"+lastStep+"222"+currentStep);

            }
            Log.d("ssd","device;333+"+app.getStateDeviceMovement()+"!!!"+lastStep+"222"+currentStep);

        }
        if (G != null && M != null) {
            if (SensorManager.getRotationMatrix(R, I, G, M)) {
                float[] previousO = O.clone();
                O = SensorManager.getOrientation(R, O);
                yaw = O[0] - previousO[0];
                Log.v(TAG, "Orientation: azimuth: " + O[0] + " pitch: " + O[1] + " roll: " + O[2] + " yaw: " + yaw);
            }
        }
    }

    /**
     * Callback for the SensorEventListener interface. Unused.
     *
     * @param sensor The sensor that changed.
     * @param i The change in accuracy for the sensor.
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        Log.d(TAG, "onAccuracyChanged() entered");
    }

    /**
     * Timer task for sending accel data on 1000ms intervals
     */
    private class SendTimerTask extends TimerTask {

        /**
         * Publish an accel event message.
         */
        @Override
        public void run() {
            Log.v(TAG, "SendTimerTask.run() entered");

            double lon = 0.0;
            double lat = 0.0;
            if (app.getCurrentLocation() != null) {
                lon = app.getCurrentLocation().getLongitude();
                lat = app.getCurrentLocation().getLatitude();
            }
            String messageData = MessageFactory.getAccelMessage(G, O, yaw, lon, lat);
            String stepData=MessageFactory.getStep(step,app.getStateDeviceMovement(),app.getStateUserMovement());
            if(amplitudeDb>60.0d){
                noiseState="Noisy";
            }
            else{
                noiseState="Quite";
            }
            String noiseLvl=MessageFactory.getNoise(amplitudeDb,noiseState);
//            String screenData=MessageFactory.getScreen(screen);
//            app.getFfe();
            try {
                // create ActionListener to handle message published results
                MyIoTActionListener listener = new MyIoTActionListener(context, Constants.ActionStateStatus.PUBLISH);
                IoTClient iotClient = IoTClient.getInstance(context);
                if (app.getConnectionType() == Constants.ConnectionType.QUICKSTART) {
                    iotClient.publishEvent(Constants.STATUS_EVENT, "json", messageData, 0, false, listener);
                } else {
                    iotClient.publishEvent(Constants.ACCEL_EVENT, "json", messageData, 0, false, listener);
                    iotClient.publishEvent(Constants.STEP_EVENT, "json", stepData, 0, false, listener);

                    iotClient.publishEvent(Constants.NOISE_EVENT, "json", noiseLvl, 0, false, listener);

//                    iotClient.publishEvent(Constants.SCREEN_EVENT, "json", screenData, 0, false, listener);


                }

                int count = app.getPublishCount();
                app.setPublishCount(++count);

                //String runningActivity = app.getCurrentRunningActivity();
                //if (runningActivity != null && runningActivity.equals(IoTPagerFragment.class.getName())) {
                    Intent actionIntent = new Intent(Constants.APP_ID + Constants.INTENT_IOT);
                    actionIntent.putExtra(Constants.INTENT_DATA, Constants.INTENT_DATA_PUBLISHED);
                    context.sendBroadcast(actionIntent);
                //}
            } catch (MqttException e) {
                Log.d(TAG, ".run() received exception on publishEvent()");
            }

            app.setAccelData(G);
            app.setStepNum(step);

            //String runningActivity = app.getCurrentRunningActivity();
            //if (runningActivity != null && runningActivity.equals(IoTPagerFragment.class.getName())) {
                Intent actionIntent = new Intent(Constants.APP_ID + Constants.INTENT_IOT);
                actionIntent.putExtra(Constants.INTENT_DATA, Constants.ACCEL_EVENT);
                context.sendBroadcast(actionIntent);
            //}
        }
    }
}
