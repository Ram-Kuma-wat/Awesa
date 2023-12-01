package com.codersworld.awesalibs.beans.login;

import com.codersworld.awesalibs.beans.user.UserBean;
 import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class LoginBean implements Serializable {
    @SerializedName("status")
    int status;
    @SerializedName("msg")
    String msg;
    @SerializedName("info")
    UserBean info;

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
