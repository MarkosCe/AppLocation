package com.proyect.location;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class ForegroundService extends Service {

    public final String CHANNEL_ID = "com.proyect.location";
    private final int ID_NOTIFICATION = 50;

    private LocationRequest locationRequest;
    private FusedLocationProviderClient fusedLocationProviderClient;

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null) {
                Log.d("ERROR", "NO HAY NADA");
                return;
            }
            for (Location location : locationResult.getLocations()) {
                updateNotification("Latitud:" + location.getLatitude());
                Log.d("FOREGROUND","OBTENIENDO UBICACION");
            }
        }
    };

    private void startLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setSmallestDisplacement(5);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startLocation();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationCallback != null && fusedLocationProviderClient != null){
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(ID_NOTIFICATION, startMyForegroundService("Tu ubicación"));

        return START_STICKY;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private Notification startMyForegroundService(String texto){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelName = "Service";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH);
            channel.setLightColor(Color.BLUE);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(channel);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
            //Actualizar y corregir
        /*PendingIntent contentIntent = PendingIntent.getActivity(this,
                1, new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);*/
            Notification notification = builder
                    .setOngoing(true)
                    .setSmallIcon(R.drawable.ic_gps)
                    .setContentTitle("GpsActivo")
                    .setContentText(texto)
                    .setPriority(NotificationManager.IMPORTANCE_HIGH)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    //.setContentIntent(contentIntent)
                    .build();
            return notification;
        }else {
            return new NotificationCompat.Builder(this,CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_gps)
                    .setContentTitle("GpsActivo")
                    .setContentText(texto)
                    .setPriority(NotificationManager.IMPORTANCE_HIGH)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .build();
        }
    }

    private void updateNotification(String text) {
        String texto = text;

        Notification notification = startMyForegroundService(texto);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(ID_NOTIFICATION, notification);
    }
}
