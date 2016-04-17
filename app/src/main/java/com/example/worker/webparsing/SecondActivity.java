package com.example.worker.webparsing;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.androidquery.util.AQUtility;
import com.example.worker.webparsing.adapter.AdapterMain;
import com.example.worker.webparsing.adapter.Item;
import com.example.worker.webparsing.utils.AppStatus;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class SecondActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private AdapterMain adapter;
    private Activity activity;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;

    public Elements title;
    public Elements content;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_layout);

        activity = this;

        swipeRefreshLayout = new SwipeRefreshLayout(activity);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_red_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_green_light,
                android.R.color.holo_blue_bright);

        recyclerView = new RecyclerView(activity);
        recyclerView.setHasFixedSize(true);
        StaggeredGridLayoutManager mLayoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        swipeRefreshLayout.addView(recyclerView);

        if (AppStatus.getInstance(this).isOnline()) {
            new NewThread().execute();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);
//            builder.setTitle("No Internet");
            builder.setMessage("Проверьте подключение и повторите попытку.");

            builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
                }
                });

                builder.setPositiveButton("Повторить", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which)
                {
                    dialog.dismiss();
                    recreate();
                }
                });
                AlertDialog dialog = builder.create(); // calling builder.create after adding buttons
                dialog.show();
                }
    }

    public class NewThread extends AsyncTask<String, Void, ArrayList<Item>> {

        @Override
        protected void onPreExecute() {
            swipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected ArrayList<Item> doInBackground(String... arg) {
            ArrayList<Item> data = new ArrayList<>();

            int MAX_PAGE = 2;

            for (int i = 1; i <= MAX_PAGE; i++) {
                final String url = "http://fratria.ru/cgi-bin/MainNews/index.cgi?line_id=0&page=" + i;
                Document doc;

                try {
                    doc = Jsoup.connect(url)
                            .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:40.0) Gecko/20100101 Firefox/40.1")
                            .cookie("auth", "token")
                            .timeout(10000)
                            .get();
                    content = doc.select(".c1-post");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                for (org.jsoup.nodes.Element contents : content) {
                    Item item = new Item();

                    // Заголовок
                    title = contents.select("h2");
                    item.setTitle(title.text());

                    // Ссылка изображения
                    try {
                        Elements link = contents.select(".c1-post-data img[src]");
                        String imageUrl = link.attr("src");
                        imageUrl = imageUrl.substring(imageUrl.indexOf("download")+8);
                        item.setImg("http://fratria.ru/download" + imageUrl);
                    } catch (Exception e) {
                    e.printStackTrace();
                    }

                    // Ссылка видео
                    try {
                        Elements videoLink = contents.select(".c1-post-data iframe[src]");
                        String videoUrl = videoLink.attr("src");
                        item.setVideo(videoUrl);
                    } catch (Exception e) {
                    e.printStackTrace();
                    }

                    // Ссылка
                    try {
                        Elements postLink = contents.select("h2 a[href^=/news/20]");
                        String postUrl = postLink.attr("href");
                        item.setLink("http://fratria.ru" + postUrl);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // Категория
                    try {
                    String categories = contents.select("h2 img[alt]").attr("alt");
                    categories = categories.toUpperCase();
                    item.setCategories(categories);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    data.add(item);
                }
            }
            return data;
        }

        @Override
        protected void onPostExecute(ArrayList<Item> data) {

            if(adapter == null) {
                adapter = new AdapterMain(activity, data);
                recyclerView.setAdapter(adapter);
                getWindow().setContentView(swipeRefreshLayout);
            } else {
                adapter.clearData();
                adapter.setData(data);
                adapter.notifyDataSetChanged();
            }
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private static long back_pressed;

    @Override
    public void onBackPressed() {
        if (back_pressed + 2000 > System.currentTimeMillis())
            super.onBackPressed();
        else {
            Toast.makeText(getApplicationContext(), R.string.back_message,
                    Toast.LENGTH_SHORT).show();
        }
        back_pressed = System.currentTimeMillis();
    }

    protected void onDestroy(){

        super.onDestroy();

        if(isTaskRoot()){

            //clean the file cache with advance option
            long triggerSize = 6000000; //starts cleaning when cache size is larger than 3M
            long targetSize = 5000000;      //remove the least recently used files until cache size is less than 2M
            AQUtility.cleanCacheAsync(this, triggerSize, targetSize);
        }

    }

    @Override
    public void onRefresh() {
        if (AppStatus.getInstance(this).isOnline()) {
            new NewThread().execute();
        } else {
            Toast.makeText(
                    getApplicationContext(),
                    R.string.connectError,
                    Toast.LENGTH_LONG).show();
            swipeRefreshLayout.setRefreshing(false);
        }
    }
}