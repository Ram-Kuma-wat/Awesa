package com.codersworld.awesalibs.beans.matches;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ReactionsBean implements Serializable {
    @SerializedName("id")
    private int id;
    @SerializedName("match_id")
    private int match_id;
    @SerializedName("team_id")
    private int team_id;
    @SerializedName("team_name")
    private String team_name;
    @SerializedName("half")
    private int half;
    @SerializedName("time")
    private String time;
    @SerializedName("timestamp")
    private Long timestamp;
    @SerializedName("reaction")
    private String reaction;
    @SerializedName("video")
    private String video;
    @SerializedName("file_name")
    private String file_name;
    @SerializedName("upload_status")
    private int upload_status;
    @SerializedName("created_date")
    private String created_date;

    public String getTeam_name() {
        return team_name;
    }

    public void setTeam_name(String team_name) {
        this.team_name = team_name;
    }

    public String getFile_name() {
        return file_name;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
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

    public int getTeam_id() {
        return team_id;
    }

    public void setTeam_id(int team_id) {
        this.team_id = team_id;
    }

    public int getHalf() {
        return half;
    }

    public void setHalf(int half) {
        this.half = half;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getReaction() {
        return reaction;
    }

    public void setReaction(String reaction) {
        this.reaction = reaction;
    }

    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }

    public int getUpload_status() {
        return upload_status;
    }

    public void setUpload_status(int upload_status) {
        this.upload_status = upload_status;
    }

    public String getCreated_date() {
        return created_date;
    }

    public void setCreated_date(String created_date) {
        this.created_date = created_date;
    }
}
