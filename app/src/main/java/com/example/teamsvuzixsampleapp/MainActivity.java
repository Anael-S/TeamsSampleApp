package com.example.teamsvuzixsampleapp;

import static android.widget.Toast.makeText;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.azure.android.communication.calling.CallClient;
import com.azure.android.communication.calling.CameraFacing;
import com.azure.android.communication.calling.CreateViewOptions;
import com.azure.android.communication.calling.DeviceManager;
import com.azure.android.communication.calling.LocalVideoStream;
import com.azure.android.communication.calling.ScalingMode;
import com.azure.android.communication.calling.VideoDeviceInfo;
import com.azure.android.communication.calling.VideoStreamRenderer;
import com.azure.android.communication.calling.VideoStreamRendererView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private DeviceManager deviceManager;
    private VideoDeviceInfo currentCamera;
    private LocalVideoStream currentVideoStream;

    VideoStreamRenderer previewRenderer;
    VideoStreamRendererView preview;

    LinearLayout localvideocontainer;

    public static final int PERMISSION_ALL = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        localvideocontainer = findViewById(R.id.localvideocontainer);
    }

    @Override
    protected void onResume() {
        super.onResume();
        handlePermissions();
    }

    public void handlePermissions() {
        String[] PERMISSIONS = {android.Manifest.permission.CAMERA};
        if (!hasPermissions(getApplicationContext(), PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        } else {
            startTeamsCallPreview();
        }
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ALL: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("RMTST", "Permissions granted > start teams preview");
                    startTeamsCallPreview();
                } else {
                    finish();
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void startTeamsCallPreview() {
        Activity activity = this;
        Log.d("RMTST", "startTeamsCallPreview");
        try {
            CallClient callClient = new CallClient();
            deviceManager = callClient.getDeviceManager(getApplicationContext()).get();
        } catch (Exception ex) {
            makeText(getApplicationContext(), "Failed to set device manager.", Toast.LENGTH_SHORT).show();
        }
        List<VideoDeviceInfo> cameras = deviceManager.getCameras();

        if (!cameras.isEmpty()) {
            currentCamera = getBackCameraOrNextAvailable(deviceManager);
            currentVideoStream = new LocalVideoStream(currentCamera, activity);

            LocalVideoStream[] videoStreams = new LocalVideoStream[1];
            videoStreams[0] = currentVideoStream;
            showPreview(currentVideoStream);
        } else {
            Toast.makeText(activity, "NO CAMERA FOUND!!", Toast.LENGTH_LONG).show();
            Log.e("RMTST", "startTeamsCallPreview > NO CAMERA ACCESS");
        }
    }

    private void showPreview(LocalVideoStream stream) {
        try {
            Log.d("RMTST", "Showing preview");
            // Create renderer
            previewRenderer = new VideoStreamRenderer(stream, this);
            preview = previewRenderer.createView(new CreateViewOptions(ScalingMode.FIT));
            preview.setTag(0);
            runOnUiThread(() -> {
                try {
                    localvideocontainer.addView(preview);
                } catch (Exception e) {
                    Log.e("RMTST", "Showing preview CRASHED 2");
                }
            });
        } catch (Exception e) {
            Log.e("RMTST", "Showing preview CRASHED");
            e.printStackTrace();
        }
    }

    public static VideoDeviceInfo getBackCameraOrNextAvailable(DeviceManager deviceManager) {
        List<VideoDeviceInfo> cameras = deviceManager.getCameras();
        for (VideoDeviceInfo camera : cameras) {
            if (camera.getCameraFacing() == CameraFacing.BACK) {
                return camera;
            }
        }
        return cameras.get(0);
    }
}