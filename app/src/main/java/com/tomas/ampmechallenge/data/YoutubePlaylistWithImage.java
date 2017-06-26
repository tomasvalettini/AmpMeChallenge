package com.tomas.ampmechallenge.data;

import android.graphics.Bitmap;

import com.google.api.services.youtube.model.Playlist;

public class YoutubePlaylistWithImage {
    private Playlist playlist;
    private Bitmap bitmap;

    public YoutubePlaylistWithImage(Playlist p) {
        playlist = p;
    }

    public Playlist getPlaylist() {
        return playlist;
    }

    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}