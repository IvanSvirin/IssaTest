package com.example.ivansv.issatest;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ArrayList<String> mp3Files = new ArrayList<>();
    public static final String mp3LinksUri = "https://drive.google.com/uc?authuser=0&id=0B-ZLtSvb7CyySEFYYXQ3bVp5VU0&export=download";
    private static long downloadId;
    private static DownloadManager downloadManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getLinks();
    }

    void initView() {
        RecyclerView recyclerViewTitles = (RecyclerView) findViewById(R.id.recycler_view);
        MainListAdapter adapter = new MainListAdapter(mp3Files);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerViewTitles.setAdapter(adapter);
        recyclerViewTitles.setLayoutManager(layoutManager);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    private void getLinks() {
        downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(mp3LinksUri));
        downloadManager.enqueue(request);
    }

    private void downloadMp3File() {

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
                downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
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
                initView();
            }
        }
    };
}

