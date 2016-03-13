package com.example.ivansv.issatest;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import wseemann.media.FFmpegMediaMetadataRetriever;

public class MainActivity extends AppCompatActivity {
    private MainListAdapter adapter;
    private static final String MP3_SAVED = "mp3_saved";
    private static final String MP3_SAVED_KEY = "mp3_saved_key";
    private ArrayList<String> mp3Files = new ArrayList<>();
    public static final String mp3LinksUri = "https://drive.google.com/uc?authuser=0&id=0B-ZLtSvb7CyySEFYYXQ3bVp5VU0&export=download";
    private static DownloadManager downloadManager;
    private SharedPreferences sharedPreferences;
    private int count = 0;
    private String subPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences(MP3_SAVED, MODE_PRIVATE);
        if (sharedPreferences.contains(MP3_SAVED_KEY)) {
            mp3Files = readFile();
        } else {
            getLinks();
        }
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    void initView() {
        RecyclerView recyclerViewTitles = (RecyclerView) findViewById(R.id.recycler_view);
        adapter = new MainListAdapter(mp3Files);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerViewTitles.setAdapter(adapter);
        recyclerViewTitles.setLayoutManager(layoutManager);

        ItemClickSupport.addTo(recyclerViewTitles).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                LayoutInflater inflater = MainActivity.this.getLayoutInflater();
                View playerView = inflater.inflate(R.layout.player_view, null);

                ImageButton playButton = (ImageButton) playerView.findViewById(R.id.playButton);
                ImageButton pauseButton = (ImageButton) playerView.findViewById(R.id.pauseButton);
                ImageButton cancelButton = (ImageButton) playerView.findViewById(R.id.cancelButton);

                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                        .setView(playerView);
                final AlertDialog playerDialog = builder.create();
                playerDialog.show();

                final MediaPlayer mp = new MediaPlayer();
                try {
                    mp.setDataSource(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC) + "/" +
                            mp3Files.get(position));
                    mp.prepare();
                    mp.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                playButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mp.start();
                    }
                });
                pauseButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mp.pause();
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
        });
    }

    private void getLinks() {
        downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(mp3LinksUri));
        downloadManager.enqueue(request);
    }

    private void downloadMp3Files() {
        downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(mp3Files.get(count)));
        request.setMimeType("audio/mp3");
        request.allowScanningByMediaScanner();
        subPath = "file" + count + ".mp3";
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_MUSIC, subPath);
        downloadManager.enqueue(request);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                if (!(sharedPreferences.contains(MP3_SAVED_KEY))) {
                    long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                    try {
                        BufferedReader bufferedReader = new BufferedReader(
                                new FileReader(downloadManager.openDownloadedFile(downloadId).getFileDescriptor()));
                        String current;
                        while ((current = bufferedReader.readLine()) != null) {
                            mp3Files.add(current);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(MP3_SAVED_KEY, true);
                    editor.apply();
                    adapter.notifyDataSetChanged();
                    downloadMp3Files();
                } else {
                    FFmpegMediaMetadataRetriever retriever = new FFmpegMediaMetadataRetriever();
                    String source = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC) + "/" + subPath;
                    retriever.setDataSource(source);
                    String title = retriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_TITLE) + ".mp3";
                    retriever.release();

                    File fileOld = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), subPath);
                    File fileNew = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), title);
                    fileOld.renameTo(fileNew);
                    mp3Files.remove(count);
                    mp3Files.add(count, title);
                    adapter.notifyDataSetChanged();
                    count++;
                    if (count < mp3Files.size()) {
                        downloadMp3Files();
                    } else {
                        writeFile(mp3Files);
                    }
                }
            }
        }
    };

    @SuppressWarnings("unchecked")
    private ArrayList<String> readFile() {
        try {
            FileInputStream fileInputStream = openFileInput("list.txt");
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            ArrayList<String> list = (ArrayList<String>) objectInputStream.readObject();
            objectInputStream.close();
            return list;
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void writeFile(ArrayList<String> list) {
        try {
            FileOutputStream fileOutputStream = openFileOutput("list.txt", MODE_PRIVATE);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(list);
            objectOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

