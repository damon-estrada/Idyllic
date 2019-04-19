package com.example.moodvisulized;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;

// Imported from the QUICKSTART GUIDE
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

// Below are the protocols needed for api calls.
// Use more as needed to accomplish any task

import com.spotify.protocol.types.Track;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

public class MainActivity extends AppCompatActivity {
    private Button loginButton;

    private static final String CLIENT_ID = "0184658057ca400693856a596026419b";
    private static final String REDIRECT_URI = "moodvisualized://callback";
    private SpotifyAppRemote mSpotifyAppRemote;

    // Request code will be used to verify if result comes from the login activity.
    // Can be set to any integer.
    private static final int REQUEST_CODE = 1003;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loginButton = (Button) findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSignInPage();
            }
        });
    }

    public void openSignInPage() {
        // We want to log into the next activity
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // We need to authorize my application to use the App Remote SDK
        // If I do not do this, the program will fail with UserNotAuthorizedException
        // This uses the built-in authorization from the Quick Start Guide
        ConnectionParams connectionParams =
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build();

        // We need to use SpotifyAppRemote.Connector to get an instance of SpotifyAppRemote
        // We do this using SpotifyAppRemote.connect using connectionParams.
        SpotifyAppRemote.connect(this, connectionParams,
                new Connector.ConnectionListener() {

                    @Override
                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        mSpotifyAppRemote = spotifyAppRemote;
                        Log.d("MainActivity", "Connected! Yay!");

                        // Now we can start interacting with App Remote
                        connected();
                    }


                    public void onFailure(Throwable throwable) {
                        // If we get here, something bad happened, figure out what happened.
                        Log.e("MainActivity", throwable.getMessage(), throwable);
                        System.out.println("\n\n\n\nFAILURE TO CONNECT APP REMOTE");
                    }
                });
    }

    private void connected() {
        // Once onConnected in onStart() succeeds, we can now start making requests.

        // For this, lets play a playlist
        mSpotifyAppRemote.getPlayerApi().play("spotify:playlist:37i9dQZF1DX2sUQwD7tbmL");

        // The following code will subscribe to PlayerState
        mSpotifyAppRemote.getPlayerApi()
                .subscribeToPlayerState()
                .setEventCallback(playerState -> {
                    final Track track = playerState.track;
                    if (track != null) {
                        Log.d("MainActivity", track.name + " by " + track.artist.name);
                    }
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        // We shall disconnect from the App Remote when we do not need it anymore.
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        super.onActivityResult(requestCode,resultCode,intent);

        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);

            switch (response.getType()) {
                // Response was successful and contains auth token.
                case TOKEN:
                    //Handle sucessful response
                    break;

                // Auth flow returned an error
                case ERROR:
                    //Handle error response;
                    break;

                default:
                    // Handle other cases here.
            }
        }
    }










}
