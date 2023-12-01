package com.codersworld.awesalibs.beans.game;

import com.codersworld.awesalibs.beans.TicketBean;
import com.codersworld.awesalibs.beans.user.UserBean;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class GameBean implements Serializable {
    @SerializedName("status")
    int status;
    @SerializedName("msg")
    String msg;
    @SerializedName("info")
    ArrayList<InfoBean> info;

    public GameBean(int status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    public GameBean() {
    }

    public ArrayList<InfoBean> getInfo() {
        return info;
    }

    public void setInfo(ArrayList<InfoBean> info) {
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

    public static class InfoBean implements Serializable {
        @SerializedName("id")
        int id;
        @SerializedName("county")
        int county;
        @SerializedName("county_id")
        int county_id;
        @SerializedName("title")
        String title;
        @SerializedName("image")
        String image;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getCounty() {
            return county;
        }

        public void setCounty(int county) {
            this.county = county;
        }

        public int getCounty_id() {
            return county_id;
        }

        public void setCounty_id(int county_id) {
            this.county_id = county_id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }
    }
}
