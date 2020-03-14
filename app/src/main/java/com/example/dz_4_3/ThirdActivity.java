package com.example.dz_4_3;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dz_4_3.components.Component;
import com.example.dz_4_3.components.MyDirectory;
import com.example.dz_4_3.components.MyFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.os.Environment.getExternalStorageDirectory;
//работа с файл. систем. - выбор изображ. для фильма
public class ThirdActivity extends AppCompatActivity {
    public static final String TAG = "===ThirdActivity===";
    public static final int REQUEST_IMG = 111;
    private  Bitmap[] bmpFiles;
    private Map<String, Bitmap> mapBmp;
    private  static List<Component> listItem;
    private  static Component curItem;  //текущ.(выделенн.) Item
    private int nmrlColor = Color.rgb(0xFF, 0xFF, 0xFF);
    private int slctColor = Color.rgb(0xE2, 0xA7, 0x6F);
    private static int curPos = -1; //т.е. 0 - то 1-ый будет выделен
    @SuppressLint("StaticFieldLeak")
    private  static View curView = null;
    private static ArrayAdapter<Component> adapter;
    private static File fileObj;    //файл системы соотв-ий curItem
    private static File esCurDir;   //текущ. директор. системы
    private Bitmap bmp = null;
    private Uri outputUri;  //путь для intent (menu->open file_img)



    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third);
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(permission != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        //распаков. посылки из MainActivity
        Bundle arguments = getIntent().getExtras();
        if(arguments != null){
            bmp =  arguments.getParcelable("bmp");
        }

        esCurDir = getExternalStorageDirectory();   //сначала текущ. = корнев. директор.
        listItem = fillDirectory(esCurDir);       //список всех файлов внутри ее
        GridView gvFiles = this.findViewById(R.id.gvOne);

        //-------------------загруз. и установ. bmp в объекты Component
        this.getAssetManager();

        adapter = new ArrayAdapter<Component>(this, R.layout.gridview_item, R.id.tvItem, listItem){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                Component component = this.getItem(position);

                ImageView ivImg = view.findViewById(R.id.ivImg);
                ivImg.setImageBitmap(component.bmp);

                TextView tvTitle = view.findViewById(R.id.tvItem);
                tvTitle.setText(component.title);
                if(position == curPos){ //curPos изм. в onItemClick
                    view.setBackgroundColor(slctColor);
                    curView = view;
                }else
                    view.setBackgroundColor(nmrlColor);
                return view;
            }
        };

        gvFiles.setAdapter(adapter);
        //-----------обработ. выбора  файла (каталога)------------
        gvFiles.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                curItem = (Component) parent.getAdapter().getItem(position);
                if (curPos != -1)   //нет выбора-снять затен.
                    curView.setBackgroundColor(nmrlColor);
                curPos = position;
                curView = view;
                curView.setBackgroundColor(slctColor); //установ. затен. для выбранн. элем.
                Toast.makeText(ThirdActivity.this, "Выбран: " + curItem.title, Toast.LENGTH_SHORT).show();
                fileObj = (curItem.title.equals(".."))? (esCurDir.getParentFile()) :
                        (new File(esCurDir, curItem.title));
            }
        });
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

    private ArrayList<Component> fillDirectory(File esMainDir) {
        ArrayList<Component> listFiles = new ArrayList<>();
        if (this.isExternalStorageReadable()) {
            File[] arrFiles = esMainDir.listFiles();
            if (arrFiles != null) {
                for (File file : arrFiles) {
                    if (file.isDirectory()) {
                        listFiles.add(new MyDirectory("[" + file.getName() + "]", "folder"));
                    }
                    //поиск по НАЗВАНИЯМ (не расширен.)!
                    else if(file.getName().contains("txt")||file.getName().contains("text") ||file.getName().contains("test")) {
                        listFiles.add(new MyFile(file.getName(), "txt"));
                    }
                    else if(file.getName().contains("img")||file.getName().contains("jpg") ||file.getName().contains("png")) {
                        listFiles.add(new MyFile(file.getName(), "img"));
                    }
                    else {
                        listFiles.add(new MyFile(file.getName(), "other"));
                    }
                }
            } else
                Toast.makeText(this, "Каталог внешнего носителя пуст! c.164", Toast.LENGTH_SHORT).show();
        }
        return listFiles;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private AssetManager getAssetManager() {    //установ bmp в items
        AssetManager assetManager = getAssets();
        int SIZE = 3;       //кол. img для для файлов не относ. к txt и folder
        String[] imageNames = new String[SIZE +2];    //new String[]{"folder.png", "music.png", "text.png", "WMV.png"};
        try {
            imageNames = assetManager.list("");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "path: "+ getFileStreamPath(imageNames[0]));
        InputStream[] inputStreams = new InputStream[imageNames.length];
        bmpFiles = new Bitmap[imageNames.length];
        mapBmp = new HashMap<>();
        try{
            for(int i = 0; i < imageNames.length; i++){
                inputStreams[i] = assetManager.open(imageNames[i]);
                bmpFiles[i] = BitmapFactory.decodeStream(inputStreams[i]);
                String iconType = imageNames[i].toLowerCase().substring(0,imageNames[i].indexOf("."));
                int finalI = i;
                if(iconType.contains("folder")) {
                    listItem.stream().filter(item->item.typeFile.equals("folder")).forEach(item->item.bmp = bmpFiles[finalI]);
                    mapBmp.put("folder", bmpFiles[i]);
                }
                else if (iconType.contains("doc") || iconType.contains("txt")){
                    listItem.stream().filter(item->item.typeFile.equals("txt")).forEach(item->item.bmp = bmpFiles[finalI]);
                    mapBmp.put("txt", bmpFiles[i]);
                }
                else if (iconType.contains("image") ||iconType.contains("img") || iconType.contains("jpg")|| iconType.contains("png")){
                    listItem.stream().filter(item->item.typeFile.equals("img")).forEach(item->item.bmp = bmpFiles[finalI]);
                    mapBmp.put("img", bmpFiles[i]);
                }
                else if(!iconType.contains("default")){
                    listItem.stream().filter(item-> item.typeFile.equals("")).forEach(item->item.bmp = bmpFiles[finalI]);
                    mapBmp.put("other", bmpFiles[i]);
                }
                inputStreams[i].close();
            }
        }catch (Exception e){
            Log.e(TAG, "Ошибка загрузки изображений, c.210: "+e.getMessage());
        }
        return assetManager;
    }

    //----------------------------------------------------------------------------------------------
    //меню
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.second_menu, menu);
        return true;
    }
//обработка меню

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_open_file: {
                if (curItem == null) {
                    Toast.makeText(this, "Не выбран файл/каталог!", Toast.LENGTH_SHORT).show();
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                //попытка выйти в папку -/0
                if (esCurDir.getName().equals(String.valueOf(0)) && curItem.title.equals("..")) {
                    Toast.makeText(this, "Это запретная область!", Toast.LENGTH_SHORT).show();
                    curView.setBackgroundColor(nmrlColor);
                    break;
                }

                if (isExternalStorageWritable()) {
                    File esMainDir = getExternalStorageDirectory();
                    TextView tvOne = findViewById(R.id.tvActivity);
                    tvOne.setText(esMainDir.getAbsolutePath());
                    AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Widget_AbsListView);    //нужен при откр. файла
                    curItem.title = curItem.title.replaceAll("[\\[\\]]", "");
                    //при клике по [..] или вошел, но не выбран файл, то наверх - в родит. каталог
                    fileObj = (curItem.title.equals("..") || curItem.title.equals(esCurDir.getName())) ?
                            (esCurDir.getParentFile()) : (new File(esCurDir, curItem.title));
                    //отображение содержимого-------------------------------------------------------
                    //--------------------если директория ------------------------------
                    if (fileObj.isDirectory()) {
                        //при клике по катал. вывести его cодержим.
                        adapter.clear();
                        List<Component> listFiles = this.fillDirectory(fileObj);//в Components еще надо IMG !
                        if (listFiles.size() > 0) {        //+IMG
                            listFiles.stream().filter(f -> f.typeFile.equals("folder")).forEach(f -> f.bmp = mapBmp.get("folder"));
                            listFiles.stream().filter(f -> f.typeFile.equals("txt")).forEach(f -> f.bmp = mapBmp.get("txt"));
                            listFiles.stream().filter(f -> f.typeFile.equals("img")).forEach(f -> f.bmp = mapBmp.get("img"));
                            listFiles.stream().filter(f -> f.typeFile.equals("other")).forEach(f -> f.bmp = mapBmp.get("other"));
                        }
                        //для глав.корн. катал. не добавл.[..]
                        //т.е. в отображ. списка файлов- будут файлы + папка [..]-ссылка на корн. кат.
                        if (fileObj.compareTo(getExternalStorageDirectory()) != 0) {
                            adapter.add(new MyDirectory(mapBmp.get("folder"), "[..]", "folder"));
                        }
                        adapter.addAll(listFiles);
                        esCurDir = fileObj;
                        curPos = -1;
                    }
                    //-------------------если файл IMG.--------------------------------------------------------------------
                    else if (fileObj.isFile() && curItem.typeFile.equals("img")) {
                        try {
                            InputStream is = new FileInputStream(fileObj);
                            bmp = BitmapFactory.decodeStream(is);
//                            bmp = scaleDownBitmap(bmp, 100, this);  //сжатие файла изобр.
                            outputUri = Uri.fromFile(fileObj);
                            //Log.e(TAG, "Third/path: "+fileObj.getAbsolutePath());
                            //Log.e(TAG, "Third/uri: "+outputUri);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        //отправить в Активность2
                        Intent intent = getIntent();
                        intent.setAction("com.example.dz_4_3.SecondActivity");
                        intent.putExtra(SecondActivity.KEY_URI, outputUri);
                        setResult(RESULT_OK, intent);
                        this.finish();
                    }

                    tvOne.setText(esCurDir.getAbsolutePath());  //в шапке абс. путь к выбр. файлу
                } else
                    Log.d(TAG, "Внешний	носитель	не	готов!");
            }
            break;
            //---------------удален. только файлов ???-----------------------------------------------------------
            case R.id.action_remove_file: {
                if (fileObj == null) {
                    Toast.makeText(this, "Директория/файл не выбран(а)!", Toast.LENGTH_SHORT).show();
                    break;
                }
                if (isExternalStorageWritable()) {
                    if (fileObj.isFile()) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault);
                        builder.setTitle("Вы действительно хотите удалить этот файл: ");
                        builder.setMessage(curItem.title + "?");
                        builder.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (fileObj.delete()) {
                                    adapter.remove(curItem);
                                    adapter.notifyDataSetChanged();
                                    curPos = -1;
                                    Log.d(TAG, "Файл " + curItem.title + " удален");
                                }
                                else Log.d(TAG, "Ошибка удаления файла " + curItem.title);
                            }
                        });
                        builder.setNegativeButton("Нет", null);
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault);
                        builder.setTitle("Вы действительно хотите удалить этот каталог: ");
                        builder.setMessage(curItem.title + "?");
                        builder.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (delDir(fileObj)) {
                                    adapter.remove(curItem);
                                    adapter.notifyDataSetChanged();
                                    curPos = -1;
                                    Log.d(TAG, "Каталог " + curItem.title + " удален");
                                }
                                else Log.d(TAG, "Ошибка удаления каталога " + curItem.title);

                            }
                        });
                        builder.setNegativeButton("Нет", null);
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                }
            }
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean delDir(File dir){
        boolean flag = false;
        File reFile = null;
        String reName="";
        if(dir.getName().contains("[]")||dir.getName().contains("]")) {
            reName = dir.getName().replaceAll("[\\[\\]]", "");
            reFile = new File(reName);
            if(dir.renameTo(reFile)) Log.d(TAG, "каталог перименован");
        }
        File[] files = dir.listFiles();
        if(files == null || files.length == 0) {    //если папка пустая
            if(dir.delete()) flag=true;
            else {
                flag = false;
                Log.d(TAG, "Каталог " + curItem.title + " НЕ удален");
            }
        }
        else {                                           //если нет
            for(int i = 0; i < files.length; i++){
                File file = files[i];
                if(file.isFile()){      //если содержим. -  файл
                    flag = file.delete();
                }
                else delDir(file);      //если папка - в рекурсию
            }
        }
        return flag;
    }

    public static Bitmap scaleDownBitmap(Bitmap photo, int newHeight, Context context) {
        final float densityMultiplier = context.getResources().getDisplayMetrics().density;
        int h= (int) (newHeight*densityMultiplier);
        int w= (int) (h * photo.getWidth()/((double) photo.getHeight()));
        photo=Bitmap.createScaledBitmap(photo, w, h, true);
        return photo;
    }
}
