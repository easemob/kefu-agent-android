package com.easemob.helpdesk.activity.manager;

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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easemob.helpdesk.HDApplication;
import com.easemob.helpdesk.R;
import com.hyphenate.kefusdk.entity.option.WorkloadScreenEntity;
import com.hyphenate.kefusdk.gsonmodel.workload.Entity;
import com.hyphenate.kefusdk.gsonmodel.workload.Response;
import com.hyphenate.kefusdk.gsonmodel.workload.SessionByMessage;
import com.hyphenate.kefusdk.gsonmodel.workload.SessionByMessageResponse;
import com.hyphenate.kefusdk.gsonmodel.workload.SessionByTime;
import com.hyphenate.kefusdk.gsonmodel.workload.SessionByTimeResponse;
import com.hyphenate.kefusdk.gsonmodel.workload.SessionTag;
import com.hyphenate.kefusdk.gsonmodel.workload.SessionTagResponse;
import com.easemob.helpdesk.utils.DateUtils;
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
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.utils.HDLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by liyuzhao on 16/6/22.
 */
public class WorkloadChartFragment extends Fragment {

    private static final String TAG = WorkloadChartFragment.class.getSimpleName();

    @BindView(R.id.tv_message_count)
    protected TextView tvMessageCount;

    @BindView(R.id.tv_session_count)
    protected TextView tvSessionCount;

    @BindView(R.id.tv_messages_avg)
    protected TextView tvMessagesAvg;

    @BindView(R.id.tv_messages_max)
    protected TextView tvMessagesMax;

    @BindView(R.id.tv_session_time_avg)
    protected TextView tvSessionTimeAvg;

    @BindView(R.id.tv_session_time_max)
    protected TextView tvSessionTimeMax;

    @BindView(R.id.trend_chart_container)
    protected LinearLayout trendChartContainer;

    @BindView(R.id.trend_chart_layout)
    protected FrameLayout trendChartLayout;

    @BindView(R.id.session_tag_chart_layout)
    protected FrameLayout sessionTagChartLayout;

    @BindView(R.id.sessionbymessage_chart_layout)
    protected FrameLayout sessionByMessageChartLayout;

    @BindView(R.id.sessionbytime_chart_layout)
    protected FrameLayout sessionByTimeChartLayout;


    @BindView(R.id.tv_refresh)
    protected View tvRefresh;

    private int trendSessionTextColor = Color.parseColor("#1f77b4");
    private String[] colors = {"#1ba8ed", "#a1e5e2", "#56c13d", "#ff531a", "#4d4d4d"};

    private boolean drawValuesEnable = false;

	private WorkloadScreenEntity screenEntity = new WorkloadScreenEntity();

    private Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.manager_fragment_workload_chart, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        tvRefresh.setVisibility(View.VISIBLE);
	    screenEntity.setCurrentTimeInfo(DateUtils.getTimeInfoByCurrentWeek().getStartTime(), DateUtils.getTimeInfoByCurrentWeek().getEndTime());
	    refreshCurrentView();
    }


    @OnClick(R.id.tv_refresh)
    public void onClickByRefresh(View view) {
        tvMessageCount.setText("--");
        tvSessionCount.setText("--");
        tvMessagesAvg.setText("--");
        tvMessagesMax.setText("--");
        tvSessionTimeAvg.setText("--");
        tvSessionTimeMax.setText("--");
        loadMessageWorkload();
    }


    public void refreshCurrentView() {
        loadMessageWorkload();
        loadTrendTotal();
        loadSessionTag();
        loadSessionCountByMessage();
        loadSessionCountByTime();
    }


    private void loadSessionCountByTime() {
        HDClient.getInstance().adminCommonManager().getWorkloadDistSessionTime(screenEntity, new HDDataCallBack<String>() {
            @Override
            public void onSuccess(final String value) {
                HDLog.d(TAG, "getWorkloadDistSessionTime-value:" + value);
                if (TextUtils.isEmpty(value)) {
                    return;
                }
                if (getActivity() == null) {
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        parseSessionCountByTime2(value);
                    }
                });
            }

            @Override
            public void onError(int error, String errorMsg) {

            }

            @Override
            public void onAuthenticationException() {

            }
        });


    }

    private void loadSessionCountByMessage() {
        HDClient.getInstance().adminCommonManager().getWorkloadDistMessageCount(screenEntity, new HDDataCallBack<String>() {
            @Override
            public void onSuccess(final String value) {
                HDLog.d(TAG, "getWorkloadDistMessageCount-value:" + value);
                if (TextUtils.isEmpty(value)) {
                    return;
                }
                if (getActivity() == null) {
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        parseSessionCountByMessage2(value);
                    }
                });
            }

            @Override
            public void onError(int error, String errorMsg) {

            }

            @Override
            public void onAuthenticationException() {

            }
        });
    }

    public void setScreenEntity(WorkloadScreenEntity screenEntity) {
        this.screenEntity = screenEntity;
    }

    private void loadSessionTag() {
        HDClient.getInstance().adminCommonManager().getWorkloadSessionTag(screenEntity, new HDDataCallBack<String>() {
            @Override
            public void onSuccess(final String value) {
                HDLog.d(TAG, "getWorkloadSessionTag-value:" + value);
                if (TextUtils.isEmpty(value)) {
                    return;
                }
                if (getActivity() == null) {
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        parseSessionTag2(value);
                    }
                });
            }

            @Override
            public void onError(int error, String errorMsg) {

            }

            @Override
            public void onAuthenticationException() {

            }
        });


    }

    private void loadTrendTotal() {
        HDClient.getInstance().adminCommonManager().getWorkloadTrendTotal(screenEntity, new HDDataCallBack<String>() {
            @Override
            public void onSuccess(final String value) {
                HDLog.d(TAG, "getWorkloadTrendTotal-value:" + value);
                if (TextUtils.isEmpty(value)) {
                    return;
                }
                if (getActivity() == null) {
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        parseTrendTotal2(value);
                    }
                });
            }

            @Override
            public void onError(int error, String errorMsg) {
                HDLog.e(TAG, "getWorkloadTrendTotal-error:" + errorMsg);
            }

            @Override
            public void onAuthenticationException() {
                if (getActivity() == null) {
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

    private void loadMessageWorkload() {
        if (getActivity() == null) {
            return;
        }

        HDClient.getInstance().adminCommonManager().getStatisticsWorkload(screenEntity, new HDDataCallBack<String>() {
            @Override
            public void onSuccess(final String value) {
                if (getActivity() == null) {
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        parseStatsticsWorkloadValue(value);
                    }
                });

            }

            @Override
            public void onError(int error, String errorMsg) {
                HDLog.e(TAG, "getStatisticsWorkload-" + error + ";msg:" + errorMsg);
            }

            @Override
            public void onAuthenticationException() {
                if (getActivity() == null) {
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

    private void parseStatsticsWorkloadValue(String value) {
        try {
            JSONObject jsonObject = new JSONObject(value);
            JSONArray jsonEntities = jsonObject.getJSONArray("entities");
            JSONObject jsonOne = jsonEntities.getJSONObject(0);
            long cnt_sc = jsonOne.getLong("cnt_sc");
            long max_st = jsonOne.getLong("max_st");
            long avg_mc = jsonOne.getLong("avg_mc");
            long cnt_mc = jsonOne.getLong("cnt_mc");
            long avg_st = jsonOne.getLong("avg_st");
            long max_mc = jsonOne.getLong("max_mc");
            tvMessageCount.setText(String.valueOf(cnt_mc));
            tvSessionCount.setText(String.valueOf(cnt_sc));
            tvMessagesAvg.setText(String.valueOf(avg_mc));
            tvMessagesMax.setText(String.valueOf(max_mc));
            tvSessionTimeAvg.setText(DateUtils.convertFromSecond((int) avg_st));
            tvSessionTimeMax.setText(DateUtils.convertFromSecond((int) max_st));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void parseTrendTotal2(String value){
        trendChartLayout.removeAllViews();
        Gson gson = new Gson();
        Response response = gson.fromJson(value, Response.class);
        if (response == null){
            return;
        }
        List<Entity> entities = response.getEntities();
        if (entities == null || entities.size() == 0){
            TextView textView = new TextView(getContext());
            textView.setGravity(Gravity.CENTER);
            textView.setText("无数据");
            trendChartLayout.addView(textView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            return;
        }

        Collections.sort(entities, new Comparator<Entity>() {
            @Override
            public int compare(Entity lhs, Entity rhs) {
                return lhs.getType().compareTo(rhs.getType());
            }
        });

        BarChart barChart = getBarChart(entities);
        if (barChart != null) {
            trendChartLayout.addView(barChart, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        } else {
            TextView textView = new TextView(getContext());
            textView.setGravity(Gravity.CENTER);
            textView.setText("无数据");
            trendChartLayout.addView(textView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }


    }

    private void parseSessionCountByTime2(String value){
        sessionByTimeChartLayout.removeAllViews();
        Gson gson = new Gson();
        SessionByTimeResponse response = gson.fromJson(value, SessionByTimeResponse.class);
        if(response == null){
            return;
        }
        List<SessionByTime> responseEntities = response.getEntities();
        if (responseEntities == null || responseEntities.size() == 0){
            TextView textView = new TextView(getContext());
            textView.setGravity(Gravity.CENTER);
            textView.setText("无数据");
            sessionTagChartLayout.addView(textView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            return;
        }
        BarChart mChart = new BarChart(getContext());
        mChart.setDrawBarShadow(false);
        mChart.setDrawValueAboveBar(true);
        mChart.setDescription("");
        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        mChart.setMaxVisibleValueCount(60);


        // scaling can now only be done on x- and y-axis separately
        mChart.setPinchZoom(false);
        mChart.setDoubleTapToZoomEnabled(false);
        mChart.setDrawGridBackground(false);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setSpaceBetweenLabels(2);
        xAxis.setValueFormatter(new MyXAxisValueFormatter());


//        YAxisValueFormatter custom = new MyYAxisValueFormatter();
        mChart.getAxisRight().setEnabled(false);
        YAxis leftAxis = mChart.getAxisLeft();
//        leftAxis.setLabelCount(2, false);
        leftAxis.setValueFormatter(new LargeValueFormatter());
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
        ArrayList<BarEntry> yVals1 = new ArrayList<>();
        for (int i = 0; i < responseEntities.size(); i++) {
            SessionByTime resultBean = responseEntities.get(i);
            xVals.add(resultBean.getName());
            yVals1.add(new BarEntry(resultBean.getCount(), i));
        }
        BarDataSet barDataSet = new BarDataSet(yVals1, "会话时长");
        barDataSet.setBarSpacePercent(35f);
        barDataSet.setDrawValues(drawValuesEnable);
        barDataSet.setColor(trendSessionTextColor);

        barDataSets.add(barDataSet);
        BarData data = new BarData(xVals, barDataSets);
        data.setValueTextSize(10f);
        mChart.setData(data);
        mChart.setScaleYEnabled(false);
        MyMarkerView mv = new MyMarkerView(getContext(), R.layout.custom_marker_view);
        mChart.setMarkerView(mv);
        sessionByTimeChartLayout.addView(mChart, getChartLayoutParams());

    }

    private FrameLayout.LayoutParams getChartLayoutParams(){
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.leftMargin = 5;
        layoutParams.topMargin = 5;
        layoutParams.rightMargin = 5;
        layoutParams.bottomMargin = 5;
        return layoutParams;
    }

    private void parseSessionCountByMessage2(String value){
        sessionByMessageChartLayout.removeAllViews();
        Gson gson = new Gson();
        SessionByMessageResponse response = gson.fromJson(value, SessionByMessageResponse.class);
        if(response == null){
            return;
        }
        List<SessionByMessage> responseEntities = response.getEntities();
        if (responseEntities == null || responseEntities.size() == 0){
            TextView textView = new TextView(getContext());
            textView.setGravity(Gravity.CENTER);
            textView.setText("无数据");
            sessionTagChartLayout.addView(textView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            return;
        }
        BarChart mChart = new BarChart(getContext());
        mChart.setDrawBarShadow(false);
        mChart.setDrawValueAboveBar(true);
        mChart.setDescription("");
        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        mChart.setMaxVisibleValueCount(60);


        // scaling can now only be done on x- and y-axis separately
        mChart.setPinchZoom(false);
        mChart.setDoubleTapToZoomEnabled(false);
        mChart.setDrawGridBackground(false);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setSpaceBetweenLabels(2);
        xAxis.setValueFormatter(new MyXAxisValueFormatter());


//        YAxisValueFormatter custom = new MyYAxisValueFormatter();
        mChart.getAxisRight().setEnabled(false);
        YAxis leftAxis = mChart.getAxisLeft();
//        leftAxis.setLabelCount(2, false);
        leftAxis.setValueFormatter(new LargeValueFormatter());
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
        ArrayList<BarEntry> yVals1 = new ArrayList<>();
        for (int i = 0; i < responseEntities.size(); i++) {
            SessionByMessage resultBean = responseEntities.get(i);
            xVals.add(resultBean.getName());
            yVals1.add(new BarEntry(resultBean.getCount(), i));
        }
        BarDataSet barDataSet = new BarDataSet(yVals1, "会话消息数");
        barDataSet.setBarSpacePercent(35f);
        barDataSet.setDrawValues(drawValuesEnable);
        barDataSet.setColor(trendSessionTextColor);

        barDataSets.add(barDataSet);
        BarData data = new BarData(xVals, barDataSets);
        data.setValueTextSize(10f);
        mChart.setData(data);
        mChart.setScaleYEnabled(false);
        MyMarkerView mv = new MyMarkerView(getContext(), R.layout.custom_marker_view);
        mChart.setMarkerView(mv);
        sessionByMessageChartLayout.addView(mChart, getChartLayoutParams());

    }

    private void parseSessionTag2(String value){
        sessionTagChartLayout.removeAllViews();
        Gson gson = new Gson();
        SessionTagResponse response = gson.fromJson(value, SessionTagResponse.class);
        if (response == null){
            return;
        }
        List<SessionTag> responseEntities = response.getEntities();
        if (responseEntities == null || responseEntities.size() == 0){
            TextView textView = new TextView(getContext());
            textView.setGravity(Gravity.CENTER);
            textView.setText("无数据");
            sessionTagChartLayout.addView(textView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            return;
        }
        BarChart mChart = new BarChart(getContext());
        mChart.setDrawBarShadow(false);
        mChart.setDrawValueAboveBar(true);
        mChart.setDescription("");
        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        mChart.setMaxVisibleValueCount(60);


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
        ArrayList<BarEntry> yVals1 = new ArrayList<>();
        for (int i = 0; i < responseEntities.size(); i++) {
            SessionTag resultBean = responseEntities.get(i);
            if (resultBean.getName().length() <= 3) {
                xVals.add(resultBean.getName());
            } else {
                xVals.add(resultBean.getName().substring(0, 3) + "...");
            }
            yVals1.add(new BarEntry(resultBean.getCount(), i));
        }
        BarDataSet barDataSet = new BarDataSet(yVals1, "会话标签");
        barDataSet.setBarSpacePercent(35f);
        barDataSet.setDrawValues(drawValuesEnable);
        barDataSet.setColor(trendSessionTextColor);

        barDataSets.add(barDataSet);
        BarData data = new BarData(xVals, barDataSets);
        data.setValueTextSize(10f);
        mChart.setData(data);
        mChart.setScaleYEnabled(false);
        MyMarkerView mv = new MyMarkerView(getContext(), R.layout.custom_marker_view);
        mChart.setMarkerView(mv);
        sessionTagChartLayout.addView(mChart, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

    }

    private BarChart getBarChart(List<Entity> resultBeanList){
        BarChart mChart = new BarChart(getContext());
        mChart.setDrawBarShadow(false);
        mChart.setDrawValueAboveBar(true);
        mChart.setDescription("");
        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        mChart.setMaxVisibleValueCount(60);


        // scaling can now only be done on x- and y-axis separately
        mChart.setPinchZoom(false);
        mChart.setDoubleTapToZoomEnabled(false);
        mChart.setDrawGridBackground(false);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setSpaceBetweenLabels(2);
        xAxis.setValueFormatter(new MyXAxisValueFormatter());

        mChart.getAxisRight().setEnabled(false);
        YAxis leftAxis = mChart.getAxisLeft();
//        leftAxis.setLabelCount(2, false);
        leftAxis.setValueFormatter(new LargeValueFormatter());
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
//        Set<Long> xAxisValue = new TreeSet<>();

        for (int i = 0; i < resultBeanList.size(); i++) {
            ArrayList<BarEntry> yVals1 = new ArrayList<>();
            Entity resultBean = resultBeanList.get(i);
            List<Map<String, Integer>> valueList = resultBean.getValue();
            for (int j = 0; j < valueList.size(); j++) {
                Map<String, Integer> itemMap = valueList.get(j);
                Set<String> keys = itemMap.keySet();
                for (String key : keys) {
                    yVals1.add(new BarEntry(itemMap.get(key), j));
//                    xAxisValue.add(key);
                    if (!xVals.contains(key)){
                        xVals.add(key);
                    }
                }
            }

            BarDataSet barDataSet = new BarDataSet(yVals1, resultBean.getType());
            barDataSet.setBarSpacePercent(35f);
            barDataSet.setDrawValues(drawValuesEnable);
            barDataSet.setColor(Color.parseColor(colors[i]));
            barDataSets.add(barDataSet);
        }
//        for (Long long1:xAxisValue) {
//            xVals.add(String.valueOf(long1));
//        }

        Collections.sort(xVals, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                try {
                    long long1 = Long.parseLong(lhs);
                    long long2 = Long.parseLong(rhs);
                    return (int) (long1 - long2);
                } catch (Exception e) {
                    return 0;
                }
            }
        });

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
        if (unbinder != null){
            unbinder.unbind();
        }
    }

}
