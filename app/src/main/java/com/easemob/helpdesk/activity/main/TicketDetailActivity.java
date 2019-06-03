package com.easemob.helpdesk.activity.main;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.helpdesk.AppConfig;
import com.easemob.helpdesk.HDApplication;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.BaseActivity;
import com.easemob.helpdesk.activity.FileDownloadActivity;
import com.easemob.helpdesk.adapter.CommentFileAdapter;
import com.hyphenate.kefusdk.entity.FileEntity;
import com.hyphenate.kefusdk.gsonmodel.ticket.LeaveMessageResponse;
import com.hyphenate.kefusdk.gsonmodel.ticket.TicketCommentsResponse;
import com.hyphenate.kefusdk.gsonmodel.ticket.TicketStatusResponse;
import com.easemob.helpdesk.image.ImageHandleUtils;
import com.easemob.helpdesk.recorder.MediaManager;
import com.easemob.helpdesk.utils.CommonUtils;
import com.easemob.helpdesk.utils.FileUtils;
import com.easemob.helpdesk.widget.pickerview.SimplePickerView;
import com.easemob.helpdesk.widget.recyclerview.MyRecyclerView;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.entity.user.HDBaseUser;
import com.hyphenate.kefusdk.entity.user.HDUser;
import com.hyphenate.kefusdk.utils.HDLog;
import com.hyphenate.kefusdk.utils.ISO8601DateFormat;
import com.hyphenate.kefusdk.utils.PathUtil;
import com.hyphenate.util.DensityUtil;
import com.leon.lfilepickerlibrary.LFilePicker;
import com.leon.lfilepickerlibrary.utils.Constant;
import com.wefika.flowlayout.FlowLayout;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.iwf.photopicker.PhotoPickerActivity;

/**
 * Created by liyuzhao on 16/8/17.
 */
public class TicketDetailActivity extends BaseActivity implements SimplePickerView.SimplePickSelectItemListener {

    private static final String TAG = TicketDetailActivity.class.getSimpleName();

    private TextView tvSubjectId;
    private TextView tvTimestamp;
    private TextView tvTitle;
    private TextView tvCreator;
    private TextView tvContent;
    private TextView tvOther;
    private TextView tvDist;
    private TextView tvStatus;
    private View rlStatus;
    private View rlAssignee;

    protected static final int REQUEST_CODE_CHOOSE_PICTURE = 1;
    protected static final int REQUEST_CODE_SELECT_LOCAL_FILE = 2;
    protected static final int REQUEST_CODE_LOCAL = 3;
    private PickCategory currentPickCategory = PickCategory.STATUS;

    private Context mContext;

    private LeaveMessageResponse.EntitiesBean ticketEntity;
    ISO8601DateFormat iso8601DateFormat = new ISO8601DateFormat();
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    private ArrayList<String> assigneeList = new ArrayList<>();
    private List<HDBaseUser> agentUsers = Collections.synchronizedList(new ArrayList<HDBaseUser>());
    private ArrayList<String> statusList = new ArrayList<>();
    private ArrayList<TicketStatusResponse.EntitiesBean> tempStatusList = new ArrayList<>();

    private SimplePickerView simplePickerView;
    private HDUser loginUser;
    private ProgressDialog pd;

    private List<TicketCommentsResponse.EntitiesBean> commentList = Collections.synchronizedList(new ArrayList<TicketCommentsResponse.EntitiesBean>());

    @BindView(R.id.listView)
    public ListView mListView;

    @BindView(R.id.input_text)
    public EditText etInput;

    @BindView(R.id.tv_send)
    public TextView tvSend;

    @BindView(R.id.file_container)
    public LinearLayout llFileContainer;

    @BindView(R.id.ll_file_count)
    public LinearLayout llFileCount;

    @BindView(R.id.tv_file_count)
    public TextView tvFileCount;

    @BindView(R.id.recyclerview)
    public MyRecyclerView myRecyclerview;

    private CommentAdapter commentAdapter;

    private List<FileEntity> fileList = Collections.synchronizedList(new ArrayList<FileEntity>());

    private CommentFileAdapter commentFileAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppConfig.setFitWindowMode(this);
        setContentView(R.layout.activity_ticket_detail);
        mContext = this;
        ButterKnife.bind(this);
        Intent intent = getIntent();
        ticketEntity = (LeaveMessageResponse.EntitiesBean) intent.getSerializableExtra("ticket");
        for (TicketStatusResponse.EntitiesBean entitiesBean : HDClient.getInstance().leaveMessageManager().getTicketstatusResponseEntityBean()) {
            if (entitiesBean.isIs_default()){
                statusList.add(0, entitiesBean.getName());
                tempStatusList.add(0, entitiesBean);
            }else{
                statusList.add(entitiesBean.getName());
                tempStatusList.add(entitiesBean);
            }
        }
        loginUser = HDClient.getInstance().getCurrentUser();

        assigneeList.add("未分配");

        initView();
        setData();
        mListView.setAdapter(commentAdapter = new CommentAdapter(commentList));
        getOtherAgents();
        getTicketComments();
    }

    private void getOtherAgents(){
        HDClient.getInstance().leaveMessageManager().getOtherAgents(new HDDataCallBack<List<HDBaseUser>>() {
            @Override
            public void onSuccess(List<HDBaseUser> value) {
                for(HDBaseUser bUser: value) {
                    agentUsers.add(bUser);
                    assigneeList.add(bUser.getNicename());
                }
            }

            @Override
            public void onError(int error, String errorMsg) {

            }

            @Override
            public void onAuthenticationException() {

            }
        });


    }
    private void initView(){
        @SuppressLint("InflateParams") View headerView = LayoutInflater.from(this).inflate(R.layout.activity_ticket_detail_header, null);
        tvSubjectId = (TextView) headerView.findViewById(R.id.subjectId);
        tvTimestamp = (TextView) headerView.findViewById(R.id.timestamp);
        tvTitle = (TextView) headerView.findViewById(R.id.title);
        tvCreator = (TextView) headerView.findViewById(R.id.creator);
        tvContent = (TextView) headerView.findViewById(R.id.tv_content);
        tvOther = (TextView) headerView.findViewById(R.id.tv_other);
        tvDist = (TextView) headerView.findViewById(R.id.tv_dist);
        tvStatus = (TextView) headerView.findViewById(R.id.tv_status);
        rlAssignee = headerView.findViewById(R.id.rl_assignee);
        rlStatus = headerView.findViewById(R.id.rl_status);
        rlAssignee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closePickerView();
                currentPickCategory = PickCategory.ASSIGNEE;
                simplePickerView = new SimplePickerView(mContext, assigneeList);
                simplePickerView.setCancelable(true);
                simplePickerView.show();
            }
        });
        rlStatus.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                closePickerView();
                currentPickCategory = PickCategory.STATUS;
                simplePickerView = new SimplePickerView(mContext, statusList);
                simplePickerView.setCancelable(true);
                simplePickerView.show();
            }
        });
        mListView.addHeaderView(headerView);
        myRecyclerview.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        myRecyclerview.setLayoutManager(layoutManager);
//        fileList
        myRecyclerview.setAdapter(commentFileAdapter = new CommentFileAdapter(this, fileList));
        mListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideFileContainer();
                hideKeyboard();
                return false;
            }
        });
        etInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideFileContainer();
                return false;
            }
        });
    }


    private void setData() {
        if(ticketEntity == null){
            return;
        }
        tvSubjectId.setText("No." + ticketEntity.getId());
        try {
            tvTimestamp.setText(simpleDateFormat.format(iso8601DateFormat.parse(ticketEntity.getCreated_at())));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        tvTitle.setText("主题:" + ticketEntity.getSubject());
        tvContent.setText("内容:" + ticketEntity.getContent());
        LeaveMessageResponse.EntitiesBean.CreatorBean creator = ticketEntity.getCreator();
        if(creator != null){
            if (creator.getName() != null){
                tvCreator.setText("发起人:" + creator.getName());
            }

            String strPhone = creator.getPhone();
            String strQQ = creator.getQq();
            String strEmail = creator.getEmail();
            String strCompany = creator.getCompany();
            StringBuilder stringBuilder = new StringBuilder();
            if(!TextUtils.isEmpty(strPhone)){
                stringBuilder.append("手机:").append(strPhone).append("\r\n");
            }
            if (!TextUtils.isEmpty(strQQ)){
                stringBuilder.append("QQ:").append(strQQ).append("\r\n");
            }
            if (!TextUtils.isEmpty(strEmail)){
                stringBuilder.append("邮箱:").append(strEmail).append("\r\n");
            }
            if (!TextUtils.isEmpty(strCompany)){
                stringBuilder.append("公司:").append(strCompany).append("\r\n");
            }
            tvOther.setText(stringBuilder.toString());
        }

        LeaveMessageResponse.EntitiesBean.AssigneeBean assignee = ticketEntity.getAssignee();
        if (assignee == null){
            tvDist.setText("未分配");
        }else{
            tvDist.setText(assignee.getName());
        }
        LeaveMessageResponse.EntitiesBean.StatusBean ticketEntityStatus = ticketEntity.getStatus();
        if (ticketEntityStatus != null){
            tvStatus.setText(ticketEntityStatus.getName());
        }

    }

    @OnClick(R.id.rl_back)
    protected void finishActivity(){
        finish();
    }

    public void simplePickerSelect(int position){
        if (currentPickCategory == PickCategory.STATUS){
            if (position >=0 && position < tempStatusList.size()){
//                tvStatus.setText(tempStatusList.get(position).getName());
                putTicketStatus(position);
            }
        }else if (currentPickCategory == PickCategory.ASSIGNEE){
            if(position >=0 && position < assigneeList.size()){
//                tvDist.setText(assigneeList.get(position));
                if (position == 0){
                    deleteTicketAssignee();
                }else if (position > 0){
                    if (position > agentUsers.size()){
                        return;
                    }
                    HDBaseUser bUser = agentUsers.get(position - 1);


                    putTicketTask(bUser);
                }
            }
        }
    }


    public void closePickerView(){
        if(simplePickerView != null && simplePickerView.isShowing()){
            simplePickerView.dismiss();
        }
    }

    private void putTicketTask(HDBaseUser baseUser){
        // http://kefu.easemob.com/tenants/35/projects/48022/tickets/204725/take?userId=dff496b2-9a7a-471e-b381-a58a140ebe29&tenantId=35&userRoles=admin,agent
        if (pd == null){
            pd = new ProgressDialog(mContext);
            pd.setCanceledOnTouchOutside(false);
        }
        pd.setMessage("请求中...");
        pd.show();

        HDClient.getInstance().leaveMessageManager().putTicketTask(baseUser, ticketEntity, new HDDataCallBack<LeaveMessageResponse.EntitiesBean>() {
            @Override
            public void onSuccess(LeaveMessageResponse.EntitiesBean value) {
                if (isFinishing()) {
                    return;
                }
                ticketEntity = value;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeDialog();
                        setData();
                        if(LeaveMessageFragment.callback != null){
                            LeaveMessageFragment.callback.onFresh(null);
                        }
                    }
                });
            }

            @Override
            public void onError(int error, String errorMsg) {
                if (isFinishing()) {
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeDialog();
                        Toast.makeText(mContext, "请求失败!", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onAuthenticationException() {
                if (isFinishing()) {
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeDialog();
                        Toast.makeText(mContext, "请求失败!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });




    }

    private void putTicketStatus(int position){
        if (loginUser == null){
            return;
        }
        if (pd == null){
            pd = new ProgressDialog(mContext);
            pd.setCanceledOnTouchOutside(false);
        }
        pd.setMessage("请求中...");
        pd.show();

        HDClient.getInstance().leaveMessageManager().putTicketStatus(ticketEntity, tempStatusList.get(position), new HDDataCallBack<LeaveMessageResponse.EntitiesBean>() {
            @Override
            public void onSuccess(LeaveMessageResponse.EntitiesBean value) {
                if (isFinishing()) {
                    return;
                }
                ticketEntity = value;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeDialog();
                        setData();
                        if(LeaveMessageFragment.callback != null){
                            LeaveMessageFragment.callback.onFresh(null);
                        }
                    }
                });
            }

            @Override
            public void onError(int error, String errorMsg) {
                if (isFinishing()) {
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeDialog();
                        Toast.makeText(mContext, "请求失败!", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onAuthenticationException() {
                if (isFinishing()) {
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeDialog();
                        Toast.makeText(mContext, "请求失败!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }

    private void deleteTicketAssignee(){

        if (loginUser == null){
            return;
        }
        if (ticketEntity.getAssignee() == null){
            return;
        }

        if (pd == null){
            pd = new ProgressDialog(mContext);
            pd.setCanceledOnTouchOutside(false);
        }
        pd.setMessage("请求中...");
        pd.show();

        HDClient.getInstance().leaveMessageManager().deleteTicketAssignee(ticketEntity, new HDDataCallBack<LeaveMessageResponse.EntitiesBean>() {
            @Override
            public void onSuccess(LeaveMessageResponse.EntitiesBean value) {
                if (isFinishing()) {
                    return;
                }
                ticketEntity = value;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeDialog();
                        setData();
                        if(LeaveMessageFragment.callback != null){
                            LeaveMessageFragment.callback.onFresh(null);
                        }
                    }
                });
            }

            @Override
            public void onError(int error, String errorMsg) {
                if (isFinishing()) {
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeDialog();
                        Toast.makeText(mContext, "请求失败!", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onAuthenticationException() {
                if (isFinishing()) {
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeDialog();
                        Toast.makeText(mContext, "请求失败!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


    }
    private View animView;

    private void playVoiceItem(View v, String voiceLocalPath){
        //播放动画
        if (animView != null){
            animView.setBackgroundResource(R.drawable.icon_audio_white_3);
            animView = null;
        }

        animView = v.findViewById(R.id.id_recorder_anim);
        animView.setBackgroundResource(R.drawable.voice_from_icon);
        AnimationDrawable anim = (AnimationDrawable) animView.getBackground();
        anim.start();

        //播放音频
        MediaManager.playSound(mContext, voiceLocalPath, new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                animView.setBackgroundResource(R.drawable.icon_audio_white_3);
            }
        });

    }


    private void getTicketComments(){
        if (loginUser == null){
            return;
        }
        HDClient.getInstance().leaveMessageManager().getTicketComments(ticketEntity, new HDDataCallBack<List<TicketCommentsResponse.EntitiesBean>>() {
            @Override
            public void onSuccess(List<TicketCommentsResponse.EntitiesBean> value) {
                if (isFinishing()) {
                    return;
                }
                commentList.clear();
                commentList.addAll(value);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        commentAdapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onError(int error, String errorMsg) {
                HDLog.e(TAG, "errorMsg:" + errorMsg);
            }

            @Override
            public void onAuthenticationException() {

            }
        });

    }


    class CommentAdapter extends BaseAdapter{
        private List<TicketCommentsResponse.EntitiesBean> mList;
        private LayoutInflater inflater;

        public CommentAdapter(List<TicketCommentsResponse.EntitiesBean> list){
            this.mList = list;
            inflater = LayoutInflater.from(mContext);
        }

        @Override
        public int getCount() {
            return mList == null ? 0 :mList.size();
        }

        @Override
        public TicketCommentsResponse.EntitiesBean getItem(int position) {
            if (position >= mList.size()){
                return null;
            }
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(TicketDetailActivity.this).inflate(R.layout.item_ticket_comment, parent, false);
                holder.tvNickname = (TextView) convertView.findViewById(R.id.tv_nickname);
                holder.tvTimestamp = (TextView) convertView.findViewById(R.id.timestamp);
                holder.tvComment = (TextView) convertView.findViewById(R.id.tv_comment);
                holder.flowLayout = (FlowLayout) convertView.findViewById(R.id.flowLayout);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            TicketCommentsResponse.EntitiesBean entitiesBean = getItem(position);
            if (entitiesBean == null) {
                return convertView;
            }

            TicketCommentsResponse.EntitiesBean.CreatorBean creatorBean = entitiesBean.getCreator();
            if (creatorBean != null) {
                holder.tvNickname.setText(creatorBean.getName());
            }

            holder.tvComment.setText(entitiesBean.getContent());

            try {
                holder.tvTimestamp.setText(simpleDateFormat.format(iso8601DateFormat.parse(entitiesBean.getCreated_at())));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            final List<TicketCommentsResponse.EntitiesBean.AttachmentsBean> attachmentsBeans = entitiesBean.getAttachments();
            if (attachmentsBeans != null && attachmentsBeans.size() > 0){
                holder.flowLayout.setVisibility(View.VISIBLE);
                holder.flowLayout.removeAllViews();
                for (final TicketCommentsResponse.EntitiesBean.AttachmentsBean bean : attachmentsBeans) {
                    final String remoteUrl = bean.getUrl();
                    final String localName = CommonUtils.stringToMD5(bean.getUrl()) + "-" + bean.getName();
                    final String localPath = PathUtil.getInstance().getFilePath() + File.separator + localName;
                    final String fileType = bean.getType();
                    if (fileType != null && fileType.equals("audio")){
                        View lengthView = inflater.inflate(R.layout.comment_audio_textview, null);
                        lengthView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                File file = new File(localPath);
                                if (file.exists()){
                                    playVoiceItem(v, localPath);
                                }else{
                                    Intent intent = new Intent();
                                    intent.setClass(getBaseContext(), FileDownloadActivity.class);
                                    intent.putExtra("remoteUrl", remoteUrl);
                                    intent.putExtra("localName", localPath);
                                    intent.putExtra("type", "audio");
                                    startActivity(intent);
                                }

                            }
                        });
                        FlowLayout.LayoutParams lp = new FlowLayout.LayoutParams(DensityUtil.dip2px(mContext, 50), DensityUtil.dip2px(mContext, 30));
                        lp.topMargin = DensityUtil.dip2px(mContext, 5);
                        lp.bottomMargin = DensityUtil.dip2px(mContext, 5);
                        lp.leftMargin = DensityUtil.dip2px(mContext, 5);
                        lp.rightMargin = DensityUtil.dip2px(mContext, 5);
                        holder.flowLayout.addView(lengthView, lp);

                    }else{
                        TextView textView = (TextView) inflater.inflate(R.layout.comment_file_textview, null);
                        textView.setText(bean.getName());
                        textView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                File file = new File(localPath);
                                if (file.exists()) {
                                    if (fileType != null && fileType.equals("audio")) {

                                        MediaManager.playSound(mContext, localPath, new MediaPlayer.OnCompletionListener() {
                                            @Override
                                            public void onCompletion(MediaPlayer mp) {

                                                Log.e(TAG, "onCompletion");
                                            }
                                        });


                                    } else {
                                        FileUtils.openFile(file, TicketDetailActivity.this);
                                    }
                                } else {
                                    Intent intent = new Intent();
                                    intent.setClass(getBaseContext(), FileDownloadActivity.class);
                                    intent.putExtra("remoteUrl", remoteUrl);
                                    intent.putExtra("localName", localPath);
                                    startActivity(intent);
                                }
                            }
                        });
                        FlowLayout.LayoutParams lp = new FlowLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, DensityUtil.dip2px(mContext, 30));
                        lp.topMargin = DensityUtil.dip2px(mContext, 5);
                        lp.bottomMargin = DensityUtil.dip2px(mContext, 5);
                        holder.flowLayout.addView(textView, lp);
                    }



                }
            }else{
                holder.flowLayout.setVisibility(View.GONE);
            }

            return convertView;
        }

        class ViewHolder{
            TextView tvNickname;
            TextView tvTimestamp;
            TextView tvComment;
            com.wefika.flowlayout.FlowLayout flowLayout;
        }

    }


    @OnClick(R.id.tv_send)
    public void onClickBySend(View view){
        String strInput = etInput.getText().toString().trim();
        if (TextUtils.isEmpty(strInput) && (fileList.size() == 0)){
            Toast.makeText(mContext, "评论内容不能为空!", Toast.LENGTH_SHORT).show();
            return;
        }
        sendComment(strInput);
        etInput.getText().clear();
        hideKeyboard();
        hideFileContainer();
    }

    private void sendComment(String content){
        if (loginUser == null){
            return;
        }
        HDClient.getInstance().leaveMessageManager().sendComment(content, fileList, ticketEntity, new HDDataCallBack<String>() {
            @Override
            public void onSuccess(String value) {
                if (isFinishing()) {
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        fileList.clear();
                        tvFileCount.setText("0");
                        commentFileAdapter.notifyDataSetChanged();
                        Toast.makeText(mContext, "评论成功!", Toast.LENGTH_SHORT).show();
                        getTicketComments();
                    }
                });
            }

            @Override
            public void onError(int error, String errorMsg) {
                if (isFinishing()) {
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, "评论失败!", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onAuthenticationException() {
                if (isFinishing()) {
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        HDApplication.getInstance().logout();
                    }
                });

            }
        });

    }


    @OnClick(R.id.ll_file_count)
    public void OnClickByllFileCount(View view){
        togglellFileContainer();
    }

    @OnClick(R.id.ll_gallary)
    public void OnClickByllGallary(View view){
        //打开相册新方法
        Intent intent = ImageHandleUtils.pickSingleImage(this, true);
        this.startActivityForResult(intent, REQUEST_CODE_CHOOSE_PICTURE);
    }


    @OnClick(R.id.ll_file)
    public void onClickByllFile(View view){
        // 打开文件列表
        new LFilePicker().withActivity(this)
                .withRequestCode(REQUEST_CODE_SELECT_LOCAL_FILE)
                .withStartPath(Environment.getExternalStorageDirectory().getPath())
                .withTitle("选择要发送的文件")
                .withIsGreater(false).withMaxNum(1)
                .withFileSize(500 * 1024)
                .start();

    }


    private void hideFileContainer(){
        llFileContainer.setVisibility(View.GONE);
    }

    private void togglellFileContainer(){
        if (llFileContainer.getVisibility() == View.VISIBLE){
            llFileContainer.setVisibility(View.GONE);
        }else{
            llFileContainer.setVisibility(View.VISIBLE);
            hideKeyboard();
        }
    }

    private void closeDialog(){
        if (pd != null && pd.isShowing()){
            pd.dismiss();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            if (requestCode == REQUEST_CODE_CHOOSE_PICTURE){
                if (data != null) {
                    ArrayList<String> picPathList = data.getStringArrayListExtra(PhotoPickerActivity.KEY_SELECTED_PHOTOS);
                    if (picPathList == null || picPathList.size() == 0) {
                        return;
                    }
                    String picPath = picPathList.get(0);
                    uploadFile(picPath);
                }
            }else if (requestCode == REQUEST_CODE_SELECT_LOCAL_FILE){
                // Get the Uri of the selected file
                List<String> files = data.getStringArrayListExtra(Constant.RESULT_INFO);
                // files is the array of the paths of files selected by the Application User.
                if (files != null && files.size() > 0){
                    for (String filePath : files){
                        uploadFile(filePath);
                    }
                }

            }
        }

    }

    private void uploadFile(final String filePath){
        if (TextUtils.isEmpty(filePath)) {
            return;
        }

        if (pd == null){
            pd = new ProgressDialog(mContext);
            pd.setCanceledOnTouchOutside(false);
        }
        pd.setMessage("请求中...");
        pd.show();

        HDClient.getInstance().leaveMessageManager().sendTicketCommentFile(filePath, new HDDataCallBack<FileEntity>() {
            @Override
            public void onSuccess(final FileEntity value) {
                if (isFinishing()) {
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeDialog();
                        setFileView(value);
                    }
                });
            }

            @Override
            public void onError(int error, String errorMsg) {
                if (isFinishing()) {
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeDialog();
                        Toast.makeText(getApplicationContext(), "文件上传失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onAuthenticationException() {
                if (isFinishing()) {
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeDialog();
                        HDApplication.getInstance().logout();
                    }
                });
            }
        });
    }

    private void setFileView(FileEntity entity){
        if (entity == null){
            return;
        }
        fileList.add(entity);
        tvFileCount.setText("" + fileList.size());
        commentFileAdapter.notifyDataSetChanged();
    }

    public void delFileClick(View view, int position){
        if (position >= fileList.size()){
            return;
        }
        fileList.remove(position);
        tvFileCount.setText("" + fileList.size());
        commentFileAdapter.notifyDataSetChanged();
    }



    public void refreshFileCount(){
        tvFileCount.setText("" + fileList.size());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closePickerView();
        closeDialog();
    }
    enum PickCategory{
        STATUS,
        ASSIGNEE
    }

}
