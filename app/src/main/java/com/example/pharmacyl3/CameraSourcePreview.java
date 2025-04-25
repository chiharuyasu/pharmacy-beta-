package com.example.pharmacyl3;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;

public class CameraSourcePreview extends FrameLayout {
    private SurfaceView mSurfaceView;

    public CameraSourcePreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CameraSourcePreview(Context context) {
        super(context);
        init();
    }

    private void init() {
        mSurfaceView = new SurfaceView(getContext());
        addView(mSurfaceView);
    }

    public SurfaceView getSurfaceView() {
        return mSurfaceView;
    }

    public SurfaceHolder getHolder() {
        return mSurfaceView.getHolder();
    }
}
