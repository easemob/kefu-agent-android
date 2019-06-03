package com.easemob.helpdesk.widget.pickerview;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bigkoo.pickerview.adapter.ArrayWheelAdapter;
import com.bigkoo.pickerview.lib.WheelView;
import com.bigkoo.pickerview.view.BasePickerView;
import com.easemob.helpdesk.HDApplication;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.mvp.MainActivity;
import com.easemob.helpdesk.utils.DialogUtils;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.entity.option.OptionEntity;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.entity.user.HDUser;

import java.util.ArrayList;

/**
 * Created by liyuzhao on 24/11/2016.
 */

public class MaxAccessPickerView extends BasePickerView implements View.OnClickListener {

    private Context mContext;
    private View btnSave;
    private TextView tvTitle;
    private WheelView wv;
    private ArrayList<String> values = new ArrayList<>();

    public MaxAccessPickerView(Context context) {
        super(context);
        this.mContext = context;
        LayoutInflater.from(context).inflate(R.layout.popup_picker_online, contentContainer);
        //
        btnSave = findViewById(R.id.tv_save);
        tvTitle = (TextView) findViewById(R.id.title);
        tvTitle.setText("最大接入数");
        btnSave.setOnClickListener(this);
        wv = (WheelView) findViewById(R.id.wheelview);
        for (int i = 0; i <= 100; i++) {
            values.add(String.valueOf(i));
        }
        wv.setCyclic(false);
        wv.setAdapter(new ArrayWheelAdapter(values));
    }

    public void checkModifiable() {
        try {
            boolean isCanModify = isModifiable();
            btnSave.setVisibility(isCanModify ? View.VISIBLE : View.INVISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isModifiable() {
        OptionEntity optionEntity = HDClient.getInstance().agentManager().getOptionEntity("allowAgentChangeMaxSessions");
        if (optionEntity != null) {
            String value = optionEntity.getOptionValue();
            if (value != null && value.equalsIgnoreCase("false")) {
                HDUser loginUser = HDClient.getInstance().getCurrentUser();
                return loginUser != null && loginUser.getRoles() != null && loginUser.getRoles().contains("admin");
            } else {
                return true;
            }
        }
        return false;
    }

    private Dialog dialog;

    @Override public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_save:
                setMaxAccessCountByServer();
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

    private void refreshUI() {
        if (mContext instanceof MainActivity) {
            ((MainActivity) mContext).refreshSessionCount(-1);
        }
    }

    private void setMaxAccessCountByServer() {
        dialog = DialogUtils.getLoadingDialog(mContext, "更新中...");
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        final int maxCount = wv.getCurrentItem();
        HDClient.getInstance().agentManager().setMaxAccessCountByServer(maxCount, new HDDataCallBack<String>() {

            @Override public void onSuccess(String value) {
                if (((Activity) mContext).isFinishing()) {
                    return;
                }
                ((Activity) mContext).runOnUiThread(new Runnable() {

                    @Override public void run() {
                        closeDialog();
                        refreshUI();
                    }
                });
            }

            @Override public void onError(int error, String errorMsg) {
                if (((Activity) mContext).isFinishing()) {
                    return;
                }
                ((Activity) mContext).runOnUiThread(new Runnable() {

                    @Override public void run() {
                        closeDialog();
                        Toast.makeText(mContext, "最大接入数修改失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override public void onAuthenticationException() {
                if (((Activity) mContext).isFinishing()) {
                    return;
                }
                ((Activity) mContext).runOnUiThread(new Runnable() {

                    @Override public void run() {
                        closeDialog();
                        HDApplication.getInstance().logout();
                    }
                });
            }
        });
    }
}
