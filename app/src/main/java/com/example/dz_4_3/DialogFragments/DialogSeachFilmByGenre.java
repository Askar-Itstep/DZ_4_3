package com.example.dz_4_3.DialogFragments;

import android.app.DialogFragment;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dz_4_3.R;
import com.example.dz_4_3.SecondActivity;
import com.example.dz_4_3.entity.Film;
import com.example.dz_4_3.entity.Genre;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
//заход из Активити №4
//диалог. окно выбора фильма по жанру
public class DialogSeachFilmByGenre extends DialogFragment implements View.OnClickListener{
    public final static String DIALOG_GENRE = "dialog_genre";
    private EditText edFilmID;
    private static String TAG = "===DialogSeachFilmByGenre===";
    private String  genre;
    private SecondActivity activity;
    private static List<Film> dbFilms;
    private static List<Genre> dbGenres;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = (View) inflater.inflate(R.layout.dialog_select_film_fragment, container, false);
        TextView tvFilm = view.findViewById(R.id.tv_id_film);
        tvFilm.setText("Введите жанр фильма");
        Button btnApplay = view.findViewById(R.id.btnSeachApply);
        Button btnCancel = view.findViewById(R.id.btnSeachCancel);
        btnApplay.setOnClickListener(this::onClick);
        btnCancel.setOnClickListener(this::onClick);
        edFilmID = view.findViewById(R.id.edFilmID);
        Bundle bundle = getArguments();
        dbFilms = (List<Film>) bundle.getSerializable("dbFilms");
        dbGenres = (List<Genre>) bundle.getSerializable("dbGenres");
        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnSeachApply:{
                Bundle arguments = this.getArguments();
                Film film = null;
                if(arguments.containsKey(DIALOG_GENRE)){
                    long genreId = -1;
                    genre = (edFilmID.getText().toString());
                    Optional<Genre> genreOptional = dbGenres.stream().filter(g->g.getTitle().regionMatches(true, 0, genre, 0, 5)).findFirst();
                    if(genreOptional.isPresent()){
                        genreId = genreOptional.get().getId();
                    }
                    else {
                        Log.d(TAG, "Такого жанра в БД нет!!");
                    }

                    long finalGenreId = genreId;
                    Optional<Film> filmOption =  dbFilms.stream().filter(f->f.getIdGenre() == finalGenreId).findFirst();
                    if(filmOption.isPresent()){
                        film = filmOption.get();
                    }else {
                        Log.d(TAG, "Такого фильма нет или неправильный ввод!");
                    }

                    Intent intent = new Intent(this.getActivity(), SecondActivity.class);
                    intent.putExtra(SecondActivity.KEY_METHOD_REVIEW, "review");    //как-бы для Update
                    intent.putExtra("curDbFilm", film);
                    intent.putExtra("dbFilms", (Serializable) dbFilms);
                    intent.putExtra("dbGenres", (Serializable) dbGenres);
                    startActivity(intent);
                }
            }
            break;

            case R.id.btnSeachCancel:this.dismiss();
                break;
        }
    }
}
