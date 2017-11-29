package com.easemob.helpdesk.activity.manager;

import com.easemob.helpdesk.utils.DateUtils;
import com.github.mikephil.charting.formatter.XAxisValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.Date;

/**
 * Created by liyuzhao on 16/7/7.
 */
public class MyXAxisValueFormatter implements XAxisValueFormatter {

    String dateInterval = "1d";

    public MyXAxisValueFormatter(){

    }

    public MyXAxisValueFormatter(String dateInterval){
        this.dateInterval = dateInterval;
    }

    @Override
    public String getXValue(String original, int index, ViewPortHandler viewPortHandler) {
        try{
            if (dateInterval == null || dateInterval.equals("1d") || dateInterval.equals("1h")) {
                if (original.length() > 4) {
                    return DateUtils.getTimestampStringForChart(new Date(Long.parseLong(original)));
                } else {
                    return original;
                }
            } else if (dateInterval.equals("1w")) {
                if (original.length() > 4) {
                    return DateUtils.getTimestampWeekForChart(new Date(Long.parseLong(original)));
                } else {
                    return original;
                }
            } else if (dateInterval.equals("1M")) {
                if (original.length() > 4) {
                    return DateUtils.getTimestampMonthForChart(new Date(Long.parseLong(original)));
                } else {
                    return original;
                }
            } else {
                return original;
            }
        }catch (Exception e){
            return original;
        }

    }
}
