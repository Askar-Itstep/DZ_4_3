package com.example.dz_4_3.components;

import android.graphics.Bitmap;

import java.io.Serializable;

public class MyFile extends Component implements Serializable {

    public MyFile() {}

    public MyFile(Bitmap bmp, String title) {
        super(bmp, title);
    }

    public MyFile(String title, String typeFile) {
        super(title, typeFile);
    }

    public MyFile(Bitmap bmp, String title, String typeFile) {
        super(bmp, title, typeFile);
    }

    @Override
    public void Add(Component ...component) throws Exception {
        throw new Exception("Операция добавления не разрешена");
    }

    @Override
    public void Remove(Component ...component) throws Exception {
        throw new Exception("Операция удаления не разрешена");
    }

    @Override
    public Component getComponent(int index) throws Exception {
        throw new Exception("Вызов компонентов не разрешен");
    }


}
