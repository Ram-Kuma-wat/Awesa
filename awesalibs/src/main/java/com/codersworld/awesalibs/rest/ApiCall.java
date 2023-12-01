package com.codersworld.awesalibs.rest;

import android.app.Activity;
import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import com.codersworld.awesalibs.R;
import com.codersworld.awesalibs.beans.CommonBean;
import com.codersworld.awesalibs.beans.county.CountyBean;
import com.codersworld.awesalibs.beans.game.GameBean;
import com.codersworld.awesalibs.beans.leagues.LeagueBean;
import com.codersworld.awesalibs.beans.login.LoginBean;
import com.codersworld.awesalibs.beans.matches.InterviewBean;
import com.codersworld.awesalibs.beans.matches.MatchesBean;
import com.codersworld.awesalibs.beans.matches.ReactionsBean;
import com.codersworld.awesalibs.beans.support.SubjectsBean;
import com.codersworld.awesalibs.beans.support.TicketsBean;
import com.codersworld.awesalibs.beans.teams.TeamsBean;
import com.codersworld.awesalibs.listeners.OnResponse;
import com.codersworld.awesalibs.storage.UserSessions;
import com.codersworld.awesalibs.utils.CommonMethods;
import com.codersworld.awesalibs.utils.SFProgress;
import com.codersworld.awesalibs.utils.Tags;
import com.google.gson.Gson;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApiCall {
    public Activity mContext = null;

    public ApiCall(Context applicationContext) {
    }

    public ApiCall(Activity ctx) {
        this.mContext = ctx;
    }

    public void userLogin(OnResponse<UniverSelObjct> onResponse,Boolean isTrue,String... params) {
        if (isTrue) {
            try {
                SFProgress.showProgressDialog(mContext, true);
            } catch (Exception e) {
            }
        }
        ApiRequest mRequest = RetrofitRequest.getRetrofitInstance(1, 2).create(ApiRequest.class);
        mRequest.userLogin(params[0],params[1],params[2],params[3]).enqueue(new Callback<CommonBean>() {
            @Override
            public void onResponse(Call<CommonBean> call, Response<CommonBean> response) {
                if (isTrue) {
                    try {
                        SFProgress.hideProgressDialog(mContext);
                    } catch (Exception e) {
                    }
                }
                try {
                    if (response != null) {
                        try {
                            onResponse.onSuccess(new UniverSelObjct(response.body(), Tags.SB_LOGIN_API, "true", ""));
                        } catch (Exception e) {
                            e.printStackTrace();
                            onResponse.onError(Tags.SB_LOGIN_API, mContext.getResources().getString(R.string.something_wrong));
                        }
                    } else {
                        onResponse.onError(Tags.SB_LOGIN_API, mContext.getResources().getString(R.string.something_wrong));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(Call<CommonBean> call, Throwable t) {
                if (isTrue) {
                    try {
                        SFProgress.hideProgressDialog(mContext);
                    } catch (Exception e) {
                    }
                }
                t.printStackTrace();
                onResponse.onError(Tags.SB_LOGIN_API, mContext.getResources().getString(R.string.something_wrong));
            }
        });
    }
     public void userSignUp(OnResponse<UniverSelObjct> onResponse,Boolean isTrue,String... params) {
        if (isTrue) {
            try {
                SFProgress.showProgressDialog(mContext, true);
            } catch (Exception e) {
            }
        }
        ApiRequest mRequest = RetrofitRequest.getRetrofitInstance(1, 2).create(ApiRequest.class);
        mRequest.userSignUp(params[0],params[1],params[2],params[3],params[4],params[5],params[6],  params[7],params[8] ).enqueue(new Callback<CommonBean>() {
            @Override
            public void onResponse(Call<CommonBean> call, Response<CommonBean> response) {
                if (isTrue) {
                    try {
                        SFProgress.hideProgressDialog(mContext);
                    } catch (Exception e) {
                    }
                }
                try {
                    if (response != null) {
                        try {
                            onResponse.onSuccess(new UniverSelObjct(response.body(), Tags.SB_SIGNUP_API, "true", ""));
                        } catch (Exception e) {
                            e.printStackTrace();
                            onResponse.onError(Tags.SB_SIGNUP_API, mContext.getResources().getString(R.string.something_wrong));
                        }
                    } else {
                        onResponse.onError(Tags.SB_SIGNUP_API, mContext.getResources().getString(R.string.something_wrong));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(Call<CommonBean> call, Throwable t) {
                if (isTrue) {
                    try {
                        SFProgress.hideProgressDialog(mContext);
                    } catch (Exception e) {
                    }
                }
                t.printStackTrace();
                onResponse.onError(Tags.SB_SIGNUP_API, mContext.getResources().getString(R.string.something_wrong));
            }
        });
    }

    public void resendOTP(OnResponse<UniverSelObjct> onResponse,Boolean isTrue,String... params) {
        if (isTrue) {
            try {
                SFProgress.showProgressDialog(mContext, true);
            } catch (Exception e) {
            }
        }
        ApiRequest mRequest = RetrofitRequest.getRetrofitInstance(1, 2).create(ApiRequest.class);
        mRequest.resendOTP(params[0],params[1],params[2],params[3] ).enqueue(new Callback<CommonBean>() {
            @Override
            public void onResponse(Call<CommonBean> call, Response<CommonBean> response) {
                if (isTrue) {
                    try {
                        SFProgress.hideProgressDialog(mContext);
                    } catch (Exception e) {
                    }
                }
                try {
                    if (response != null) {
                        try {
                            onResponse.onSuccess(new UniverSelObjct(response.body(), Tags.SB_RESEND_OTP_API, "true", ""));
                        } catch (Exception e) {
                            e.printStackTrace();
                            onResponse.onError(Tags.SB_RESEND_OTP_API, mContext.getResources().getString(R.string.something_wrong));
                        }
                    } else {
                        onResponse.onError(Tags.SB_RESEND_OTP_API, mContext.getResources().getString(R.string.something_wrong));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(Call<CommonBean> call, Throwable t) {
                if (isTrue) {
                    try {
                        SFProgress.hideProgressDialog(mContext);
                    } catch (Exception e) {
                    }
                }
                t.printStackTrace();
                onResponse.onError(Tags.SB_RESEND_OTP_API, mContext.getResources().getString(R.string.something_wrong));
            }
        });
    }

    public void verifySignUpOTP(OnResponse<UniverSelObjct> onResponse,Boolean isTrue,String... params) {
        if (isTrue) {
            try {
                SFProgress.showProgressDialog(mContext, true);
            } catch (Exception e) {
            }
        }
        ApiRequest mRequest = RetrofitRequest.getRetrofitInstance(1, 2).create(ApiRequest.class);
        mRequest.verifySignUpOTP(params[0],params[1],params[2],params[3],params[4] ).enqueue(new Callback<CommonBean>() {
            @Override
            public void onResponse(Call<CommonBean> call, Response<CommonBean> response) {
                if (isTrue) {
                    try {
                        SFProgress.hideProgressDialog(mContext);
                    } catch (Exception e) {
                    }
                }
                try {
                    if (response != null) {
                        try {
                            onResponse.onSuccess(new UniverSelObjct(response.body(), Tags.SB_SIGNUP_VERIFY_OTP_API, "true", ""));
                        } catch (Exception e) {
                            e.printStackTrace();
                            onResponse.onError(Tags.SB_SIGNUP_VERIFY_OTP_API, mContext.getResources().getString(R.string.something_wrong));
                        }
                    } else {
                        onResponse.onError(Tags.SB_SIGNUP_VERIFY_OTP_API, mContext.getResources().getString(R.string.something_wrong));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(Call<CommonBean> call, Throwable t) {
                if (isTrue) {
                    try {
                        SFProgress.hideProgressDialog(mContext);
                    } catch (Exception e) {
                    }
                }
                t.printStackTrace();
                onResponse.onError(Tags.SB_SIGNUP_VERIFY_OTP_API, mContext.getResources().getString(R.string.something_wrong));
            }
        });
    }
    public void forgotPassword(OnResponse<UniverSelObjct> onResponse, String... strParams) {
        try {
            SFProgress.showProgressDialog(mContext, true);
        } catch (Exception e) {
        }
        ApiRequest mRequest = RetrofitRequest.getRetrofitInstance(1, 2).create(ApiRequest.class);
        mRequest.forgotPassword(strParams[0],strParams[1]).enqueue(new Callback<CommonBean>() {
            @Override
            public void onResponse(Call<CommonBean> call, Response<CommonBean> response) {
                try {
                    SFProgress.hideProgressDialog(mContext);
                } catch (Exception e) {
                }
                try {
                    if (response != null) {
                        try {
                            onResponse.onSuccess(new UniverSelObjct(response.body(), Tags.SB_FORGOT_PASSWORD_API, "true", ""));
                        } catch (Exception e) {
                            e.printStackTrace();
                            onResponse.onError(Tags.SB_FORGOT_PASSWORD_API, mContext.getResources().getString(R.string.something_wrong));
                        }
                    } else {
                        onResponse.onError(Tags.SB_FORGOT_PASSWORD_API, mContext.getResources().getString(R.string.something_wrong));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<CommonBean> call, Throwable t) {
                try {
                    SFProgress.hideProgressDialog(mContext);
                } catch (Exception e) {
                }
                t.printStackTrace();
                onResponse.onError(Tags.SB_FORGOT_PASSWORD_API, mContext.getResources().getString(R.string.something_wrong));
            }
        });
    }
    public void resetPassword(OnResponse<UniverSelObjct> onResponse, String... strParams) {
        try {
            SFProgress.showProgressDialog(mContext, true);
        } catch (Exception e) {
        }
        ApiRequest mRequest = RetrofitRequest.getRetrofitInstance(1, 2).create(ApiRequest.class);
        mRequest.resetPassword(strParams[0],strParams[1],strParams[2] ).enqueue(new Callback<CommonBean>() {
            @Override
            public void onResponse(Call<CommonBean> call, Response<CommonBean> response) {
                try {
                    SFProgress.hideProgressDialog(mContext);
                } catch (Exception e) {
                }
                try {
                    if (response != null) {
                        try {
                            onResponse.onSuccess(new UniverSelObjct(response.body(), Tags.SB_FORGOT_PASSWORD_RESET_API, "true", ""));
                        } catch (Exception e) {
                            e.printStackTrace();
                            onResponse.onError(Tags.SB_FORGOT_PASSWORD_RESET_API, mContext.getResources().getString(R.string.something_wrong));
                        }
                    } else {
                        onResponse.onError(Tags.SB_FORGOT_PASSWORD_RESET_API, mContext.getResources().getString(R.string.something_wrong));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<CommonBean> call, Throwable t) {
                try {
                    SFProgress.hideProgressDialog(mContext);
                } catch (Exception e) {
                }
                t.printStackTrace();
                onResponse.onError(Tags.SB_FORGOT_PASSWORD_RESET_API, mContext.getResources().getString(R.string.something_wrong));
            }
        });
    }
    public void supportTicket(OnResponse<UniverSelObjct> onResponse,Boolean isTrue,String... params) {
        if (isTrue) {
            try {
                SFProgress.showProgressDialog(mContext, true);
            } catch (Exception e) {
            }
        }
        ApiRequest mRequest = RetrofitRequest.getRetrofitInstance(1, 2).create(ApiRequest.class);
        mRequest.supportTicket(params[0],params[1],params[2],params[3],params[4],params[5],params[6]).enqueue(new Callback<TicketsBean>() {
            @Override
            public void onResponse(Call<TicketsBean> call, Response<TicketsBean> response) {
                if (isTrue) {
                    try {
                        SFProgress.hideProgressDialog(mContext);
                    } catch (Exception e) {
                    }
                }
                try {
                    if (response != null) {
                        try {
                            //  CommonBean mBean = new Gson().fromJson(response.body().toString(), CommonBean.class);
                            onResponse.onSuccess(new UniverSelObjct(response.body(), Tags.SB_API_SUPPORT, "true", ""));
                        } catch (Exception e) {
                            e.printStackTrace();
                            onResponse.onError(Tags.SB_API_SUPPORT, mContext.getResources().getString(R.string.something_wrong));
                        }
                    } else {
                        onResponse.onError(Tags.SB_API_SUPPORT, mContext.getResources().getString(R.string.something_wrong));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(Call<TicketsBean> call, Throwable t) {
                if (isTrue) {
                    try {
                        SFProgress.hideProgressDialog(mContext);
                    } catch (Exception e) {
                    }
                }
                t.printStackTrace();
                onResponse.onError(Tags.SB_API_SUPPORT, mContext.getResources().getString(R.string.something_wrong));
            }
        });
    }

    public void getGames(OnResponse<UniverSelObjct> onResponse,Boolean isTrue,String... params) {
        if (isTrue) {
            try {
                SFProgress.showProgressDialog(mContext, true);
            } catch (Exception e) {
            }
        }
        ApiRequest mRequest = RetrofitRequest.getRetrofitInstance(1, 2).create(ApiRequest.class);
        mRequest.getGames(params[0],params[1]).enqueue(new Callback<GameBean>() {
            @Override
            public void onResponse(Call<GameBean> call, Response<GameBean> response) {
                if (isTrue) {
                    try {
                        SFProgress.hideProgressDialog(mContext);
                    } catch (Exception e) {
                    }
                }
                try {
                    if (response != null) {
                        try {
                            //  CommonBean mBean = new Gson().fromJson(response.body().toString(), CommonBean.class);
                            onResponse.onSuccess(new UniverSelObjct(response.body(), Tags.SB_GAME_CATEGORY_API, "true", ""));
                        } catch (Exception e) {
                            e.printStackTrace();
                            onResponse.onError(Tags.SB_GAME_CATEGORY_API, mContext.getResources().getString(R.string.something_wrong));
                        }
                    } else {
                        onResponse.onError(Tags.SB_GAME_CATEGORY_API, mContext.getResources().getString(R.string.something_wrong));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(Call<GameBean> call, Throwable t) {
                if (isTrue) {
                    try {
                        SFProgress.hideProgressDialog(mContext);
                    } catch (Exception e) {
                    }
                }
                t.printStackTrace();
                onResponse.onError(Tags.SB_GAME_CATEGORY_API, mContext.getResources().getString(R.string.something_wrong));
            }
        });
    }

    public void getTeams(OnResponse<UniverSelObjct> onResponse,Boolean isTrue,String... params) {
        if (isTrue) {
            try {
                SFProgress.showProgressDialog(mContext, true);
            } catch (Exception e) {
            }
        }
        ApiRequest mRequest = RetrofitRequest.getRetrofitInstance(1, 2).create(ApiRequest.class);
        mRequest.getTeams(params[0],params[1],params[2],params[3]).enqueue(new Callback<TeamsBean>() {
            @Override
            public void onResponse(Call<TeamsBean> call, Response<TeamsBean> response) {
                if (isTrue) {
                    try {
                        SFProgress.hideProgressDialog(mContext);
                    } catch (Exception e) {
                    }
                }
                try {
                    if (response != null) {
                        try {
                            onResponse.onSuccess(new UniverSelObjct(response.body(), Tags.SB_TEAMS_API, "true", ""));
                        } catch (Exception e) {
                            e.printStackTrace();
                            onResponse.onError(Tags.SB_TEAMS_API, mContext.getResources().getString(R.string.something_wrong));
                        }
                    } else {
                        onResponse.onError(Tags.SB_TEAMS_API, mContext.getResources().getString(R.string.something_wrong));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(Call<TeamsBean> call, Throwable t) {
                if (isTrue) {
                    try {
                        SFProgress.hideProgressDialog(mContext);
                    } catch (Exception e) {
                    }
                }
                t.printStackTrace();
                onResponse.onError(Tags.SB_TEAMS_API, mContext.getResources().getString(R.string.something_wrong));
            }
        });
    }

    public void getOpponentTeams(OnResponse<UniverSelObjct> onResponse,Boolean isTrue,String... params) {
        if (isTrue) {
            try {
                SFProgress.showProgressDialog(mContext, true);
            } catch (Exception e) {
            }
        }
        ApiRequest mRequest = RetrofitRequest.getRetrofitInstance(1, 2).create(ApiRequest.class);
        mRequest.getOpponentTeams(params[0],params[1],params[2],params[3],params[4]).enqueue(new Callback<TeamsBean>() {
            @Override
            public void onResponse(Call<TeamsBean> call, Response<TeamsBean> response) {
                if (isTrue) {
                    try {
                        SFProgress.hideProgressDialog(mContext);
                    } catch (Exception e) {
                    }
                }
                try {
                    if (response != null) {
                        try {
                            onResponse.onSuccess(new UniverSelObjct(response.body(), Tags.SB_OPPONENT_TEAMS_API, "true", ""));
                        } catch (Exception e) {
                            e.printStackTrace();
                            onResponse.onError(Tags.SB_OPPONENT_TEAMS_API, mContext.getResources().getString(R.string.something_wrong));
                        }
                    } else {
                        onResponse.onError(Tags.SB_OPPONENT_TEAMS_API, mContext.getResources().getString(R.string.something_wrong));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(Call<TeamsBean> call, Throwable t) {
                if (isTrue) {
                    try {
                        SFProgress.hideProgressDialog(mContext);
                    } catch (Exception e) {
                    }
                }
                t.printStackTrace();
                onResponse.onError(Tags.SB_OPPONENT_TEAMS_API, mContext.getResources().getString(R.string.something_wrong));
            }
        });
    }

    public void getLeagues(OnResponse<UniverSelObjct> onResponse,Boolean isTrue,String... params) {
        if (isTrue) {
            try {
                SFProgress.showProgressDialog(mContext, true);
            } catch (Exception e) {
            }
        }
        ApiRequest mRequest = RetrofitRequest.getRetrofitInstance(1, 2).create(ApiRequest.class);
        mRequest.getLeagues(params[0],params[1],params[2],params[3],params[4]).enqueue(new Callback<LeagueBean>() {
            @Override
            public void onResponse(Call<LeagueBean> call, Response<LeagueBean> response) {
                if (isTrue) {
                    try {
                        SFProgress.hideProgressDialog(mContext);
                    } catch (Exception e) {
                    }
                }
                try {
                    if (response != null) {
                        try {
                            onResponse.onSuccess(new UniverSelObjct(response.body(), Tags.SB_LEAGUE_API, "true", ""));
                        } catch (Exception e) {
                            e.printStackTrace();
                            onResponse.onError(Tags.SB_LEAGUE_API, mContext.getResources().getString(R.string.something_wrong));
                        }
                    } else {
                        onResponse.onError(Tags.SB_LEAGUE_API, mContext.getResources().getString(R.string.something_wrong));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(Call<LeagueBean> call, Throwable t) {
                if (isTrue) {
                    try {
                        SFProgress.hideProgressDialog(mContext);
                    } catch (Exception e) {
                    }
                }
                t.printStackTrace();
                onResponse.onError(Tags.SB_LEAGUE_API, mContext.getResources().getString(R.string.something_wrong));
            }
        });
    }

    public void getMatches(OnResponse<UniverSelObjct> onResponse,Boolean isTrue,String... params) {
        if (isTrue) {
            try {
                SFProgress.showProgressDialog(mContext, true);
            } catch (Exception e) {
            }
        }
        ApiRequest mRequest = RetrofitRequest.getRetrofitInstance(1, 2).create(ApiRequest.class);
        mRequest.getMatches(params[0],params[1],params[2],params[3],params[4]).enqueue(new Callback<MatchesBean>() {
            @Override
            public void onResponse(Call<MatchesBean> call, Response<MatchesBean> response) {
                if (isTrue) {
                    try {
                        SFProgress.hideProgressDialog(mContext);
                    } catch (Exception e) {
                    }
                }
                try {
                    if (response != null) {
                        try {
                            onResponse.onSuccess(new UniverSelObjct(response.body(), Tags.SB_USER_MATCHES_API, "true", ""));
                        } catch (Exception e) {
                            e.printStackTrace();
                            onResponse.onError(Tags.SB_USER_MATCHES_API, mContext.getResources().getString(R.string.something_wrong));
                        }
                    } else {
                        onResponse.onError(Tags.SB_USER_MATCHES_API, mContext.getResources().getString(R.string.something_wrong));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(Call<MatchesBean> call, Throwable t) {
                if (isTrue) {
                    try {
                        SFProgress.hideProgressDialog(mContext);
                    } catch (Exception e) {
                    }
                }
                t.printStackTrace();
                onResponse.onError(Tags.SB_USER_MATCHES_API, mContext.getResources().getString(R.string.something_wrong));
            }
        });
    }

    public void getMatchDetail(OnResponse<UniverSelObjct> onResponse,Boolean isTrue,String... params) {
        if (isTrue) {
            try {
                SFProgress.showProgressDialog(mContext, true);
            } catch (Exception e) {
            }
        }
        ApiRequest mRequest = RetrofitRequest.getRetrofitInstance(1, 2).create(ApiRequest.class);
        mRequest.getMatchDetail(params[0],params[1] ).enqueue(new Callback<MatchesBean>() {
            @Override
            public void onResponse(Call<MatchesBean> call, Response<MatchesBean> response) {
                if (isTrue) {
                    try {
                        SFProgress.hideProgressDialog(mContext);
                    } catch (Exception e) {
                    }
                }
                try {
                    if (response != null) {
                        try {
                            onResponse.onSuccess(new UniverSelObjct(response.body(), Tags.SB_MATCH_DETAIL_API, "true", ""));
                        } catch (Exception e) {
                            e.printStackTrace();
                            onResponse.onError(Tags.SB_MATCH_DETAIL_API, mContext.getResources().getString(R.string.something_wrong));
                        }
                    } else {
                        onResponse.onError(Tags.SB_MATCH_DETAIL_API, mContext.getResources().getString(R.string.something_wrong));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(Call<MatchesBean> call, Throwable t) {
                if (isTrue) {
                    try {
                        SFProgress.hideProgressDialog(mContext);
                    } catch (Exception e) {
                    }
                }
                t.printStackTrace();
                onResponse.onError(Tags.SB_MATCH_DETAIL_API, mContext.getResources().getString(R.string.something_wrong));
            }
        });
    }
    public void deleteVideos(OnResponse<UniverSelObjct> onResponse,Boolean isTrue,String... params) {
        if (isTrue) {
            try {
                SFProgress.showProgressDialog(mContext, true);
            } catch (Exception e) {
            }
        }
        ApiRequest mRequest = RetrofitRequest.getRetrofitInstance(1, 2).create(ApiRequest.class);
        mRequest.deleteVideos(params[0],params[1] ,params[2] ,params[3] ).enqueue(new Callback<CommonBean>() {
            @Override
            public void onResponse(Call<CommonBean> call, Response<CommonBean> response) {
                if (isTrue) {
                    try {
                        SFProgress.hideProgressDialog(mContext);
                    } catch (Exception e) {
                    }
                }
                try {
                    if (response != null) {
                        try {
                            onResponse.onSuccess(new UniverSelObjct(response.body(), Tags.SB_DELETE_VIDEO_API, "true", ""));
                        } catch (Exception e) {
                            e.printStackTrace();
                            onResponse.onError(Tags.SB_DELETE_VIDEO_API, mContext.getResources().getString(R.string.something_wrong));
                        }
                    } else {
                        onResponse.onError(Tags.SB_DELETE_VIDEO_API, mContext.getResources().getString(R.string.something_wrong));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(Call<CommonBean> call, Throwable t) {
                if (isTrue) {
                    try {
                        SFProgress.hideProgressDialog(mContext);
                    } catch (Exception e) {
                    }
                }
                t.printStackTrace();
                onResponse.onError(Tags.SB_DELETE_VIDEO_API, mContext.getResources().getString(R.string.something_wrong));
            }
        });
    }

    public void getCounty(OnResponse<UniverSelObjct> onResponse,Boolean isTrue,String... params) {
        if (isTrue) {
            try {
                SFProgress.showProgressDialog(mContext, true);
            } catch (Exception e) {
            }
        }
        ApiRequest mRequest = RetrofitRequest.getRetrofitInstance(1, 2).create(ApiRequest.class);
        mRequest.getCounty(params[0],params[1],params[2]).enqueue(new Callback<CountyBean>() {
            @Override
            public void onResponse(Call<CountyBean> call, Response<CountyBean> response) {
                if (isTrue) {
                    try {
                        SFProgress.hideProgressDialog(mContext);
                    } catch (Exception e) {
                    }
                }
                try {
                    if (response != null) {
                        try {
                            onResponse.onSuccess(new UniverSelObjct(response.body(), Tags.SB_COUNTY_API, "true", ""));
                        } catch (Exception e) {
                            e.printStackTrace();
                            onResponse.onError(Tags.SB_COUNTY_API, mContext.getResources().getString(R.string.something_wrong));
                        }
                    } else {
                        onResponse.onError(Tags.SB_COUNTY_API, mContext.getResources().getString(R.string.something_wrong));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(Call<CountyBean> call, Throwable t) {
                if (isTrue) {
                    try {
                        SFProgress.hideProgressDialog(mContext);
                    } catch (Exception e) {
                    }
                }
                t.printStackTrace();
                onResponse.onError(Tags.SB_COUNTY_API, mContext.getResources().getString(R.string.something_wrong));
            }
        });
    }

    public void getSubjects(OnResponse<UniverSelObjct> onResponse,Boolean isTrue,String... params) {
        if (isTrue) {
            try {
                SFProgress.showProgressDialog(mContext, true);
            } catch (Exception e) {
            }
        }
        ApiRequest mRequest = RetrofitRequest.getRetrofitInstance(1, 2).create(ApiRequest.class);
        mRequest.getSubjects("1").enqueue(new Callback<SubjectsBean>() {
            @Override
            public void onResponse(Call<SubjectsBean> call, Response<SubjectsBean> response) {
                if (isTrue) {
                    try {
                        SFProgress.hideProgressDialog(mContext);
                    } catch (Exception e) {
                    }
                }
                try {
                    if (response != null) {
                        try {
                            onResponse.onSuccess(new UniverSelObjct(response.body(), Tags.SB_SUPPORT_SUBJECTS_API, "true", ""));
                        } catch (Exception e) {
                            e.printStackTrace();
                            onResponse.onError(Tags.SB_SUPPORT_SUBJECTS_API, mContext.getResources().getString(R.string.something_wrong));
                        }
                    } else {
                        onResponse.onError(Tags.SB_SUPPORT_SUBJECTS_API, mContext.getResources().getString(R.string.something_wrong));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(Call<SubjectsBean> call, Throwable t) {
                if (isTrue) {
                    try {
                        SFProgress.hideProgressDialog(mContext);
                    } catch (Exception e) {
                    }
                }
                t.printStackTrace();
                onResponse.onError(Tags.SB_SUPPORT_SUBJECTS_API, mContext.getResources().getString(R.string.something_wrong));
            }
        });
    }

    public void createMatch(OnResponse<UniverSelObjct> onResponse,Boolean isTrue,String... params) {
        if (isTrue) {
            try {
                SFProgress.showProgressDialog(mContext, true);
            } catch (Exception e) {
            }
        }
        ApiRequest mRequest = RetrofitRequest.getRetrofitInstance(1, 2).create(ApiRequest.class);
        mRequest.createMatch(params[0],params[1],params[2],params[3],params[4],params[5],params[6],params[7],params[8],params[9]).enqueue(new Callback<MatchesBean>() {
            @Override
            public void onResponse(Call<MatchesBean> call, Response<MatchesBean> response) {
                if (isTrue) {
                    try {
                        SFProgress.hideProgressDialog(mContext);
                    } catch (Exception e) {
                    }
                }
                try {
                    if (response != null) {
                        try {
                            onResponse.onSuccess(new UniverSelObjct(response.body(), Tags.SB_CREATE_MATCH_API, "true", ""));
                        } catch (Exception e) {
                            e.printStackTrace();
                            onResponse.onError(Tags.SB_CREATE_MATCH_API, mContext.getResources().getString(R.string.something_wrong));
                        }
                    } else {
                        onResponse.onError(Tags.SB_CREATE_MATCH_API, mContext.getResources().getString(R.string.something_wrong));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(Call<MatchesBean> call, Throwable t) {
                if (isTrue) {
                    try {
                        SFProgress.hideProgressDialog(mContext);
                    } catch (Exception e) {
                    }
                }
                t.printStackTrace();
                onResponse.onError(Tags.SB_CREATE_MATCH_API, mContext.getResources().getString(R.string.something_wrong));
            }
        });
    }

    public void makeActions(OnResponse<UniverSelObjct> onResponse, Boolean isTrue, RequestBody user_id, RequestBody match_id,
                            RequestBody team_id, RequestBody time, RequestBody reaction, RequestBody half, MultipartBody.Part mVideo, ReactionsBean mReactionsBean) {
         if (isTrue) {
            try {
                SFProgress.showProgressDialog(mContext, true);
            } catch (Exception e) {
            }
        }

        ApiRequest mRequest = RetrofitRequest.getRetrofitInstance(1, 2).create(ApiRequest.class);
        mRequest.makeActions(user_id,match_id, team_id,time,reaction,half,mVideo).enqueue(new Callback<CommonBean>() {
            @Override
            public void onResponse(Call<CommonBean> call, Response<CommonBean> response) {
                if (isTrue) {
                    try {
                        SFProgress.hideProgressDialog(mContext);
                    } catch (Exception e) {
                    }
                }
                try {
                    if (response != null) {
                        try {
                            onResponse.onSuccess(new UniverSelObjct(response.body(), Tags.SB_CREATE_MATCH_ACTION_API, "true", new Gson().toJson(mReactionsBean)));
                        } catch (Exception e) {
                            e.printStackTrace();
                            onResponse.onError(Tags.SB_CREATE_MATCH_ACTION_API, mContext.getResources().getString(R.string.something_wrong));
                        }
                    } else {
                        onResponse.onError(Tags.SB_CREATE_MATCH_ACTION_API, mContext.getResources().getString(R.string.something_wrong));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(Call<CommonBean> call, Throwable t) {
                if (isTrue) {
                    try {
                        SFProgress.hideProgressDialog(mContext);
                    } catch (Exception e) {
                    }
                }
                t.printStackTrace();
                try{
                    onResponse.onError(Tags.SB_CREATE_MATCH_ACTION_API, mContext.getResources().getString(R.string.something_wrong));
                }catch (Exception e11){
                    e11.printStackTrace();
                }
            }
        });
    }

    public void uploadInterview(OnResponse<UniverSelObjct> onResponse, Boolean isTrue, RequestBody user_id, RequestBody match_id,
                           MultipartBody.Part mVideo, InterviewBean mReactionsBean) {
         if (isTrue) {
            try {
                SFProgress.showProgressDialog(mContext, true);
            } catch (Exception e) {
            }
        }

        ApiRequest mRequest = RetrofitRequest.getRetrofitInstance(1, 2).create(ApiRequest.class);
        mRequest.uploadInterview(user_id,match_id,mVideo).enqueue(new Callback<CommonBean>() {
            @Override
            public void onResponse(Call<CommonBean> call, Response<CommonBean> response) {
                if (isTrue) {
                    try {
                        SFProgress.hideProgressDialog(mContext);
                    } catch (Exception e) {
                    }
                }
                try {
                    if (response != null) {
                        try {
                            onResponse.onSuccess(new UniverSelObjct(response.body(), Tags.SB_UPLOAD_INTERVIEW_API, "true", new Gson().toJson(mReactionsBean)));
                        } catch (Exception e) {
                            e.printStackTrace();
                            onResponse.onError(Tags.SB_UPLOAD_INTERVIEW_API, mContext.getResources().getString(R.string.something_wrong));
                        }
                    } else {
                        onResponse.onError(Tags.SB_UPLOAD_INTERVIEW_API, mContext.getResources().getString(R.string.something_wrong));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(Call<CommonBean> call, Throwable t) {
                if (isTrue) {
                    try {
                        SFProgress.hideProgressDialog(mContext);
                    } catch (Exception e) {
                    }
                }
                t.printStackTrace();
                try{
                    onResponse.onError(Tags.SB_UPLOAD_INTERVIEW_API, mContext.getResources().getString(R.string.something_wrong));
                }catch (Exception ex1){
                    onResponse.onError(Tags.SB_UPLOAD_INTERVIEW_API, "Something went wrong, try again1.");
                }
            }
        });
    }

    public void updateProfile(OnResponse<UniverSelObjct> onResponse, RequestBody firstname, RequestBody lastname,
                            RequestBody user_id, RequestBody phone, RequestBody username, RequestBody email, MultipartBody.Part image) {
             try {
                SFProgress.showProgressDialog(mContext, true);
            } catch (Exception e) {
            }
        ApiRequest mRequest = RetrofitRequest.getRetrofitInstance(1, 2).create(ApiRequest.class);
        Call mCall =  mRequest.updateProfile(firstname,lastname, user_id,phone,username,email);
        if (image !=null){
            mCall =  mRequest.updateProfileWithImage(firstname,lastname, user_id,phone,username,email,image);
        }
        mCall.enqueue(new Callback<CommonBean>() {
            @Override
            public void onResponse(Call<CommonBean> call, Response<CommonBean> response) {
                     try {
                        SFProgress.hideProgressDialog(mContext);
                    } catch (Exception e) {
                    }

                try {
                    if (response != null) {
                        try {
                            onResponse.onSuccess(new UniverSelObjct(response.body(), Tags.SB_UPDATE_PROFILE_API, "true", ""));
                        } catch (Exception e) {
                            e.printStackTrace();
                            onResponse.onError(Tags.SB_UPDATE_PROFILE_API, mContext.getResources().getString(R.string.something_wrong));
                        }
                    } else {
                        onResponse.onError(Tags.SB_UPDATE_PROFILE_API, mContext.getResources().getString(R.string.something_wrong));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(Call<CommonBean> call, Throwable t) {
                     try {
                        SFProgress.hideProgressDialog(mContext);
                    } catch (Exception e) {
                    }
                t.printStackTrace();
                onResponse.onError(Tags.SB_UPDATE_PROFILE_API, mContext.getResources().getString(R.string.something_wrong));
            }
        });
    }

    public void deleteAccount(OnResponse<UniverSelObjct> onResponse,Boolean isTrue,String... params) {
        if (isTrue) {
            try {
                SFProgress.showProgressDialog(mContext, true);
            } catch (Exception e) {
            }
        }
        ApiRequest mRequest = RetrofitRequest.getRetrofitInstance(1, 2).create(ApiRequest.class);
        mRequest.deleteAccount(params[0]).enqueue(new Callback<CommonBean>() {
            @Override
            public void onResponse(Call<CommonBean> call, Response<CommonBean> response) {
                if (isTrue) {
                    try {
                        SFProgress.hideProgressDialog(mContext);
                    } catch (Exception e) {
                    }
                }
                try {
                    if (response != null) {
                        try {
                            onResponse.onSuccess(new UniverSelObjct(response.body(), Tags.SB_DELETE_ACCOUNT_API, "true", ""));
                        } catch (Exception e) {
                            e.printStackTrace();
                            onResponse.onError(Tags.SB_DELETE_ACCOUNT_API, mContext.getResources().getString(R.string.something_wrong));
                        }
                    } else {
                        onResponse.onError(Tags.SB_DELETE_ACCOUNT_API, mContext.getResources().getString(R.string.something_wrong));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(Call<CommonBean> call, Throwable t) {
                if (isTrue) {
                    try {
                        SFProgress.hideProgressDialog(mContext);
                    } catch (Exception e) {
                    }
                }
                t.printStackTrace();
                onResponse.onError(Tags.SB_DELETE_ACCOUNT_API, mContext.getResources().getString(R.string.something_wrong));
            }
        });
    }


}
