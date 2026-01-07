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

import com.codersworld.awesalibs.beans.matches.InterviewBean;
 import com.codersworld.awesalibs.database.DatabaseHelper;
import com.codersworld.awesalibs.utils.CommonMethods;

import java.util.ArrayList;

public class InterviewsDAO {

    private static final String TAG = InterviewsDAO.class.getSimpleName();
    private static final String TABLE_INTERVIEWS = "interviews";

    // Contacts Table Columns names
    private static final String COLUMN_KEY_ID = "id";
    private static final String COLUMN_MATCH_ID = "match_id";
    private static final String COLUMN_VIDEO_NAME = "video_name";
    private static final String COLUMN_VIDEO_PATH = "video_path";
    private static final String COLUMN_CREATED_DATE = "created_date";
    private static final String COLUMN_STATUS = "upload_status";
    private static final String COLUMN_UPLOAD_TYPE = "upload_type";
    private static final String COLUMN_GAME_CATEGORY = "game_category";

    private SQLiteDatabase mDatabase;
    private final Context mContext;

    public InterviewsDAO(SQLiteDatabase database, Context context) {
        mDatabase = database;
        mContext = context;
    }

    public static String getCreateTable() {
        return "CREATE TABLE " + TABLE_INTERVIEWS
                + "("
                + COLUMN_KEY_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_MATCH_ID + " INT ,"
                + COLUMN_VIDEO_NAME + " TEXT ,"
                + COLUMN_VIDEO_PATH + " TEXT ,"
                + COLUMN_STATUS + " INT ,"
                + COLUMN_CREATED_DATE + " TEXT,"
                + COLUMN_UPLOAD_TYPE + " INT,"
                + COLUMN_GAME_CATEGORY + " INT)";
    }

    public static String getDropTable() {
        return "DROP TABLE IF EXISTS " + TABLE_INTERVIEWS;
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

        mDatabase.delete(TABLE_INTERVIEWS, "id = ?", selectionArgs);
    }
    public void insert(String...param) {
        initDBHelper();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_MATCH_ID, param[0]);
        contentValues.put(COLUMN_VIDEO_NAME, param[1]);
        contentValues.put(COLUMN_VIDEO_PATH, param[2]);
        contentValues.put(COLUMN_STATUS, param[3]);
        contentValues.put(COLUMN_CREATED_DATE, param[4]);
        contentValues.put(COLUMN_UPLOAD_TYPE, 0);
        contentValues.put(COLUMN_GAME_CATEGORY, param[5]);

        mDatabase.insert(TABLE_INTERVIEWS, null, contentValues);
    }

    public int getRowCount( String match_id) {
        initDBHelper();

        String selection = COLUMN_MATCH_ID + " = ?"; // "1 = 1 AND " +
        String[] selectionArgs = {match_id};

        Cursor cursor = mDatabase.query(TABLE_INTERVIEWS, new String[] {"*"}, selection, selectionArgs, "", "", "");

        int count = cursor.getCount(); // getInt(0);
        cursor.close();

        return count;
    }

    public ArrayList<InterviewBean> selectAll(String match_id) {
        initDBHelper();
        String getAllDetails = " SELECT * FROM " + TABLE_INTERVIEWS + " where 1 = 1 " + ((CommonMethods.isValidString(match_id)) ? " AND match_id=" + match_id : "") +" order by id DESC";
        Cursor cursor = mDatabase.rawQuery(getAllDetails, null);
        ArrayList<InterviewBean> dataList = manageCursor(cursor);
        closeCursor(cursor);
        return dataList;
    }

    public ArrayList<InterviewBean> selectAllUploaded(@Nullable String match_id, int type) {
        initDBHelper();

        Cursor cursor;
        ArrayList<InterviewBean> dataList;

        if (match_id == null) {
            cursor = mDatabase.query(TABLE_INTERVIEWS, new String[] {"*"}, null, null, null, null,  COLUMN_KEY_ID + " DESC");
        } else {
            String selection = COLUMN_MATCH_ID + " = ?"; // "1 = 1 AND " +
            String[] selectionArgs = {match_id};

            cursor = mDatabase.query(TABLE_INTERVIEWS, new String[] {"*"}, selection, selectionArgs, null, null,  COLUMN_KEY_ID + " DESC");
        }

        dataList = manageCursor(cursor);
        if (type==2){
            try{
                dataList.removeIf(bean -> (bean.getUpload_type() == 1 || CommonMethods.getTimeDifferenceInHours(bean.getCreated_date(),"yyyy/MM/dd HH:mm:ss") > 0.5));
            }catch (Exception e){
                dataList.removeIf(bean -> (CommonMethods.getTimeDifferenceInHours(bean.getCreated_date(),"yyyy/MM/dd HH:mm:ss") > 0.5));
            }
        }

        cursor.close();
        return dataList;
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
        model.setUpload_type(cursor.getInt(cursor.getColumnIndex(COLUMN_UPLOAD_TYPE)));
        model.setGame_category(cursor.getInt(cursor.getColumnIndex(COLUMN_GAME_CATEGORY)));
        return model;
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
    public void updateUploadType(String upload_type, String matchId) {
        initDBHelper();

        ContentValues values = new ContentValues();
        values.put(COLUMN_UPLOAD_TYPE, upload_type);

        String selection = COLUMN_MATCH_ID + " = ?";
        String[] selectionArgs = { matchId };

        mDatabase.update(TABLE_INTERVIEWS, values, selection, selectionArgs);
    }
}
