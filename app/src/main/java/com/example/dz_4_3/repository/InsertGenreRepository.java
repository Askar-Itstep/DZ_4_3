package com.example.dz_4_3.repository;

import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.example.dz_4_3.DBCallback;
import com.example.dz_4_3.dao.FilmDao;
import com.example.dz_4_3.dao.GenreDao;
import com.example.dz_4_3.entity.Film;
import com.example.dz_4_3.entity.Genre;

import java.util.List;

public class InsertGenreRepository extends AsyncTask <Object, Void, Void>{
    private GenreDao genreDao;
    private DBCallback callback;

    public InsertGenreRepository(GenreDao genreDao, DBCallback callback) {
        this.genreDao = genreDao;
        this.callback = callback;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected Void doInBackground(Object... objects) {
        if(objects[0] instanceof List){
            List<Genre> genres = (List<Genre>) objects[0];
            genres.forEach(g->genreDao.save(g));
        }
        else genreDao.save((Genre) objects[0]);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
//        super.onPostExecute(aVoid);
        callback.onSave();
    }
}
