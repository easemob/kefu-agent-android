package com.easemob.helpdesk.recorder;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.easemob.helpdesk.R;

/**
 * Created by liyuzhao on 16/9/28.
 */
public class DialogManager {

    private Dialog mDialog;
    private ImageView mIcon;
    private ImageView mVoice;

    private TextView mLabel;

    private Context mContext;

    public DialogManager(Context context){
        mContext = context;
    }

    public void showRecordingDialog(){
        mDialog = new Dialog(mContext, R.style.Theme_Audio_Dialog);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.dialog_recorder, null);
        mDialog.setContentView(view);
        mIcon = (ImageView) mDialog.findViewById(R.id.dialog_recorder_icon);
        mVoice = (ImageView) mDialog.findViewById(R.id.dialog_recorder_voice);
        mLabel = (TextView) mDialog.findViewById(R.id.dialog_recorder_label);
        mDialog.show();
    }

    public void recording(){
        if (mDialog != null && mDialog.isShowing()){
            mIcon.setVisibility(View.VISIBLE);
            mVoice.setVisibility(View.VISIBLE);
            mLabel.setVisibility(View.VISIBLE);

            mIcon.setImageResource(R.drawable.recorder);
            mLabel.setText("手指上划,取消发送");
            mLabel.setBackgroundResource(R.drawable.label_style);
        }
    }

    public void wantToCancel(){
        if (mDialog != null && mDialog.isShowing()){
            mIcon.setVisibility(View.VISIBLE);
            mVoice.setVisibility(View.GONE);
            mLabel.setVisibility(View.VISIBLE);

            mIcon.setImageResource(R.drawable.cancel);
            mLabel.setText("松开手指,取消发送");
            mLabel.setBackgroundResource(R.drawable.label_style_cancel);

        }
    }


    public void tooShort(){
        if (mDialog != null && mDialog.isShowing()){
            mIcon.setVisibility(View.VISIBLE);
            mVoice.setVisibility(View.GONE);
            mLabel.setVisibility(View.VISIBLE);

            mIcon.setImageResource(R.drawable.voice_to_short);
            mLabel.setText("录音时间过短");
        }
    }

    public void dismissDialog(){
        if (mDialog != null && mDialog.isShowing()){
            mDialog.dismiss();
            mDialog = null;
        }
    }

    public void updateVoiceLevel(int level){
        if (mDialog != null && mDialog.isShowing()){
            int resId = mContext.getResources().getIdentifier("v" + level, "drawable", mContext.getPackageName());
//            Log.i("info","资源名："+mContext.getPackageName());
            mVoice.setImageResource(resId);
        }
    }






}
