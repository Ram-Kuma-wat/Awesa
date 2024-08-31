package com.codersworld.awesalibs.beans;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class SponsorsBean implements Serializable {
    @SerializedName("id")
    int  id;
    @SerializedName("image")
    String  image;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
