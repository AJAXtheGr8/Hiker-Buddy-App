package decodev.appstudio.hikerbuddy;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.Random;


public class GPS_Service extends Service{

    LocationListener listener;
    LocationManager locationManager;
    Parameters parameters = new Parameters();
    Notification notification;
    NotificationCompat.Builder notificationBuilder;
    PendingIntent pendingIntent;
    Intent notificationIntent;
    SharedPreferences sharedPreferences;
    NotificationManager notificationManager;

    public static final String NOTIFICATION_CHANNEL_ID = "HikerBuddyNotification";
    public static final int NOTIFICATION_ID = 1;
    public static final String INTENT_FILTER = "IntentFilter";
    public static final String LATITUDE = "latitudeValue";
    public static final String LONGITUDE = "longitudeValue";
    public static final String DISTANCE = "distanceValue";
    public static final String SERVICE_STATUS = "serviceStatus";
    public static final String MyPREFERENCES = "sharedPreferences";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        notificationBuilder = new NotificationCompat.Builder(this,NOTIFICATION_CHANNEL_ID);
        sharedPreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);



        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                parameters.CalculateValues(location);
                buildNotification();
                notificationManager.notify(NOTIFICATION_ID,notification);

                // put intent filter to broadcast values to the app live
                Intent intent = new Intent(INTENT_FILTER);
                intent.putExtra(LATITUDE,parameters.getLatitude());
                intent.putExtra(LONGITUDE,parameters.getLongitude());
                intent.putExtra(DISTANCE,parameters.getDistanceTravelled());
                sendBroadcast(intent);

                // To update data on startup of app from the app drawer while service is running
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putFloat(LATITUDE,(float)parameters.getLatitude());
                editor.putFloat(LONGITUDE,(float)parameters.getLongitude());
                editor.putFloat(DISTANCE,(float)parameters.getDistanceTravelled());
                editor.apply();

                //Toast.makeText(GPS_Service.this, Parameters.elapsedTime+" seconds", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                //Opens the settings to enable location
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        };

        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        // no permission inspection required as it is done in the main activity
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,25,listener);

        Log.i("SERVICE_LIFECYCLE:","onCreate");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        buildNotification();
        startForeground(NOTIFICATION_ID,notification);
        Log.i("SERVICE_LIFECYCLE:","onStartCommand");
        return START_NOT_STICKY;
    }

    private void createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "Live Updates",
                    NotificationManager.IMPORTANCE_LOW
            );

            notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    public void buildNotification(){

        //Intent to start main activity upon clicking the notification
        notificationIntent = new Intent(this,NewHike.class);
        Bundle bundle = new Bundle();
        //for sending updated value to the activity when it is opened from notification
        bundle.putDouble(LATITUDE,parameters.getLatitude());
        bundle.putDouble(LONGITUDE,parameters.getLongitude());
        bundle.putDouble(DISTANCE,parameters.getDistanceTravelled());
        bundle.putBoolean(SERVICE_STATUS,true);
        notificationIntent.putExtras(bundle);

        pendingIntent = PendingIntent.getActivity(this, new Random().nextInt(),notificationIntent,0);

        notificationBuilder.setContentTitle("Hiker Buddy Live Updates");
        notificationBuilder.setContentText("DIST: " + String.format("%.0f", parameters.getDistanceTravelled()) + "m   "
                + "LAT:" + String.format("%.6f", parameters.getLatitude())
                + ", LON:" + String.format("%.6f", parameters.getLongitude()));
        notificationBuilder.setSmallIcon(R.drawable.ic_location);
        notificationBuilder.setContentIntent(pendingIntent);

        notification = notificationBuilder.build();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        parameters.stopTimer();

        if(locationManager!=null) {
            locationManager.removeUpdates(listener);
        }

        notificationManager.cancel(NOTIFICATION_ID);
        Log.i("SERVICE_LIFECYCLE:","onDestroy");
    }


}
