package com.easemob.helpdesk.adapter.manager;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.easemob.helpdesk.R;
import com.easemob.helpdesk.emoticon.utils.SimpleCommonUtils;
import com.easemob.helpdesk.utils.CommonUtils;
import com.easemob.helpdesk.utils.DateUtils;
import com.hyphenate.kefusdk.entity.HDSession;
import com.hyphenate.kefusdk.entity.HDMessage;
import com.hyphenate.kefusdk.messagebody.HDImageMessageBody;
import com.hyphenate.kefusdk.messagebody.HDNormalFileMessageBody;
import com.hyphenate.kefusdk.messagebody.HDTextMessageBody;
import com.hyphenate.kefusdk.messagebody.HDVoiceMessageBody;
import com.jude.easyrecyclerview.adapter.BaseViewHolder;

import java.util.Date;

import static com.easemob.helpdesk.R.id.originType;

/**
 * Created by liyuzhao on 16/6/21.
 */
public class CurrentSessionHolder extends BaseViewHolder<HDSession> {

    TextView nameTextView;
    ImageView avatar;
    TextView unReadMsgNum;
    TextView tvTime;
    TextView tvMessage;
    ImageView tvOriginType;

    public CurrentSessionHolder(ViewGroup parent) {
        super(parent, R.layout.row_chat_history);
        avatar = $(R.id.avatar);
        nameTextView = $(R.id.name);
        unReadMsgNum = $(R.id.unread_msg_number);
        tvMessage = $(R.id.message);
        tvTime = $(R.id.time);
        tvOriginType = $(originType);
    }

    @Override
    public void setData(HDSession data) {
        super.setData(data);
        if (data == null){
            return;
        }
        if (data.getUser() != null){
            nameTextView.setText(data.getUser().getNicename());
        }
        unReadMsgNum.setVisibility(View.INVISIBLE);
        if (data.hasUnReadMessage()){
            unReadMsgNum.setVisibility(View.VISIBLE);
            if (data.getUnReadMessageCount() <= 99){
                unReadMsgNum.setText(data.getUnReadMessageCount() + "");
            }else{
                unReadMsgNum.setText("99+");
            }
        }
        String originType = data.getOriginType();
        if (originType != null){
            switch (originType) {
                case "weibo":
                    tvOriginType.setImageResource(R.drawable.channel_weibo_icon);
                    break;
                case "weixin":
                    tvOriginType.setImageResource(R.drawable.channel_wechat_icon);
                    break;
                case "webim":
                    tvOriginType.setImageResource(R.drawable.channel_web_icon);
                    break;
                case "app":
                    tvOriginType.setImageResource(R.drawable.channel_app_icon);
                    break;
            }
        }

        HDMessage lastMessage = data.getLastChatMessage();
        if (lastMessage == null){
            return;
        }
        if (lastMessage.getBody() == null){
            tvMessage.setText("");
        }else if (lastMessage.getBody() instanceof HDVoiceMessageBody){
            tvMessage.setText("[语音]");
        }else if (lastMessage.getBody() instanceof HDImageMessageBody){
            tvMessage.setText("[图片]");
        }else if (lastMessage.getBody() instanceof HDNormalFileMessageBody){
            tvMessage.setText("[文件]");
        }else if (lastMessage.getBody() instanceof HDTextMessageBody){
            SimpleCommonUtils.spannableEmoticonFilter(tvMessage, CommonUtils.convertStringByMessageText(((HDTextMessageBody)lastMessage.getBody()).getMessage()));
        }

        if (lastMessage.getTimestamp() > 0){
            tvTime.setText(DateUtils.getTimestampString(new Date(lastMessage.getTimestamp())));
        }else if (data.getCreateDateTime() > 0){
            tvTime.setText(DateUtils.getTimestampString(new Date(data.getCreateDateTime())));
        }



    }
}
