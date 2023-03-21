package com.group7.gallerium.activities;

import android.content.ContentValues;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Button;
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
import androidx.camera.video.Quality;
import androidx.camera.video.QualitySelector;
import androidx.camera.video.Recorder;
import androidx.camera.video.VideoCapture;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.group7.gallerium.R;

import java.util.concurrent.ExecutionException;

public class CameraActivity extends AppCompatActivity {
    private PreviewView previewView;
    private Preview preview;
    private ProcessCameraProvider processCameraProvider;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private Button btnCapture, btnSwitch, btnFlash;
    private int flashMode = ImageCapture.FLASH_MODE_AUTO;
    private CameraSelector lensFacing = CameraSelector.DEFAULT_FRONT_CAMERA;
    private ImageAnalysis imageAnalysis;
    private OrientationEventListener orientationEventListener;
    private ScaleGestureDetector.OnScaleGestureListener listener;
    private ScaleGestureDetector scaleGestureDetector;
    private ImageCapture imageCapture;
    private VideoCapture videoCapture;
    private Recorder recorder;
    private Camera cam;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        lensFacing = CameraSelector.DEFAULT_BACK_CAMERA;
        previewView = findViewById(R.id.previewView);
        btnCapture = findViewById(R.id.capture_button);
        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                captureImage();
            }
        });
        btnSwitch = findViewById(R.id.camera_switch);
        btnSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flipCamera();
            }
        });
        btnFlash = findViewById(R.id.flash);
        btnFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flashOption();
            }
        });
        startCamera();
    }

    private void startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    processCameraProvider = cameraProviderFuture.get();
                    bindImageAnalysis(processCameraProvider);
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }


    private void flipCamera() {
        if (lensFacing == CameraSelector.DEFAULT_FRONT_CAMERA)
        {
            lensFacing = CameraSelector.DEFAULT_BACK_CAMERA;
            btnFlash.setEnabled(false);
        }
        else if (lensFacing == CameraSelector.DEFAULT_BACK_CAMERA)
        {
            lensFacing = CameraSelector.DEFAULT_FRONT_CAMERA;
            btnFlash.setEnabled(true);
        }
        startCamera();
    }

    private void flashOption() {
        if (flashMode == ImageCapture.FLASH_MODE_AUTO) {
            flashMode = ImageCapture.FLASH_MODE_ON;
            btnFlash.setBackground(AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_flash_on));
        } else if (flashMode == ImageCapture.FLASH_MODE_ON) {
            flashMode = ImageCapture.FLASH_MODE_OFF;
            btnFlash.setBackground(AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_flash_off));
        } else if (flashMode == ImageCapture.FLASH_MODE_OFF) {
            flashMode = ImageCapture.FLASH_MODE_AUTO;
            btnFlash.setBackground(AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_flash_auto));
        }
        processCameraProvider.unbind(imageCapture);
        imageCapture.setFlashMode(flashMode);
        processCameraProvider.bindToLifecycle(this, lensFacing, imageCapture);
    }


    private void bindImageAnalysis(@NonNull ProcessCameraProvider cameraProvider) {
        imageAnalysis =
                new ImageAnalysis.Builder().setTargetResolution(new Size(1280, 720))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();
        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), new ImageAnalysis.Analyzer() {
            @Override
            public void analyze(@NonNull ImageProxy image) {
                image.close();
            }
        });
        orientationEventListener = new OrientationEventListener(this) {
            @Override
            public void onOrientationChanged(int orientation) {

            }
        };

        listener = new ScaleGestureDetector.OnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
                ZoomState f = cam.getCameraInfo().getZoomState().getValue();
                if (f!=null) Log.d("Zoom", String.valueOf(f.getZoomRatio()));

                float scale = scaleGestureDetector.getScaleFactor();
                if (f!=null) cam.getCameraControl().setZoomRatio(Math.max(f.getMinZoomRatio(), Math.min(f.getMaxZoomRatio(), scale * f.getZoomRatio())));
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

        previewView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                view.performClick();
                return scaleGestureDetector.onTouchEvent(motionEvent);
            }
        });

        previewView.setScaleType(PreviewView.ScaleType.FIT_CENTER);
        orientationEventListener.enable();
        preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        imageCapture =
                new ImageCapture.Builder()
                        .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation())
                        .setFlashMode(flashMode)
                        .setJpegQuality(100)
                        .build();
        recorder = new Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HD))
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
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/G-Camera");
            }

        // Create output options object which contains file + metadata
        var outputOptions = new ImageCapture.OutputFileOptions.Builder(getContentResolver(),
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues)
                .build();

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(ImageCapture.OutputFileResults outputFileResults) {
                        Toast.makeText(getApplicationContext(), "Captured: " + outputFileResults.getSavedUri(), Toast.LENGTH_SHORT).show();
                        Log.d("G-capture", "Photo capture succeeded: " + outputFileResults.getSavedUri());
                    }
                    @Override
                    public void onError(ImageCaptureException error) {
                        Log.e("G-capture", "Photo capture failed: " + error.getMessage());
                    }
                }
        );
    }

    private void captureVideo() {

    }

}