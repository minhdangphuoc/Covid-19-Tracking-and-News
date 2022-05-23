package com.example.covid19trackingandnews;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;

import java.io.Serializable;
import java.text.ParseException;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class newsitem implements Serializable{
    private String url = "";
    private String imageUrl = "";
    private String title = "";
    private String author = "";
    private String date = "";

    // Constructor
    public newsitem(String url, String imageUrl, String title, String author, String date) {
        this.url = url;
        this.imageUrl = imageUrl;
        this.title = title;
        this.author = author;
        this.date = date;
    }

    // Getters and setters
    public String getUrl(){
        return this.url;
    }
    public String getImageUrl(){
        return this.imageUrl;
    }
    public String getTitle(){
        return this.title;
    }
    public String getAuthor(){
        return this.author;
    }
    public String getDate(){
        return this.date;
    }
    public String getDateFromPublishDate() throws ParseException {
        SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        Date currentTime = Calendar.getInstance().getTime();
        long diff = ChronoUnit.DAYS.between(timeFormat.parse(this.date).toInstant(), currentTime.toInstant());

        if(diff < 30) return diff + " days ago";
        else return this.date.substring(0, this.date.indexOf("T"));
    }
}
