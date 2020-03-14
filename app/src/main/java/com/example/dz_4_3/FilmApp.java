package com.example.dz_4_3;

import android.annotation.SuppressLint;
import android.app.Application;
import android.arch.persistence.room.Room;
import android.util.Log;

import com.example.dz_4_3.dao.FilmDao;

public class FilmApp extends Application {
    private static final String DB_NAME = "film_db";
    private static FilmApp instance;
    private FilmDB db;
    private static final String TAG = "===FilmApp===";

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        db = Room.databaseBuilder(this, FilmDB.class, DB_NAME).allowMainThreadQueries().build();
    }

    public static FilmApp getInstance(){
        if(instance == null)
            Log.d(TAG, "instance IS NULL!");
        else  Log.d(TAG, "instance IS GOOD!");
        return instance;
    }

    public FilmDB getDb(){
        return db;
    }
}
