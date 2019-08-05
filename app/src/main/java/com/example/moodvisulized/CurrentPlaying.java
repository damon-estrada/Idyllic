package com.example.moodvisulized;

import java.io.Serializable;
import java.util.Locale;

public class CurrentPlaying implements Serializable {

    private String danceability;
    private String liveness;
    private String valence;
    private String speechiness;
    private String instrumentalness;

    private String loudness;
    private String key;
    private String energy;
    private String tempo;
    private String acousticness;

    private int duration_ms;
    private String trackUri;

    private String coverArtUrl;
    private String artistCoverArtUrl;

    /* Setters */
    public void setDanceability(String danceability) {this.danceability = danceability;}
    public void setLiveness(String liveness) {this.liveness = liveness;}
    public void setValence(String valence) {this.valence = valence;}
    public void setSpeechiness(String speechiness) {this.speechiness = speechiness;}
    public void setInstrumentalness(String instrumentalness) {this.instrumentalness = instrumentalness;}

    public void setLoudness(String loudness) {this.loudness = loudness;}
    public void setKey(String key) {this.key = key;}
    public void setEnergy(String energy) {this.energy = energy;}
    public void setTempo(String tempo) {this.tempo = tempo;}
    public void setAcousticness(String acousticness) {this.acousticness = acousticness;}

    public void setDuration_ms(int duration_ms) { this.duration_ms = duration_ms;}
    public void setTrackUri(String trackUri) {this.trackUri = trackUri;}

    public void setCoverArtUrl(String coverArtUrl) {this.coverArtUrl = coverArtUrl;}

    public void setArtistCoverArtUrl(String artistCoverArtUrl) {this.artistCoverArtUrl = artistCoverArtUrl;}
    /*--------------------------------------------------------------------------------------------*/

    /* Getters */
    public String getDanceability() {return danceability;}
    public String getLiveness() {return liveness;}
    public String getValence() {return valence;}
    public String getSpeechiness() {return speechiness;}
    public String getInstrumentalness() {return instrumentalness;}

    public String getLoudness() {return loudness;}
    public String getKey() {return key;}
    public String getEnergy() {return energy;}
    public String getTempo() {return tempo;}
    public String getAcousticness() {return acousticness;}

    public int getDuration_ms() {return duration_ms;}
    public String getTrackUri() {return trackUri;}

    public String getCoverArtUrl() {return coverArtUrl;}
    public String getArtistCoverArtUrl() {return artistCoverArtUrl;}
    /*--------------------------------------------------------------------------------------------*/

    /**
     * First constructor will create a basic current playing object
     */
    public CurrentPlaying() {
        this.danceability = "";
        this.liveness = "";
        this.valence = "";
        this.speechiness = "";
        this.instrumentalness = "";

        this.loudness = "";
        this.key = "";
        this.energy = "";
        this.tempo = "";
        this.acousticness = "";

        this.duration_ms = 0;
        this.trackUri = "";

        this.coverArtUrl = "";
        this.artistCoverArtUrl = "";
    }

    /**
     * This will generate an object with already known values for the obj.
     * @param danceability
     * @param liveness
     * @param valence
     * @param speechiness
     * @param instrumentalness
     * @param loudness
     * @param key
     * @param energy
     * @param tempo
     * @param acousticness
     * @param duration_ms
     * @param coverArtUrl
     */
    public CurrentPlaying(float danceability, float liveness, float valence, float speechiness,
                          float instrumentalness, float loudness, float key, float energy,
                          float tempo, float acousticness, int duration_ms, String trackUri,
                          String coverArtUrl, String artistCoverArtUrl) {
        this.danceability = String.format(Locale.US, "%.4s", danceability);
        this.liveness = String.format(Locale.US, "%.4s", liveness);
        this.valence = String.format(Locale.US, "%.4s", valence);
        this.speechiness = String.format(Locale.US, "%.4s", speechiness);
        this.instrumentalness = String.format(Locale.US, "%.4s", instrumentalness);

        this.loudness = String.format(Locale.US, "%.5s dB", loudness);
        this.key = String.format(Locale.US, "%s", formatKey(key));
        this.energy = String.format(Locale.US, "%.4s", energy);
        this.tempo = String.format(Locale.US, "%.5s BPM",tempo);
        this.acousticness = String.format(Locale.US, "%.4s", acousticness);

        this.duration_ms = duration_ms;
        this.trackUri = trackUri;

        this.coverArtUrl = coverArtUrl;
        this.artistCoverArtUrl = artistCoverArtUrl;
    }

    /**
     * This constructor will construct a new object based on an existing object.
     * This is useful when sending an obj to another activity.
     * @param curTrack The obj being assigned to a new object
     */
    public CurrentPlaying(CurrentPlaying curTrack) {
        this.danceability = curTrack.danceability;
        this.liveness = curTrack.liveness;
        this.valence = curTrack.valence;
        this.speechiness = curTrack.speechiness;
        this.instrumentalness = curTrack.instrumentalness;

        this.loudness = curTrack.loudness;
        this.key = curTrack.key;
        this.energy = curTrack.energy;
        this.tempo = curTrack.tempo;
        this.acousticness = curTrack.acousticness;

        this.duration_ms = curTrack.duration_ms;
        this.trackUri = curTrack.trackUri;

        this.coverArtUrl = curTrack.coverArtUrl;
        this.artistCoverArtUrl = curTrack.artistCoverArtUrl;
    }

    public String formatKey(float key) {
        switch (Math.round(key)) {
            case -1:
                return String.format("%s", KeyIdentifier.UNKOWN.stringIdentifier());
            case 0:
                return String.format("%s", KeyIdentifier.C.stringIdentifier());
            case 1:
                return String.format("%s", KeyIdentifier.CSHARP.stringIdentifier());
            case 2:
                return String.format("%s", KeyIdentifier.D.stringIdentifier());
            case 3:
                return String.format("%s", KeyIdentifier.DSHARP.stringIdentifier());
            case 4:
                return String.format("%s", KeyIdentifier.E.stringIdentifier());
            case 5:
                return String.format("%s", KeyIdentifier.F.stringIdentifier());
            case 6:
                return String.format("%s", KeyIdentifier.FSHARP.stringIdentifier());
            case 7:
                return String.format("%s", KeyIdentifier.G.stringIdentifier());
            case 8:
                return String.format("%s", KeyIdentifier.GSHARP.stringIdentifier());
            case 9:
                return String.format("%s", KeyIdentifier.A.stringIdentifier());
            case 10:
                return String.format("%s", KeyIdentifier.ASHARP.stringIdentifier());
            case 11:
                return String.format("%s", KeyIdentifier.B.stringIdentifier());
            default:
                return String.format("%s", KeyIdentifier.UNKOWN.stringIdentifier());
        }
    }

    public enum KeyIdentifier
    {
        UNKOWN(-1) {@Override public String stringIdentifier() {return "Unknown";}},
        C(0) {@Override public String stringIdentifier() {return "C";}},
        CSHARP(1) {@Override public String stringIdentifier() {return "C#";}},
        D(2) {@Override public String stringIdentifier() {return "D";}},
        DSHARP(3) {@Override public String stringIdentifier() {return "D#";}},
        E(4) {@Override public String stringIdentifier() {return "E";}},
        F(5) {@Override public String stringIdentifier() {return "F";}},
        FSHARP(6) {@Override public String stringIdentifier() {return "F#";}},
        G(7) {@Override public String stringIdentifier() {return "G";}},
        GSHARP(8) {@Override public String stringIdentifier() {return "G#";}},
        A(9) {@Override public String stringIdentifier() {return "A";}},
        ASHARP(10) {@Override public String stringIdentifier() {return "A#";}},
        B(11) {@Override public String stringIdentifier() {return "B";}};

        private int numberIdentifier;
        public abstract String stringIdentifier();

        KeyIdentifier(int numIdentifier) {this.numberIdentifier = numIdentifier;}
    }
}
