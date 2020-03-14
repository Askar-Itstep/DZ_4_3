package com.example.dz_4_3;

import com.example.dz_4_3.entity.DBEntity;

import java.util.List;

public interface DBCallback<T extends DBEntity> {
    void onSelectCollection(List<T> collection);
    void onSelectSingleItem(T item);
    void onSave();
    void onUpdate();
    void onDelete();
}
