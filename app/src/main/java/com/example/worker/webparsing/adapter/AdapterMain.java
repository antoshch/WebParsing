package com.example.worker.webparsing.adapter;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.example.worker.webparsing.R;
import com.example.worker.webparsing.descriptionAxtivityWebwiev.DescriptionActivity;

//import java.text.DateFormat;
//import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class AdapterMain extends RecyclerView.Adapter<AdapterMain.ViewHolder> {

    private ArrayList<Item> data;
    private AQuery aq;
    private Activity activity;

    public AdapterMain(Activity activity, ArrayList<Item> data) {
        this.activity = activity;
        this.data = data;
        aq = new AQuery(activity);
    }

    public ArrayList<Item> getData() {
        return data;
    }

    public void setData(ArrayList<Item> data) {
        this.data = data;
    }

    public void clearData() {
        data.clear();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private ImageView stgvImageView;
        private TextView siteurl;
        private TextView articleTitle;
        private TextView category;
        private at.markushi.ui.CircleButton share;

        public ViewHolder(View holderView) {
            super(holderView);
            cardView = (CardView) holderView.findViewById(R.id.card_view);
            stgvImageView = (ImageView) holderView.findViewById(R.id.stgvImageView);
            siteurl = (TextView) holderView.findViewById(R.id.siteurl);
            articleTitle = (TextView) holderView.findViewById(R.id.articleTitle);
            category = (TextView) holderView.findViewById(R.id.category);
            share = (at.markushi.ui.CircleButton) holderView.findViewById(R.id.button0);
        }
    }

    @Override
    public AdapterMain.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        Item item = data.get(i);
        aq.id(viewHolder.articleTitle).text(item.getTitle());
        aq.id(viewHolder.category).text(item.getCategories());
        aq.id(viewHolder.siteurl).text(item.getLink());
        aq.id(viewHolder.stgvImageView).clear();
        if (TextUtils.equals(item.getImg(),""))
            aq.id(viewHolder.stgvImageView).gone();
        else {

            String youtubeVideo = "";
            if (item.getVideo().contains("lj-toys") && item.getVideo().contains("youtube") && item.getVideo().contains("vid=")) {
                try {
                    youtubeVideo = item.getVideo().substring(item.getVideo().indexOf("vid=") + 4, item.getVideo().indexOf("&", item.getVideo().indexOf("vid=") + 4));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (item.getVideo().contains("youtube") && item.getVideo().contains("embed/")) {
                try {
                    youtubeVideo = item.getVideo().substring(item.getVideo().indexOf("embed/") + 6);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            String youtubeVideoImage = youtubeVideo;
            if (youtubeVideoImage.contains("?")) {
                youtubeVideoImage = youtubeVideoImage.substring(0, youtubeVideoImage.indexOf("?"));
            }

            if (!youtubeVideo.equals("")) {
                aq.id(viewHolder.stgvImageView).visible().image("http://img.youtube.com/vi/" + youtubeVideoImage + "/0.jpg", true, true, 640, 0, null, AQuery.FADE_IN, 9.0f / 16.0f);
            }
            else {
                aq.id(viewHolder.stgvImageView).visible().image(item.getImg(), true, true, 640, 0, null, AQuery.FADE_IN, AQuery.RATIO_PRESERVE);
            }
        }

        final String url = item.getLink();
        aq.id(viewHolder.cardView).clicked(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent i = new Intent(activity, DescriptionActivity.class);
                    i.setData(Uri.parse(url));
                    activity.startActivity(i);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        aq.id(viewHolder.share).clicked(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, url);
                sendIntent.setType("text/plain");
                activity.startActivity(Intent.createChooser(sendIntent,  "Поделиться через:"));
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}