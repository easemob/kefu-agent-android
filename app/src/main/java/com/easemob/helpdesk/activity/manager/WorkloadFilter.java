package com.easemob.helpdesk.activity.manager;

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
import com.easemob.helpdesk.activity.BaseActivity;
import com.easemob.helpdesk.activity.SelectCategoryTreeActivity;
import com.easemob.helpdesk.utils.DateUtils;
import com.easemob.helpdesk.utils.PreferenceUtils;
import com.easemob.helpdesk.utils.TimeInfo;
import com.easemob.helpdesk.widget.pickerview.SimplePickerView;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.entity.HDCategorySummary;
import com.hyphenate.kefusdk.entity.TechChannel;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.entity.agent.AgentUser;
import com.hyphenate.kefusdk.manager.main.UserCustomInfoManager;
import com.hyphenate.kefusdk.utils.JsonUtils;
import com.zdxd.tagview.OnTagDeleteListener;
import com.zdxd.tagview.Tag;
import com.zdxd.tagview.TagView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by liyuzhao on 16/6/30.
 */
public class WorkloadFilter extends BaseActivity implements SimplePickerView.SimplePickSelectItemListener{

    private static final String TAG = WorkloadFilter.class.getSimpleName();


    private static final int REQUEST_CODE_ADD_CATEGORY = 0x01;

    private static final String[] dataStrings = {"今天", "昨天", "本周", "本月", "上月"};
    private static final String[] sessionTagStrings = {"全部会话","全部会话标签","指定会话标签","未指定会话标签"};

    private boolean isShowTag;
    private boolean isShowTechChannel;
    private List<TechChannel> techChannels;

    private ArrayList<String> techChannelOptions = new ArrayList<>();
    private ArrayList<Long> enttyIds = new ArrayList<>();
    private ArrayList<AgentUser> agentUsers = new ArrayList<>();
    private ArrayList<String> agentUserShows = new ArrayList<>();
    private TechChannel currentTechChannel;


    @BindView(R.id.rl_back)
    protected View btnBack;

    @BindView(R.id.right)
    protected TextView btnFilter;

    @BindView(R.id.rl_time)
    protected RelativeLayout rlTimeLayout;

    @BindView(R.id.rl_begintime)
    protected RelativeLayout rlBeginTimeLayout;
    @BindView(R.id.rl_endtime)
    protected RelativeLayout rlEndTimeLayout;

    @BindView(R.id.rl_sessiontag)
    protected RelativeLayout rlSessionTagLayout;

    @BindView(R.id.rl_addsessiontag)
    protected RelativeLayout rlAddSessionTag;

    @BindView(R.id.tagview_layout)
    protected LinearLayout tagViewLayout;

    @BindView(R.id.tagview)
    protected TagView tagGroup;

    @BindView(R.id.rl_techchannel)
    protected RelativeLayout rlTechChannel;

    @BindView(R.id.tv_techchannel_text)
    protected TextView tvTechChannelText;

    @BindView(R.id.rl_agent)
    protected RelativeLayout rlAgent;

    @BindView(R.id.tv_agent_text)
    protected TextView tvAgentText;


    @BindView(R.id.tv_time_text)
    protected TextView tvTimeText;
    @BindView(R.id.tv_begin_time)
    protected TextView tvBeginTime;
    @BindView(R.id.tv_end_time)
    protected TextView tvEndTime;

    @BindView(R.id.tv_sessiontag_text)
    protected TextView tvSessionTagText;

    private TimeInfo currentTimeInfo;
    private String currentSessionTagValue = "all";

    private SimplePickerView simplePickerView;
    private TimePickerView pvTime;
    private Context mContext;


    private PickCategory currentPickCategory = PickCategory.TIME;
    private PickTime currentPickTime = PickTime.BEGINTIME;

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
//    private ArrayWheelAdapter<HDUser> agentAdapter;
    private String agentUserId = null;
    private Unbinder unbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppConfig.setFitWindowMode(this);
        setContentView(R.layout.activity_workload_filter);
        unbinder = ButterKnife.bind(this);
        mContext = this;
        String techValue = PreferenceUtils.getInstance().getTechChannel();
        techChannels = JsonUtils.getTechChannels(techValue);
        if (!UserCustomInfoManager.getInstance().getCategoryIsUpdated()) {
            HDClient.getInstance().chatManager().asyncGetCategoryTree();
        }
        initView();
        initListener();
        initData();

    }

    private void initView(){
        pvTime = new TimePickerView(this, TimePickerView.Type.ALL);
        pvTime.setCyclic(false);
        pvTime.setCancelable(true);
        //时间选择后回调
        pvTime.setOnTimeSelectListener(new PVTimeSelectListener());
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
            timeMatch(currentTimeInfo);
        }
    }

    @OnClick(R.id.rl_back)
    public void onClickByBack(View view){
        finish();
    }

    @OnClick(R.id.right)
    public void onClickByFilter(View view){
        Intent sIntent = new Intent();
        sIntent.putExtra("timeinfo", currentTimeInfo);
        if(currentTechChannel != null){
            sIntent.putExtra("techChannel", currentTechChannel);
        }
        if (TextUtils.isEmpty(currentSessionTagValue)){
            if(enttyIds.size() > 0){
                sIntent.putExtra("ids", enttyIds.toString());
            }
        }else{
            sIntent.putExtra("ids", currentSessionTagValue);
        }

        if (!TextUtils.isEmpty(agentUserId)){
            sIntent.putExtra("agentUserId", agentUserId);
        }
        setResult(RESULT_OK, sIntent);
        finish();
    }

    @OnClick(R.id.rl_time)
    public void onClickByRlTime(View view){
        closePickerView();
        currentPickCategory = PickCategory.TIME;
        simplePickerView = new SimplePickerView(mContext, dataStrings);
        simplePickerView.setCancelable(true);
        simplePickerView.show();
    }

    @OnClick(R.id.rl_begintime)
    public void onClickByBeginTime(View view){
        closePVTime();
        currentPickTime = PickTime.BEGINTIME;
        if (currentTimeInfo != null){
            pvTime.setTime(new Date(currentTimeInfo.getStartTime()));
        }else{
            pvTime.setTime(new Date(System.currentTimeMillis()));
        }
        pvTime.show();
    }


    @OnClick(R.id.rl_techchannel)
    public void onClickByRlTechChannel(View view){
        closePickerView();
        currentPickCategory = PickCategory.TECHCHANNEL;
        simplePickerView = new SimplePickerView(mContext, techChannelOptions);
        simplePickerView.setCancelable(true);
        simplePickerView.show();
    }

    @OnClick(R.id.rl_sessiontag)
    public void onClickByRlSessionTag(View view){
        closePickerView();
        currentPickCategory = PickCategory.SESSIONTAG;
        simplePickerView = new SimplePickerView(mContext, sessionTagStrings);
        simplePickerView.setCancelable(true);
        simplePickerView.show();
    }

    @OnClick(R.id.rl_addsessiontag)
    public void onClickByRlAddSessionTag(View view) {
        startActivityForResult(new Intent(this, SelectCategoryTreeActivity.class).putExtra("ids", enttyIds.toString()), REQUEST_CODE_ADD_CATEGORY);
        overridePendingTransition(R.anim.activity_open, 0);
    }

    @OnClick(R.id.rl_agent)
    public void onClickByRlAgent(View view){
        closePickerView();
        currentPickCategory = PickCategory.AGENT;
        simplePickerView = new SimplePickerView(mContext, agentUserShows);
        simplePickerView.setCancelable(true);
        simplePickerView.show();
    }





    @OnClick(R.id.rl_endtime)
    public void onClickByEndTime(View view){
        closePVTime();
        currentPickTime = PickTime.ENDTIME;
        if (currentTimeInfo != null){
            pvTime.setTime(new Date(currentTimeInfo.getEndTime()));
        }else{
            pvTime.setTime(new Date(System.currentTimeMillis()));
        }
        pvTime.show();
    }

    private void initListener(){
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
        isShowTechChannel = intent.getBooleanExtra("showtechchannel", false);
        if (isShowTechChannel){
            rlTechChannel.setVisibility(View.VISIBLE);
        }else{
            rlTechChannel.setVisibility(View.GONE);
        }
        if (isShowTag){
            tagViewLayout.setVisibility(View.VISIBLE);
            rlSessionTagLayout.setVisibility(View.VISIBLE);
        }else{
            tagViewLayout.setVisibility(View.GONE);
            rlSessionTagLayout.setVisibility(View.GONE);
        }
        rlAddSessionTag.setVisibility(View.GONE);

        if(currentTimeInfo == null){
            currentTimeInfo = DateUtils.getYesterdayStartAndEndTime();
        }
        timeMatch(currentTimeInfo);
        tvBeginTime.setText(dateFormat.format(new Date(currentTimeInfo.getStartTime())));
        tvEndTime.setText(dateFormat.format(new Date(currentTimeInfo.getEndTime())));

        techChannelOptions.add("全部关联");
        for (TechChannel channel : techChannels) {
            techChannelOptions.add(channel.name);
        }

        agentUserShows.add("全部客服");
        asyncLoadAllAgents();
    }



    private boolean timeEqual(TimeInfo info1, TimeInfo info2){
        return info1.getStartTime() / 60000 == info2.getStartTime() / 60000 && info1.getEndTime() / 60000 == info2.getEndTime() / 60000;
    }


    private void timeMatch(TimeInfo info1) {
        if (timeEqual(info1, DateUtils.getTodayStartAndEndTime())){
            tvTimeText.setText("今天");
        }else if (timeEqual(info1, DateUtils.getYesterdayStartAndEndTime())) {
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

    public void simplePickerSelect(int position){
        if (currentPickCategory == PickCategory.TIME){
            if (position >= dataStrings.length){
                return;
            }
            tvTimeText.setText(dataStrings[position]);
            switch (position){//今天 ,昨天", "本周", "本月", "上月
                case 0:
                    currentTimeInfo = DateUtils.getTodayStartAndEndTime();
                    break;
                case 1:
                    currentTimeInfo = DateUtils.getYesterdayStartAndEndTime();
                    break;
                case 2:
                    currentTimeInfo = DateUtils.getTimeInfoByCurrentWeek();
                    break;
                case 3:
                    currentTimeInfo = DateUtils.getTimeInfoByCurrentMonth();
                    break;
                case 4:
                    currentTimeInfo = DateUtils.getTimeInfoByLastMonth();
                    break;
            }
            timeMatch(currentTimeInfo);
            tvBeginTime.setText(dateFormat.format(new Date(currentTimeInfo.getStartTime())));
            tvEndTime.setText(dateFormat.format(new Date(currentTimeInfo.getEndTime())));
        }else if (currentPickCategory == PickCategory.TECHCHANNEL){
            tvTechChannelText.setText(techChannelOptions.get(position));
            if (position > 0 && techChannels.size() >= position){
                currentTechChannel = techChannels.get(position - 1);
            }else{
                currentTechChannel = null;
            }
        }else if (currentPickCategory == PickCategory.SESSIONTAG){
            tvSessionTagText.setText(sessionTagStrings[position]);
            //{"全部会话","全部会话标签","指定会话标签","未指定会话标签"};
            switch (position){
                case 0:
                    rlAddSessionTag.setVisibility(View.GONE);
                    enttyIds.clear();
                    currentSessionTagValue = "all";
                    break;
                case 1:
                    rlAddSessionTag.setVisibility(View.GONE);
//                    List<HDCategorySummary> treeEntities = HDDBManager.getInstance().loadAllLeafNode();
                    enttyIds.clear();
                    tagGroup.removeAllViews();
//                    for (HDCategorySummary item :
//                            treeEntities) {
//                        enttyIds.add(item.id);
//                    }
                    currentSessionTagValue = "yes";
                    break;
                case 2:
                    rlAddSessionTag.setVisibility(View.VISIBLE);
                    tagGroup.removeAllViews();
                    enttyIds.clear();
                    currentSessionTagValue = null;
                    break;
                case 3:
                    rlAddSessionTag.setVisibility(View.GONE);
                    enttyIds.clear();
                    tagGroup.removeAllViews();
                    enttyIds.add(0L);
                    currentSessionTagValue = "no";
                    break;
            }
        }else if (currentPickCategory == PickCategory.AGENT){
            if (position > 0 && position <= agentUsers.size()){
                tvAgentText.setText(agentUserShows.get(position));
                agentUserId = agentUsers.get(position -1).user.getUserId();
            }else{
                tvAgentText.setText("全部客服");
                agentUserId = "";

            }

        }
    }

    private void asyncLoadAllAgents() {
        agentUsers.clear();
        HDClient.getInstance().agentManager().getAgentList(true, new HDDataCallBack<List<AgentUser>>() {
            @Override
            public void onSuccess(final List<AgentUser> value) {
                if (isFinishing()) {return;}
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            agentUsers.addAll(value);
                            for (AgentUser agentUser: agentUsers) {
                                agentUserShows.add(agentUser.user.getNicename());
                            }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            if (requestCode == REQUEST_CODE_ADD_CATEGORY){
                if (data == null){
                    return;
                }
                HDCategorySummary entty = (HDCategorySummary) data.getSerializableExtra("tree");
                enttyIds.add(entty.id);
                setTagView(entty);
            }

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
        String strColor ;
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
        TECHCHANNEL,
        SESSIONTAG,
        AGENT
    }

    enum PickTime{
        BEGINTIME,
        ENDTIME
    }


}
