package com.example.moodvisulized;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.spotify.android.appremote.api.error.UserNotAuthorizedException;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import org.w3c.dom.Text;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyCallback;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.Playlist;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import kaaes.spotify.webapi.android.models.SavedTrack;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.UserPrivate;
import kaaes.spotify.webapi.android.models.UserPublic;
import retrofit.Callback;
import retrofit.ErrorHandler;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit2.Call;

public class UserStatistics extends AppCompatActivity {

    private static final int REQUEST_CODE = 1003;
    private static final String CLIENT_ID = "0184658057ca400693856a596026419b";
    private static final String REDIRECT_URI = "moodvisualized://callback";
    Album album;                            // info on the current album
    Map<String, Object> options = new HashMap<String, Object>(); // for each call
    Button mySavedTracks;
    private String accessToken;
    List<Track> userSavedTracks = new ArrayList<>(); // getting all saved tracks

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_statistics);

        AuthenticationRequest.Builder builder =
                new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);

        // IMPORTANT, YOU NEED TO ADD TO THE SCOPES IF YOU WANT TO ACCESS MORE INFO!!!!
        builder.setScopes(new String[]{"streaming", "user-read-private", "user-read-recently-played," +
                "user-top-read", "user-library-read"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);

        mySavedTracks = (Button) findViewById(R.id.mySavedTracks);

        mySavedTracks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        getUserTopTracks();
                    }
                };

                Thread myThread = new Thread(runnable);
                myThread.start();
            }
        });
    }

    public void getAlbumInfo() {
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

        SpotifyService spotify = restAdapter.create(SpotifyService.class);

        try {
            album = spotify.getAlbum("47KIS7mtjSEIhfUkMmo30e");
            Message msg = handler.obtainMessage();
            Bundle bundle = new Bundle();
            String releaseDate = album.release_date;
            bundle.putString("Key", releaseDate);
            msg.setData(bundle);
            handler.sendMessage(msg);
        } catch (RetrofitError e) {
            System.out.println(e.getResponse().getStatus());
            System.out.println(e.getResponse().getReason());
        }
    }

    public void getUserTopTracks() {

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

        SpotifyService spotify = restAdapter.create(SpotifyService.class);

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
                System.out.println("I counter: " + i);
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

            System.out.println("Hello friend, it has been awhile since you were successful");
            for (i = 0; i < userSavedTracks.size(); i++) {
                System.out.println("Track: " + userSavedTracks.get(i).name + "by -> "
                        + userSavedTracks.get(i).artists.get(0).name);
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
            TextView tv = (TextView) findViewById(R.id.albumName);
            tv.setText(string);
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
                    // Whenever we want to update our interface, it is bad practice to do so inside a thread
                    // (PROGRAM CRASH, thread locks, etc)
                    accessToken = response.getAccessToken();
                    System.out.println("MY ACCESS TOKEN: " + accessToken);
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
