package com.codersworld.awesalibs.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.codersworld.awesalibs.database.dao.DBVideoUplaodDao;
import com.codersworld.awesalibs.database.dao.GamesCategoryDAO;
import com.codersworld.awesalibs.database.dao.InterviewsDAO;
import com.codersworld.awesalibs.database.dao.MatchActionsDAO;
import com.codersworld.awesalibs.database.dao.VideoMasterDAO;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "awesa_db_2024.db";
    public static final int DATABASE_VERSION = 1;
    private Context mContext;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // create all tables
        sqLiteDatabase.execSQL(VideoMasterDAO.getCreateTableVideoMaster());
        sqLiteDatabase.execSQL(GamesCategoryDAO.getCreateTable());
        sqLiteDatabase.execSQL(MatchActionsDAO.getCreateTable());
        sqLiteDatabase.execSQL(InterviewsDAO.getCreateTable());
      /*  sqLiteDatabase.execSQL(BusRoutesDao.getCreateTableHindi());
        sqLiteDatabase.execSQL(PlaceCategoryDao.getCreateTable());
        sqLiteDatabase.execSQL(PlacesDao.getCreateTable());*/
    }

    public void drop(SQLiteDatabase sqLiteDatabase) {
        // drop all tables
        sqLiteDatabase.execSQL(VideoMasterDAO.getDropTableVideoMaster());
        sqLiteDatabase.execSQL(GamesCategoryDAO.getDropTable());
        sqLiteDatabase.execSQL(MatchActionsDAO.getDropTable());
        sqLiteDatabase.execSQL(InterviewsDAO.getDropTable());
        /*
        sqLiteDatabase.execSQL(BusRoutesDao.getDropTableHindi());
        sqLiteDatabase.execSQL(PlaceCategoryDao.getDropTable());
        sqLiteDatabase.execSQL(PlacesDao.getDropTable());*/
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        if (newVersion >= oldVersion) {
            drop(sqLiteDatabase);
            onCreate(sqLiteDatabase);
        }
    }
}
