package com.example.dz_4_3.repository;

import android.os.AsyncTask;

import com.example.dz_4_3.DBCallback;
import com.example.dz_4_3.dao.GenreDao;
import com.example.dz_4_3.entity.Genre;

import java.util.List;


//производит выборку из табл. Book
//есть ссылка в MainActivity
public class SelectGenreRepository extends AsyncTask<Long, Void, Object> {
    private GenreDao dao;
    private DBCallback callback;

    public SelectGenreRepository(GenreDao dao, DBCallback callback) {
        this.dao = dao;
        this.callback = callback;
    }

    @Override
    protected Object doInBackground(Long... longs) {
        if (longs != null && longs.length > 0){
            return dao.getGenreById(longs[0]);
        } else {
            return dao.getAllGenre();
        }
    }

    @Override
    protected void onPostExecute(Object o) {
        if (o instanceof List){
            callback.onSelectCollection((List<Genre>) o);
        } else {
            callback.onSelectSingleItem((Genre)o);
        }
    }
}
