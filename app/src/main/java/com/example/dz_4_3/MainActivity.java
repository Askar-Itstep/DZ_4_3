package com.example.dz_4_3;

import android.Manifest;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.example.dz_4_3.DialogFragments.Addable;
import com.example.dz_4_3.DialogFragments.DialogSeachFilmByID;
import com.example.dz_4_3.DialogFragments.Editable;
import com.example.dz_4_3.entity.DBEntity;
import com.example.dz_4_3.entity.Film;
import com.example.dz_4_3.entity.Genre;
import com.example.dz_4_3.repository.DeleteFilmRepository;
import com.example.dz_4_3.repository.InsertFilmRepository;
import com.example.dz_4_3.repository.InsertGenreRepository;
import com.example.dz_4_3.repository.SelectFilmRepository;
import com.example.dz_4_3.repository.SelectGenreRepository;
import com.example.dz_4_3.repository.UpdateFilmRepository;
import com.example.dz_4_3.repository.UpdateGenreRepository;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements DBCallback<DBEntity>, Editable, Addable { //DeletableInterface,

    private static int nmrlColor = Color.rgb(0xD3, 0xDF, 0xA4);    //#d3 df a4
    private static int slctColor = Color.rgb(0xE2, 0xA7, 0x6F);
    private static int curPos = -1;
    private static View curView = null;
    final	static		String	ADAPTER_KEY_ID	= "adapter_key_id";
    final	static		String	ADAPTER_KEY_TITLE	= "adapter_key_title";
    final	static		String	ADAPTER_KEY_DATE		= "adapter_key_date";
    final	static		String	ADAPTER_KEY_GENRE	= "adapter_key_genre";
    final	static		String	ADAPTER_KEY_PATH	= "adapter_key_pathImage";

    private static HashMap<String, String> curMap;
    private static final String TAG = "===MainActivity===";
    private ArrayList<HashMap<String, String>> items;
    private static SimpleAdapter adapter;

    private static List<Film> dbFilms = new ArrayList<>();  //жанры из БД (запис. из врем. об.)
    private static List<Genre> dbGenres = new ArrayList<>();
    private static Film curDbFilm;
    private static Genre curDbGenre;
    private static final int REQUEST_KEY_UPDATE = 1;
    private static final int REQUEST_KEY_ADD = 2;
    private static final int REQUEST_SELECT_ONE_FILM = 3;
    static long curId = -1;
    private static List<Genre> genres;  //жанры из врем. объекта
    private static List<Film> films;
    private String defaultUri;
    private ListFragment listFragment;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //-- Разрешения ------ + SetFilms()-----------
        int permission = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(permission != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
//а)-динам. загруз. из врем. объекта
        SetFilms setFilms = new SetFilms().invoke();    //refactoring
        genres = setFilms.getGenres();
        films = setFilms.getFilms();
//--------- заполнить БД ------------------------------------------------------------------------

        InsertGenreRepository insertGenreRepository = new InsertGenreRepository(FilmApp.getInstance().getDb().getGenreDao(), this);
        insertGenreRepository.execute(genres);

        InsertFilmRepository insertFilmRepository = new InsertFilmRepository(FilmApp.getInstance().getDb().getFilmDao(), this);
        insertFilmRepository.execute(films);

        //------------------------выбрать все из БД  ---------------------------
        SelectGenreRepository selectGenreRepository = new SelectGenreRepository(FilmApp.getInstance().getDb().getGenreDao(), MainActivity.this);
        selectGenreRepository.execute();   //будет сообщ. в логе - взврат вызова в  стр. 137 (обработка в паралл. потоке)

        SelectFilmRepository selectFilmRepository = new SelectFilmRepository(FilmApp.getInstance().getDb().getFilmDao(), MainActivity.this);
        selectFilmRepository.execute();

        Log.e(TAG, "База данных загружена!");
    }

    //+++++++++++++++++++++++++++  методы  DBCallback  ->    onPostExecute(Object o)  +++++++++++++++++++++
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onSelectCollection(List<DBEntity> collection) {
        dbFilms.clear();
        films.clear();
        if(collection.get(0) instanceof Film) {
            for (DBEntity film : collection) {
                Log.e("MainActivity", "Selected film collection of title: " + ((Film) film).getTitle());
                dbFilms.add((Film) film);
            }
            films = dbFilms;
        }

        else {
            dbGenres.clear();
            genres.clear();
            for (DBEntity genre : collection) {
                Log.i("MainActivity", "Selected genre collection of title: " + ((Genre) genre).getTitle());
                if(dbGenres == null) Log.e(TAG, "dbGenre returned IS NULL!");
                else
                    dbGenres.add((Genre) genre);
            }
            genres = dbGenres;
        }
                                                    //-заполнить адаптер -----
        items=  setItemsForAdapter(dbGenres, dbFilms);        //genres,  films - из БД
        adapter = setAdapter(items);

        listFragment = new MyListFragment();
        listFragment.setListAdapter(adapter);
        FragmentManager manager = this.getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.flFragmentContainer, listFragment);
        transaction.commit();
    }

    private SimpleAdapter setAdapter(ArrayList<HashMap<String, String>> items) {
        adapter = new SimpleAdapter(this, items, R.layout.film_item, new String[]{
                MainActivity.ADAPTER_KEY_ID,
                MainActivity.ADAPTER_KEY_TITLE,
                MainActivity.ADAPTER_KEY_DATE,
                MainActivity.ADAPTER_KEY_GENRE,
                MainActivity.ADAPTER_KEY_PATH
        }, new int []{R.id.tvId, R.id.tvTitle, R.id.tvDate, R.id.tvGenre, R.id.ivImg} );
        return adapter;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private ArrayList<HashMap<String, String>> setItemsForAdapter(List<Genre> genres, List<Film> films) {
        items = new ArrayList<>();
        for(int i =0; i < films.size(); i++){
            HashMap<String, String> map = new HashMap<>();
            Film film = films.get(i);
            map.put(MainActivity.ADAPTER_KEY_ID, String.valueOf(film.getId()));
            map.put(MainActivity.ADAPTER_KEY_TITLE, film.getTitle());
            map.put(MainActivity.ADAPTER_KEY_DATE, film.getDate().toString());
            String value =  genres.stream().filter(g->(g.getId() == film.getIdGenre())).findFirst().get().getTitle();
            map.put(MainActivity.ADAPTER_KEY_GENRE,  value);
            map.put(MainActivity.ADAPTER_KEY_PATH, film.getPathImage());
            items.add(map);
        }
        return items;
    }
    @Override
    public void onSelectSingleItem(DBEntity item) {
        if(item instanceof Film){
            Log.i("MainActivity", "Selected film title: " + ((Film)item).getTitle());
            curDbFilm = (Film) item;
        }
        else {
            Log.i("MainActivity", "Selected genre title: " + ((Genre)item).getTitle());
            curDbGenre = (Genre) item;
        }
    }

    @Override
    public void onSave() {
        Toast.makeText(this, "SAVED SUCCESSFULLY", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpdate() {
        Log.d(TAG, "Update!");
    }

    @Override
    public void onDelete() {    //удаляеся в сщщтв. методе иеню, а здесь работа с отображен.
        items.remove(curMap);
        adapter.notifyDataSetChanged();
        Log.d(TAG, "Delete!");
    }

//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    //***************динам. пре-установ. списка фильмов для адаптера (до insert DB)
    private class SetFilms {
        private List<Genre> genres;
        private List<Film> films;

        public List<Genre> getGenres() {
            return genres;
        }

        public List<Film> getFilms() {
            return films;
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        public SetFilms invoke() {
            //1)- genre
            genres = new ArrayList<>();
            genres.add(new Genre((long) 1, "action"));
            genres.add(new Genre((long) 2, "sport"));
            genres.add(new Genre((long) 3, "phantastic"));
            genres.add(new Genre((long) 4, "melodrama"));

            //2)films vol.1
            films = new ArrayList<>();
            films.add(new Film(1L, "Matrix", new Date(99, 8, 22), 3));
            films.add(new Film(2L, "Terminator", new Date(84, 5, 22), 3));
            films.add(new Film(3L, "Predator", new Date(90, 6, 17), 3));
            films.add(new Film(4L, "Rocky4", new Date(85, 4, 14), 2));
            films.add(new Film(5L, "Titanic", new Date(97, 9, 12), 4));
            films.add(new Film(6L, "Commando", new Date(86, 3, 15), 1));
            for (int i = 7; i < 12; i++){
                int year = new Random().nextInt(69)+ 50;
                int month = new Random().nextInt(12)+1;
                int day = new Random().nextInt(24);
                Film film = new Film((long) i, "title#:" + i,  new Date(year, month, day), i % 4 + 1);
                films.add(film);
            }
            //3) - IMG в папке Picture (cейчас директивно, а в перспективе выбор по типу onLoad()
            if (setAvatarFilm()) {
                Log.d(TAG, "Постеры фильмов перенесены!");
            }

            return this;
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        private boolean setAvatarFilm() {
            int permission = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if(permission != PackageManager.PERMISSION_GRANTED){    //true
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }

            File esPicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            if(this.isExternalStorageWritable()){
                if (esPicDir.exists() == false)
                    esPicDir.mkdir();
//копир. файлов из папки Assets в в директорию PICTURE--------------------------------
                //1)--------вытаскив. потоки из папки Assets -------------
                AssetManager assetManager = MainActivity.this.getAssets();
                List<String> imageNames = new ArrayList<>();
                try {
                    imageNames = new ArrayList<>(Arrays.asList(assetManager.list("")));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                List<InputStream> inputStreams = new ArrayList<>();
                imageNames.stream().filter(i -> i.contains(".")).forEach(i -> {
                    try {
                        inputStreams.add(assetManager.open(i));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                final List<byte[]> buffers = new ArrayList<>();   //для кажд. фйла свой буффер
                for (InputStream is : inputStreams) {
                    try {
                        buffers.add(new byte[is.available()]);  //заготовки для буфера
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                for (int i = 0; i < inputStreams.size(); i++) {
                    try {
                        inputStreams.get(i).read(buffers.get(i));
                        inputStreams.get(i).close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                //2)----------------------перебрoс. в директорию PICTURE ------------------------------------------------------
                List<File> listPicture = new ArrayList<>();
                File picDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                //a)-----------созд. файлов в конечн. папке --------------------------------
                imageNames.stream().filter(i -> i.contains(".png") || i.contains(".jpg"))
                        .forEach( iName -> listPicture.add(new File(picDir, iName)));

                //б)---------------------запись изобр. в файлы-------------------------------------------
                List<FileOutputStream> outputStreams = new ArrayList<FileOutputStream>();
                for (int i = 0; i < listPicture.size(); i++) {
                    try {                                           //извлечение потоков
                        outputStreams.add(new FileOutputStream(listPicture.get(i), false));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                for (int i = 0; i < listPicture.size(); i++) {
                    try {
                        outputStreams.get(i).write(buffers.get(i));     //запись
                        outputStreams.get(i).close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                //------------проверка конечн. папки -------------------------------------------------------------
                try {
                    InputStream is = new FileInputStream(listPicture.get(0));
                    Bitmap bmp = BitmapFactory.decodeStream(is); // ;

                    if (bmp != null)
                        Log.e(TAG, "bmp2 IS GOOD!");
                    else Log.e(TAG, "bmp2 IS NULL!"); //??????????????
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //3----------------------------
                File[] files = esPicDir.listFiles();
                if(files == null){
                    Toast.makeText(MainActivity.this, "Каталог носителя пуст!", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "В Каталоге нет изображений!");
                    return false;
                }
                defaultUri = Arrays.stream(files).filter(fl->fl.getName().contains("default")).findFirst().get().toURI().getPath();

                for (Film film: films) {                                    //совпадение частей имен
                    Arrays.stream(files).filter(f -> f.isFile()&& f.getName().regionMatches(true, 0, film.getTitle(), 0, f.getName().indexOf(".")))
                            .forEach(f -> film.setPathImage(f.toURI().getPath()));
                    if(film.getTitle().contains("title")){
                        film.setPathImage(defaultUri);
                    }
//                    Log.e(TAG, film.getPathImage());
                }
                return true;
            }
            Log.e(TAG, "Ошибка создания постера фильма!");
            return false;
        }

        public boolean isExternalStorageWritable() {
            String state = Environment.getExternalStorageState();
            return (state.equals(Environment.MEDIA_MOUNTED)||
                    state.equals(Environment.MEDIA_MOUNTED_READ_ONLY));
        }
        public boolean isExternalStorageReadable() {
            String state = Environment.getExternalStorageState();
            return (state.equals(Environment.MEDIA_MOUNTED) ||
                    state.equals(Environment.MEDIA_MOUNTED_READ_ONLY));
        }
    }


    public static class MyListFragment extends ListFragment {   //почему STATIC ??
        @Override
        public void onResume() {
            super.onResume();
            this.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener(){
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    curMap = (HashMap<String, String>)parent.getAdapter().getItem(position);
                    String	str	= "Название фильма  : " + curMap.get(MainActivity.ADAPTER_KEY_TITLE)  + "\n" +
                            "дата выхода : "	+ curMap.get(MainActivity.ADAPTER_KEY_DATE) + "\n" +
                            "жанр     : "	+ curMap.get(MainActivity.ADAPTER_KEY_GENRE);
                    if(curPos != -1)
                        curView.setBackgroundColor(nmrlColor);
                    curPos = position;
                    curView = view;
                    curView.setBackgroundColor(slctColor);
                    Toast.makeText(parent.getContext(), str, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_create_film:{  //рез-т возвр. в onActivityResult (стр. 386)
                Intent intent = new Intent(this, SecondActivity.class);
                intent.putExtra(SecondActivity.KEY_METHOD_ADD, "add");
                intent.putExtra("dbFilms", (Serializable) dbFilms);
                intent.putExtra("dbGenres", (Serializable) dbGenres);
                intent.putExtra("defaultUri", defaultUri);
                startActivityForResult(intent, MainActivity.REQUEST_KEY_ADD);
            }
            break;

            case R.id.action_update_film:{
                if (curPos == -1) {
                    Toast.makeText(this, "Выберите фильм", Toast.LENGTH_SHORT).show();
                    break;
                }
                Intent intent = new Intent(this, SecondActivity.class);
                intent.putExtra(SecondActivity.KEY_METHOD_UPDATE, "update");
                intent.putExtra("curMap", curMap); //передача текущ. контейнера с фильмом
                intent.putExtra("dbFilms", (Serializable) dbFilms);
                intent.putExtra("dbGenres", (Serializable) dbGenres);
                startActivityForResult(intent, MainActivity.REQUEST_KEY_UPDATE);
                //startActivity(intent);
            }
            break;
//переход в 4 Активность----------------------------------------
            case R.id.action_work_genre:{
                Intent intent = new Intent(this, FourActivity.class);
                intent.putExtra("dbFilms", (Serializable) dbFilms);
                intent.putExtra("dbGenres", (Serializable) dbGenres);
                startActivity(intent);
            }
            break;

            case R.id.action_remove_film:{
                curId = Long.parseLong(curMap.get(MainActivity.ADAPTER_KEY_ID));
                DeleteFilmRepository deleteFilmRepository = new DeleteFilmRepository(FilmApp.getInstance().getDb().getFilmDao(), MainActivity.this);
                deleteFilmRepository.execute(dbFilms.stream().filter(f->f.getId() == curId).findFirst().get());

                SelectFilmRepository selectFilmRepository = new SelectFilmRepository(FilmApp.getInstance().getDb().getFilmDao(), MainActivity.this);
                selectFilmRepository.execute();
            }
            break;

            case R.id.action_select_film_on_id:{
                DialogSeachFilmByID dialog = new DialogSeachFilmByID();
                Bundle bundle = new Bundle();
                bundle.putString(DialogSeachFilmByID.DIALOG_ID, "id");
                bundle.putSerializable("dbFilms", (Serializable) dbFilms);
                bundle.putSerializable("dbGenres", (Serializable) dbGenres);
                dialog.setArguments(bundle);
                dialog.show(getFragmentManager(), "dialog");
            }
            break;
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            if (requestCode == REQUEST_KEY_UPDATE) {
                if(bundle.containsKey("curDbFilm")){    //НЕ ЗАХОДИТ-т.к. в SecondActiv. в пакете  теперь не отправл.
//                    curDbFilm = (Film) bundle.getSerializable("curDbFilm");
//                    curMap.put(MainActivity.ADAPTER_KEY_ID, String.valueOf(curDbFilm.getId()));
//                    curMap.put(MainActivity.ADAPTER_KEY_TITLE, curDbFilm.getTitle());
//                    //Log.e("!!!!!!!!!", "Main/curDBFilm: "+curDbFilm.getTitle());
//                    curMap.put(MainActivity.ADAPTER_KEY_DATE, curDbFilm.getDate().toString());
//                    String value =  genres.stream().filter(g->(g.getId() == curDbFilm.getIdGenre())).findFirst().get().getTitle();
//                    curMap.put(MainActivity.ADAPTER_KEY_GENRE,  value);
//                    curMap.put(MainActivity.ADAPTER_KEY_PATH, curDbFilm.getPathImage());
//                    SelectFilmRepository selectFilmRepository = new SelectFilmRepository(FilmApp.getInstance().getDb().getFilmDao(), MainActivity.this);
//                    selectFilmRepository.execute();
                }
                if(bundle.containsKey("dbFilms")) {
                    dbFilms = (List<Film>) bundle.getSerializable("dbFilms");   //!!!!!! - есть update
                }
                if(bundle.containsKey("dbGenres"))
                    dbGenres = (List<Genre>) bundle.getSerializable("dbGenres");
            }
            else if(requestCode == REQUEST_KEY_ADD){
                if(bundle.containsKey("curDbFilm")){
                    curDbFilm = (Film) bundle.getSerializable("curDbFilm"); Log.d(TAG, "curDbFilm: "+curDbFilm.getTitle());
                    add(curDbFilm);
//                    SelectFilmRepository selectFilmRepository = new SelectFilmRepository(FilmApp.getInstance().getDb().getFilmDao(), MainActivity.this);
//                    selectFilmRepository.execute();
                }
            }
            else if(requestCode == REQUEST_SELECT_ONE_FILM){
                if(bundle.containsKey("curDbFilm"))
                    curDbFilm = (Film) bundle.getSerializable("curDbFilm");
            }
      //1)-------------------внести изм. в отображ. ч/з адаптер (без вызова БД)-------------------------
//            adapter = setAdapter( setItemsForAdapter(dbGenres, dbFilms));
//            listFragment.setListAdapter(adapter);
//            FragmentManager manager = this.getFragmentManager();
//            FragmentTransaction transaction = manager.beginTransaction();
//            transaction.commit();
      //2)-----------------------------так тоже работ., но медленнее!!!!---------------------------------
            SelectFilmRepository selectFilmRepository = new SelectFilmRepository(FilmApp.getInstance().getDb().getFilmDao(), MainActivity.this);
            selectFilmRepository.execute();
        }
    }

    //======================методы для возврата из фрагмента ===============================
    @Override
    public void add(DBEntity entity) {
        if(entity instanceof Genre) {
//            InsertGenreRepository insertGenreRepository = new InsertGenreRepository(FilmApp.getInstance().getDb().getGenreDao(), MainActivity.this);
//            insertGenreRepository.execute(entity);
        }
        else {
            InsertFilmRepository insertFilmRepository = new InsertFilmRepository( FilmApp.getInstance().getDb().getFilmDao(), (DBCallback) this);
            insertFilmRepository.execute(curDbFilm);
        }
    }


    @Override
    public void edit(DBEntity entity) {
        if(entity instanceof Genre) {
            UpdateGenreRepository updateGenreRepository = new UpdateGenreRepository(FilmApp.getInstance().getDb().getGenreDao(), MainActivity.this);
            updateGenreRepository.execute(entity);
        }else {
            UpdateFilmRepository updateFilmRepository = new UpdateFilmRepository(FilmApp.getInstance().getDb().getFilmDao(), MainActivity.this);
            updateFilmRepository.execute(curDbFilm);
        }
    }

//    @Override
//    protected void onRestart() {
//        super.onRestart();
//        Intent intent = getIntent();
//        if(intent.getExtras().get("dbGenres") == null){
//            Log.e("!!!!!!!!!", "intent isNULL");
//
//        }
//        else {
//            List<Genre> tempGenres = (List<Genre>) intent.getExtras().get("dbGenres");
//            if(tempGenres != null)
//                dbGenres = tempGenres;
//            List<Film> tempFilms = (List<Film>) intent.getExtras().get("dbFilms");
//            if(tempFilms != null)
//                dbFilms = tempFilms;
//        }
//\
//    }
}
