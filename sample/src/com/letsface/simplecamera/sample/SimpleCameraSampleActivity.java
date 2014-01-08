
package com.letsface.simplecamera.sample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.letsface.simplecamera.CameraActivity;

public class SimpleCameraSampleActivity extends Activity implements OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.back_camera).setOnClickListener(this);
        findViewById(R.id.front_camera).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(this, CameraActivity.class);

        switch (view.getId()) {
            case R.id.front_camera:
                intent.putExtra(CameraActivity.EXTRA_CAMERA, CameraActivity.CAMERA_FRONT);
                break;
            case R.id.back_camera:
                intent.putExtra(CameraActivity.EXTRA_CAMERA, CameraActivity.CAMERA_BACK);
                break;
        }

        startActivity(intent);
    }

}
