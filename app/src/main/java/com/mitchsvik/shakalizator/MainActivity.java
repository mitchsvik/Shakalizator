package com.mitchsvik.shakalizator;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
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
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private ImageView photo;
    private SeekBar scaleBar;
    private Button filterRed, filterGreen, filterBlue, filterEmpty, saveButton,camera;
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
                Toast.makeText(MainActivity.this,"Saved to gallery",Toast.LENGTH_SHORT).show();
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
        camera = (Button) findViewById(R.id.camera);
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,CameraView.class);
                startActivityForResult(intent,2);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK&&requestCode ==1) {
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
        if(resultCode == RESULT_OK && requestCode == 2){
            byte[] jpegData = App.getInstance().getCapturedPhotoData();
            loadedBm = decodeSampledBitmapFromResourceMemOpt(jpegData, photo.getWidth(),
                    photo.getHeight());
            filteredBm = loadedBm.copy(Bitmap.Config.ARGB_8888, true);
            photo.setImageBitmap(filteredBm);
        }
    }
    public static Bitmap decodeSampledBitmapFromResourceMemOpt(
            byte[] bytes, int reqWidth, int reqHeight) {
        try {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);

            options.inSampleSize = calculateInSampleSize(options, reqWidth,
                    reqHeight);
            options.inPurgeable = true;
            options.inInputShareable = true;
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;

            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);

        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }


        return inSampleSize;
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
        }
    }
}
