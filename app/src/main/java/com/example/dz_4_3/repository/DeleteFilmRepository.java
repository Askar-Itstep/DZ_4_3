package com.example.dz_4_3.repository;

import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.example.dz_4_3.DBCallback;
import com.example.dz_4_3.dao.FilmDao;
import com.example.dz_4_3.entity.Film;

import java.util.Arrays;
import java.util.List;

public class DeleteFilmRepository extends AsyncTask<Object, Void, Void> {
    private FilmDao dao;
    private DBCallback callback;
    private static String TAG = "===DeleteFilmRepository===";

    public DeleteFilmRepository(FilmDao dao, DBCallback callback) {
        this.dao = dao;
        this.callback = callback;
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected Void doInBackground(Object... objects) {
//        if(objects[0] == null) Log.e(TAG, "object IS NULL!");
        if(objects[0] instanceof List){
            List<Film> filmes = (List<Film>) objects[0];
            filmes.forEach(f->dao.delete(f));
        }
        else {
            dao.delete((Film) objects[0]);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        callback.onDelete();
    }
}
