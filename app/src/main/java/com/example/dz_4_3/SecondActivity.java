package com.example.dz_4_3;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dz_4_3.entity.DBEntity;
import com.example.dz_4_3.entity.Film;
import com.example.dz_4_3.entity.Genre;
import com.example.dz_4_3.repository.InsertFilmRepository;
import com.example.dz_4_3.repository.InsertGenreRepository;
import com.example.dz_4_3.repository.SelectFilmRepository;
import com.example.dz_4_3.repository.SelectGenreRepository;
import com.example.dz_4_3.repository.UpdateFilmRepository;
import com.example.dz_4_3.repository.UpdateGenreRepository;

import java.io.Serializable;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

//----активность по работе с фильмами (измен. и добавлен.) --------------
public class SecondActivity extends AppCompatActivity implements DBCallback<DBEntity>{
    public final static String KEY_TITLE = "key_lastname";
    public final static String KEY_DATE = "key_firstname";
    public final static String KEY_GENRE = "key_genre";
    public final static String KEY_URI = "key_uri";

    public final static String KEY_METHOD_ADD = "key_add";        //разные ключи для добавлен.
    public final static String KEY_METHOD_UPDATE = "key_update";  //.. и измен
    public final static String KEY_METHOD_REVIEW = "key_review";
    public static final int REQUEST_IMG = 1;

    private EditText edTitle;
    private EditText edGenre;
    private EditText edDate;
    private ImageView ivPoster;

    private static String TAG = "===SecondActivity===";
    private static HashMap<String, String> curMap;
    private static List<Film> dbFilms;
    private static List<Genre> dbGenres;
    private static Film curDbFilm;
    private static Genre curDbGenre;
    private static long curId = -1;
    private Intent backData;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        edTitle = findViewById(R.id.edFilmName);
        edDate = findViewById(R.id.edDate);
        edGenre = findViewById(R.id.edGenreFilm);
        ivPoster = findViewById(R.id.ivPoster);
        backData= new Intent(this, MainActivity.class);;
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if(bundle == null) {
            Log.d(TAG, "bundle IS NULL!");
            return;
        }
        dbFilms = (List<Film>) bundle.getSerializable("dbFilms");
        dbGenres = (List<Genre>) bundle.getSerializable("dbGenres");

        if(bundle.containsKey(SecondActivity.KEY_METHOD_UPDATE)) {
            curMap = (HashMap<String, String>) bundle.getSerializable("curMap");
            String title = curMap.get(MainActivity.ADAPTER_KEY_TITLE);
            edTitle.setText(title);
            String date = curMap.get(MainActivity.ADAPTER_KEY_DATE);
            edDate.setText(date);
            String genre = curMap.get(MainActivity.ADAPTER_KEY_GENRE);
            edGenre.setText(genre);
            String uri = curMap.get(MainActivity.ADAPTER_KEY_PATH);
            ivPoster.setImageURI(Uri.parse(uri));

            curId = Long.parseLong(curMap.get(MainActivity.ADAPTER_KEY_ID));
            curDbFilm = seachFilm(curId);
            //Log.e("###########", "curFilm: "+curDbFilm.getTitle());
        }
        else if(bundle.containsKey(SecondActivity.KEY_METHOD_ADD)) {
            curDbFilm = new Film();
            curDbFilm.setPathImage(bundle.getString("defaultUri"));
        }

        else if(bundle.containsKey(SecondActivity.KEY_METHOD_REVIEW)){
            curDbFilm = (Film) bundle.getSerializable("curDbFilm");
            edTitle.setText(curDbFilm.getTitle());
            edDate.setText(String.valueOf(curDbFilm.getDate()));

            int genreId =  curDbFilm.getIdGenre();
            Optional<Genre> genreOption = dbGenres.stream().filter(g->g.getId() == genreId).findFirst();
            if(genreOption.isPresent())
                edGenre.setText(genreOption.get().getTitle());
            String uri = curDbFilm.getPathImage();
            ivPoster.setImageURI(Uri.parse(uri));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private Film seachFilm(long id) {
//        SelectFilmRepository selectFilmRepository = new SelectFilmRepository(FilmApp.getInstance().getDb().getFilmDao(), SecondActivity.this);
//        selectFilmRepository.execute(curId);    //так нельзя - мгновенн. выход в гл. активн.
        Optional<Film> optionalFilm = dbFilms.stream().filter((f->f.getId() == curId)).findFirst();
        if(optionalFilm.isPresent()){
            return optionalFilm.get();
        }
        else return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void btnClick(View view) {
        int id = view.getId();
        switch (id){
            case R.id.btnApply: {
                curDbFilm.setTitle(edTitle.getText().toString());
                String dateString = edDate.getText().toString();
                curDbFilm.setDate(Date.valueOf(dateString));
                String newGenreString =  edGenre.getText().toString();
                Genre genre = new Genre(newGenreString);
           //========= Изменение жанра =======================================================================================
                Optional<Genre> genreOptional = dbGenres.stream().filter(g -> g.getTitle().equals(newGenreString)).findFirst();//если такой жанр в БД есть
                if (genreOptional.isPresent()) {
                    if (genre != null || !genre.getTitle().equals("") || !genre.getTitle().equals(" ")) {   //и он не пустой
                        Optional<Genre> idSeachGenre = dbGenres.stream().filter(g -> g.getTitle().equals(newGenreString)).findFirst();
                        if (idSeachGenre.isPresent()) {
                            if(curDbFilm.getIdGenre() != idSeachGenre.get().getId())
                                curDbFilm.setIdGenre(Math.toIntExact(idSeachGenre.get().getId()));    //update id genre film
                        }
                        else Log.e(TAG, "Такого жанра в БД не существует!");
                    }
                    else  Log.e(TAG, "Введен пустой жанр!");
                }
                else {                //----если нов. назв. жанра нет в БД - выдать предупреждение -
                    Toast.makeText(this, "Жанр добавляется по отдельной кнопке меню!", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Попытка добваления жанра!");
                }

                //============== Изменение фильма (получ. посылки из DialogSeachFilmByID)====================================
                Intent intent = getIntent();
                Bundle bundleArgs = intent.getExtras();
                if(bundleArgs.containsKey(SecondActivity.KEY_METHOD_UPDATE) ||bundleArgs.containsKey(SecondActivity.KEY_METHOD_REVIEW)) {
                    Log.e("!!!!!!!!", "Second/ update begin curDbFilm: "+curDbFilm.getTitle());
                    UpdateFilmRepository updateFilmRepository = new UpdateFilmRepository(FilmApp.getInstance().getDb().getFilmDao(), SecondActivity.this);
                    updateFilmRepository.execute(curDbFilm);
                    SelectFilmRepository selectFilmRepository = new SelectFilmRepository(FilmApp.getInstance().getDb().getFilmDao(), SecondActivity.this);
                    selectFilmRepository.execute();
                }
                else if (bundleArgs.containsKey(SecondActivity.KEY_METHOD_ADD)){
                    backData.putExtra("curDbFilm", (Serializable) curDbFilm);
                    setResult(RESULT_OK, backData);
                    this.finish();
                }
            }
            break;

            case R.id.btnCancel: setResult(RESULT_CANCELED);
                finish();
                break;

            case R.id.btnChangeImage:{
                Intent intent = new Intent(this, ThirdActivity.class);
                startActivityForResult(intent, SecondActivity.REQUEST_IMG);
            }
            break;
        }
    }
    @Override       //рез-т от кнопки "Измеинть изображ." - DialogFragmFilm
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == SecondActivity.REQUEST_IMG && resultCode == RESULT_OK){
            Uri uri = (Uri) data.getExtras().get(SecondActivity.KEY_URI);
            ImageView ivImage = findViewById(R.id.ivPoster);
            ivImage.setImageURI(uri);
            curDbFilm.setPathImage(String.valueOf(uri));
        }
    }

    //====================методы interface DBCallback=======onPostExecute(Object o) =========================================
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onSelectCollection(List<DBEntity> collection) {
        //----------------------------FILM----------------------------------
        if(collection.get(0) instanceof Film) {
            dbFilms.clear();
            for (DBEntity film : collection) {
                Log.i(TAG, "Selected film collection of title: " + ((Film) film).getTitle());
                dbFilms.add((Film) film);
            }
            //---------------пакет для возвр. в main---------
            backData.putExtra("dbFilms", (Serializable) dbFilms);
        }
  //----------------------------------------GENRE-----------------------------------------
        else if(collection.get(0) instanceof Genre){
            dbGenres.clear();
            for (DBEntity genre : collection) {
                Log.i(TAG, "Selected genre collection of title: " + ((Genre) genre).getTitle());
                dbGenres.add((Genre) genre);
            }
            backData.putExtra("dbGenres", (Serializable) dbGenres);
        }
        setResult(RESULT_OK, backData);
        this.finish();

    }

    @Override
    public void onSelectSingleItem(DBEntity item) {
        if(item instanceof Film){
            Log.e(TAG, "Selected film title: " + ((Film)item).getTitle());
            curDbFilm = (Film)item;
            backData.putExtra("curDbFilm", (Serializable) curDbFilm);
        }
        else {
            Log.e(TAG, "Selected genre title: " + ((Genre)item).getTitle());
            curDbGenre = (Genre)item;
            backData.putExtra("curDbGenre", (Serializable) curDbGenre);
        }
        setResult(RESULT_OK, backData); //для какого запроса???????
        this.finish();
    }

    @Override
    public void onSave() {
        Toast.makeText(this, "SAVED SUCCESSFULLY", Toast.LENGTH_SHORT).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onUpdate() {
        Log.e("!!!!!!!!", "Second/update2");
        Toast.makeText(this, "Фильмы обновлены!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDelete() {

    }

}
