package com.proyect.location;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
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

    Handler handler = new Handler();
    String latitud = "";
    private LocationRequest locationRequest;
    private FusedLocationProviderClient fusedLocationProviderClient;

    /*Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Log.d("SERVICE", "temporizador");
            handler.postDelayed(runnable, 1000);
        }
    };*/

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null) {
                //Toast.makeText(ForegroundService.this, "no hay nada", Toast.LENGTH_SHORT).show();
                Log.d("ERROR", "NO HAY NADA");
                return;
            }
            for (Location location : locationResult.getLocations()) {
                //Toast.makeText(ForegroundService.this, "hay algo", Toast.LENGTH_SHORT).show();
                //textViewUbicacion.setText("Lat: " + location.getLatitude());
                //latitud = location.getLatitude() + "";
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
        //handler.postDelayed(runnable, 1000);
        startLocation();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        /*if (handler != null){
            handler.removeCallbacks(runnable);
        }*/
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

        Notification notification = new NotificationCompat.Builder(this,CHANNEL_ID)
                                    .setSmallIcon(R.drawable.ic_gps)
                                    .setContentTitle("GpsActivo")
                                    .setContentText("holaa")
                                    .setPriority(NotificationManager.IMPORTANCE_HIGH)
                                    .setCategory(Notification.CATEGORY_SERVICE)
                                    .build();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            startMyForegroundService();
        }else {
            startForeground(50, notification);
        }

        return START_STICKY;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startMyForegroundService(){
        //String NOTIFICATION_CHANNEL_ID = "com.proyect.location";
        String channelName = "Service";
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH);
        channel.setLightColor(Color.BLUE);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(channel);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        Notification notification = builder
                                    .setOngoing(true)
                                    .setSmallIcon(R.drawable.ic_gps)
                                    .setContentTitle("GpsActivo")
                                    .setContentText("Hola")
                                    .setPriority(NotificationManager.IMPORTANCE_HIGH)
                                    .setCategory(Notification.CATEGORY_SERVICE)
                                    .build();
        startForeground(50, notification);
    }
}
