package com.codersworld.awesalibs.beans;

import androidx.annotation.Nullable;

import com.codersworld.awesalibs.beans.matches.MatchesBean;
import com.codersworld.awesalibs.beans.matches.ScoresBean;
import com.codersworld.awesalibs.beans.user.UserBean;
 import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class CommonBean implements Serializable {
    @SerializedName("status")
    int status;
    @SerializedName("app_signup_allowed")
    int app_signup_allowed;
    @SerializedName("msg")
    String msg;
    @SerializedName("token")
    String token;
    @SerializedName("total_actions")
    int totalActions;
    @SerializedName("local_id")
    String localId;
    @SerializedName("info")
    UserBean info;
    @SerializedName("ticket")
    TicketBean ticket;
    @SerializedName("videos")
    MatchesBean.VideosBean videos;
    @SerializedName("scores")
    ArrayList<ScoresBean> scores;
    @SerializedName("sponsors")
    ArrayList<SponsorsBean> sponsors;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public ArrayList<SponsorsBean> getSponsors() {
        return sponsors;
    }

    public void setSponsors(ArrayList<SponsorsBean> sponsors) {
        this.sponsors = sponsors;
    }

    public MatchesBean.VideosBean getVideos() {
        return videos;
    }

    public void setVideos(MatchesBean.VideosBean videos) {
        this.videos = videos;
    }

    public void setTotalActions(int totalActions) {
        this.totalActions = totalActions;
    }

    public int getTotalActions() {
        return totalActions;
    }

    public int getApp_signup_allowed() {
        return app_signup_allowed;
    }

    public void setApp_signup_allowed(int app_signup_allowed) {
        this.app_signup_allowed = app_signup_allowed;
    }

    public TicketBean getTicket() {
        return ticket;
    }

    public void setTicket(TicketBean ticket) {
        this.ticket = ticket;
    }

    public CommonBean(int status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    public CommonBean() {
    }

    public ArrayList<ScoresBean> getScores() {
        return scores;
    }

    public void setScores(ArrayList<ScoresBean> scores) {
        this.scores = scores;
    }

    public UserBean getInfo() {
        return info;
    }

    public void setInfo(UserBean info) {
        this.info = info;
    }

    public int getLocalId() {
        try {
            return Integer.parseInt(localId);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public void setLocalId(String localId) {
        this.localId = localId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
