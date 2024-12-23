package com.codersworld.awesalibs.database;


import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.codersworld.awesalibs.listeners.QueryExecutor;
import com.codersworld.awesalibs.utils.Tags;

import java.util.concurrent.atomic.AtomicInteger;

public class DatabaseManager {
    private static String TAG = DatabaseManager.class.getSimpleName();
    private final AtomicInteger mOpenCounter = new AtomicInteger();
    private static DatabaseManager instance;
    private final SQLiteOpenHelper mDatabaseHelper;
    private SQLiteDatabase mDatabase;

    public DatabaseManager(SQLiteOpenHelper helper) {
        mDatabaseHelper = helper;
    }

    public synchronized SQLiteDatabase openDatabase() {
        mDatabase = mDatabaseHelper.getWritableDatabase();
        Log.d(TAG,"Database open counter: " + mOpenCounter.get());
        return mDatabase;
    }

    public synchronized void closeDatabase() {
       try {
           mDatabase.close();
           Log.d(TAG, "Database close counter: " + mOpenCounter.get());
       } catch (SQLiteException e) {
           Log.e(TAG, e.getLocalizedMessage(), e);
       }
    }

    public void executeQuery(QueryExecutor executor) {
       try {
           SQLiteDatabase database = openDatabase();
           executor.run(database);
           closeDatabase();
       } catch (SQLiteException e) {
           Log.e(TAG, e.getLocalizedMessage(), e);
       }
    }

    public void executeQueryTask(final QueryExecutor executor) {
        new Thread(() -> {
            SQLiteDatabase database = openDatabase();
            executor.run(database);
            closeDatabase();
        }).start();
    }
}
