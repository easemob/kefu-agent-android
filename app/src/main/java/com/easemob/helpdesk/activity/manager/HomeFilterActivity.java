package com.easemob.helpdesk.activity.manager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bigkoo.pickerview.TimePickerView;
import com.easemob.helpdesk.AppConfig;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.BaseActivity;
import com.easemob.helpdesk.utils.DateUtils;
import com.easemob.helpdesk.utils.TimeInfo;
import com.easemob.helpdesk.widget.pickerview.SimplePickerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by liyuzhao on 16/6/27.
 */
public class HomeFilterActivity extends BaseActivity implements SimplePickerView.SimplePickSelectItemListener{

    @BindView(R.id.rl_back)
    protected View tvBack;

    @BindView(R.id.right)
    protected TextView tvFilter;

//    private static final String[] dataStrings = {"小时", "天", "周", "月"};
    private static final String[] dataStrings = {"天", "周", "月"};

    private static final String[] channelString = {"全部渠道", "网页", "APP"};

    private TimeInfo currentTimeInfo;

    @BindView(R.id.rl_time)
    protected RelativeLayout rlTime;

    @BindView(R.id.rl_begintime)
    protected RelativeLayout rlBeginTime;

    @BindView(R.id.rl_endtime)
    protected RelativeLayout rlEndTime;

    @BindView(R.id.rl_channel)
    protected RelativeLayout rlChannel;


    @BindView(R.id.tv_time_text)
    protected TextView tvTimeText;

    @BindView(R.id.tv_begin_time)
    protected TextView tvBeginTime;

    @BindView(R.id.tv_end_time)
    protected TextView tvEndTime;

    @BindView(R.id.tv_channel_text)
    protected TextView tvChannelText;

    private Context mContext;


    private PickCategory currentPickCategory = PickCategory.TIME;
    private PickTime currentPickTime = PickTime.BEGINTIME;

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    private SimplePickerView simplePickerView;
    private TimePickerView pvTime;
    private String dateInterval = null;
    private Unbinder unbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppConfig.setFitWindowMode(this);
        setContentView(R.layout.activity_home_filter);
        unbinder = ButterKnife.bind(this);
        mContext = this;
        initView();
        initListener();
        initData();
    }

    private void initView(){
        pvTime = new TimePickerView(this, TimePickerView.Type.ALL);
        pvTime.setCyclic(false);
        pvTime.setCancelable(false);
        //时间选择后回调
        pvTime.setOnTimeSelectListener(new PVTimeSelectListener());
    }

    private void initListener(){

    }

    private void initData(){
        Intent intent = getIntent();
        currentTimeInfo = (TimeInfo) intent.getSerializableExtra("timeinfo");
        dateInterval = intent.getStringExtra("dateInterval");
        if (dateInterval != null){
//             if (dateInterval.equals("1h")){
//                    tvTimeText.setText(dataStrings[0]);
//             }else if (dateInterval.equals("1d")){
//                 tvTimeText.setText(dataStrings[1]);
//             }else if (dateInterval.equals("1w")){
//                 tvTimeText.setText(dataStrings[2]);
//             }else if (dateInterval.equals("1M")){
//                 tvTimeText.setText(dataStrings[3]);
//             }
            switch (dateInterval) {
                case "1d":
                    tvTimeText.setText(dataStrings[0]);
                    break;
                case "1w":
                    tvTimeText.setText(dataStrings[1]);
                    break;
                case "1M":
                    tvTimeText.setText(dataStrings[2]);
                    break;
            }

        }

        if (currentTimeInfo != null){
            tvBeginTime.setText(dateFormat.format(new Date(currentTimeInfo.getStartTime())));
            tvEndTime.setText(dateFormat.format(new Date(currentTimeInfo.getEndTime())));
        }else{
            tvBeginTime.setText("");
            tvEndTime.setText("");
        }

    }

    class PVTimeSelectListener implements TimePickerView.OnTimeSelectListener{

        @Override
        public void onTimeSelect(Date date) {
            if (currentTimeInfo == null){
                currentTimeInfo = DateUtils.getTimeInfoByCurrentWeek();
            }
            if (currentPickTime == PickTime.BEGINTIME){
                if (currentTimeInfo.getEndTime() < date.getTime()){
                    currentTimeInfo.setEndTime(date.getTime());
                    tvEndTime.setText(dateFormat.format(date));
                }
                currentTimeInfo.setStartTime(date.getTime());
                tvBeginTime.setText(dateFormat.format(date));

            }else if (currentPickTime == PickTime.ENDTIME){
                if (date.getTime() < currentTimeInfo.getStartTime()){
                    currentTimeInfo.setStartTime(date.getTime());
                    tvBeginTime.setText(dateFormat.format(date));
                }
                currentTimeInfo.setEndTime(date.getTime());
                tvEndTime.setText(dateFormat.format(date));
            }
        }
    }


    @OnClick(R.id.rl_back)
    protected void onClickByBack(View view){
        finish();
    }

    @OnClick(R.id.right)
    protected void onClickByFilter(View view){

        Intent sIntent = new Intent();
        sIntent.putExtra("timeInfo", currentTimeInfo);
        if (!TextUtils.isEmpty(dateInterval)){
            sIntent.putExtra("dateInterval", dateInterval);
        }
        setResult(RESULT_OK, sIntent);
        finish();
    }
    @OnClick(R.id.rl_time)
    protected void onClickByRlTime(View view){
        closePickerView();
        currentPickCategory = PickCategory.TIME;
        simplePickerView = new SimplePickerView(mContext, dataStrings);
        simplePickerView.setCancelable(true);
        simplePickerView.show();
    }

    @OnClick(R.id.rl_channel)
    protected void onClickByRlChannel(View view){
        closePickerView();
        currentPickCategory = PickCategory.CHANNEL;
        simplePickerView = new SimplePickerView(mContext, channelString);
        simplePickerView.setCancelable(true);
        simplePickerView.show();
    }

    @OnClick(R.id.rl_begintime)
    protected void onClickByRlBeginTime(View view){
        closePVTime();
        currentPickTime = PickTime.BEGINTIME;
        if (currentTimeInfo != null){
            pvTime.setTime(new Date(currentTimeInfo.getStartTime()));
        }else{
            pvTime.setTime(new Date(System.currentTimeMillis()));
        }
        pvTime.show();
    }

    @OnClick(R.id.rl_endtime)
    protected void onClickByRlEndTime(View view){
        closePVTime();
        currentPickTime = PickTime.ENDTIME;
        if (currentTimeInfo != null){
            pvTime.setTime(new Date(currentTimeInfo.getEndTime()));
        }else{
            pvTime.setTime(new Date(System.currentTimeMillis()));
        }
        pvTime.show();

    }

    public void simplePickerSelect(int position){
        if (currentPickCategory == PickCategory.TIME){
            if (position >= dataStrings.length){
                return;
            }
            tvTimeText.setText(dataStrings[position]);
            switch (position){
                case 0://天
                    dateInterval = "1d";
                    break;
                case 1://周
                    dateInterval = "1w";
                    break;
                case 2://月
                    dateInterval = "1M";
                    break;
            }
        }
//        else if (currentPickCategory == PickCategory.CHANNEL){
//
//        }
    }



    public void closePickerView(){
        if (simplePickerView != null && simplePickerView.isShowing()){
            simplePickerView.dismiss();
        }
    }

    public void closePVTime(){
        if (pvTime != null && pvTime.isShowing()){
            pvTime.dismiss();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (unbinder != null){
            unbinder.unbind();
        }
        closePickerView();
        closePVTime();
    }

    enum PickCategory{
        TIME,
        CHANNEL
    }

    enum PickTime{
        BEGINTIME,
        ENDTIME
    }


}
