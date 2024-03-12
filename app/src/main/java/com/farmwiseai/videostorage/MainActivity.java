package com.farmwiseai.videostorage;
import java.io.ByteArrayOutputStream;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_VIDEO_CAPTURE = 1;

    private static final int MAX_VIDEO_DURATION = 30; // Maximum video duration in seconds
    private static final int TIMER_INTERVAL = 1000; // Timer update interval in milliseconds
    private boolean isRecording = false;
    private long recordingStartTime = 0;
    private static ArrayList<VideoData> base64List = new ArrayList<>();




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        clearSavedVideos();
        base64List = getBase64ListFromSharedPreferences();



        findViewById(R.id.button_capture).setOnClickListener(v -> {
            if (base64List.size() < 10) {
                dispatchTakeVideoIntent();
            } else {
                Toast.makeText(this, "Maximum videos captured", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.button_decode).setOnClickListener(v -> {
            //decodeAndPlayVideo();
        });

        findViewById(R.id.saved_video).setOnClickListener(v -> {
            ArrayList<String> videoNames = new ArrayList<>();
            for (VideoData videoData : base64List) {
                videoNames.add(videoData.getVideoName());
            }

            Intent intent = new Intent(MainActivity.this, VideoListView.class);
            intent.putStringArrayListExtra("videoNames", videoNames);

            startActivity(intent);
        });
    }

    private void dispatchTakeVideoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            File videoFile = null;
            try {
                videoFile = createVideoFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (videoFile != null) {
                Uri videoUri = FileProvider.getUriForFile(this, "com.farmwiseai.videostorage.fileprovider", videoFile);
                takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri);
                takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 30);
                takeVideoIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
            }
        }
    }

    private File createVideoFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String videoFileName = "VIDEO_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        return File.createTempFile(videoFileName, ".mp4", storageDir);
    }


//    private void decodeAndPlayVideo() {
//        VideoData lastVideoData = getLastVideoDataFromSharedPreferences();
//        if (lastVideoData == null || lastVideoData.getBase64Data() == null || lastVideoData.getBase64Data().isEmpty()) {
//            Toast.makeText(this, "No videos to decode", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        String base64String = lastVideoData.getBase64Data();
//        byte[] videoBytes = Base64.decode(base64String, Base64.DEFAULT);
//        decodedVideoFile = new File(getFilesDir(), "decoded_video.mp4");
//
//        try (InputStream inputStream = new ByteArrayInputStream(videoBytes);
//             FileOutputStream outputStream = new FileOutputStream(decodedVideoFile)) {
//
//            byte[] buffer = new byte[1024];
//            int bytesRead;
//            while ((bytesRead = inputStream.read(buffer)) != -1) {
//                outputStream.write(buffer, 0, bytesRead);
//            }
//
//            Uri videoUri = FileProvider.getUriForFile(this, "com.farmwiseai.videostorage.fileprovider", decodedVideoFile);
//            Intent playIntent = new Intent(Intent.ACTION_VIEW, videoUri);
//            playIntent.setDataAndType(videoUri, "video/mp4");
//            playIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            startActivity(playIntent);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//            Toast.makeText(this, "Error decoding video", Toast.LENGTH_SHORT).show();
//        }
//    }





    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri videoUri = data.getData();
                try {
                    String base64String = convertVideoToBase64(videoUri);
                    VideoData vd = new VideoData(("video" + (base64List.size() + 1)), base64String);
                    base64List.add(vd);

                    // Save the updated base64List to SharedPreferences
                    saveBase64ListToSharedPreferences();

                    Toast.makeText(this, "Video converted to Base64 and added to list", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error converting video to Base64", Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(this, "Video URI is null", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Video recording cancelled", Toast.LENGTH_SHORT).show();
        }
    }


    private String convertVideoToBase64(Uri videoUri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(videoUri);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;
        int totalBytesRead = 0;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            totalBytesRead += bytesRead;
            outputStream.write(buffer, 0, bytesRead);
            if (totalBytesRead > 1024 * 1024) { // Limit the buffer size to 1MB
                byte[] chunk = outputStream.toByteArray();
                String base64Chunk = Base64.encodeToString(chunk, Base64.DEFAULT);
                outputStream.reset();
                totalBytesRead = 0;
                outputStream.write(buffer, bytesRead, buffer.length - bytesRead);
                System.out.println("Converted chunk to Base64: " );// Write remaining bytes to output stream
                return base64Chunk;
            }
        }
        // Encode the remaining bytes
        byte[] remainingBytes = outputStream.toByteArray();
        String base64String = Base64.encodeToString(remainingBytes, Base64.DEFAULT);
        System.out.println("Converted remaining bytes to Base64: " ); // Add this line
        return base64String;
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveBase64ListToSharedPreferences();
    }

    @Override
    protected void onResume() {
        super.onResume();
        base64List = getBase64ListFromSharedPreferences();

    }

    private void saveBase64ListToSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("my_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(base64List);
        editor.putString("base64List", json);
        editor.apply();
        System.out.println("Saved base64List size: " + base64List.size());
    }



    private ArrayList<VideoData> getBase64ListFromSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("my_prefs", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("base64List", null);
        Type type = new TypeToken<ArrayList<VideoData>>() {}.getType();
        ArrayList<VideoData> base64List = gson.fromJson(json, type);
        if (base64List == null) {
            base64List = new ArrayList<>();
        }
        return base64List;
    }

    private String[] getVideoNamesArray() {
        ArrayList<VideoData> base64List = getBase64ListFromSharedPreferences();
        if (base64List != null) {
            String[] videoNamesArray = new String[base64List.size()];
            for (int i = 0; i < base64List.size(); i++) {
                videoNamesArray[i] = base64List.get(i).getVideoName();
            }
            return videoNamesArray;
        }
        return new String[0];
    }
    private void saveVideoNamesToSharedPreferences(ArrayList<String> videoNames) {
        SharedPreferences sharedPreferences = getSharedPreferences("my_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(videoNames);
        editor.putString("videoNames", json);
        editor.apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearSavedVideos();
    }

    private void clearSavedVideos() {
        SharedPreferences sharedPreferences = getSharedPreferences("my_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("base64List");
        editor.apply();
    }



}