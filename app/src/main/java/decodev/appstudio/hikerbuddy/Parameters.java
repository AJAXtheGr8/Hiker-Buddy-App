package decodev.appstudio.hikerbuddy;


import android.location.Location;
import android.os.Handler;

import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;

public class Parameters {
    static Handler handler = new Handler();
    static long elapsedTime = 0 ;
    static boolean timerIsRunning = false;

    private Queue<Location> locations = new LinkedList<>();
    private double distanceTravelled = 0,longitude,latitude;




    public  void CalculateValues(Location location){

        //to start timer only once when updating the views on mainActivity
        if(timerIsRunning==false){
            runTimer();
            timerIsRunning=true;
        }

        locations.add(location);

        longitude = location.getLongitude();
        latitude = location.getLatitude();


        int locationsQueueSize = locations.size();
        Location loc1 = location,loc2=location;

        if(locationsQueueSize >=2){

            loc1 = locations.poll();
            loc2 = locations.peek();

        }

        distanceTravelled +=  loc1.distanceTo(loc2);

    }


    public double getDistanceTravelled(){
        return distanceTravelled;
    }

    public double getLatitude(){
        return latitude;
    }

    public double getLongitude(){
        return  longitude;
    }


    public void runTimer()
    {
        timer.run();
    }

    public static void stopTimer(){
        handler.removeCallbacks(timer);
        timerIsRunning = false;
    }

    private static Runnable timer = new Runnable() {
        @Override
        public void run() {
            long hours = elapsedTime / 3600;
            long minutes = (elapsedTime % 3600) / 60;
            long secs = elapsedTime % 60;


            String time = String.format(Locale.getDefault(),
                    "%02d:%02d:%02d", hours,
                    minutes, secs);


            NewHike.timerTextView.setText(time);


            if (timerIsRunning) {
                elapsedTime++;
            }

            handler.postDelayed(this, 1000);
        }
    };
}
