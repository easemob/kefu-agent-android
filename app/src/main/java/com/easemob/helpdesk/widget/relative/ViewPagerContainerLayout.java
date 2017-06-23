package com.easemob.helpdesk.widget.relative;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

/**
 * Created by lyuzhao on 2016/1/19.
 */
public class ViewPagerContainerLayout extends RelativeLayout {
    ViewPager child_viewpager;
    float startX;


    public ViewPagerContainerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN://按下
                try {
                    startX = ev.getX();
                    getParent().requestDisallowInterceptTouchEvent(true);
                } catch (Exception e) {
                }
                break;
            //滑动，在此时里层viewpager的第一页和最后一页滑动做处理
            case MotionEvent.ACTION_MOVE:
                try {
                    if (startX == ev.getX()) {
                        if (0 == child_viewpager.getCurrentItem() || child_viewpager.getCurrentItem() == (child_viewpager.getAdapter().getCount() - 1)) {
                            getParent().requestDisallowInterceptTouchEvent(false);
                        }
                    } else if (startX < ev.getX()) {
                        // 里层viewpager已经是第一页，此时继续向左滑(手指从左往右滑)
                        if (child_viewpager.getCurrentItem() == 0) {
                            getParent().requestDisallowInterceptTouchEvent(false);
                        }
                    } else if (startX > ev.getX()) {
                        if (child_viewpager.getCurrentItem() == child_viewpager.getAdapter().getCount() - 1) {
                            getParent().requestDisallowInterceptTouchEvent(false);
                        }
                    } else {
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                } catch (Exception e) {
                }
                break;
            case MotionEvent.ACTION_UP://抬起
            case MotionEvent.ACTION_CANCEL:
                getParent().requestDisallowInterceptTouchEvent(false);
                break;
        }
        return false;
    }


    //注入里层的viewpager
    public void setChildViewPager(ViewPager childViewPager) {
        this.child_viewpager = childViewPager;
    }
}
