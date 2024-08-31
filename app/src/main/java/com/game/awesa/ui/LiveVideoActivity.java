package com.game.awesa.ui;


import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.game.awesa.R;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LiveVideoActivity extends AppCompatActivity {

    private static final String TAG = "VideoStreaming";
    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private static final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private SurfaceView surfaceView;
    private MediaRecorder mediaRecorder;
    private File videoFile;
    private ExecutorService uploadExecutor;
    private boolean isRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_video);

        surfaceView = findViewById(R.id.surfaceView);
        Button startRecordingButton = findViewById(R.id.startRecordingButton);
        Button stopRecordingButton = findViewById(R.id.stopRecordingButton);

        uploadExecutor = Executors.newSingleThreadExecutor();

        if (allPermissionsGranted()) {
            setupSurfaceHolder();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        startRecordingButton.setOnClickListener(v -> startRecording());
        stopRecordingButton.setOnClickListener(v -> stopRecording());
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void setupSurfaceHolder() {
        SurfaceHolder holder = surfaceView.getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
                setupMediaRecorder(surfaceHolder);
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
                if (mediaRecorder != null) {
                    mediaRecorder.release();
                    mediaRecorder = null;
                }
            }
        });
    }

    private void setupMediaRecorder(SurfaceHolder holder) {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setPreviewDisplay(holder.getSurface());
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        videoFile = new File(getExternalFilesDir(null), "video.mp4");
        mediaRecorder.setOutputFile(videoFile.getAbsolutePath());
        mediaRecorder.setVideoFrameRate(30);
        mediaRecorder.setVideoSize(1920, 1080);
        mediaRecorder.setVideoEncodingBitRate(10000000);
    }

    private void startRecording() {
        if (mediaRecorder != null) {
            try {
                mediaRecorder.prepare();
                mediaRecorder.start();
                isRecording = true;
                Toast.makeText(this, "Recording started", Toast.LENGTH_SHORT).show();
                uploadVideo();
            } catch (IOException e) {
                Log.e(TAG, "Error starting media recorder", e);
            }
        }else{
            Toast.makeText(this, "failed Recording start", Toast.LENGTH_SHORT).show();

        }
    }

    private void stopRecording() {
        if (isRecording) {
            mediaRecorder.stop();
            mediaRecorder.reset();
            setupMediaRecorder(surfaceView.getHolder());
            isRecording = false;
            Toast.makeText(this, "Recording stopped", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadVideo() {
        uploadExecutor.execute(() -> {
            OkHttpClient client = new OkHttpClient();
            try {
                while (isRecording) {
                    // Delay to simulate real-time upload
                    Thread.sleep(5000);

                    // Upload video file to server
                    RequestBody videoFileBody = RequestBody.create(videoFile, MediaType.parse("video/mp4"));
                    MultipartBody.Part videoPart = MultipartBody.Part.createFormData("video", videoFile.getName(), videoFileBody);


                    MultipartBody requestBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addPart(videoPart)
                            .build();

                    Request request = new Request.Builder()
                            .url("https://sportapp.boonoserver.de/restapi/upload-video")
                            .post(requestBody)
                            .build();
                    /*
                    Request request = new Request.Builder()
                            .url("https://yourserver.com/upload")
                            .post(videoPart)
                            .build();*/

                    try{
                        // Execute the request
                        client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                e.printStackTrace();
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                if (response.isSuccessful()) {
                                    System.out.println("Video uploaded successfully");
                                } else {
                                    System.out.println("Video upload failed");
                                }
                            }
                        });
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                   /* try (Response response = client.newCall(request).execute()) {
                        if (!response.isSuccessful()) {
                            Log.e(TAG, "Upload failed: " + response.message());
                        } else {
                            Log.i(TAG, "Upload successful: " + response.message());
                        }
                    }*/
                }
            } catch (Exception e) {
                Log.e(TAG, "Error uploading video", e);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (uploadExecutor != null) {
            uploadExecutor.shutdown();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                setupSurfaceHolder();
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}
