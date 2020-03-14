package com.example.dz_4_3;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dz_4_3.DialogFragments.Addable;
import com.example.dz_4_3.DialogFragments.DialogFragGenrEdit;
import com.example.dz_4_3.DialogFragments.DialogSeachFilmByGenre;
import com.example.dz_4_3.DialogFragments.Editable;
import com.example.dz_4_3.entity.DBEntity;
import com.example.dz_4_3.entity.Film;
import com.example.dz_4_3.entity.Genre;
import com.example.dz_4_3.repository.DeleteGenreRepository;
import com.example.dz_4_3.repository.InsertGenreRepository;
import com.example.dz_4_3.repository.SelectGenreRepository;
import com.example.dz_4_3.repository.UpdateGenreRepository;

import java.io.Serializable;
import java.util.List;
//заход из Глав. активн.
//акивность по работе с жанрами-------------------------------------
public class FourActivity extends AppCompatActivity implements DBCallback<DBEntity>, Editable, Addable {

    private static final String TAG = "===FourActivity===";
    private ListView listGenre;
    private int nmrlColor = Color.rgb(0xED, 0xE2, 0x75);
    private int slctColor = Color.rgb(0xE2, 0xA7, 0x6F);
    //индекс выбран. элем. списка
    private int curItem = -1;   //indx 1-элем. = 0!
    private View curView = null; //текущ. выбран. элем. списка - пока null
    private static List<Genre> dbGenres = null;
    private static List<Film> dbFilms = null;
    private  static  Genre curGenre;
    private static ArrayAdapter<Genre> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_four);
        listGenre = this.findViewById(R.id.lvGenre);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        dbFilms = (List<Film>) bundle.getSerializable("dbFilms");
        dbGenres = (List<Genre>) bundle.getSerializable("dbGenres");
        adapter = new ArrayAdapter<Genre>(this, R.layout.genre_item, R.id.tvGenreItem, dbGenres){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                Genre genre = this.getItem(position);    //dbGenres.get(position);
                View view  = super.getView(position, convertView, parent);
                TextView tvTitle =  view.findViewById(R.id.tvGenreItem);
                tvTitle.setText(genre.getTitle());
                if(position == curItem){
                    view.setBackgroundColor(slctColor);
                    curView = view;
                } else
                    view.setBackgroundColor(nmrlColor);
                return view;
            }
        };
        listGenre.setAdapter(adapter);
        listGenre.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                curGenre = dbGenres.get(position);
                if(curItem != -1){
                    curView.setBackgroundColor(nmrlColor);
                }
                //установ. выделен. на текущ. элем. списка
                curItem = position;
                curView = view;
                curView.setBackgroundColor(slctColor);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.genre_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.action_create_genre:{
                DialogFragGenrEdit dialog = new DialogFragGenrEdit();
                Bundle bundleArgs = new Bundle();
                bundleArgs.putString(DialogFragGenrEdit.DIALOG_METHOD_ADD, "add");
                bundleArgs.putString(DialogFragGenrEdit.DIALOG_OLDTITLE, "");
                bundleArgs.putSerializable("dbGenres", (Serializable) dbGenres);
                dialog.setArguments(bundleArgs);
                dialog.show(getFragmentManager(), "dialog");
            }
            break;
            case R.id.action_remove_genre:{
                DeleteGenreRepository deleteGenreRepository = new DeleteGenreRepository(FilmApp.getInstance().getDb().getGenreDao(), FourActivity.this);
                deleteGenreRepository.execute(curGenre);

                SelectGenreRepository selectGenreRepository = new SelectGenreRepository(FilmApp.getInstance().getDb().getGenreDao(), FourActivity.this);
                selectGenreRepository.execute();
            }
            break;
            case R.id.action_update_genre:{
                if(curGenre == null){
                    Toast.makeText(this, "Снчала выберите жанр!", Toast.LENGTH_SHORT).show();
                    break;
                }
                DialogFragGenrEdit dialog = new DialogFragGenrEdit();
                Bundle bundleArgs = new Bundle();
                bundleArgs.putString(DialogFragGenrEdit.DIALOG_METHOD_UPDATE, "update");
                bundleArgs.putSerializable("curGenre", curGenre);
                bundleArgs.putSerializable("dbGenres", (Serializable) dbGenres);
                dialog.setArguments(bundleArgs);
                dialog.show(getFragmentManager(), "dialog");
            }
            break;
            case R.id.action_select_film_on_genre:{
                DialogSeachFilmByGenre dialog = new DialogSeachFilmByGenre();
                Bundle bundleArgs = new Bundle();
                bundleArgs.putString(DialogSeachFilmByGenre.DIALOG_GENRE, "dialog_genre");
                bundleArgs.putSerializable("dbFilms", (Serializable) dbFilms);
                bundleArgs.putSerializable("dbGenres", (Serializable) dbGenres);
                dialog.setArguments(bundleArgs);
                dialog.show(getFragmentManager(), "dialog");
            }
            break;
        }
        return true;
    }
//==========================================================================================================interface CallBack
    @Override
    public void onSelectCollection(List<DBEntity> collection) {
        dbGenres.clear();
        for (DBEntity genre : collection) {
            Log.i("MainActivity", "Selected genre collection of title: " + ((Genre) genre).getTitle());
            if(dbGenres == null) Log.e(TAG, "dbGenre returned IS NULL!");
            else
                dbGenres.add((Genre) genre);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onSelectSingleItem(DBEntity item) {
        Log.i("MainActivity", "Selected genre title: " + ((Genre)item).getTitle());
        curGenre = (Genre) item;
    }

    @Override
    public void onSave() {
        Toast.makeText(this, "Сохранен новый жанр!", Toast.LENGTH_SHORT).show();
        SelectGenreRepository selectGenreRepository = new SelectGenreRepository(FilmApp.getInstance().getDb().getGenreDao(), FourActivity.this);
        selectGenreRepository.execute();
    }

    @Override
    public void onUpdate() {
        SelectGenreRepository selectGenreRepository = new SelectGenreRepository(FilmApp.getInstance().getDb().getGenreDao(), FourActivity.this);
        selectGenreRepository.execute();
        adapter.notifyDataSetChanged();
        Log.d(TAG, "Произведено обновление жанра!");
    }

    @Override
    public void onDelete() {
        dbGenres.remove(curGenre);
        adapter.notifyDataSetChanged();
        Log.d(TAG, "Произведено удаление!");
    }
//==================================================================================================================interface Addable
    @Override
    public void add(DBEntity entity) {
        InsertGenreRepository insertGenreRepository = new InsertGenreRepository(FilmApp.getInstance().getDb().getGenreDao(), FourActivity.this);
        insertGenreRepository.execute(entity);
        SelectGenreRepository selectGenreRepository = new SelectGenreRepository(FilmApp.getInstance().getDb().getGenreDao(), FourActivity.this);
        selectGenreRepository.execute();
    }

    @Override
    public void edit(DBEntity entity) {
        UpdateGenreRepository updateGenreRepository = new UpdateGenreRepository(FilmApp.getInstance().getDb().getGenreDao(), FourActivity.this);
        updateGenreRepository.execute(entity);
        SelectGenreRepository selectGenreRepository = new SelectGenreRepository(FilmApp.getInstance().getDb().getGenreDao(), FourActivity.this);
        selectGenreRepository.execute();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("dbGenres", (Serializable) dbGenres);
        startActivity(intent);
    }
}

