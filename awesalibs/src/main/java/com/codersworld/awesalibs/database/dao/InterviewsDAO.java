package com.codersworld.awesalibs.database.dao;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.codersworld.awesalibs.beans.matches.InterviewBean;
 import com.codersworld.awesalibs.database.DatabaseHelper;
import com.codersworld.awesalibs.utils.CommonMethods;

import java.util.ArrayList;

public class InterviewsDAO {

    private static final String TABLE_INTERVIEWS = "interviews";

    // Contacts Table Columns names
    private static final String COLUMN_KEY_ID = "id";
    private static final String COLUMN_MATCH_ID = "match_id";
    private static final String COLUMN_VIDEO_NAME = "video_name";
    private static final String COLUMN_VIDEO_PATH = "video_path";
    private static final String COLUMN_CREATED_DATE = "created_date";
    private static final String COLUMN_STATUS = "upload_status";

    private SQLiteDatabase mDatabase;
    private Context mContext;

    public InterviewsDAO(SQLiteDatabase database, Context context) {
        mDatabase = database;
        mContext = context;
    }

    public static String getCreateTable() {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_INTERVIEWS
                + "("
                + COLUMN_KEY_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_MATCH_ID + " INT ,"
                + COLUMN_VIDEO_NAME + " TEXT ,"
                + COLUMN_VIDEO_PATH + " TEXT ,"
                + COLUMN_STATUS + " INT ,"
                + COLUMN_CREATED_DATE + " TEXT)";

        return CREATE_TABLE;
    }

    public static String getDropTable() {
        return "DROP TABLE IF EXISTS " + TABLE_INTERVIEWS;
    }

    public void initDBHelper() {
        try {
            DatabaseHelper mHelper = new DatabaseHelper(mContext);
            mDatabase = mHelper.getWritableDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteAll(String id, int counter) {
        initDBHelper();
        try {
            String delete_all = " DELETE " + " FROM " + TABLE_INTERVIEWS;
            if (CommonMethods.isValidString(id)) {
                delete_all += " where id =" + id;
            }
            mDatabase.execSQL(delete_all);
        } catch (Exception e) {
            e.printStackTrace();
            if (counter == 0) {
                deleteAll(id, 1);
            }
        }
    }

    public void deleteUploadedVideos() {
        initDBHelper();
        try {
            String delete_all = " DELETE " + " FROM " + TABLE_INTERVIEWS + " where upload_status==1";
            mDatabase.execSQL(delete_all);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void insert(String...param) {
        initDBHelper();
             String[] bindArgs = {
                     param[0],
                     param[1],
                     param[2],
                     param[3],
                     param[4]
            };

            String insertUser = " INSERT INTO "
                    + TABLE_INTERVIEWS
                    + " ( "
                    + COLUMN_MATCH_ID
                    + " , "
                    + COLUMN_VIDEO_NAME
                    + " , "
                    + COLUMN_VIDEO_PATH
                    + " , "
                    + COLUMN_STATUS
                    + " , "
                    + COLUMN_CREATED_DATE
                    + " ) "
                    + " VALUES "
                    + " (?,?,?,?,?)";
            mDatabase.execSQL(insertUser, bindArgs);
    }

    public int getRowCount( String match_id) {
        initDBHelper();
        int count = 0;
        String query = "SELECT COUNT(*) FROM " + TABLE_INTERVIEWS + " where 1=1";
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

    public ArrayList<InterviewBean> selectAll(String match_id) {
        initDBHelper();
        String getAllDetails = " SELECT * FROM " + TABLE_INTERVIEWS + " where 1 = 1 " + ((CommonMethods.isValidString(match_id)) ? " AND match_id=" + match_id : "") +" order by id DESC";
        //String getAllDetails = " SELECT * FROM " + TABLE_INTERVIEWS + " where upload_status = 0 " + ((CommonMethods.isValidString(match_id)) ? " AND match_id=" + match_id : "") +" order by id DESC";
       // Log.e("interviewquery",getAllDetails);
        Cursor cursor = mDatabase.rawQuery(getAllDetails, null);
        ArrayList<InterviewBean> dataList = manageCursor(cursor);
        closeCursor(cursor);
        return dataList;
    }

    public ArrayList<InterviewBean> selectAllUploaded(String match_id ) {
        initDBHelper();
        String getAllDetails = " SELECT * FROM " + TABLE_INTERVIEWS + " where 1=1 " + ((CommonMethods.isValidString(match_id)) ? " AND match_id=" + match_id : "") + " order by id DESC";
        Cursor cursor = mDatabase.rawQuery(getAllDetails, null);
        ArrayList<InterviewBean> dataList = manageCursor(cursor);
        closeCursor(cursor);
        return dataList;
    }

    public ArrayList<InterviewBean> selectSingle(int counter) {
        initDBHelper();
        try {
            String getAllDetails = " SELECT * FROM " + TABLE_INTERVIEWS + " where upload_status = 0 order by id DESC LIMIT 1";
            Cursor cursor = mDatabase.rawQuery(getAllDetails, null);
            ArrayList<InterviewBean> dataList = manageCursor(cursor);
            closeCursor(cursor);
            return dataList;
        } catch (Exception e) {
            e.printStackTrace();
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
       // Log.e("video_path",video_path);
        String[] bindArgs = {
                String.valueOf(video_name),
                String.valueOf(video_path),
                String.valueOf((CommonMethods.isValidString(video_path)) ? 1 : 0),
                String.valueOf(id)
        };
        String update = " UPDATE "
                + TABLE_INTERVIEWS
                + " SET "
                + COLUMN_VIDEO_NAME
                + " = ?, "
                + COLUMN_VIDEO_PATH
                + " = ?, "
                + COLUMN_STATUS
                + " = ? WHERE " + COLUMN_KEY_ID + "= ?";
        mDatabase.execSQL(update, bindArgs);
    }

    public void updateVideoAll() {
       try {
           String[] bindArgs = {"180"
           };
           String update = " UPDATE "
                   + TABLE_INTERVIEWS
                   + " SET "
                   + COLUMN_MATCH_ID
                   + " = ? WHERE 1=1";
           mDatabase.execSQL(update, bindArgs);
       }catch (Exception e){

       }
    }

    protected void closeCursor(Cursor cursor) {
       try{ if (cursor != null) {
            cursor.close();
        }
        }catch (Exception e){
           e.printStackTrace();
       }
    }

    public int getLastInsertedId() {
        initDBHelper();
        String countQuery = "SELECT  max(id) FROM " + TABLE_INTERVIEWS;
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
        //return cursor.getCount();
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
