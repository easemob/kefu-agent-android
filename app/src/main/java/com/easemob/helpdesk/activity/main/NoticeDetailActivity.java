package com.easemob.helpdesk.activity.main;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.helpdesk.AppConfig;
import com.easemob.helpdesk.HDApplication;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.BaseActivity;
import com.easemob.helpdesk.activity.visitor.CustomerDetailActivity;
import com.easemob.helpdesk.utils.DialogUtils;
import com.easemob.helpdesk.utils.PreferenceUtils;
import com.easemob.helpdesk.widget.popupwindow.SelectChannelPopupWindow;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.bean.OptionEntity;
import com.hyphenate.kefusdk.bean.TechChannel;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.entity.HDUser;
import com.hyphenate.kefusdk.gsonmodel.main.NoticesResponse;
import com.hyphenate.kefusdk.utils.HDLog;
import com.hyphenate.kefusdk.utils.JsonUtils;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
