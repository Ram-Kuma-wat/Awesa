package com.codersworld.awesalibs.database.dao;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.codersworld.awesalibs.beans.VideoUploadBean;
import com.codersworld.awesalibs.database.DatabaseHelper;
import com.codersworld.awesalibs.utils.CommonMethods;

import java.util.ArrayList;

public class VideoMasterDAO {

    private static final String TAG = VideoMasterDAO.class.getSimpleName();
    private static final String TABLE_VIDEO_MASTER = "video_master";

    // Contacts Table Columns names
    private static final String COLUMN_KEY_ID = "_id";
    private static final String COLUMN_VIDEO_NAME = "video_name";
    private static final String COLUMN_MATCH_ID = "match_id";
    private static final String COLUMN_VIDEO_TYPE = "video_ext";
    private static final String COLUMN_VIDEO_HALF = "video_half";
    private static final String COLUMN_VIDEO_PATH = "video_path";
    private static final String COLUMN_VIDEO_STATUS = "upload_status";
    private static final String COLUMN_DATE = "date";

    private SQLiteDatabase mDatabase;
    private final Context mContext;

    public VideoMasterDAO(SQLiteDatabase database, Context context) {
        mDatabase = database;
        mContext = context;
    }

    public void initDBHelper() {
        try {
            DatabaseHelper mHelper = new DatabaseHelper(mContext);
            mDatabase = mHelper.getWritableDatabase();
        } catch (SQLiteException e) {
            Log.e("VideoMasterDAO", e.getLocalizedMessage(), e);
        }
    }

    public void updateVideoAll() {
        ContentValues values = new ContentValues();
        values.put(COLUMN_VIDEO_STATUS, String.valueOf(0));

        String selection = "1 = ?";
        String[] selectionArgs = { String.valueOf(1) };

        mDatabase.update(TABLE_VIDEO_MASTER, values, selection, selectionArgs);
    }

    public static String getCreateTableVideoMaster() {
        return "CREATE TABLE " + TABLE_VIDEO_MASTER
                + "("
                + COLUMN_KEY_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_VIDEO_NAME + " TEXT ,"
                + COLUMN_MATCH_ID + " TEXT ,"
                + COLUMN_VIDEO_TYPE + " TEXT ,"
                + COLUMN_VIDEO_HALF + " TEXT ,"
                + COLUMN_VIDEO_PATH + " TEXT ,"
                + COLUMN_VIDEO_STATUS + " TEXT ,"
                + COLUMN_DATE + " TEXT)";
    }

    public static String getDropTableVideoMaster() {
        return "DROP TABLE IF EXISTS " + TABLE_VIDEO_MASTER;
    }

    public void deleteAll() {
        initDBHelper();
        String[] selectionArgs = { String.valueOf(1) };

        mDatabase.delete(TABLE_VIDEO_MASTER, "1 = ?", selectionArgs);
    }

    public void insert(VideoUploadBean videoUpload) {
        initDBHelper();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_MATCH_ID, videoUpload.getMatch_id());
        contentValues.put(COLUMN_VIDEO_NAME, videoUpload.getVideo_name());
        contentValues.put(COLUMN_VIDEO_HALF, videoUpload.getVideo_half());
        contentValues.put(COLUMN_VIDEO_STATUS, videoUpload.getUpload_status());
        contentValues.put(COLUMN_VIDEO_TYPE, videoUpload.getVideo_ext());
        contentValues.put(COLUMN_VIDEO_PATH, videoUpload.getVideo_path());
        contentValues.put(COLUMN_DATE, videoUpload.getDate());

        mDatabase.insert(TABLE_VIDEO_MASTER, null, contentValues);
    }

    public void insert(ArrayList<VideoUploadBean> arrayList) {
        initDBHelper();
        for (VideoUploadBean videoUpload : arrayList) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(COLUMN_MATCH_ID, videoUpload.getMatch_id());
            contentValues.put(COLUMN_VIDEO_NAME, videoUpload.getVideo_name());
            contentValues.put(COLUMN_VIDEO_HALF, videoUpload.getVideo_half());
            contentValues.put(COLUMN_VIDEO_STATUS, videoUpload.getUpload_status());
            contentValues.put(COLUMN_VIDEO_TYPE, videoUpload.getVideo_ext());
            contentValues.put(COLUMN_VIDEO_PATH, videoUpload.getVideo_path());
            contentValues.put(COLUMN_DATE, videoUpload.getDate());

            mDatabase.insert(TABLE_VIDEO_MASTER, null, contentValues);
        }
    }

    public void updateVideo(int id, int isUploaded) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_VIDEO_STATUS, String.valueOf(isUploaded));

        String selection = COLUMN_KEY_ID + " = ?";
        String[] selectionArgs = { String.valueOf(id) };

        mDatabase.update(TABLE_VIDEO_MASTER, values, selection, selectionArgs);
    }

    public int getTotalCount(String id) {
        initDBHelper();
        int count = 0;
        String query = "SELECT COUNT(*) FROM match_reactions where 1=1";
        if (CommonMethods.isValidString(id)){
            query +=" AND match_id="+id;
        }
        Cursor cursor = mDatabase.rawQuery(query, null);
        if (cursor.moveToNext()) {
            count = cursor.getInt(0);
        }
        closeCursor(cursor);
        return count;
    }

    public int deleteVideoById(int id) {
        initDBHelper();

        String selection = "1 = 1 AND " + COLUMN_MATCH_ID + " = ?";
        String[] selectionArgs = {String.valueOf(id)};

        return mDatabase.delete(TABLE_VIDEO_MASTER, selection, selectionArgs);
    }

    public int deleteVideoByMatch(int id, int video_half) {
        initDBHelper();
        String selection = "1 = 1 AND " + COLUMN_MATCH_ID + " = ? AND " + COLUMN_VIDEO_HALF + " = ?";
        String[] selectionArgs = {
            String.valueOf(id),
            String.valueOf(video_half)
        };

        return mDatabase.delete(TABLE_VIDEO_MASTER, selection, selectionArgs);
    }

    public int getLatestInsertedId() {
        initDBHelper();
        String countQuery = "SELECT  max(_id) FROM " + TABLE_VIDEO_MASTER;
        Cursor cursor = mDatabase.rawQuery(countQuery, null);
        int maxid = 0;
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                maxid = cursor.getInt(0);
            }
            closeCursor(cursor);
        }
        return maxid;
    }

    public ArrayList<VideoUploadBean> selectAll() {
        initDBHelper();

        String selection = "1 = 1";

        Cursor cursor = mDatabase.query(TABLE_VIDEO_MASTER, new String[] {"*"}, selection, null, null, null,  COLUMN_KEY_ID + " DESC");

        ArrayList<VideoUploadBean>  dataList = manageCursor(cursor);

        cursor.close();
        return dataList;
    }

    public ArrayList<VideoUploadBean> selectAll(String match_id, String video_half) {
        initDBHelper();

        String selection = COLUMN_MATCH_ID + " = ? AND " + COLUMN_VIDEO_HALF + " = ?";
        String[] selectionArgs = {
                String.valueOf(match_id),
                String.valueOf(video_half)
        };

        Cursor cursor = mDatabase.query(TABLE_VIDEO_MASTER, new String[] {"*"}, selection, selectionArgs, null, null,  COLUMN_KEY_ID + " DESC");

        ArrayList<VideoUploadBean>  dataList = manageCursor(cursor);

        cursor.close();
        return dataList;
    }

    @SuppressLint("Range")
    protected VideoUploadBean cursorToData(Cursor cursor) {
        VideoUploadBean model = new VideoUploadBean();
        model.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_KEY_ID)));
        model.setVideo_name(cursor.getString(cursor.getColumnIndex(COLUMN_VIDEO_NAME)));
        model.setMatch_id(cursor.getString(cursor.getColumnIndex(COLUMN_MATCH_ID)));
        model.setVideo_ext(cursor.getString(cursor.getColumnIndex(COLUMN_VIDEO_TYPE)));
        model.setVideo_half(cursor.getString(cursor.getColumnIndex(COLUMN_VIDEO_HALF)));
        model.setVideo_path(cursor.getString(cursor.getColumnIndex(COLUMN_VIDEO_PATH)));
        model.setUpload_status(cursor.getInt(cursor.getColumnIndex(COLUMN_VIDEO_STATUS)));
        model.setDate(cursor.getString(cursor.getColumnIndex(COLUMN_DATE)));
        return model;
    }

    protected void closeCursor(Cursor cursor) {
        if (cursor != null) {
            cursor.close();
        }
    }

    protected ArrayList<VideoUploadBean> manageCursor(Cursor cursor) {
        ArrayList<VideoUploadBean> dataList = new ArrayList<VideoUploadBean>();
        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                VideoUploadBean singleModel = cursorToData(cursor);
                if (singleModel != null) {
                    dataList.add(singleModel);
                }
                cursor.moveToNext();
            }
        }
        return dataList;
    }
}
