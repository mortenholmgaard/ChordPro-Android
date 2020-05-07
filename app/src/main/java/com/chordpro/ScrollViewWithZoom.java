package com.chordpro;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ScrollView;

import androidx.annotation.RequiresApi;

public class ScrollViewWithZoom extends ScrollView {
    public ScrollViewWithZoom(Context context) {
        super(context);
    }

    public ScrollViewWithZoom(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScrollViewWithZoom(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ScrollViewWithZoom(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private ScaleGestureDetector scaleDetector;

    public void setScaleDetector(ScaleGestureDetector scaleDetector) {
        this.scaleDetector = scaleDetector;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        boolean result = super.dispatchTouchEvent(event);
        if (scaleDetector != null) {
            scaleDetector.onTouchEvent(event);
            return true;
        }
        return result;
    }
}
