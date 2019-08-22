package com.example.moodvisulized;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class CardViewAdapter extends PagerAdapter {

    private List<CardAttributes> trackModels;
    private LayoutInflater layoutInflater;
    private Context context;
    private List<View> views = new ArrayList<>();


    /*********************
     * Getters & Setters *
     *********************/
    public List<View> getViews() {
        return views;
    }

    /****************
     * Constructors *
     ***************/
    public CardViewAdapter(List<CardAttributes> trackModels, Context context) {
        this.trackModels = trackModels;
        this.context = context;
    }

    /***********
     * Methods *
     ***********/
    @Override
    public int getCount() {
        return trackModels.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view.equals(o);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {

        layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.card, container, false);

        ImageView coverArt;
        TextView trackName, trackArtists;

        coverArt = (ImageView) view.findViewById(R.id.trackCoverArt);
        trackName = (TextView) view.findViewById(R.id.trackName);
        trackArtists = (TextView) view.findViewById(R.id.trackArtists);

        /* Have url, load the image and load into corresponding coverart */
        Picasso.get()
                .load(trackModels.get(position).getImage())
                .into(coverArt);
        trackName.setText(trackModels.get(position).getName());
        trackArtists.setText(trackModels.get(position).getArtist());

        container.addView(view, 0);

        views.add(view);

        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View)object);
    }
}
