package com.codersworld.awesalibs.beans.support;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class TicketsBean implements Serializable {
    @SerializedName("status")
    int status;
    @SerializedName("msg")
    String msg;
    @SerializedName("info")
    ArrayList<InfoBean> info;

    public TicketsBean(int status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    public TicketsBean() {
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
        @SerializedName("user_id")
        int user_id;
        @SerializedName("name")
        String name;
        @SerializedName("email")
        String email;
        @SerializedName("subject")
        String subject;
        @SerializedName("comment")
        String comment;
        @SerializedName("source")
        String source;
        @SerializedName("status")
        String status;
        @SerializedName("modified_date")
        String modified_date;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getUser_id() {
            return user_id;
        }

        public void setUser_id(int user_id) {
            this.user_id = user_id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getModified_date() {
            return modified_date;
        }

        public void setModified_date(String modified_date) {
            this.modified_date = modified_date;
        }
    }
}
