package com.example.moodvisulized;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Album;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit2.Callback;

public class fetchData extends AsyncTask<Void, Void, Void> {
// https://api.myjson.com/bins/7zqgs  -> Stored information

    /*
    private String data = "";
    private String dataParsed = "";
    private String singleParsed = ""; // single json obj parsed (1 obj. only)


    // access our JSON data
    @Override
    protected Void doInBackground(Void... voids) {
        try {
            URL url = new URL("https://api.myjson.com/bins/7zqgs");

            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            InputStream inputStream = httpURLConnection.getInputStream();

            // Read the data from the stream
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line = "";

            while (line != null) {
                // Read the next line from the JSON file
                line = bufferedReader.readLine();
                data = data + line;
            }

            // This is the JSON array
            JSONArray jA = new JSONArray(data);
            for (int i = 0; i < jA.length(); i++) {
                // Each object is from the JSON file. the {} = obj
                JSONObject jO = (JSONObject) jA.get(i);
                singleParsed = "Name: " + jO.get("name") + "\n" +
                               "Password: " + jO.get("password") + "\n" +
                               "Contact: " + jO.get("contact") + "\n" +
                               "Country: " + jO.get("country") + "\n";
                // add each json obj to dataParsed
                dataParsed = dataParsed + singleParsed;
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
    */

/*
    // This is whenever the above finished
    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        MainActivity.data.setText(this.dataParsed);
    }
*/
    @Override
    protected Void doInBackground(Void... voids) {

        /*
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(SpotifyApi.SPOTIFY_WEB_API_ENDPOINT)
                .setRequestInterceptor(new RequestInterceptor() {
                    @Override
                    public void intercept(RequestFacade request) {
                        request.addHeader("Authorization", "Bearer " + accessToken);
                    }
                })
                .build();

        SpotifyService spotify = restAdapter.create(SpotifyService.class);
    */

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        //System.out.println("ALBUM RELEASE: " + spotify.getAlbum("2dIGnmEIy1WZIcZCFSj6i8").release_date);
    }
}
