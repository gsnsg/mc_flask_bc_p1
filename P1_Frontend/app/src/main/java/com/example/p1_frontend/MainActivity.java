package com.example.p1_frontend;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PackageManagerCompat;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.graphics.Bitmap;
import android.widget.ImageView;
import android.widget.Button;
import android.os.Bundle;
import android.content.Intent;

import java.io.ByteArrayOutputStream;

public class MainActivity extends AppCompatActivity {


    private Handler mainHandler = new Handler(Looper.getMainLooper());

    private ImageView previewImageView;

    private void startCamera() {
        Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(openCameraIntent, 101);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check if permission is granted or not. If not provided ask the user for permission
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[] { Manifest.permission.CAMERA },101);
        }

        final Button takePhotoButton = (Button) findViewById(R.id.click_photo_button);
        takePhotoButton.setOnClickListener(v -> startCamera());

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("Image Helper", "Printing here");

        if(requestCode == 101 && resultCode == Activity.RESULT_OK) {
            Bitmap clickedImageBitmap =  (Bitmap) data.getExtras().get("data");
            Bundle bundle = new Bundle();
            bundle.putParcelable("BitmapImage", clickedImageBitmap);

            Intent openUploader = new Intent(this, ImageUploaderActivity.class);
            openUploader.putExtras(bundle);

            runOnUiThread(new Runnable() {
                public void run() {
                    startActivity(openUploader);
                }
            });
        }

    }
}