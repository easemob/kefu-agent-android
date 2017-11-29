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
import android.widget.TextView;

import com.easemob.helpdesk.HDApplication;
import com.easemob.helpdesk.R;
import com.hyphenate.kefusdk.entity.option.WorkmanshipScreenEntity;
import com.hyphenate.kefusdk.gsonmodel.workman.AvgResTimeResponse;
import com.hyphenate.kefusdk.gsonmodel.workman.EffectiveResponse;
import com.hyphenate.kefusdk.gsonmodel.workman.FirstResTimeResponse;
import com.hyphenate.kefusdk.gsonmodel.workman.VisitorMarkResponse;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.view.PieChartView;

/**
 * Created by liyuzhao on 16/6/23.
 */
public class WorkmanChartFragment extends Fragment {

    private static final String TAG = "WorkmanChartFragment";

    @BindView(R.id.tv_satisfaction)
    protected TextView tvSatisfaction;

    @BindView(R.id.tv_answer_avg)
    protected TextView tvAnswerAvg;

    @BindView(R.id.tv_answer_max)
    protected TextView tvAnswerMax;

    @BindView(R.id.tv_session_answer_avg)
    protected TextView tvSessionAnswerAvg;

    @BindView(R.id.tv_session_answer_max)
    protected TextView tvSessionAnswerMax;

    @BindView(R.id.satisfaction_pie_layout)
    protected FrameLayout satisfactionPieLayout;

    @BindView(R.id.effective_session_layout)
    protected FrameLayout effectiveSessionLayout;

    @BindView(R.id.first_res_time_layout)
    protected FrameLayout firstResTimeLayout;

    @BindView(R.id.avg_res_time_layout)
    protected FrameLayout avgResTimeLayout;

    @BindView(R.id.tv_refresh)
    protected View tvRefresh;

    private boolean hasLables = true;
    private boolean hasLablesOutside = true;
    private boolean hasCenterCircle = false;
    private boolean hasLableForSelected = false;

    private int[] colors = {Color.parseColor("#1f77b4"), Color.parseColor("#aec7e8")};
    private int trendSessionTextColor = Color.parseColor("#1f77b4");
    DecimalFormat df = new DecimalFormat("#.#");

    private boolean drawValuesEnable = false;

    private WorkmanshipScreenEntity screenEntity = new WorkmanshipScreenEntity();

    private Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.manage_fragment_workman_chart, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        tvRefresh.setVisibility(View.VISIBLE);
        screenEntity.setCurrentTimeInfo(DateUtils.getTimeInfoByCurrentWeek().getStartTime(), DateUtils.getTimeInfoByCurrentWeek().getEndTime());
        refreshCurrentView();
    }

    @OnClick(R.id.tv_refresh)
    public void onClickByRefresh(){
        tvSatisfaction.setText("--");
        tvAnswerAvg.setText("--");
        tvAnswerMax.setText("--");
        tvSessionAnswerAvg.setText("--");
        tvSessionAnswerMax.setText("--");
        loadOverviewData();
    }

    public void refreshCurrentView(){
        loadOverviewData();
        loadStaticfaction();
        loadEffectiveSession();
        loadFirstResTime();
        loadAvgResTime();
    }

    private void loadAvgResTime(){
        HDClient.getInstance().adminCommonManager().getWorkmanDistAvgResTime(screenEntity, new HDDataCallBack<String>() {

            @Override
            public void onSuccess(final String value) {
                if (TextUtils.isEmpty(value)) {
                    return;
                }
                if (getActivity() == null) {
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        parseDistAvgResTime(value);
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

    private void loadFirstResTime(){
        HDClient.getInstance().adminCommonManager().getWorkmanDistFirstResTime(screenEntity, new HDDataCallBack<String>() {

            @Override
            public void onSuccess(final String value) {
                if (TextUtils.isEmpty(value)) {
                    return;
                }
                if (getActivity() == null) {
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        parseDistFirstResTime(value);
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

    private void loadEffectiveSession(){
        HDClient.getInstance().adminCommonManager().getWorkmanDistEffective(screenEntity, new HDDataCallBack<String>() {

            @Override
            public void onSuccess(final String value) {
                if (TextUtils.isEmpty(value)) {
                    return;
                }
                if (getActivity() == null) {
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        parseDistEffective(value);
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

    private void loadStaticfaction(){
        HDClient.getInstance().adminCommonManager().getWorkmanDistVisitorMark(screenEntity, new HDDataCallBack<String>() {
            @Override
            public void onSuccess(final String value) {
                if (TextUtils.isEmpty(value)) {
                    return;
                }
                if (getActivity() == null) {
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        parseDistVisitorMark(value);
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

    private void loadOverviewData() {
        HDClient.getInstance().adminCommonManager().getStatisticsWorkQualityTotal(screenEntity, new HDDataCallBack<String>() {
            @Override
            public void onSuccess(final String value) {
                if (getActivity() == null) {
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        parseWorkQualityTotalData(value);
                    }
                });

            }

            @Override
            public void onError(int error, String errorMsg) {
                if (getActivity() == null) {
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

    private void parseDistEffective(String data){
        //{"status":"OK","entities":[{"count":96.0,"name":"有效","key":1},{"count":8.0,"name":"无效","key":0}],"totalElements":2}
        Gson gson = new Gson();
        EffectiveResponse response = gson.fromJson(data, EffectiveResponse.class);
        List<EffectiveResponse.EntitiesBean> entities = response.getEntities();
        double sumCount = 0.0;

        for (EffectiveResponse.EntitiesBean bean : entities) {
            sumCount += bean.getCount();
        }
        final PieChartView chartView = new PieChartView(getContext());
        PieChartData chartData;
        int numValues = entities.size();
        List<SliceValue> values = new ArrayList<>();

        for (int i = 0; i < numValues; ++i){
            EffectiveResponse.EntitiesBean bean = entities.get(i);
            SliceValue sliceValue = new SliceValue((float) bean.getCount(), colors[i]);
            double percent = bean.getCount() * 100 /sumCount;
            sliceValue.setLabel(bean.getName() + ":" + df.format(percent) + "%");
            values.add(sliceValue);
        }
        chartData = new PieChartData(values);
        chartData.setHasLabels(hasLables);
        chartData.setHasLabelsOnlyForSelected(hasLableForSelected);
        chartData.setHasLabelsOutside(hasLablesOutside);
        chartData.setHasCenterCircle(hasCenterCircle);
        chartView.setPieChartData(chartData);
        chartView.setValueSelectionEnabled(false);
        chartView.setInteractive(false);
        effectiveSessionLayout.removeAllViews();
        effectiveSessionLayout.addView(chartView);
    }

    private void parseDistAvgResTime(String value){
        avgResTimeLayout.removeAllViews();
        Gson gson = new Gson();
        AvgResTimeResponse response = gson.fromJson(value, AvgResTimeResponse.class);
        if (response == null){
            return;
        }
        List<AvgResTimeResponse.EntitiesBean> responseEntities = response.getEntities();
        if (responseEntities == null || responseEntities.size() == 0){
            TextView textView = new TextView(getContext());
            textView.setGravity(Gravity.CENTER);
            textView.setText("无数据");
            avgResTimeLayout.addView(textView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
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
            AvgResTimeResponse.EntitiesBean resultBean = responseEntities.get(i);
            xVals.add(resultBean.getName());
            yVals1.add(new BarEntry((float) resultBean.getCount(), i));
        }
        BarDataSet barDataSet = new BarDataSet(yVals1, "响应时长");
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
        avgResTimeLayout.addView(mChart, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private void parseDistFirstResTime(String value){
        firstResTimeLayout.removeAllViews();
        Gson gson = new Gson();
        FirstResTimeResponse response = gson.fromJson(value, FirstResTimeResponse.class);
        if (response == null){
            return;
        }
        List<FirstResTimeResponse.EntitiesBean> responseEntities = response.getEntities();
        if (responseEntities == null || responseEntities.size() == 0){
            TextView textView = new TextView(getContext());
            textView.setGravity(Gravity.CENTER);
            textView.setText("无数据");
            firstResTimeLayout.addView(textView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
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
            FirstResTimeResponse.EntitiesBean resultBean = responseEntities.get(i);
            xVals.add(resultBean.getName());
            yVals1.add(new BarEntry((float) resultBean.getCount(), i));
        }
        BarDataSet barDataSet = new BarDataSet(yVals1, "首次响应时长");
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
        firstResTimeLayout.addView(mChart, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private void parseDistVisitorMark(String data){
        Gson gson = new Gson();
        VisitorMarkResponse response = gson.fromJson(data, VisitorMarkResponse.class);
        final List<VisitorMarkResponse.EntitiesBean> entitiesBeans = response.getEntities();
        final PieChartView chartView = new PieChartView(getContext());
        PieChartData chartData;
        int numValues = entitiesBeans.size();
        List<SliceValue> values = new ArrayList<>();

        double totalCount = 0.0;
        for (int i = 0; i < numValues; ++i){
            VisitorMarkResponse.EntitiesBean entitiesBean = entitiesBeans.get(i);
            totalCount += entitiesBean.getCount();
        }

        for (int i = 0; i < numValues; ++i){
            VisitorMarkResponse.EntitiesBean entitiesBean = entitiesBeans.get(i);
            SliceValue sliceValue = new SliceValue(entitiesBean.getCount(), colors[i]);
            double percent = entitiesBean.getCount() * 100 / totalCount;
            sliceValue.setLabel(entitiesBean.getName() + ":" + df.format(percent) + "%");
            values.add(sliceValue);
        }
        chartData = new PieChartData(values);
        chartData.setHasLabels(hasLables);
        chartData.setHasLabelsOnlyForSelected(hasLableForSelected);
        chartData.setHasLabelsOutside(hasLablesOutside);
        chartData.setHasCenterCircle(hasCenterCircle);

        chartView.setPieChartData(chartData);

        chartView.setValueSelectionEnabled(false);
        chartView.setInteractive(false);
        satisfactionPieLayout.removeAllViews();
        satisfactionPieLayout.addView(chartView);
    }

    private void parseWorkQualityTotalData(String data){
        try {
            JSONObject jsonObject = new JSONObject(data);
            JSONArray jsonArray = jsonObject.getJSONArray("entities");
            JSONObject jsonTotal = jsonArray.getJSONObject(0);
            double avg_vm = jsonTotal.getDouble("avg_vm");
            long max_ar = jsonTotal.getLong("max_ar");
            long max_fr = jsonTotal.getLong("max_fr");
            long avg_ar = jsonTotal.getLong("avg_ar");
            long avg_fr = jsonTotal.getLong("avg_fr");
            tvSatisfaction.setText(String.valueOf(avg_vm));
            tvAnswerAvg.setText(DateUtils.convertFromSecond((int) avg_fr));
            tvAnswerMax.setText(DateUtils.convertFromSecond((int) max_fr));
            tvSessionAnswerAvg.setText(DateUtils.convertFromSecond((int) avg_ar));
            tvSessionAnswerMax.setText(DateUtils.convertFromSecond((int) max_ar));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setScreenEntity(WorkmanshipScreenEntity screenEntity) {
        this.screenEntity = screenEntity;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null){
            unbinder.unbind();
        }
    }
}
