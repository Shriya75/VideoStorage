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
import java.lang.reflect.Type;
import java.util.ArrayList;

public class VideoListView extends AppCompatActivity {

    private ListView listView;
    private ArrayList<VideoData> base64List;
    private int videoCounter = 1; // Initialize a counter for the video

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
                // Decode and play the selected video
                String base64Data = base64List.get(position).getBase64Data();
                byte[] videoBytes = Base64.decode(base64Data, Base64.DEFAULT);

                // Save the decoded video with a unique name
                String videoFileName = "decoded_video_" + videoCounter + ".mp4";
                File decodedVideoFile = new File(getFilesDir(), "decoded_videos/" + videoFileName);
                Uri videoUri = FileProvider.getUriForFile(this, "com.farmwiseai.videostorage.fileprovider", decodedVideoFile);

                // Increment the videoCounter for the next video
                videoCounter++;

                // Use the videoUri for playing the video or sharing it, etc.
                Intent playIntent = new Intent(Intent.ACTION_VIEW);
                playIntent.setDataAndType(videoUri, "video/mp4");
                playIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(playIntent);
            });
        } else {
            Toast.makeText(this, "Null video list", Toast.LENGTH_SHORT).show();
        }
    }

    private ArrayList<VideoData> getBase64ListFromSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("my_prefs", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("base64List", null);
        Type type = new TypeToken<ArrayList<VideoData>>() {}.getType();
        return gson.fromJson(json, type);
    }
}
