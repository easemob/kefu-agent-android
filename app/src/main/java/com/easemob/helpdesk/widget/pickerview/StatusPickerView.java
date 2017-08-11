package com.easemob.helpdesk.widget.pickerview;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.bigkoo.pickerview.adapter.ArrayWheelAdapter;
import com.bigkoo.pickerview.lib.WheelView;
import com.bigkoo.pickerview.view.BasePickerView;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.mvp.MainActivity;
import com.easemob.helpdesk.utils.DialogUtils;
import com.easemob.helpdesk.utils.EMToast;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.entity.HDUser;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by liyuzhao on 16/3/2.
 */
public class StatusPickerView extends BasePickerView implements View.OnClickListener {

    private Context mContext;
    private View btnSave;
    private WheelView wv;
    private ArrayList<String> values = new ArrayList<>();

    private String[] statuStrings = {"空闲", "忙碌", "离开", "隐身"};

    private Dialog dialog;

    public StatusPickerView(Context context) {
        super(context);
        this.mContext = context;
        LayoutInflater.from(context).inflate(R.layout.popup_picker_online, contentContainer);
        //
        btnSave = findViewById(R.id.tv_save);
        btnSave.setOnClickListener(this);
        wv = (WheelView) findViewById(R.id.wheelview);
        Collections.addAll(values, statuStrings);
        wv.setCyclic(false);
        wv.setAdapter(new ArrayWheelAdapter(values));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_save:
                setUserStatus(wv.getCurrentItem());
                dismiss();
                break;
        }
    }

    private void closeDialog() {
        if (dialog == null) {
            return;
        }
        dialog.dismiss();
    }

    private void refreshUI(String status) {
        if (mContext instanceof MainActivity) {
            ((MainActivity) mContext).agentStatusUpdated(status);
        }
    }


    private void setStatusByServer(final int index, final String status) {
        final HDUser loginUser = HDClient.getInstance().getCurrentUser();
        if (loginUser == null){
            return;
        }
        HDClient.getInstance().agentManager().setStatusByServer(status, new HDDataCallBack<String>() {

            @Override
            public void onSuccess(String value) {
                if (((Activity) mContext).isFinishing()) {
                    return;
                }
                loginUser.setOnLineState(status);
                HDClient.getInstance().setLoginUser(loginUser);
                ((Activity) mContext).runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        closeDialog();
                        switch (index) {
                            case 0:
                                break;
                            case 1:
                            case 2:
                            case 3:
                                EMToast.makeStyleableToast(mContext, "系统将不再为您分配新会话").show();
                                break;
                        }

                        refreshUI(status);
                    }
                });

            }

            @Override
            public void onError(int error, String errorMsg) {
                if (((Activity) mContext).isFinishing()) {
                    return;
                }
                ((Activity) mContext).runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        closeDialog();
                        String textString = "设置" + statuStrings[index] + "失败!";
                        Toast.makeText(mContext, textString, Toast.LENGTH_SHORT).show();
                    }
                });

            }

        });


    }


    private void setUserStatus(int index) {
        dialog = DialogUtils.getLoadingDialog(mContext, "更新中...");
        dialog.show();
        String status = "Online";
        switch (index) {
            case 0:
                status = "Online";
                break;
            case 1:
                status = "Busy";
                break;
            case 2:
                status = "Leave";
                break;
            case 3:
                status = "Hidden";
                break;
        }
        setStatusByServer(index, status);
    }


}
