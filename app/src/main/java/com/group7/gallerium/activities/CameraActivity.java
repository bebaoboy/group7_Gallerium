package com.group7.gallerium.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaMetadataEditor;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.view.OrientationEventListener;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.ZoomState;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.FallbackStrategy;
import androidx.camera.video.FileDescriptorOutputOptions;
import androidx.camera.video.FileOutputOptions;
import androidx.camera.video.MediaStoreOutputOptions;
import androidx.camera.video.OutputOptions;
import androidx.camera.video.Quality;
import androidx.camera.video.QualitySelector;
import androidx.camera.video.Recorder;
import androidx.camera.video.VideoCapture;
import androidx.camera.video.Recording;
import androidx.camera.video.RecordingStats;
import androidx.camera.video.VideoRecordEvent;
import androidx.camera.view.PreviewView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.group7.gallerium.R;

import java.io.File;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

@SuppressWarnings("rawtypes")
public class CameraActivity extends AppCompatActivity {
    private PreviewView previewView, videoPreviewView;
    private Preview preview;
    private ProcessCameraProvider processCameraProvider;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private Button btnCapture, btnSwitch, btnFlash, btnVideo, btnPause, btnStop, btnSwitchVideo, btnFlashVideo;
    private int flashMode = ImageCapture.FLASH_MODE_AUTO;
    private CameraSelector lensFacing = CameraSelector.DEFAULT_FRONT_CAMERA;
    private ImageAnalysis imageAnalysis;
    private OrientationEventListener orientationEventListener;
    private ScaleGestureDetector.OnScaleGestureListener listener;
    private ScaleGestureDetector scaleGestureDetector;
    private ImageCapture imageCapture;
    private VideoCapture videoCapture;
    private Recording recording;
    private Recorder recorder;
    Camera cam;
    private boolean isRecording = false, isVideo = false;
    private ConstraintLayout captureLayout, videoLayout;
    private TextView duration;
    private LocationManager locationManager;
    private Location currentLocation;
    LocationListener mLocationListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        lensFacing = CameraSelector.DEFAULT_BACK_CAMERA;
        previewView = findViewById(R.id.previewView);
        videoPreviewView = findViewById(R.id.video_previewView);
        captureLayout = findViewById(R.id.capture_layout);
        videoLayout = findViewById(R.id.video_layout);
        videoLayout.setVisibility(View.GONE);
        captureLayout.setVisibility(View.VISIBLE);
        btnCapture = findViewById(R.id.capture_button);
        btnPause = findViewById(R.id.video_camera_pause);
        btnVideo = findViewById(R.id.video_capture_button);
        btnStop = findViewById(R.id.video_camera_stop);
        duration = findViewById(R.id.duration);
        btnCapture.setOnClickListener(view -> captureImage());
        btnCapture.setOnLongClickListener(view -> {
            captureLayout.setVisibility(View.GONE);
            videoLayout.setVisibility(View.VISIBLE);
            isVideo = true;
            flashMode = ImageCapture.FLASH_MODE_OFF;
            startCamera();
            return true;
        });
        btnVideo.setOnClickListener(view -> captureVideo());
        btnVideo.setOnLongClickListener(view -> {
            captureLayout.setVisibility(View.VISIBLE);
            videoLayout.setVisibility(View.GONE);
            isVideo = false;
            flashMode = ImageCapture.FLASH_MODE_AUTO;
            startCamera();
            return true;
        });
        btnSwitch = findViewById(R.id.camera_switch);
        btnSwitch.setOnClickListener(view -> flipCamera());
        btnFlash = findViewById(R.id.flash);
        btnFlash.setOnClickListener(view -> flashOption());
        btnSwitchVideo = findViewById(R.id.video_camera_switch);
        btnSwitchVideo.setOnClickListener(view -> flipCamera());
        btnFlashVideo = findViewById(R.id.video_flash);
        btnFlashVideo.setOnClickListener(view -> flashOption());
        btnPause.setVisibility(View.GONE);
        btnPause.setOnClickListener(view -> pause());
        btnStop.setVisibility(View.GONE);
        btnStop.setOnClickListener(view -> stop());

        // Get LocationManager object from System Service LOCATION_SERVICE
        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        } catch (Exception e) {
            // currentLocation = null;
        }

        startCamera();
    }

    private void startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                processCameraProvider = cameraProviderFuture.get();
                bindImageAnalysis(processCameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }


    private void flipCamera() {
        if (lensFacing == CameraSelector.DEFAULT_FRONT_CAMERA) {
            lensFacing = CameraSelector.DEFAULT_BACK_CAMERA;
            btnFlash.setEnabled(true);
        } else if (lensFacing == CameraSelector.DEFAULT_BACK_CAMERA) {
            lensFacing = CameraSelector.DEFAULT_FRONT_CAMERA;
            btnFlash.setEnabled(false);
        }
        startCamera();
    }

    private void flashOption() {
        if (flashMode == ImageCapture.FLASH_MODE_AUTO) {
            flashMode = ImageCapture.FLASH_MODE_ON;
            btnFlash.setBackground(AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_flash_on));
            btnFlashVideo.setBackground(AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_flash_on));
        } else if (flashMode == ImageCapture.FLASH_MODE_ON) {
            flashMode = ImageCapture.FLASH_MODE_OFF;
            btnFlash.setBackground(AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_flash_off));
            btnFlashVideo.setBackground(AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_flash_off));
        } else if (flashMode == ImageCapture.FLASH_MODE_OFF) {
            if (isVideo) {
                flashMode = ImageCapture.FLASH_MODE_ON;
                btnFlashVideo.setBackground(AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_flash_on));
            } else {
                flashMode = ImageCapture.FLASH_MODE_AUTO;
                btnFlashVideo.setBackground(AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_flash_auto));
            }
            btnFlash.setBackground(AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_flash_auto));
        }

        if (isVideo) {
            cam.getCameraControl().enableTorch(flashMode != ImageCapture.FLASH_MODE_OFF);
        }
        processCameraProvider.unbind(imageCapture);
        imageCapture.setFlashMode(flashMode);
        processCameraProvider.bindToLifecycle(this, lensFacing, imageCapture, videoCapture,
                preview);
    }


    @SuppressLint("RestrictedApi")
    private void bindImageAnalysis(@NonNull ProcessCameraProvider cameraProvider) {
        imageAnalysis =
                new ImageAnalysis.Builder().setTargetResolution(new Size(1280, 720))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();
        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), ImageProxy::close);
        orientationEventListener = new OrientationEventListener(this) {
            @Override
            public void onOrientationChanged(int orientation) {

            }
        };

        listener = new ScaleGestureDetector.OnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
                ZoomState f = cam.getCameraInfo().getZoomState().getValue();
                if (f != null) Log.d("Zoom", String.valueOf(f.getZoomRatio()));

                float scale = scaleGestureDetector.getScaleFactor();
                if (f != null)
                    cam.getCameraControl().setZoomRatio(Math.max(f.getMinZoomRatio(), Math.min(f.getMaxZoomRatio(), scale * f.getZoomRatio())));
                return true;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
            }
        };
        scaleGestureDetector = new ScaleGestureDetector(getApplicationContext(), listener);

        orientationEventListener.enable();
        if (!isVideo) {
            previewView.setOnTouchListener((view, motionEvent) -> {
                view.performClick();
                return scaleGestureDetector.onTouchEvent(motionEvent);
            });

            previewView.setScaleType(PreviewView.ScaleType.FIT_CENTER);
            preview = new Preview.Builder().build();
            preview.setSurfaceProvider(previewView.getSurfaceProvider());
        } else {
            videoPreviewView.setOnTouchListener((view, motionEvent) -> {
                view.performClick();
                return scaleGestureDetector.onTouchEvent(motionEvent);
            });

            videoPreviewView.setScaleType(PreviewView.ScaleType.FIT_CENTER);
            preview = new Preview.Builder().build();
            preview.setSurfaceProvider(videoPreviewView.getSurfaceProvider());
        }
        imageCapture =
                new ImageCapture.Builder()
                        .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation())
                        .setFlashMode(flashMode)
                        .setJpegQuality(100)
                        .build();
        var qualitySelector = QualitySelector.fromOrderedList(
                Arrays.asList(Quality.HD, Quality.SD),
                FallbackStrategy.lowerQualityThan(Quality.HD));
        recorder = new Recorder.Builder()
                .setQualitySelector(qualitySelector)
                .build();
        videoCapture = VideoCapture.withOutput(recorder);

        cameraProvider.unbindAll();
        cam = cameraProvider.bindToLifecycle(this, lensFacing, imageCapture, videoCapture,
                preview);
    }

    public void captureImage() {
        var name = "G-img-" + System.currentTimeMillis();
        var contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/G-Camera");
        }
        var metaData = new ImageCapture.Metadata();
        metaData.setLocation(currentLocation);
        // Create output options object which contains file + metadata
        var outputOptions = new ImageCapture.OutputFileOptions.Builder(getContentResolver(),
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues).setMetadata(metaData)
                .build();

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Toast.makeText(getApplicationContext(), "Captured: " + outputFileResults.getSavedUri(), Toast.LENGTH_SHORT).show();
                        Log.d("G-capture", "Photo capture succeeded: " + outputFileResults.getSavedUri());
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException error) {
                        Log.e("G-capture", "Photo capture failed: " + error.getMessage());
                    }
                }
        );
    }

    @SuppressLint("RestrictedApi")
    private void captureVideo() {
        isRecording = true;
        btnVideo.setVisibility(View.GONE);
        btnPause.setVisibility(View.VISIBLE);
        btnStop.setVisibility(View.VISIBLE);
        btnSwitchVideo.setEnabled(false);
        btnFlashVideo.setEnabled(false);

        var name = "G-vid-" + System.currentTimeMillis();
        var contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
//        contentValues.put(MediaStore.Video.VideoColumns.LATITUDE, currentLocation.getLatitude());
//        contentValues.put(MediaStore.Video.VideoColumns.LONGITUDE, currentLocation.getLongitude());
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            contentValues.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/G-Camera");
        }

        var mediaStoreOutput = new MediaStoreOutputOptions.Builder(
                getContentResolver(),
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                .setContentValues(contentValues).setFileSizeLimit(524288000)
                .setLocation(currentLocation)
                .build();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            recording = recorder
                    .prepareRecording(getApplicationContext(), mediaStoreOutput)
                    .start(ContextCompat.getMainExecutor(this), videoRecordEvent -> {
                        RecordingStats recordingStats = videoRecordEvent.getRecordingStats();
                        if (videoRecordEvent instanceof VideoRecordEvent.Start) {
                            // Handle the start of a new active recording              ...
                        } else if (videoRecordEvent instanceof VideoRecordEvent.Pause) {
                            // Handle the case where the active recording is paused              ...
                        } else if (videoRecordEvent instanceof VideoRecordEvent.Resume) {
                            // Handles the case where the active recording is resumed              ...
                        } else if (videoRecordEvent instanceof VideoRecordEvent.Finalize) {
                            VideoRecordEvent.Finalize finalizeEvent = (VideoRecordEvent.Finalize) videoRecordEvent;
                            duration.setText("00:00:00");
                            // Handles a finalize event for the active recording, checking Finalize.getError()
                            int error = finalizeEvent.getError();
                            if (error != VideoRecordEvent.Finalize.ERROR_NONE) {

                            }
                        }
                        String newDuration;
                        long duration = recordingStats.getRecordedDurationNanos() / 1000000000;
                        var hours = duration / 3600;
                        var minutes = (duration % 3600) / 60;
                        var seconds = duration % 60;

                        newDuration = String.format(Locale.CHINA,"%02d:%02d:%02d", hours, minutes, seconds);
                        Log.d("viddur", newDuration);
                        this.duration.setText(newDuration);
                    });

        }
        else {
            recording = recorder
                    .prepareRecording(getApplicationContext(), mediaStoreOutput)
                    .withAudioEnabled()
                    .start(ContextCompat.getMainExecutor(this), videoRecordEvent -> {
                        RecordingStats recordingStats = videoRecordEvent.getRecordingStats();
                        if (videoRecordEvent instanceof VideoRecordEvent.Start) {
                            // Handle the start of a new active recording              ...
                        } else if (videoRecordEvent instanceof VideoRecordEvent.Pause) {
                            // Handle the case where the active recording is paused              ...
                        } else if (videoRecordEvent instanceof VideoRecordEvent.Resume) {
                            // Handles the case where the active recording is resumed              ...
                        } else if (videoRecordEvent instanceof VideoRecordEvent.Finalize) {
                            VideoRecordEvent.Finalize finalizeEvent = (VideoRecordEvent.Finalize) videoRecordEvent;
                            duration.setText("00:00:00");
                            // Handles a finalize event for the active recording, checking Finalize.getError()
                            int error = finalizeEvent.getError();
                            if (error != VideoRecordEvent.Finalize.ERROR_NONE) {

                            }
                        }
                        String newDuration;
                        long duration = recordingStats.getRecordedDurationNanos() / 1000000000;
                        var hours = duration / 3600;
                        var minutes = (duration % 3600) / 60;
                        var seconds = duration % 60;

                        newDuration = String.format(Locale.CHINA,"%02d:%02d:%02d", hours, minutes, seconds);
                        Log.d("viddur", newDuration);
                        this.duration.setText(newDuration);
                    });
        }



            // All events, including VideoRecordEvent.Status, contain RecordingStats.
            // This can be used to update the UI or track the recording duration.
//            videoCapture.startRecording(outputOptions, ContextCompat.getMainExecutor(this),
//                    new VideoCapture.OnVideoSavedCallback() {
//                        @Override
//                        public void onVideoSaved(@NonNull VideoCapture.OutputFileResults outputFileResults) {
//                            Toast.makeText(getApplicationContext(), "Saving...", Toast.LENGTH_SHORT).show();
//                        }
//
//                        @Override
//                        public void onError(int videoCaptureError, @NonNull String message, @Nullable Throwable cause) {
//                            Toast.makeText(getApplicationContext(), "Error: " + message, Toast.LENGTH_SHORT).show();
//                        }
//                    }
//
//            );

    }

    private void pause() {
        if (isRecording) {
            isRecording = false;
            btnPause.setBackground(AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_play));
            recording.pause();
            // pause
        } else {
            isRecording = true;
            btnPause.setBackground(AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_pause));
            recording.resume();
            // resume
        }
    }

    private void stop() {
        recording.stop();
        isRecording = false;
        duration.setText("00:00:00");
        btnVideo.setBackground(AppCompatResources.getDrawable(getApplicationContext(), R.drawable.round_button));
        btnPause.setVisibility(View.GONE);
        btnStop.setVisibility(View.GONE);
        btnVideo.setVisibility(View.VISIBLE);
        btnSwitchVideo.setEnabled(true);
        btnFlashVideo.setEnabled(true);
    }

    @Override
    protected void onPause() {
        if (isRecording)
        {
            stop();
        }
        locationManager.removeUpdates(mLocationListener);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isRecording) recording.resume();
        // Create a criteria object to retrieve provider
        Criteria criteria = new Criteria();

        // Get the name of the best provider
        String provider = locationManager.getBestProvider(criteria, true);

        mLocationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                //System.out.println("got new location");
                //Log.i("mLocationListener", "Got location");   // for logCat should ->  import android.util.Log;

                // Stops the new update requests.
                locationManager.removeUpdates(mLocationListener);
                currentLocation = location;
            }

            public void onStatusChanged(java.lang.String s, int i, android.os.Bundle bundle) {
            }

            public void onProviderEnabled(java.lang.String s) {
            }

            public void onProviderDisabled(java.lang.String s) {
            }

        };
        // (String) provider, time in milliseconds when to check for an update, distance to change in coordinates to request an update, LocationListener.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        } else {
            try {
                locationManager.requestLocationUpdates(provider, 500, 0.0f, mLocationListener);
            } catch (Exception e) {

            }
        }
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }
}