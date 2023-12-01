package com.codersworld.awesalibs.beans.support;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class SubjectsBean implements Serializable {
    @SerializedName("status")
    int status;
    @SerializedName("msg")
    String msg;
    @SerializedName("info")
    ArrayList<InfoBean> info;

    public SubjectsBean(int status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    public SubjectsBean() {
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
        @SerializedName("title")
        String title;

        public InfoBean(){}
        public InfoBean( int id, String title){
            this.id=id;
            this.title=title;
        }
        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }
}
