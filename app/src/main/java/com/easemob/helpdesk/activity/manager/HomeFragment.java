package com.easemob.helpdesk.activity.manager;

import android.app.Activity;
import android.content.Intent;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easemob.helpdesk.HDApplication;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.utils.AvatarManager;
import com.hyphenate.kefusdk.gsonmodel.manager.CountGroupByAgentResponse;
import com.hyphenate.kefusdk.gsonmodel.manager.SessionTrendResponse;
import com.easemob.helpdesk.utils.CommonUtils;
import com.easemob.helpdesk.utils.DateUtils;
import com.easemob.helpdesk.utils.TimeInfo;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.entity.user.HDUser;
import com.hyphenate.kefusdk.utils.HDLog;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by liyuzhao on 16/6/12.
 */
public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    private static final int REQUEST_CODE_SESSION_FILTER = 0x01;
    private static final int REQUEST_CODE_MESSAGE_FILTER = 0x02;
    private String[] colors = {"#1ba8ed", "#a1e5e2", "#56c13d", "#ff531a", "#4d4d4d"};

    DecimalFormat deformat = new DecimalFormat("###,###,###,##0");
    /**
     * 今日会话数
     */
    @BindView(R.id.tv_sessions_today)
    protected TextView tvSessionsToday;
    /**
     * 处理中会话数
     */
    @BindView(R.id.tv_inprocess_sessions)
    protected TextView tvInProcessSessions;
    /**
     * 在线客服数
     */
    @BindView(R.id.tv_customer_service_online)
    protected TextView tvCustomerServiceOnline;
    /**
     * 今日消息数
     */
    @BindView(R.id.tv_messages_today)
    protected TextView tvMessagesToday;

    @BindView(R.id.session_trend_layout)
    protected FrameLayout sessionTrendLayout;

    @BindView(R.id.message_trend_layout)
    protected FrameLayout messageTrendLayout;

    @BindView(R.id.currentsession_chart_layout)
    protected FrameLayout currentSessionChartLayout;

    @BindView(R.id.iv_avatar)
    protected ImageView ivAvatar;
    @BindView(R.id.iv_status)
    protected ImageView ivStatus;

    @BindView(R.id.tv_refresh)
    protected View ivRefresh;

    @BindView(R.id.session_chart_container)
    protected LinearLayout sessionChartContainer;

    @BindView(R.id.message_chart_container)
    protected LinearLayout messageChartContainer;

    @BindView(R.id.message_filter)
    protected View messageFilter;

    @BindView(R.id.session_filter)
    protected View sessionFilter;

    protected HDUser currentLoginUser;

    protected TimeInfo currentSessionTimeInfo;
    protected TimeInfo currentMessageTimeInfo;

    protected String sessionDateInterval = "1d";
    protected String messageDateInterval = "1d";

    private boolean drawValueEnable = false;
    private Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manager_home, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        currentLoginUser = HDClient.getInstance().getCurrentUser();
        currentSessionTimeInfo = DateUtils.getTimeInfoByCurrentMonth();
        currentMessageTimeInfo = DateUtils.getTimeInfoByCurrentMonth();
        initView();
        loadFirstStatus();
        refreshAgentAvatar();

        loadTodayDataFromRemote();
        loadSessionTrendFromRemote();
        loadMessageTrendFromRemote();
        loadCurrentDaySessionCountByAgent();

    }

    private void loadFirstStatus() {
        if (currentLoginUser != null) {
            refreshOnline(currentLoginUser.getOnLineState());
        }
    }

    public void refreshOnline(String status) {
        CommonUtils.setAgentStatusView(ivStatus, status);
    }

    public void refreshAgentAvatar() {
        if (ivAvatar != null) {
            AvatarManager.getInstance().refreshAgentAvatar(getActivity(), ivAvatar);
        }
    }

    private void initView() {
    }

    @OnClick(R.id.session_filter)
    protected void onClickBySessionFilter(View view){
        Intent intent = new Intent();
        intent.setClass(getContext(), HomeFilterActivity.class);
        intent.putExtra("timeinfo", currentSessionTimeInfo);
        intent.putExtra("dateInterval", sessionDateInterval);
        startActivityForResult(intent, REQUEST_CODE_SESSION_FILTER);
    }

    @OnClick(R.id.message_filter)
    protected void onClickByMessageFilter(View view){
        Intent intent = new Intent();
        intent.setClass(getContext(), HomeFilterActivity.class);
        intent.putExtra("timeinfo", currentMessageTimeInfo);
        intent.putExtra("dateInterval", messageDateInterval);
        startActivityForResult(intent, REQUEST_CODE_MESSAGE_FILTER);
    }

    private void loadMessageTrendFromRemote() {
        if (currentLoginUser == null){
            return;
        }

        HDClient.getInstance().adminCommonManager().getMessageTrend(currentMessageTimeInfo.getStartTime(), currentMessageTimeInfo.getEndTime(), messageDateInterval, new HDDataCallBack<String>() {
            @Override
            public void onSuccess(final String value) {
                if (getActivity() == null) {
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        parseMessageTrend3(value);
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

    private void loadSessionTrendFromRemote() {
        HDClient.getInstance().adminCommonManager().getSessionTrend(currentSessionTimeInfo.getStartTime(), currentSessionTimeInfo.getEndTime(), sessionDateInterval, new HDDataCallBack<String>() {
            @Override
            public void onSuccess(final String value) {
                HDLog.d(TAG, "getSessionTrend-value:" + value);
                if (getActivity() == null) {
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        parseSessionTrend3(value);
                    }
                });
            }

            @Override
            public void onError(int error, String errorMsg) {
                HDLog.e(TAG, "getSessionTrend-error:" + errorMsg);
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

    private void loadCurrentDaySessionCountByAgent(){
        HDClient.getInstance().adminCommonManager().getCurrentDayServiceSessionCountGroupByAgent(new HDDataCallBack<String>() {
            @Override
            public void onSuccess(final String value) {
                if (getActivity() == null) {
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        parseCurrentDaySessionCountByAgent2(value);
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

    private void parseCurrentDaySessionCountByAgent2(String value){
        Gson gson = new Gson();
        List<CountGroupByAgentResponse> responseList = gson.fromJson(value, new TypeToken<List<CountGroupByAgentResponse>>(){}.getType());
        currentSessionChartLayout.removeAllViews();
        if (responseList == null || responseList.isEmpty()){
            TextView textView = new TextView(getContext());
            textView.setGravity(Gravity.CENTER);
            textView.setText("无数据");
            currentSessionChartLayout.addView(textView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
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

        int count = responseList.size();
        ArrayList<String> xVals = new ArrayList<>();
        ArrayList<BarEntry> yVals1 = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            CountGroupByAgentResponse response = responseList.get(i);
            String xStrVal = response.getAgentNiceName();
            xVals.add(xStrVal == null ? "" : xStrVal);
            yVals1.add(new BarEntry(response.getCount(), i));
        }

        BarDataSet set1;
        if (mChart.getData() != null && mChart.getData().getDataSetCount() > 0){
            set1 = (BarDataSet) mChart.getData().getDataSetByIndex(0);
            set1.setYVals(yVals1);
            mChart.getData().setXVals(xVals);
            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();

        }else{
            set1 = new BarDataSet(yVals1,"客服");
            set1.setBarSpacePercent(35f);
//            set1.setColors(ColorTemplate.MATERIAL_COLORS);
            set1.setColor(Color.parseColor(colors[0]));

            set1.setValueFormatter(new ValueFormatter() {

                @Override
                public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                    return deformat.format(value);
                }
            });
            set1.setDrawValues(drawValueEnable);
            ArrayList<IBarDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1);
            BarData data = new BarData(xVals, dataSets);
            data.setValueTextSize(10f);
            mChart.setData(data);
        }
        mChart.setScaleYEnabled(false);
        MyMarkerView mv = new MyMarkerView(getContext(), R.layout.custom_marker_view);
        mChart.setMarkerView(mv);
        currentSessionChartLayout.addView(mChart, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private void loadTodayDataFromRemote() {
        //获取今日新会话数
        HDClient.getInstance().adminCommonManager().getTodayNewServiceSessionCount(new HDDataCallBack<String>() {
            @Override
            public void onSuccess(final String value) {
                if (getActivity() == null) {
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (TextUtils.isDigitsOnly(value)){
                            tvSessionsToday.setText(value);
                        }else{
                            tvSessionsToday.setText("--");
                        }
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
                        tvSessionsToday.setText("--");
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

        //获取处理中会话数
        HDClient.getInstance().adminCommonManager().getCurrentServiceSessionCount(new HDDataCallBack<String>() {
            @Override
            public void onSuccess(final String value) {
                if (getActivity() == null) {
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (TextUtils.isDigitsOnly(value)){
                            tvInProcessSessions.setText(value);
                        }else{
                            tvInProcessSessions.setText("--");
                        }

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
                        tvInProcessSessions.setText("--");
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

        //获取在线客服数
        HDClient.getInstance().adminCommonManager().getCurrentOnlineAgentCount(new HDDataCallBack<String>() {
            @Override
            public void onSuccess(final String value) {
                if (getActivity() == null) {
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (TextUtils.isDigitsOnly(value)){
                            tvCustomerServiceOnline.setText(value);
                        }else{
                            tvCustomerServiceOnline.setText("--");
                        }
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
                        tvCustomerServiceOnline.setText("--");
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

        //获取消息数
        HDClient.getInstance().adminCommonManager().getTodayTotalMessageCount(new HDDataCallBack<String>() {
            @Override
            public void onSuccess(final String value) {
                if (getActivity() == null) {
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (TextUtils.isDigitsOnly(value)){
                            tvMessagesToday.setText(value);
                        }else{
                            tvMessagesToday.setText("--");
                        }
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
                        tvMessagesToday.setText("--");
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

    @OnClick(R.id.tv_refresh)
    public void onClickByRefresh(View view) {
        tvSessionsToday.setText("--");
        tvInProcessSessions.setText("--");
        tvCustomerServiceOnline.setText("--");
        tvMessagesToday.setText("--");
        loadTodayDataFromRemote();

    }

    private BarChart getBarChart(List<SessionTrendResponse.ResultBean> resultBeanList, String dateInterval){
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
        xAxis.setValueFormatter(new MyXAxisValueFormatter(dateInterval));


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
        ArrayList<String> xVals = new ArrayList<>();
        for (int i = 0; i < resultBeanList.size(); i++) {
            SessionTrendResponse.ResultBean resultBean = resultBeanList.get(i);
            List<SessionTrendResponse.ResultBean.ValueBean> resultBeanValue = resultBean.getValue();
            for (int j = 0; j < resultBeanValue.size(); j++) {
                SessionTrendResponse.ResultBean.ValueBean valueBean = resultBeanValue.get(j);
                if (!xVals.contains(String.valueOf(valueBean.getTime()))){
                    xVals.add(String.valueOf(valueBean.getTime()));
                }
            }
        }
        Collections.sort(xVals, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                try{
                    long long1 = Long.parseLong(lhs);
                    long long2 = Long.parseLong(rhs);
                    if (long1 == long2){
                        return 0;
                    }else if (long1 > long2){
                        return 1;
                    }else{
                        return -1;
                    }
                }catch (Exception e){
                    return 0;
                }
            }
        });

        List<IBarDataSet> barDataSets = new ArrayList<>();
        for (int i = 0; i < resultBeanList.size(); i++) {
            ArrayList<BarEntry> yVals1 = new ArrayList<>();
            SessionTrendResponse.ResultBean resultBean = resultBeanList.get(i);
            List<SessionTrendResponse.ResultBean.ValueBean> tempResultBeanValues = new ArrayList<>();
            List<SessionTrendResponse.ResultBean.ValueBean> resultBeanValue = resultBean.getValue();
            for (int m = 0; m < xVals.size(); m++) {
                SessionTrendResponse.ResultBean.ValueBean tempBean = new SessionTrendResponse.ResultBean.ValueBean();
                long xValue = Long.parseLong(xVals.get(m));
                tempBean.setTime(xValue);
                int defaultValue = 0;
                for (int n = 0; n < resultBeanValue.size(); n++) {
                    SessionTrendResponse.ResultBean.ValueBean tmpBean = resultBeanValue.get(n);
                    if (tmpBean.getTime() == xValue){
                        defaultValue = tmpBean.getValue();
                        break;
                    }
                }
                tempBean.setValue(defaultValue);
                tempResultBeanValues.add(tempBean);
            }

            for (int j = 0; j < tempResultBeanValues.size(); j++) {
                SessionTrendResponse.ResultBean.ValueBean valueBean = tempResultBeanValues.get(j);
                yVals1.add(new BarEntry(valueBean.getValue(), j));
            }
            String showText;
            String stringType = resultBean.getType();
            int color = Color.parseColor(colors[0]);
            switch (stringType) {
                case "app":
                    showText = "手机APP";
                    color = Color.parseColor(colors[0]);
                    break;
                case "webim":
                    showText = "网页";
                    color = Color.parseColor(colors[1]);
                    break;
                case "weixin":
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
                default:
                    showText = stringType;
                    break;
            }
            BarDataSet barDataSet = new BarDataSet(yVals1, showText);
            barDataSet.setBarSpacePercent(35f);
            barDataSet.setColor(color);
            barDataSet.setDrawValues(drawValueEnable);
            barDataSets.add(barDataSet);

        }

        BarData data = new BarData(xVals, barDataSets);
        data.setValueTextSize(10f);
        mChart.setData(data);
        mChart.setScaleYEnabled(false);
        // create a custom MarkerView (extend MarkerView) and specify the layout
        // to use for it
        MyMarkerView mv = new MyMarkerView(getContext(), R.layout.custom_marker_view);
        // define an offset to change the original position of the marker
        // (optional)
        // mv.setOffsets(-mv.getMeasuredWidth() / 2, -mv.getMeasuredHeight());

        // set the marker to the chart
        mChart.setMarkerView(mv);

        return mChart;
    }

    private void parseMessageTrend3(String value){
        HDLog.d(TAG, "parseMessageTrend:" + value);
        messageTrendLayout.removeAllViews();
        Gson gson = new Gson();
        SessionTrendResponse response = gson.fromJson(value, SessionTrendResponse.class);
        if (response == null){
            return;
        }
        List<SessionTrendResponse.ResultBean> resultBeanList = response.getResult();
        Collections.sort(resultBeanList, new Comparator<SessionTrendResponse.ResultBean>() {
            @Override
            public int compare(SessionTrendResponse.ResultBean lhs, SessionTrendResponse.ResultBean rhs) {
                return lhs.getType().compareTo(rhs.getType());
            }
        });

        if (resultBeanList.isEmpty()){
            TextView textView = new TextView(getContext());
            textView.setGravity(Gravity.CENTER);
            textView.setText("无数据");
            messageTrendLayout.addView(textView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            return;
        }
        BarChart barChart = getBarChart(resultBeanList, messageDateInterval);
        if (barChart != null){
            messageTrendLayout.addView(barChart, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }else{
            TextView textView = new TextView(getContext());
            textView.setGravity(Gravity.CENTER);
            textView.setText("无数据");
            messageTrendLayout.addView(textView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        }




    }

    private void parseSessionTrend3(String value){
        HDLog.d(TAG, "parseSessionTrend:" + value);
        sessionTrendLayout.removeAllViews();
        Gson gson = new Gson();
        SessionTrendResponse response = gson.fromJson(value, SessionTrendResponse.class);
        if (response == null){
            return;
        }
        List<SessionTrendResponse.ResultBean> resultBeanList = response.getResult();
        Collections.sort(resultBeanList, new Comparator<SessionTrendResponse.ResultBean>() {
            @Override
            public int compare(SessionTrendResponse.ResultBean lhs, SessionTrendResponse.ResultBean rhs) {
                return lhs.getType().compareTo(rhs.getType());
            }
        });

        if (resultBeanList.isEmpty()){
            TextView textView = new TextView(getContext());
            textView.setGravity(Gravity.CENTER);
            textView.setText("无数据");
            sessionTrendLayout.addView(textView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            return;
        }
        BarChart mChart = getBarChart(resultBeanList, sessionDateInterval);
        if (mChart != null){
            sessionTrendLayout.addView(mChart, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }else{
            TextView textView = new TextView(getContext());
            textView.setGravity(Gravity.CENTER);
            textView.setText("无数据");
            sessionTrendLayout.addView(textView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        }


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK){
            if (requestCode == REQUEST_CODE_SESSION_FILTER){
                String dateInterval = data.getStringExtra("dateInterval");
                if (!TextUtils.isEmpty(dateInterval)){
                    sessionDateInterval = dateInterval;
                }
                TimeInfo timeInfo = (TimeInfo) data.getSerializableExtra("timeInfo");
                if (timeInfo != null){
                    currentSessionTimeInfo = timeInfo;
                }
                loadSessionTrendFromRemote();
            }else if (requestCode == REQUEST_CODE_MESSAGE_FILTER){
                String dateInterval = data.getStringExtra("dateInterval");
                if (!TextUtils.isEmpty(dateInterval)){
                    messageDateInterval = dateInterval;
                }
                TimeInfo timeInfo = (TimeInfo) data.getSerializableExtra("timeInfo");
                if (timeInfo != null){
                    currentMessageTimeInfo = timeInfo;
                }
                loadMessageTrendFromRemote();
            }
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null){
            unbinder.unbind();
        }
    }

}
