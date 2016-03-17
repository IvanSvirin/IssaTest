package com.example.ivansv.issatest;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

public class MainListAdapter extends RecyclerView.Adapter<MainListAdapter.MainViewHolder> {
    private static ArrayList<String> titles;
    private static boolean isPaused = false;
    private static Activity activity;

    public MainListAdapter(ArrayList<String> titles, Activity activity) {
        MainListAdapter.titles = titles;
        MainListAdapter.activity = activity;
    }

    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View v = inflater.inflate(R.layout.list_item, parent, false);
        return new MainViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MainViewHolder holder, int position) {
        holder.title = titles.get(position);
        holder.textViewTitle.setText(holder.title);
    }

    @Override
    public int getItemCount() {
        return titles.size();
    }

    static class MainViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle;
        String title;

        public MainViewHolder(View itemView) {
            super(itemView);
            textViewTitle = (TextView) itemView.findViewById(R.id.title);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setPlayer(getLayoutPosition());
                }
            });
        }
    }

    private static void setPlayer(int position) {
        LayoutInflater inflater = activity.getLayoutInflater();
        View playerView = inflater.inflate(R.layout.player_view, null);

        ImageButton playButton = (ImageButton) playerView.findViewById(R.id.playButton);
        ImageButton pauseButton = (ImageButton) playerView.findViewById(R.id.pauseButton);
        ImageButton cancelButton = (ImageButton) playerView.findViewById(R.id.cancelButton);

        final AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setView(playerView);
        final AlertDialog playerDialog = builder.create();
        playerDialog.show();

        final MediaPlayer mp = new MediaPlayer();
        try {
            mp.setDataSource(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC) + "/" +
                    titles.get(position));
            mp.prepare();
            mp.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mp.start();
                isPaused = false;
            }
        });
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPaused) {
                    mp.start();
                    isPaused = false;
                } else{
                    mp.pause();
                    isPaused = true;
                }
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mp.stop();
                playerDialog.cancel();
            }
        });
    }
}
