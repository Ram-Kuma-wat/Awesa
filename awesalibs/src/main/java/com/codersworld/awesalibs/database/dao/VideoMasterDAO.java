package com.codersworld.awesalibs.database.dao;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.codersworld.awesalibs.database.DatabaseHelper;
import com.codersworld.awesalibs.utils.CommonMethods;

import java.util.ArrayList;


public class VideoMasterDAO {

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
    private Context mContext;

    public VideoMasterDAO(SQLiteDatabase database, Context context) {
        mDatabase = database;
        mContext = context;
    }

    public void updateVideoAll() {
        String[] bindArgs = {
                String.valueOf("0"),
        };
        String update = " UPDATE "
                + TABLE_VIDEO_MASTER
                + " SET "
                + COLUMN_VIDEO_STATUS
                + " = ? WHERE 1=1";
        mDatabase.execSQL(update, bindArgs);
        /* ContentValues values = new ContentValues();
        values.put(COLUMN_VIDEO_STATUS, "0");
        mDatabase.update(TABLE_VIDEO_MASTER, values, "1=?", new String[]{"1"});*/
    }

    public void initDBHelper() {
        try {
            DatabaseHelper mHelper = new DatabaseHelper(mContext);
            mDatabase = mHelper.getWritableDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getCreateTableVideoMaster() {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_VIDEO_MASTER
                + "("
                + COLUMN_KEY_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_VIDEO_NAME + " TEXT ,"
                + COLUMN_MATCH_ID + " TEXT ,"
                + COLUMN_VIDEO_TYPE + " TEXT ,"
                + COLUMN_VIDEO_HALF + " TEXT ,"
                + COLUMN_VIDEO_PATH + " TEXT ,"
                + COLUMN_VIDEO_STATUS + " TEXT ,"
                + COLUMN_DATE + " TEXT)";

        return CREATE_TABLE;
    }

    public static String getDropTableVideoMaster() {
        return "DROP TABLE IF EXISTS " + TABLE_VIDEO_MASTER;
    }

    public void deleteAll() {
        initDBHelper();
        String delete_all = " DELETE "
                + " FROM "
                + TABLE_VIDEO_MASTER;

        mDatabase.execSQL(delete_all);
    }

    public void insert(ArrayList<DBVideoUplaodDao> arrayList) {
        initDBHelper();
        for (DBVideoUplaodDao singleInput : arrayList) {
            String[] bindArgs = {
                    singleInput.getVideo_name(),
                    singleInput.getMatch_id(),
                    String.valueOf(singleInput.getVideo_ext()),
                    String.valueOf(singleInput.getVideo_half()),
                    String.valueOf(singleInput.getVideo_path()),
                    String.valueOf(singleInput.getUpload_status()),
                    String.valueOf(singleInput.getDate()),
            };

            String insertUser = " INSERT INTO "
                    + TABLE_VIDEO_MASTER
                    + " ( "
                    + COLUMN_VIDEO_NAME
                    + " , "
                    + COLUMN_MATCH_ID
                    + " , "
                    + COLUMN_VIDEO_TYPE
                    + " , "
                    + COLUMN_VIDEO_HALF
                    + " , "
                    + COLUMN_VIDEO_PATH
                    + " , "
                    + COLUMN_VIDEO_STATUS
                    + " , "
                    + COLUMN_DATE
                    + " ) "
                    + " VALUES "
                    + " (?,?,?,?,?,?,?)";
            mDatabase.execSQL(insertUser, bindArgs);
        }
    }

    public void updateVideo(int id, int isUploaded) {
        String[] bindArgs = {
                String.valueOf(isUploaded),
                String.valueOf(id)
        };
        String update = " UPDATE "
                + TABLE_VIDEO_MASTER
                + " SET "
                + COLUMN_VIDEO_STATUS
                + " = ? WHERE " + COLUMN_KEY_ID + "= ?";
        mDatabase.execSQL(update, bindArgs);
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
    public boolean deleteVideoById(int id) {
        initDBHelper();
        try {
            String deleteSingleRow = " DELETE "
                    + " FROM "
                    + TABLE_VIDEO_MASTER
                    + " WHERE "
                    + COLUMN_KEY_ID
                    + " = "
                    + id;
            if (getTotalCount(id+"")==0) {
                mDatabase.execSQL(deleteSingleRow);
                return true;
            }else{
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return deleteVideoById(id);
        }
    }

    public void deleteVideoByMatch(int id, int video_half) {
        initDBHelper();
        try {
            String deleteSingleRow = " DELETE "
                    + " FROM "
                    + TABLE_VIDEO_MASTER
                    + " WHERE "
                    + COLUMN_MATCH_ID
                    + " = "
                    + id
                    + " AND video_half = " + video_half;
            if (getTotalCount(id+"")==0) {
                mDatabase.execSQL(deleteSingleRow);
            }
        } catch (Exception e) {
            e.printStackTrace();
            deleteVideoByMatch(id, video_half);
        }
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
        //return cursor.getCount();
    }

    public int getTodoItemCount() {
        initDBHelper();
        String countQuery = "SELECT  * FROM " + TABLE_VIDEO_MASTER;
        Cursor cursor = mDatabase.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        closeCursor(cursor);
        return cnt;
    }

    public ArrayList<DBVideoUplaodDao> selectAll() {
        initDBHelper();
        String getAllDetails = " SELECT " + " * " + " FROM "
                + TABLE_VIDEO_MASTER + " where 1=1 order by _id DESC";
        Cursor cursor = mDatabase.rawQuery(getAllDetails, null);
        ArrayList<DBVideoUplaodDao> dataList = manageCursor(cursor);
        closeCursor(cursor);
        return dataList;
    }

    public ArrayList<DBVideoUplaodDao> selectAll1(String match_id, String half) {
        initDBHelper();
        String getAllDetails = " SELECT " + " * " + " FROM "
                + TABLE_VIDEO_MASTER + " where upload_status = 0 " + ((CommonMethods.isValidString(match_id)) ? " AND match_id=" + match_id : "") + ((CommonMethods.isValidString(half)) ? " AND video_half=" + half : "") + " order by _id ASC LIMIT 1";
        Cursor cursor = mDatabase.rawQuery(getAllDetails, null);
        ArrayList<DBVideoUplaodDao> dataList = manageCursor(cursor);
        closeCursor(cursor);
        return dataList;
    }


    @SuppressLint("Range")
    protected DBVideoUplaodDao cursorToData(Cursor cursor) {
        DBVideoUplaodDao model = new DBVideoUplaodDao();
        model.setmId(cursor.getInt(cursor.getColumnIndex(COLUMN_KEY_ID)));
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

    protected ArrayList<DBVideoUplaodDao> manageCursor(Cursor cursor) {
        ArrayList<DBVideoUplaodDao> dataList = new ArrayList<DBVideoUplaodDao>();
        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                DBVideoUplaodDao singleModel = cursorToData(cursor);
                if (singleModel != null) {
                    dataList.add(singleModel);
                }
                cursor.moveToNext();
            }
        }
        return dataList;
    }
}
