package com.example.moodvisulized;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.spotify.android.appremote.api.PlayerApi;
import com.spotify.android.appremote.internal.PlayerApiImpl;
import com.spotify.protocol.types.PlayerContext;
import com.spotify.protocol.types.PlayerState;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import java.io.BufferedReader;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.AudioFeaturesTracks;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.SavedTrack;
import kaaes.spotify.webapi.android.models.Track;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.http.GET;
import retrofit2.http.HTTP;

public class UserStatistics extends AppCompatActivity {

    private static final int REQUEST_CODE = 1003;
    private static final String CLIENT_ID = "0184658057ca400693856a596026419b";
    private static final String REDIRECT_URI = "moodvisualized://callback";

    Button mySavedTracks;
    Button currentSong;                             // The current song playing button
    List<Track> userSavedTracks = new ArrayList<>(); // getting all saved tracks
    List<Track> userTopTracks = new ArrayList<>(); // getting all top songs
    Map<String, Object> options = new HashMap<String, Object>(); // for each call

    SpotifyService spotify;                         // Service variable
    private String accessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_statistics);

        AuthenticationRequest.Builder builder =
                new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);

        // IMPORTANT, YOU NEED TO ADD TO THE SCOPES IF YOU WANT TO ACCESS MORE INFO!!!!
        builder.setScopes(new String[]{"streaming", "user-read-private", "user-read-recently-played," +
                "user-top-read", "user-library-read, user-read-currently-playing"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);

        // Get access to spotify services
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
        // If we made it here we are good to go! Start getting service.
        spotify = restAdapter.create(SpotifyService.class);

        mySavedTracks = (Button) findViewById(R.id.mySavedTracks);
        currentSong = (Button) findViewById(R.id.currentPlaying);

        mySavedTracks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        getUserTopTracks();
                        overlay(getImageColors(getTrackAudioFeatures()));
                    }
                };

                Thread myThread = new Thread(runnable);
                myThread.start();
            }
        });

        currentSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // To prevent locking up the UI, launch a new thread.
                Runnable runnable2 = new Runnable() {
                    @Override
                    public void run() {
                        getCurrentSong();
                    }
                };

                Thread thread2 = new Thread(runnable2);
                thread2.start();
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public ArrayList<Float> getTrackAudioFeatures() {
        ArrayList<Float> results = new ArrayList<>();
        float acousticness = 0; // 0.0 - 1.0 (acoustic)
        //android.os.Parcelable.Creator<AudioFeaturesTrack> creator;
        float danceability = 0; // (not danceable) 0.0 - 1.0 (danceable)
        int durationMs = 0; // How long the song is in ms
        float energy = 0;   // 0.0 - 1.0 (loud, fast, noisy)
        float instrumentalness = 0; // (vocals present) 0.0 - 1.0 (no vocal content)
        float loudness = 0; // -60 - 0 (how loud a song is)
        float tempo = 0;  // BPM measured in each song
        float valence = 0; // (angry, depresses, sad ) 0.0 - 1.0 (most positive, happy, cheerful track)

        try {
            int totalTracks = userTopTracks.size();

            // Compute for all parameters above
            for (Track track : userTopTracks) {
                AudioFeaturesTracks audioFeaturesTracks = spotify.getTracksAudioFeatures(track.id);

                acousticness += audioFeaturesTracks.audio_features.get(0).acousticness;
                danceability += audioFeaturesTracks.audio_features.get(0).danceability;
                durationMs += audioFeaturesTracks.audio_features.get(0).duration_ms;
                energy += audioFeaturesTracks.audio_features.get(0).energy;
                instrumentalness += audioFeaturesTracks.audio_features.get(0).instrumentalness;
                loudness += audioFeaturesTracks.audio_features.get(0).loudness;
                tempo += audioFeaturesTracks.audio_features.get(0).tempo;
                valence += audioFeaturesTracks.audio_features.get(0).valence;
            }
            results.add(acousticness / totalTracks);
            results.add(danceability / totalTracks);
            results.add( (float) (durationMs / totalTracks) );
            results.add(energy / totalTracks);
            results.add(instrumentalness / totalTracks);
            results.add(loudness / totalTracks);
            results.add(tempo / totalTracks);
            results.add(valence / totalTracks);

        } catch (RetrofitError e) {
            System.out.println(e.getResponse().getStatus());
            System.out.println(e.getResponse().getReason());
        }

        return results;
    }

    public void getUserTopTracks() {
        try {
            int totalTopSongs = 0;
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
     * [ acousticness, danceability, duration, energy, instrumentalness,
     *   loudness, tempo, valence] --> 1, 3, 6, 7 -----> 0, 1, 2, 3, 4
     *
     */
    public void overlay(ArrayList<Integer> colors) {

        // Get your images from their files
        try {
            // Access the drawable resources
            Resources res = getResources();
            Drawable dI1 = res.getDrawable(R.drawable.danceability1_img);
            Drawable dI2 = res.getDrawable(R.drawable.danceability2_img);
            Drawable eI = res.getDrawable(R.drawable.energy_img);
            Drawable tI = res.getDrawable(R.drawable.tempo_img);
            Drawable vI = res.getDrawable(R.drawable.valence_img);

            // Format: 0x   00      00      00      00
            //              alpha   red     green   blue
            // (0x00) 0 - 255 (OxFF)
            dI1.setColorFilter(colors.get(0), PorterDuff.Mode.MULTIPLY);
            dI2.setColorFilter(colors.get(1), PorterDuff.Mode.MULTIPLY);
            eI.setColorFilter(colors.get(2), PorterDuff.Mode.MULTIPLY);
            tI.setColorFilter(colors.get(3), PorterDuff.Mode.MULTIPLY);
            vI.setColorFilter(colors.get(4), PorterDuff.Mode.MULTIPLY);

            // Create an array of drawable resources.
            Drawable[] stackImgs = new Drawable[6];
            stackImgs[0] = res.getDrawable(R.drawable.mood_image);
            stackImgs[1] = dI1;
            stackImgs[2] = dI2;
            stackImgs[3] = eI;
            stackImgs[4] = tI;
            stackImgs[5] = vI;

            // Stack the images ontop of each other
            LayerDrawable layerDrawable = new LayerDrawable(stackImgs);
            // display the image
            ImageView imageView = (ImageView) findViewById(R.id.moodPicture);
            imageView.setImageDrawable(layerDrawable);

        } catch (Exception e) {
            // Path to images not found.
            System.out.println("IMAGES NOT FOUND: " + e);
        }
    }


    /**
     *
     * @param results Ordered as: [ acousticness, danceability, duration, energy, instrumentalness,
     *      *                             loudness, tempo, valence]
     * @return and arraylist of colors computed by parameters to pain the iage.
     */

    public ArrayList<Integer> getImageColors(ArrayList<Float> results) {
        ArrayList<Integer> imageColors = new ArrayList<>();
        System.out.println("danceability 1: " + results.get(1));
        System.out.println("energy: " + results.get(3));
        System.out.println("tempo: " + results.get(6));
        System.out.println("Valence: " + results.get(7));

        for (int i = 0; i < results.size(); i++) {
            switch (i) {
                case 2: // danceability [not active = <.50] [Active = >.50]
                    if (results.get(i) >= 0.5) {
                        imageColors.add(Color.MAGENTA);
                        // lighter shade of magenta
                        imageColors.add(Color.rgb(118, 21, 178));
                    } else {
                        //whiteish grey
                        imageColors.add(Color.rgb(227, 220, 232));
                        //lighter grey
                        imageColors.add(Color.rgb(222, 219, 224));
                    }
                break;
                case 3: // energy
                    if (results.get(i) >= 0.5) {
                        imageColors.add(Color.RED);
                    } else {
                        imageColors.add(Color.rgb(28, 28, 33));
                    }
                    break;
                case 6: // tempo
                    if (results.get(i) >= 130) {
                        // blueish
                        imageColors.add(Color.rgb(0, 225, 255));
                    } else {
                        // gray
                        imageColors.add(Color.rgb(104, 104, 127));
                    }
                    break;
                case 7: // valence
                    if (results.get(i) >= 0.5) {
                        // orange
                        imageColors.add(Color.rgb(255 ,144 ,0));
                    } else {
                        imageColors.add(Color.CYAN);
                    }
            }
        }
        return imageColors;
    }

    private void getCurrentSong() {
        // As of May 8, 2019 there is not a way from the kaae wrapper to get current song
        // Let's roll our own handler.

        //@GET("https://api.spotify.com/v1/me/player/currently-playing")
        // Full call:
        // GET "https://api.spotify.com/v1/me/player/currently-playing" -H "Authorization: Bearer {your access token}"


        String urlStr = "https://api.spotify.com/v1/me/player/currently-playing";

        try {
            URL url = new URL(urlStr);
            URLConnection request = url.openConnection();
            // I need to tell spotify who I am with my access code
            request.setRequestProperty("Authorization", "Bearer " + accessToken);
            request.connect();

            JsonParser parser = new JsonParser(); // From gson
            JsonElement root = parser.parse(new InputStreamReader((InputStream) request.getContent()));
            JsonObject rootObj = root.getAsJsonObject();
            // For some reason and thanks to stack overflow the blow get().getAsString() does not
            // work. the .toString() works however.
            String currentSong = rootObj.get("name").toString();

            System.out.println(currentSong);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /**
     * getUserTopTracks() gets the user's whole saved song library.
     */
    public void getUserSavedLibrary() {
        try {
            int totalSongsInLibrary = 0;
            int i = 0;
            // Limit is how many songs we can grab at a time (Max is 50 for this call)
            int limit = 50;
            int updateIndexBy = 50; // where to start the new index in the library.
            // Offset is the starting point of which we index our songs
            // REMEMBER: update offset to get more than 50 songs. It starts at 0
            // and when updated to (offset = 50) will start at the 50th song while
            // (limit) will fetch the next 50 songs in this case.
            int offset = 0;
            Pager<SavedTrack> savedTrackPager = new Pager<>();

            totalSongsInLibrary = spotify.getMySavedTracks().total;
            options.put(SpotifyService.LIMIT, limit); // get 50 songs at a time.
            options.put(SpotifyService.OFFSET, offset); // initially 0 to start at top of library.

            while (i <= totalSongsInLibrary) {
                savedTrackPager = spotify.getMySavedTracks(options);
                for (SavedTrack savedTrack : savedTrackPager.items) {
                    userSavedTracks.add(savedTrack.track); // save the track from the fetch
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

            //Message msg = handler.obtainMessage();
            //Bundle bundle = new Bundle();
            //String numberOfPlaylists = Integer.toString(userPlaylists.describeContents());
            //System.out.println("Test: " + userProfilePublic.id);
            //bundle.putString("Key", numberOfPlaylists);
            //msg.setData(bundle);
            //handler.sendMessage(msg);
        } catch (RetrofitError e) {
            System.out.println(e.getResponse().getStatus());
            System.out.println(e.getResponse().getReason());
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle bundle = msg.getData();
            String string = bundle.getString("Key");
            // Update the text view with the release date of the album
            //TextView tv = (TextView) findViewById(R.id.albumName);
            //tv.setText(string);
        }
    };

    /**
     *
     * @param requestCode
     * @param resultCode
     * @param intent
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);

            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    // Handle successful response
                    accessToken = response.getAccessToken();
                    break;

                // Auth flow returned an error
                case ERROR:
                    // Handle error response
                    System.out.println("We reached a failure in onActivityResult method");
                    break;

                // Most likely auth flow was cancelled
                default:
                    // Handle other cases
            }
        }
    }
}
