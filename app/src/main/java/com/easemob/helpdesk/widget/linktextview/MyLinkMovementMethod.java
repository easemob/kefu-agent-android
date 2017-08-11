package com.easemob.helpdesk.widget.linktextview;

import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.view.MotionEvent;
import android.widget.TextView;

/**
 * Created by liyuzhao on 16/9/13.
 */
public class MyLinkMovementMethod extends LinkMovementMethod {
    private long lastClickTime;
    private static final long CLICK_DELAY = 500L;

    @Override
    public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_UP ||
                action == MotionEvent.ACTION_DOWN) {
            int x = (int) event.getX();
            int y = (int) event.getY();

            x -= widget.getTotalPaddingLeft();
            y -= widget.getTotalPaddingTop();

            x += widget.getScrollX();
            y += widget.getScrollY();

            Layout layout = widget.getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);

            LongClickableSpan[] link = buffer.getSpans(off, off, LongClickableSpan.class);
            if (link.length != 0) {
                if (action == MotionEvent.ACTION_UP) {

                    if (System.currentTimeMillis() - lastClickTime < CLICK_DELAY) {
                        //点击事件
                        link[0].onClick(widget);
                    } else {
                        link[0].onLongClick(widget);
                    }

                } else {
                    lastClickTime = System.currentTimeMillis();
                }
                return true;
            } else {
                Selection.removeSelection(buffer);
            }
        }
        return super.onTouchEvent(widget, buffer, event);
    }
}
