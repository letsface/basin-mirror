
package com.letsface.simplecamera;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.SensorManager;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.util.List;

public class CameraHolder {

    private int mCameraId;
    private Camera mCamera;
    private SurfaceHolder mHolder;
    private int mRequestedPreviewWidth, mRequestedPreviewHeight;
    private List<Camera.Size> mSupportedPreviewSizes;
    private OrientationListener mOrientationListener;

    public CameraHolder(Context context) {
        mOrientationListener = new OrientationListener(context);
    }

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
            stopPreviewAndRelease();
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

    public Camera getCamera() {
        return mCamera;
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
        float ratio = mRequestedPreviewWidth / (float) mRequestedPreviewHeight;
        long area = mRequestedPreviewWidth * mRequestedPreviewHeight;
        Camera.Size res = mSupportedPreviewSizes.get(0);
        float minRatioDiff = Float.MAX_VALUE;
        long minAreaDiff = Long.MAX_VALUE;
        for (Camera.Size size : mSupportedPreviewSizes) {
            float ratio1 = size.width / (float) size.height;
            float ratioDiff = Math.abs(ratio1 - ratio);

            long area1 = size.width * size.height;
            long areaDiff = Math.abs(area1 - area);

            if (ratioDiff < minRatioDiff
                    || (Math.abs(ratioDiff - minRatioDiff) < 1e-6 && areaDiff < minAreaDiff)) {
                minRatioDiff = ratioDiff;
                minAreaDiff = areaDiff;
                res = size;
            }
        }
        return res;
    }

    public void start() {
        mOrientationListener.enable();

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
        mOrientationListener.disable();
        stopPreviewAndRelease();
    }

    private void stopPreviewAndRelease() {
        if (!ready())
            return;

        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    private void setRotate(int rotation) {
        if (!ready())
            return;
        Camera.Parameters params = mCamera.getParameters();
        params.setRotation(rotation);
        mCamera.setParameters(params);
    }

    private class OrientationListener extends OrientationEventListener {

        private int mRotation = -1;

        public OrientationListener(Context context) {
            super(context, SensorManager.SENSOR_DELAY_UI);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            if (orientation == ORIENTATION_UNKNOWN)
                return;
            CameraInfo info = new CameraInfo();
            Camera.getCameraInfo(mCameraId, info);
            orientation = (orientation + 45) / 90 * 90;
            int rotation = 0;
            if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
                rotation = (info.orientation - orientation + 360) % 360;
            } else {
                rotation = (info.orientation + orientation) % 360;
            }
            if (mRotation != rotation) {
                setRotate(rotation);
                mRotation = rotation;
            }
        }

    }

}
