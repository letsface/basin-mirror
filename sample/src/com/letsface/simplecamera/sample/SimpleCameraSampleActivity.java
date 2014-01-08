
package com.letsface.simplecamera.sample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.letsface.simplecamera.CameraActivity;

public class SimpleCameraSampleActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        startActivity(new Intent(this, CameraActivity.class));
        finish();
    }

}
