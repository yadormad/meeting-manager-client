package com.yador.meeting_manager_client;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.yador.meeting_manager_client.model.FacilitatedMeeting;
import com.yador.meeting_manager_client.rest.MeetingsApi;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.yador.meeting_manager_client.Dependency.Dependencies.MEETINGAPI;
import static com.yador.meeting_manager_client.Dependency.Dependencies.MODEL;

public class MeetingsService extends Service {
    private Dependency dependency;
    private static final int NOTIFY_ID = 101;
    private static Timer timer = new Timer();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        dependency = Dependency.getInstance();
        timer.scheduleAtFixedRate(new mainTask(), 0, 5000);
    }

    private class mainTask extends TimerTask
    {
        @Override
        public void run() {
            MeetingsApi meetingsApi = (MeetingsApi) dependency.getDependency(MEETINGAPI);
            if(meetingsApi != null) {
                final Intent intent = new Intent(MeetingActivity.BROADCAST_ACTION);
                Call<List<FacilitatedMeeting>> meetingsCall = meetingsApi.getAllMeetings();
                meetingsCall.enqueue(new Callback<List<FacilitatedMeeting>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<FacilitatedMeeting>> call, @NonNull Response<List<FacilitatedMeeting>> response) {
                        if (response.isSuccessful()) {
                            List<FacilitatedMeeting> update = response.body();
                            List<FacilitatedMeeting> oldModel = (List<FacilitatedMeeting>) dependency.getDependency(MODEL);
                            if(update != null && !update.equals(oldModel)) {
                                dependency.putDependency(MODEL, update);
                                sendBroadcast(intent);
                                addNotification();
                            }

                        } else {
                            Log.w("response code " + response.code(), "response code " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<FacilitatedMeeting>> call, @NonNull Throwable t) {
                        Log.w("failure " + t, "failure " + t);
                    }
                });
            }
        }
    }

    private void addNotification() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this.getApplicationContext(), NOTIFY_ID +"");

        Intent notificationIntent = new Intent(this, MeetingActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
        bigText.bigText("");
        bigText.setBigContentTitle("New meetings");
        bigText.setSummaryText("Updated meetings");
        mBuilder.setContentIntent(contentIntent);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher_round);
        mBuilder.setContentTitle("New meetings");
        mBuilder.setContentText("Updated meetings");
        mBuilder.setPriority(Notification.PRIORITY_MAX);
        mBuilder.setStyle(bigText);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "YOUR_CHANNEL_ID";
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
            mBuilder.setChannelId(channelId);
        }
        manager.notify(0, mBuilder.build());
    }
}
