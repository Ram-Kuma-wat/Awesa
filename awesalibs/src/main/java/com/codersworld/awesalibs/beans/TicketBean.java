package com.codersworld.awesalibs.beans;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class TicketBean implements Serializable {

    @SerializedName("id")
    String id;

    public TicketBean() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}