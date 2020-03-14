package com.example.dz_4_3.repository;

import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.example.dz_4_3.DBCallback;
import com.example.dz_4_3.dao.FilmDao;
import com.example.dz_4_3.entity.Film;

import java.util.List;

public class InsertFilmRepository extends AsyncTask<Object, Void, Void> {
    private FilmDao filmDao;
    private DBCallback callback;

    public InsertFilmRepository(FilmDao filmDao, DBCallback callback) {
        this.filmDao = filmDao;
        this.callback = callback;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected Void doInBackground(Object... objects) {
        if(objects[0] instanceof List){
            List<Film> films = (List<Film>) objects[0];
            films.forEach(f->filmDao.save(f));
        }
        else filmDao.save((Film) objects[0]);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
//        super.onPostExecute(aVoid);
        callback.onSave();
    }
}
