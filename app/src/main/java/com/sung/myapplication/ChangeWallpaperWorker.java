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
import java.util.Random;

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

        if (imageUris == null || imageUris.isEmpty()) {
            Log.e("WallpaperWorker", "No images available");
            return Result.failure();
        }

        Random random = new Random();
        int randomIndex = random.nextInt(imageUris.size());
        Uri selectedImageUri = imageUris.get(randomIndex);

        if (imageUris.size() > 1) {
            int count = 0;
            while (selectedImageUri.equals(currentWallpaperUri)) {
                Log.d("ChangeWallpaperWorker", "selection repeated : "+selectedImageUri.toString()+
                        "\n  reselect : "+ ++count);
                randomIndex = random.nextInt(imageUris.size());
                selectedImageUri = imageUris.get(randomIndex);
            }
        }
        MySharedPreferencesHelper.saveCurrnetWallpaperUri(getApplicationContext(), selectedImageUri);

        try {
            InputStream inputStream = getApplicationContext().getContentResolver().openInputStream(selectedImageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            WallpaperUtils.changeWallpaper(bitmap, screenSelected, getApplicationContext());
            Log.d("WallpaperWorker", "Wallpaper changed");
            MySharedPreferencesHelper.saveCurrnetWallpaperUri(getApplicationContext(), selectedImageUri);
            inputStream.close();
            return Result.success();
        } catch (Exception e) {
            Log.e("WallpaperWorker", "Error setting wallpaper", e);
            return Result.failure();
        }
    }
}