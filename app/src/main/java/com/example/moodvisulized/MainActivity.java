package com.example.moodvisulized;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    /* for the button animation */
    private AlphaAnimation buttonClicked = new AlphaAnimation(1F, 0.5F);

    Button fetchSpotifyData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fetchSpotifyData = (Button) findViewById(R.id.fetchSpotifyData);

        fetchSpotifyData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClicked);
                toSpotifyData(v);
            }
        });
    }

    @Override
    protected void onStart() {super.onStart();}

    public void toSpotifyData(View view) {
        Intent intent = new Intent(this, UserStatistics.class);
        startActivity(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
