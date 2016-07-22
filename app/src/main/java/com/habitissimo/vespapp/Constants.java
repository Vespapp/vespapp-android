package com.habitissimo.vespapp;

import android.text.TextUtils;

public class Constants {
    public static final String PICTURES_LIST = "com.habitissimo.vespapp.PICTURES_LIST";
    public static final String KEY_CAPTURE = "com.habitissimo.vespapp.CAPTURE";
    public static final String PREFERENCES = "VespappPreferences";
    public static final String API_BASE_URL = "http://vespapp.uib.es/api/";
//    public static final String API_BASE_URL = "http://minerales.uib.es/api/";
//    public static final String API_BASE_URL = "http://192.168.0.100/api/";

    public static boolean isBaseApiUrlDefined() {
        return !TextUtils.isEmpty(API_BASE_URL);
    }
}
