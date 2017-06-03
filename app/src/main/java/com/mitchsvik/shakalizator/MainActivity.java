package com.mitchsvik.shakalizator;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
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
    private SeekBar bar;
    private Button filter1, save;
    Bitmap bm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        photo = (ImageView) findViewById(R.id.imagePhoto);
        photo.setScaleType(ImageView.ScaleType.FIT_XY);
        filter1 = (Button) findViewById(R.id.buttonFilter1);
        save = (Button) findViewById(R.id.buttonSave);
        filter1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterRGB(128, 255, 255);
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
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
        bar = (SeekBar) findViewById(R.id.seekBar);
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                photo.requestLayout();
                Bitmap finalBitmap = Bitmap.createScaledBitmap(bm, photo.getWidth()*(101-progress)/100, photo.getHeight()*(101-progress)/100, false);
                photo.setImageBitmap(finalBitmap);
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
                bm = ((BitmapDrawable)photo.getDrawable()).getBitmap();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void filterRGB(double R, double G, double B) {
        if (bm != null) {
            Bitmap localbm = ((BitmapDrawable)photo.getDrawable()).getBitmap();
            Bitmap newBmp = Bitmap.createBitmap(localbm.getWidth(), localbm.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(newBmp);
            c.drawBitmap(localbm, 0, 0, new Paint());
            for (int i=0; i<newBmp.getWidth(); i++)
                for (int j=0; j<newBmp.getHeight(); j++) {
                    byte blue = (byte) ((newBmp.getPixel(i, j) & 0xff));
                    byte green = (byte) (((newBmp.getPixel(i, j) >> 8) & 0xff));
                    byte red = (byte) (((newBmp.getPixel(i, j) >> 16) & 0xff));
                    int newPixel= Color.rgb((int)(red*R), (int)(green*G), (int)(blue*B));
                    newBmp.setPixel(i, j, newPixel);
                }
            photo.setImageBitmap(newBmp);
        }
    }

    private void save(byte[] bytes, File file) throws IOException {
        OutputStream output = null;
        try {
            output = new FileOutputStream(file);
            output.write(bytes);
        } finally {
            if (null != output) {
                output.close();
            }
        }
    }
}
