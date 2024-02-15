package decodev.appstudio.hikerbuddy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

public class Statistics extends AppCompatActivity {


    public static final String TOTAL_DISTANCE = "total distance";
    public static final String TOTAL_TIME = "total time";
    public static final String TOTAL_AVG_SPEED = "total average speed";
    public static final String BEST_DISTANCE = "best distance";
    public static final String BEST_TIME = "best time";
    public static final String BEST_SPEED = "best speed";
    public static final String MyPREFERENCES = "sharedPreferences";

    TextView bestDist,bestTime,bestSpeed,totalDist,totalTime,totalSpeed;
    SharedPreferences sharedPreferences;
    float bD,bS,tD,tS;
    long bT,tT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        sharedPreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        bestDist = findViewById(R.id.bestDistTextView);
        bestSpeed = findViewById(R.id.bestSpeedTextView);
        bestTime = findViewById(R.id.bestTimeView);
        totalDist = findViewById(R.id.totalDistTextView);
        totalSpeed = findViewById(R.id.totalSpeedTextView);
        totalTime = findViewById(R.id.totalTimeTextView);



        setViews();
    }

    public void setViews(){

        bD = sharedPreferences.getFloat(BEST_DISTANCE,0);
        bS = sharedPreferences.getFloat(BEST_SPEED,0);
        bT = sharedPreferences.getLong(BEST_TIME,0);

        tD = sharedPreferences.getFloat(TOTAL_DISTANCE,0);
        tS = sharedPreferences.getFloat(TOTAL_AVG_SPEED,0);
        tT = sharedPreferences.getLong(TOTAL_TIME,0);

        bestDist.setText("DISTANCE:\n"+String.format("%.0f",bD)+" m");
        bestTime.setText("TIME:\n"+(bT/3600)+" hrs");
        bestSpeed.setText("AVERAGE\nSPEED:\n"+String.format("%.0f",bS)+" m/s");

        totalDist.setText("DISTANCE:\n"+String.format("%.0f",tD)+" m");
        totalTime.setText("TIME:\n"+(tT/3600)+" hrs");
        totalSpeed.setText("AVERAGE\nSPEED:\n"+String.format("%.0f",tS)+" m/s");
    }


}
