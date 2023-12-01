package com.codersworld.awesalibs.beans.teams;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class TeamsBean implements Serializable {
    @SerializedName("status")
    int status;
    @SerializedName("msg")
    String msg;
    @SerializedName("info")
    ArrayList<InfoBean> info;
    @SerializedName("favourite")
    ArrayList<InfoBean> favourite;

    public TeamsBean(int status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    public TeamsBean() {
    }

    public ArrayList<InfoBean> getFavourite() {
        return favourite;
    }

    public void setFavourite(ArrayList<InfoBean> favourite) {
        this.favourite = favourite;
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
        @SerializedName("game_category")
        int game_category;
        @SerializedName("slug")
        String slug;
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

        public int getGame_category() {
            return game_category;
        }

        public void setGame_category(int game_category) {
            this.game_category = game_category;
        }

        public String getSlug() {
            return slug;
        }

        public void setSlug(String slug) {
            this.slug = slug;
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
