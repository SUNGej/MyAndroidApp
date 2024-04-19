package com.sung.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;

import android.Manifest;
import android.app.WallpaperManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    Button buttonSelectImage;
    Button buttonChangeWallpaper;
    Button buttonSelectRandom;
    TextView textViewSelectedDirectory;
    ImageView imageViewSelectedImage;
    Bitmap imageBitmap = null;
    DocumentFile directorySelected = null;
    ArrayList<DocumentFile> imageFiles = null;
    ArrayList<Uri> imageUris = null;

    boolean isImageSet = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonSelectImage = findViewById(R.id.buttonSelectImage);
        buttonSelectRandom = findViewById(R.id.buttonSelectRandom);
        buttonChangeWallpaper = findViewById(R.id.buttonChangeWallpaper);
        textViewSelectedDirectory = findViewById(R.id.textViewSelectedDirectory);
        imageViewSelectedImage = findViewById(R.id.imageViewSelectedImage);

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
                    try {
                        changeWallpaper(imageBitmap);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    Toast.makeText(MainActivity.this, "!!Select Image first!!", Toast.LENGTH_LONG).show();
                }
            }
        });

        loadData();
    }

    public void loadData() {
        if (MySharedPreferencesHelper.loadSelectedDirectoryUri(this) != null) {
            directorySelected = DocumentFile.fromTreeUri(this, MySharedPreferencesHelper.loadSelectedDirectoryUri(this));
            textViewSelectedDirectory.setText(directorySelected.getUri().toString());
            imageUris = MySharedPreferencesHelper.loadImageUris(this);
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

    public void changeWallpaper(Bitmap imageBitmap) throws IOException {
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
        wallpaperManager.setBitmap(imageBitmap);
        Toast.makeText(this, "Wallpaper changed.", Toast.LENGTH_LONG).show();
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
}