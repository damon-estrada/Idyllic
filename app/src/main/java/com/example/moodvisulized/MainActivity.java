package com.example.moodvisulized;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.VideoView;

import com.spotify.android.appremote.api.ContentApi;

import java.net.URI;

public class MainActivity extends AppCompatActivity {

    /* for the button animation */
    private AlphaAnimation buttonClicked = new AlphaAnimation(1F, 0.5F);

    VideoView mVideoView;
    MediaPlayer mMediaPlayer = null;

    Button fetchSpotifyData;

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

        try {
            /* video view stops audio player (spotify), this will prevent that */
            initPlayer();

        } catch (Exception e) {
            e.printStackTrace();
        }



        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        initPlayer();
    }

    public void toSpotifyData(View view) {
        Intent intent = new Intent(this, UserStatistics.class);
        startActivity(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        releasePlayer();
    }


    private void initPlayer() {
        /* Path to the video */
        String path = "android.resource://" + getPackageName() + "/" + R.raw.video;

        mVideoView.setVideoURI(Uri.parse(path));

        /* If phone api > 26, we can use this to allow audio to remain */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mVideoView.setAudioFocusRequest(AudioManager.AUDIOFOCUS_NONE);
        }
        mVideoView.start();
    }
    private void releasePlayer() {
        mVideoView.stopPlayback();
    }
}
