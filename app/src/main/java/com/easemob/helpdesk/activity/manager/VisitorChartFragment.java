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
import com.easemob.helpdesk.utils.DateUtils;
import com.hyphenate.kefusdk.entity.option.VisitorsScreenEntity;
import com.hyphenate.kefusdk.gsonmodel.workload.Entity;
import com.hyphenate.kefusdk.gsonmodel.workload.Response;
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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.utils.HDLog;

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
 * Created by liyuzhao on 16/7/6.
 */
public class VisitorChartFragment extends Fragment {

    private static final String TAG = VisitorChartFragment.class.getSimpleName();

    @BindView(R.id.tv_refresh)
    protected View tvRefresh;

    @BindView(R.id.trend_visitor_layout)
    protected FrameLayout visitorTrendLayout;

    @BindView(R.id.tv_visitor_total)
    protected TextView tvVisitorTotal;

    @BindView(R.id.visitor_chart_container)
    protected LinearLayout visiorChartContainer;

    private String[] colors = {"#1ba8ed", "#a1e5e2", "#56c13d", "#ff531a", "#4d4d4d", "#1f77b4", "#aec7e8"};

    private VisitorsScreenEntity screenEntity = new VisitorsScreenEntity();

	private Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.manager_fragment_visitors_chart, container, false);
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

    public void refreshCurrentView() {
        loadVisitorTotal();
        loadVisitorTrend();
    }

    @OnClick(R.id.tv_refresh)
    public void onClickByFilter(View view){
        tvVisitorTotal.setText("--");
        loadVisitorTotal();
    }

    private void loadVisitorTotal(){
        if (getActivity() == null){
            return;
        }
        HDClient.getInstance().adminCommonManager().getManageVisitorTotal(screenEntity, new HDDataCallBack<String>() {
            @Override
            public void onSuccess(final String value) {
                if (getActivity() == null) {
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        parseVisitorTotal(value);
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

    private void loadVisitorTrend(){
        HDClient.getInstance().adminCommonManager().getManageVisitorTrend(screenEntity, new HDDataCallBack<String>() {
            @Override
            public void onSuccess(final String value) {
                if (getActivity() == null) {
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        HDLog.d(TAG, "getManageVisitorTrend-value:" + value);
                        parseVisitorTrend(value);
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

    private void parseVisitorTotal(String value){
        //{"status":"OK","entities":[{"count":50.0}],"totalElements":1}
        JsonParser jsonParser = new JsonParser();
        JsonElement jsonElement = jsonParser.parse(value);
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        JsonArray  jsonArray = jsonObject.getAsJsonArray("entities");
        if (jsonArray != null && jsonArray.size() > 0){
            JsonObject jsonObject1 = jsonArray.get(0).getAsJsonObject();
            int visitorTotals = jsonObject1.get("count").getAsInt();
            tvVisitorTotal.setText(String.valueOf(visitorTotals));
        }else{
            tvVisitorTotal.setText("--");
        }
    }

    private void parseVisitorTrend(String value){
        visitorTrendLayout.removeAllViews();
        Gson gson = new Gson();
        Response response = gson.fromJson(value, Response.class);
        if (response == null){
            return;
        }
        List<Entity> resultBeanList = response.getEntities();
        if (resultBeanList == null || resultBeanList.size() == 0){
            TextView textView = new TextView(getContext());
            textView.setGravity(Gravity.CENTER);
            textView.setText("无数据");
            visitorTrendLayout.addView(textView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            return;
        }

        Collections.sort(resultBeanList, new Comparator<Entity>() {
            @Override
            public int compare(Entity lhs, Entity rhs) {
                return lhs.getType().compareTo(rhs.getType());
            }
        });
        BarChart barChart = getBarChart(resultBeanList);
        if (barChart != null) {
            visitorTrendLayout.addView(barChart, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        } else {
            TextView textView = new TextView(getContext());
            textView.setGravity(Gravity.CENTER);
            textView.setText("无数据");
            visitorTrendLayout.addView(textView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
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

        for (int i = 0; i < resultBeanList.size(); i++) {
            ArrayList<BarEntry> yVals1 = new ArrayList<>();
            Entity resultBean = resultBeanList.get(i);
            List<Map<String, Integer>> valueList = resultBean.getValue();
            for (int j = 0; j < valueList.size(); j++) {
                Map<String, Integer> itemMap = valueList.get(j);
                Set<String> keys = itemMap.keySet();
                for (String key :
                        keys) {
                    yVals1.add(new BarEntry(itemMap.get(key), j));
                    if (!xVals.contains(key)){
                        xVals.add(key);
                    }
                }

            }
            String stringType = resultBean.getType();
            if (TextUtils.isEmpty(stringType)){
                continue;
            }
            String showText;
            int color = Color.parseColor(colors[0]);
            switch (stringType) {
                case "app":
                case "one":
                case "channelId":
                    showText = "手机APP";
                    color = Color.parseColor(colors[0]);
                    break;
                case "webim":
                case "two":
                case "visitorTag":
                    showText = "网页";
                    color = Color.parseColor(colors[1]);
                    break;
                case "weixin":
                case "other":
                    showText = "微信";
                    color = Color.parseColor(colors[2]);
                    break;
                case "weibo":
                    showText = "微博";
                    color = Color.parseColor(colors[3]);
                    break;
                case "phone":
                    showText = "呼叫中心";
                    color = Color.parseColor(colors[4]);
                    break;
                case "rest":
                    showText = "REST";
                    color = Color.parseColor(colors[5]);
                    break;
                case "slack":
                    showText = "SLACK";
                    color = Color.parseColor(colors[6]);
                    break;
                default:
                    showText = stringType;
                    break;
            }
            showText = resultBean.getName();
            BarDataSet barDataSet = new BarDataSet(yVals1, showText);
            barDataSet.setBarSpacePercent(35f);
	        barDataSet.setDrawValues(false);
            barDataSet.setColor(color);
            barDataSets.add(barDataSet);
        }
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

    public void setScreenEntity(VisitorsScreenEntity screenEntity) {
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
