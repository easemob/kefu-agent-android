package com.easemob.helpdesk.activity.visitor;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.easemob.helpdesk.AppConfig;
import com.easemob.helpdesk.HDApplication;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.BaseActivity;
import com.easemob.helpdesk.mvp.ChatActivity;
import com.easemob.helpdesk.utils.DialogUtils;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.entity.HDSession;
import com.hyphenate.kefusdk.utils.HDLog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.easemob.helpdesk.R.id.originType;


/**
 * Created by liyuzhao on 14/02/2017.
 */

public class CustomerDetailActivity extends BaseActivity {
    private static final String TAG = "CustomerDetailActivity";
    private String visitorId;
    private CustomerInfoFragment customerInfoFragment;
    public FragmentTransaction fragmentTransaction;

    @BindView(R.id.ll_contact)
    protected LinearLayout llContact;
    private Unbinder unbinder;

    private boolean showContactVistor;
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppConfig.setFitWindowMode(this);
        setContentView(R.layout.activity_visitor_detail);
        unbinder = ButterKnife.bind(this);
        Intent intent = getIntent();
        visitorId = intent.getStringExtra("userId");
        showContactVistor = intent.getBooleanExtra("showContact", false);
        if (showContactVistor) {
            llContact.setVisibility(View.VISIBLE);
        } else {
            llContact.setVisibility(View.GONE);
        }
        fragmentTransaction = getSupportFragmentManager().beginTransaction();

        customerInfoFragment = new CustomerInfoFragment();
        customerInfoFragment.setArguments(intent.getExtras());
        fragmentTransaction.add(R.id.layout_container,customerInfoFragment);
        fragmentTransaction.commit();

    }

    @OnClick(R.id.iv_back)
    public void back() {
        finish();
    }

    @OnClick(R.id.btn_contact)
    public void onClickByBtnContact() {
        dialog = DialogUtils.getLoadingDialog(this, R.string.info_loading);
        dialog.show();
        HDClient.getInstance().visitorManager().getCreateSessionService(visitorId, new HDDataCallBack<HDSession>() {
            @Override
            public void onSuccess(final HDSession sessionEntty) {
                if (isFinishing()) {
                    return;
                }
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        closeDialog();
//                        Toast.makeText(getApplicationContext(), "回呼成功！", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent();
                        intent.setClass(CustomerDetailActivity.this, ChatActivity.class);
                        intent.putExtra("visitorid", sessionEntty.getServiceSessionId());
                        intent.putExtra("techChannelName", sessionEntty.getTechChannelName());
                        intent.putExtra("originType", originType);
                        intent.putExtra("user", sessionEntty.getUser());
                        intent.putExtra("chatGroupId", sessionEntty.getChatGroupId());
                        startActivity(intent);
                        finish();
                    }
                });

            }

            @Override
            public void onError(final int error, String errorMsg) {
                if (isFinishing()) {
                    return;
                }
                HDLog.d(TAG, "callback-error:" + errorMsg);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeDialog();
                        if (error == 400) {
                            Toast.makeText(getApplicationContext(), "回呼失败，该访客有尚未结束的会话！", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "回呼失败！", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

        });


    }

    private void closeDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeDialog();
        if (unbinder != null) {
            unbinder.unbind();
        }
    }

}
