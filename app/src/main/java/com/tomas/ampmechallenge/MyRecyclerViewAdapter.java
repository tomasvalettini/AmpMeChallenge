package com.tomas.ampmechallenge;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.api.services.youtube.model.PlaylistItem;
import com.tomas.ampmechallenge.data.YoutubePlaylistWithImage;

import java.util.List;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.CustomViewHolder> {
    List<YoutubePlaylistWithImage> playlistWithImageList;
    private Context mContext;

    public MyRecyclerViewAdapter(Context context, List<YoutubePlaylistWithImage> feedItemList) {
        this.playlistWithImageList = feedItemList;
        this.mContext = context;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.youtube_playlist, null);
        CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, int i) {
        YoutubePlaylistWithImage item = playlistWithImageList.get(i);

        customViewHolder.imageView.setImageBitmap(item.getBitmap());
        customViewHolder.textView.setText(item.getPlaylist().getSnippet().getTitle());
    }

    @Override
    public int getItemCount() {
        return (null != playlistWithImageList ? playlistWithImageList.size() : 0);
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {
        protected ImageView imageView;
        protected TextView textView;

        public CustomViewHolder(View view) {
            super(view);
            this.imageView = (ImageView) view.findViewById(R.id.thumbnail);
            this.textView = (TextView) view.findViewById(R.id.title);
        }
    }
}