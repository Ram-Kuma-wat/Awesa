package com.game.awesa.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.game.awesa.R;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class VideoStreamingActivity extends AppCompatActivity implements SurfaceHolder.Callback, Camera.PreviewCallback {

    private static final String TAG = "VideoStreamingActivity";

    private static final int REQUEST_CAMERA_PERMISSION = 1001;
    private static final String SERVER_URL = "https://sportapp.boonoserver.de/restapi/upload-video"; // Replace with your WebSocket server URL

    private SurfaceView surfaceView;
    private Camera camera;
    private SurfaceHolder surfaceHolder;

    private Button btnStartStop;
    private boolean isStreaming = false;

    private WebSocket webSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_video);

        surfaceView = findViewById(R.id.surfaceView);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);

        btnStartStop = findViewById(R.id.btnStartStop);
        btnStartStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isStreaming) {
                    stopStreaming();
                } else {
                    startStreaming();
                }
            }
        });
    }

    private void startStreaming() {
        if (checkCameraPermission()) {
            initCamera();
            connectWebSocket();
            isStreaming = true;
            btnStartStop.setText("Stop");
        }
    }

    private void stopStreaming() {
        releaseCamera();
        disconnectWebSocket();
        isStreaming = false;
        btnStartStop.setText("Start");
    }

    private boolean checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            return false;
        }
        return true;
    }

    private void initCamera() {
        camera = Camera.open();
        Camera.Parameters parameters = camera.getParameters();
        List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
        Camera.Size selectedSize = sizes.get(0); // Choose the appropriate size based on your requirements
        parameters.setPreviewSize(selectedSize.width, selectedSize.height);
        parameters.setPreviewFormat(ImageFormat.NV21); // Format of the preview frames
        camera.setParameters(parameters);
        camera.setPreviewCallback(this);

        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void releaseCamera() {
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    private void connectWebSocket() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(SERVER_URL)
                .build();

        WebSocketListener webSocketListener = new WebSocketListener() {
            @Override
            public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
                super.onOpen(webSocket, response);
                Log.d(TAG, "WebSocket connected");
                VideoStreamingActivity.this.webSocket = webSocket;
            }

            @Override
            public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
                super.onMessage(webSocket, text);
                Log.d(TAG, "Received message: " + text);
            }

            @Override
            public void onMessage(@NotNull WebSocket webSocket, @NotNull ByteString bytes) {
                super.onMessage(webSocket, bytes);
                Log.d(TAG, "Received bytes: " + bytes.hex());
            }

            @Override
            public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, Response response) {
                super.onFailure(webSocket, t, response);
                Log.e(TAG, "WebSocket connection failed: " + t.getMessage());
            }
        };

        webSocket = client.newWebSocket(request, webSocketListener);
    }

    private void disconnectWebSocket() {
        if (webSocket != null) {
            webSocket.cancel();
            webSocket = null;
        }
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        // Surface created, setup camera preview
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        // Surface changed, handle camera preview size changes if necessary
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        // Surface destroyed, release camera resources if needed
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        // This method is called every time a new frame is available from the camera preview
        if (webSocket != null && webSocket.send(ByteString.of(data))) {
            // Successfully sent video frame to WebSocket server
        } else {
            Log.e(TAG, "Failed to send video frame");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startStreaming();
            } else {
                Log.e(TAG, "Camera permission denied");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopStreaming();
    }
}
