package com.example.admin.wobeassignment.customViews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;

import com.android.volley.toolbox.NetworkImageView;

/**
 * Created by Admin on 22-09-2017.
 */

public class CircularImageView extends NetworkImageView {

    private int borderWidth = 1;
    private int viewWidth;
    private int viewHeight;
    private Bitmap image;
    private Paint paint;
    private Paint paintBorder;
    private BitmapShader shader;

    public CircularImageView(Context context) {
        super(context);
        setup();
    }

    public CircularImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    public CircularImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setup();
    }

    private void setup() {
        // init paint
        paint = new Paint();
        paint.setAntiAlias(true);

        paintBorder = new Paint();
        setBorderColor(Color.WHITE);
        paintBorder.setAntiAlias(true);
    }

    public void setBorderWidth(int borderWidth) {
        this.borderWidth = borderWidth;
        this.invalidate();
    }

    public void setBorderColor(int borderColor) {
        if (paintBorder != null)
            paintBorder.setColor(borderColor);

        this.invalidate();
    }

    private void loadBitmap() {
        BitmapDrawable bitmapDrawable = (BitmapDrawable) this.getDrawable();

        if (bitmapDrawable != null)
            image = bitmapDrawable.getBitmap();
    }

    @SuppressLint("DrawAllocation")
    @Override
    public void onDraw(Canvas canvas) {

        loadBitmap();

        if (image != null) {
            shader = new BitmapShader(Bitmap.createScaledBitmap(image,
                    canvas.getWidth(), canvas.getHeight(), false),
                    Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            paint.setShader(shader);
            int circleCenter = viewWidth / 2;

            canvas.drawCircle(circleCenter + borderWidth, circleCenter
                    + borderWidth, circleCenter + borderWidth, paintBorder);
            canvas.drawCircle(circleCenter + borderWidth, circleCenter
                    + borderWidth, circleCenter, paint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = measureWidth(widthMeasureSpec);
        int height = measureHeight(heightMeasureSpec, widthMeasureSpec);

        viewWidth = width - (borderWidth * 2);
        viewHeight = height - (borderWidth * 2);

        setMeasuredDimension(width, height);
    }

    private int measureWidth(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {

            result = specSize;
        } else {

            result = viewWidth;

        }

        return result;
    }

    private int measureHeight(int measureSpecHeight, int measureSpecWidth) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpecHeight);
        int specSize = MeasureSpec.getSize(measureSpecHeight);

        if (specMode == MeasureSpec.EXACTLY) {

            result = specSize;
        } else {
            result = viewHeight;
        }
        return result;
    }
}
