
package com.letsface.simplecamera;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.media.ExifInterface;
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
    private static final int REQ_IMAGE_CAPTURE = 73846;

    private static final String EXTRA_FRONT_CAMERA = "extra_front_camera";
    private static final String EXTRA_CONFIRM = "extra_confirm";
    private static final String EXTRA_HEIGHT = "extra_height";

    public static class IntentBuilder {

        private Activity mActivity;
        private boolean mUseFront, mConfirm;
        private int mHeight;
        private boolean mUseSystem;

        public IntentBuilder(Activity activity) {
            mActivity = activity;
        }

        public IntentBuilder setUseFrontCamera(boolean useFront) {
            mUseFront = useFront;
            return this;
        }

        public IntentBuilder setConfirm(boolean confirm) {
            mConfirm = confirm;
            return this;
        }

        public IntentBuilder setDesiredImageHeight(int height) {
            mHeight = height;
            return this;
        }

        public IntentBuilder setUseSystemCamera(boolean useSystem) {
            mUseSystem = useSystem;
            return this;
        }

        private Intent build() {
            if (mUseSystem) {
                return new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            }
            Intent intent = new Intent(mActivity, CameraActivity.class);
            intent.putExtra(EXTRA_FRONT_CAMERA, mUseFront);
            intent.putExtra(EXTRA_CONFIRM, mConfirm);
            intent.putExtra(EXTRA_HEIGHT, mHeight);
            return intent;
        }

        public void start() {
            mActivity.startActivityForResult(build(), REQ_IMAGE_CAPTURE);
        }
    }

    public static class IntentResult {

        private Bitmap mPreview;
        private Uri mImageUri;

        public static IntentResult parse(int requestCode, int resultCode, Intent data) {
            if (requestCode != REQ_IMAGE_CAPTURE)
                return null;
            if (resultCode != RESULT_OK)
                return null;
            return new IntentResult(data);
        }

        private IntentResult(Intent data) {
            mPreview = data.getParcelableExtra("data");
            Uri uri = data.getParcelableExtra(MediaStore.EXTRA_OUTPUT);
            if (uri == null) {
                uri = data.getData();
            }
            mImageUri = uri;
        }

        public Bitmap getPreviewImage() {
            return mPreview;
        }

        public Uri getImageUri() {
            return mImageUri;
        }

    }

    private boolean usesFrontCamera() {
        return getIntent().getBooleanExtra(EXTRA_FRONT_CAMERA, false);
    }

    private boolean needsConfirm() {
        return getIntent().getBooleanExtra(EXTRA_CONFIRM, false);
    }

    private int getDesiredImageHeight() {
        return getIntent().getIntExtra(EXTRA_HEIGHT, 0);
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
        return getPreviewPicture(100, 100);
    }

    private Bitmap getPreviewPicture(int targetW, int targetH) {
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

    private String saveImageCopy(int h) {
        Bitmap bmp = getPreviewPicture(h, h);
        try {
            File fo = getOutputFile();
            FileOutputStream fos = new FileOutputStream(fo);
            try {
                bmp.compress(CompressFormat.JPEG, 90, fos);
                return fo.getAbsolutePath();
            } finally {
                fos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mPictureFilePath;
    }

    private void confirmed() {
        Intent intent = new Intent();
        intent.putExtra("data", getPreviewPicture());
        int h = getDesiredImageHeight();
        if (h > 0) {
            mPictureFilePath = saveImageCopy(h);
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(mPictureFilePath)));
        setResult(RESULT_OK, intent);
        finish();
    }

    private void retake() {
        // do nothing
    }

    public static int getPictureRotation(Uri uri) {
        return getPictureRotation(uri.getPath());
    }

    public static int getPictureRotation(String path) {
        int rotation = 0;
        try {
            ExifInterface exif = new ExifInterface(path);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotation = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotation = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotation = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rotation;
    }

}
