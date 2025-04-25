package com.example.pharmacyl3;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;
import android.Manifest;
import android.content.pm.PackageManager;
import java.io.IOException;
import java.util.List;

public class BarcodeScannerActivity extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private CameraSourcePreview cameraPreview;
    private Camera camera;
    private boolean isProcessingFrame = false;
    private BarcodeScanner barcodeScanner;
    private boolean hasShownResult = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_scanner);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        toolbar.setBackgroundColor(getResources().getColor(R.color.primaryGreen));

        View overlay = findViewById(R.id.barcode_overlay);
        if (overlay != null) {
            overlay.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        }

        cameraPreview = findViewById(R.id.camera_preview);
        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                        Barcode.FORMAT_ALL_FORMATS
                )
                .build();
        barcodeScanner = BarcodeScanning.getClient(options);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            setupSurfaceCallback();
        }
    }

    private void setupSurfaceCallback() {
        SurfaceHolder holder = cameraPreview.getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                startCamera(holder);
            }
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}
            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                if (camera != null) {
                    camera.stopPreview();
                    camera.setPreviewCallback(null);
                    camera.release();
                    camera = null;
                }
            }
        });
    }

    private void startCamera(SurfaceHolder holder) {
        try {
            camera = Camera.open();
            camera.setDisplayOrientation(90);
            Camera.Parameters params = camera.getParameters();
            // Check if FOCUS_MODE_CONTINUOUS_PICTURE is supported
            List<String> focusModes = params.getSupportedFocusModes();
            if (focusModes != null && focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            }
            params.setPreviewFormat(ImageFormat.NV21);
            camera.setParameters(params);
            camera.setPreviewDisplay(holder);
            camera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    if (isProcessingFrame || hasShownResult) return;
                    isProcessingFrame = true;
                    Camera.Size size = camera.getParameters().getPreviewSize();
                    InputImage image = InputImage.fromByteArray(
                            data,
                            size.width,
                            size.height,
                            90,
                            InputImage.IMAGE_FORMAT_NV21
                    );
                    barcodeScanner.process(image)
                            .addOnSuccessListener(barcodes -> {
                                if (!barcodes.isEmpty() && !hasShownResult) {
                                    hasShownResult = true;
                                    handleScanResult(barcodes.get(0).getRawValue());
                                }
                                isProcessingFrame = false;
                            })
                            .addOnFailureListener(e -> {
                                isProcessingFrame = false;
                            });
                }
            });
            camera.startPreview();
        } catch (IOException | RuntimeException e) {
            Log.e("BarcodeScanner", "Camera preview error", e);
            Toast.makeText(this, "Unable to start camera: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupSurfaceCallback();
            } else {
                Toast.makeText(this, "Camera permission is required to scan barcodes", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.release();
            camera = null;
        }
        if (barcodeScanner != null) {
            barcodeScanner.close();
        }
        hasShownResult = false;
        super.onDestroy();
    }

    private void handleScanResult(String barcode) {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Scan Result")
                .setMessage(barcode)
                .setPositiveButton("OK", (dialog, which) -> finish())
                .setOnDismissListener(dialog -> finish())
                .show();
    }
}
