package com.example.swim;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;

public class PerspectiveImageView extends View {

    private Bitmap arrowBitmap;
    private Paint paint;
    private Matrix matrix;
    private android.graphics.Camera camera;

    public PerspectiveImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    private void initialize() {
        arrowBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.white_arrow);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        matrix = new Matrix();
        camera = new android.graphics.Camera();
    }

    public void setPerspectiveRotation(float degree) {
        // Reset the camera position
        camera.save();
        camera.rotateX(60); //tilt amount
        camera.rotateZ(degree); // Rotate around the vertical axis
        camera.getMatrix(matrix);
        camera.restore();

        // This will give the illusion that the arrow is rotating around its center from the user's perspective
        matrix.preTranslate(-arrowBitmap.getWidth() / 2, -arrowBitmap.getHeight() / 2);
        matrix.postTranslate(arrowBitmap.getWidth() / 2, arrowBitmap.getHeight() / 2);

        invalidate(); // Invalidate the view to trigger a redraw
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // Draw the arrow bitmap with the matrix applied to it
        canvas.drawBitmap(arrowBitmap, matrix, paint);
    }
}

