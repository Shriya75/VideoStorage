package com.farmwiseai.videostorage;
public class VideoData {
    private String videoName;
    private String base64Data;

    public VideoData(String videoName, String base64Data) {
        this.videoName = videoName;
        this.base64Data = base64Data;
    }

    public VideoData() {

    }

    public String getVideoName() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    public String getBase64Data() {
        return base64Data;
    }

    public void setBase64Data(String base64Data) {
        this.base64Data = base64Data;
    }
}
