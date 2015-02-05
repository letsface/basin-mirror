
package com.letsface.simplecamera;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private CameraHolder mCameraHolder;

    public CameraPreview(Context context) {
        this(context, null);
    }

    public CameraPreview(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraPreview(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        final SurfaceHolder holder = getHolder();
        holder.addCallback(this);
    }

    public void setCameraHolder(CameraHolder holder) {
        mCameraHolder = holder;
    }

    // TODO: resize view to keep preview aspect ratio
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mCameraHolder.setPreviewDisplay(holder);
        mCameraHolder.setPreviewSize(width, height);
        mCameraHolder.start();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mCameraHolder.stop();
    }

}
