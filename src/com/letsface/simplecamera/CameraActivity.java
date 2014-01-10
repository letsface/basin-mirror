
package com.letsface.simplecamera;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;

import com.letsface.simplecamera.ScreenOrientationObserver.OnOrientationChangeListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CameraActivity extends Activity implements OnClickListener,
        OnOrientationChangeListener {

    private static final int REQ_CONFIRM_PICTURE = 1000;

    private static final String EXTRA_FRONT_CAMERA = "extra_front_camera";
    private static final String EXTRA_CONFIRM = "extra_confirm";

    public static class IntentBuilder {

        private Context mContext;
        private boolean mUseFront, mConfirm;

        public IntentBuilder(Context context) {
            mContext = context;
        }

        public IntentBuilder setUseFrontCamera(boolean useFront) {
            mUseFront = useFront;
            return this;
        }

        public IntentBuilder setConfirm(boolean confirm) {
            mConfirm = confirm;
            return this;
        }

        public Intent build() {
            Intent intent = new Intent(mContext, CameraActivity.class);
            intent.putExtra(EXTRA_FRONT_CAMERA, mUseFront);
            intent.putExtra(EXTRA_CONFIRM, mConfirm);
            return intent;
        }

    }

    private boolean usesFrontCamera() {
        return getIntent().getBooleanExtra(EXTRA_FRONT_CAMERA, false);
    }

    private boolean needsConfirm() {
        return getIntent().getBooleanExtra(EXTRA_CONFIRM, false);
    }

    private CameraHolder mCameraHolder;
    private ScreenOrientationObserver mOrientationObserver;
    private ImageButton mCaptureButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR
                | ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        setContentView(R.layout.activity_camera);

        mCameraHolder = new CameraHolder();
        if (usesFrontCamera())
            mCameraHolder.setCameraId(CameraInfo.CAMERA_FACING_FRONT);

        CameraPreview preview = new CameraPreview(this);
        preview.setCameraHolder(mCameraHolder);

        ((ViewGroup) findViewById(R.id.preview)).addView(preview);

        mCaptureButton = (ImageButton) findViewById(R.id.action_capture);
        mCaptureButton.setOnClickListener(this);

        mOrientationObserver = new ScreenOrientationObserver(this, this);
    }

    @Override
    public void onOrientationChange(int orientation) {
        mCameraHolder.setScreenOrientation(orientation);
        mCaptureButton.setRotation(360 - orientation);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCameraHolder.start();
        mOrientationObserver.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCameraHolder.stop();
        mOrientationObserver.stop();
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

    private String mPictureFilePath;

    private File getOutputFile() {
        File storage = new File(Environment.getExternalStorageDirectory(), "BasinMirror");
        storage.mkdirs();
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date());
        return new File(storage, "IMG_" + timestamp + ".jpg");
    }

    private final PictureCallback mPictureCallback = new PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            try {
                File file = getOutputFile();
                FileOutputStream fos = new FileOutputStream(file);
                try {
                    fos.write(data);
                } finally {
                    fos.close();
                }
                mPictureFilePath = file.getAbsolutePath();
                afterPictureTaken();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    private void afterPictureTaken() {
        if (needsConfirm()) {
            showConfirmActivity();
        } else {
            confirmed();
        }
    }

    private Bitmap getPreviewPicture() {
        // limit the size of the parcelable
        int targetW = 100;
        int targetH = 100;

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mPictureFilePath, opts);
        int photoW = opts.outWidth;
        int photoH = opts.outHeight;

        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        opts.inJustDecodeBounds = false;
        opts.inSampleSize = scaleFactor;
        opts.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mPictureFilePath, opts);
        Matrix mat = new Matrix();
        mat.postRotate(mCameraHolder.getCameraRotation());
        Bitmap res = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), mat,
                true);
        if (bitmap != res) { // returned bitmap may be the same object
            bitmap.recycle();
        }
        return res;
    }

    private void showConfirmActivity() {
        Intent intent = new Intent(this, PictureConfirmActivity.class);
        Uri uri = Uri.fromFile(new File(mPictureFilePath));
        intent.putExtra(PictureConfirmActivity.EXTRA_URI, uri);
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
        Intent intent = new Intent();
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(mPictureFilePath)));
        intent.putExtra("data", getPreviewPicture());
        setResult(RESULT_OK, intent);
        finish();
    }

    private void retake() {
        // do nothing
    }

}
