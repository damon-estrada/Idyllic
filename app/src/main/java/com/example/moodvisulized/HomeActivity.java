package com.example.moodvisulized;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.types.Image;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.AudioFeaturesTrack;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;

public class HomeActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1094;
    private static final String CLIENT_ID = "0184658057ca400693856a596026419b";
    private static final String REDIRECT_URI = "com.example.moodvisualized://callback";

    /* The UI variables */
    ArrayList<TextView> uiElements = new ArrayList<>(); // Each UI variable but represented as num.
    Bitmap coverArtImg;                                // Current track cover art.
    String coverArtUrl;                               // The url for the CoverArt

    ImageView currentTrackArtist;
    ImageView backgroundBanner2;
    ImageButton currentTrackArrow;
    ImageView userPicture;

    /* The variables for holding essential information */
    private String currentTrackUri = "";         // The track Id for the current song playing
    private String currentArtistUri = "";       // The current artist Uri
    private String previousTrackUri = "";      // This is to reduce API calls if same song is play.
    private String tmpTrackUri;               // To aid the process.
    AudioFeaturesTrack check;

    /* Variables needed for service to spotify calls or access to app-remote */
    private ConnectionParams connectionParams;   // Connection Params for the app remote
    private SpotifyAppRemote mSpotifyAppRemote; // The app remote
    private SpotifyService spotify;            // Service variable
    private String accessToken = null;        // Needed to make calls that require access token.

    /* Debugging purposes */
    private static final String TAG = HomeActivity.class.getSimpleName();

    /* Objects */
    CurrentPlaying curTrack = null;
    CurrentPlaying receivedObj = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // to remove the status bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_home);

        currentTrackArtist = (ImageView) findViewById(R.id.currentlyPlayingArtist);
        backgroundBanner2 = (ImageView) findViewById(R.id.allTimeFavoriteTrackBanner);
        currentTrackArrow = (ImageButton) findViewById(R.id.currentTrackPlayingArrow);
        userPicture = (ImageView) findViewById(R.id.userPicture);

        AuthenticationRequest.Builder builder =
                new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);

        /* IMPORTANT, YOU NEED TO ADD TO THE SCOPES IF YOU WANT TO ACCESS MORE INFO!!!! */
        builder.setScopes(new String[]{
                "streaming",                        // Controls the playback of a Spotify track.
                "user-read-private",               // Read access to user's subscription details.
                "user-read-recently-played",      // Read access to a user's recently played tracks.
                "user-top-read",                 // Read access to a user's top artists/tracks.
                "user-library-read",            // Permission to see user's library.
                "user-read-currently-playing", // Permission to see what user is currently playing.
                "user-read-playback-state",   // To read information on playerState
                "app-remote-control"
        });
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);

        /* Get access to spotify services */
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(SpotifyApi.SPOTIFY_WEB_API_ENDPOINT)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setRequestInterceptor(new RequestInterceptor() {
                    @Override
                    public void intercept(RequestFacade request) {
                        request.addHeader("Authorization", "Bearer " + accessToken);
                    }
                })
                .build();

        /* For mAppRemoteSet the connection parameters */
        connectionParams = new ConnectionParams.Builder(CLIENT_ID)
                .setRedirectUri(REDIRECT_URI)
                .showAuthView(true)
                .build();

        /* If we made it here we are good to go! Start getting service. */
        spotify = restAdapter.create(SpotifyService.class);

        /* Store all the audio features ui elements in an array list to parse over later */
        uiElements.add(findViewById(R.id.danceabilityNum));
        uiElements.add(findViewById(R.id.livenessNum));
        uiElements.add(findViewById(R.id.valenceNum));
        uiElements.add(findViewById(R.id.speechinessNum));
        uiElements.add(findViewById(R.id.instrumentalnessNum));

        uiElements.add(findViewById(R.id.loudnessNum));
        uiElements.add(findViewById(R.id.keyNum));
        uiElements.add(findViewById(R.id.energyNum));
        uiElements.add(findViewById(R.id.tempoNum));
        uiElements.add(findViewById(R.id.acousticnessNum));

        /* implement a small delay to prevent the user from not allowing a full fetch */
        currentTrackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toTrackAudioFeatures(v);
            }
        });

        /* Let us see if we receive anything first */

        try {
            Log.d(TAG, "onCreate: Second and more run through");
            Intent i = getIntent();
            receivedObj = (CurrentPlaying) i.getSerializableExtra("object");

            /* If we reach this, then we received something */
            curTrack = new CurrentPlaying(receivedObj);
        } catch (Exception e) {

            /* If we get here, then we are in the first run through of the app */
            Log.d(TAG, "onCreate: First run through");
            curTrack = new CurrentPlaying();
            curTrack.trackUri = "init";
        }
    }

    @Override
    protected void onStart() {

        super.onStart();

        /* To prevent multiple instances of app remote being active. */
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);

        /* Connect to SpotifyAppRemote */
        SpotifyAppRemote.connect(this, connectionParams, new Connector.ConnectionListener() {
            @Override
            public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                mSpotifyAppRemote = spotifyAppRemote;
                Log.d(TAG, "App remote Connected!");
                Runnable onConnected = new Runnable() {
                    @Override
                    public void run() {
                        connected();
                    }
                };
                Thread launch = new Thread(onConnected);
                launch.start();
            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.e(TAG, throwable.getMessage(), throwable);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();

        /* When app not in use, disconnect.
         * We need to disconnect to prevent the HomeActivity from thinking the same song
         * is plying when in reality a new song updated if we are in UserStatistics Activity
         * when the new song comes. Disconnecting helps prevent this error
         */
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * Onward to the the next activity which is the Track stats screen.
     * @param view  Refering to the next activity view
     */
    public void toTrackAudioFeatures(View view) {
        /* I want to send the uiValues and coverart to UserStatistcs */
        Intent intent = new Intent(this, UserStatistics.class);

        Log.d(TAG, "toTrackAudioFeatures: Activity sending Object with data");

        /* Set the coverArtUrl here to ensure we got it */
        curTrack.coverArtUrl = coverArtUrl;

        /* The object being sent to UserStatistics Activity */
        intent.putExtra("curTrackObj", curTrack);

        startActivity(intent);
    }

    /**
     * This method will subscribe to the player state for future use
     */
    public void connected() {
        /* Subscribe to the PlayerState */
        mSpotifyAppRemote.getPlayerApi()
                .subscribeToPlayerState()
                .setEventCallback(playerState -> {

                    /* ATTEMPTING TO CACHE THINGS HERE */
                    /* We need to check if the received obj 1st param is the same as the current
                       fetch. If so, do not make another api call since the same song is playing.
                       We will make a quick check here to ensure it is a new song playing.
                     */

                    /* Get current track uri */
                    currentTrackUri = playerState.track.uri;

                    /* Get the uri, done twice since two ':' are present */
                    currentTrackUri = currentTrackUri.substring(currentTrackUri.indexOf(":") + 1);
                    currentTrackUri = currentTrackUri.substring(currentTrackUri.indexOf(":") + 1);

                    Log.d(TAG, "connected: playerState track uri: " + "spotify:track:" + currentTrackUri);
                    Log.d(TAG, "connected: curTrack track uri: " + curTrack.trackUri);

                    /* So if the track uri are not the same, make api call; new song playing */
                    if (!("spotify:track:" + currentTrackUri).equals(curTrack.trackUri)) {
                        Log.d(TAG, "connected: API CALL; NEW SONG");

                        /* get current artist playing */
                        currentArtistUri = playerState.track.artist.uri;
                        currentArtistUri = currentArtistUri.
                                substring(currentArtistUri.indexOf(":") + 1);
                        currentArtistUri = currentArtistUri.
                                substring(currentArtistUri.indexOf(":") + 1);
                        Log.d(TAG, "connected ASSIGNING CURRENT ARTIST URI: " + currentArtistUri);

                        coverArtUrl = playerState.track.imageUri.toString();

                        /* Remove the parts to reach the bare url to the img */
                        coverArtUrl = coverArtUrl.substring(coverArtUrl.indexOf(":") + 1);
                        coverArtUrl = coverArtUrl.substring(coverArtUrl.indexOf(":") + 1);
                        coverArtUrl = coverArtUrl.substring(0, coverArtUrl.indexOf("\'"));

                        /* Get the cover art of the current playing song */
                        mSpotifyAppRemote.getImagesApi()
                                .getImage(playerState.track.imageUri, Image.Dimension.LARGE)
                                .setResultCallback(bitmap -> {
                                    coverArtImg = bitmap;
                                });

                        /* grab current track features and update the UI with these details */
                        beginAsyncTask();
                        fetchProfilePic();

                    } else {
                    /* else, return the same obj back from the UserStatistics activity */
                       coverArtUrl = curTrack.coverArtUrl;
                       Log.d(TAG, "connected: else condition (same song playing): ");
                    }
                });
    }

    /**
     * Get audio analysis for the song that is playing right now.
     * @return A CurrentPlaying object that will house all the information needed.
     */
    public CurrentPlaying getTrackAudioFeatures() {
        int retryCount = 0;
        int maxAttempt = 3;

        while (true) {
            try {
                AudioFeaturesTrack trackAudioFeatures = spotify.getTrackAudioFeatures(currentTrackUri);

                /* Create a new object */
                Log.d(TAG, "connected: NEW SONG IS PLAYING");
                curTrack = new CurrentPlaying(
                        trackAudioFeatures.danceability,
                        trackAudioFeatures.liveness,
                        trackAudioFeatures.valence,
                        trackAudioFeatures.speechiness,
                        trackAudioFeatures.instrumentalness,

                        trackAudioFeatures.loudness,
                        trackAudioFeatures.key,
                        trackAudioFeatures.energy,
                        trackAudioFeatures.tempo,
                        trackAudioFeatures.acousticness,
                        trackAudioFeatures.duration_ms,
                        trackAudioFeatures.uri,
                        ""
                );

                break;

            } catch (RetrofitError e) {
                if (retryCount == maxAttempt) {
                    e.getBody();
                    e.getResponse();
                } else {retryCount++;}
            }
        }
        return curTrack;
    }

    public void beginAsyncTask() {
            ExampleAsyncTask task = new ExampleAsyncTask(this);
            task.execute();
    }

    private static class ExampleAsyncTask extends AsyncTask<Void, TextView, CurrentPlaying> {
        private WeakReference<HomeActivity> activityWeakRef;

        ExampleAsyncTask(HomeActivity activity) {
            activityWeakRef = new WeakReference<>(activity);
        }

        /* Happens before, the setup */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            /* get strong reference which will be destroyed after this scope ends */
            HomeActivity activity = activityWeakRef.get();
            /* This will return if garbage collection is happening so we return immediately */
            if (activity == null || activity.isFinishing()) {
                return;
            }
        }

        /**
         * Here we are getting the currently playing track audio features
         *
         * @param voids nothing
         * @return an array list of the track audio features
         */
        @Override
        protected CurrentPlaying doInBackground(Void... voids) {
            /* get strong reference which will be destroyed after this scope ends */
            HomeActivity activity = activityWeakRef.get();
            if (activity == null || activity.isFinishing()) {
                return null;
            }

            return activity.getTrackAudioFeatures();
        }

        /**
         * At this point, we have the populated array list and need to now update
         * the TextView for the current song playing.
         *
         * @param trackFeaturesObj
         */
        @Override
        protected void onPostExecute(CurrentPlaying trackFeaturesObj) {
            super.onPostExecute(trackFeaturesObj);

            /* get strong reference which will be destroyed after this scope ends */
            HomeActivity activity = activityWeakRef.get();
            if (activity == null || activity.isFinishing()) {
                return;
            }

            /* Should have a nicely formatted obj here */
        }
    }

    /**
     * When the back button is pressed, I do not want to go back to the login screen.
     */
    @Override
    public void onBackPressed() {
        // super.onBackPressed();
        Toast.makeText(getApplicationContext(), "You shall not go BACK!", Toast.LENGTH_SHORT).show();
    }

    public void fetchProfilePic() {
        UserProfilePicAsyncTask uPPAT = new UserProfilePicAsyncTask(this);
        uPPAT.execute();
    }

    private static class UserProfilePicAsyncTask extends AsyncTask<String, Void, Bitmap> {

        Bitmap bmFetched;

        /* Create a weak reference to try and minimize leaks */
        /* The variables accessible by the class */
        private WeakReference<HomeActivity> activityWeakRef;

        UserProfilePicAsyncTask(HomeActivity activity) {
            activityWeakRef = new WeakReference<>(activity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            /* get strong reference which will be destroyed after this scope ends */
            HomeActivity activity = activityWeakRef.get();

            /* This will return if garbage collection is happening so we return immediately */
            if (activity == null || activity.isFinishing()) {
                return;
            }
        }

        @Override
        protected Bitmap doInBackground(String... strings) {

            /* get strong reference which will be destroyed after this scope ends */
            HomeActivity activity = activityWeakRef.get();

            /* if the activity is null or finishing, do not do anything */
            if (activity == null || activity.isFinishing()) {
                return null;
            }

            try {
                /* Get the current user's profile picture */
                InputStream in = new URL("https://platform-lookaside.fbsbx.com/platform/profilepic/?asid=627824893978125&height=200&width=200&ext=1565622679&hash=AeQCwprawguG10t4").
                        openStream();

                bmFetched = BitmapFactory.decodeStream(in);
                in.close();

                /* Get current playing artist uri */
                String artistPicUri = activity.spotify.
                                    getArtist(activity.currentArtistUri).
                                    images.
                                    get(1).
                                    url;

                URL url = new URL(artistPicUri);
                Bitmap bmArtistArt = BitmapFactory.decodeStream(url.openStream());
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                       activity.currentTrackArtist.setImageBitmap(bmArtistArt);

                    }
                });

            } catch (Exception e) {
                Log.e("Error Occurred: ", e.getMessage());
                e.printStackTrace();
            }
            return bmFetched;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);

            /* get strong reference which will be destroyed after this scope ends */
            HomeActivity activity = activityWeakRef.get();

            /* if the activity is null or finishing, do not do anything */
            if (activity == null || activity.isFinishing()) {
                // Do nothing
            } else {
                /* Set the user's profile picture */
                activity.userPicture.setImageBitmap(bitmap);
            }
        }
    }

    /**
     * This method will handle acccessToken or failure as necessary
     * @param requestCode
     * @param resultCode
     * @param intent
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        /* Check if result comes from the correct activity */
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);

            switch (response.getType()) {
                /* Response was successful and contains auth token */
                case TOKEN:
                    /* Handle successful response */
                    Log.d(TAG, "Auth token given!: " + response.getAccessToken());
                    accessToken = response.getAccessToken();
                    break;

                /* Auth flow returned an error */
                case ERROR:

                    /* Handle error response */
                    Log.e(TAG, "onActivityResult: failure ");
                    break;

                /* Most likely auth flow was cancelled */
                default:
                    // Handle other cases
            }
        }
    }
}
