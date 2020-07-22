package com.zoomers.flowerfinder.ui.main;

import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A class to hold pictures in the history
 */
public class PictureContent {

    public static final List<PictureItem> ITEMS = new ArrayList<>();

    public static void loadImage(File file, String result, String lat, String lng) {
        PictureItem newItem = new PictureItem();
        newItem.uri = Uri.fromFile(file);
        newItem.result = result;
        newItem.lat = lat;
        newItem.lng = lng;
        addItem(newItem);
    }

    private static void addItem(PictureItem item) {
        ITEMS.add(0, item); // prepend item
    }

    // PictureItem class
    public static class PictureItem {
        public Uri uri;
        public String result;
        public String lat;
        public String lng;
    }
}