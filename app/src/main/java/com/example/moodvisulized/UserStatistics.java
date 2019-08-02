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
    ArrayList<String> uiValues = new ArrayList<>();  // Value of all track audio features.
    private static ArrayList<String> uiValuesClone = new ArrayList<>(); // copy of uiValues
    int backPressed;                                // to prevent going back so fetch completes.
    Bitmap bmCoverArt;
    static Bitmap bmCoverArtClone;
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
        startUiTask();
    }

    @Override
    protected void onStart() {super.onStart();}

    @Override
    protected void onResume() {super.onResume();}

    @Override
    protected void onStop() {super.onStop();}

    public void toHomeActivity() {

        /* I want to send the object back to the HomeActivity */
        Intent intent = new Intent(this, HomeActivity.class);

        Log.d(TAG, "toHomeActivity: Activity sending Object with data");

        /* The object being sent to UserStatistics Activity */
        intent.putExtra("object", receivedObj);

        startActivity(intent);
    }



    public void startUiTask() {
        UpdateUiAsync task = new UpdateUiAsync(this);
        task.execute();
    }

    private static class UpdateUiAsync extends AsyncTask<Void, Void, Void> {

        private WeakReference<UserStatistics> activityWeakRef;

        UpdateUiAsync(UserStatistics activity) {
            activityWeakRef = new WeakReference<>(activity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            /* get strong reference which will be destroyed after this scope ends */
            UserStatistics activity = activityWeakRef.get();

            /* This will return if garbage collection is happening so we return immediately */
            if (activity == null || activity.isFinishing()) {
                return;
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            /* get strong reference which will be destroyed after this scope ends */
            UserStatistics activity = activityWeakRef.get();

            /* This will return if garbage collection is happening so we return immediately */
            if (activity == null || activity.isFinishing()) {
                return null;
            }

            /* Use this to retry the fetch just in case the user is too fast for fetch of the
               track audio features.
             */
            try {
                Log.d(TAG, "doInBackground: Retrieving CoverArt");
                String imgUrl = "https://i.scdn.co/image/" + activity.receivedObj.getCoverArtUrl();

                URL url = new URL(imgUrl);
                activity.bmCoverArt = BitmapFactory.decodeStream(url.openStream());

                activity.bmCoverArtClone = activity.bmCoverArt;

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        /* update the UI with the object information */
                        activity.coverArtImg.setImageBitmap(activity.bmCoverArt);
                        activity.uiElements.get(1).setText(activity.receivedObj.getDanceability());
                        activity.uiElements.get(2).setText(activity.receivedObj.getLiveness());
                        activity.uiElements.get(3).setText(activity.receivedObj.getValence());
                        activity.uiElements.get(4).setText(activity.receivedObj.getSpeechiness());
                        activity.uiElements.get(5).setText(activity.receivedObj.getInstrumentalness());

                        activity.uiElements.get(6).setText(activity.receivedObj.getLoudness());
                        activity.uiElements.get(7).setText(activity.receivedObj.getKey());
                        activity.uiElements.get(8).setText(activity.receivedObj.getEnergy());
                        activity.uiElements.get(9).setText(activity.receivedObj.getTempo());
                        activity.uiElements.get(10).setText(activity.receivedObj.getAcousticness());
                    }
                });

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
            UserStatistics activity = activityWeakRef.get();

            /* This will return if garbage collection is happening so we return immediately */
            if (activity == null || activity.isFinishing()) {
                return;
            }

            /* First song success will yield playing data.
               However, if same song is playing, clone to reuse for next time.

               Also, if a new song plays, we need to dump the contents to prevent the
               list from gorwing and getting a null pointer exception as mentioned below.
            */
            activity.uiValuesClone.clear();
            activity.uiValuesClone.addAll(activity.uiValues);
            Log.d(TAG, "POST EXECUTE UICLONE SIZE: " + activity.uiValuesClone.size());

            /* We need to dump the contents of uiValues since it keeps adding to the list
             * Which is problematic when the user keeps exiting and coming back to the activity
             * which throws an exception since uiElements only has 11 elements and uiValues
             * will continue to grow.
             * Therefore, uiElements does not have an index 12 so Error.*/
            activity.uiValues.clear();
        }
    }

    public void startUiClone() {
        UpdateUiAsyncClone task = new UpdateUiAsyncClone(this);
        task.execute();
    }

    private static class UpdateUiAsyncClone extends AsyncTask<Void, Void, Void> {

        private WeakReference<UserStatistics> activityWeakRef;

        UpdateUiAsyncClone(UserStatistics activity) {
            activityWeakRef = new WeakReference<>(activity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            /* get strong reference which will be destroyed after this scope ends */
            UserStatistics activity = activityWeakRef.get();

            /* This will return if garbage collection is happening so we return immediately */
            if (activity == null || activity.isFinishing()) {
                return;
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            /* get strong reference which will be destroyed after this scope ends */
            UserStatistics activity = activityWeakRef.get();

            /* This will return if garbage collection is happening so we return immediately */
            if (activity == null || activity.isFinishing()) {
                return null;
            }

            /* Use this to retry the fetch just in case the user is too fast for fetch of the
               track audio features.
             */
            try {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        activity.coverArtImg.setImageBitmap(activity.bmCoverArtClone);

                        /* Start at 1 since CoverArt Url is at index 0 */
                        for (int i = 1; i < activity.uiValuesClone.size(); i++) {

                            /* On every iteration, get the txt to be updated */
                            activity.uiElements.get(i)
                                    .setText(activity.uiValuesClone.get(i));
                        }
                    }
                });

                Log.d(TAG, "doInBackground UICLONE SIZE: " + activity.uiValuesClone.size());
                for (int i = 0; i < activity.uiValuesClone.size(); i++) {
                    Log.d(TAG, "doInBackground UICLONE: " + activity.uiValuesClone.get(i));
                }
                Log.d(TAG, "doInBackground UICLONE: FINISHED");
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            /* get strong reference which will be destroyed after this scope ends */
            UserStatistics activity = activityWeakRef.get();

            /* This will return if garbage collection is happening so we return immediately */
            if (activity == null || activity.isFinishing()) {
                return;
            }
        }
    }

    /**
     * When the back button is pressed, I do not want to go back unless the fetch has been completed.
     */
    @Override
    public void onBackPressed() {
        if (backPressed < 11 && backPressed > 1) {
            Toast.makeText(getApplicationContext(), "Fetch not finished yet", Toast.LENGTH_SHORT).show();
        } else {
            super.onBackPressed();
            toHomeActivity();
        }
    }
}
