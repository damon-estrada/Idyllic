package com.example.moodvisulized;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;

import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;

public class AllTimeFavorite implements Serializable {

    /* Debugging purposes */
    private static final String TAG = AllTimeFavorite.class.getSimpleName();

    private ArrayList<Track> favTrackList;

    public ArrayList<Track> getFavTrackList() {
        return favTrackList;
    }

    /**
     * Setter will set the favorite tracks to the already inii object
     * @param favTrackList the user's favorite tracks (10) of them.
     */
    public void setFavTrackList(Pager<Track> favTrackList) {
        this.favTrackList.addAll(favTrackList.items);
    }

    /* Constructor will instatiate a new object */
    public AllTimeFavorite() {
        this.favTrackList = new ArrayList<>();
    }


    public void giveReport() {
        Log.d(TAG, "giveReport: All Time favorite Tracks ");
        for (int i = 0; i < favTrackList.size(); i++) {
            Log.d(TAG, "Name: " + favTrackList.get(i).name);
            Log.d(TAG, "Artist: " + favTrackList.get(i).artists.get(0).name);
            Log.d(TAG, "-------------------------------------------------");
        }
    }
}
