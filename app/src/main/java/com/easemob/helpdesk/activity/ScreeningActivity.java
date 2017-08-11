package com.easemob.helpdesk.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bigkoo.pickerview.TimePickerView;
import com.easemob.helpdesk.AppConfig;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.utils.DateUtils;
import com.easemob.helpdesk.utils.PreferenceUtils;
import com.easemob.helpdesk.utils.TimeInfo;
import com.easemob.helpdesk.widget.pickerview.SimplePickerView;
import com.hyphenate.kefusdk.bean.HDCategorySummary;
import com.hyphenate.kefusdk.bean.TechChannel;
import com.hyphenate.kefusdk.utils.CategoryTreeUtils;
import com.hyphenate.kefusdk.utils.JsonUtils;
import com.zdxd.tagview.OnTagDeleteListener;
import com.zdxd.tagview.Tag;
import com.zdxd.tagview.TagView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by liyuzhao on 16/2/26.
 */
public class ScreeningActivity extends BaseActivity implements View.OnClickListener , SimplePickerView.SimplePickSelectItemListener {

    private View ibtnBack;
    private TextView ibtnFilter;
    private Context mContext;

    private static final int REQUEST_CODE_VISITORNAME = 0x01;
    private static final int REQUEST_CODE_ADD_CATEGORY = 0x02;

    private static final String[] dateStrings = {"今天", "昨天", "本周", "本月", "上月", "指定时间"};
    private static final String[] channelStrings = {"全部渠道", "网页", "APP", "微信", "微博"};
    private static final String[] sessionTagStrings = {"全部会话","全部会话标签","指定会话标签","未指定会话标签"};
    private boolean isShowTag;
    private List<TechChannel> techChannels;



    private ArrayList<String> techChannelOptions = new ArrayList<>();
    private ArrayList<Long> enttyIds = new ArrayList<Long>();

    private TimeInfo currentTimeInfo;
    private String currentOriginType;
    private TechChannel currentTechChannel;
    private String currentVisitorName;

    private RelativeLayout rlTimeLayout;
    private RelativeLayout rlChannelLayout;
    private RelativeLayout rlTechChannelLayout;
    private RelativeLayout rlSessionTagLayout;
    private RelativeLayout rlBeginTimeLayout;
    private RelativeLayout rlEndTimeLayout;
    private RelativeLayout rlVisitorNameLayout;
    private RelativeLayout rlAddSessionTag;
    private LinearLayout tagViewLayout;

    private TextView tvTimeText;
    private TextView tvChannelText;
    private TextView tvTechChannelText;
    private TextView tvSessionTagText;
    private TextView tvVisitorNameText;
    private TagView tagGroup;

    private TextView tvBeginTime;
    private TextView tvEndTime;

    private PickCategory currentPickCategory = PickCategory.TIME;
    private PickTime currentPickTime = PickTime.BEGINTIME;


    private SimplePickerView simplePickerView;
    private TimePickerView pvTime;
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppConfig.setFitWindowMode(this);
        setContentView(R.layout.activity_screening);
        mContext = this;
        String techValue = PreferenceUtils.getInstance().getTechChannel();
        techChannels = JsonUtils.getTechChannels(techValue);
        initView();
        initListener();
        initData();
    }

    private void initView(){
        ibtnBack = $(R.id.rl_back);
        ibtnFilter = $(R.id.right);
        rlTimeLayout = $(R.id.rl_time);
        tvTimeText = $(R.id.tv_time_text);
        rlChannelLayout = $(R.id.rl_channel);
        tvChannelText = $(R.id.tv_channel_text);
        rlTechChannelLayout = $(R.id.rl_techchannel);
        tvTechChannelText = $(R.id.tv_techchannel_text);
        rlSessionTagLayout = $(R.id.rl_sessiontag);
        tvSessionTagText = $(R.id.tv_sessiontag_text);

        rlBeginTimeLayout = $(R.id.rl_begintime);
        rlEndTimeLayout = $(R.id.rl_endtime);
        tvBeginTime = $(R.id.tv_begin_time);
        tvEndTime = $(R.id.tv_end_time);

        rlVisitorNameLayout = $(R.id.rl_visitorname);
        tvVisitorNameText = $(R.id.tv_visitorname_text);

        rlAddSessionTag = $(R.id.rl_addsessiontag);
        tagGroup = $(R.id.tagview);

        tagViewLayout = $(R.id.tagview_layout);

        pvTime = new TimePickerView(this, TimePickerView.Type.ALL);
        pvTime.setCyclic(false);
        pvTime.setCancelable(true);
        //时间选择后回调
        pvTime.setOnTimeSelectListener(new PVTimeSelectListener());
    }

    private void initListener(){
        ibtnBack.setOnClickListener(this);
        ibtnFilter.setOnClickListener(this);

        rlTimeLayout.setOnClickListener(new RlClickListener());
        rlChannelLayout.setOnClickListener(new RlClickListener());
        rlTechChannelLayout.setOnClickListener(new RlClickListener());
        rlSessionTagLayout.setOnClickListener(new RlClickListener());
        rlBeginTimeLayout.setOnClickListener(new PVTimeOnClickListener());
        rlEndTimeLayout.setOnClickListener(new PVTimeOnClickListener());

        rlVisitorNameLayout.setOnClickListener(this);
        rlAddSessionTag.setOnClickListener(this);

        tagGroup.setOnTagDeleteListener(new OnTagDeleteListener() {
            @Override
            public void onTagDeleted(TagView view, Tag tag, int position) {
                enttyIds.remove(position);
                view.remove(position);
            }
        });
    }

    private void initData(){
        Intent intent = getIntent();
        currentTimeInfo = (TimeInfo) intent.getSerializableExtra("timeinfo");
        isShowTag = intent.getBooleanExtra("showtag", false);
        if (isShowTag) {
            tagViewLayout.setVisibility(View.VISIBLE);
            rlSessionTagLayout.setVisibility(View.VISIBLE);
        } else {
            tagViewLayout.setVisibility(View.GONE);
            rlSessionTagLayout.setVisibility(View.GONE);
        }
        rlAddSessionTag.setVisibility(View.GONE);

        if (currentTimeInfo == null) {
            currentTimeInfo = DateUtils.getTimeInfoByCurrentWeek();
//            tvTimeText.setText("指定时间");

        }
//        else if (timeEqual(currentTimeInfo, DateUtils.getTimeInfoByCurrentWeek())) {
//            tvTimeText.setText("本周");
//        } else {
//            tvTimeText.setText("指定时间");
//        }
        timeMatch(currentTimeInfo);
        if (currentTimeInfo != null) {
            tvBeginTime.setText(dateFormat.format(new Date(currentTimeInfo.getStartTime())));
            tvEndTime.setText(dateFormat.format(new Date(currentTimeInfo.getEndTime())));
        } else {
            tvBeginTime.setText("");
            tvEndTime.setText("");
        }

        techChannelOptions.add("全部关联");
        for (TechChannel channel : techChannels) {
            techChannelOptions.add(channel.name);
        }
    }

    private boolean timeEqual(TimeInfo info1, TimeInfo info2){
        return info1.getStartTime() / 60000 == info2.getStartTime() / 60000 && info1.getEndTime() / 60000 == info2.getEndTime() / 60000;
    }

    private void timeMatch(TimeInfo info1) {
        if (timeEqual(info1, DateUtils.getTodayStartAndEndTime())) {
            tvTimeText.setText("今天");
        } else if (timeEqual(info1, DateUtils.getYesterdayStartAndEndTime())) {
            tvTimeText.setText("昨天");
        } else if (timeEqual(info1, DateUtils.getTimeInfoByCurrentWeek())) {
            tvTimeText.setText("本周");
        } else if (timeEqual(info1, DateUtils.getTimeInfoByCurrentMonth())) {
            tvTimeText.setText("本月");
        } else if (timeEqual(info1, DateUtils.getTimeInfoByLastMonth())) {
            tvTimeText.setText("上月");
        } else {
            tvTimeText.setText("指定时间");
        }
    }


    class RlClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            if(simplePickerView != null && simplePickerView.isShowing()){
                simplePickerView.dismiss();
            }
            switch (view.getId()){
                case R.id.rl_time:
                    currentPickCategory = PickCategory.TIME;
                    simplePickerView = new SimplePickerView(mContext, dateStrings);
                    simplePickerView.setCancelable(true);
                    simplePickerView.show();
                    break;

                case R.id.rl_channel:
                    if (channelStrings == null){
                        return;
                    }
                    currentPickCategory = PickCategory.CHANNEL;
                    simplePickerView = new SimplePickerView(mContext, channelStrings);
                    simplePickerView.setCancelable(true);
                    simplePickerView.show();
                    break;
                case R.id.rl_techchannel:
                    if (techChannelOptions == null){
                        return;
                    }
                    currentPickCategory = PickCategory.TECHCHANNEL;
                    simplePickerView = new SimplePickerView(mContext, techChannelOptions);
                    simplePickerView.setCancelable(true);
                    simplePickerView.show();
                    break;
                case R.id.rl_sessiontag:
                    currentPickCategory = PickCategory.SESSIONTAG;
                    simplePickerView = new SimplePickerView(mContext, sessionTagStrings);
                    simplePickerView.setCancelable(true);
                    simplePickerView.show();
                    break;
            }

        }
    }


    class PVTimeSelectListener implements TimePickerView.OnTimeSelectListener{

        @Override
        public void onTimeSelect(Date date) {
            if(currentTimeInfo == null){
                currentTimeInfo = DateUtils.getTodayStartAndEndTime();
            }
            tvTimeText.setText("指定时间");
            if (currentPickTime == PickTime.BEGINTIME) {
                if(currentTimeInfo.getEndTime()<date.getTime()){
                    currentTimeInfo.setEndTime(date.getTime());
                    tvEndTime.setText(dateFormat.format(date));
                }
                currentTimeInfo.setStartTime(date.getTime());
                tvBeginTime.setText(dateFormat.format(date));
            } else if (currentPickTime == PickTime.ENDTIME) {
                if(date.getTime() < currentTimeInfo.getStartTime()){
                    currentTimeInfo.setStartTime(date.getTime());
                    tvBeginTime.setText(dateFormat.format(date));
                }
                currentTimeInfo.setEndTime(date.getTime());
                tvEndTime.setText(dateFormat.format(date));
            }

        }
    }


    class PVTimeOnClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            if(pvTime != null && pvTime.isShowing()){
                pvTime.dismiss();
            }
            switch (view.getId()) {
                case R.id.rl_begintime:
                    currentPickTime = PickTime.BEGINTIME;
                    if (currentTimeInfo != null) {
                        pvTime.setTime(new Date(currentTimeInfo.getStartTime()));
                    } else {
                        pvTime.setTime(new Date(System.currentTimeMillis()));
                    }
                    pvTime.show();
                    break;
                case R.id.rl_endtime:
                    currentPickTime = PickTime.ENDTIME;
                    if (currentTimeInfo != null) {
                        pvTime.setTime(new Date(currentTimeInfo.getEndTime()));
                    } else {
                        pvTime.setTime(new Date(System.currentTimeMillis()));
                    }
                    pvTime.show();
                    break;
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_back:
                finish();
                break;
            case R.id.right:
                Intent sIntent = new Intent();
                sIntent.putExtra("timeinfo", currentTimeInfo);
                if(!TextUtils.isEmpty(currentOriginType)){
                    sIntent.putExtra("originType",currentOriginType);
                }
                if(currentTechChannel != null){
                    sIntent.putExtra("techChannel", currentTechChannel);
                }
                if(!TextUtils.isEmpty(currentVisitorName)){
                    sIntent.putExtra("visitorName", currentVisitorName);
                }
                if(enttyIds.size() > 0){
                    sIntent.putExtra("ids", enttyIds.toString());
                }
                setResult(RESULT_OK, sIntent);
                finish();
                break;
            case R.id.rl_visitorname:
                Intent modifyIntent = new Intent();
                modifyIntent.setClass(mContext, ModifyActivity.class);
                modifyIntent.putExtra("index", ModifyActivity.SCREENING_MODIFY_VISITORNAME);
                if(!TextUtils.isEmpty(currentVisitorName)){
                    modifyIntent.putExtra("content", currentVisitorName);
                }
                startActivityForResult(modifyIntent, REQUEST_CODE_VISITORNAME);
                break;
            case R.id.rl_addsessiontag:
                startActivityForResult(new Intent(this, SelectCategoryTreeActivity.class).putExtra("ids",enttyIds.toString()),REQUEST_CODE_ADD_CATEGORY);
                overridePendingTransition(R.anim.activity_open, 0);
                break;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_VISITORNAME) {
                currentVisitorName = data.getStringExtra("content");
                if (TextUtils.isEmpty(currentVisitorName)) {
                    tvVisitorNameText.setText("全部");
                } else {
                    tvVisitorNameText.setText(currentVisitorName);
                }
            } else if (requestCode == REQUEST_CODE_ADD_CATEGORY) {
                if(data == null){
                    return;
                }
                HDCategorySummary entty = (HDCategorySummary) data.getSerializableExtra("tree");
                enttyIds.add(entty.id);
//                oldEnttyIds.add(entty.id);
//                treeEntities.add(entty);
                setTagView(entty);
            }
        }


    }

    public void simplePickerSelect(int position){
        if(currentPickCategory == PickCategory.TIME){
            tvTimeText.setText(dateStrings[position]);
            //{"今天", "昨天", "本周", "本月", "上月", "指定时间"};
            switch (position) {
                case 0:
                    currentTimeInfo = DateUtils.getTodayStartAndEndTime();
//                    rlBeginTimeLayout.setEnabled(false);
//                    rlEndTimeLayout.setEnabled(false);
                    break;
                case 1:
                    currentTimeInfo = DateUtils.getYesterdayStartAndEndTime();
//                    rlBeginTimeLayout.setEnabled(false);
//                    rlEndTimeLayout.setEnabled(false);
                    break;
                case 2:
                    currentTimeInfo = DateUtils.getTimeInfoByCurrentWeek();
//                    rlBeginTimeLayout.setEnabled(false);
//                    rlEndTimeLayout.setEnabled(false);
                    break;
                case 3:
                    currentTimeInfo = DateUtils.getTimeInfoByCurrentMonth();
//                    rlBeginTimeLayout.setEnabled(false);
//                    rlEndTimeLayout.setEnabled(false);
                    break;
                case 4:
                    currentTimeInfo = DateUtils.getTimeInfoByLastMonth();
//                    rlBeginTimeLayout.setEnabled(false);
//                    rlEndTimeLayout.setEnabled(false);
                    break;
                case 5:
                    currentTimeInfo = null;
//                    rlBeginTimeLayout.setEnabled(true);
//                    rlEndTimeLayout.setEnabled(true);
                    break;
            }

            if(currentTimeInfo != null){
                tvBeginTime.setText(dateFormat.format(new Date(currentTimeInfo.getStartTime())));
                tvEndTime.setText(dateFormat.format(new Date(currentTimeInfo.getEndTime())));
            }else{
                tvBeginTime.setText("");
                tvEndTime.setText("");
            }
        }else if(currentPickCategory == PickCategory.CHANNEL){
            tvChannelText.setText(channelStrings[position]);
            //"全部渠道", "网页", "APP", "微信", "微博"
            switch (position){
                case 0:
                    currentOriginType = "";
                    break;
                case 1:
                    currentOriginType = "webim";
                    break;
                case 2:
                    currentOriginType = "app";
                    break;
                case 3:
                    currentOriginType = "weixin";
                    break;
                case 4:
                    currentOriginType = "weibo";
                    break;

            }
        }else if(currentPickCategory == PickCategory.TECHCHANNEL){
            tvTechChannelText.setText(techChannelOptions.get(position));
            if(position > 0 && techChannels.size() >= position){
                currentTechChannel = techChannels.get(position - 1);
            }else {
                currentTechChannel = null;
            }
        }else if (currentPickCategory == PickCategory.SESSIONTAG){
            tvSessionTagText.setText(sessionTagStrings[position]);
            //{"全部会话","全部会话标签","指定会话标签","未指定会话标签"};
            switch(position){
                case 0:
                    rlAddSessionTag.setVisibility(View.GONE);
                    enttyIds.clear();
                    break;
                case 1:
                    rlAddSessionTag.setVisibility(View.GONE);
                   List<HDCategorySummary> treeEntities =  CategoryTreeUtils.getInstance().loadAllLeafNode();
                    enttyIds.clear();
                    tagGroup.removeAllViews();
                    for (HDCategorySummary item:treeEntities) {
                        enttyIds.add(item.id);
                    }
                    break;
                case 2:
                    rlAddSessionTag.setVisibility(View.VISIBLE);
                    tagGroup.removeAllViews();
                    enttyIds.clear();
                    break;
                case 3:
                    rlAddSessionTag.setVisibility(View.GONE);
                    enttyIds.clear();
                    tagGroup.removeAllViews();
                    enttyIds.add(0L);
                    break;
                default:
                    break;

            }




        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(simplePickerView != null && simplePickerView.isShowing()){
            simplePickerView.dismiss();
        }
        if(pvTime != null && pvTime.isShowing()){
            pvTime.dismiss();
        }
    }



    private void setTagViews(List<HDCategorySummary> list){
        Tag tag;
        if(list == null || list.size() == 0){
            tagGroup.addTags(new java.util.ArrayList<Tag>());
            return;
        }
        ArrayList<Tag> tags = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            HDCategorySummary entty = list.get(i);
            String rootName = (TextUtils.isEmpty(entty.rootName)) ? "" : entty.rootName + ">";
            tag = new Tag(rootName + entty.name);
            tag.id = entty.id;
            tag.radius = 10f;
            int color = (int)entty.color;
            String strColor;
            if(color == 0){
                strColor = "#000000";
            }else if(color == 255){
                strColor = "#ffffff";
            }else{
                strColor = "#" + Integer.toHexString(color);
                strColor = strColor.substring(0, 7);
            }
            tag.layoutColor = Color.parseColor(strColor);
            tag.isDeletable = true;
            tags.add(tag);

        }
        tagGroup.addTags(tags);
    }

    private void setTagView(HDCategorySummary entty){
        if(entty == null){
            return;
        }
        String rootName = (TextUtils.isEmpty(entty.rootName)) ? "" : entty.rootName + ">";
        Tag tag = new Tag(rootName + entty.name);
        tag.id = entty.id;
        tag.radius = 10f;
        int color = (int)entty.color;
        String strColor;
        if(color == 0){
            strColor = "#000000";
        }else if(color == 255){
            strColor = "#ffffff";
        }else{
            strColor = "#" + Integer.toHexString(color);
            strColor = strColor.substring(0, 7);
        }
        tag.layoutColor = Color.parseColor(strColor);
        tag.isDeletable = true;
        tagGroup.addTag(tag);
    }


    public enum PickCategory{
        TIME,
        CHANNEL,
        TECHCHANNEL,
        SESSIONTAG
    }

    public enum PickTime{
        BEGINTIME,
        ENDTIME
    }


}
