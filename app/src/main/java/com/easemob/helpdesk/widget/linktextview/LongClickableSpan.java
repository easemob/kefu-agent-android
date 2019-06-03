package com.easemob.helpdesk.widget.linktextview;

import android.text.style.ClickableSpan;
import android.view.View;

/**
 * 超链接点击和长按事件
 * Created by liyuzhao on 16/9/13.
 */
public abstract class LongClickableSpan extends ClickableSpan {

    /**
     * 长按事件
     * @param widget
     */
    public abstract void onLongClick(View widget);
}
