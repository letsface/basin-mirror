
package com.letsface.simplecamera;

import android.content.Context;
import android.hardware.SensorManager;
import android.view.OrientationEventListener;

public class ScreenOrientationObserver {

    public interface OnOrientationChangeListener {
        void onOrientationChange(int orientation, ScreenOrientationObserver observer);
    }

    private OnOrientationChangeListener mListener;
    private OrientationEventListener mOrientationEventListener;
    private int mOrientation = OrientationEventListener.ORIENTATION_UNKNOWN;

    public ScreenOrientationObserver(Context context, OnOrientationChangeListener listener) {
        mListener = listener;
        mOrientationEventListener = new OrientationEventListener(context,
                SensorManager.SENSOR_DELAY_UI) {
            @Override
            public void onOrientationChanged(int orientation) {
                if (orientation == ORIENTATION_UNKNOWN)
                    return;
                orientation = (orientation + 45) / 90 * 90;
                if (mOrientation != orientation) {
                    mOrientation = orientation;
                    if (mListener != null) {
                        mListener.onOrientationChange(mOrientation, ScreenOrientationObserver.this);
                    }
                }
            }
        };
    }

    public void start() {
        mOrientationEventListener.enable();
    }

    public void stop() {
        mOrientationEventListener.disable();
    }

    public int getOrientation() {
        return mOrientation;
    }

    public int getRotation() {
        if (mOrientation == OrientationEventListener.ORIENTATION_UNKNOWN)
            return 0;
        return 360 - mOrientation;
    }

}
