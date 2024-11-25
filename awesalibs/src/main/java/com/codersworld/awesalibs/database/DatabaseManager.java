package com.codersworld.awesalibs.database;


import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.codersworld.awesalibs.listeners.QueryExecutor;
import com.codersworld.awesalibs.utils.Tags;

import java.util.concurrent.atomic.AtomicInteger;

public class DatabaseManager {

    private final AtomicInteger mOpenCounter = new AtomicInteger();
    private static DatabaseManager instance;
    private SQLiteOpenHelper mDatabaseHelper;
    private SQLiteDatabase mDatabase;

    public DatabaseManager(SQLiteOpenHelper helper) {
        mDatabaseHelper = helper;
    }

//    public static synchronized void initializeInstance(SQLiteOpenHelper helper) {
//        if (instance == null) {
//            instance = new DatabaseManager(helper);
//        }
//    }
//
//    public static synchronized DatabaseManager getInstance() {
//        if (instance == null) {
//            throw new IllegalStateException(DatabaseManager.class.getSimpleName() +
//                    " is not initialized, call initializeInstance(..) method first.");
//        }
//        return instance;
//    }

    public synchronized SQLiteDatabase openDatabase() {
        //if (mOpenCounter.incrementAndGet() == 1) {
            // Opening new database
            mDatabase = mDatabaseHelper.getWritableDatabase();
        //}
        Log.d(Tags.TAG,"Database open counter: " + mOpenCounter.get());
        return mDatabase;
    }

    public synchronized void closeDatabase() {
       try {
           //if (mOpenCounter.decrementAndGet() == 0) {
               // Closing database
               mDatabase.close();
           //}
           Log.d(Tags.TAG, "Database close counter: " + mOpenCounter.get());
       }catch (Exception e){
           e.printStackTrace();
       }
    }

    public void executeQuery(QueryExecutor executor) {
       try {
          // Log.e("executeQry","yes");
           SQLiteDatabase database = openDatabase();
           executor.run(database);
           closeDatabase();
       }catch (Exception e){
           e.printStackTrace();
       }
    }
    public void executeQuery1(QueryExecutor executor,String from) {
       try {
           Log.e("executeQry","yes => "+from);
           SQLiteDatabase database = openDatabase();
           executor.run(database);
           closeDatabase();
       }catch (Exception e){
           e.printStackTrace();
       }
    }

    public void executeQueryTask(final QueryExecutor executor) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase database = openDatabase();
                executor.run(database);
                closeDatabase();
            }
        }).start();
    }
}
