package com.cocodev.university.delhi.duplugin.Utility;

import java.util.ArrayList;

/**
 * Created by Sudarshan on 16-06-2017.
 */

public class Notice {

    private String uid;
    //private String department;
    private long time;
    private long deadline;
    private String description;
    private String title;
    private ArrayList<String> imageUrls;
    public Notice(){
        //default constructor
    }

    public Notice(String title,String department, long time, long deadline, String description,ArrayList<String> imageUrls) {
        //this.department = department;
        this.title = title;
        this.time = time;
        this.deadline = deadline;
        this.description = description;
        this.imageUrls = imageUrls;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getDeadline() {
        return deadline;
    }

    public void setDeadline(long deadline) {
        this.deadline = deadline;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public ArrayList<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(ArrayList<String> imageUrls) {
        this.imageUrls = imageUrls;
    }
}
