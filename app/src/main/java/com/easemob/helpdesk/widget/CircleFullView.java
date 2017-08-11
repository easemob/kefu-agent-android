package com.easemob.helpdesk.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/**
 * Created by liyuzhao on 16/6/27.
 */
public class CircleFullView extends View {

    @ColorInt
    public int defaultColor = Color.RED;

    public CircleFullView(Context context) {
        super(context);
    }

    public CircleFullView(Context context, int color) {
        super(context);
        defaultColor = color;
    }

    public CircleFullView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CircleFullView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    ShapeDrawable mShapeDrawable = null;


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mShapeDrawable = new ShapeDrawable(new OvalShape());
        mShapeDrawable.getPaint().setColor(defaultColor);
        int radius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics());
        Rect bounds = new Rect(0, 0, radius, radius);
        mShapeDrawable.setBounds(bounds);
        mShapeDrawable.draw(canvas);
    }
}
