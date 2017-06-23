package com.easemob.helpdesk.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liyuzhao on 16/6/20.
 */
public class CheckableLayout extends LinearLayout implements Checkable {

    private List<Checkable> checkables;
    private boolean checked;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CheckableLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    public CheckableLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public CheckableLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CheckableLayout(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        checkables = new ArrayList<>();
        setDescendantFocusability(FOCUS_BEFORE_DESCENDANTS);
    }

    @Override
    public void setChecked(boolean checked) {
        if (this.checked != checked) {
            for (Checkable checkable: checkables){
                checkable.setChecked(checked);
            }
            this.checked = checked;
        }
//        setBackgroundColor(getResources().getColor(checked ? R.color.manager_left_menu_item_color_pressed : R.color.manager_left_menu_item_color_normal));
    }

    @Override
    public boolean isChecked() {
        return checked;
    }

    @Override
    public void toggle() {
        setChecked(!checked);
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        int children = getChildCount();
        for (int i = 0; i < children; i++){
            View view = getChildAt(i);
            if (view instanceof Checkable){
                Checkable checkable = (Checkable)view;
                view.setFocusableInTouchMode(false);
                view.setFocusable(false);
                view.setClickable(false);
                view.setDuplicateParentStateEnabled(false);
                checkables.add(checkable);
            }
        }

    }
}
