package com.sung.myapplication;

import android.app.WallpaperManager;
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
import java.util.Random;

public class ChangeWallpaperWorker extends Worker {

    public ChangeWallpaperWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        ArrayList<Uri> imageUris = MySharedPreferencesHelper.loadImageUris(getApplicationContext());
        if (imageUris == null || imageUris.isEmpty()) {
            Log.e("WallpaperWorker", "No images available");
            return Result.failure();
        }

        Random random = new Random();
        Uri selectedImageUri = imageUris.get(random.nextInt(imageUris.size()));

        try {
            InputStream inputStream = getApplicationContext().getContentResolver().openInputStream(selectedImageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            WallpaperManager wallpaperManager = WallpaperManager.getInstance(getApplicationContext());
            wallpaperManager.setBitmap(bitmap);
            Log.d("WallpaperWorker", "Wallpaper changed");
            inputStream.close();
            return Result.success();
        } catch (Exception e) {
            Log.e("WallpaperWorker", "Error setting wallpaper", e);
            return Result.failure();
        }
    }
}