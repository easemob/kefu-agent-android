package com.easemob.helpdesk.activity.manager;

import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;

import java.text.DecimalFormat;

/**
 * Created by liyuzhao on 16/7/7.
 */
public class MyYAxisValueFormatter implements YAxisValueFormatter {

    private DecimalFormat mFormat;

    public MyYAxisValueFormatter(){
        mFormat = new DecimalFormat("###,###,###,##0");
    }

    @Override
    public String getFormattedValue(float value, YAxis yAxis) {
//        if (value > 0 && value < 1){
//            return "";
//        }else{
//           return mFormat.format(value);
//        }

        if (value * 10 % 10 != 0){
            return "";
        }else{
            return mFormat.format(value);
        }

    }
}
