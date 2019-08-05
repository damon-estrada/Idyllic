package com.example.moodvisulized;

import android.content.res.Resources;
import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class AllTimeFavoriteTracks extends AppCompatActivity {

    /* Debugging purposes */
    private static final String TAG = AllTimeFavoriteTracks.class.getSimpleName();

    /* Global variables */
    ArrayList<String> trackName = new ArrayList<>();
    ArrayList<String> artistNames = new ArrayList<>();
    ArrayList<String> imgUri = new ArrayList<>();
    ArrayList<ImageView> imageViews = new ArrayList<>();
    ArrayList<TextView> textViews = new ArrayList<>();

    /* Device information */
    int deviceWidth = (Resources.getSystem().getDisplayMetrics().widthPixels);
    int deviceHeight = (Resources.getSystem().getDisplayMetrics().heightPixels);

    /* Image Buttons & Info */
    ImageButton sButton;
    ImageButton mButton;
    ImageButton lButton;

    ArrayList<String> shortTerm = new ArrayList<>();
    ArrayList<String> mediumTerm = new ArrayList<>();
    ArrayList<String> longTerm = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // to remove the status bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_all_time_favorite_tracks);

        /* Add the image views */
        imageViews.add(findViewById(R.id.firstFavTrack)); imageViews.add(findViewById(R.id.secondFavTrack));
        imageViews.add(findViewById(R.id.thirdFavTrack)); imageViews.add(findViewById(R.id.fourthFavTrack));
        imageViews.add(findViewById(R.id.fifthFavTrack)); imageViews.add(findViewById(R.id.sixthFavTrack));
        imageViews.add(findViewById(R.id.seventhFavTrack)); imageViews.add(findViewById(R.id.eighthFavTrack));
        imageViews.add(findViewById(R.id.ninthFavTrack)); imageViews.add(findViewById(R.id.tenthFavTrack));

        /* Add the text views */
        textViews.add(findViewById(R.id.firstFavTrackName)); textViews.add(findViewById(R.id.firstFavTrackArtist));
        textViews.add(findViewById(R.id.secondFavTrackName)); textViews.add(findViewById(R.id.secondFavTrackArtist));
        textViews.add(findViewById(R.id.thirdFavTrackName)); textViews.add(findViewById(R.id.thirdFavTrackArtist));
        textViews.add(findViewById(R.id.fourthFavTrackName)); textViews.add(findViewById(R.id.fourthFavTrackArtist));
        textViews.add(findViewById(R.id.fifthFavTrackName)); textViews.add(findViewById(R.id.fifthFavTrackArtist));
        textViews.add(findViewById(R.id.sixthFavTrackName)); textViews.add(findViewById(R.id.sixthFavTrackArtist));
        textViews.add(findViewById(R.id.seventhFavTrackName)); textViews.add(findViewById(R.id.seventhFavTrackArtist));
        textViews.add(findViewById(R.id.eighthFavTrackName)); textViews.add(findViewById(R.id.eighthFavTrackArtist));
        textViews.add(findViewById(R.id.ninthFavTrackName)); textViews.add(findViewById(R.id.ninthFavTrackArtist));
        textViews.add(findViewById(R.id.tenthFavTrackName)); textViews.add(findViewById(R.id.tenthFavTrackArtist));

        sButton = (ImageButton) findViewById(R.id.shotTerm);
        mButton = (ImageButton) findViewById(R.id.midTerm);
        lButton = (ImageButton) findViewById(R.id.longTerm);

        /* Set listeners if the user wants to change the length of time for fav tracks */
        sButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {updateUi(shortTerm);}});

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {updateUi(mediumTerm);}});

        lButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {updateUi(longTerm);}});
    }

    @Override
    protected void onStart() {
        super.onStart();

        parseFavTracks();

        /* Update ui by default with shortTerm results. */
        updateUi(shortTerm);
    }

    /**
     * Function will populate the artist names and track titles along with the coverart.
     */
    public void parseFavTracks() {

        String filePath = getBaseContext().getFilesDir() + "/" + "favoriteTracks.txt";

        try {
            FileReader in = new FileReader(filePath);
            BufferedReader bR = new BufferedReader(in);
            String curLine;
            String[] parseLine;

            while ( (curLine = bR.readLine()) != null) {
                parseLine = curLine.split(",");
                if (!parseLine[0].equals(" ")) {
                    trackName.add(parseLine[0]);
                    artistNames.add(parseLine[1]);
                    imgUri.add(parseLine[2]);
                }
            }

            /* OMG CALLING TRIM MAKES URLs WORK WITH PICASSO */
            for (int i = 0; i < trackName.size(); i++) {
                if (i < 10) {
                    shortTerm.add(trackName.get(i).trim());
                    shortTerm.add(artistNames.get(i).trim());
                    shortTerm.add(imgUri.get(i).trim());
                } else if (i < 20) {
                    mediumTerm.add(trackName.get(i).trim());
                    mediumTerm.add(artistNames.get(i).trim());
                    mediumTerm.add(imgUri.get(i).trim());
                } else {
                    longTerm.add(trackName.get(i).trim());
                    longTerm.add(artistNames.get(i).trim());
                    longTerm.add(imgUri.get(i).trim());
                }
            }

            /*
            for (int i = 0; i < shortTerm.size(); i++) {
                Log.d(TAG, "parseFavTracks: short Term: " + shortTerm.get(i));
            }*/

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateUi(ArrayList<String> term) {

        int i;
        int j = 0;
        int k = 0;
        String imageUrl;

        for (i = 0; i < term.size(); i++) {

            /* Name of track */
            if (i % 3 == 0) {
                TextView updateTV = (TextView) textViews.get(j);
                updateTV.setText(term.get(i));
                j++;
            }

            /* Name of Artist(s) */
            else if (i % 3 == 1) {
                TextView updateTV = (TextView) textViews.get(j);
                updateTV.setText(term.get(i));
                j++;
            }

            /* Track cover art */
            else {
                imageUrl = term.get(i);
                ImageView coverArt = (ImageView) imageViews.get(k);

                Picasso.get().setLoggingEnabled(true);
                Picasso.get().load(imageUrl)
                        .resize(600, 600)
                        .into(coverArt);

                k++;
            }
        }
    }
}
