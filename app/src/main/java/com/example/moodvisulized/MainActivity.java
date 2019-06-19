package com.example.moodvisulized;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.VideoView;

import java.net.URI;

public class MainActivity extends AppCompatActivity {

    /* for the button animation */
    private AlphaAnimation buttonClicked = new AlphaAnimation(1F, 0.5F);

    private VideoView mVideoView;

    Button fetchSpotifyData;
    Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // to remove the status bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        fetchSpotifyData = (Button) findViewById(R.id.fetchSpotifyData);

        fetchSpotifyData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClicked);
                toSpotifyData(v);
            }
        });


        mVideoView = (VideoView) findViewById(R.id.loginVideo);

        String path = "android.resource://" + getPackageName() + "/" + R.raw.video;

        try {
            mVideoView.setVideoURI(Uri.parse(path));
        } catch (Exception e) {
            e.printStackTrace();
        }


        mVideoView.start();

        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
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
