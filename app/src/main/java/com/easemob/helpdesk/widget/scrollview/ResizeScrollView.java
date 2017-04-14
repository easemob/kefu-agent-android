package com.easemob.helpdesk.widget.scrollview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

/**
 * Created by liyuzhao on 16/10/8.
 */
public class ResizeScrollView extends ScrollView {


    public ResizeScrollView(Context context) {
        super(context);
    }

    public ResizeScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private OnResizeListener mListener;

    public interface OnResizeListener {
        void OnResize(int w, int h, int oldw, int oldh);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mListener != null){
            mListener.OnResize(w, h, oldw, oldh);
        }
    }

    public void setOnResizeListener(OnResizeListener listener){
        this.mListener = listener;
    }

}
