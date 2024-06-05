package com.sung.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.InputStream;
import java.util.ArrayList;

public class ChangeWallpaperWorker extends Worker {

    public ChangeWallpaperWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        ArrayList<Uri> imageUris = MySharedPreferencesHelper.loadImageUris(getApplicationContext());
        String screenSelected = MySharedPreferencesHelper.loadScreenSelected(getApplicationContext());
        Uri currentWallpaperUri = MySharedPreferencesHelper.loadCurrentWallpaperUri(getApplicationContext());
        int currentIndex = MySharedPreferencesHelper.loadCurrentIndex(getApplicationContext());

        if (imageUris == null || imageUris.isEmpty()) {
            Log.e("WallpaperWorker", "No images available");
            return Result.failure();
        }

        currentIndex++;
        if (currentIndex >= imageUris.size()) {
            currentIndex = 0;
        }
        Uri selectedImageUri = imageUris.get(currentIndex);

        if (imageUris.size() >= 2) {
            if (selectedImageUri.equals(currentWallpaperUri)) {
                currentIndex++;
                selectedImageUri = imageUris.get(currentIndex);
            }
        }

        try {
            InputStream inputStream = getApplicationContext().getContentResolver().openInputStream(selectedImageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            WallpaperUtils.changeWallpaper(bitmap, screenSelected, getApplicationContext());
            Log.d("WallpaperWorker", "Wallpaper changed");
            inputStream.close();
            MySharedPreferencesHelper.saveCurrnetWallpaperUri(getApplicationContext(), selectedImageUri);
            MySharedPreferencesHelper.saveCurrentIndex(getApplicationContext(), currentIndex);
            return Result.success();
        } catch (Exception e) {
            Log.e("WallpaperWorker", "Error setting wallpaper", e);
            return Result.failure();
        }
    }
}