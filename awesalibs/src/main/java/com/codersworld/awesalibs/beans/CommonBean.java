package com.codersworld.awesalibs.beans;

import com.codersworld.awesalibs.beans.matches.ScoresBean;
import com.codersworld.awesalibs.beans.user.UserBean;
 import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class CommonBean implements Serializable {
    @SerializedName("status")
    int status;
    @SerializedName("msg")
    String msg;
    @SerializedName("info")
    UserBean info;
    @SerializedName("ticket")
    TicketBean ticket;
    @SerializedName("scores")
    ArrayList<ScoresBean> scores;

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
