package com.sung.myapplication;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import androidx.annotation.Nullable;

import java.io.IOException;

public class WallpaperUtils {

    public static void changeWallpaper(Bitmap imageBitmap, String screenSelected, Context context) {
        Bitmap scaledBitmap = scaleBitmapToDisplay(imageBitmap, context);
        Bitmap finalBitmap = addPaddingToBitmap(scaledBitmap, context);

        WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
        try {
            if (screenSelected.equals("Home")) {
                wallpaperManager.setBitmap(finalBitmap, null, false, WallpaperManager.FLAG_SYSTEM);
            } else if (screenSelected.equals("Lock")) {
                wallpaperManager.setBitmap(finalBitmap, null, false, WallpaperManager.FLAG_LOCK);
            } else {
                wallpaperManager.setBitmap(finalBitmap);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Bitmap scaleBitmapToDisplay(Bitmap bitmap, Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;
        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();

        float bitmapRatio = (float) bitmapWidth / bitmapHeight;
        float screenRatio = (float) screenWidth / screenHeight;

        int newWidth, newHeight;
        if (bitmapRatio > screenRatio) {
            newWidth = screenWidth;
            newHeight = (int) (newWidth / bitmapRatio);
        } else {
            newHeight = screenHeight;
            newWidth = (int) (newHeight * bitmapRatio);
        }
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);

        return scaledBitmap;
    }
    public static Bitmap addPaddingToBitmap(Bitmap bitmap, Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;
        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();

        Bitmap finalBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(finalBitmap);
        canvas.drawColor(Color.BLACK);

        int left = (screenWidth - bitmapWidth) / 2;
        int top = (screenHeight - bitmapHeight) / 2;
        canvas.drawBitmap(bitmap, left, top, null);

        return finalBitmap;
    }

}
