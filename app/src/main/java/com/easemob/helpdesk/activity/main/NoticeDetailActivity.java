package com.easemob.helpdesk.activity.main;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.helpdesk.AppConfig;
import com.easemob.helpdesk.HDApplication;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.AlertDialog;
import com.easemob.helpdesk.activity.BaseActivity;
import com.easemob.helpdesk.activity.FileDownloadActivity;
import com.easemob.helpdesk.activity.chat.ShowNormalFileActivity;
import com.easemob.helpdesk.activity.visitor.CustomerDetailActivity;
import com.easemob.helpdesk.utils.CommonUtils;
import com.easemob.helpdesk.utils.DialogUtils;
import com.easemob.helpdesk.utils.FileUtils;
import com.easemob.helpdesk.utils.PreferenceUtils;
import com.easemob.helpdesk.widget.popupwindow.SelectChannelPopupWindow;
import com.easemob.tagview.TagTextView;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.entity.option.OptionEntity;
import com.hyphenate.kefusdk.entity.TechChannel;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.entity.user.HDUser;
import com.hyphenate.kefusdk.gsonmodel.main.NoticesResponse;
import com.hyphenate.kefusdk.utils.HDLog;
import com.hyphenate.kefusdk.utils.JsonUtils;
import com.hyphenate.kefusdk.utils.PathUtil;
import com.zdxd.tagview.TagView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.zip.Inflater;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * Created by liyuzhao on 20/03/2017.
 */

public class NoticeDetailActivity extends BaseActivity {

    private static final String TAG = "NoticeDetailActivity";

    @BindView(R.id.tv_date)
    protected TextView tvDate;

    @BindView(R.id.tv_nicename)
    protected TextView tvNicename;

    @BindView(R.id.btn_detail)
    protected Button btnDetail;

    @BindView(R.id.tv_visitor_id)
    protected TextView tvVisitorId;

    @BindView(R.id.tv_content)
    protected TextView tvContent;

    @BindView(R.id.listview)
    protected ListView fileListView;

    @BindView(R.id.rl_visitor)
    protected RelativeLayout rlVisitor;

    private NoticesResponse.EntitiesBean noticeEntity;

    private Dialog dialog;
    private HDUser loginUser;


    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppConfig.setFitWindowMode(this);
        setContentView(R.layout.activity_notice_detail);
        ButterKnife.bind(this);
        loginUser = HDClient.getInstance().getCurrentUser();
        noticeEntity = (NoticesResponse.EntitiesBean) getIntent().getSerializableExtra("notice");
        setData();
        HDLog.d(TAG, "status:" + noticeEntity.getStatus());

    }




    private void setData(){
        try{
            String name = noticeEntity.getActor().getName();
            tvNicename.setText(name);
            tvDate.setText(dateFormat.format(new Date(noticeEntity.getCreated_at())));
            String content = noticeEntity.getObject().getContent().getDetail();
            if (TextUtils.isEmpty(content)){
                content = noticeEntity.getObject().getContent().getSummary();
            }
            tvContent.setText(content);
        }catch (Exception e){
            e.printStackTrace();
        }

        OptionEntity centerVisible = HDClient.getInstance().agentManager().getOptionEntity("agentVisitorCenterVisible");
        Boolean customersVisible = false;

        if (centerVisible != null && centerVisible.getOptionValue() != null && centerVisible.getOptionValue().equals("true")) {
            customersVisible = true;
        }

        List<NoticesResponse.EntitiesBean.ObjectBean.RedirectInfoBean> infoBeans =  noticeEntity.getObject().getRedirectInfo();
        if (infoBeans != null && !infoBeans.isEmpty() && customersVisible){
            NoticesResponse.EntitiesBean.ObjectBean.RedirectInfoBean infoBean = infoBeans.get(0);
            rlVisitor.setVisibility(View.VISIBLE);
            final String nickname = infoBean.getVisitorNickname();
            final String imAccount = infoBean.getIm_id();
            final String visitorId = infoBean.getVisitorUserId();
            tvVisitorId.setText(nickname);
            btnDetail.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    if (TextUtils.isEmpty(visitorId)){
                        getTechChannel(imAccount, nickname);
                    }else{
                        postAgentLinks(visitorId);
                    }

                }
            });

        }else{
            rlVisitor.setVisibility(View.GONE);
        }
        setFileData();

    }

    private void setFileData(){
        List<NoticesResponse.EntitiesBean.ObjectBean.FileEntity> fileEntities = null;
        try {
            fileEntities = noticeEntity.getObject().getFiles();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (fileEntities == null || fileEntities.isEmpty()) {
            return;
        }

        fileListView.setAdapter(new FileAdapter(this, fileEntities));

        fileListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final NoticesResponse.EntitiesBean.ObjectBean.FileEntity fileEntity = (NoticesResponse.EntitiesBean.ObjectBean.FileEntity) parent.getItemAtPosition(position);
                final String fileUrl = fileEntity.getUrl();
                final File localFile = new File(PathUtil.getInstance().getFilePath(), CommonUtils.stringToMD5(fileUrl)  + "_"+ fileEntity.getName());
                if (localFile.exists()) {
                    FileUtils.openFile(localFile, NoticeDetailActivity.this);
                    return;
                }
                new android.support.v7.app.AlertDialog.Builder(NoticeDetailActivity.this).setMessage("确认下载此文件吗？")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        goDownload(fileUrl, localFile.getPath());
                    }
                }).setNegativeButton("取消", null).show();
            }
        });

    }

    private void goDownload(String remoteUrl, String localPath) {
        Intent intent = new Intent(NoticeDetailActivity.this, FileDownloadActivity.class);
        intent.putExtra("remoteUrl", remoteUrl);
        intent.putExtra("localName", localPath);
        startActivity(intent);
    }



    class FileAdapter extends ArrayAdapter<NoticesResponse.EntitiesBean.ObjectBean.FileEntity> {
        private LayoutInflater inflater;
        public FileAdapter(Context context, List<NoticesResponse.EntitiesBean.ObjectBean.FileEntity> fileEntities) {
            super(context, 1, fileEntities);
            inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.layout_file_item, null);
                viewHolder = new ViewHolder();
                viewHolder.fileName = convertView.findViewById(R.id.tv_file_name);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            NoticesResponse.EntitiesBean.ObjectBean.FileEntity fileEntity = getItem(position);
            String fileName = fileEntity.getName();
            viewHolder.fileName.setText(fileName);
            return convertView;
        }

        class ViewHolder {
            TextView fileName;
        }

    }



    public WindowManager.LayoutParams params;

    private void getTechChannel(final String imAccount, final String nickName) {
        String techValue = PreferenceUtils.getInstance().getTechChannel();
        final List<TechChannel> techChannels = JsonUtils.getTechChannels(techValue);
        if (techChannels != null && techChannels.size() == 1) {
            TechChannel techChannel = techChannels.get(0);
            createVisitor(imAccount, nickName, techChannel);
        } else {
            SelectChannelPopupWindow popupWindow = new SelectChannelPopupWindow(this, techChannels, new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    TechChannel techChannel = techChannels.get(position);
                    createVisitor(imAccount, nickName, techChannel);
                }
            });

            popupWindow.showAtLocation(findViewById(R.id.ly_root), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
            params = getWindow().getAttributes();
            //当弹出Popupwindow时，背景变半透明
            params.alpha = 0.7f;
            getWindow().setAttributes(params);
            //设置PopupWindow关闭监听，当PopupWindow关闭，背景恢复1f
            popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    params = getWindow().getAttributes();
                    params.alpha = 1f;
                    getWindow().setAttributes(params);
                }
            });
        }
    }

    private void createVisitor(String imAccount, String nickname, TechChannel techChannel){
        dialog = DialogUtils.getLoadingDialog(this, R.string.info_loading);
        dialog.setCancelable(false);
        dialog.show();

        HDClient.getInstance().visitorManager().createVisitor(imAccount, nickname, techChannel, new HDDataCallBack<String>() {
            @Override
            public void onSuccess(final String value) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeDialog();
                        toCustomerDetail(loginUser.getTenantId(), value);
                    }
                });

            }

            @Override
            public void onError(int error, String errorMsg) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeDialog();
                        Toast.makeText(NoticeDetailActivity.this, "联系失败！", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onAuthenticationException() {
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


    private void postAgentLinks(final String visitorId){
        if (dialog == null){
            dialog = DialogUtils.getLoadingDialog(this, R.string.info_loading);
            dialog.setCancelable(false);
        }
        if (!dialog.isShowing()){
            dialog.show();
        }
        HDClient.getInstance().agentManager().postAgentLinks(visitorId, new HDDataCallBack<String>() {
            @Override
            public void onSuccess(final String value) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeDialog();
                        toCustomerDetail(loginUser.getTenantId(), value);
                    }
                });
            }

            @Override
            public void onError(int error, String errorMsg) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeDialog();
                        Toast.makeText(NoticeDetailActivity.this, "联系失败！", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


    }


    private void toCustomerDetail(long tenantId, String visitorId){
        //CustomerDetailActivity
        Intent intent = new Intent();
        intent.setClass(this, CustomerDetailActivity.class);
        intent.putExtra("userId", visitorId);
        intent.putExtra("tenantId", tenantId);
        intent.putExtra("showContact", true);
        startActivity(intent);
        finish();
    }




    private void closeDialog(){
        if (dialog != null && dialog.isShowing()){
            dialog.dismiss();
            dialog = null;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeDialog();
    }

    @OnClick(R.id.rl_back)
    public void onClickByLeft(){
        finish();
    }

}
