package com.sung.myapplication;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    Button buttonSelectImage;
    Button buttonChangeWallpaper;
    Button buttonSelectRandom;
    TextView textViewSelectedDirectory;
    ImageView imageViewSelectedImage;
    Switch switchDailyWallpaper;
    Bitmap imageBitmap = null;
    DocumentFile directorySelected = null;
    ArrayList<DocumentFile> imageFiles = null;
    ArrayList<Uri> imageUris = null;

    boolean isImageSet = false;
    boolean isSwitchOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonSelectImage = findViewById(R.id.buttonSelectImage);
        buttonSelectRandom = findViewById(R.id.buttonSelectRandom);
        buttonChangeWallpaper = findViewById(R.id.buttonChangeWallpaper);
        textViewSelectedDirectory = findViewById(R.id.textViewSelectedDirectory);
        imageViewSelectedImage = findViewById(R.id.imageViewSelectedImage);
        switchDailyWallpaper = findViewById(R.id.switchDailyWallpaper);

        loadData();

        buttonSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectDirectory();

            }
        });
        buttonSelectRandom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageUris != null && !imageUris.isEmpty()) {
                    selectRandom(imageUris);
                } else {
                    Toast.makeText(MainActivity.this, "!!Select directory contains Image file!!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        buttonChangeWallpaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isImageSet) {
                    WallpaperUtils.changeWallpaper(imageBitmap, getApplicationContext());
                } else {
                    Toast.makeText(MainActivity.this, "!!Select Image first!!", Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(MainActivity.this, "Wallpaper changed.", Toast.LENGTH_SHORT).show();
            }
        });

        switchDailyWallpaper.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MySharedPreferencesHelper.saveSwitchState(MainActivity.this, isChecked);
                if (isChecked) {
                    scheduleWallpaperChange();
                } else {
                    cancelWallpaperChange();
                }
            }
        });
    }

    public void loadData() {
        if (MySharedPreferencesHelper.loadSelectedDirectoryUri(this) != null) {
            directorySelected = DocumentFile.fromTreeUri(this, MySharedPreferencesHelper.loadSelectedDirectoryUri(this));
            textViewSelectedDirectory.setText(directorySelected.getUri().toString());
            imageUris = MySharedPreferencesHelper.loadImageUris(this);
            isSwitchOn = MySharedPreferencesHelper.loadSwitchState(this);
            switchDailyWallpaper.setChecked(isSwitchOn);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101) {
            if (resultCode == RESULT_OK) {
                processData(data);
            }
        }
    }

    public void selectDirectory() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, 101);
    }

    public void processData(Intent data) {
        Uri uri = data.getData();
        getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        directorySelected = DocumentFile.fromTreeUri(this, uri);
        DocumentFile[] directoryFiles = directorySelected.listFiles();
        textViewSelectedDirectory.setText(directorySelected.getUri().toString());
        imageFiles = getImageFilesOnly(directoryFiles);
        imageUris = new ArrayList<Uri>(imageFiles.size());
        for (int i = 0; i < imageFiles.size(); i++) {
            imageUris.add(imageFiles.get(i).getUri());
        }

        MySharedPreferencesHelper.saveSelectedDirectoryUri(MainActivity.this, directorySelected.getUri());
        MySharedPreferencesHelper.saveImageUris(MainActivity.this, imageUris);
    }

    public ArrayList<DocumentFile> getImageFilesOnly(DocumentFile[] files) {
        ArrayList<DocumentFile> imageFiles = new ArrayList<DocumentFile>(0);
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile() && files[i].getType().contains("image")) {
                imageFiles.add(files[i]);
            }
        }
        return imageFiles;
    }

    public void selectRandom(ArrayList<Uri> imageUris) {
        Random random = new Random();
        int randomIndex = random.nextInt(imageUris.size());
        Uri fileUri = imageUris.get(randomIndex);

        ContentResolver resolver = getContentResolver();
        try {
            InputStream inputStream = resolver.openInputStream(fileUri);
            imageBitmap = BitmapFactory.decodeStream(inputStream);
            imageViewSelectedImage.setImageBitmap(imageBitmap);
            isImageSet = true;

            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void scheduleWallpaperChange() {
        PeriodicWorkRequest changeWallpaperRequest =
                new PeriodicWorkRequest.Builder(ChangeWallpaperWorker.class, 1, TimeUnit.DAYS)
                        .setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS)
                        .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork("myDailyWallpaper", ExistingPeriodicWorkPolicy.UPDATE, changeWallpaperRequest);

        Toast.makeText(this, "Daily Wallpaper Activated!", Toast.LENGTH_SHORT).show();
    }

    public void cancelWallpaperChange() {
        WorkManager.getInstance(this).cancelUniqueWork("myDailyWallpaper");

        Toast.makeText(this, "Daily Wallpaper Cancelled.", Toast.LENGTH_SHORT).show();
    }

    private long calculateInitialDelay() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay();
        return Duration.between(now, nextMidnight).toMillis();
    }
}