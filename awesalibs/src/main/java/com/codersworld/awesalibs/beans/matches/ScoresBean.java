package com.codersworld.awesalibs.beans.matches;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ScoresBean implements Serializable {
    @SerializedName("id")
    int id;
    @SerializedName("team1_score")
    int team1_score;
    @SerializedName("team2_score")
    int team2_score;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTeam1_score() {
        return team1_score;
    }

    public void setTeam1_score(int team1_score) {
        this.team1_score = team1_score;
    }

    public int getTeam2_score() {
        return team2_score;
    }

    public void setTeam2_score(int team2_score) {
        this.team2_score = team2_score;
    }
}
