
package com.letsface.simplecamera.sample;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.letsface.simplecamera.CameraActivity;

public class SimpleCameraSampleActivity extends Activity implements OnClickListener,
        OnCheckedChangeListener {

    private RadioGroup mCameraSelect, mCameraActivitySelect;
    private CheckBox mConfirmCheck;
    private ImageView mPreview, mImage;
    private View mBuiltinOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCameraActivitySelect = (RadioGroup) findViewById(R.id.camera_activity_select);
        mBuiltinOptions = findViewById(R.id.built_in_options);
        mCameraSelect = (RadioGroup) findViewById(R.id.camera_select);
        mConfirmCheck = (CheckBox) findViewById(R.id.confirm);
        mPreview = (ImageView) findViewById(R.id.preview);
        mImage = (ImageView) findViewById(R.id.image);

        mCameraActivitySelect.setOnCheckedChangeListener(this);
        findViewById(R.id.action_capture).setOnClickListener(this);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        mBuiltinOptions.setVisibility(checkedId == R.id.built_in_camera ? View.VISIBLE
                : View.INVISIBLE);
    }

    @Override
    public void onClick(View view) {
        new CameraActivity.IntentBuilder()
                .setUseFrontCamera(mCameraSelect.getCheckedRadioButtonId() == R.id.front_camera)
                .setConfirm(mConfirmCheck.isChecked())
                .setDesiredImageHeight(480)
                .setUseSystemCamera(
                        mCameraActivitySelect.getCheckedRadioButtonId() == R.id.system_camera)
                .start(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        CameraActivity.IntentResult result = CameraActivity.IntentResult.parse(requestCode,
                resultCode, data);
        if (result != null) {
            loadImage(result);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void loadImage(CameraActivity.IntentResult result) {
        Bitmap bmp = result.getPreviewImage();
        if (bmp != null) {
            mPreview.setImageBitmap(bmp);
        }
        Uri uri = result.getImageUri();
        if (uri != null) {
            mImage.setImageURI(uri);
        }
    }

}
