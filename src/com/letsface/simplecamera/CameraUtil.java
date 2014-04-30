
package com.letsface.simplecamera;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CameraUtil {

    private static final String TAG = "CameraUtil";

    public interface PictureTakenCallback {
        void onPictureTaken(String path);

        void onError(Exception e);
    }

    private PictureTakenCallback mCallback;

    static File getOutputFile() {
        File storage = new File(Environment.getExternalStorageDirectory(), "BasinMirror");
        storage.mkdirs();
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date());
        return new File(storage, "IMG_" + timestamp + ".jpg");
    }

    public void takePhoto(Camera camera, PictureTakenCallback cb) {
        if (camera == null)
            return;
        mCallback = cb;
        camera.autoFocus(mAutoFocusCallback);
    }

    // TODO: DRY, duplicates in CameraActivity
    public static String getScaledPicture(String picturePath, int targetDim) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(picturePath, opts);
        int photoW = opts.outWidth;
        int photoH = opts.outHeight;

        int scaleFactor = Math.max(photoW / targetDim, photoH / targetDim);

        opts.inJustDecodeBounds = false;
        opts.inSampleSize = scaleFactor;
        opts.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(picturePath, opts);
        Log.v(TAG, "scaled image size: " + bitmap.getWidth() + ", " + bitmap.getHeight());
        return saveImage(bitmap);
    }

    public static String saveImage(Bitmap bmp) {
        try {
            File fo = CameraUtil.getOutputFile();
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
        return null;
    }

    private final AutoFocusCallback mAutoFocusCallback = new AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            // capture no matter focus is successful or not
            camera.takePicture(mShutterCallback, null, mPictureCallback);
        }
    };

    private final ShutterCallback mShutterCallback = new ShutterCallback() {
        @Override
        public void onShutter() {
            // do nothing, just for the shutter click sound
        }
    };

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
                mCallback.onPictureTaken(file.getAbsolutePath());
            } catch (IOException e) {
                mCallback.onError(e);
            }
        }
    };

}
