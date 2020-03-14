package com.example.dz_4_3.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.io.Serializable;

@Entity
public class Genre extends DBEntity implements Serializable {
    @PrimaryKey
    private Long id;
    private String title;

    public Genre() { }

    public Genre(String title) {
        this.title = title;
    }

    public Genre(Long id, String title) {
        this.id = id;
        this.title = title;
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
}
