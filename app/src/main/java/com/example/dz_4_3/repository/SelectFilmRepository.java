package com.example.dz_4_3.repository;

import android.os.AsyncTask;

import com.example.dz_4_3.DBCallback;
import com.example.dz_4_3.dao.FilmDao;
import com.example.dz_4_3.entity.Film;

import java.util.List;

//производит выборку из табл. Book
//есть ссылка в MainActivity
public class SelectFilmRepository extends AsyncTask<Long, Void, Object> {
    private FilmDao dao;
    private DBCallback callback;

    public SelectFilmRepository(FilmDao dao, DBCallback callback) {
        this.dao = dao;
        this.callback = callback;
    }

    @Override
    protected Object doInBackground(Long... longs) {
        if (longs != null && longs.length > 0){
            return dao.getFilmById(longs[0]);
        } else {
            return dao.getAllFilm();
        }
    }

    @Override
    protected void onPostExecute(Object o) {
//        super.onPostExecute(o);
        if (o instanceof List){
            callback.onSelectCollection((List<Film>) o);
        } else {
            callback.onSelectSingleItem((Film)o);
        }
    }
}
