package com.example.moodvisulized;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.VideoView;

public class MainActivity extends AppCompatActivity {

    /* For the button animation */
    private AlphaAnimation buttonClicked = new AlphaAnimation(1F, 0.5F);

    VideoView mVideoView;
    Button connectToSpotify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* To remove the status bar */
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        connectToSpotify = (Button) findViewById(R.id.connectToSpotify);

        connectToSpotify.setOnClickListener(new View.OnClickListener() {
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

    @Override
    protected void onStop() {
        super.onStop();
        releasePlayer();
    }

    /**
     * Onward to the the next activity which is the home screen.
     * @param view  Refering to the next activity view
     */
    public void toSpotifyData(View view) {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }


    /**
     * This is where the looping video is created at the login of the application.
     * Currently, This works if the uer's api is at or above 26.
     * TODO: Work on a solution to continue background music when the application
     *      is launched instead of the audioless video stopping all audio.
     */
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
