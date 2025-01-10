package com.codersworld.awesalibs.database.dao;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.codersworld.awesalibs.beans.matches.InterviewBean;
import com.codersworld.awesalibs.database.DatabaseHelper;
import com.codersworld.awesalibs.utils.CommonMethods;

import java.util.ArrayList;

public class ExtraTimeDAO {

    private static final String TAG = ExtraTimeDAO.class.getSimpleName();
    private static final String TABLE_EXTRA_TIME = "extratime";

    // Contacts Table Columns names
    private static final String COLUMN_KEY_ID = "id";
    private static final String COLUMN_MATCH_ID = "match_id";
    private static final String COLUMN_VIDEO_NAME = "video_name";
    private static final String COLUMN_VIDEO_PATH = "video_path";
    private static final String COLUMN_CREATED_DATE = "created_date";
    private static final String COLUMN_STATUS = "upload_status";

    private SQLiteDatabase mDatabase;
    private final Context mContext;

    public ExtraTimeDAO(SQLiteDatabase database, Context context) {
        mDatabase = database;
        mContext = context;
    }

    public static String getCreateTable() {
        return "CREATE TABLE " + TABLE_EXTRA_TIME
                + "("
                + COLUMN_KEY_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_MATCH_ID + " INT ,"
                + COLUMN_VIDEO_NAME + " TEXT ,"
                + COLUMN_VIDEO_PATH + " TEXT ,"
                + COLUMN_STATUS + " INT ,"
                + COLUMN_CREATED_DATE + " TEXT)";
    }

    public static String getDropTable() {
        return "DROP TABLE IF EXISTS " + TABLE_EXTRA_TIME;
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

        mDatabase.delete(TABLE_EXTRA_TIME, "id = ?", selectionArgs);
    }

    public void deleteUploadedVideos() {
        initDBHelper();

        String[] selectionArgs = { String.valueOf(1) };
        mDatabase.delete(TABLE_EXTRA_TIME, "upload_status = ?", selectionArgs);
    }


    public void insert(String...param) {
        initDBHelper();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_MATCH_ID, param[0]);
        contentValues.put(COLUMN_VIDEO_NAME, param[1]);
        contentValues.put(COLUMN_VIDEO_PATH, param[2]);
        contentValues.put(COLUMN_STATUS, param[3]);
        contentValues.put(COLUMN_CREATED_DATE, param[4]);

        mDatabase.insert(TABLE_EXTRA_TIME, null, contentValues);
    }

    public int getRowCount( String match_id) {
        initDBHelper();

        String selection = COLUMN_MATCH_ID + " = ?"; // "1 = 1 AND " +
        String[] selectionArgs = {match_id};

        Cursor cursor = mDatabase.query(TABLE_EXTRA_TIME, new String[] {"*"}, selection, selectionArgs, "", "", "");

        int count = cursor.getCount(); // getInt(0);
        cursor.close();

        return count;
    }

    public ArrayList<InterviewBean> selectAll(String match_id) {
        initDBHelper();
        String getAllDetails = " SELECT * FROM " + TABLE_EXTRA_TIME + " where 1 = 1 " + ((CommonMethods.isValidString(match_id)) ? " AND match_id=" + match_id : "") +" order by id DESC";
        Cursor cursor = mDatabase.rawQuery(getAllDetails, null);
        ArrayList<InterviewBean> dataList = manageCursor(cursor);
        closeCursor(cursor);
        return dataList;
    }

    public ArrayList<InterviewBean> selectAllUploaded(@Nullable String match_id) {
        initDBHelper();

        Cursor cursor;
        ArrayList<InterviewBean> dataList;

        if (match_id == null) {
            cursor = mDatabase.query(TABLE_EXTRA_TIME, new String[] {"*"}, null, null, null, null,  COLUMN_KEY_ID + " DESC");
        } else {
            String selection = COLUMN_MATCH_ID + " = ?"; // "1 = 1 AND " +
            String[] selectionArgs = {match_id};

            cursor = mDatabase.query(TABLE_EXTRA_TIME, new String[] {"*"}, selection, selectionArgs, null, null,  COLUMN_KEY_ID + " DESC");
        }

        dataList = manageCursor(cursor);

        cursor.close();
        return dataList;
    }

    public ArrayList<InterviewBean> selectSingle(int counter) {
        initDBHelper();
        try {
            String getAllDetails = " SELECT * FROM " + TABLE_EXTRA_TIME + " where upload_status = 0 order by id DESC LIMIT 1";
            Cursor cursor = mDatabase.rawQuery(getAllDetails, null);
            ArrayList<InterviewBean> dataList = manageCursor(cursor);
            closeCursor(cursor);
            return dataList;
        } catch (SQLiteException e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
            return (counter == 0) ? selectSingle(1) : new ArrayList<>();
        }
    }

    @SuppressLint("Range")
    protected InterviewBean cursorToData(Cursor cursor) {
        InterviewBean model = new InterviewBean();
        model.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_KEY_ID)));
        model.setMatch_id(cursor.getInt(cursor.getColumnIndex(COLUMN_MATCH_ID)));
        model.setFile_name(cursor.getString(cursor.getColumnIndex(COLUMN_VIDEO_NAME)));
        model.setVideo(cursor.getString(cursor.getColumnIndex(COLUMN_VIDEO_PATH)));
        model.setUpload_status(cursor.getInt(cursor.getColumnIndex(COLUMN_STATUS)));
        model.setCreated_date(cursor.getString(cursor.getColumnIndex(COLUMN_CREATED_DATE)));
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

        mDatabase.update(TABLE_EXTRA_TIME, values, selection, selectionArgs);
    }

    protected void closeCursor(Cursor cursor) {
        if (cursor != null) {
            cursor.close();
        }
    }

    protected ArrayList<InterviewBean> manageCursor(Cursor cursor) {
        ArrayList<InterviewBean> dataList = new ArrayList<InterviewBean>();
        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                InterviewBean singleModel = cursorToData(cursor);
                if (singleModel != null) {
                    dataList.add(singleModel);
                }
                cursor.moveToNext();
            }
        }
        return dataList;
    }
}
