package com.easemob.helpdesk.activity;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
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
     */
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = this;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(Color.parseColor("#3C4147"));
            } else {
                setTranslucentStatus(true);
            }
        }
        if (!(this instanceof LoginActivity)) {
            HDApplication.getInstance().pushActivity(this);
        }
        manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
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
            if (getCurrentFocus() != null) manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    protected void showSoftkeyboard(final EditText etView) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                InputMethodManager inputManager = (InputMethodManager) etView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(etView, 0);
            }
        }, 100);
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        if (!(this instanceof LoginActivity)) {
            HDApplication.getInstance().popActivity(this);
        }
    }

    /**
     * 通过xml查找相应的ID，通用方法
     */
    protected <T extends View> T $(@IdRes int id) {
        return (T) findViewById(id);
    }

    public Drawable getResourceDrawable(@DrawableRes int resID) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return getResources().getDrawable(resID, null);
        } else {
            return getResources().getDrawable(resID);
        }
    }
}
