
package com.letsface.simplecamera;

import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.util.List;

public class CameraHolder {

    private int mCameraId;
    private Camera mCamera;
    private SurfaceHolder mHolder;
    private int mRequestedPreviewWidth, mRequestedPreviewHeight;
    private List<Camera.Size> mSupportedPreviewSizes;
    private int mCameraRotation;

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

    private void setPictureSize() {
        if (!ready())
            return;
        Camera.Parameters params = mCamera.getParameters();
        Camera.Size size = params.getSupportedPictureSizes().get(0);
        params.setPictureSize(size.width, size.height);
        mCamera.setParameters(params);
        params = mCamera.getParameters();
        size = params.getPictureSize();
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
        open();
        if (!ready())
            return;

        try {
            mCamera.setPreviewDisplay(mHolder);
            setPreviewSize();
            setPictureSize();
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
        setCameraRotation();
    }

    public void stop() {
        stopPreviewAndRelease();
    }

    private void stopPreviewAndRelease() {
        if (!ready())
            return;

        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    private void setCameraRotation() {
        if (!ready())
            return;
        Camera.Parameters params = mCamera.getParameters();
        params.setRotation(mCameraRotation);
        mCamera.setParameters(params);
    }

    public void setScreenOrientation(int orientation) {
        CameraInfo info = new CameraInfo();
        Camera.getCameraInfo(mCameraId, info);
        int rotation = 0;
        if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
            rotation = (info.orientation - orientation + 360) % 360;
        } else {
            rotation = (info.orientation + orientation) % 360;
        }
        mCameraRotation = rotation;
        setCameraRotation();
    }

    public int getCameraRotation() {
        return mCameraRotation;
    }

}
