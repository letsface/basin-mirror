
package com.letsface.simplecamera.sample;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioGroup;

import com.letsface.simplecamera.CameraActivity;

public class SimpleCameraSampleActivity extends Activity implements OnClickListener {

    private static final int REQ_CAPTURE = 1000;

    private RadioGroup mCameraSelect;
    private CheckBox mConfirmCheck;
    private ImageView mPreview, mImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCameraSelect = (RadioGroup) findViewById(R.id.camera_select);
        mConfirmCheck = (CheckBox) findViewById(R.id.confirm);
        mPreview = (ImageView) findViewById(R.id.preview);
        mImage = (ImageView) findViewById(R.id.image);

        findViewById(R.id.action_capture).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent intent = new CameraActivity.IntentBuilder(this)
                .setUseFrontCamera(mCameraSelect.getCheckedRadioButtonId() == R.id.front_camera)
                .setConfirm(mConfirmCheck.isChecked())
                .build();
        startActivityForResult(intent, REQ_CAPTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQ_CAPTURE:
                if (resultCode == RESULT_OK) {
                    loadImage(data);
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void loadImage(Intent data) {
        Bitmap bmp = data.getParcelableExtra("data");
        if (bmp != null) {
            mPreview.setImageBitmap(bmp);
        }
        Uri uri = data.getParcelableExtra(MediaStore.EXTRA_OUTPUT);
        if (uri != null) {
            mImage.setImageURI(uri);
            int rotation = CameraActivity.getPictureRotation(uri);
            mImage.setRotation(rotation);
        }
    }

}
