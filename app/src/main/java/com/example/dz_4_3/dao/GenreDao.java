package com.example.dz_4_3.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.example.dz_4_3.entity.Genre;

import java.util.List;

@Dao
public interface GenreDao {
    @Query("select * from genre")
    List<Genre> getAllGenre();

    @Query("select * from genre where id = :id")
    Genre getGenreById(Long id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void save(Genre genre);

    @Update
    void update (Genre genre);

    @Delete
    void delete(Genre genre);
}


