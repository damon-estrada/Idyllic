package com.example.moodvisulized;

import android.graphics.Bitmap;
import android.widget.ImageView;

public class CardAttributes {

    private String imageUrl;
    private String name;
    private String artist;

    /****************
     * Constructors *
     ***************/
    public CardAttributes(String imageUrl, String name, String artist) {
        this.imageUrl = imageUrl;
        this.name = name;
        this.artist = artist;
    }

    /*******************
     * Getters/Setters *
     *******************/
    public String getImage() {
        return imageUrl;
    }

    public void setImage(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }
}
