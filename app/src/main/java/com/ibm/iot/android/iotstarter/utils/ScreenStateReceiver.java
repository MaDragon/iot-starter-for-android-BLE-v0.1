package com.ibm.iot.android.iotstarter.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ibm.iot.android.iotstarter.IoTStarterApplication;
import com.ibm.iot.android.iotstarter.fragments.IoTPagerFragment;
import com.ibm.iot.android.iotstarter.iot.IoTClient;

import org.eclipse.paho.client.mqttv3.MqttException;

/**
 * Created by malone on 21/09/16.
 */
public class ScreenStateReceiver extends BroadcastReceiver{
//    private final Context context;
//    app = (IoTStarterApplication) Context.getApplicationContext();

private String screenValue="On";
    @Override
    public void onReceive(Context context, Intent intent) {
        String action =intent.getAction();

        if(Intent.ACTION_SCREEN_ON.equals(action)){
            Log.d("Scree","On");
            screenValue="ON";
            String screenData = MessageFactory.getScreen(screenValue);
            Log.d("SSS",screenData);
            try {
                // create ActionListener to handle message published results
                MyIoTActionListener listener = new MyIoTActionListener(context, Constants.ActionStateStatus.PUBLISH);
                IoTClient iotClient = IoTClient.getInstance(context);
                iotClient.publishEvent(Constants.SCREEN_EVENT, "json", screenData, 0, false, listener);
//            int count = app.getPublishCount();
//            app.setPublishCount(++count);
//
//            String runningActivity = app.getCurrentRunningActivity();
//            if (runningActivity != null && runningActivity.equals(IoTPagerFragment.class.getName())) {
//                Intent actionIntent = new Intent(Constants.APP_ID + Constants.INTENT_IOT);
//                actionIntent.putExtra(Constants.INTENT_DATA, Constants.INTENT_DATA_PUBLISHED);
//                context.sendBroadcast(actionIntent);
//            }
            } catch (MqttException e) {
                // Publish failed
            }

//            setScreenData("ON");
        }
        else if(Intent.ACTION_SCREEN_OFF.equals(action)){
            Log.d("Scree","Off");
//            setScreenState("OFF");
            screenValue="OFF";
            String screenData = MessageFactory.getScreen(screenValue);

            try {
                // create ActionListener to handle message published results
                MyIoTActionListener listener = new MyIoTActionListener(context, Constants.ActionStateStatus.PUBLISH);
                IoTClient iotClient = IoTClient.getInstance(context);
                iotClient.publishEvent(Constants.SCREEN_EVENT, "json", screenData, 0, false, listener);
//            int count = app.getPublishCount();
//            app.setPublishCount(++count);
//
//            String runningActivity = app.getCurrentRunningActivity();
//            if (runningActivity != null && runningActivity.equals(IoTPagerFragment.class.getName())) {
//                Intent actionIntent = new Intent(Constants.APP_ID + Constants.INTENT_IOT);
//                actionIntent.putExtra(Constants.INTENT_DATA, Constants.INTENT_DATA_PUBLISHED);
//                context.sendBroadcast(actionIntent);
//            }
            } catch (MqttException e) {
                // Publish failed
            }

        }
        Log.d("SSS",screenValue);


    }
}
