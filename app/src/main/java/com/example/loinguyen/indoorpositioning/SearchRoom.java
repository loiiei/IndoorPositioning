package com.example.loinguyen.indoorpositioning;

import ir.mirrajabi.searchdialog.core.Searchable;

public class SearchRoom implements Searchable {

    private String mTitle;
    public SearchRoom(String mTitle){
        this.mTitle = mTitle;
    }

    public void setmTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    @Override
    public String getTitle() {
        return mTitle;
    }
}
