package me.ashish.mankyscannerviewtest;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "CAMERA";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLog.getInstance().MyLog(TAG, "onCreate", "called");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();
        } else {
            MankyScannerView mankyScannerView = new MankyScannerView(this);
            setContentView(mankyScannerView);
            mankyScannerView.startCamera();
            mLog.getInstance().MyLog(TAG, "STARTING CAMERA", "called");
        }


    }

    private void requestCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MankyScannerView mankyScannerView = new MankyScannerView(this);
        setContentView(mankyScannerView);
        mankyScannerView.startCamera();
    }
}
