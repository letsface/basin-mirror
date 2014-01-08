
package com.letsface.simplecamera;

import android.app.Activity;
import android.hardware.Camera;
import android.view.Surface;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.util.List;

public class CameraHolder {

    private int mCameraId;
    private Camera mCamera;
    private SurfaceHolder mHolder;
    private int mRequestedPreviewWidth, mRequestedPreviewHeight;
    private List<Camera.Size> mSupportedPreviewSizes;

    public void setCameraId(int id) {
        mCameraId = id;
    }

    void setPreviewDisplay(SurfaceHolder holder) {
        mHolder = holder;
    }

    void setPreviewSize(int width, int height) {
        mRequestedPreviewWidth = width;
        mRequestedPreviewHeight = height;
    }

    private void open() {
        try {
            stop();
            // TODO: async open
            mCamera = Camera.open(mCameraId);
            getPreviewSizes();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    public boolean ready() {
        return mCamera != null;
    }

    private void getPreviewSizes() {
        if (!ready())
            return;

        Camera.Parameters params = mCamera.getParameters();
        mSupportedPreviewSizes = params.getSupportedPreviewSizes();
    }

    private void setPreviewSize() {
        if (!ready())
            return;

        Camera.Parameters params = mCamera.getParameters();
        Camera.Size optSize = getOptimalPreviewSize();
        params.setPreviewSize(optSize.width, optSize.height);
        mCamera.setParameters(params);
    }

    private Camera.Size getOptimalPreviewSize() {
        // Fullscreen size is always supported(?)
        // TODO: select size with aspect ratio
        for (Camera.Size size : mSupportedPreviewSizes) {
            if (size.width == mRequestedPreviewWidth && size.height == mRequestedPreviewHeight)
                return size;
        }
        return mSupportedPreviewSizes.get(0);
    }

    public void start() {
        open();
        if (!ready())
            return;

        try {
            mCamera.setPreviewDisplay(mHolder);
            setPreviewSize();
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        if (!ready())
            return;

        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    public void setCameraDisplayOrientation(Activity activity) {
        if (!ready())
            return;

        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(mCameraId, info);

        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else { // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        mCamera.setDisplayOrientation(result);
    }

}
