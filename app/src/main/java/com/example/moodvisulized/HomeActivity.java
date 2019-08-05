package com.example.moodvisulized;

import android.content.Intent;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.AudioFeaturesTrack;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.Track;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;

public class HomeActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1094;
    private static final String CLIENT_ID = "0184658057ca400693856a596026419b";
    private static final String REDIRECT_URI = "com.example.moodvisualized://callback";

    /* The UI variables */
    ArrayList<TextView> uiElements = new ArrayList<>(); // Each UI variable but represented as num.

    String coverArtUri;                               // The uri for the CoverArt
    String coverArtUrl = "";
    String artistCoverArtUrl = "";
    String userCoverArtUrl = "";

    ImageView currentTrackArtist;
    ImageView backgroundBanner2;
    ImageButton currentTrackArrow;
    ImageView userPicture;
    ImageView allTimeFavArrow;

    /* The variables for holding essential information */
    private String currentTrackUri = "";         // The track Id for the current song playing
    private String currentArtistUri = "";       // The current artist Uri

    /* Variables needed for service to spotify calls or access to app-remote */
    private ConnectionParams connectionParams;   // Connection Params for the app remote
    private SpotifyAppRemote mSpotifyAppRemote; // The app remote
    private SpotifyService spotify;            // Service variable
    private String accessToken = null;        // Needed to make calls that require access token.

    /* Debugging purposes */
    private static final String TAG = HomeActivity.class.getSimpleName();
    boolean overwriteTxt = false;

    /* Objects */
    CurrentPlaying curTrack = null;
    CurrentPlaying receivedObj = null;
    AllTimeFavorite favTracksShort = new AllTimeFavorite();   // favorite tracks from short_term
    AllTimeFavorite favTracksMedium = new AllTimeFavorite(); // favorite tracks from medium_term
    AllTimeFavorite favTracksLong = new AllTimeFavorite();  // favorite tracks from long_term

    /* Date variables */
    Calendar cal = Calendar.getInstance();              //
    int dayOfYear = cal.get(Calendar.DAY_OF_YEAR);
    int offsetFileCreated = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // to remove the status bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_home);

        userPicture = (ImageView) findViewById(R.id.userPicture);
        currentTrackArtist = (ImageView) findViewById(R.id.currentlyPlayingArtist);
        currentTrackArrow = (ImageButton) findViewById(R.id.currentTrackPlayingArrow);

        backgroundBanner2 = (ImageView) findViewById(R.id.allTimeFavoriteTrackBanner);
        allTimeFavArrow = (ImageView) findViewById(R.id.allTimeFavoriteTrackArrow);

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

        /* onWard to AllTimeFavoriteTracks activity */
        allTimeFavArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {toAllTimeFavoriteTracks(v);}
        });

        /* Checking to see if the file does not exists and then write the current date */
        if (!checkFileExists("statsUpdateLog.txt")) {
            writeInternalStorageDate();
            Log.d(TAG, "onCreate: DOY does not exists, It was created!");
            offsetFileCreated = 7;
            overwriteTxt = true;
        }

        Log.d(TAG, "onCreate: DOY File EXISTS");

        /* For mAppRemoteSet the connection parameters */
        connectionParams = new ConnectionParams.Builder(CLIENT_ID)
                .setRedirectUri(REDIRECT_URI)
                .showAuthView(true)
                .build();

        /* Let us see if we receive anything first */
        Intent i = getIntent();
        if ( (receivedObj = (CurrentPlaying) i.getSerializableExtra("object")) != null) {

            Log.d(TAG, "onCreate: received object: " + receivedObj.getCoverArtUrl());
            Log.d(TAG, "onCreate: Second and more run through");

            /* If we reach this, then we received something from currently playing activity */
            curTrack = new CurrentPlaying(receivedObj);

            Log.d(TAG, "onCreate: CURTRACK coverart url: " + curTrack.getCoverArtUrl());
            Log.d(TAG, "onCreate: CURTRACK ARTIST COVERART URL: " + curTrack.getArtistCoverArtUrl());
            Log.d(TAG, "onCreate: CURTRACK TRACK URI: " + curTrack.getTrackUri());
            
        } else {

            /* If we get here, then we are in the first run through of the app */
            Log.d(TAG, "onCreate: First run through");
            curTrack = new CurrentPlaying();
            curTrack.setTrackUri("init");
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
                connected();
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

        /* When app not in use, disconnect. */
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
        Log.d(TAG, "onStop: App remote disconnected");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    /**
     * Onward to the the next activity which is the Track stats screen.
     * @param view  Refering to the next activity view
     */
    public void toTrackAudioFeatures(View view) {
        /* I want to send the uiValues and coverart to UserStatistics */
        Intent intent = new Intent(this, UserStatistics.class);

        Log.d(TAG, "toTrackAudioFeatures: coverart url: " + curTrack.getCoverArtUrl());
        Log.d(TAG, "toTrackAudioFeatures: artist coverart url" + curTrack.getArtistCoverArtUrl());

        /* The object being sent to UserStatistics Activity */
        intent.putExtra("curTrackObj", curTrack);

        startActivity(intent);
    }

    public void toAllTimeFavoriteTracks(View view) {

        Intent intent = new Intent(this, AllTimeFavoriteTracks.class);

        Log.d(TAG, "toAllTimeFavoriteTracks: Going to AllTimeFavoriteTracks view");


        /* if we updated, make sure to write */
        if (overwriteTxt) {
            clearFile("favoriteTracks.txt");
            writeToInternalStorage(favTracksShort, "short");
            writeToInternalStorage(favTracksMedium, "medium");
            writeToInternalStorage(favTracksLong, "long");
        }

        overwriteTxt = false;

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

                    /* Get current track uri */
                    currentTrackUri = playerState.track.uri;

                    /* Get the uri, done twice since two ':' are present */
                    currentTrackUri = currentTrackUri.substring(currentTrackUri.indexOf(":") + 1);
                    currentTrackUri = currentTrackUri.substring(currentTrackUri.indexOf(":") + 1);

                    /* get current artist playing */
                    currentArtistUri = playerState.track.artist.uri;
                    currentArtistUri = currentArtistUri.
                            substring(currentArtistUri.indexOf(":") + 1);
                    currentArtistUri = currentArtistUri.
                            substring(currentArtistUri.indexOf(":") + 1);

                    /* So if the track uri are not the same, make api call; new song playing */
                    if (!("spotify:track:" + currentTrackUri).equals(curTrack.getTrackUri())) {
                        Log.d(TAG, "connected: API CALL; NEW SONG");

                        coverArtUri = playerState.track.imageUri.toString();

                        /* Remove the parts to reach the bare url to the img */
                        coverArtUri = coverArtUri.substring(coverArtUri.indexOf(":") + 1);
                        coverArtUri = coverArtUri.substring(coverArtUri.indexOf(":") + 1);
                        coverArtUri = coverArtUri.substring(0, coverArtUri.indexOf("\'"));

                        /* grab current track features and update the UI with these details */
                        beginAsyncTask();

                        /* Fetch the artist picture asap then send to picasso */
                        fetchArtistPic();

                    } else {
                    /* else, return the same obj back from the UserStatistics activity */
                       coverArtUrl = curTrack.getCoverArtUrl();
                       artistCoverArtUrl = curTrack.getArtistCoverArtUrl();

                       setUrlPicture(curTrack.getArtistCoverArtUrl(), currentTrackArtist);
                       Log.d(TAG, "connected: else condition (same song playing): ");
                    }
                    /* Fetch the user's profile picture */
                    fetchProfilePic();
                });

        /* If this is true, rewrite the new date we last fetched */
        if (updateFavTracksRankings(offsetFileCreated)) {
            writeInternalStorageDate();
            fetchFavTracks();
            overwriteTxt = true;
            Log.d(TAG, "connected: Fetching fav tracks, time to update.");
        }
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

                /* Update the object created */
                Log.d(TAG, "connected: NEW SONG IS PLAYING");

                /* Get current playing artist URL from URI */
                // https://i.scdn.co/image/8db0e8c0838b50a3c88da6f4ce7dcdaec0384ce5
                coverArtUrl = "https://i.scdn.co/image/" + coverArtUri;
                Log.d(TAG, "getTrackAudioFeatures: " + coverArtUrl);

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
                        coverArtUrl,
                        "init"
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

    private static class ExampleAsyncTask extends AsyncTask<Void, TextView, Void> {
        private WeakReference<HomeActivity> activityWeakRef;

        ExampleAsyncTask(HomeActivity activity) {
            activityWeakRef = new WeakReference<>(activity);
        }

        /**
         * Here we are getting the currently playing track audio features
         *
         * @param voids nothing
         * @return an array list of the track audio features
         */
        @Override
        protected Void doInBackground(Void... voids) {
            /* get strong reference which will be destroyed after this scope ends */
            HomeActivity activity = activityWeakRef.get();
            if (activity == null || activity.isFinishing()) {
                return null;
            }

            activity.getTrackAudioFeatures();

            return null;
        }
    }

    /**
     * When the back button is pressed, I do not want to go back to the login screen.
     */
    @Override
    public void onBackPressed() {
        // super.onBackPressed();
        //Toast.makeText(getApplicationContext(), "You shall not go BACK!", Toast.LENGTH_SHORT).show();
    }

    public void fetchProfilePic() {
        UserProfilePicAsyncTask uPPAT = new UserProfilePicAsyncTask(this);
        uPPAT.execute();
    }

    public void fetchArtistPic() {
        ArtistProfilePicAsyncTask aPPAT = new ArtistProfilePicAsyncTask(this);
        aPPAT.execute();
    }

    public void fetchFavTracks() {
        FavoriteTracksAsyncTask fTAT = new FavoriteTracksAsyncTask(this);
        fTAT.execute();
    }

    private static class FavoriteTracksAsyncTask extends AsyncTask<Artist, Void, Void> {

        /* Create a weak reference to try and minimize leaks */
        /* The variables accessible by the class */
        private WeakReference<HomeActivity> activityWeakRef;

        FavoriteTracksAsyncTask(HomeActivity activity) {
            activityWeakRef = new WeakReference<>(activity);
        }

        @Override
        protected Void doInBackground(Artist... artists) {
            /* get strong reference which will be destroyed after this scope ends */
            HomeActivity activity = activityWeakRef.get();

            /* if the activity is null or finishing, do not do anything */
            if (activity == null || activity.isFinishing()) {
                return null;
            }

            /* Setup the call to get the user's top tracks. Pull top ten and start from beginning*/
            Map<String, Object> options = new HashMap<>();
            options.put(SpotifyService.LIMIT, 10);
            options.put(SpotifyService.OFFSET, 0);

            /* Make the call */
            options.put(SpotifyService.TIME_RANGE, "short_term");
            Pager<Track> userTopTracksShort = activity.spotify.getTopTracks(options);
            activity.favTracksShort.setFavTrackList(userTopTracksShort);

            options.put(SpotifyService.TIME_RANGE, "medium_term");
            Pager<Track> userTopTracksMid = activity.spotify.getTopTracks(options);
            activity.favTracksMedium.setFavTrackList(userTopTracksMid);

            options.put(SpotifyService.TIME_RANGE, "long_term");
            Pager<Track> userTopTracksLong = activity.spotify.getTopTracks(options);

            /* Update the object with this newly found information */
            activity.favTracksLong.setFavTrackList(userTopTracksLong);

            return null;
        }
    }

    private static class ArtistProfilePicAsyncTask extends AsyncTask<String, Void, Void> {

        /* Create a weak reference to try and minimize leaks */
        /* The variables accessible by the class */
        private WeakReference<HomeActivity> activityWeakRef;

        ArtistProfilePicAsyncTask(HomeActivity activity) {
            activityWeakRef = new WeakReference<>(activity);
        }

        @Override
        protected Void doInBackground(String... strings) {
            /* get strong reference which will be destroyed after this scope ends */
            HomeActivity activity = activityWeakRef.get();

            /* if the activity is null or finishing, do not do anything */
            if (activity == null || activity.isFinishing()) {
                return null;
            }

            try {
                /* Get current playing artist URL from URI */
                activity.artistCoverArtUrl = new URL(activity.spotify
                        .getArtist(activity.currentArtistUri)
                        .images
                        .get(1)
                        .url)
                        .toString();

                activity.curTrack.setArtistCoverArtUrl(activity.artistCoverArtUrl);

            } catch (Exception e) {
                Log.e("Error Occurred: ", e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            /* get strong reference which will be destroyed after this scope ends */
            HomeActivity activity = activityWeakRef.get();

            /* if the activity is null or finishing, do not do anything */
            if (activity == null || activity.isFinishing()) {
                return;
            }

            activity.setUrlPicture(activity.artistCoverArtUrl, activity.currentTrackArtist);
        }
    }

    public void setUrlPicture(String picUrl, ImageView view) {

        Log.d(TAG, "setUrlPicture: PICASSO CALLED W/: " + picUrl + ": " + view.toString());
        Picasso.get()
                .load(picUrl)
                .into(view);
    }

    private static class UserProfilePicAsyncTask extends AsyncTask<String, Void, String> {

        /* Create a weak reference to try and minimize leaks */
        /* The variables accessible by the class */
        private WeakReference<HomeActivity> activityWeakRef;

        UserProfilePicAsyncTask(HomeActivity activity) {
            activityWeakRef = new WeakReference<>(activity);
        }

        @Override
        protected String doInBackground(String... strings) {
            /* get strong reference which will be destroyed after this scope ends */
            HomeActivity activity = activityWeakRef.get();

            /* if the activity is null or finishing, do not do anything */
            if (activity == null || activity.isFinishing()) {
                return null;
            }

            try {
                /* Get the current user's profile picture */
                return activity.userCoverArtUrl = activity.spotify
                        .getMe()
                        .images
                        .get(0)
                        .url;

            } catch (Exception e) {
                Log.e("Error Occurred: ", e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String urlString) {
            super.onPostExecute(urlString);

            /* get strong reference which will be destroyed after this scope ends */
            HomeActivity activity = activityWeakRef.get();

            /* if the activity is null or finishing, do not do anything */
            if (activity == null || activity.isFinishing()) {
                // Do nothing
            } else {
                /* Set the user's profile picture */
                activity.setUrlPicture(urlString, activity.userPicture);
            }
        }
    }

    /**
     * Clear the contents of the file.
     * @param fileName the file being cleared
     */
    public void clearFile(String fileName) {

        try {
            String filePath = getBaseContext().getFilesDir() + "/" + fileName;
            PrintWriter pw = new PrintWriter(filePath);
            pw.close();
        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    public boolean checkFileExists(String fileName) {

        try {
            /* If file exists, do nothing and return true */
            String filePath = getBaseContext().getFilesDir() + "/" + fileName;
            File file = new File(filePath);
            if (file.isFile()) {
                return true;
            } else {
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();

            return false;
        }
    }

    /**
     * Allows us to write to internal storage the newly updated tracks with our top tracks.
     * @param favTracks an object holding all the necessary information.
     * @param type short, medium, or long perspective of results.
     */
    public void writeToInternalStorage(AllTimeFavorite favTracks, String type) {

        String fileName = "favoriteTracks.txt";

        try {

            FileOutputStream out = openFileOutput(fileName, MODE_APPEND);

            for (int i = 0; i < favTracks.getFavTrackList().size(); i++) {
                out.write((favTracks.getFavTrackList().get(i).name + ", ").getBytes());
                for (int j = 0; j < favTracks.getFavTrackList().get(j).artists.size(); j++) {
                    out.write((favTracks.getFavTrackList().get(i).artists.get(j).name + ", ").getBytes());
                }
                out.write((favTracks.getFavTrackList().get(i).album.images.get(0).url + "\n").getBytes());
                out.flush();
            }

            out.close();

            Log.d(TAG, "writeToInternalStorage: Data saved");

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "writeToInternalStorage: " + e.getMessage() + e.getCause());
        }
    }

    /**
     * Allows us to read from a file that is in internal storage.
     * @param fileName the file we want to read from internal storage
     */
    public void readFromInternalStorage(String fileName) {

        try {
            FileInputStream in = getBaseContext().openFileInput(fileName);
            InputStreamReader iSR = new InputStreamReader(in);

            BufferedReader bR = new BufferedReader(iSR);
            StringBuilder sB = new StringBuilder();

            String curLine;

            while ((curLine = bR.readLine()) != null) {
                sB.append(curLine);
                sB.append("\n");
            }

            Log.d(TAG, "readFromInternalStorage: Storage Read");
            Log.d(TAG, "readFromInternalStorage: " + sB);

        } catch (Exception e) {
            Log.e(TAG, "readFromInternalStorage: " + e.getCause() );
        }
    }

    /**
     * If the file does not exists or we need to re-update the last fetch date, call this.
     */
    public void writeInternalStorageDate() {
        String fileName = "statsUpdateLog.txt";

        try {

            FileOutputStream out = openFileOutput(fileName, MODE_PRIVATE);

            out.write((dayOfYear + "").getBytes());
            out.flush();

            out.close();

            Log.d(TAG, "writeInternalStorageDate: Day of the year logged");

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "writeInternalStorageDate: " + e.getMessage() + e.getCause());
        }
    }

    /**
     * We will return the DOY that we performed a fetch of top tracks
     * @return the Day of the Year (DOY)
     */
    public String readInternalStorageDate() {
        String result = null;
        try {

            FileInputStream in = getBaseContext().openFileInput("statsUpdateLog.txt");
            InputStreamReader iSR = new InputStreamReader(in);

            BufferedReader bR = new BufferedReader(iSR);
            StringBuilder sB = new StringBuilder();

            String curLine;

            while ((curLine = bR.readLine()) != null) {
                sB.append(curLine);
            }

            Log.d(TAG, "readInternalStorageDate: storage read");

            result = sB.toString();

        } catch (Exception e) {
            Log.e(TAG, "readFromInternalStorage: " + e.getCause() );
        }

        return result;
    }

    /**
     * This is used to chekck if we should update the allTimeFavoriteTracks activity results.
     * @return true if the DOY is greater than last fetch, false else.
     */
    public boolean updateFavTracksRankings(int offset) {
        String readDOY = readInternalStorageDate();
        int strToInt;
        int week = 7;
        int result = 0;
        Log.d(TAG, "updateFavTracksRankings: " + dayOfYear);

        try {
           strToInt = Integer.parseInt(readDOY);
           result = strToInt + week;

        } catch (NumberFormatException e) {
            strToInt = 0;
        }

        Log.d(TAG, "updateFavTracksRankings: DOY update will occur next -> " + result);

        /* Here we are seeing if a week has gone by to then re update the user's rankings */
        if (dayOfYear >= (result - offset)) {
            return true;
        } else {
            return false;
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