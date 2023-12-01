package com.codersworld.awesalibs.customviews;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;

public class TouchImageView extends ImageView {
    static final int CLICK = 3;
    static final int DRAG = 1;
    static final int NONE = 0;
    static final int ZOOM = 2;
    Context context;
    PointF last = new PointF();
    float[] m;
    ScaleGestureDetector mScaleDetector;
    Matrix matrix;
    float maxScale = 3.0f;
    float minScale = 1.0f;
    int mode = 0;
    int oldMeasuredHeight;
    int oldMeasuredWidth;
    protected float origHeight;
    protected float origWidth;
    float saveScale = 1.0f;
    PointF start = new PointF();
    int viewHeight;
    int viewWidth;

    public TouchImageView(Context context2) {
        super(context2);
        sharedConstructing(context2);
    }

    public TouchImageView(Context context2, AttributeSet attrs) {
        super(context2, attrs);
        sharedConstructing(context2);
    }

    private void sharedConstructing(Context context2) {
        super.setClickable(true);
        this.context = context2;
        this.mScaleDetector = new ScaleGestureDetector(context2, new ScaleListener());
        Matrix matrix2 = new Matrix();
        this.matrix = matrix2;
        this.m = new float[9];
        setImageMatrix(matrix2);
        setScaleType(ScaleType.MATRIX);
        setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                TouchImageView.this.mScaleDetector.onTouchEvent(event);
                PointF curr = new PointF(event.getX(), event.getY());
                int action = event.getAction();
                if (action == 0) {
                    TouchImageView.this.last.set(curr);
                    TouchImageView.this.start.set(TouchImageView.this.last);
                    TouchImageView.this.mode = 1;
                } else if (action == 1) {
                    TouchImageView.this.mode = 0;
                    int xDiff = (int) Math.abs(curr.x - TouchImageView.this.start.x);
                    int yDiff = (int) Math.abs(curr.y - TouchImageView.this.start.y);
                    if (xDiff < 3 && yDiff < 3) {
                        TouchImageView.this.performClick();
                    }
                } else if (action != 2) {
                    if (action == 6) {
                        TouchImageView.this.mode = 0;
                    }
                } else if (TouchImageView.this.mode == 1) {
                    float deltaX = curr.x - TouchImageView.this.last.x;
                    float deltaY = curr.y - TouchImageView.this.last.y;
                    TouchImageView touchImageView = TouchImageView.this;
                    float fixTransX = touchImageView.getFixDragTrans(deltaX, (float) touchImageView.viewWidth, TouchImageView.this.origWidth * TouchImageView.this.saveScale);
                    TouchImageView touchImageView2 = TouchImageView.this;
                    TouchImageView.this.matrix.postTranslate(fixTransX, touchImageView2.getFixDragTrans(deltaY, (float) touchImageView2.viewHeight, TouchImageView.this.origHeight * TouchImageView.this.saveScale));
                    TouchImageView.this.fixTrans();
                    TouchImageView.this.last.set(curr.x, curr.y);
                }
                TouchImageView touchImageView3 = TouchImageView.this;
                touchImageView3.setImageMatrix(touchImageView3.matrix);
                TouchImageView.this.invalidate();
                return true;
            }
        });
    }

    public void setMaxZoom(float x) {
        this.maxScale = x;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        private ScaleListener() {
        }

        public boolean onScaleBegin(ScaleGestureDetector detector) {
            TouchImageView.this.mode = 2;
            return true;
        }

        public boolean onScale(ScaleGestureDetector detector) {
            float mScaleFactor = detector.getScaleFactor();
            float origScale = TouchImageView.this.saveScale;
            TouchImageView.this.saveScale *= mScaleFactor;
            if (TouchImageView.this.saveScale > TouchImageView.this.maxScale) {
                TouchImageView touchImageView = TouchImageView.this;
                touchImageView.saveScale = touchImageView.maxScale;
                mScaleFactor = TouchImageView.this.maxScale / origScale;
            } else if (TouchImageView.this.saveScale < TouchImageView.this.minScale) {
                TouchImageView touchImageView2 = TouchImageView.this;
                touchImageView2.saveScale = touchImageView2.minScale;
                mScaleFactor = TouchImageView.this.minScale / origScale;
            }
            if (TouchImageView.this.origWidth * TouchImageView.this.saveScale <= ((float) TouchImageView.this.viewWidth) || TouchImageView.this.origHeight * TouchImageView.this.saveScale <= ((float) TouchImageView.this.viewHeight)) {
                TouchImageView.this.matrix.postScale(mScaleFactor, mScaleFactor, (float) (TouchImageView.this.viewWidth / 2), (float) (TouchImageView.this.viewHeight / 2));
            } else {
                TouchImageView.this.matrix.postScale(mScaleFactor, mScaleFactor, detector.getFocusX(), detector.getFocusY());
            }
            TouchImageView.this.fixTrans();
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public void fixTrans() {
        this.matrix.getValues(this.m);
        float[] fArr = this.m;
        float transX = fArr[2];
        float transY = fArr[5];
        float fixTransX = getFixTrans(transX, (float) this.viewWidth, this.origWidth * this.saveScale);
        float fixTransY = getFixTrans(transY, (float) this.viewHeight, this.origHeight * this.saveScale);
        if (fixTransX != 0.0f || fixTransY != 0.0f) {
            this.matrix.postTranslate(fixTransX, fixTransY);
        }
    }

    /* access modifiers changed from: package-private */
    public float getFixTrans(float trans, float viewSize, float contentSize) {
        float maxTrans;
        float minTrans;
        if (contentSize <= viewSize) {
            minTrans = 0.0f;
            maxTrans = viewSize - contentSize;
        } else {
            minTrans = viewSize - contentSize;
            maxTrans = 0.0f;
        }
        if (trans < minTrans) {
            return (-trans) + minTrans;
        }
        if (trans > maxTrans) {
            return (-trans) + maxTrans;
        }
        return 0.0f;
    }

    /* access modifiers changed from: package-private */
    public float getFixDragTrans(float delta, float viewSize, float contentSize) {
        if (contentSize <= viewSize) {
            return 0.0f;
        }
        return delta;
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        this.viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        int size = MeasureSpec.getSize(heightMeasureSpec);
        this.viewHeight = size;
        int i = this.oldMeasuredHeight;
        int i2 = this.viewWidth;
        if ((i != i2 || i != size) && i2 != 0 && size != 0) {
            this.oldMeasuredHeight = size;
            this.oldMeasuredWidth = i2;
            if (this.saveScale == 1.0f) {
                Drawable drawable = getDrawable();
                if (drawable != null && drawable.getIntrinsicWidth() != 0 && drawable.getIntrinsicHeight() != 0) {
                    int bmWidth = drawable.getIntrinsicWidth();
                    int bmHeight = drawable.getIntrinsicHeight();
                    float scale = Math.min(((float) this.viewWidth) / ((float) bmWidth), ((float) this.viewHeight) / ((float) bmHeight));
                    this.matrix.setScale(scale, scale);
                    float redundantYSpace = (((float) this.viewHeight) - (((float) bmHeight) * scale)) / 2.0f;
                    float redundantXSpace = (((float) this.viewWidth) - (((float) bmWidth) * scale)) / 2.0f;
                    this.matrix.postTranslate(redundantXSpace, redundantYSpace);
                    this.origWidth = ((float) this.viewWidth) - (redundantXSpace * 2.0f);
                    this.origHeight = ((float) this.viewHeight) - (2.0f * redundantYSpace);
                    setImageMatrix(this.matrix);
                } else {
                    return;
                }
            }
            fixTrans();
        }
    }
}
