
package com.letsface.simplecamera;

import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CameraUtil {

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
        camera.takePicture(mShutterCallback, null, mPictureCallback);
    }

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
