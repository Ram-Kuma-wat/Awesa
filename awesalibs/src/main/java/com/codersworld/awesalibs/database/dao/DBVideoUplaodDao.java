package com.codersworld.awesalibs.database.dao;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class DBVideoUplaodDao implements Serializable {
    @SerializedName("_id")
    private int mId;
    @SerializedName("video_name")
    private String video_name;
    @SerializedName("match_id")
    private String match_id;
    @SerializedName("video_ext")
    private String video_ext;
    @SerializedName("video_half")
    private String video_half;
    @SerializedName("video_path")
    private String video_path;
    @SerializedName("upload_status")
    private int upload_status;
    @SerializedName("date")
    private String date;

    public int getmId() {
        return mId;
    }

    public void setmId(int mId) {
        this.mId = mId;
    }

    public String getVideo_name() {
        return video_name;
    }

    public void setVideo_name(String video_name) {
        this.video_name = video_name;
    }

    public String getMatch_id() {
        return match_id;
    }

    public void setMatch_id(String match_id) {
        this.match_id = match_id;
    }

    public String getVideo_ext() {
        return video_ext;
    }

    public void setVideo_ext(String video_ext) {
        this.video_ext = video_ext;
    }

    public String getVideo_half() {
        return video_half;
    }

    public void setVideo_half(String video_half) {
        this.video_half = video_half;
    }

    public String getVideo_path() {
        return video_path;
    }

    public void setVideo_path(String video_path) {
        this.video_path = video_path;
    }

    public int getUpload_status() {
        return upload_status;
    }

    public void setUpload_status(int upload_status) {
        this.upload_status = upload_status;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
