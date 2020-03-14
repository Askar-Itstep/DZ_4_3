package com.example.dz_4_3.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;

import java.io.Serializable;
import java.sql.Date;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(foreignKeys = @ForeignKey(entity = Genre.class, parentColumns = "id", childColumns =
        "id_genre", onDelete = CASCADE, onUpdate = CASCADE))
@TypeConverters(DateConvert.class)
public class Film extends DBEntity implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private Long id;
    private String title;
    private Date date;
    @ColumnInfo(name = "id_genre")
    private int idGenre;
    private String pathImage;

    @Ignore
    public Film() {}

    public Film(Long id, String title, Date date, int idGenre) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.idGenre = idGenre;
    }
    @Ignore
    public Film(Long id, String title, Date date, String pathImage) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.pathImage = pathImage;
    }
    @Ignore
    public Film(String newFilmTitle, Date newFilmDate) {
        this.title = newFilmTitle;
        this.date = newFilmDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getIdGenre() {
        return idGenre;
    }

    public void setIdGenre(int idGenre) {
        this.idGenre = idGenre;
    }

    public String getPathImage() {
        return pathImage;
    }

    public void setPathImage(String pathImage) {
        this.pathImage = pathImage;
    }
}

