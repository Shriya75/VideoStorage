package com.farmwiseai.videostorage;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class VideoListView extends AppCompatActivity {

    private ListView listView;
    private ArrayList<VideoData> base64List;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_view);

        listView = findViewById(R.id.list);
        base64List = getBase64ListFromSharedPreferences();

        if (base64List != null) {
            ArrayList<String> videoNames = new ArrayList<>();
            for (VideoData videoData : base64List) {
                videoNames.add(videoData.getVideoName());
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, videoNames);
            listView.setAdapter(adapter);
            Toast.makeText(this, "Video list fetched", Toast.LENGTH_SHORT).show();

            listView.setOnItemClickListener((parent, view, position, id) -> {
                // Get the selected video's Base64 data
                String base64Data = base64List.get(position).getBase64Data();

                // Decode the Base64 data to create the video file
                try {
                    byte[] videoBytes = Base64.decode(base64Data, Base64.DEFAULT);
                    File videoFile = createVideoFile(videoBytes);

                    // Play the video file
                    if (videoFile != null) {
                        Uri videoUri = FileProvider.getUriForFile(this, "com.farmwiseai.videostorage.fileprovider", videoFile);
                        Intent playIntent = new Intent(Intent.ACTION_VIEW);
                        playIntent.setDataAndType(videoUri, "video/mp4");
                        playIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(playIntent);
                    } else {
                        Toast.makeText(this, "Error creating video file", Toast.LENGTH_SHORT).show();
                    }
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error decoding video", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "Null video list", Toast.LENGTH_SHORT).show();
        }
    }

    private ArrayList<VideoData> getBase64ListFromSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("my_prefs", MODE_PRIVATE);
        String json = sharedPreferences.getString("base64List", null);
        return json != null ? new Gson().fromJson(json, new TypeToken<ArrayList<VideoData>>(){}.getType()) : null;
    }

    private File createVideoFile(byte[] videoBytes) {
        File videoFile = null;
        try {
            videoFile = File.createTempFile("video", ".mp4", getCacheDir());
            FileOutputStream fos = new FileOutputStream(videoFile);
            fos.write(videoBytes);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return videoFile;
    }
}
