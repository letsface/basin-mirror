
package com.letsface.simplecamera;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

public class CameraActivity extends Activity {

    public static final String EXTRA_CAMERA = "extra_camera";
    public static final String CAMERA_BACK = "back";
    public static final String CAMERA_FRONT = "front";

    private CameraHolder mCameraHolder;

    private int getIntentCameraId() {
        Intent intent = getIntent();
        String camera = intent.getStringExtra(EXTRA_CAMERA);
        if (CAMERA_FRONT.equals(camera)) {
            return Camera.CameraInfo.CAMERA_FACING_FRONT;
        }
        return Camera.CameraInfo.CAMERA_FACING_BACK;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_camera);

        mCameraHolder = new CameraHolder();
        mCameraHolder.setCameraId(getIntentCameraId());

        CameraPreview preview = new CameraPreview(this);
        preview.setCameraHolder(mCameraHolder);

        ((ViewGroup) findViewById(R.id.preview)).addView(preview);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCameraHolder.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCameraHolder.stop();
    }

}
