package com.sung.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import java.util.ArrayList;

public class MySharedPreferencesHelper {
    private static final String PREF_SELECTED_FOLDER_URI = "selectedFolderUri";
    private static final String PREF_SELECTED_IMAGE_URIS = "selectedImageUris";
    private static final String PREF_KEY_SWITCH_STATE = "SwitchState";
    private static final String PREF_RADIO_GROUP_STATE = "RadioGroupState";
    private static final String PREF_SCREEN_SELECTED = "ScreenSelected";
    private static final String PREF_CURRENT_WALLPAPER_URI = "CurrentWallpaperUri";

    public static void saveSelectedDirectoryUri(Context context, Uri folderUri) {
        SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PREF_SELECTED_FOLDER_URI, folderUri.toString());
        editor.apply();
    }

    public static Uri loadSelectedDirectoryUri(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String folderUriString = prefs.getString(PREF_SELECTED_FOLDER_URI, null);
        if (folderUriString != null) {
            return Uri.parse(folderUriString);
        } else {
            return null;
        }
    }

    public static void saveImageUris(Context context, ArrayList<Uri> imageUris) {
        SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(PREF_SELECTED_IMAGE_URIS + "_size", imageUris.size());
        for (int i = 0; i < imageUris.size(); i++) {
            editor.putString(PREF_SELECTED_IMAGE_URIS + "_" + i, imageUris.get(i).toString());
        }
        editor.apply();
    }

    public static ArrayList<Uri> loadImageUris(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        int size = prefs.getInt(PREF_SELECTED_IMAGE_URIS + "_size", 0);
        ArrayList<Uri> imageUris = new ArrayList<Uri>();
        for (int i = 0; i < size; i++) {
            String uri = prefs.getString(PREF_SELECTED_IMAGE_URIS + "_" + i, null);
            if (uri != null) {
                imageUris.add(Uri.parse(uri));
            }
        }
        return imageUris;
    }

    public static void saveSwitchState(Context context, boolean isOn) {
        SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(PREF_KEY_SWITCH_STATE, isOn);
        editor.apply();
    }

    public static boolean loadSwitchState(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        return prefs.getBoolean(PREF_KEY_SWITCH_STATE, false);
    }

    public static void saveRadioGroupState(Context context, int checkedId) {
        SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(PREF_RADIO_GROUP_STATE, checkedId);
        editor.apply();
    }

    public static int loadRadioGroupState(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        return prefs.getInt(PREF_RADIO_GROUP_STATE, -1);
    }

    public static void saveScreenSelected(Context context, String screenSelected) {
        SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PREF_SCREEN_SELECTED, screenSelected);
        editor.apply();
    }

    public static String loadScreenSelected(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        return prefs.getString(PREF_SCREEN_SELECTED, "Both");
    }

    public static void resetData(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }

    public static void saveCurrnetWallpaperUri(Context context, Uri imageUri) {
        SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PREF_CURRENT_WALLPAPER_URI, imageUri.toString());
        editor.apply();
    }

    public static Uri loadCurrentWallpaperUri(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String currentWallpaperUriString = prefs.getString(PREF_CURRENT_WALLPAPER_URI, null);
        if (currentWallpaperUriString != null) {
            return Uri.parse(currentWallpaperUriString);
        } else {
            return null;
        }
    }
}
