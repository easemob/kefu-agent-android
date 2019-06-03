package com.easemob.helpdesk.emoticon.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.easemob.helpdesk.R;
import com.easemob.helpdesk.emoticon.data.AppBean;
import com.easemob.helpdesk.mvp.BaseChatActivity;
import com.easemob.helpdesk.mvp.ChatActivity;

import java.util.ArrayList;

public class ChattingAppsAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private Context mContext;
    private ArrayList<AppBean> mDdata = new ArrayList<>();

    public ChattingAppsAdapter(Context context, ArrayList<AppBean> data) {
        this.mContext = context;
        this.inflater = LayoutInflater.from(context);
        if (data != null) {
            this.mDdata = data;
        }
    }

    @Override public int getCount() {
        return mDdata.size();
    }

    @Override public Object getItem(int position) {
        return mDdata.get(position);
    }

    @Override public long getItemId(int position) {
        return position;
    }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = inflater.inflate(R.layout.item_app_userdef, parent, false);
            viewHolder.iv_icon = (ImageView) convertView.findViewById(R.id.iv_icon);
            viewHolder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final AppBean appBean = mDdata.get(position);
        if (appBean != null) {
            viewHolder.iv_icon.setBackgroundResource(appBean.getIcon());
            viewHolder.tv_name.setText(appBean.getFuncName());
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    final int id = appBean.getId();
                    switch (id) {
                        case 1:
                            if (mContext instanceof BaseChatActivity) {
                                ((BaseChatActivity) mContext).selectPicFromLocal();
                            }
                            break;
                        case 2:
                            if (mContext instanceof BaseChatActivity) {
                                ((BaseChatActivity) mContext).selectVideoFromLocal();
                            }
                            break;
                        case 3:
                            if (mContext instanceof BaseChatActivity) {
                                ((BaseChatActivity) mContext).selectFileFromLocal();
                            }
                            break;
                        case 4:
                            if (mContext instanceof ChatActivity) {
                                ((ChatActivity) mContext).toPhraseUI();
                            }
                            break;
                        case 5:
                            if (mContext instanceof ChatActivity) {
                                ((ChatActivity) mContext).toCustomWebView();
                            }
                            break;
                        case 6:
                            if (mContext instanceof ChatActivity) {
                                ((ChatActivity) mContext).eval_send(null);
                            }
                            break;
                    }
                }
            });
        }
        return convertView;
    }

    class ViewHolder {
        public ImageView iv_icon;
        public TextView tv_name;
    }
}
