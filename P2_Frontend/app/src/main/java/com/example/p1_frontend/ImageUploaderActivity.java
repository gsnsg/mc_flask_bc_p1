package com.example.p1_frontend;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Base64;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ImageUploaderActivity extends AppCompatActivity {

    private final OkHttpClient client = new OkHttpClient();

    private ImageView displayImage;

    private Bitmap selectedImageBitMap;

    private Boolean ongoingRequest = false;

    private void startCamera() {
        TextView txtView = (TextView) findViewById(R.id.prediction_label);
        txtView.setText("");
        Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(openCameraIntent, 101);
    }

    private void setupImageView() {
        displayImage = (ImageView) findViewById(R.id.display_image);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            selectedImageBitMap = bundle.getParcelable("BitmapImage");
            if (selectedImageBitMap != null) {
                displayImage.setImageBitmap(selectedImageBitMap);
            }
        }
    }

    private void setupRetakeButton() {

        Button retakeButton = (Button) findViewById(R.id.retake_button);
        retakeButton.setOnClickListener(v -> startCamera());
    }

    private void setupUploadButton() {
        Button btn = (Button) findViewById(R.id.upload_img_button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!ongoingRequest)
                    ongoingRequest = true;
                    makePostRequest();
                }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_uploader);

        setupImageView();
        setupRetakeButton();
        setupUploadButton();
    }

    private String getImageEncoding() {
        Bitmap bitmap = selectedImageBitMap.copy(selectedImageBitMap.getConfig(), false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(byteArray);
    }

    private void makePostRequest() {
        MediaType MEDIA_TYPE = MediaType.parse("application/json");
        // Use this url for emulator debugging
//        String url = "http://10.0.2.2:5000/v1/classify";
        // Use this url for physical device debugging
        String url = "http://localhost:5000/v1/classify";
        String fileName = Instant.now().getEpochSecond() + ".png";
        JSONObject postData = new JSONObject();

        try {
            postData.put("img", getImageEncoding());
            postData.put("file_name", fileName);
        } catch(JSONException e){
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(MEDIA_TYPE, postData.toString());

        // Reference: https://stackoverflow.com/questions/2785485/is-there-a-unique-android-device-id
        String uuid = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("uid", uuid)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (response.code() != 200) {
                    Log.v("Error code: ", String.valueOf(response.code()));
                } else {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            try {
                                TextView txtView = (TextView) findViewById(R.id.prediction_label);
                                String jsonData = response.body().string();
                                JSONObject Jobject = new JSONObject(jsonData);
                                int predictedValue = Jobject.getInt("value");
                                txtView.setText("Predicted Value: " + String.valueOf(predictedValue));
                                showToast("Image Classified Successfully!");
                            } catch (Exception e) {
                                showToast(e.toString());
                            }
                        }
                    });

                }
                runOnUiThread(new Runnable() {
                    public void run() {
                        ongoingRequest = false;
                    }
                });

            }
        });
    }

    private void showToast(String text) {
        runOnUiThread(() -> Toast.makeText(ImageUploaderActivity.this,
                text,
                Toast.LENGTH_LONG).show());
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == Activity.RESULT_OK) {
            selectedImageBitMap = (Bitmap) data.getExtras().get("data");
            runOnUiThread(() -> displayImage.setImageBitmap(selectedImageBitMap));
        }
    }
}
