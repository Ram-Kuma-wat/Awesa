package com.codersworld.awesalibs.rest;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.codersworld.awesalibs.R;
import com.codersworld.awesalibs.beans.CommonBean;
import com.codersworld.awesalibs.beans.county.CountyBean;
import com.codersworld.awesalibs.beans.game.GameBean;
import com.codersworld.awesalibs.beans.leagues.LeagueBean;
import com.codersworld.awesalibs.beans.matches.MatchesBean;
import com.codersworld.awesalibs.beans.support.SubjectsBean;
import com.codersworld.awesalibs.beans.support.TicketsBean;
import com.codersworld.awesalibs.beans.teams.TeamsBean;
import com.codersworld.awesalibs.listeners.OnResponse;
import com.codersworld.awesalibs.storage.UserSessions;
import com.codersworld.awesalibs.utils.CommonMethods;
import com.codersworld.awesalibs.utils.SFProgress;
import com.codersworld.awesalibs.utils.Tags;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApiCall {
    public Activity mContext;

    public ApiCall(Activity ctx) {
        this.mContext = ctx;
    }

    public void userLogin(OnResponse<UniversalObject> onResponse, Boolean isTrue, String... params) {
        if (isTrue) {
            SFProgress.showProgressDialog(mContext, true);
        }

        ApiRequest mRequest = RetrofitRequest.getRetrofitInstance(1, UserSessions.getAccessToken(mContext)).create(ApiRequest.class);
        mRequest.userLogin(params[0],params[1],params[2],params[3],CommonMethods.getIMEI(mContext),"android",CommonMethods.getDeviceModel(mContext)).enqueue(new Callback<CommonBean>() {
            @Override
            public void onResponse(@NonNull Call<CommonBean> call, @NonNull Response<CommonBean> response) {
                if (isTrue) {
                    SFProgress.hideProgressDialog(mContext);
                }
                try {
                    onResponse.onSuccess(new UniversalObject(response.body(), Tags.SB_LOGIN_API, true, ""));
                } catch (Exception e) {
                    onResponse.onError(Tags.SB_LOGIN_API, mContext.getResources().getString(R.string.something_wrong));
                }
            }
            @Override
            public void onFailure(@NonNull Call<CommonBean> call, @NonNull Throwable t) {
                if (isTrue) {
                    SFProgress.hideProgressDialog(mContext);
                }

                onResponse.onError(Tags.SB_LOGIN_API, mContext.getResources().getString(R.string.something_wrong));
            }
        });
    }

     public void userSignUp(OnResponse<UniversalObject> onResponse, Boolean isTrue, String... params) {
        if (isTrue) {
            SFProgress.showProgressDialog(mContext, true);
        }
        ApiRequest mRequest = RetrofitRequest.getRetrofitInstance(1, UserSessions.getAccessToken(mContext)).create(ApiRequest.class);
        mRequest.userSignUp(params[0],params[1],params[2],params[3],params[4],params[5],params[6],  params[7],params[8],CommonMethods.getIMEI(mContext),"android",CommonMethods.getDeviceModel(mContext) ).enqueue(new Callback<CommonBean>() {
            @Override
            public void onResponse(@NonNull Call<CommonBean> call, @NonNull Response<CommonBean> response) {
                if (isTrue) {
                    SFProgress.hideProgressDialog(mContext);
                }
                try {
                    onResponse.onSuccess(new UniversalObject(response.body(), Tags.SB_SIGNUP_API, true, ""));
                } catch (Exception e) {
                    onResponse.onError(Tags.SB_SIGNUP_API, mContext.getResources().getString(R.string.something_wrong));
                }
            }
            @Override
            public void onFailure(@NonNull Call<CommonBean> call, @NonNull Throwable t) {
                if (isTrue) {
                    SFProgress.hideProgressDialog(mContext);
                }
                onResponse.onError(Tags.SB_SIGNUP_API, mContext.getResources().getString(R.string.something_wrong));
            }
        });
    }

    public void resendOTP(OnResponse<UniversalObject> onResponse, Boolean isTrue, String... params) {
        if (isTrue) {
            SFProgress.showProgressDialog(mContext, true);
        }
        ApiRequest mRequest = RetrofitRequest.getRetrofitInstance(1, UserSessions.getAccessToken(mContext)).create(ApiRequest.class);
        mRequest.resendOTP(params[0],params[1],params[2],params[3] ,CommonMethods.getIMEI(mContext),"android",CommonMethods.getDeviceModel(mContext)).enqueue(new Callback<CommonBean>() {
            @Override
            public void onResponse(@NonNull Call<CommonBean> call, @NonNull Response<CommonBean> response) {
                if (isTrue) {
                    SFProgress.hideProgressDialog(mContext);
                }
                try {
                    onResponse.onSuccess(new UniversalObject(response.body(), Tags.SB_RESEND_OTP_API, true, ""));
                } catch (Exception e) {
                    onResponse.onError(Tags.SB_RESEND_OTP_API, mContext.getResources().getString(R.string.something_wrong));

                }
            }
            @Override
            public void onFailure(@NonNull Call<CommonBean> call, @NonNull Throwable t) {
                if (isTrue) {
                    SFProgress.hideProgressDialog(mContext);
                }

                onResponse.onError(Tags.SB_RESEND_OTP_API, mContext.getResources().getString(R.string.something_wrong));
            }
        });
    }

    public void verifySignUpOTP(OnResponse<UniversalObject> onResponse, Boolean isTrue, String... params) {
        if (isTrue) {
            SFProgress.showProgressDialog(mContext, true);
        }
        ApiRequest mRequest = RetrofitRequest.getRetrofitInstance(1, UserSessions.getAccessToken(mContext)).create(ApiRequest.class);
        mRequest.verifySignUpOTP(params[0],params[1],params[2],params[3],params[4] ,CommonMethods.getIMEI(mContext),"android",CommonMethods.getDeviceModel(mContext)).enqueue(new Callback<CommonBean>() {
            @Override
            public void onResponse(@NonNull Call<CommonBean> call, @NonNull Response<CommonBean> response) {
                if (isTrue) {
                    SFProgress.hideProgressDialog(mContext);
                }
                try {
                    onResponse.onSuccess(new UniversalObject(response.body(), Tags.SB_SIGNUP_VERIFY_OTP_API, true, ""));
                } catch (Exception e) {
                    onResponse.onError(Tags.SB_SIGNUP_VERIFY_OTP_API, mContext.getResources().getString(R.string.something_wrong));
                }
            }
            @Override
            public void onFailure(@NonNull Call<CommonBean> call, @NonNull Throwable t) {
                if (isTrue) {
                    SFProgress.hideProgressDialog(mContext);
                }
                onResponse.onError(Tags.SB_SIGNUP_VERIFY_OTP_API, mContext.getResources().getString(R.string.something_wrong));
            }
        });
    }

    public void forgotPassword(OnResponse<UniversalObject> onResponse, String... strParams) {
        SFProgress.showProgressDialog(mContext, true);
        ApiRequest mRequest = RetrofitRequest.getRetrofitInstance(1, UserSessions.getAccessToken(mContext)).create(ApiRequest.class);
        mRequest.forgotPassword(strParams[0],strParams[1],CommonMethods.getIMEI(mContext),"android",CommonMethods.getDeviceModel(mContext)).enqueue(new Callback<CommonBean>() {
            @Override
            public void onResponse(@NonNull Call<CommonBean> call, @NonNull Response<CommonBean> response) {
            SFProgress.hideProgressDialog(mContext);
                try {
                    onResponse.onSuccess(new UniversalObject(response.body(), Tags.SB_FORGOT_PASSWORD_API, true, ""));
                } catch (Exception e) {
                    onResponse.onError(Tags.SB_FORGOT_PASSWORD_API, mContext.getResources().getString(R.string.something_wrong));
                }
            }

            @Override
            public void onFailure(@NonNull Call<CommonBean> call, @NonNull Throwable t) {
                SFProgress.hideProgressDialog(mContext);
                onResponse.onError(Tags.SB_FORGOT_PASSWORD_API, mContext.getResources().getString(R.string.something_wrong));
            }
        });
    }

    public void resetPassword(OnResponse<UniversalObject> onResponse, String... strParams) {
        SFProgress.showProgressDialog(mContext, true);
        ApiRequest mRequest = RetrofitRequest.getRetrofitInstance(1, UserSessions.getAccessToken(mContext)).create(ApiRequest.class);
        mRequest.resetPassword(strParams[0],strParams[1],strParams[2] ,CommonMethods.getIMEI(mContext),"android",CommonMethods.getDeviceModel(mContext)).enqueue(new Callback<CommonBean>() {
            @Override
            public void onResponse(@NonNull Call<CommonBean> call, @NonNull Response<CommonBean> response) {
SFProgress.hideProgressDialog(mContext);
                try {
                    onResponse.onSuccess(new UniversalObject(response.body(), Tags.SB_FORGOT_PASSWORD_RESET_API, true, ""));
                } catch (Exception e) {
                    onResponse.onError(Tags.SB_FORGOT_PASSWORD_RESET_API, mContext.getResources().getString(R.string.something_wrong));
                }
            }

            @Override
            public void onFailure(@NonNull Call<CommonBean> call, @NonNull Throwable t) {
SFProgress.hideProgressDialog(mContext);
                onResponse.onError(Tags.SB_FORGOT_PASSWORD_RESET_API, mContext.getResources().getString(R.string.something_wrong));
            }
        });
    }

    public void supportTicket(OnResponse<UniversalObject> onResponse, Boolean isTrue, String... params) {
        if (isTrue) {
            SFProgress.showProgressDialog(mContext, true);
        }
        ApiRequest mRequest = RetrofitRequest.getRetrofitInstance(1, UserSessions.getAccessToken(mContext)).create(ApiRequest.class);
        mRequest.supportTicket(params[0],params[1],params[2],params[3],params[4],params[5],params[6],CommonMethods.getIMEI(mContext),"android",CommonMethods.getDeviceModel(mContext)).enqueue(new Callback<TicketsBean>() {
            @Override
            public void onResponse(@NonNull Call<TicketsBean> call, @NonNull Response<TicketsBean> response) {
                if (isTrue) {
                    SFProgress.hideProgressDialog(mContext);
                }
                try {
                    onResponse.onSuccess(new UniversalObject(response.body(), Tags.SB_API_SUPPORT, true, ""));
                } catch (Exception e) {
                    onResponse.onError(Tags.SB_API_SUPPORT, mContext.getResources().getString(R.string.something_wrong));
                }
            }
            @Override
            public void onFailure(@NonNull Call<TicketsBean> call, @NonNull Throwable t) {
                if (isTrue) {
                    SFProgress.hideProgressDialog(mContext);
                }
                onResponse.onError(Tags.SB_API_SUPPORT, mContext.getResources().getString(R.string.something_wrong));
            }
        });
    }

    public void getGames(OnResponse<UniversalObject> onResponse, Boolean isTrue, String... params) {
        if (isTrue) {
            SFProgress.showProgressDialog(mContext, true);
        }
        ApiRequest mRequest = RetrofitRequest.getRetrofitInstance(1, UserSessions.getAccessToken(mContext)).create(ApiRequest.class);
        mRequest.getGames(params[0],params[1],CommonMethods.getIMEI(mContext),"android",CommonMethods.getDeviceModel(mContext)).enqueue(new Callback<GameBean>() {
            @Override
            public void onResponse(@NonNull Call<GameBean> call, @NonNull Response<GameBean> response) {
                if (isTrue) {
                    SFProgress.hideProgressDialog(mContext);
                }
                try {

                    onResponse.onSuccess(new UniversalObject(response.body(), Tags.SB_GAME_CATEGORY_API, true, ""));
                } catch (Exception e) {
                    onResponse.onError(Tags.SB_GAME_CATEGORY_API, mContext.getResources().getString(R.string.something_wrong));
                }
            }
            @Override
            public void onFailure(@NonNull Call<GameBean> call, @NonNull Throwable t) {
                if (isTrue) {
                    SFProgress.hideProgressDialog(mContext);
                }
                onResponse.onError(Tags.SB_GAME_CATEGORY_API, mContext.getResources().getString(R.string.something_wrong));
            }
        });
    }

    public void getTeams(OnResponse<UniversalObject> onResponse, Boolean isTrue, String... params) {
        if (isTrue) {
            SFProgress.showProgressDialog(mContext, true);
        }
        ApiRequest mRequest = RetrofitRequest.getRetrofitInstance(1, UserSessions.getAccessToken(mContext)).create(ApiRequest.class);
        mRequest.getTeams(params[0],params[1],params[2],params[3],CommonMethods.getIMEI(mContext),"android",CommonMethods.getDeviceModel(mContext)).enqueue(new Callback<TeamsBean>() {
            @Override
            public void onResponse(@NonNull Call<TeamsBean> call, @NonNull Response<TeamsBean> response) {
                if (isTrue) {
                    SFProgress.hideProgressDialog(mContext);
                }
                try {
                    onResponse.onSuccess(new UniversalObject(response.body(), Tags.SB_TEAMS_API, true, ""));
                } catch (Exception e) {
                    onResponse.onError(Tags.SB_TEAMS_API, mContext.getResources().getString(R.string.something_wrong));
                }
            }
            @Override
            public void onFailure(@NonNull Call<TeamsBean> call, @NonNull Throwable t) {
                if (isTrue) {
                    SFProgress.hideProgressDialog(mContext);
                }
                onResponse.onError(Tags.SB_TEAMS_API, mContext.getResources().getString(R.string.something_wrong));
            }
        });
    }

    public void getOpponentTeams(OnResponse<UniversalObject> onResponse, Boolean isTrue, String... params) {
        if (isTrue) {
            SFProgress.showProgressDialog(mContext, true);
        }
        ApiRequest mRequest = RetrofitRequest.getRetrofitInstance(1, UserSessions.getAccessToken(mContext)).create(ApiRequest.class);
        mRequest.getOpponentTeams(params[0],params[1],params[2],params[3],params[4],CommonMethods.getIMEI(mContext),"android",CommonMethods.getDeviceModel(mContext)).enqueue(new Callback<TeamsBean>() {
            @Override
            public void onResponse(@NonNull Call<TeamsBean> call, @NonNull Response<TeamsBean> response) {
                if (isTrue) {
                    SFProgress.hideProgressDialog(mContext);
                }
                try {
                    onResponse.onSuccess(new UniversalObject(response.body(), Tags.SB_OPPONENT_TEAMS_API, true, ""));
                } catch (Exception e) {
                    onResponse.onError(Tags.SB_OPPONENT_TEAMS_API, mContext.getResources().getString(R.string.something_wrong));
                }
            }
            @Override
            public void onFailure(@NonNull Call<TeamsBean> call, @NonNull Throwable t) {
                if (isTrue) {
                    SFProgress.hideProgressDialog(mContext);
                }
                onResponse.onError(Tags.SB_OPPONENT_TEAMS_API, mContext.getResources().getString(R.string.something_wrong));
            }
        });
    }

    public void getLeagues(OnResponse<UniversalObject> onResponse, Boolean isTrue, String... params) {
        if (isTrue) {
            SFProgress.showProgressDialog(mContext, true);
        }
        ApiRequest mRequest = RetrofitRequest.getRetrofitInstance(1, UserSessions.getAccessToken(mContext)).create(ApiRequest.class);
        mRequest.getLeagues(params[0],params[1],params[2],params[3],params[4],CommonMethods.getIMEI(mContext),"android",CommonMethods.getDeviceModel(mContext)).enqueue(new Callback<LeagueBean>() {
            @Override
            public void onResponse(@NonNull Call<LeagueBean> call, @NonNull Response<LeagueBean> response) {
                if (isTrue) {
                    SFProgress.hideProgressDialog(mContext);
                }
                try {
                    onResponse.onSuccess(new UniversalObject(response.body(), Tags.SB_LEAGUE_API, true, ""));
                } catch (Exception e) {
                    onResponse.onError(Tags.SB_LEAGUE_API, mContext.getResources().getString(R.string.something_wrong));
                }
            }
            @Override
            public void onFailure(@NonNull Call<LeagueBean> call, @NonNull Throwable t) {
                if (isTrue) {
                    SFProgress.hideProgressDialog(mContext);
                }
                onResponse.onError(Tags.SB_LEAGUE_API, mContext.getResources().getString(R.string.something_wrong));
            }
        });
    }

    public void getMatches(OnResponse<UniversalObject> onResponse, Boolean isTrue, String... params) {
        if (isTrue) {
            SFProgress.showProgressDialog(mContext, true);
        }

        ApiRequest mRequest = RetrofitRequest.getRetrofitInstance(1, UserSessions.getAccessToken(mContext)).create(ApiRequest.class);
        mRequest.getMatches(params[0], params[1], params[2], params[3], params[4], CommonMethods.getIMEI(mContext), "android", CommonMethods.getDeviceModel(mContext)).enqueue(new Callback<MatchesBean>() {
            @Override
            public void onResponse(@NonNull Call<MatchesBean> call, @NonNull Response<MatchesBean> response) {
                if (isTrue) {
                    SFProgress.hideProgressDialog(mContext);
                }

                try {
                    onResponse.onSuccess(new UniversalObject(response.body(), Tags.SB_USER_MATCHES_API, true, ""));
                } catch (Exception e) {
                    onResponse.onError(Tags.SB_USER_MATCHES_API, mContext.getResources().getString(R.string.something_wrong));
                }
            }
            @Override
            public void onFailure(@NonNull Call<MatchesBean> call, @NonNull Throwable error) {
                if (isTrue) {
                    SFProgress.hideProgressDialog(mContext);
                }

                onResponse.onError(Tags.SB_USER_MATCHES_API, mContext.getResources().getString(R.string.something_wrong));
            }
        });
    }

    public void getMatchDetail(OnResponse<UniversalObject> onResponse, List<MatchesBean.VideosBean> currentList, Boolean isTrue, String... params) {
        if (isTrue) {
            SFProgress.showProgressDialog(mContext, true);
        }

        ApiRequest mRequest = RetrofitRequest.getRetrofitInstance(1, UserSessions.getAccessToken(mContext)).create(ApiRequest.class);
        mRequest.getMatchDetail(params[0], params[1], CommonMethods.getIMEI(mContext), "android", CommonMethods.getDeviceModel(mContext)).enqueue(new Callback<MatchesBean>() {
            @Override
            public void onResponse(@NonNull Call<MatchesBean> call, @NonNull Response<MatchesBean> response) {
                if (isTrue) {
                    SFProgress.hideProgressDialog(mContext);
                }
                try {
                    onResponse.onSuccess(new UniversalObject(response.body(), Tags.SB_MATCH_DETAIL_API, true, "", currentList));
                } catch (Exception e) {
                    onResponse.onError(Tags.SB_MATCH_DETAIL_API, mContext.getResources().getString(R.string.something_wrong));
                }
            }
            @Override
            public void onFailure(@NonNull Call<MatchesBean> call, @NonNull Throwable t) {
                if (isTrue) {
                    SFProgress.hideProgressDialog(mContext);
                }
                onResponse.onError(Tags.SB_MATCH_DETAIL_API, mContext.getResources().getString(R.string.something_wrong));
            }
        });
    }
    public void deleteVideos(OnResponse<UniversalObject> onResponse, Boolean isTrue, String... params) {
        if (isTrue) {
            SFProgress.showProgressDialog(mContext, true);
        }
        ApiRequest mRequest = RetrofitRequest.getRetrofitInstance(1, UserSessions.getAccessToken(mContext)).create(ApiRequest.class);
        mRequest.deleteVideos(params[0], params[1], params[2], params[3], CommonMethods.getIMEI(mContext), "android", CommonMethods.getDeviceModel(mContext)).enqueue(new Callback<CommonBean>() {
            @Override
            public void onResponse(@NonNull Call<CommonBean> call, @NonNull Response<CommonBean> response) {
                if (isTrue) {
                    SFProgress.hideProgressDialog(mContext);
                }
                try {
                    onResponse.onSuccess(new UniversalObject(response.body(), Tags.SB_DELETE_VIDEO_API, true, ""));
                } catch (Exception e) {
                    onResponse.onError(Tags.SB_DELETE_VIDEO_API, mContext.getResources().getString(R.string.something_wrong));
                }
            }
            @Override
            public void onFailure(@NonNull Call<CommonBean> call, @NonNull Throwable error) {
                if (isTrue) {
                    SFProgress.hideProgressDialog(mContext);
                }

                onResponse.onError(Tags.SB_DELETE_VIDEO_API, mContext.getResources().getString(R.string.something_wrong));
            }
        });
    }

    public void getCounty(OnResponse<UniversalObject> onResponse, Boolean isTrue, String... params) {
        if (isTrue) {
            SFProgress.showProgressDialog(mContext, true);
        }
        ApiRequest mRequest = RetrofitRequest.getRetrofitInstance(1, UserSessions.getAccessToken(mContext)).create(ApiRequest.class);
        mRequest.getCounty(params[0],params[1],params[2],CommonMethods.getIMEI(mContext),"android",CommonMethods.getDeviceModel(mContext)).enqueue(new Callback<CountyBean>() {
            @Override
            public void onResponse(@NonNull Call<CountyBean> call, @NonNull Response<CountyBean> response) {
                if (isTrue) {
                    SFProgress.hideProgressDialog(mContext);
                }
                try {
                    onResponse.onSuccess(new UniversalObject(response.body(), Tags.SB_COUNTY_API, true, ""));
                } catch (Exception e) {
                    onResponse.onError(Tags.SB_COUNTY_API, mContext.getResources().getString(R.string.something_wrong));
                }
            }
            @Override
            public void onFailure(@NonNull Call<CountyBean> call, @NonNull Throwable t) {
                if (isTrue) {
                    SFProgress.hideProgressDialog(mContext);
                }
                onResponse.onError(Tags.SB_COUNTY_API, mContext.getResources().getString(R.string.something_wrong));
            }
        });
    }

    public void getSubjects(OnResponse<UniversalObject> onResponse, Boolean isTrue, String... params) {
        if (isTrue) {
            SFProgress.showProgressDialog(mContext, true);
        }
        ApiRequest mRequest = RetrofitRequest.getRetrofitInstance(1, UserSessions.getAccessToken(mContext)).create(ApiRequest.class);
        mRequest.getSubjects("1",CommonMethods.getIMEI(mContext),"android",CommonMethods.getDeviceModel(mContext)).enqueue(new Callback<SubjectsBean>() {
            @Override
            public void onResponse(@NonNull Call<SubjectsBean> call, @NonNull Response<SubjectsBean> response) {
                if (isTrue) {
                    SFProgress.hideProgressDialog(mContext);
                }

                try {
                    onResponse.onSuccess(new UniversalObject(response.body(), Tags.SB_SUPPORT_SUBJECTS_API, true, ""));
                } catch (Exception e) {
                    onResponse.onError(Tags.SB_SUPPORT_SUBJECTS_API, mContext.getResources().getString(R.string.something_wrong));
                }
            }
            @Override
            public void onFailure(@NonNull Call<SubjectsBean> call, @NonNull Throwable t) {
                if (isTrue) {
                    SFProgress.hideProgressDialog(mContext);
                }

                onResponse.onError(Tags.SB_SUPPORT_SUBJECTS_API, mContext.getResources().getString(R.string.something_wrong));
            }
        });
    }

    public void createMatch(OnResponse<UniversalObject> onResponse, Boolean isTrue, String... params) {
        if (isTrue) {
            SFProgress.showProgressDialog(mContext, true);
        }
        ApiRequest mRequest = RetrofitRequest.getRetrofitInstance(1, UserSessions.getAccessToken(mContext)).create(ApiRequest.class);
        mRequest.createMatch(params[0],params[1],params[2],params[3],params[4],params[5],params[6],params[7],params[8],params[9],CommonMethods.getIMEI(mContext),"android",CommonMethods.getDeviceModel(mContext)).enqueue(new Callback<MatchesBean>() {
            @Override
            public void onResponse(@NonNull Call<MatchesBean> call, @NonNull Response<MatchesBean> response) {
                if (isTrue) {
                    SFProgress.hideProgressDialog(mContext);
                }

                try {
                    onResponse.onSuccess(new UniversalObject(response.body(), Tags.SB_CREATE_MATCH_API, true, ""));
                } catch (Exception e) {
                    onResponse.onError(Tags.SB_CREATE_MATCH_API, mContext.getResources().getString(R.string.something_wrong));
                }
            }
            @Override
            public void onFailure(@NonNull Call<MatchesBean> call, @NonNull Throwable t) {
                if (isTrue) {
                    SFProgress.hideProgressDialog(mContext);
                }
                onResponse.onError(Tags.SB_CREATE_MATCH_API, mContext.getResources().getString(R.string.something_wrong));
            }
        });
    }

    public void updateProfile(OnResponse<UniversalObject> onResponse, RequestBody firstname, RequestBody lastname,
                              RequestBody user_id, RequestBody phone, RequestBody username, RequestBody email, MultipartBody.Part image) {
             SFProgress.showProgressDialog(mContext, true);
        ApiRequest mRequest = RetrofitRequest.getRetrofitInstance(1, UserSessions.getAccessToken(mContext)).create(ApiRequest.class);
        Call<CommonBean> mCall =  mRequest.updateProfile(firstname,lastname, user_id,phone,username,email,CommonMethods.getIMEI(mContext),"android",CommonMethods.getDeviceModel(mContext));
        if (image !=null){
            mCall =  mRequest.updateProfileWithImage(firstname,lastname, user_id,phone,username,email,image,CommonMethods.getIMEI(mContext),"android",CommonMethods.getDeviceModel(mContext));
        }
        mCall.enqueue(new Callback<CommonBean>() {
            @Override
            public void onResponse(@NonNull Call<CommonBean> call, @NonNull Response<CommonBean> response) {
                     SFProgress.hideProgressDialog(mContext);

                try {
                    onResponse.onSuccess(new UniversalObject(response.body(), Tags.SB_UPDATE_PROFILE_API, true, ""));
                } catch (Exception e) {
                    onResponse.onError(Tags.SB_UPDATE_PROFILE_API, mContext.getResources().getString(R.string.something_wrong));
                }
            }
            @Override
            public void onFailure(@NonNull Call<CommonBean> call, @NonNull Throwable t) {
                     SFProgress.hideProgressDialog(mContext);
                onResponse.onError(Tags.SB_UPDATE_PROFILE_API, mContext.getResources().getString(R.string.something_wrong));
            }
        });
    }

    public void deleteAccount(OnResponse<UniversalObject> onResponse, Boolean isTrue, String... params) {
        if (isTrue) {
            SFProgress.showProgressDialog(mContext, true);
        }
        ApiRequest mRequest = RetrofitRequest.getRetrofitInstance(1, UserSessions.getAccessToken(mContext)).create(ApiRequest.class);
        mRequest.deleteAccount(params[0],CommonMethods.getIMEI(mContext),"android",CommonMethods.getDeviceModel(mContext)).enqueue(new Callback<CommonBean>() {
            @Override
            public void onResponse(@NonNull Call<CommonBean> call, @NonNull Response<CommonBean> response) {
                if (isTrue) {
                    SFProgress.hideProgressDialog(mContext);
                }
                try {
                    onResponse.onSuccess(new UniversalObject(response.body(), Tags.SB_DELETE_ACCOUNT_API, true, ""));
                } catch (Exception e) {
                    onResponse.onError(Tags.SB_DELETE_ACCOUNT_API, mContext.getResources().getString(R.string.something_wrong));
                }
            }
            @Override
            public void onFailure(@NonNull Call<CommonBean> call, @NonNull Throwable t) {
                if (isTrue) {
                    SFProgress.hideProgressDialog(mContext);
                }
                onResponse.onError(Tags.SB_DELETE_ACCOUNT_API, mContext.getResources().getString(R.string.something_wrong));
            }
        });
    }
    public void updateMatchCount(OnResponse<UniversalObject> onResponse, Boolean isTrue, String... params) {
        if (isTrue) {
            SFProgress.showProgressDialog(mContext, true);
        }
        ApiRequest mRequest = RetrofitRequest.getRetrofitInstance(1, UserSessions.getAccessToken(mContext)).create(ApiRequest.class);
        mRequest.updateMatchCount(params[0],params[1] ,params[2]  ,CommonMethods.getIMEI(mContext),"android",CommonMethods.getDeviceModel(mContext)).enqueue(new Callback<CommonBean>() {
            @Override
            public void onResponse(@NonNull Call<CommonBean> call, @NonNull Response<CommonBean> response) {
                if (isTrue) {
                    SFProgress.hideProgressDialog(mContext);
                }
                try {
                    onResponse.onSuccess(new UniversalObject(response.body(), Tags.SB_UPDATE_MATCH_COUNT_API, true, ""));
                } catch (Exception e) {
                onResponse.onError(Tags.SB_UPDATE_MATCH_COUNT_API, mContext.getResources().getString(R.string.something_wrong));
                }
            }
            @Override
            public void onFailure(@NonNull Call<CommonBean> call, @NonNull Throwable t) {
                if (isTrue) {
                    SFProgress.hideProgressDialog(mContext);
                }
                onResponse.onError(Tags.SB_UPDATE_MATCH_COUNT_API, mContext.getResources().getString(R.string.something_wrong));
            }
        });
    }
     public void checkSignup(OnResponse<UniversalObject> onResponse, Boolean isTrue) {
        if (isTrue) {
            SFProgress.showProgressDialog(mContext, true);
        }
        ApiRequest mRequest = RetrofitRequest.getRetrofitInstance(1, UserSessions.getAccessToken(mContext)).create(ApiRequest.class);
        mRequest.checkSignup( CommonMethods.getIMEI(mContext),"android",CommonMethods.getDeviceModel(mContext)).enqueue(new Callback<CommonBean>() {
            @Override
            public void onResponse(@NonNull Call<CommonBean> call, @NonNull Response<CommonBean> response) {
                if (isTrue) {
                    SFProgress.hideProgressDialog(mContext);
                }
                try {
                    onResponse.onSuccess(new UniversalObject(response.body(), Tags.SB_CHECK_SIGNUP_API, true, ""));
                } catch (Exception e) {
                    onResponse.onError(Tags.SB_CHECK_SIGNUP_API, mContext.getResources().getString(R.string.something_wrong));
                }
            }
            @Override
            public void onFailure(@NonNull Call<CommonBean> call, @NonNull Throwable t) {
                if (isTrue) {
                    SFProgress.hideProgressDialog(mContext);
                }
                onResponse.onError(Tags.SB_CHECK_SIGNUP_API, mContext.getResources().getString(R.string.something_wrong));
            }
        });
    }


}
