package com.example.moodvisulized;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

// Below are the protocols needed for api calls.
// Use more as needed to accomplish any task


public class MainActivity extends AppCompatActivity {

    private Button fetchSpotifyData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        fetchSpotifyData = (Button) findViewById(R.id.fetchSpotifyData);

        fetchSpotifyData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toSpotifyData(v);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public void toSpotifyData(View view) {
        Intent intent = new Intent(this, UserStatistics.class);
        startActivity(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
