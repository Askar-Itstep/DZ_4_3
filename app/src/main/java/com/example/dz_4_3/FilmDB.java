package com.example.dz_4_3;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import com.example.dz_4_3.dao.FilmDao;
import com.example.dz_4_3.dao.GenreDao;
import com.example.dz_4_3.entity.Film;
import com.example.dz_4_3.entity.Genre;

@Database(entities = {Film.class, Genre.class}, version = 1)
public abstract class FilmDB extends RoomDatabase {
    abstract FilmDao getFilmDao();
    abstract GenreDao getGenreDao();
}
