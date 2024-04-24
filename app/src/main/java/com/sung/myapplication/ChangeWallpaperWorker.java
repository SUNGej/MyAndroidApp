package com.sung.myapplication;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

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
            Bitmap scaledBitmap = scaleBitmapToDisplay(bitmap);
            Bitmap finalBitmap = addPaddingToBitmap(scaledBitmap);
            WallpaperManager wallpaperManager = WallpaperManager.getInstance(getApplicationContext());
            wallpaperManager.setBitmap(finalBitmap);
            Log.d("WallpaperWorker", "Wallpaper changed");
            inputStream.close();
            return Result.success();
        } catch (Exception e) {
            Log.e("WallpaperWorker", "Error setting wallpaper", e);
            return Result.failure();
        }
    }

    public Bitmap scaleBitmapToDisplay(Bitmap bitmap) {
        // 화면 크기 가져오기
        WindowManager windowManager = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;

        // 이미지 크기 가져오기
        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();

        // 이미지 비율 계산
        float bitmapRatio = (float) bitmapWidth / bitmapHeight;

        // 화면 비율 계산
        float screenRatio = (float) screenWidth / screenHeight;

        // 이미지 크기 조정
        int newWidth, newHeight;
        if (bitmapRatio > screenRatio) {
            // 이미지의 가로를 화면에 맞추고, 세로는 비율 유지하여 조정
            newWidth = screenWidth;
            newHeight = (int) (newWidth / bitmapRatio);
        } else {
            // 이미지의 세로를 화면에 맞추고, 가로는 비율 유지하여 조정
            newHeight = screenHeight;
            newWidth = (int) (newHeight * bitmapRatio);
        }

        // 새로운 비트맵 생성
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);

        return scaledBitmap;
    }
    public Bitmap addPaddingToBitmap(Bitmap bitmap) {
        // 화면 크기 가져오기
        WindowManager windowManager = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;

        // 비트맵 크기 가져오기
        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();

        // 새로운 비트맵 생성
        Bitmap finalBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);

        // 캔버스에 그리기
        Canvas canvas = new Canvas(finalBitmap);
        canvas.drawColor(Color.BLACK); // 배경을 검은색으로 채우기

        // 이미지를 중앙에 그리기
        int left = (screenWidth - bitmapWidth) / 2;
        int top = (screenHeight - bitmapHeight) / 2;
        canvas.drawBitmap(bitmap, left, top, null);

        return finalBitmap;
    }
}