
package com.letsface.simplecamera;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

public class CameraActivity extends Activity {

    private CameraHolder mCameraHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_camera);

        mCameraHolder = new CameraHolder();
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
