
package com.letsface.simplecamera;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class CameraActivity extends Activity implements OnClickListener {

    private static final int REQ_CONFIRM_PICTURE = 1000;

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

        findViewById(R.id.action_capture).setOnClickListener(this);
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

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.action_capture) {
            takePhoto();
        }
    }

    private void takePhoto() {
        Camera camera = mCameraHolder.getCamera();
        if (camera == null)
            return;
        camera.takePicture(null, null, mPictureCallback);
    }

    private byte[] mPictureData;

    private final PictureCallback mPictureCallback = new PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            mPictureData = data;
            showConfirmActivity();
        }

    };

    private Bitmap getPreviewPicture() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int targetW = dm.widthPixels;
        int targetH = dm.heightPixels;

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(mPictureData, 0, mPictureData.length, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeByteArray(mPictureData, 0, mPictureData.length,
                bmOptions);
        return bitmap;
    }

    private void showConfirmActivity() {
        Bitmap bitmap = getPreviewPicture();
        Intent intent = new Intent(this, PictureConfirmActivity.class);
        intent.putExtra(PictureConfirmActivity.EXTRA_IMAGE, bitmap);
        startActivityForResult(intent, REQ_CONFIRM_PICTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQ_CONFIRM_PICTURE:
                if (resultCode == RESULT_OK) {
                    confirmed();
                } else {
                    retake();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void confirmed() {
        Toast.makeText(this, "confirmed", Toast.LENGTH_LONG).show();
    }

    private void retake() {
        Toast.makeText(this, "retake", Toast.LENGTH_LONG).show();
    }

}
