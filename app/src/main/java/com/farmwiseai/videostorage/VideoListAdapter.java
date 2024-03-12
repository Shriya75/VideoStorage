package com.farmwiseai.videostorage;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
public class VideoListAdapter extends ArrayAdapter<VideoData> {

    private Context context;
    private ArrayList<VideoData> videoNames;

    public VideoListAdapter(Context context, ArrayList<VideoData> videoNames) {
        super(context, R.layout.list_item_video, videoNames);
        this.context = context;
        this.videoNames = videoNames;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_video, parent, false);
        }


        TextView text1 = convertView.findViewById(android.R.id.text1);
        text1.setText(videoNames.get(position).getVideoName());
        return convertView;

    }
    
}

