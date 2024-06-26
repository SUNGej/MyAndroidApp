package com.sung.myapplication;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    Button buttonSelectImage;
    Button buttonShuffle;
    Button buttonChangeWallpaper;
    Button buttonReset;
    Button buttonPrev;
    Button buttonNext;
    TextView textViewSelectedDirectory;
    ImageView imageViewSelectedImage;
    Switch switchDailyWallpaper;
    RadioGroup radioGroupSelectScreen;
    RadioButton radioButtonHome;
    RadioButton radioButtonLock;
    RadioButton radioButtonBoth;
    Bitmap imageBitmap = null;
    DocumentFile directorySelected = null;
    ArrayList<DocumentFile> imageFiles = null;
    ArrayList<Uri> imageUris = null;
    Uri currentImageUri = null;

    boolean isImageSet = false;
    boolean isSwitchOn = false;
    int radioGroupCheckedId = -1;
    int currentIndex;
    String screenSelected = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonSelectImage = findViewById(R.id.buttonSelectImage);
        buttonShuffle = findViewById(R.id.buttonShuffle);
        buttonChangeWallpaper = findViewById(R.id.buttonChangeWallpaper);
        buttonReset = findViewById(R.id.buttonReset);
        buttonPrev = findViewById(R.id.buttonPrev);
        buttonNext = findViewById(R.id.buttonNext);
        textViewSelectedDirectory = findViewById(R.id.textViewSelectedDirectory);
        imageViewSelectedImage = findViewById(R.id.imageViewSelectedImage);
        switchDailyWallpaper = findViewById(R.id.switchDailyWallpaper);
        switchDailyWallpaper.setClickable(false);
        radioGroupSelectScreen = findViewById(R.id.radioGroupSelectScreen);
        radioButtonHome = findViewById(R.id.radioButtonHome);
        radioButtonLock = findViewById(R.id.radioButtonLock);
        radioButtonBoth = findViewById(R.id.radioButtonBoth);

        loadData();

        buttonSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectDirectory();
            }
        });
        buttonShuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageUris == null) {
                    Toast.makeText(MainActivity.this, "!!Select Directory First!!", Toast.LENGTH_SHORT).show();
                } else {
                    shuffleImageList(imageUris);
                }
            }
        });
        buttonChangeWallpaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isImageSet) {
                    WallpaperUtils.changeWallpaper(imageBitmap, screenSelected, getApplicationContext());
                    MySharedPreferencesHelper.saveCurrentIndex(MainActivity.this, currentIndex);
                    MySharedPreferencesHelper.saveScreenSelected(MainActivity.this, screenSelected);
                    MySharedPreferencesHelper.saveRadioGroupState(MainActivity.this, radioGroupCheckedId);
                    MySharedPreferencesHelper.saveCurrnetWallpaperUri(MainActivity.this, currentImageUri);
                } else {
                    if (directorySelected == null) {
                        Toast.makeText(MainActivity.this, "!!Select Directory first!!", Toast.LENGTH_SHORT).show();
                    }
                    Toast.makeText(MainActivity.this, "!!Select Image first!!", Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(MainActivity.this, "Wallpaper changed.", Toast.LENGTH_SHORT).show();
            }
        });
        buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (directorySelected == null && imageUris == null) {
                    Toast.makeText(MainActivity.this, "No selection now", Toast.LENGTH_SHORT).show();
                } else {
                    MySharedPreferencesHelper.resetData(getApplicationContext());
                    resetData();
                    Toast.makeText(MainActivity.this, "Selection reset", Toast.LENGTH_SHORT).show();
                }
            }
        });
        buttonPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (directorySelected == null) {
                    return;
                } else if (imageUris != null && imageUris.size() <= 1) {
                    return;
                }
                currentIndex--;
                if (currentIndex < 0) {
                    currentIndex = imageUris.size() - 1;
                }
                setImageView(imageUris, currentIndex);
                currentImageUri = imageUris.get(currentIndex);
            }
        });
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (directorySelected == null) {
                    return;
                } else if (imageUris != null && imageUris.size() <= 1) {
                    return;
                }
                currentIndex++;
                if (currentIndex >= imageUris.size()) {
                    currentIndex = 0;
                }
                setImageView(imageUris, currentIndex);
                currentImageUri = imageUris.get(currentIndex);
            }
        });

        switchDailyWallpaper.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked && directorySelected != null && imageUris != null) {
                    MySharedPreferencesHelper.saveSwitchState(MainActivity.this, isChecked);
                    scheduleWallpaperChange();
                } else {
                    cancelWallpaperChange();
                }
            }
        });

        radioGroupSelectScreen.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                radioGroupCheckedId = checkedId;

                if (checkedId == radioButtonHome.getId()) {
                    screenSelected = "Home";
                } else if (checkedId == radioButtonLock.getId()) {
                    screenSelected = "Lock";
                } else if (checkedId == radioButtonBoth.getId()) {
                    screenSelected = "Both";
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
            setRadioButtonsClickable(!isSwitchOn);
        }
        setSwitchDailyWallpaperClickable();
        screenSelected = MySharedPreferencesHelper.loadScreenSelected(this);
        radioGroupCheckedId = MySharedPreferencesHelper.loadRadioGroupState(this);
        setRadioGroupSelectScreenState();
        currentIndex = MySharedPreferencesHelper.loadCurrentIndex(this);
        if (currentIndex >= 0) {
            setImageView(imageUris, currentIndex);
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
        imageViewSelectedImage.setImageResource(R.drawable.ic_launcher_foreground);
        setSwitchDailyWallpaperClickable();

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

        if (imageUris.size() == 1) {
            Toast.makeText(this, "There is only 1 image", Toast.LENGTH_SHORT).show();
        } else {
            int count = 0;
            while (fileUri.equals(currentImageUri)) {
                Log.d("Main", "selection repeated : "+fileUri.toString()+
                        "\n  reselect : "+ ++count);
                randomIndex = random.nextInt(imageUris.size());
                fileUri = imageUris.get(randomIndex);
            }
        }
        currentImageUri = fileUri;

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

    public void setImageView(ArrayList<Uri> imageUris, int index) {
        ContentResolver resolver = getContentResolver();
        try {
            InputStream inputStream = resolver.openInputStream(imageUris.get(index));
            imageBitmap = BitmapFactory.decodeStream(inputStream);
            imageViewSelectedImage.setImageBitmap(imageBitmap);
            isImageSet = true;

            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void shuffleImageList(ArrayList<Uri> imageUris) {
        if (imageUris.size() >= 1) {
            if (imageUris.size() >= 2) {
                Collections.shuffle(imageUris, new Random(System.currentTimeMillis()));
                Toast.makeText(this, imageUris.size()+" Images Shuffled.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Only 1 image file exists", Toast.LENGTH_SHORT).show();
            }
            imageViewSelectedImage.setImageResource(R.drawable.ic_launcher_foreground);
            isImageSet = false;
            currentIndex = -1;
            MySharedPreferencesHelper.saveImageUris(this, imageUris);
            MySharedPreferencesHelper.saveCurrentIndex(this, currentIndex);
        } else {
            Toast.makeText(this, "!!No image file!!\nSelect directory contains image file", Toast.LENGTH_SHORT).show();
        }
    }

    public void scheduleWallpaperChange() {
        MySharedPreferencesHelper.saveScreenSelected(this, screenSelected);
        MySharedPreferencesHelper.saveRadioGroupState(this, radioGroupSelectScreen.getCheckedRadioButtonId());
        setRadioButtonsClickable(false);

        PeriodicWorkRequest changeWallpaperRequest =
                new PeriodicWorkRequest.Builder(ChangeWallpaperWorker.class, 1, TimeUnit.DAYS)
                        .setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS)
                        .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork("myDailyWallpaper", ExistingPeriodicWorkPolicy.UPDATE, changeWallpaperRequest);

        Toast.makeText(this, "Daily Wallpaper Activated!", Toast.LENGTH_SHORT).show();
    }

    public void cancelWallpaperChange() {
        WorkManager.getInstance(this).cancelUniqueWork("myDailyWallpaper");

        MySharedPreferencesHelper.saveSwitchState(this, false);
        setRadioButtonsClickable(true);

        Toast.makeText(this, "Daily Wallpaper Cancelled.", Toast.LENGTH_SHORT).show();
    }

    private long calculateInitialDelay() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay();
        return Duration.between(now, nextMidnight).toMillis();
    }

    public void resetData() {
        directorySelected = null;
        imageUris = null;
        imageBitmap = null;
        imageFiles = null;
        isImageSet = false;
        textViewSelectedDirectory.setText(null);
        switchDailyWallpaper.setChecked(false);
        isSwitchOn = false;
        imageViewSelectedImage.setImageResource(R.drawable.ic_launcher_foreground);
        setSwitchDailyWallpaperClickable();
    }

    public void setSwitchDailyWallpaperClickable() {
        if (imageUris == null || imageUris.size() == 0) {
            switchDailyWallpaper.setClickable(false);
            switchDailyWallpaper.setTextColor(Color.GRAY);
        } else {
            switchDailyWallpaper.setClickable(true);
            switchDailyWallpaper.setTextColor(Color.BLACK);
        }
    }

    public void setRadioGroupSelectScreenState() {
        if (radioGroupCheckedId == radioButtonHome.getId()) {
            radioButtonHome.setChecked(true);
        } else if (radioGroupCheckedId == radioButtonLock.getId()) {
            radioButtonLock.setChecked(true);
        } else if (radioGroupCheckedId == radioButtonBoth.getId() || radioGroupCheckedId == -1) {
            radioButtonBoth.setChecked(true);
        }
    }

    public void setRadioButtonsClickable(boolean clickable) {
        radioButtonHome.setClickable(clickable);
        radioButtonLock.setClickable(clickable);
        radioButtonBoth.setClickable(clickable);
        if (clickable) {
            radioButtonHome.setTextColor(Color.BLACK);
            radioButtonLock.setTextColor(Color.BLACK);
            radioButtonBoth.setTextColor(Color.BLACK);
        } else {
            radioButtonHome.setTextColor(Color.GRAY);
            radioButtonLock.setTextColor(Color.GRAY);
            radioButtonBoth.setTextColor(Color.GRAY);
        }
    }
}