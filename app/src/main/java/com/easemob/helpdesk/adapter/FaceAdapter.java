package com.easemob.helpdesk.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.easemob.helpdesk.R;
import com.easemob.helpdesk.entity.ChatEmoji;

import java.util.List;


public class FaceAdapter extends BaseAdapter {

    private List<ChatEmoji> data;

    private LayoutInflater inflater;

    private int size=0;

    public FaceAdapter(Context context, List<ChatEmoji> list) {
        this.inflater=LayoutInflater.from(context);
        this.data=list;
        this.size=list.size();
    }

    @Override
    public int getCount() {
        return this.size;
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ChatEmoji emoji=data.get(position);
        ViewHolder viewHolder;
        if(convertView == null) {
            viewHolder=new ViewHolder();
            convertView=inflater.inflate(R.layout.item_face, parent, false);
            viewHolder.iv_face=(ImageView)convertView.findViewById(R.id.item_iv_face);
            convertView.setTag(viewHolder);
        } else {
            viewHolder=(ViewHolder)convertView.getTag();
        }
        if(emoji.getId() == R.drawable.icon_face_delete_2) {
            convertView.setBackgroundDrawable(null);
            viewHolder.iv_face.setImageResource(emoji.getId());
        } else if(TextUtils.isEmpty(emoji.getCharacter())) {
            convertView.setBackgroundDrawable(null);
            viewHolder.iv_face.setImageDrawable(null);
        } else {
            viewHolder.iv_face.setTag(emoji);
            viewHolder.iv_face.setImageResource(emoji.getId());
        }

        return convertView;
    }

    class ViewHolder {

        public ImageView iv_face;
    }
}