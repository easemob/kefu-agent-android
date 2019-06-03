package com.easemob.helpdesk.activity.manager.realtimemonitor;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.easemob.helpdesk.HDApplication;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.manager.LargeValueFormatter;
import com.easemob.helpdesk.activity.manager.MyMarkerView;
import com.easemob.helpdesk.activity.manager.MyXAxisValueFormatter;
import com.easemob.helpdesk.activity.manager.model.WaitCountResponse;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.google.gson.Gson;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.chat.HelpDeskManager;
import com.hyphenate.kefusdk.entity.user.HDUser;
import com.hyphenate.kefusdk.utils.HDLog;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by liyuzhao on 13/03/2017.
 */

public class WaitCountFragment extends Fragment {

    private static final String TAG = "WaitCountFragment";
    @BindView(R.id.fl_waitcount)
    protected FrameLayout waitCountLayout;

    Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.manager_realtime_fragment_waitcount, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadData();

    }


    private void loadData(){
        HDUser loginUser = HDClient.getInstance().getCurrentUser();
        if (loginUser == null){
            return;
        }
        HelpDeskManager.getInstance().getMonitorWaitCount(loginUser.getTenantId(), new HDDataCallBack<String>() {
            @Override
            public void onSuccess(final String value) {
                if (getActivity() == null){
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        parseWaitCountResponse(value);

                    }
                });
            }

            @Override
            public void onError(int error, String errorMsg) {
                HDLog.e(TAG, "error:" + errorMsg);
                if (getActivity() == null){
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
            }

            @Override
            public void onAuthenticationException() {
                if (getActivity() == null){
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    HDApplication.getInstance().logout();
                    }
                });
            }
        });



    }



    private void parseWaitCountResponse(String value){
        HDLog.d(TAG, "parseWaitCountResponse:" + value);
        waitCountLayout.removeAllViews();
        Gson gson = new Gson();
        WaitCountResponse response = gson.fromJson(value, WaitCountResponse.class);
        if (response == null){
            return;
        }
        List<WaitCountResponse.EntitiesBean> resultBeanList = response.getEntities();

        if (resultBeanList == null || resultBeanList.size() == 0){
            addEmptyView();
            return;
        }

        BarChart barChart = getBarChart(resultBeanList);
        if (barChart != null) {
            waitCountLayout.addView(barChart, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        } else {
            addEmptyView();
        }
    }

    private void addEmptyView() {
        TextView textView = new TextView(getContext());
        textView.setGravity(Gravity.CENTER);
        textView.setText("无数据");
        waitCountLayout.addView(textView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private BarChart getBarChart(List<WaitCountResponse.EntitiesBean> resultBeanList){
        BarChart mChart = new BarChart(getContext());
        mChart.setDrawBarShadow(false);
        mChart.setDrawValueAboveBar(true);
        mChart.setDescription("");
        // if more than 30 entries are displayed in the chart, no values will be
        // drawn
        mChart.setMaxVisibleValueCount(30);

        // scaling can now only be done on x- and y-axis separately
        mChart.setPinchZoom(false);
        mChart.setDoubleTapToZoomEnabled(false);
        mChart.setDrawGridBackground(false);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setSpaceBetweenLabels(2);
        xAxis.setValueFormatter(new MyXAxisValueFormatter());

        YAxisValueFormatter custom = new LargeValueFormatter();
        mChart.getAxisRight().setEnabled(false);
        YAxis leftAxis = mChart.getAxisLeft();
//        leftAxis.setLabelCount(2, false);
        leftAxis.setValueFormatter(custom);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setSpaceTop(15f);
        leftAxis.setAxisMinValue(0f); // this replaces setStartAtZero(true)

        Legend l = mChart.getLegend();
        l.setPosition(Legend.LegendPosition.ABOVE_CHART_RIGHT);
        l.setForm(Legend.LegendForm.CIRCLE);
        l.setFormSize(9f);
        l.setTextSize(11f);
        l.setXEntrySpace(4f);


        List<IBarDataSet> barDataSets = new ArrayList<>();
        ArrayList<String> xVals = new ArrayList<>();

        for (int i = 0; i < resultBeanList.size(); i++){
            ArrayList<BarEntry> yVals1 = new ArrayList<>();
            WaitCountResponse.EntitiesBean resultBean = resultBeanList.get(i);
            List<Map<String, Integer>> valueList = resultBean.getValue();
            for (int j = 0; j < valueList.size(); j++){
                Map<String, Integer> itemMap = valueList.get(j);
                Set<String> keys = itemMap.keySet();
                for (String key : keys){
                    String date = dateTransfer(key);
                    yVals1.add(new BarEntry(itemMap.get(key), j));
                    if (!xVals.contains(key)){
                        xVals.add(date);
                    }
                }
            }
            String stringType = resultBean.getKey();
            if (TextUtils.isEmpty(stringType)){
                continue;
            }
            int color = Color.GREEN;
            String showText = "排队人数"/*resultBean.getKey()*/;
            BarDataSet barDataSet = new BarDataSet(yVals1, showText);
            barDataSet.setBarSpacePercent(35f);
            barDataSet.setDrawValues(true);
            barDataSet.setColor(color);
            barDataSets.add(barDataSet);
        }

        BarData data = new BarData(xVals, barDataSets);
        data.setValueTextSize(10f);
        mChart.setData(data);
        mChart.setScaleYEnabled(false);
        MyMarkerView mv = new MyMarkerView(getContext(), R.layout.custom_marker_view);
        mChart.setMarkerView(mv);
        return mChart;
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private static String dateTransfer(String timestamp) {
        Calendar calendar = Calendar.getInstance();
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumIntegerDigits(2);
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String data = String.format("%s-%s %s:%s", nf.format(calendar.get(Calendar.MONTH)+1), nf.format(calendar.get(Calendar.DAY_OF_MONTH)),
                nf.format(calendar.get(Calendar.HOUR_OF_DAY)), nf.format(calendar.get(Calendar.MINUTE)));
        return data;
    }


}
