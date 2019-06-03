package com.easemob.helpdesk.activity.test;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;

import com.easemob.helpdesk.utils.CommonUtils;
import com.easemob.helpdesk.widget.imageview.CircleDrawable;

/**
 * Created by liyuzhao on 31/03/2017.
 */

public class TestActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout llView = new LinearLayout(this);
        llView.setBackgroundColor(Color.WHITE);
        CircleDrawable circleDrawable = new CircleDrawable(this, Color.GREEN);

//        ImageView imageView = new ImageView(this);
        circleDrawable.setLayoutParams(new LinearLayout.LayoutParams(CommonUtils.convertDip2Px(this, 10), CommonUtils.convertDip2Px(this, 10)));
        llView.addView(circleDrawable);

        setContentView(llView);



    }
}
