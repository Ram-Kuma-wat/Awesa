package com.codersworld.awesalibs.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.codersworld.awesalibs.database.dao.ExtraTimeDAO;
import com.codersworld.awesalibs.database.dao.GamesCategoryDAO;
import com.codersworld.awesalibs.database.dao.InterviewsDAO;
import com.codersworld.awesalibs.database.dao.MatchActionsDAO;
import com.codersworld.awesalibs.database.dao.VideoMasterDAO;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "awesa_db_2024.db";
    public static final int DATABASE_VERSION = 6;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // create all tables
        sqLiteDatabase.execSQL(VideoMasterDAO.getCreateTableVideoMaster());
        sqLiteDatabase.execSQL(GamesCategoryDAO.getCreateTable());
        sqLiteDatabase.execSQL(MatchActionsDAO.getCreateTable());
        sqLiteDatabase.execSQL(InterviewsDAO.getCreateTable());
    }

    public void drop(SQLiteDatabase sqLiteDatabase) {
        // drop all tables
        sqLiteDatabase.execSQL(VideoMasterDAO.getDropTableVideoMaster());
        sqLiteDatabase.execSQL(GamesCategoryDAO.getDropTable());
        sqLiteDatabase.execSQL(MatchActionsDAO.getDropTable());
        sqLiteDatabase.execSQL(InterviewsDAO.getDropTable());
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        Log.e("oldVersion : ",oldVersion+"");
        if (oldVersion ==4) {
            sqLiteDatabase.execSQL("ALTER TABLE match_reactions ADD COLUMN upload_type INT");
            sqLiteDatabase.execSQL("ALTER TABLE interviews ADD COLUMN upload_type INT");
        }else if (oldVersion ==5) {
            sqLiteDatabase.execSQL("ALTER TABLE match_reactions ADD COLUMN game_category INT");
            sqLiteDatabase.execSQL("ALTER TABLE interviews ADD COLUMN game_category INT");
        }
    }
}
