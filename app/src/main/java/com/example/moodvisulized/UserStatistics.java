package com.example.moodvisulized;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Credentials;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.types.Image;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import org.w3c.dom.Text;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyCallback;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.AudioFeaturesTrack;
import kaaes.spotify.webapi.android.models.AudioFeaturesTracks;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.SavedTrack;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.UserPrivate;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class UserStatistics extends AppCompatActivity {

    private static final int REQUEST_CODE = 1337;
    private static final String CLIENT_ID = "0184658057ca400693856a596026419b";
    private static final String REDIRECT_URI = "moodvisualized://callback";

    Bitmap coverArt = null;
    List<Track> userSavedTracks = new ArrayList<>();       // getting all saved tracks
    List<Track> userSongTodayTracks = new ArrayList<>();  // Stores the date everything was added
    List<Track> userTopTracks = new ArrayList<>();       // getting all top song
    Map<String, Object> options = new HashMap<String, Object>();     // for each call
    SpotifyService spotify;                            // Service variable
    private SpotifyAppRemote mSpotifyAppRemote;       // the app remote

    String currentTrack = "";
    private String accessToken = null;
    private String dateToday = new SimpleDateFormat("MM-dd").
                                                    format(Calendar.getInstance().getTime());

    private String currentTrackUri = ""; // The track Id for the current song playing
    private ArrayList<Float> curTrackAudioFet = new ArrayList<>(); // current track audio features.
    ArrayList<TextView> uiElements = new ArrayList<>();
    ImageView coverArtImg;
    TextView updateTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // to remove the status bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_user_statistics);

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
        System.out.println("Connected");

        coverArtImg = (ImageView) findViewById(R.id.coverArt);
        coverArtImg.setImageResource(R.drawable.mood_image);

        /* Store all the ui elements in an array list to parse over later */
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
    }

    @Override
    protected void onStart() {
        super.onStart();

        /* Set the connection parameters */
        ConnectionParams connectionParams =
                new ConnectionParams.Builder(CLIENT_ID)
                .setRedirectUri(REDIRECT_URI)
                .showAuthView(true)
                .build();

        SpotifyAppRemote.connect(this, connectionParams, new Connector.ConnectionListener() {
            @Override
            public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                mSpotifyAppRemote = spotifyAppRemote;
                Log.d("UserStatistics", "App remote Connected!");
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
                Log.e("UserStatistics", throwable.getMessage(), throwable);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        System.out.println("Resume end");
    }

    @Override
    protected void onStop() {
        super.onStop();
        /* When app not in use, disconnect, might change if we need it for longer */
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }

    /**
     * This method will subscribe to the player state for future use
     */
    public void connected() {
        /* Subscribe to the PlayerState */
        mSpotifyAppRemote.getPlayerApi()
                .subscribeToPlayerState()
                .setEventCallback(playerState -> {

                    /* get current track uri */
                    currentTrackUri = playerState.track.uri;

                    /* just get the uri, done twince since two ':' */
                    currentTrackUri = currentTrackUri.substring(currentTrackUri.indexOf(":") + 1);
                    currentTrackUri = currentTrackUri.substring(currentTrackUri.indexOf(":") + 1);

                    /* Get the cover art of the current playing song */
                    mSpotifyAppRemote.getImagesApi()
                            .getImage(playerState.track.imageUri, Image.Dimension.LARGE)
                            .setResultCallback(bitmap -> {
                                coverArtImg.setImageBitmap(bitmap);
                            });

                    /* grab current track features and update the UI with these details */
                    beginAsyncTask();
                });
    }

    /**
     * Get audio analysis for the song that is playing right now
     */
    public ArrayList getTrackAudioFeatures() {
        float danceability = 0;
        float liveness = 0;
        float valence = 0;
        float speechiness = 0;
        float instrumentalness = 0;

        float loudness = 0;
        float key = 0;
        float energy = 0;
        float tempo = 0;
        float acousticness = 0;
        int retryCount = 0;
        int maxAttempt = 3;
        ArrayList<Float> trackFeatures = new ArrayList<>();

        while (true) {
            try {
                AudioFeaturesTrack trackAudioFeatures = spotify.getTrackAudioFeatures(currentTrackUri);

                danceability = trackAudioFeatures.danceability;
                liveness = trackAudioFeatures.liveness;
                valence = trackAudioFeatures.valence;
                speechiness = trackAudioFeatures.speechiness;
                instrumentalness = trackAudioFeatures.instrumentalness;

                loudness = trackAudioFeatures.loudness;
                key = trackAudioFeatures.key;
                energy = trackAudioFeatures.energy;
                tempo = trackAudioFeatures.tempo;
                acousticness = trackAudioFeatures.acousticness;

                trackFeatures.add(danceability);
                trackFeatures.add(liveness);
                trackFeatures.add(valence);
                trackFeatures.add(speechiness);
                trackFeatures.add(instrumentalness);

                trackFeatures.add(loudness);
                trackFeatures.add(key);
                trackFeatures.add(energy);
                trackFeatures.add(tempo);
                trackFeatures.add(acousticness);
                break;

            } catch (RetrofitError e) {
                if (retryCount == maxAttempt) {
                    e.getBody();
                    e.getResponse();
                } else {retryCount++;}
            }
        }
        return trackFeatures;
    }

    public void beginAsyncTask() {
        ExampleAsyncTask task = new ExampleAsyncTask(this);
        task.execute();
    }

    private static class ExampleAsyncTask extends AsyncTask<Void, TextView, ArrayList<Float>> {
        private WeakReference<UserStatistics> activityWeakRef;

        ExampleAsyncTask(UserStatistics activity) {
            activityWeakRef = new WeakReference<>(activity);
        }

        /* Happens before, the setup */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            /* get strong reference which will be destroyed after this scope ends */
            UserStatistics activity = activityWeakRef.get();
            /* This will return if garbage collection is happening so we return immediately */
            if (activity == null || activity.isFinishing()) {
                return;
            }

            /* uncomment this is we want to display something in the uielements before updating
            for (int i = 0; i < 10; i++) {
                activity.updateTxt = (TextView) activity.uiElements.get(i);
                updateTxt.setText(String.format("%s", curTrackAudioFet.get(i).toString()));
                activity.updateTxt.setText(String.format("%s","Loading"));
            }
            */
        }

        /**
         * Here we are getting the currently playing track audio features
         * @param voids nothing
         * @return an array list of the track audio features
         */
        @Override
        protected ArrayList<Float> doInBackground(Void... voids) {
            /* get strong reference which will be destroyed after this scope ends */
            UserStatistics activity = activityWeakRef.get();
            if (activity == null || activity.isFinishing()) {
                return null;
            }

            return activity.getTrackAudioFeatures();
        }

        /**
         *  At this point, we have the populated array list and need to now update
         *  the TextView for the current song playing.
         * @param trackFeatures
         */
        @Override
        protected void onPostExecute(ArrayList<Float> trackFeatures) {
            super.onPostExecute(trackFeatures);

            /* get strong reference which will be destroyed after this scope ends */
            UserStatistics activity = activityWeakRef.get();
            if (activity == null || activity.isFinishing()) {
                return;
            }

            /* make all floats that can be represented as a percentage */
            for (int i = 0; i < trackFeatures.size(); i++) {
                if (i != 5 && i != 6 && i != 8) {
                    trackFeatures.set(i, (trackFeatures.get(i) * 100));
                }
            }

            for (int i = 0; i < trackFeatures.size(); i++) {
                /* correctly identify the key rather than put a number */
                if (i != 5 && i != 6 && i != 8) {
                    activity.updateTxt = (TextView) activity.uiElements.get(i);
                    activity.updateTxt.setText(String.format(java.util.Locale.US, "%.5s%%", trackFeatures.get(i).toString()));

                } else if (i == 8) {
                    activity.updateTxt = (TextView) activity.uiElements.get(i);
                    activity.updateTxt.setText(String.format(java.util.Locale.US, "%.5s", trackFeatures.get(i).toString()));
                } else if (i == 5) {
                    activity.updateTxt = (TextView) activity.uiElements.get(i);
                    activity.updateTxt.setText(String.format(java.util.Locale.US, "%.5s dB", trackFeatures.get(i).toString()));
                } else {
                    activity.updateTxt = (TextView) activity.uiElements.get(i);
                    switch (Math.round(trackFeatures.get(i))) {
                        case -1:
                            activity.updateTxt.setText(String.format("%s", KeyIdentifier.UNKOWN.stringIdentifier()));
                            break;
                        case 0:
                            activity.updateTxt.setText(String.format("%s", KeyIdentifier.C.stringIdentifier()));
                            break;
                        case 1:
                            activity.updateTxt.setText(String.format("%s", KeyIdentifier.CSHARP.stringIdentifier()));
                            break;
                        case 2:
                            activity.updateTxt.setText(String.format("%s", KeyIdentifier.D.stringIdentifier()));
                            break;
                        case 3:
                            activity.updateTxt.setText(String.format("%s", KeyIdentifier.DSHARP.stringIdentifier()));
                            break;
                        case 4:
                            activity.updateTxt.setText(String.format("%s", KeyIdentifier.E.stringIdentifier()));
                            break;
                        case 5:
                            activity.updateTxt.setText(String.format("%s", KeyIdentifier.F.stringIdentifier()));
                            break;
                        case 6:
                            activity.updateTxt.setText(String.format("%s", KeyIdentifier.FSHARP.stringIdentifier()));
                            break;
                        case 7:
                            activity.updateTxt.setText(String.format("%s", KeyIdentifier.G.stringIdentifier()));
                            break;
                        case 8:
                            activity.updateTxt.setText(String.format("%s", KeyIdentifier.GSHARP.stringIdentifier()));
                            break;
                        case 9:
                            activity.updateTxt.setText(String.format("%s", KeyIdentifier.A.stringIdentifier()));
                            break;
                        case 10:
                            activity.updateTxt.setText(String.format("%s", KeyIdentifier.ASHARP.stringIdentifier()));
                            break;
                        case 11:
                            activity.updateTxt.setText(String.format("%s", KeyIdentifier.B.stringIdentifier()));
                            break;
                        default:
                            break;
                    }
                }

            }
        }

    /**
     * getUserSavedLibrary gets the user's whole saved songs from their library.
     * This iterates until the first song ever saved. It also does other things such
     * as populate userSongTodayTracks which will check to see if a song today was saved
     * earlier in the past year(s).
     */
    public void getUserSavedLibrary() {
        try {
            int totalSongsInLibrary;
            int i = 0;
            // Limit is how many songs we can grab at a time (Max is 50 for this call)
            int limit = 50;
            int updateIndexBy = 50; // where to start the new index in the library.
            int offset = 0;
            String tmp;
            Pager<SavedTrack> savedTrackPager;

            totalSongsInLibrary = spotify.getMySavedTracks().total;
            options.put(SpotifyService.LIMIT, limit); // get 50 songs at a time.
            options.put(SpotifyService.OFFSET, offset); // initially 0 to start at top of library.

            while (i <= totalSongsInLibrary) {
                savedTrackPager = spotify.getMySavedTracks(options);

                // iterate though the first songs fetched
                for (SavedTrack savedTrack : savedTrackPager.items) {
                    userSavedTracks.add(savedTrack.track); // save the track from the fetch

                    // capture the instance of the track being analyzed
                    tmp = savedTrack.added_at;

                    // reformat it to MM-dd
                    tmp = reformatDate(tmp);

                    if (tmp.equals(dateToday)) {
                        userSongTodayTracks.add(savedTrack.track);
                    }
                }

                if ( (totalSongsInLibrary - i) >= 50 ) {
                    i += updateIndexBy; // This means there are more than 50 songs left, increase by so.
                    options.put(SpotifyService.OFFSET, offset += updateIndexBy); // New start point is at 50th song.
                } else if(i == totalSongsInLibrary) {
                    break; // To get out of the infinite loop
                } else {
                    i += (totalSongsInLibrary - i); // update by remained to not go over.
                    options.put(SpotifyService.OFFSET, offset += (totalSongsInLibrary - i)); // New start point is at 50th song.
                }
            }
        } catch (RetrofitError e) {
            System.out.println(e.getResponse().getStatus());
            System.out.println(e.getResponse().getReason());
        }
    }

    /**
     * This method will restructure the spotify added_at format(yyyy-MM-ddT##..) to (MM-dd)
     * @param trackDate The added_at track string date
     * @return The new formatted (MM-dd) string
     */
    public String reformatDate(String trackDate) {
        /* I want to ignore the timezone and year, just capture the MM-dd */
        int indexStart = trackDate.indexOf("-"); // this is the month
        int indexEnd = trackDate.indexOf("T"); // this is the day of the month
        trackDate = trackDate.substring(indexStart + 1, indexEnd);

        return trackDate;
    }

    public enum KeyIdentifier
    {
        UNKOWN(-1) {@Override public String stringIdentifier() {return "Unknown";}},
        C(0) {@Override public String stringIdentifier() {return "C";}},
        CSHARP(1) {@Override public String stringIdentifier() {return "C#";}},
        D(2) {@Override public String stringIdentifier() {return "D";}},
        DSHARP(3) {@Override public String stringIdentifier() {return "D#";}},
        E(4) {@Override public String stringIdentifier() {return "E";}},
        F(5) {@Override public String stringIdentifier() {return "F";}},
        FSHARP(6) {@Override public String stringIdentifier() {return "F#";}},
        G(7) {@Override public String stringIdentifier() {return "G";}},
        GSHARP(8) {@Override public String stringIdentifier() {return "G#";}},
        A(9) {@Override public String stringIdentifier() {return "A";}},
        ASHARP(10) {@Override public String stringIdentifier() {return "A#";}},
        B(11) {@Override public String stringIdentifier() {return "B";}};

        private int numberIdentifier;
        public abstract String stringIdentifier();

        KeyIdentifier(int numIdentifier) {this.numberIdentifier = numIdentifier;}
    }

    public void getUserTopTracks() {
        try {
            int totalTopSongs;
            int i = 0;
            // Limit is how many songs we can grab at a time (Max is 50 for this call)
            int limit = 50;
            int updateIndexBy = 50; // where to start the new index in the library.
            // Offset is the starting point of which we index our songs
            // REMEMBER: update offset to get more than 50 songs. It starts at 0
            // and when updated to (offset = 50) will start at the 50th song while
            // (limit) will fetch the next 50 songs in this case.
            int offset = 0;
            String timeRange = "long_term"; // How far back to calculate topSongs
                                            // long_term - years back
                                            // medium_term - 6 months back
                                            // short_term - 1 month back
            Pager<Track> topTrackPager = new Pager<>();

            totalTopSongs = spotify.getTopTracks().total;
            options.put(SpotifyService.LIMIT, limit); // get 50 songs at a time.
            options.put(SpotifyService.OFFSET, offset); // initially 0 to start at top of library.
            options.put(SpotifyService.TIME_RANGE, timeRange); // get recent songs

            while (i <= totalTopSongs) {
                topTrackPager = spotify.getTopTracks(options);
                for (Track track : topTrackPager.items) {
                    userTopTracks.add(track); // save the track from the fetch
                }

                if ( (totalTopSongs - i) >= 50 ) {
                    i += updateIndexBy; // This means there are more than 50 songs left, increase by so.
                    options.put(SpotifyService.OFFSET, offset += updateIndexBy); // New start point is at 50th song.
                    options.put(SpotifyService.TIME_RANGE, timeRange); // get recent songs
                } else if(i == totalTopSongs) {
                    break; // To get out of the infinite loop
                } else {
                    i += (totalTopSongs - i); // update by remained to not go over.
                    options.put(SpotifyService.OFFSET, offset += (totalTopSongs - i)); // New start point is at 50th song.
                    options.put(SpotifyService.TIME_RANGE, timeRange); // get recent songs
                }
            }

            for (Track track : userTopTracks) {
                System.out.println("Top Songs: " + track.name + " by -> " + track.artists.get(0).name);
            }

        } catch (RetrofitError e) {
            System.out.println(e.getResponse().getStatus());
            System.out.println(e.getResponse().getReason());
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
                    Log.d("UserStatistics", "Auth token given!: " + response.getAccessToken());
                    accessToken = response.getAccessToken();
                    break;

                /* Auth flow returned an error */
                case ERROR:
                    /* Handle error response */
                    System.out.println("We reached a failure in onActivityResult method");
                    break;

                /* Most likely auth flow was cancelled */
                default:
                    // Handle other cases
            }
        }
    }
}
