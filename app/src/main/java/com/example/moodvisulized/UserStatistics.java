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
import java.io.InputStream;
import java.io.InputStreamReader;
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
    ImageView coverArtImg;

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
        System.out.println("Connected");

        coverArtImg = (ImageView) findViewById(R.id.coverArt);
        coverArtImg.setImageResource(R.drawable.mood_image);


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
                connected();
                Runnable fetchInfo = new Runnable() {
                    @Override
                    public void run() {
                        getSongArtwork(getCurrentSongJSON());
                    }
                };

                Thread fit = new Thread(fetchInfo);
                fit.start();
            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.e("UserStatistics", throwable.getMessage(), throwable);
            }
        });

        Log.d("UserStatistics", "size of array: " + curTrackAudioFet.size());
    }

    @Override
    protected void onResume() {
        super.onResume();
        //updateTrackValUI();

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
    private void connected() {
        /* Subscribe to the PlayerState */
        mSpotifyAppRemote.getPlayerApi()
                .subscribeToPlayerState()
                .setEventCallback(playerState -> {
                    final com.spotify.protocol.types.Track track = playerState.track;
                    currentTrackUri = playerState.track.uri;
                    Log.d("US", "TRACK URI: " + currentTrackUri);

                    /* Get the cover art of the current playing song */
                    mSpotifyAppRemote.getImagesApi()
                            .getImage(playerState.track.imageUri, Image.Dimension.LARGE)
                            .setResultCallback(bitmap -> {
                                coverArtImg.setImageBitmap(bitmap);
                            });

                });
    }



    /**
     * Get audio analysis for the song that is playing right now
     */
    public void getTrackAudioFeatures() {
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


        try {
            Track track = spotify.getTrack("4s2BWgnSQ9NZiOQM4PP4HB");
            AudioFeaturesTrack trackAudioFeatures = spotify.getTrackAudioFeatures(track.uri);

            danceability = trackAudioFeatures.danceability;
            liveness = trackAudioFeatures.liveness;
            valence = trackAudioFeatures.liveness;
            speechiness = trackAudioFeatures.speechiness;
            instrumentalness = trackAudioFeatures.instrumentalness;

            loudness = trackAudioFeatures.loudness;
            key = trackAudioFeatures.key;
            energy = trackAudioFeatures.energy;
            tempo = trackAudioFeatures.tempo;
            acousticness = trackAudioFeatures.acousticness;

            curTrackAudioFet.add(danceability);
            curTrackAudioFet.add(liveness);
            curTrackAudioFet.add(valence);
            curTrackAudioFet.add(speechiness);
            curTrackAudioFet.add(instrumentalness);

            curTrackAudioFet.add(loudness);
            curTrackAudioFet.add(key);
            curTrackAudioFet.add(energy);
            curTrackAudioFet.add(tempo);
            curTrackAudioFet.add(acousticness);

            System.out.println("Array size inside METHOD: " + curTrackAudioFet.size());


        } catch (RetrofitError e) {
            e.getBody();
            e.getResponse();

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
     * The current song's JSON file that corresponds to it
     * @return JSON file
     */
    private JsonObject getCurrentSongJSON() {
        /* As of May 8, 2019 there is not a way from the kaae wrapper to get current song
           Let's roll our own handler.
         */

        //@GET("https://api.spotify.com/v1/me/player/currently-playing")
        // Full call:
        // GET "https://api.spotify.com/v1/me/player/currently-playing" -H "Authorization: Bearer {your access token}"

        String urlStr = "https://api.spotify.com/v1/me/player/currently-playing";
        JsonObject rootObj = null;
        JsonElement root;

        try {
            URL url = new URL(urlStr);
            URLConnection request = url.openConnection();
            // I need to tell spotify who I am with my access code
            request.setRequestProperty("Authorization", "Bearer " + accessToken);
            request.connect();

            JsonParser parser = new JsonParser(); // From gson
            try {
                root = parser.parse(new InputStreamReader((InputStream) request.getContent()));
            } catch (Exception e) {
                /* I handle the null reference below in getArtwork */
                return rootObj;
            }
                rootObj = root.getAsJsonObject();

            System.out.println("JSON FILE: " + rootObj);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return rootObj;
    }

    /**
     * This method will retrieve the url the artwork is located at and store it in coverArt variable
     * @param jsonData the current playing song's JSON file
     */
    public void getSongArtwork(JsonObject jsonData) {
        /* Always experience a cache miss, retry since next time a song is playing */
        if (jsonData == null)
            return;

        try {
            /* This will get the "images" section from the json file. */
            JsonElement images = jsonData.get("item").
                    getAsJsonObject().get("album").
                    getAsJsonObject().get("images");

            /* This will get the "name" section from the json file */
            JsonElement trackName = jsonData.get("item").
                    getAsJsonObject().get("album").
                    getAsJsonObject().get("name");

            /* This will get the "track id" section from the json file */
            JsonElement trackId = jsonData.get("item").
                    getAsJsonObject().get("id");

            String imageURL;
            JsonElement imageDetails;

            /* Obtain the section as a json array from the three elements in the section. */
            JsonArray imagesArray = images.getAsJsonArray();

            /* This will get the URL from the 640x640 cover art */
            imageDetails = imagesArray.get(0).getAsJsonObject().get("url");
            imageURL = imageDetails.toString();

            /* Lets clean up the URL by stripping the " occurances */
            imageURL = imageURL.replace("\"", "");
            currentTrack = trackName.toString().replace("\"", "");
            currentTrackUri = trackId.toString().replace("\"", "");

            System.out.println("URL: " + imageURL);
            System.out.println("Current Track: " + currentTrack);
            System.out.println("Current Track Uri: " + currentTrackUri);

            InputStream in = new URL(imageURL).openStream();

            /* Decode bitmap */
            coverArt = BitmapFactory.decodeStream(in);

            /* Re-structure bitmap */
            coverArt = coverArt.createScaledBitmap(coverArt, 900, 900, true);

            in.close();
        } catch (Exception e) {e.printStackTrace();}
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
