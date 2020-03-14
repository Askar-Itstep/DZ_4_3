package com.example.dz_4_3.repository;

import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.example.dz_4_3.DBCallback;
import com.example.dz_4_3.dao.FilmDao;
import com.example.dz_4_3.dao.GenreDao;
import com.example.dz_4_3.entity.Film;
import com.example.dz_4_3.entity.Genre;

import java.util.List;

public class UpdateFilmRepository extends AsyncTask<Object, Void, Void> {
    private FilmDao filmDao;
    private DBCallback callback;
    private static String TAG = "===UpdateFilmRepository===";

    public UpdateFilmRepository(FilmDao filmDao, DBCallback callback) {
        this.filmDao = filmDao;
        this.callback = callback;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected Void doInBackground(Object... objects) {
        if(objects[0] instanceof List){
            List<Film> films = (List<Film>) objects[0];
            films.forEach(f->filmDao.update(f));
        }
        else filmDao.update((Film) objects[0]);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {

        callback.onUpdate();
    }
}
