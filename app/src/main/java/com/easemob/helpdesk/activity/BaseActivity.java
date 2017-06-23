package com.easemob.helpdesk.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.easemob.helpdesk.HDApplication;
import com.easemob.helpdesk.mvp.LoginActivity;

import java.util.Timer;
import java.util.TimerTask;

public class BaseActivity extends AppCompatActivity {

    private InputMethodManager manager;

    protected Activity mActivity;
    /**
     * Perform initialization of all fragments and loaders.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = this;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatus(true);
        }
        if(!(this instanceof LoginActivity)){
            HDApplication.getInstance().pushActivity(this);
        }
        manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    private void setTranslucentStatus(boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    /**
     * 隐藏软键盘
     */
    protected void hideKeyboard() {
        if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getCurrentFocus() != null)
                manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    protected void showSoftkeyboard(final EditText etView) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                InputMethodManager inputManager = (InputMethodManager) etView.getContext().getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(etView, 0);
            }
        }, 100);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(!(this instanceof LoginActivity)){
            HDApplication.getInstance().popActivity(this);
        }
    }

    /**
     * 通过xml查找相应的ID，通用方法
     * @param id
     * @param <T>
     * @return
     */
    protected <T extends View> T $(@IdRes int id) {
        return (T) findViewById(id);
    }



	/**
     * 传入EditText的id
     * 没有传入的EditText不做处理
     * @return id 数组
     */
    public int[] hideSoftByEditViewIds(){
        return null;
    }

	/**
     * 传入要过滤的View
     * 过滤之后点击将不会有隐藏软键盘的操作
     *
     * @return id 数组
     */
    public View[] filterViewByIds(){
        return null;
    }

    // 是否触摸在指定view上面，对某个控件过滤
    public boolean isTouchView(View[] views, MotionEvent ev){
        if (views == null || views.length == 0) return false;
        int[] location = new int[2];
        for(View view : views){
            view.getLocationOnScreen(location);
            int x = location[0];
            int y = location[1];
            if (ev.getX() > x && ev.getX() < (x + view.getWidth())
                    && ev.getY() > y && ev.getY() < (y + view.getHeight())){
                return true;
            }
        }
        return false;
    }

    // 是否触摸在指定view上面，对某个控件过滤
    public boolean isTouchView(int[] ids, MotionEvent ev){
        int[] location = new int[2];
        for (int id : ids){
            View view = findViewById(id);
            if (view == null) continue;
            view.getLocationOnScreen(location);
            int x = location[0];
            int y = location[1];
            if (ev.getX() > x && ev.getX() < (x + view.getWidth())
                    && ev.getY() > y && ev.getY() < (y + view.getHeight())){
                return true;
            }
        }
        return false;
    }

	/**
     * 隐藏键盘
     * @param v 焦点所在View
     * @param ids 输入框
     *
     * @return true 代表焦点在edit上
     */
    public boolean isFousEditText(View v, int... ids){
        if (v instanceof EditText){
            EditText tmp_et = (EditText) v;
            for (int id : ids){
                if (tmp_et.getId() == id){
                    return true;
                }
            }
        }
        return false;
    }

	/**
     * 清除editText的焦点
     * @param v 焦点所在View
     * @param ids 输入框
     */
    public void clearViewFocus(View v, int... ids){
        if (null != v && null != ids && ids.length > 0){
            for (int id : ids){
                if (v.getId() == id){
                    v.clearFocus();
                    break;
                }
            }
        }
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN){
            if (isTouchView(filterViewByIds(), ev)) return super.dispatchTouchEvent(ev);
            if (hideSoftByEditViewIds() == null || hideSoftByEditViewIds().length == 0)
                return super.dispatchTouchEvent(ev);
            View v = getCurrentFocus();
            if (isFousEditText(v, hideSoftByEditViewIds())){
                if (isTouchView(hideSoftByEditViewIds(), ev))
                    return super.dispatchTouchEvent(ev);
                //隐藏键盘
                hideKeyboard();
                clearViewFocus(v, hideSoftByEditViewIds());
            }

        }

        return super.dispatchTouchEvent(ev);
    }
}
