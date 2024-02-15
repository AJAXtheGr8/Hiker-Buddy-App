package decodev.appstudio.hikerbuddy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button newHike;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        newHike = findViewById(R.id.newHikeButton);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(NewHike.serviceRunning==true){
            newHike.setText("Continue Hike");
        }
    }

    //New Hike onClick method to open map activity
    public void openNewHikeActivity(View view) {
        Intent intent = new Intent(this,NewHike.class);
        startActivity(intent);

    }


    public void openStatisticsActivity(View view) {
        Intent intent = new Intent(this,Statistics.class);
        startActivity(intent);
    }

}
