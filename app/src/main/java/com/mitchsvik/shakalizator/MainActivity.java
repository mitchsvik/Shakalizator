package com.mitchsvik.shakalizator;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private ImageView photo;
    private SeekBar scaleBar;
    private Button filterRed, filterGreen, filterBlue, filterEmpty, saveButton;
    private double scaleRate = 1.0;
    Bitmap loadedBm, filteredBm;
    float[] colorTransform;
    ColorMatrix colorMatrix;
    ColorFilter colorFilter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        photo = (ImageView) findViewById(R.id.imagePhoto);
        photo.setScaleType(ImageView.ScaleType.FIT_XY);

        filterEmpty = (Button) findViewById(R.id.buttonFilterEmpty);
        filterRed = (Button) findViewById(R.id.buttonFilterRed);
        filterGreen = (Button) findViewById(R.id.buttonFilterGreen);
        filterBlue = (Button) findViewById(R.id.buttonFilterBlue);
        saveButton = (Button) findViewById(R.id.buttonSave);

        filterEmpty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorTransform = new float[]{
                        1, 0, 0, 0, 0,
                        0, 1, 0, 0, 0,
                        0, 0, 1, 0, 0,
                        0, 0, 0, 1, 0};
                filterRGB();
            }
        });
        filterRed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorTransform = new float[]{
                        1, 0, 0, 0, 0,
                        0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0,
                        0, 0, 0, 1, 0};
                filterRGB();
            }
        });
        filterGreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorTransform = new float[]{
                        0, 0, 0, 0, 0,
                        0, 1, 0, 0, 0,
                        0, 0, 0, 0, 0,
                        0, 0, 0, 1, 0};
                filterRGB();
            }
        });
        filterBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorTransform = new float[]{
                        0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0,
                        0, 0, 1, 0, 0,
                        0, 0, 0, 1, 0};
                filterRGB();
            }
        });
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                    }
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            1);
                    return;
                }
                MediaStore.Images.Media.insertImage(getContentResolver(), ((BitmapDrawable)photo.getDrawable()).getBitmap(), new Date().toString(), "Shakalized photo");
            }
        });

        scaleBar = (SeekBar) findViewById(R.id.seekBar);
        scaleBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                scaleRate = (10.1-Math.sqrt(++progress))/10.0;
                proceedImage(scaleRate);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        Button clickButton = (Button) findViewById(R.id.button);
        clickButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (data == null) {
                return;
            }
            try {
                InputStream inputStream = this.getContentResolver().openInputStream(data.getData());
                File targetFile = new File(this.getCacheDir(), "r");
                OutputStream outStream = new FileOutputStream(targetFile);

                final byte[] buffer = new byte[8 * 1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outStream.write(buffer, 0, bytesRead);
                }
                inputStream.close();
                outStream.close();
                Uri uri = Uri.fromFile(targetFile);

                photo.setImageURI(uri);
                loadedBm = ((BitmapDrawable)photo.getDrawable()).getBitmap();
                filteredBm = loadedBm.copy(Bitmap.Config.ARGB_8888, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void proceedImage(double scaledRate) {
        photo.requestLayout();
        if (filteredBm != null) {
            Bitmap finalBitmap = Bitmap.createScaledBitmap(filteredBm, (int) (photo.getWidth() * scaledRate),
                    (int) (photo.getHeight() * scaledRate), false);
            photo.setImageBitmap(finalBitmap);
        }
    }

    private void filterRGB() {
        if (loadedBm != null) {
            colorMatrix = new ColorMatrix(colorTransform);
            colorFilter = new ColorMatrixColorFilter(colorMatrix);

            filteredBm = loadedBm.copy(Bitmap.Config.ARGB_8888, true);
            Paint paint = new Paint();
            paint.setColorFilter(colorFilter);

            Canvas canvas = new Canvas(filteredBm);
            canvas.drawBitmap(filteredBm, 0, 0, paint);

            proceedImage(scaleRate);
            //photo.setImageBitmap(filteredBm);
        }
    }

//    private void save(byte[] bytes, File file) throws IOException {
//        OutputStream output = null;
//        try {
//            output = new FileOutputStream(file);
//            output.write(bytes);
//        } finally {
//            if (null != output) {
//                output.close();
//            }
//        }
//    }
}
