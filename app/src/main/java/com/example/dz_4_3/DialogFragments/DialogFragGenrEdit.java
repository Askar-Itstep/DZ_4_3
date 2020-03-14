package com.example.dz_4_3.DialogFragments;

import android.app.DialogFragment;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.dz_4_3.R;
import com.example.dz_4_3.entity.Genre;

import java.util.List;
import java.util.Optional;

//--диалог. окно редактур. жанров----------------------------------------
public class DialogFragGenrEdit extends DialogFragment implements View.OnClickListener{

    public final static String DIALOG_OLDTITLE = "dialog_oldtitle";
//-----------------------------разные ключт для добавлен. и измен.------------------------------
    public final static String DIALOG_METHOD_ADD = "dialog_add";
    public final static String DIALOG_METHOD_UPDATE = "dialog_update";

    private EditText edOldTitle;
    private EditText edNewTitle;
    private static String TAG = "===DialogFragGenrEdit===";
    private Addable addable;
    private Editable editable;
    private Genre curGenre;
    private List<Genre> dbGenres;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_edit_genre_fragment, container, false);
        view.findViewById(R.id.btnApplyGenre).setOnClickListener(this::onClick);
        view.findViewById(R.id.btnCancelGenre).setOnClickListener(this::onClick);
        //Log.e(TAG, "!!!!!!!!!!!!!!!!!!");
        edOldTitle = view.findViewById(R.id.edOldTitle);
        edNewTitle = view.findViewById(R.id.edNewTitle);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Bundle bundleArgs = this.getArguments();
        if(bundleArgs.containsKey(DialogFragGenrEdit.DIALOG_METHOD_UPDATE)){
            curGenre = (Genre) bundleArgs.getSerializable("curGenre");
            String oldTitle = curGenre.getTitle();
            edOldTitle.setText(oldTitle);
        }

        dbGenres = (List<Genre>) bundleArgs.getSerializable("dbGenres");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        addable = (Addable)context;
        editable = (Editable) context;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onClick(View v) {//Update
        switch (v.getId()){
       case R.id.btnApplyGenre:                     //забрость в поля Активности знач. из EditText Dialog
                Bundle bundleArgs = this.getArguments();
                Genre genre = new Genre();
                String newTitle = edNewTitle.getText().toString();
                genre = new Genre(newTitle);
                if(bundleArgs.containsKey(DialogFragGenrEdit.DIALOG_METHOD_UPDATE)) {
                    //--------------------------------------если  жанр c тем же назван. в БД есть-------------------------------
                    Optional<Genre> genreOptional = dbGenres.stream().filter(g -> g.getTitle().equals(newTitle)).findFirst();
                    if (genreOptional.isPresent()) {
                        Log.e(TAG, "Введен такой же жанр!");
                        this.dismiss();
                        break;
                    }
                    if (genre == null || genre.getTitle().equals("") || genre.getTitle().equals(" ")) {  //и он не пустой
                        Log.e(TAG, "Введен пустой жанр!");
                        this.dismiss();
                        break;
                    }
                    editable.edit(genre);
                }
                else if(bundleArgs.containsKey(DialogFragGenrEdit.DIALOG_METHOD_ADD) ){
                    Genre finalGenre = genre;
                    Log.e(TAG, "genre: "+genre.getTitle());
                    Optional<Genre> genreOption = dbGenres.stream().filter(g->g.getTitle().regionMatches(true, 0, finalGenre.getTitle(), 0, 5)).findFirst();
                    if(genreOption.isPresent()){            // и такой элем. найден
                        Log.e(TAG, "Такой жанр есть в Базе");
                    }
                    else
                        addable.add(genre);
                }
                this.dismiss();
                break;
            case R.id.btnCancelGenre:
                this.dismiss();
                break;
        }
    }
}
