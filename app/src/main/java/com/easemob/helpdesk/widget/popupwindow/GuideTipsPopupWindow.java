package com.easemob.helpdesk.widget.popupwindow;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.utils.PreferenceUtils;

/**
 * Created by benson on 2018/4/4.
 */

public class GuideTipsPopupWindow extends PopupWindow {

    private ClickListener listener;
    private String[] contents = { "客服在线状态以后变成了下划线", "最大接待人数设置如果变成灰色，说明只有管理员才可以操作" };
    private boolean flag;

    public GuideTipsPopupWindow(Context context) {
        super(context);
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setBackgroundResource(R.drawable.guidetips_icon);
        linearLayout.setGravity(Gravity.CENTER_VERTICAL);

        final TextView textView = new TextView(context);
        textView.setText(contents[0]);
        textView.setTextColor(Color.WHITE);
        textView.setCompoundDrawablesWithIntrinsicBounds(null, null, context.getResources().getDrawable(R.drawable.guide_tips_delete_icon), null);
        textView.setGravity(Gravity.CENTER_VERTICAL);
        linearLayout.addView(textView);

        setBackgroundDrawable(new ColorDrawable(0000000000));
        setContentView(linearLayout);

        textView.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                dismiss();
                listener.onClick();
                textView.setText(contents[1]);
                if (flag) {
                    dismiss();
                    PreferenceUtils.getInstance().setIsFirst(false);
                }
                flag = true;
            }
        });
    }

    public void setClickListener(ClickListener listener) {
        this.listener = listener;
    }

    public interface ClickListener {
        void onClick();
    }
}
