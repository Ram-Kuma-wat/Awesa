package com.codersworld.awesalibs.beans.matches;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class MatchesBean implements Serializable {
    @SerializedName("status")
    int status;
    @SerializedName("msg")
    String msg;
    @SerializedName("info")
    ArrayList<InfoBean> info;

    public MatchesBean(int status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    public MatchesBean() {
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
        @SerializedName("total_rows")
        int total_rows;
        @SerializedName("total_actions")
        int total_actions;
        @SerializedName("user_id")
        int user_id;
        @SerializedName("game_category")
        int game_category;
        @SerializedName("county_id")
        int county_id;
        @SerializedName("league_id")
        int league_id;
        @SerializedName("team_id")
        int team_id;
        @SerializedName("opponent_team_id")
        int opponent_team_id;
        @SerializedName("location_type")
        int location_type;
        @SerializedName("status")
        int status;
        @SerializedName("modified_date")
        String modified_date;
        @SerializedName("created_date")
        String created_date;
        @SerializedName("county_title")
        String county_title;
        @SerializedName("county_image")
        String county_image;
        @SerializedName("league_title")
        String league_title;
        @SerializedName("league_image")
        String league_image;
        @SerializedName("game_title")
        String game_title;
        @SerializedName("game_image")
        String game_image;
        @SerializedName("team1")
        String team1;
        @SerializedName("team1_image")
        String team1_image;
        @SerializedName("team2")
        String team2;
        @SerializedName("team2_image")
        String team2_image;
        @SerializedName("interview")
        String interview;
        @SerializedName("interview_thumbnail")
        String interview_thumbnail;
        @SerializedName("videos")
        ArrayList<VideosBean> videos;

        public int getTotal_actions() {
            return total_actions;
        }

        public void setTotal_actions(int total_actions) {
            this.total_actions = total_actions;
        }

        public String getInterview() {
            return interview;
        }

        public void setInterview(String interview) {
            this.interview = interview;
        }

        public String getInterview_thumbnail() {
            return interview_thumbnail;
        }

        public void setInterview_thumbnail(String interview_thumbnail) {
            this.interview_thumbnail = interview_thumbnail;
        }

        public ArrayList<VideosBean> getVideos() {
            return videos;
        }

        public void setVideos(ArrayList<VideosBean> videos) {
            this.videos = videos;
        }

        public int getTotal_rows() {
            return total_rows;
        }

        public void setTotal_rows(int total_rows) {
            this.total_rows = total_rows;
        }

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

        public int getGame_category() {
            return game_category;
        }

        public void setGame_category(int game_category) {
            this.game_category = game_category;
        }

        public int getCounty_id() {
            return county_id;
        }

        public void setCounty_id(int county_id) {
            this.county_id = county_id;
        }

        public int getLeague_id() {
            return league_id;
        }

        public void setLeague_id(int league_id) {
            this.league_id = league_id;
        }

        public int getTeam_id() {
            return team_id;
        }

        public void setTeam_id(int team_id) {
            this.team_id = team_id;
        }

        public int getOpponent_team_id() {
            return opponent_team_id;
        }

        public void setOpponent_team_id(int opponent_team_id) {
            this.opponent_team_id = opponent_team_id;
        }

        public int getLocation_type() {
            return location_type;
        }

        public void setLocation_type(int location_type) {
            this.location_type = location_type;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public String getModified_date() {
            return modified_date;
        }

        public void setModified_date(String modified_date) {
            this.modified_date = modified_date;
        }

        public String getCreated_date() {
            return created_date;
        }

        public void setCreated_date(String created_date) {
            this.created_date = created_date;
        }

        public String getCounty_title() {
            return county_title;
        }

        public void setCounty_title(String county_title) {
            this.county_title = county_title;
        }

        public String getCounty_image() {
            return county_image;
        }

        public void setCounty_image(String county_image) {
            this.county_image = county_image;
        }

        public String getLeague_title() {
            return league_title;
        }

        public void setLeague_title(String league_title) {
            this.league_title = league_title;
        }

        public String getLeague_image() {
            return league_image;
        }

        public void setLeague_image(String league_image) {
            this.league_image = league_image;
        }

        public String getGame_title() {
            return game_title;
        }

        public void setGame_title(String game_title) {
            this.game_title = game_title;
        }

        public String getGame_image() {
            return game_image;
        }

        public void setGame_image(String game_image) {
            this.game_image = game_image;
        }

        public String getTeam1() {
            return team1;
        }

        public void setTeam1(String team1) {
            this.team1 = team1;
        }

        public String getTeam1_image() {
            return team1_image;
        }

        public void setTeam1_image(String team1_image) {
            this.team1_image = team1_image;
        }

        public String getTeam2() {
            return team2;
        }

        public void setTeam2(String team2) {
            this.team2 = team2;
        }

        public String getTeam2_image() {
            return team2_image;
        }

        public void setTeam2_image(String team2_image) {
            this.team2_image = team2_image;
        }

    }
    public static class VideosBean implements  Serializable{
        @SerializedName("id")
        int id;
        @SerializedName("local_id")
        int local_id;
        @SerializedName("match_id")
        int match_id;
        @SerializedName("title")
        String title;
        @SerializedName("isDelete")
        String isDelete;
         @SerializedName("time")
        String time;
        @SerializedName("reaction")
        String reaction;
        @SerializedName("video")
        String video;
        @SerializedName("local_video")
        String local_video;
        @SerializedName("thumbnail")
        String thumbnail;
        @SerializedName("half")
        int half;
        @SerializedName("views")
        int views;

        public int getMatch_id() {
            return match_id;
        }

        public void setMatch_id(int match_id) {
            this.match_id = match_id;
        }

        public int getLocal_id() {
            return local_id;
        }

        public void setLocal_id(int local_id) {
            this.local_id = local_id;
        }

        public String getLocal_video() {
            return local_video;
        }

        public void setLocal_video(String local_video) {
            this.local_video = local_video;
        }

        public String getIsDelete() {
            return isDelete;
        }

        public void setIsDelete(String isDelete) {
            this.isDelete = isDelete;
        }

        public String getThumbnail() {
            return thumbnail;
        }

        public void setThumbnail(String thumbnail) {
            this.thumbnail = thumbnail;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getReaction() {
            return reaction;
        }

        public void setReaction(String reaction) {
            this.reaction = reaction;
        }

        public String getVideo() {
            return video;
        }

        public void setVideo(String video) {
            this.video = video;
        }

        public int getHalf() {
            return half;
        }

        public void setHalf(int half) {
            this.half = half;
        }

        public int getViews() {
            return views;
        }

        public void setViews(int views) {
            this.views = views;
        }
    }
}
