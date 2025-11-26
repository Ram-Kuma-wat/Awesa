package com.codersworld.awesalibs.beans.matches;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class InterviewBean implements Serializable {
    @SerializedName("id")
    int id;
    @SerializedName("match_id")
    int match_id;
    @SerializedName("created_date")
    String created_date;
    @SerializedName("video")
    private String video;
    @SerializedName("file_name")
    private String file_name;
    @SerializedName("upload_status")
    private int upload_status;
    @SerializedName("upload_type")
    private int upload_type;

    public int getUpload_type() {
        return upload_type;
    }

    public void setUpload_type(int upload_type) {
        this.upload_type = upload_type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMatch_id() {
        return match_id;
    }

    public void setMatch_id(int match_id) {
        this.match_id = match_id;
    }

    public String getCreated_date() {
        return created_date;
    }

    public void setCreated_date(String created_date) {
        this.created_date = created_date;
    }

    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }

    public String getFile_name() {
        return file_name;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }

    public int getUpload_status() {
        return upload_status;
    }

    public void setUpload_status(int upload_status) {
        this.upload_status = upload_status;
    }
}
