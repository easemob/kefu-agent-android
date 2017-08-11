package com.easemob.helpdesk.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easemob.helpdesk.R;
import com.easemob.helpdesk.entity.SlidingMenuItemEntity;
import com.flyco.roundview.RoundTextView;

import java.util.List;

/**
 * Created by liyuzhao on 16/2/29.
 */
public class SlidingMenuListAdapter extends BaseAdapter {

    private Context mContext;
    private List<SlidingMenuItemEntity> mList;

    public SlidingMenuListAdapter(Context context, List<SlidingMenuItemEntity> list) {
        this.mContext = context;
        this.mList = list;
    }


    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public SlidingMenuItemEntity getItem(int i) {
        return mList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    public void updateListItem(List<SlidingMenuItemEntity> list) {
        this.mList = list;
        notifyDataSetChanged();
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item_slidingmenu, null);
            holder.itemView = (RelativeLayout) convertView.findViewById(R.id.item_view);
            holder.imageView = (ImageView) convertView.findViewById(R.id.icon_left);
            holder.txtName = (TextView) convertView.findViewById(R.id.title_name);
//            holder.countTextView = (RoundTextView) convertView.findViewById(R.id.tv_count);
            holder.tvCount = (TextView) convertView.findViewById(R.id.tv_count);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        SlidingMenuItemEntity entty = getItem(position);
        try{
            convertView.setId(entty.id);
        }catch (Exception ignored){}

        try{
            holder.imageView.setImageDrawable(entty.icon);
        }catch (Exception ignored){}

        holder.txtName.setText(entty.title);
        if(entty.count>0){
            holder.tvCount.setVisibility(View.VISIBLE);
            if(entty.count > 99){
                holder.tvCount.setText("99+");
            }else {
                holder.tvCount.setText(entty.count+"");
            }
        }else {
            holder.tvCount.setVisibility(View.GONE);
        }

        return convertView;
    }


    class ViewHolder {
        RelativeLayout itemView;
        ImageView imageView;
        TextView txtName;
        TextView tvCount;
        RoundTextView countTextView;
    }
}
