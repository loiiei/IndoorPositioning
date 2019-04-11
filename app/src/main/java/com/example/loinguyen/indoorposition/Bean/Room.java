package com.example.loinguyen.indoorposition.Bean;

import android.annotation.SuppressLint;
import android.os.Parcel;

import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;

import java.io.Serializable;

@SuppressLint("ParcelCreator")
public class Room implements SearchSuggestion, Serializable {
    private int id;
    private String title;
    private String description;
    private Double x;
    private Double y;

    public Room(){};

    public Room(String suggestion) {
        title= suggestion;
    }

    @Override
    public String getBody() {
        String name = title + " " + description;
        return name;
    }

    public Room(Double x, Double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

    }

    public Double getX() {
        return x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public Double getY() {
        return y;
    }

    public void setY(Double y) {
        this.y = y;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
