package com.codersworld.awesalibs.rest;


import com.codersworld.awesalibs.beans.CommonBean;
import com.codersworld.awesalibs.beans.county.CountyBean;
import com.codersworld.awesalibs.beans.game.GameBean;
import com.codersworld.awesalibs.beans.leagues.LeagueBean;
import com.codersworld.awesalibs.beans.matches.MatchesBean;
import com.codersworld.awesalibs.beans.support.SubjectsBean;
import com.codersworld.awesalibs.beans.support.TicketsBean;
import com.codersworld.awesalibs.beans.teams.TeamsBean;
import com.codersworld.awesalibs.utils.ProgressCallback;
import com.codersworld.awesalibs.utils.Tags;


import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Streaming;
import retrofit2.http.Tag;

public interface ApiRequest {

    @POST(Tags.SB_LOGIN_API)
    @FormUrlEncoded
    Call<CommonBean> userLogin(@Field("username") String strUsername, @Field("password") String strPassword, @Field("device_id") String strFCMCode, @Field("version") String strVersion, @Field("login_id") String login_id, @Field("device_type") String device_type, @Field("device_model") String device_model);

    @POST(Tags.SB_SIGNUP_API)
    @FormUrlEncoded
    Call<CommonBean> userSignUp(@Field("firstname") String str1, @Field("lastname") String str2,
                                @Field("email") String str3, @Field("phone") String str4,
                                @Field("username") String str5, @Field("password") String str6,
                                @Field("confirm_password") String confirm_password,
                                @Field("device_id") String str7, @Field("version") String str8, @Field("login_id") String login_id, @Field("device_type") String device_type, @Field("device_model") String device_model);

    @POST(Tags.SB_RESEND_OTP_API)
    @FormUrlEncoded
    Call<CommonBean> resendOTP(@Field("email") String str3, @Field("device_id") String str7, @Field("version") String str8, @Field("from") String str9, @Field("login_id") String login_id, @Field("device_type") String device_type, @Field("device_model") String device_model);

    @POST(Tags.SB_SIGNUP_VERIFY_OTP_API)
    @FormUrlEncoded
    Call<CommonBean> verifySignUpOTP(@Field("email") String str3, @Field("otp") String str4, @Field("device_id") String str7, @Field("version") String str8, @Field("type") String type, @Field("login_id") String login_id, @Field("device_type") String device_type, @Field("device_model") String device_model);

    @POST(Tags.SB_FORGOT_PASSWORD_API)
    @FormUrlEncoded
    Call<CommonBean> forgotPassword(@Field("email") String strEmail, @Field("action") String strAction, @Field("login_id") String login_id, @Field("device_type") String device_type, @Field("device_model") String device_model);


    @POST(Tags.SB_FORGOT_PASSWORD_RESET_API)
    @FormUrlEncoded
    Call<CommonBean> resetPassword(@Field("password") String strPassword, @Field("email") String strEmail, @Field("otp") String strOTP, @Field("login_id") String login_id, @Field("device_type") String device_type, @Field("device_model") String device_model);

    @Multipart
    @POST(Tags.SB_UPDATE_PROFILE_API)
    Call<CommonBean> updateProfile(@Part("firstname") RequestBody firstname,
                                   @Part("lastname") RequestBody lastname,
                                   @Part("user_id") RequestBody user_id,
                                   @Part("phone") RequestBody phone,
                                   @Part("username") RequestBody username,
                                   @Part("email") RequestBody email,
                                   @Part("login_id") String login_id,
                                   @Part("device_type") String device_type,
                                   @Part("device_model") String device_model);

    @Multipart
    @POST(Tags.SB_UPDATE_PROFILE_API)
    Call<CommonBean> updateProfileWithImage(@Part("firstname") RequestBody firstname,
                                            @Part("lastname") RequestBody lastname,
                                            @Part("user_id") RequestBody user_id,
                                            @Part("phone") RequestBody phone,
                                            @Part("username") RequestBody username,
                                            @Part("email") RequestBody email,
                                            @Part MultipartBody.Part image,
                                            @Part("login_id") String login_id,
                                            @Part("device_type") String device_type,
                                            @Part("device_model") String device_model);




    @POST(Tags.SB_API_SUPPORT)
    @FormUrlEncoded
    Call<TicketsBean> supportTicket(@Field("user_id") String user_id,
                                    @Field("name") String name,
                                    @Field("email") String email,
                                    @Field("subject_id") String subject_id,
                                    @Field("subject") String subject,
                                    @Field("comment") String comment,
                                    @Field("source") String source,
                                    @Field("login_id") String login_id,
                                    @Field("device_type") String device_type,
                                    @Field("device_model") String device_model);

    @POST(Tags.SB_GAME_CATEGORY_API)
    @FormUrlEncoded
    Call<GameBean> getGames(@Field("user_id") String user_id, @Field("device_id") String device_id, @Field("login_id") String login_id, @Field("device_type") String device_type, @Field("device_model") String device_model);

    @POST(Tags.SB_TEAMS_API)
    @FormUrlEncoded
    Call<TeamsBean> getTeams(@Field("search") String search,
                             @Field("user_id") String user_id,
                             @Field("game_category") String game_category,
                             @Field("league_id") String league_id,
                             @Field("login_id") String login_id,
                             @Field("device_type") String device_type,
                             @Field("device_model") String device_model);

    @POST(Tags.SB_OPPONENT_TEAMS_API)
    @FormUrlEncoded
    Call<TeamsBean> getOpponentTeams(@Field("search") String search,
                                     @Field("user_id") String user_id,
                                     @Field("game_category") String game_category,
                                     @Field("team_id") String team_id,
                                     @Field("league_id") String league_id,
                                     @Field("login_id") String login_id,
                                     @Field("device_type") String device_type,
                                     @Field("device_model") String device_model);

    @POST(Tags.SB_LEAGUE_API)
    @FormUrlEncoded
    Call<LeagueBean> getLeagues(@Field("search") String search,
                                @Field("user_id") String user_id,
                                @Field("game_category") String game_category,
                                @Field("county") String county,
                                @Field("team_id") String team_id,
                                @Field("login_id") String login_id,
                                @Field("device_type") String device_type,
                                @Field("device_model") String device_model);

    @POST(Tags.SB_USER_MATCHES_API)
    @FormUrlEncoded
    Call<MatchesBean> getMatches(@Field("page") String page,
                                 @Field("search") String search,
                                 @Field("user_id") String user_id,
                                 @Field("game_category") String game_category,
                                 @Field("game_id") String game_id,
                                 @Field("login_id") String login_id,
                                 @Field("device_type") String device_type,
                                 @Field("device_model") String device_model);

    @POST(Tags.SB_MATCH_DETAIL_API)
    @FormUrlEncoded
    Call<MatchesBean> getMatchDetail(@Field("user_id") String user_id,
                                     @Field("game_id") String game_id,
                                     @Field("login_id") String login_id,
                                     @Field("device_type") String device_type,
                                     @Field("device_model") String device_model);

    @POST(Tags.SB_DELETE_VIDEO_API)
    @FormUrlEncoded
    Call<CommonBean> deleteVideos(@Field("user_id") String user_id,
                                  @Field("game_id") String game_id,
                                  @Field("type") String type,
                                  @Field("video_id") String video_id,
                                  @Field("login_id") String login_id,
                                  @Field("device_type") String device_type,
                                  @Field("device_model") String device_model);
    @POST(Tags.SB_UPDATE_MATCH_COUNT_API)
    @FormUrlEncoded
    Call<CommonBean> updateMatchCount(@Field("user_id") String user_id, @Field("match_id") String game_id,   @Field("actions") String actions, @Field("login_id") String login_id, @Field("device_type") String device_type, @Field("device_model") String device_model);
    //user_id, game_id, type(1,2 for video/interview), video_id

    @POST(Tags.SB_COUNTY_API)
    @FormUrlEncoded
    Call<CountyBean> getCounty(@Field("search") String search,
                               @Field("user_id") String user_id,
                               @Field("game_category") String game_category,
                               @Field("login_id") String login_id,
                               @Field("device_type") String device_type,
                               @Field("device_model") String device_model);

    @POST(Tags.SB_SUPPORT_SUBJECTS_API)
    @FormUrlEncoded
    Call<SubjectsBean> getSubjects(@Field("user_id") String user_id,
                                   @Field("login_id") String login_id,
                                   @Field("device_type") String device_type,
                                   @Field("device_model") String device_model);

    @POST(Tags.SB_DELETE_ACCOUNT_API)
    @FormUrlEncoded
    Call<CommonBean> deleteAccount(@Field("user_id") String user_id,
                                   @Field("login_id") String login_id,
                                   @Field("device_type") String device_type,
                                   @Field("device_model") String device_model);
    @POST(Tags.SB_CHECK_SIGNUP_API)
    @FormUrlEncoded
    Call<CommonBean> checkSignup(  @Field("login_id") String login_id, @Field("device_type") String device_type, @Field("device_model") String device_model);

    @POST(Tags.SB_CREATE_MATCH_API)
    @FormUrlEncoded
    Call<MatchesBean> createMatch(@Field("user_id") String user_id,
                                  @Field("game_category") String game_category,
                                  @Field("county_id") String county_id,
                                  @Field("team_id") String team_id,
                                  @Field("league_id") String league_id,
                                  @Field("opponent_team_id") String opponent_team_id,
                                  @Field("location_type") String location_type,
                                  @Field("sort") String sort,
                                  @Field("order") String order,
                                  @Field("search") String search,
                                  @Field("login_id") String login_id,
                                  @Field("device_type") String device_type,
                                  @Field("device_model") String device_model);

    @Streaming // Use this annotation to stream the request body
    @Multipart
    @POST(Tags.SB_CREATE_MATCH_ACTION_API)
    Call<CommonBean> makeActions(@Part("user_id") RequestBody user_id,
                                 @Part("local_id") RequestBody local_id,
                                 @Part("match_id") RequestBody match_id,
                                 @Part("team_id") RequestBody team_id,
                                 @Part("time") RequestBody time,
                                 @Part("reaction") RequestBody reaction,
                                 @Part("half") RequestBody half,
                                 @Part MultipartBody.Part image,
                                 @Tag ProgressCallback progressCallback);

    @Multipart
    @POST(Tags.SB_UPLOAD_INTERVIEW_API)
    Call<CommonBean> uploadInterview(@Part("user_id") RequestBody user_id,
                                     @Part("match_id") RequestBody match_id,
                                     @Part MultipartBody.Part image,
                                     @Tag ProgressCallback progressCallback);

    @POST(Tags.SB_CREATE_MATCH_ACTION_API)
    @FormUrlEncoded
    Call<CommonBean> makeActions1(@Field("user_id") String user_id,
                                  @Field("match_id") String match_id,
                                  @Field("team_id") String team_id,
                                  @Field("time") String time,
                                  @Field("reaction") String reaction,
                                  @Field("half") String half,
                                  @Field("login_id") String login_id,
                                  @Field("device_type") String device_type,
                                  @Field("device_model") String device_model);

}
