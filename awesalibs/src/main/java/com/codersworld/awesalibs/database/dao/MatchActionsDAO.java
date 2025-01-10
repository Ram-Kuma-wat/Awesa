package com.codersworld.awesalibs.database.dao;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.codersworld.awesalibs.beans.VideoUploadBean;
import com.codersworld.awesalibs.beans.matches.InterviewBean;
import com.codersworld.awesalibs.beans.matches.ReactionsBean;
import com.codersworld.awesalibs.database.DatabaseHelper;
import com.codersworld.awesalibs.utils.CommonMethods;

import java.util.ArrayList;

public class MatchActionsDAO {

    private static final String TAG = MatchActionsDAO.class.getSimpleName();
    private static final String TABLE_MATCH_REACTIONS = "match_reactions";

    // Contacts Table Columns names
    private static final String COLUMN_KEY_ID = "id";
    private static final String COLUMN_MATCH_ID = "match_id";
    private static final String COLUMN_TEAM_ID = "team_id";
    private static final String COLUMN_HALF = "half";
    private static final String COLUMN_TIME = "time";
    private static final String COLUMN_TIMESTAMP = "timestamp";
    private static final String COLUMN_REACTION = "reaction";
    private static final String COLUMN_VIDEO_NAME = "video_name";
    private static final String COLUMN_VIDEO_PATH = "video_path";
    private static final String COLUMN_CREATED_DATE = "created_date";
    private static final String COLUMN_STATUS = "upload_status";
    private static final String COLUMN_TEAM_NAME = "team_name";

    private SQLiteDatabase mDatabase;
    private final Context mContext;

    public MatchActionsDAO(SQLiteDatabase database, Context context) {
        mDatabase = database;
        mContext = context;
    }

    public static String getCreateTable() {
        return "CREATE TABLE " + TABLE_MATCH_REACTIONS
                + "("
                + COLUMN_KEY_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_MATCH_ID + " INT ,"
                + COLUMN_TEAM_ID + " INT ,"
                + COLUMN_HALF + " INT ,"
                + COLUMN_TIME + " TEXT ,"
                + COLUMN_TIMESTAMP + " INTEGER ,"
                + COLUMN_REACTION + " TEXT ,"
                + COLUMN_VIDEO_NAME + " TEXT ,"
                + COLUMN_VIDEO_PATH + " TEXT ,"
                + COLUMN_STATUS + " INT ,"
                + COLUMN_TEAM_NAME + " TEXT ,"
                + COLUMN_CREATED_DATE + " TEXT)";
    }

    public static String getDropTable() {
        return "DROP TABLE IF EXISTS " + TABLE_MATCH_REACTIONS;
    }

    public void initDBHelper() {
        try {
            DatabaseHelper mHelper = new DatabaseHelper(mContext);
            mDatabase = mHelper.getWritableDatabase();
        } catch (SQLiteException e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
        }
    }

    public void deleteAll(int id) {
        initDBHelper();

        String[] selectionArgs = { String.valueOf(id) };

        mDatabase.delete(TABLE_MATCH_REACTIONS, "id = ?", selectionArgs);
    }

    public void deleteByMatch(String id, String half, int counter) {
        initDBHelper();

        String selection = "1 = 1 AND match_id = ? AND half = ?";
        String[] selectionArgs = { id, half };

        mDatabase.delete(TABLE_MATCH_REACTIONS, selection, selectionArgs);
    }

    public void deleteUploadedVideos() {
        initDBHelper();

        String[] selectionArgs = { String.valueOf(1) };
        mDatabase.delete(TABLE_MATCH_REACTIONS, "upload_status = ?", selectionArgs);
    }

    public void insert(ArrayList<ReactionsBean> arrayList) {
        initDBHelper();

        for (ReactionsBean mBean : arrayList) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(COLUMN_MATCH_ID, mBean.getMatch_id());
            contentValues.put(COLUMN_TEAM_ID, mBean.getTeam_id());
            contentValues.put(COLUMN_HALF, mBean.getHalf());
            contentValues.put(COLUMN_TIME, mBean.getTime());
            contentValues.put(COLUMN_TIMESTAMP, mBean.getTimestamp());
            contentValues.put(COLUMN_REACTION, mBean.getReaction());
            contentValues.put(COLUMN_VIDEO_NAME, mBean.getFile_name());
            contentValues.put(COLUMN_VIDEO_PATH, mBean.getVideo());
            contentValues.put(COLUMN_STATUS, 0);
            contentValues.put(COLUMN_CREATED_DATE, mBean.getCreated_date());
            contentValues.put(COLUMN_TEAM_NAME, mBean.getTeam_name());

            mDatabase.insert(TABLE_MATCH_REACTIONS, null, contentValues);
        }
    }

    public int getGoalCount(String team_id, String match_id) {
        initDBHelper();
        int count = 0;
        String query = "SELECT COUNT(*) FROM " + TABLE_MATCH_REACTIONS + " where 1=1 AND reaction = 'goal'";
        if (CommonMethods.isValidString(team_id)) {
            query += " AND team_id = " + team_id;
        }
        if (CommonMethods.isValidString(match_id)) {
            query += " AND match_id = " + match_id;
        }
        Cursor cursor = mDatabase.rawQuery(query, null);
        if (cursor.moveToNext()) {
            count = cursor.getInt(0);
        }
        closeCursor(cursor);
        return count;
    }

    public ArrayList<ReactionsBean> selectAll(String match_id, String half, int type) {
        initDBHelper();
        String getAllDetails = " SELECT * FROM " + TABLE_MATCH_REACTIONS + " where upload_status = 0 " + ((CommonMethods.isValidString(match_id)) ? " AND match_id=" + match_id : "") + ((CommonMethods.isValidString(half)) ? " AND half=" + half : "") + ((type == 0) ? " AND video_path !=''" : "") + " order by id DESC";
         Cursor cursor = mDatabase.rawQuery(getAllDetails, null);
        ArrayList<ReactionsBean> dataList = manageCursor(cursor);
        closeCursor(cursor);
        return dataList;
    }

    public ArrayList<ReactionsBean> selectAllForDelete(String match_id, String half) {
        initDBHelper();
        String getAllDetails = " SELECT * FROM " + TABLE_MATCH_REACTIONS + " where 1=1 " + ((CommonMethods.isValidString(match_id)) ? " AND match_id=" + match_id : "") + ((CommonMethods.isValidString(half)) ? " AND half=" + half : "") +  " AND video_path =''"  + " order by id DESC";
        Cursor cursor = mDatabase.rawQuery(getAllDetails, null);
        ArrayList<ReactionsBean> dataList = manageCursor(cursor);
        closeCursor(cursor);
        return dataList;
    }

    public ArrayList<ReactionsBean> selectAllForPreview(String match_id) {
        initDBHelper();

        String selection = "1 = 1 AND " + COLUMN_MATCH_ID + " = ?";
        String[] selectionArgs = {
                match_id,
        };

        Cursor cursor = mDatabase.query(
            TABLE_MATCH_REACTIONS, new String[] {"*"},
            selection,
            selectionArgs,
            null,
            null,
            COLUMN_HALF + ", " + COLUMN_TIME + " ASC"
        );

        ArrayList<ReactionsBean>  dataList = manageCursor(cursor);
        cursor.close();
        return dataList;
    }

    public ArrayList<ReactionsBean> selectAllForTrim(String matchId) {
        initDBHelper();
        String getAllDetails = "SELECT * FROM " + TABLE_MATCH_REACTIONS + " where " + COLUMN_VIDEO_PATH + " = ? AND " + COLUMN_MATCH_ID + " = ? ORDER BY half, time ASC";
        Cursor cursor = mDatabase.rawQuery(getAllDetails, new String[]{
                "",
                matchId
        });
        ArrayList<ReactionsBean> dataList = manageCursor(cursor);
        closeCursor(cursor);

        return dataList;
    }

    public ArrayList<ReactionsBean> selectAllUploaded(@Nullable String match_id, String half, int type) {
        initDBHelper();

        Cursor cursor;
        ArrayList<ReactionsBean> dataList;

        if (match_id == null) {
            cursor = mDatabase.query(TABLE_MATCH_REACTIONS, new String[] {"*"}, null, null, null, null,  COLUMN_KEY_ID + " ASC" + ", " + COLUMN_MATCH_ID + " DESC");
        } else {
            String selection = COLUMN_MATCH_ID + " = ?"; // "1 = 1 AND " +
            String[] selectionArgs = {match_id};

            cursor = mDatabase.query(TABLE_MATCH_REACTIONS, new String[] {"*"}, selection, selectionArgs, null, null,  COLUMN_KEY_ID + " ASC" + ", " + COLUMN_MATCH_ID + " DESC");
        }

        dataList = manageCursor(cursor);

        cursor.close();
        return dataList;
    }

    public int getTotalCount(String match_id) {
        initDBHelper();

        String selection = COLUMN_MATCH_ID + " = ?";
        String[] selectionArgs = {match_id};

        Cursor cursor = mDatabase.query(TABLE_MATCH_REACTIONS, new String[] {"*"}, selection, selectionArgs, "", "", "");

        int count = cursor.getCount();
        cursor.close();

        return count;
    }

    @SuppressLint("Range")
    protected ReactionsBean cursorToData(Cursor cursor) {
        ReactionsBean model = new ReactionsBean();
        model.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_KEY_ID)));
        model.setMatch_id(cursor.getInt(cursor.getColumnIndex(COLUMN_MATCH_ID)));
        model.setTeam_id(cursor.getInt(cursor.getColumnIndex(COLUMN_TEAM_ID)));
        model.setHalf(cursor.getInt(cursor.getColumnIndex(COLUMN_HALF)));
        model.setTime(cursor.getString(cursor.getColumnIndex(COLUMN_TIME)));
        model.setTimestamp(cursor.getLong(cursor.getColumnIndex(COLUMN_TIMESTAMP)));
        model.setReaction(cursor.getString(cursor.getColumnIndex(COLUMN_REACTION)));
        model.setFile_name(cursor.getString(cursor.getColumnIndex(COLUMN_VIDEO_NAME)));
        model.setVideo(cursor.getString(cursor.getColumnIndex(COLUMN_VIDEO_PATH)));
        model.setUpload_status(cursor.getInt(cursor.getColumnIndex(COLUMN_STATUS)));
        model.setCreated_date(cursor.getString(cursor.getColumnIndex(COLUMN_CREATED_DATE)));
        model.setTeam_name(cursor.getString(cursor.getColumnIndex(COLUMN_TEAM_NAME)));
        return model;
    }

    public void updateVideo(String video_name, String video_path, int id) {
        initDBHelper();

        ContentValues values = new ContentValues();
        values.put(COLUMN_VIDEO_NAME, video_name);
        values.put(COLUMN_VIDEO_PATH, video_path);
        values.put(COLUMN_STATUS, (CommonMethods.isValidString(video_path)) ? 1 : 0);

        String selection = COLUMN_KEY_ID + " = ?";
        String[] selectionArgs = { String.valueOf(id) };

        mDatabase.update(TABLE_MATCH_REACTIONS, values, selection, selectionArgs);
    }

    public void updateAction(String action, int id) {
        initDBHelper();

        ContentValues values = new ContentValues();
        values.put(COLUMN_REACTION, action);

        String selection = COLUMN_KEY_ID + " = ?";
        String[] selectionArgs = { String.valueOf(id) };

        mDatabase.update(TABLE_MATCH_REACTIONS, values, selection, selectionArgs);
    }

    protected void closeCursor(Cursor cursor) {
        if (cursor != null) {
            cursor.close();
        }
    }

    public int getLastInsertedId() {
        initDBHelper();
        String countQuery = "SELECT  max(id) FROM " + TABLE_MATCH_REACTIONS;
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

    protected ArrayList<ReactionsBean> manageCursor(Cursor cursor) {
        ArrayList<ReactionsBean> dataList = new ArrayList<ReactionsBean>();
        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                ReactionsBean singleModel = cursorToData(cursor);
                if (singleModel != null) {
                    dataList.add(singleModel);
                }
                cursor.moveToNext();
            }
        }
        return dataList;
    }
}
