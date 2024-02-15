package decodev.appstudio.hikerbuddy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.telephony.ServiceState;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class NewHike extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Button btn_start, btn_stop;
    static TextView latTextView, longTextView, distTextView, timerTextView;
    static boolean serviceRunning = false;
    private double distanceTravelled = 0, longitude, latitude;
    private static long totalElapsedTime;
    BroadcastReceiver broadcastReceiver;
    SharedPreferences sharedPreferences;


    public static final String INTENT_FILTER = "IntentFilter";
    public static final String LATITUDE = "latitudeValue";
    public static final String LONGITUDE = "longitudeValue";
    public static final String DISTANCE = "distanceValue";
    public static final String SERVICE_STATUS = "serviceStatus";
    public static final String MyPREFERENCES = "sharedPreferences";
    public static final String HIKE_DISTANCE = "hike distance";
    public static final String HIKE_TIME = "hike time";
    public static final String HIKE_SPEED = "hike speed";
    public static final String TOTAL_DISTANCE = "total distance";
    public static final String TOTAL_TIME = "total time";
    public static final String TOTAL_AVG_SPEED = "total average speed";
    public static final String BEST_DISTANCE = "best distance";
    public static final String BEST_TIME = "best time";
    public static final String BEST_SPEED = "best speed";
    public static final String LAST_KNOWN_LAT = "last known latitude";
    public static final String LAST_KNOWN_LON = "last known longitude";
    ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_hike);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        onNewIntent(getIntent());
        sharedPreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        //Log.i("DEBUGGING","onCreate");

        btn_start = findViewById(R.id.start_button);
        btn_stop = findViewById(R.id.end_button);
        latTextView = findViewById(R.id.latTextView);
        longTextView = findViewById(R.id.lonTextView);
        distTextView = findViewById(R.id.distTextView);
        timerTextView = findViewById(R.id.timerTextView);

        // To get value from shared preferences while service is running and app is opened from app drawer
        if (serviceRunning) {
            latitude = sharedPreferences.getFloat(LATITUDE, 0);
            longitude = sharedPreferences.getFloat(LONGITUDE, 0);
            distanceTravelled = sharedPreferences.getFloat(DISTANCE, 0);
        } else {
            latitude = sharedPreferences.getFloat(LAST_KNOWN_LAT, 0);
            longitude = sharedPreferences.getFloat(LAST_KNOWN_LON, 0);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("DEBUGGING", "onResume");
        setViews();

        if (broadcastReceiver == null) {
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    latitude = (double) intent.getExtras().get(LATITUDE);
                    longitude = (double) intent.getExtras().get(LONGITUDE);
                    distanceTravelled = (double) intent.getExtras().get(DISTANCE);
                    setViews();

                    mMap.clear();
                    LatLng myLocation = new LatLng(latitude, longitude);
                    mMap.addMarker(new MarkerOptions().position(myLocation).title("You are here"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 17f));
                }
            };
            registerReceiver(broadcastReceiver, new IntentFilter(INTENT_FILTER));
        }


        if (!serviceRunning) {
            btn_start.setEnabled(true);
            btn_stop.setEnabled(false);
        } else {
            btn_start.setEnabled(false);
            btn_stop.setEnabled(true);
        }

        // android sdk version 23 or higher require runtime user permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!runtime_permissions()) {
                enable_buttons();
            }
        } else {
            enable_buttons();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Log.i("DEBUGGING","onDestroy");
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Log.i("DEBUGGING","onPause");
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Bundle extras = intent.getExtras();
        if (extras != null) {
            if (extras.containsKey(LATITUDE)) {
                latitude = extras.getDouble(LATITUDE);
            }
            if (extras.containsKey(LONGITUDE)) {
                longitude = extras.getDouble(LONGITUDE);
            }
            if (extras.containsKey(DISTANCE)) {
                distanceTravelled = extras.getDouble(DISTANCE);
            }
            if (extras.containsKey(SERVICE_STATUS)) {
                serviceRunning = extras.getBoolean(SERVICE_STATUS);
            }
        }
    }

    private void enable_buttons() {
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), GPS_Service.class);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NewHike.this.startForegroundService(i);
                } else {
                    startService(i);
                }

                serviceRunning = true;

                btn_start.setEnabled(false);
                btn_stop.setEnabled(true);
            }
        });

        btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), GPS_Service.class);

                serviceRunning = false;
                totalElapsedTime = Parameters.elapsedTime;
                float avgSpeed = (float) (distanceTravelled / totalElapsedTime);
                Parameters.elapsedTime = 0;

                btn_start.setEnabled(true);
                btn_stop.setEnabled(false);

                // To reset live update values
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putFloat(LATITUDE, 0);
                editor.putFloat(LONGITUDE, 0);
                editor.putFloat(DISTANCE, 0);
                editor.putFloat(LAST_KNOWN_LAT, (float) latitude);
                editor.putFloat(LAST_KNOWN_LON, (float) longitude);


                Toast.makeText(NewHike.this, "Distance:" + String.format("%.0f", distanceTravelled) + "m \n"
                        + "Time: " + (--totalElapsedTime) + "s \n"
                        + "Avg Speed: " + String.format("%.0f", avgSpeed) + "m/s", Toast.LENGTH_SHORT).show();

                float totalDistance = sharedPreferences.getFloat(TOTAL_DISTANCE, 0) + (float) distanceTravelled;
                long totalTime = sharedPreferences.getLong(TOTAL_TIME, 0) + totalElapsedTime;

                editor.putLong(TOTAL_TIME, totalTime);
                editor.putFloat(TOTAL_DISTANCE, totalDistance);
                editor.putFloat(TOTAL_AVG_SPEED,(totalDistance/totalTime));

                float bestDistance = sharedPreferences.getFloat(BEST_DISTANCE, 0);
                long bestTime = sharedPreferences.getLong(BEST_TIME, 0);
                float bestSpeed = sharedPreferences.getFloat(BEST_SPEED, 0);

                if (avgSpeed > bestSpeed) {
                    editor.putFloat(BEST_SPEED, avgSpeed);
                }
                if (bestDistance < distanceTravelled) {
                    editor.putFloat(BEST_DISTANCE, (float) distanceTravelled);
                }
                if (bestTime<totalElapsedTime){
                    editor.putLong(BEST_TIME,totalElapsedTime);
                }

                editor.apply();

                stopService(i);
            }
        });

    }

    public void setViews() {
        distTextView.setText(String.format("%.0f", distanceTravelled) + " m");
        longTextView.setText(String.format("%.6f", longitude));
        latTextView.setText(String.format("%.6f", latitude));
    }


    //Requesting Location services from user at runtime
    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean runtime_permissions() {
        if ((Build.VERSION.SDK_INT >= 23) && (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                && (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                != PackageManager.PERMISSION_GRANTED)) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 100);
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED)
                enable_buttons();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                runtime_permissions();
            }
        }
    }

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_new_hike);
//        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
//        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
//                .findFragmentById(R.id.map);
//        mapFragment.getMapAsync(this);
//    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng myLocation = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(myLocation).title("You are here"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 17f));

        if (!serviceRunning) {
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(myLocation).title("Last Known Location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 17f));
        }
    }
}
