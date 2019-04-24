package com.example.moodvisulized;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

// Imported from the QUICKSTART GUIDE
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

// Below are the protocols needed for api calls.
// Use more as needed to accomplish any task

import com.spotify.protocol.types.Item;
import com.spotify.protocol.types.Track;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyCallback;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.SavedTrack;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit2.Call;
import retrofit2.Callback;

public class MainActivity extends AppCompatActivity implements Item {

    private Button fetchSpotifyData;
    private static final String CLIENT_ID = "0184658057ca400693856a596026419b";
    private static final String REDIRECT_URI = "moodvisualized://callback";

    // Request code will be used to verify if result comes from the login activity.
    // Can be set to any integer.

    private SpotifyAppRemote mSpotifyAppRemote;

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

    public void toSpotifyData(View view) {
        // Do something in response to button
        // This constructor takes in :
        // this - this activity
        // class - where the intent should delvier to
        Intent intent = new Intent(this, UserStatistics.class);
        startActivity(intent);
    }



    @Override
    protected void onStart() {
        super.onStart();
/*
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
        // We want to log into the next activity
        // We need to authorize my application to use the App Remote SDK
        // If I do not do this, the program will fail with UserNotAuthorizedException
        // This uses the built-in authorization from the Quick Start Guide
        ConnectionParams mConnectionParams =
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build();

        // We need to use SpotifyAppRemote.Connector to get an instance of SpotifyAppRemote
        // We do this using SpotifyAppRemote.connect using connectionParams.
        SpotifyAppRemote.connect(this, mConnectionParams,
                new Connector.ConnectionListener() {

                    @Override
                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        mSpotifyAppRemote = spotifyAppRemote;
                        Log.d("MainActivity", "\n\nConnected! Yay!\n\n");

                        // Now we can start interacting with App Remote
                        connected();
                    }


                    public void onFailure(Throwable throwable) {
                        // If we get here, something bad happened, figure out what happened.
                        Log.e("MainActivity", throwable.getMessage(), throwable);
                        System.out.println("\n\n\n\nFAILURE TO CONNECT APP REMOTE");
                    }
                });
                */
    }

    private void connected() {
        // Once onConnected in onStart() succeeds, we can now start making requests.

        // THIS IS HOW WE GET THE CURRENT TRACK NAME AND BY ARTIST
        mSpotifyAppRemote.getPlayerApi()
                .subscribeToPlayerState()
                .setEventCallback(playerState -> {
                    final Track track = playerState.track;
                    if (track != null) {
                        Log.d("Track: ", track.name + " by " + track.artist.name);
                        Log.d("URI: ", track.uri);
                        Log.d("Duration",  Long.toString(track.duration));
                    }
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        // We shall disconnect from the App Remote when we do not need it anymore.
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }
}
