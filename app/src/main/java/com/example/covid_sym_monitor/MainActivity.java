package com.example.covid_sym_monitor;

//Main Activity Class contains all the button listeners and processes done in the main screen of the app.

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;



public class MainActivity extends AppCompatActivity {

    HashMap<String, Float> symptoms = new HashMap<String, Float>(){{
        put("Nausea",0.0f);
        put("Headache",0.0f);
        put("Diarrhea",0.0f);
        put("Sore Throat",0.0f);
        put("Fever",0.0f);
        put("Muscle Ache",0.0f);
        put("Smell Taste Loss",0.0f);
        put("Cough",0.0f);
        put("Shortness of breath",0.0f);
        put("Feeling tired",0.0f);
        put("Heart Rate",0.0f);
        put("Respiratory Rate",0.0f);
    }};

    private static final int VIDEO_CAPTURE = 101;
    float hr = 0.0f;
    String folder_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/HeartRate/";
    String videoName = "heart.mp4";
    String mpjegName = "heartRate.mjpeg";
    String aviName = "final.avi";
    DatabaseHandler dbHandler;

    TextView respRate, heartRate1;
    ProgressDialog progressDialog;
    CameraActivity cameraActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        configurePermissions();

        progressDialog = new ProgressDialog(this);
        heartRate1 = (TextView) findViewById(R.id.heartRate);
        respRate = (TextView) findViewById(R.id.respRate);
        dbHandler = new DatabaseHandler(MainActivity.this);
        cameraActivity = new CameraActivity();
        Button heart_rate_btn = (Button)findViewById(R.id.heartRateMeasure);
        heart_rate_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT,45);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivityForResult(intent, VIDEO_CAPTURE);
            }
        });

        Button resp_btn = (Button)findViewById(R.id.breathRateMeasure);
        resp_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProgressDialogWithTitle("Calculating Respiratory Rate!");
                Intent intent = new Intent(MainActivity.this, AccelerometerSensorHandlerService.class);
                startService(intent);
            }
        });

        Button sym_btn = (Button)findViewById(R.id.symptomsMeasure);
        sym_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(MainActivity.this, SymptomsActivity.class);
                intent.putExtra("symptom", symptoms);
                startActivity(intent);
            }
        });
    }

    void configurePermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}
                        , 10);
            }
            return;
        }
    }

    protected void onActivityResult(int requestCode,
                                    int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VIDEO_CAPTURE) {
            if (resultCode == RESULT_OK )
            {
                File videoFileName;
                FileInputStream inputStream = null;
                OutputStream outputStream = null;
                AssetFileDescriptor assetFileDescriptor = null;

                try {
                    assetFileDescriptor = getContentResolver().openAssetFileDescriptor(data.getData(), "r");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                try {
                    inputStream = assetFileDescriptor.createInputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                File dir = new File(folder_path);
                if (!dir.exists())
                {
                    dir.mkdirs();
                }

                videoFileName = new File(dir, videoName);

                if (videoFileName.exists()) {
                    videoFileName.delete();
                }

                try {
                    outputStream = new FileOutputStream(videoFileName);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                byte[] buf = new byte[1024];
                int len;

                while (true) {
                    try
                    {
                        if (((len = inputStream.read( buf)) > 0))
                        {
                            outputStream.write(buf, 0, len);
                        }
                        else
                        {
                            inputStream.close();
                            outputStream.close();
                            break;
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

                // Function to convert video to avi for processing the heart rate
                convertVideoCommands();

                Toast.makeText(this, "Video has been saved to:\n" +
                        data.getData(), Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Video recording cancelled.",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Failed to record video",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    public void convertVideoCommands()
    {
        FFmpeg ffmpeg = FFmpeg.getInstance(this);
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {

                @Override
                public void onStart() {
                }

                @Override
                public void onFailure() {
                }

                @Override
                public void onSuccess() {
                }

                @Override
                public void onFinish() {
                }
            });
        } catch (FFmpegNotSupportedException e) {
        }

        File newfile = new File(folder_path + mpjegName);

        if (newfile.exists()) {
            newfile.delete();
        }

        try {
            ffmpeg.execute(new String[]{"-i", folder_path + videoName, "-vcodec", "mjpeg", folder_path + mpjegName}, new ExecuteBinaryResponseHandler() {

                @Override
                public void onStart()
                {
                    showProgressDialogWithTitle("Measuring Heart Rate");
                }

                @Override
                public void onProgress(String message) {
                }

                @Override
                public void onFailure(String message) {
                }

                @Override
                public void onSuccess(String message) {
                }

                @Override
                public void onFinish() {
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
        }

        File avi_newfile = new File(folder_path + aviName);

        if (avi_newfile.exists()) {
            avi_newfile.delete();
        }
        try {
            ffmpeg.execute(new String[]{"-i", folder_path + mpjegName, "-vcodec", "mjpeg", folder_path + aviName}, new ExecuteBinaryResponseHandler() {
                @Override
                public void onStart() {
                }

                @Override
                public void onProgress(String message) {
                }

                @Override
                public void onFailure(String message) {
                }

                @Override
                public void onSuccess(String message) {
                }

                @Override
                public void onFinish()
                {

                    while(true)
                    {
                        try {
                            String heartRate = cameraActivity.measureHeartRate(folder_path, aviName);
                            if (heartRate != "" )
                            {
                                hr = Float.parseFloat(heartRate);
                                heartRate1.setText("The Heart Rate is: " + heartRate + "\n");
                                hideProgressDialogWithTitle();
                                break;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

        } catch (FFmpegCommandAlreadyRunningException e) {
        }

    }


    //Function to show the Processing Dialog box
    private void showProgressDialogWithTitle(String substring) {
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(substring);
        progressDialog.show();
    }


    // Function to hide the processing dialog box
    private void hideProgressDialogWithTitle() {
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.dismiss();
    }


    // Receives the final respiratory rate sent by the Accelerometer service class
    private BroadcastReceiver bReceiver = new BroadcastReceiver(){

        @SuppressLint("SetTextI18n")
        @Override
        public void onReceive(Context context, Intent intent) {

            // Display the respiratory rate
            float output = intent.getFloatExtra("success", 0.0f);
            Log.d("Output", String.valueOf(output));
            hideProgressDialogWithTitle();
            respRate.setText("Respiration rate : "+String.valueOf(output));
            symptoms.put("Respiration Rate", output);


        }
    };

    protected void onResume(){
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(bReceiver, new IntentFilter("message"));
    }

    protected void onPause (){
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(bReceiver);
    }

}
