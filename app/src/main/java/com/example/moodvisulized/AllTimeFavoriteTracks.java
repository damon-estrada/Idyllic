package com.example.moodvisulized;

import android.animation.ArgbEvaluator;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewPager;
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
import com.squareup.picasso.Target;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class AllTimeFavoriteTracks extends AppCompatActivity {

    /* Debugging purposes */
    private static final String TAG = AllTimeFavoriteTracks.class.getSimpleName();

    /* Global variables */
    ArrayList<String> trackName = new ArrayList<>();
    ArrayList<String> artistNames = new ArrayList<>();
    ArrayList<String> imgUri = new ArrayList<>();

    /* Image Buttons & Info */
    ImageButton sButton;
    ImageButton mButton;
    ImageButton lButton;
    ImageView trackCoverArt;

    ArrayList<String> shortTerm = new ArrayList<>();
    ArrayList<String> mediumTerm = new ArrayList<>();
    ArrayList<String> longTerm = new ArrayList<>();

    ViewPager viewPager;
    CardViewAdapter cardViewAdapter;
    List<CardAttributes> trackModels;
    Integer[] colors = new Integer[10];
    ArgbEvaluator argbEvaluator = new ArgbEvaluator();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* To remove the status bar */
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_all_time_favorite_tracks);

        /* definitions of where each button/view is */
        sButton = (ImageButton) findViewById(R.id.shotTerm);
        mButton = (ImageButton) findViewById(R.id.midTerm);
        lButton = (ImageButton) findViewById(R.id.longTerm);
        trackCoverArt = (ImageView) findViewById(R.id.trackCoverArt);

        viewPager = findViewById(R.id.viewPager);

        /* Create the artist cards from the top ten list */
        trackModels = new ArrayList<>();
        parseFavTracks();

        Integer[] colorsNew = {
                getResources().getColor(R.color.black),
                getResources().getColor(R.color.black),
                getResources().getColor(R.color.black),
                getResources().getColor(R.color.black),
                getResources().getColor(R.color.black),
                getResources().getColor(R.color.black),
                getResources().getColor(R.color.black),
                getResources().getColor(R.color.black),
                getResources().getColor(R.color.black),
                getResources().getColor(R.color.black)
        };

        colors = colorsNew;

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (position < (cardViewAdapter.getCount() - 1) && position < (colors.length - 1)) {
                    viewPager.setBackgroundColor(
                            (Integer) argbEvaluator.evaluate(
                                    positionOffset,
                                    colors[position],
                                    colors[position + 1]
                            )
                    );
                } else {
                    viewPager.setBackgroundColor(colors[colors.length - 1]);
                }

            }

            @Override
            public void onPageSelected(int i) {

            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        /* Set listeners if the user wants to change the length of time for fav tracks */
        sButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {initCardView(shortTerm);}});

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {initCardView(mediumTerm);}});

        lButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {initCardView(longTerm);}});
    }

    @Override
    protected void onStart() {
        super.onStart();

        /* Update ui by default with shortTerm results. */
        initCardView(shortTerm);
        //getArtworkColors();
    }

    /**
     * Function will populate the artist names and track titles along with the coverart.
     */
    public boolean parseFavTracks() {

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
            return false;
        }

        return true;
    }

    /**
     * Initialize the card view by creating cardViewAdapter and setting its attributes.
     * @param term short, medium, or long term spanning the different time periods.
     */
    public void initCardView(ArrayList<String> term) {

        trackModels.clear(); // Clear any contents to not add onto the continued top 10
        String imageUrl;
        String trackArtist;
        String trackName;

        for (int i = 0; i < term.size(); i += 3) {
            /* Gets the third spot of every fetch which holds the url */
            trackName = term.get(i);
            trackArtist = term.get(i + 1);
            imageUrl = term.get(i + 2);

            trackModels.add(new CardAttributes(
                            imageUrl,
                            trackName,
                            trackArtist
                    )
            );
        }

        cardViewAdapter = new CardViewAdapter(trackModels, this);

        viewPager.setAdapter(cardViewAdapter);
    }

    public int getArtworkColors() {

        int curPixel, width, height = 0, redAvg = 0, greenAvg = 0, blueAvg = 0;

        Log.d(TAG, "getArtworkColors: ");

        ImageView cur = (ImageView) cardViewAdapter.getViews().get(0).findViewById(R.id.trackCoverArt);
        Bitmap bm = ((BitmapDrawable) cur.getDrawable()).getBitmap();

        for (width = 0; width < bm.getWidth(); width++) {
            for (height = 0; height < bm.getHeight(); height++) {
                curPixel = bm.getPixel(width, height);

                redAvg += Color.red(curPixel);
                greenAvg += Color.green(curPixel);
                blueAvg += Color.blue(curPixel);
            }
        }

        redAvg = redAvg / (width * height);
        greenAvg = greenAvg / (width * height);
        blueAvg = blueAvg / (width * height);


        return Color.rgb(redAvg, greenAvg, blueAvg);
    }
}
