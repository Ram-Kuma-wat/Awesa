package com.codersworld.awesalibs.database.dao;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.codersworld.awesalibs.beans.game.GameBean;

import java.util.ArrayList;


public class GamesCategoryDAO {

    private static final String TABLE_GAME_CATEGORY = "game_category";

    // Contacts Table Columns names
    private static final String COLUMN_KEY_ID = "_id";
    private static final String COLUMN_GAME_CAT_ID = "id";
    private static final String COLUMN_NAME = "title";
    private static final String COLUMN_IMAGE = "image";
    private static final String COLUMN_COUNTY = "county";
    private static final String COLUMN_COUNTY_ID = "county_id";

    private SQLiteDatabase mDatabase;
    private Context mContext;

    public GamesCategoryDAO(SQLiteDatabase database, Context context) {
        mDatabase = database;
        mContext = context;
    }

    public static String getCreateTable() {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_GAME_CATEGORY
                + "("
                + COLUMN_KEY_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_GAME_CAT_ID + " INT ,"
                + COLUMN_NAME + " TEXT ,"
                + COLUMN_IMAGE + " TEXT ,"
                + COLUMN_COUNTY + " INT ,"
                + COLUMN_COUNTY_ID + " INT)";

        return CREATE_TABLE;
    }

    public static String getDropTable() {
        return "DROP TABLE IF EXISTS " + TABLE_GAME_CATEGORY;
    }

    public void deleteAll() {
        String delete_all = " DELETE "
                + " FROM "
                + TABLE_GAME_CATEGORY;
        mDatabase.execSQL(delete_all);
    }

    public void insert(ArrayList<GameBean.InfoBean> arrayList) {
        for (GameBean.InfoBean singleInput : arrayList) {
            String[] bindArgs = {
                    singleInput.getId()+"",
                    singleInput.getTitle(),
                    singleInput.getImage(),
                    singleInput.getCounty()+"",
                    singleInput.getCounty_id()+"",
            };

            String insertUser = " INSERT INTO "
                    + TABLE_GAME_CATEGORY
                    + " ( "
                    + COLUMN_GAME_CAT_ID
                    + " , "
                    + COLUMN_NAME
                    + " , "
                    + COLUMN_IMAGE
                    + " , "
                    + COLUMN_COUNTY
                    + " , "
                    + COLUMN_COUNTY_ID
                    + " ) "
                    + " VALUES "
                    + " (?,?,?,?,?)";
            mDatabase.execSQL(insertUser, bindArgs);
        }
    }
    public int getRowCount(){
        int count=0;
        Cursor cursor = mDatabase.rawQuery("SELECT COUNT(*) FROM "+TABLE_GAME_CATEGORY,null);
        if(cursor.moveToNext()){
            count = cursor.getInt(0);
        }
        closeCursor(cursor);
        return count;
    }

    public ArrayList<GameBean.InfoBean> selectAll() {
        String getAllDetails = " SELECT "
                + " * "
                + " FROM "
                + TABLE_GAME_CATEGORY+" order by _id DESC";
        Cursor cursor = mDatabase.rawQuery(getAllDetails, null);
        ArrayList<GameBean.InfoBean> dataList = manageCursor(cursor);
        closeCursor(cursor);
        return dataList;
    }


    @SuppressLint("Range")
    protected GameBean.InfoBean cursorToData(Cursor cursor) {
        GameBean.InfoBean model = new GameBean.InfoBean();
        model.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_KEY_ID)));
        model.setTitle(cursor.getString(cursor.getColumnIndex(COLUMN_NAME)));
        model.setImage(cursor.getString(cursor.getColumnIndex(COLUMN_IMAGE)));
        model.setCounty(cursor.getInt(cursor.getColumnIndex(COLUMN_COUNTY)));
        model.setCounty_id(cursor.getInt(cursor.getColumnIndex(COLUMN_COUNTY_ID)));
        return model;
    }

    protected void closeCursor(Cursor cursor) {
        if (cursor != null) {
            cursor.close();
        }
    }

    protected ArrayList<GameBean.InfoBean> manageCursor(Cursor cursor) {
        ArrayList<GameBean.InfoBean> dataList = new ArrayList<GameBean.InfoBean>();
        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                GameBean.InfoBean singleModel = cursorToData(cursor);
                if (singleModel != null) {
                    dataList.add(singleModel);
                }
                cursor.moveToNext();
            }
        }
        return dataList;
    }
}
