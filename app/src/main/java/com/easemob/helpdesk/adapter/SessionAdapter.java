package com.easemob.helpdesk.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easemob.helpdesk.R;
import com.easemob.helpdesk.emoticon.utils.SimpleCommonUtils;
import com.easemob.helpdesk.listener.OnItemClickListener;
import com.easemob.helpdesk.utils.CommonUtils;
import com.easemob.helpdesk.utils.DateUtils;
import com.easemob.helpdesk.utils.PreferenceUtils;
import com.hyphenate.kefusdk.entity.HDSession;
import com.hyphenate.kefusdk.messagebody.HDImageMessageBody;
import com.hyphenate.kefusdk.entity.HDMessage;
import com.hyphenate.kefusdk.messagebody.HDNormalFileMessageBody;
import com.hyphenate.kefusdk.messagebody.HDTextMessageBody;
import com.hyphenate.kefusdk.messagebody.HDVideoMessageBody;
import com.hyphenate.kefusdk.messagebody.HDVoiceMessageBody;
import com.hyphenate.kefusdk.manager.session.CurrentSessionManager;
import com.hyphenate.kefusdk.utils.MessageUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.easemob.helpdesk.R.id.dark;
import static com.easemob.helpdesk.R.id.originType;

/**
 * Created by lyuzhao on 2015/12/10.
 */
public class SessionAdapter extends RecyclerView.Adapter<SessionAdapter.MyViewHolder> implements Filterable {

    private Context mContext;
    public OnItemClickListener clickListener;
    private List<HDSession> sessionList;
    private HDSession[] sessions;
    private Filter mFilter;
    private static final int HANDLER_MESSAGE_REFRESH_LIST = 0;

    private WeakHandler handler;

    private List<HDSession> unStickSessions = new ArrayList<>();
    private List<HDSession> StickSessions = new ArrayList<>();

    private static class WeakHandler extends Handler {
        WeakReference<SessionAdapter> weakReference;

        public WeakHandler(SessionAdapter adapter) {
            this.weakReference = new WeakReference<SessionAdapter>(adapter);
        }

        private void refreshList() {
            SessionAdapter adapter = weakReference.get();
            if (null != adapter) {
                String stickSessionId = PreferenceUtils.getInstance().getStickSessionId();
                if (stickSessionId != null && stickSessionId.trim().length() > 0) {
                    List<String> stickSessionIds = Arrays.asList(stickSessionId.split(" "));

                    adapter.unStickSessions.clear();
                    adapter.StickSessions.clear();

                    for (String id : stickSessionIds) {
                        for (HDSession session : adapter.sessionList) {
                            if (id.equals(session.getServiceSessionId())) {
                                adapter.StickSessions.add(session);
                            } else if (!stickSessionIds.contains(session.getServiceSessionId()) && !adapter.unStickSessions.contains(session)) {
                                adapter.unStickSessions.add(session);
                            }
                        }
                    }
                    adapter.sessionList.clear();
                    adapter.sessionList.addAll(adapter.unStickSessions);

                    for (int i = 0; i < adapter.StickSessions.size(); i++) {
                        adapter.sessionList.add(i, adapter.StickSessions.get(i));
                    }
                }
                adapter.sessions = adapter.sessionList.toArray(new HDSession[adapter.sessionList.size()]);
                adapter.notifyDataSetChanged();
            }
        }

        @Override public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case HANDLER_MESSAGE_REFRESH_LIST:
                    refreshList();
                    break;
                default:
                    break;
            }
        }
    }

    //置顶操作
    public void refreshStick(int position) {
        HDSession session = sessionList.get(position);

        String stickSessionId = PreferenceUtils.getInstance().getStickSessionId();
        StringBuilder builder = new StringBuilder();
        List<String> stickSessionIds = new ArrayList<>();
        if (stickSessionId != null && stickSessionId.length() > 0) {
            stickSessionIds.addAll(Arrays.asList(stickSessionId.split(" ")));

            if (!stickSessionIds.contains(session.getServiceSessionId())) {
                builder.append(session.getServiceSessionId()).append(" ");
                builder.append(stickSessionId);
            } else {
                builder.append(session.getServiceSessionId()).append(" ");
                for (String id : stickSessionIds) {
                    if (!id.equals(session.getServiceSessionId())) {
                        builder.append(id).append(" ");
                    }
                }
            }
        } else {
            builder.append(session.getServiceSessionId()).append(" ");
        }
        PreferenceUtils.getInstance().setStickSessionId(builder.toString());

        sessionList.remove(position);
        sessionList.add(0, session);
        sessions = sessionList.toArray(new HDSession[sessionList.size()]);
        notifyDataSetChanged();
    }

    //取消置顶操作
    public void cancelRefreshStick(int position) {
        HDSession session = sessionList.get(position);

        String stickSessionId = PreferenceUtils.getInstance().getStickSessionId();
        StringBuilder builder = new StringBuilder();
        List<String> stickSessionIds = new ArrayList<>();
        stickSessionIds.addAll(Arrays.asList(stickSessionId.split(" ")));

        if (stickSessionIds.contains(session.getServiceSessionId())) {
            stickSessionIds.remove(session.getServiceSessionId());
        }

        for (String id : stickSessionIds) {
            builder.append(id).append(" ");
        }

        PreferenceUtils.getInstance().setStickSessionId(builder.toString());

        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }

    public SessionAdapter(Context context) {
        this.mContext = context;
        handler = new WeakHandler(this);
        sessionList = CurrentSessionManager.getInstance().getSessions();
    }

    @Override public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View convertView = LayoutInflater.from(mContext).inflate(R.layout.row_chat_history, viewGroup, false);
        return new MyViewHolder(convertView);
    }

    public void refresh() {
        if (handler.hasMessages(HANDLER_MESSAGE_REFRESH_LIST)) {
            return;
        }
        Message msg = handler.obtainMessage(HANDLER_MESSAGE_REFRESH_LIST);
        handler.sendMessage(msg);
    }

    @SuppressLint("RecyclerView") @Override public void onBindViewHolder(final MyViewHolder viewHolder, final int position) {
        if (sessions == null || position >= sessions.length || position < 0) {
            return;
        }
        final HDSession sEntity = sessions[position];
        if (sEntity == null) {
            return;
        }
        if (sEntity.getUser() != null) {
            if (!TextUtils.isEmpty(sEntity.getUser().getNicename())) {
                viewHolder.nameTextView.setText(sEntity.getUser().getNicename());
            } else if (!TextUtils.isEmpty(sEntity.getUser().getUsername())) {
                viewHolder.nameTextView.setText(sEntity.getUser().getUsername());
            } else {
                viewHolder.nameTextView.setText(sEntity.getUser().getUserId());
            }
        }
        viewHolder.unReadMsgNum.setVisibility(View.INVISIBLE);

        if (StickSessions.contains(sEntity)) {
            viewHolder.itemView.setBackgroundColor(Color.parseColor("#F0FFFF"));
        }else {
            viewHolder.itemView.setBackgroundColor(Color.parseColor("#ffffff"));

        }
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {

            @Override public void onClick(View v) {
                if (clickListener != null) {
                    clickListener.onClick(viewHolder.itemView, position);
                }
            }
        });
        viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override public boolean onLongClick(View v) {
                if (clickListener != null) {
                    if (StickSessions.contains(sEntity)) {
                        clickListener.onLongClick(viewHolder.itemView, position, 0);
                    }else {
                        clickListener.onLongClick(viewHolder.itemView, position, 1);
                    }
                }
                return false;
            }
        });
        if (sEntity.hasUnReadMessage()) {
            viewHolder.unReadMsgNum.setVisibility(View.VISIBLE);
            if (sEntity.getUnReadMessageCount() <= 99) {
                viewHolder.unReadMsgNum.setText(sEntity.getUnReadMessageCount() + "");
            } else {
                viewHolder.unReadMsgNum.setText("99+");
            }
        }
        String originType = sEntity.getOriginType();
        if (originType != null) {
            switch (originType) {
                case "weibo":
                    viewHolder.ivOriginType.setImageResource(R.drawable.channel_weibo_icon);
                    break;
                case "weixin":
                    viewHolder.ivOriginType.setImageResource(R.drawable.channel_wechat_icon);
                    break;
                case "webim":
                    viewHolder.ivOriginType.setImageResource(R.drawable.channel_web_icon);
                    break;
                case "app":
                    viewHolder.ivOriginType.setImageResource(R.drawable.channel_app_icon);
                    break;
            }
        }

        HDMessage lastMessage = sEntity.getLastChatMessage();
        if (lastMessage == null) {
            viewHolder.tvTime.setText(DateUtils.getTimestampString(new Date(sEntity.getCreateDateTime())));
            viewHolder.tvMessage.setText("");
            return;
        }
        if (MessageUtils.isRecallMessage(lastMessage)) {
            viewHolder.tvMessage.setText("[被撤回的消息]");
        } else if (lastMessage.getBody() == null) {
            viewHolder.tvMessage.setText("");
        } else if (lastMessage.getBody() instanceof HDVoiceMessageBody) {
            viewHolder.tvMessage.setText("[语音]");
        } else if (lastMessage.getBody() instanceof HDVideoMessageBody) {
            viewHolder.tvMessage.setText("[视频]");
        } else if (lastMessage.getBody() instanceof HDImageMessageBody) {
            viewHolder.tvMessage.setText("[图片]");
        } else if (lastMessage.getBody() instanceof HDNormalFileMessageBody) {
            viewHolder.tvMessage.setText("[文件]");
        } else if (lastMessage.getBody() instanceof HDTextMessageBody) {
            //            viewHolder.tvMessage.setText(SmileUtils.getSmiledText(mContext, CommonUtils.convertStringByMessageText(((HDTextMessageBody) lastMessage.body).getMessage())),
            //                    TextView.BufferType.SPANNABLE);
            SimpleCommonUtils.spannableEmoticonFilter(viewHolder.tvMessage,
                    CommonUtils.convertStringByMessageText(((HDTextMessageBody) lastMessage.getBody()).getMessage()));
        }

        if (lastMessage.getTimestamp() > 0) {
            viewHolder.tvTime.setText(DateUtils.getTimestampString(new Date(lastMessage.getTimestamp())));
        } else if (sEntity.getCreateDateTime() > 0) {
            viewHolder.tvTime.setText(DateUtils.getTimestampString(new Date(sEntity.getCreateDateTime())));
        } else {
            viewHolder.tvTime.setText("");
        }
    }

    public HDSession getItem(int position) {
        if (sessions != null && position < sessions.length) {
            return sessions[position];
        }
        return null;
    }

    @Override public int getItemCount() {
        return sessions == null ? 0 : sessions.length;
    }

    @Override public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new SessionFilter(sessionList);
        }
        return mFilter;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView nameTextView;
        ImageView avatar;
        TextView unReadMsgNum;
        TextView tvTime;
        TextView tvMessage;
        RelativeLayout list_item_layout;
        ImageView ivOriginType;

        public MyViewHolder(View itemView) {
            super(itemView);
            avatar = (ImageView) itemView.findViewById(R.id.avatar);
            nameTextView = (TextView) itemView.findViewById(R.id.name);
            unReadMsgNum = (TextView) itemView.findViewById(R.id.unread_msg_number);
            tvMessage = (TextView) itemView.findViewById(R.id.message);
            tvTime = (TextView) itemView.findViewById(R.id.time);
            list_item_layout = (RelativeLayout) itemView.findViewById(R.id.list_item_layout);
            ivOriginType = (ImageView) itemView.findViewById(originType);
        }
    }

    public class SessionFilter extends Filter {
        List<HDSession> mOriginalValues = null;

        public SessionFilter(List<HDSession> list) {
            this.mOriginalValues = list;
        }

        @Override protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();
            if (mOriginalValues == null) {
                mOriginalValues = new ArrayList<HDSession>();
            }
            if (prefix == null || prefix.length() == 0) {
                results.values = mOriginalValues;
                results.count = mOriginalValues.size();
            } else {
                String prefixString = prefix.toString().toLowerCase(Locale.US);
                final int count = mOriginalValues.size();
                final ArrayList<HDSession> newValues = new ArrayList<HDSession>();
                for (int i = 0; i < count; i++) {
                    final HDSession value = mOriginalValues.get(i);
                    HDSession.CustomerInfo customerInfo = value.getCustomerInfo();
                    String nickName = null;
                    String trueName = null;
                    if (customerInfo != null) {
                        if (customerInfo.customerNiceName != null) {
                            nickName = customerInfo.customerNiceName.toLowerCase(Locale.US);
                        }
                        if (customerInfo.customerTrueName != null) {
                            trueName = customerInfo.customerTrueName.toLowerCase(Locale.US);
                        }

                        if ((nickName != null && nickName.contains(prefixString)) || (trueName != null && trueName.contains(prefixString))) {
                            newValues.add(value);
                        }
                    } else {
                        String valueText;
                        if (!TextUtils.isEmpty(value.getUser().getNicename())) {
                            valueText = value.getUser().getNicename().toLowerCase(Locale.US);
                        } else {
                            valueText = value.getUser().getUserId().toLowerCase(Locale.US);
                        }
                        if (valueText.contains(prefixString)) {
                            newValues.add(value);
                        }
                    }
                }
                results.values = newValues;
                results.count = newValues.size();
            }
            return results;
        }

        @Override protected void publishResults(CharSequence constraint, FilterResults results) {
            sessionList = (List<HDSession>) results.values;
            refresh();
        }
    }
}
