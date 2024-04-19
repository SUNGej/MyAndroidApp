package com.sung.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import java.util.ArrayList;

public class MySharedPreferencesHelper {
    private static final String PREF_SELECTED_FOLDER_URI = "selectedFolderUri";
    private static final String PREF_SELECTED_IMAGE_URIS = "selectedImageUris";

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
}
