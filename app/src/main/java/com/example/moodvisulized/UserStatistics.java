package com.example.moodvisulized;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Credentials;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
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
import kaaes.spotify.webapi.android.models.UserPublic;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class UserStatistics extends AppCompatActivity {

    /* The UI variables */
    ArrayList<TextView> uiElements = new ArrayList<>(); // An array of elements that make up the UI.
    ImageView coverArtImg;                             // Current track cover art.
    int backPressed;                                // to prevent going back so fetch completes.
    public CurrentPlaying receivedObj;


    /* Debugging purposes */
    private static final String TAG = UserStatistics.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* to remove the status bar */
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_user_statistics);

        /* Need a reference to the cover art id */
        coverArtImg = (ImageView) findViewById(R.id.coverArt);

        /* Store all the audio features ui elements in an array list to parse over later */
        uiElements.add(findViewById(R.id.coverArt));
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

        /* Get the passed object and use it to populate the UI */

        Intent i = getIntent();
        receivedObj = (CurrentPlaying) i.getSerializableExtra("curTrackObj");
    }

    @Override
    protected void onStart() {super.onStart();
    /* load up the passed url */
        Log.d(TAG, "onStart: Picasso is calling: " + receivedObj.getCoverArtUrl());
        Log.d(TAG, "onStart: TEST TEST : " + receivedObj.getArtistCoverArtUrl());
        Picasso.get()
                .load(receivedObj.getCoverArtUrl()).into(coverArtImg);
        uiElements.get(1).setText(receivedObj.getDanceability());
        uiElements.get(2).setText(receivedObj.getLiveness());
        uiElements.get(3).setText(receivedObj.getValence());
        uiElements.get(4).setText(receivedObj.getSpeechiness());
        uiElements.get(5).setText(receivedObj.getInstrumentalness());

        uiElements.get(6).setText(receivedObj.getLoudness());
        uiElements.get(7).setText(receivedObj.getKey());
        uiElements.get(8).setText(receivedObj.getEnergy());
        uiElements.get(9).setText(receivedObj.getTempo());
        uiElements.get(10).setText(receivedObj.getAcousticness());
    }

    @Override
    protected void onResume() {super.onResume();}

    @Override
    protected void onStop() {super.onStop();}

    public void toHomeActivity() {

        /* I want to send the object back to the HomeActivity */
        Intent intent = new Intent(this, HomeActivity.class);

        /* The object being sent to UserStatistics Activity */
        intent.putExtra("object", receivedObj);

        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        toHomeActivity();
    }
}
