package com.example.worker.webparsing.adapter;

import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.Date;

public class Item {

    private String logo;
    private String title;
    private String link;
    private String description;
    private String author;
    private String category;
    private Date pubDate;
    private String img;
    private String video;

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDate() {
        return pubDate;
    }

    public void setDate(Date date) {
        this.pubDate = date;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCategories() {
        return category;
    }

    public void setCategories(String category) {
        this.category = category;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }
}