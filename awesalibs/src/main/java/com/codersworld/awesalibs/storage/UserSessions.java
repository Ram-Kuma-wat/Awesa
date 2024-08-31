package com.codersworld.awesalibs.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


import com.codersworld.awesalibs.beans.user.UserBean;
import com.codersworld.awesalibs.utils.CommonMethods;
import com.codersworld.awesalibs.utils.Tags;
import com.google.gson.Gson;

public class UserSessions {
    public static SharedPreferences mPrefs = null;
    public static SharedPreferences.Editor prefsEditor = null;
    public static Context mContext;

    public UserSessions() {

    }

    public UserSessions(Context ctx) {
        this.mContext = ctx;
        initSession();
    }

    public static void initSession() {
        if (mPrefs == null) {
            mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        }
        if (prefsEditor == null) {
            prefsEditor = mPrefs.edit();
        }
    }

    public static void saveIsProfile(Context ctx, String strProfile) {
        mContext = ctx;
        initSession();
        prefsEditor.putString(Tags.SB_IS_PROFILE, strProfile);
        prefsEditor.commit();
    }

    public static String getIsProfile(Context ctx) {
        mContext = ctx;
        initSession();
        return mPrefs.getString(Tags.SB_IS_PROFILE, "-1");
    }

    public static void saveAccessToken(Context ctx, String strToken) {
        mContext = ctx;
        initSession();
        prefsEditor.putString(Tags.SB_ACCESS_TOKEN, strToken);
        prefsEditor.commit();
    }

    public static String getAccessToken(Context ctx) {
        mContext = ctx;
        initSession();
        return mPrefs.getString(Tags.SB_ACCESS_TOKEN, "");
    }

    public static void saveAppStart(Context ctx, String strVal) {
        mContext = ctx;
        initSession();
        prefsEditor.putString(Tags.SB_APP_START, strVal);
        prefsEditor.commit();
    }

    public static String getAppStart(Context ctx) {
        mContext = ctx;
        initSession();
        return mPrefs.getString(Tags.SB_APP_START, "");
    }

    public static void saveUserInfo(Context ctx, UserBean mUserBean) {
        mContext = ctx;
        initSession();
        prefsEditor.putString(Tags.SB_USER_INFO, (mUserBean !=null)?new Gson().toJson(mUserBean):"");
        prefsEditor.commit();
    }

    public static void clearUserInfo(Context ctx) {
        mContext = ctx;
        initSession();
        prefsEditor.putString(Tags.SB_USER_INFO, "");
        prefsEditor.commit();
    }

    public static UserBean getUserInfo(Context ctx) {
        mContext = ctx;
        initSession();
        if (mPrefs.getString(Tags.SB_USER_INFO, "").isEmpty()) {
            return null;
        } else {
            return new Gson().fromJson(mPrefs.getString(Tags.SB_USER_INFO, ""), UserBean.class);
        }
    }

    public static void saveAppStatus(Context context, String str) {
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(Tags.SB_APP_STATUS, str);
        editor.commit();
    }

    public static String getAppStatus(Context context) {
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return mPrefs.getString(Tags.SB_APP_STATUS, "");
    }

    public static void saveSponsors(Context context, String str) {
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString("sponsors", str);
        editor.commit();
    }

    public static String getSponsors(Context context) {
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return mPrefs.getString("sponsors", "");
    }

    public static void saveOpened(Context context, int str) {
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putInt(Tags.SB_IS_OPENED, str);
        editor.commit();
    }

    public static int getOpened(Context context) {
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return mPrefs.getInt(Tags.SB_IS_OPENED, 0);
    }
   public static void saveFcmToken(Context context, String str) {
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(Tags.SB_FCM_ID, CommonMethods.isValidString(str)?str:"");
        editor.commit();
    }

    public static String getFcmToken(Context context) {
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return mPrefs.getString(Tags.SB_FCM_ID, "NO GCM");
    }
   public static void saveLanguage(Context context, String str) {
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(Tags.SB_LANGUAGE, CommonMethods.isValidString(str)?str:"");
        editor.commit();
    }

    public static String getLanguage(Context context) {
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return mPrefs.getString(Tags.SB_LANGUAGE, "");
    }

     public static void saveUpdate(Context context, int str) {
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putInt("isUpdate", str);
        editor.commit();
    }

    public static int getUpdate(Context context) {
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return mPrefs.getInt("isUpdate", 0);
    }

}
