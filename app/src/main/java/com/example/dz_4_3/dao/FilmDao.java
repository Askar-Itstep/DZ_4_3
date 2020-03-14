package com.example.dz_4_3.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.example.dz_4_3.entity.Film;

import java.util.List;

@Dao
public interface FilmDao {
    @Query("select * from film")
    List<Film> getAllFilm();

    @Query("select * from film where id = :id")
    Film getFilmById(Long id);  //получить фильм по номеру  (исполь-ся в SelectFilmRepository)

    @Query("select * from film where id_genre = :id")
    List<Film> getFilmByIdGenre(Long id);               //получить фильмы по номеру жанра (исполь-ся в SelectFilmRepository???)

    @Query("select f.id, f.date, f.id_genre, f.title, f.pathImage from " +
            "film as f, genre g where f.id_genre = g.id and g.title = :genre")
    List<Film> getFilmByGenre(String genre);                            //получить фильмы по жанру (исполь-ся в SelectFilmRepository???)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void save(Film film);                   //сохран. филь м в БД (исполь-ся в InsertFilmRepository)

    @Update
    void update (Film film);   // cоздать UpdateFilmRepository???

    @Delete
    void delete(Film film);     // DeleteFilmRepository???
}
