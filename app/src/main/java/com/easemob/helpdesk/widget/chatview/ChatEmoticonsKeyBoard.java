package com.easemob.helpdesk.widget.chatview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.easemob.helpdesk.R;

import sj.keyboard.XhsEmoticonsKeyBoard;
import sj.keyboard.utils.EmoticonsKeyboardUtils;

public class ChatEmoticonsKeyBoard extends XhsEmoticonsKeyBoard {

    public final int APPS_HEIGHT_PER_LINE = 120;

    private final int APPS_PER_LINE = 4;

    public ChatEmoticonsKeyBoard(Context context, AttributeSet attrs){
        super(context, attrs);
        if (mBtnVoice instanceof RecorderButton){
            ((RecorderButton)mBtnVoice).setAudioFinishRecorderListener(new RecorderButton.AudioFinishRecorderListener() {
                @Override
                public void onFinish(float seconds, String filePath) {
                    if (mAudioFinishRecorderListener != null){
                        mAudioFinishRecorderListener.onFinish(seconds, filePath);
                    }
                }
            });
        }
    }

    private AudioFinishRecorderListener mAudioFinishRecorderListener;

    public void setAudioFinishRecorderListener(AudioFinishRecorderListener listener){
        this.mAudioFinishRecorderListener = listener;
    }


    public interface AudioFinishRecorderListener{
        void onFinish(float seconds, String filePath);
    }



    @Override
    protected void inflateKeyboardBar() {
        mInflater.inflate(R.layout.view_keyboard_userdef, this);
    }


    @Override
    protected View inflateFunc() {
        return mInflater.inflate(R.layout.view_func_emoticon_userdef, this, false);
    }

    @Override
    public void reset() {
        EmoticonsKeyboardUtils.closeSoftKeyboard(getContext());
        mLyKvml.hideAllFuncView();
        mBtnFace.setImageResource(R.drawable.face_icon);
    }

    @Override
    public void onFuncChange(int key) {
        if (FUNC_TYPE_EMOTION == key){
            mBtnFace.setImageResource(R.drawable.keyboard_icon);
        }else{
            mBtnFace.setImageResource(R.drawable.face_icon);
        }
        checkVoice();
    }

    @Override
    protected void showText() {
        mEtChat.setVisibility(View.VISIBLE);
        mBtnFace.setVisibility(View.VISIBLE);
        mBtnVoice.setVisibility(View.GONE);
    }


    @Override
    protected void showVoice() {
        mEtChat.setVisibility(GONE);
        mBtnFace.setVisibility(GONE);
        mBtnVoice.setVisibility(VISIBLE);
        reset();
    }


    @Override
    protected void checkVoice() {
        if (mBtnVoice.isShown()){
            mBtnVoiceOrText.setImageResource(R.drawable.keyboard_icon);
        }else{
            mBtnVoiceOrText.setImageResource(R.drawable.mic_icon);
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == sj.keyboard.R.id.btn_voice_or_text){
            if (mEtChat.isShown()){
                mBtnVoiceOrText.setImageResource(R.drawable.keyboard_icon);
                showVoice();
            }else{
                showText();
                mBtnVoiceOrText.setImageResource(R.drawable.mic_icon);
                EmoticonsKeyboardUtils.openSoftKeyboard(mEtChat);

            }
        }else if (i == sj.keyboard.R.id.btn_face){
            toggleFuncView(FUNC_TYPE_EMOTION);
        }else if (i == sj.keyboard.R.id.btn_multimedia){
            toggleFuncView(FUNC_TYPE_APPPS);
            setFuncViewHeight(EmoticonsKeyboardUtils.dip2px(getContext(),
                    (appNum / APPS_PER_LINE + (appNum % APPS_PER_LINE > 0 ? 1 : 0)) * APPS_HEIGHT_PER_LINE));
        }

    }
}
