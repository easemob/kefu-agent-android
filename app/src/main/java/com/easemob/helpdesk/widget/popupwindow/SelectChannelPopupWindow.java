package com.easemob.helpdesk.widget.popupwindow;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easemob.helpdesk.R;
import com.hyphenate.kefusdk.entity.TechChannel;

import java.util.List;

/**
 * Created by liyuzhao on 22/03/2017.
 */

public class SelectChannelPopupWindow extends PopupWindow {

    private Context mContext;
    private View view;

    public SelectChannelPopupWindow(Context context, List<TechChannel>  list, final AdapterView.OnItemClickListener itemClickListener){
        this.view = LayoutInflater.from(context).inflate(R.layout.pop_select_channels, null);
        //设置外部可点击
        this.setOutsideTouchable(true);
        // mMenuView添加OnTouchListener监听获取触屏位置如果在选择框外面则销毁弹出框
        this.view.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int height = view.findViewById(R.id.pop_layout).getTop();
                int y = (int)event.getY();
                if (event.getAction() == MotionEvent.ACTION_UP){
                    if (y < height){
                        dismiss();
                    }
                }
                return true;
            }
        });

        ListView listView = (ListView) view.findViewById(R.id.listview);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dismiss();
                if (itemClickListener != null){
                    itemClickListener.onItemClick(parent, view, position, id);
                }
            }
        });
        listView.setAdapter(new ChannelAdapter(context, R.layout.pop_select_channel_item, list));
        /**设置弹出窗口特征*/
        //设置视图
        this.setContentView(this.view);
        // 设置弹出窗体的宽和高
        this.setHeight(RelativeLayout.LayoutParams.WRAP_CONTENT);
        this.setWidth(RelativeLayout.LayoutParams.MATCH_PARENT);

        //设置弹出窗体可点击
        this.setFocusable(true);
        //实例化一个ColorDrawable颜色半透明
        ColorDrawable dw = new ColorDrawable(0xb000000);
        //设置弹出窗体的背景
        this.setBackgroundDrawable(dw);

        //设置弹出窗体显示时的动画，从底部向上弹出
        this.setAnimationStyle(R.style.select_channel_anim);
    }



    class ChannelAdapter extends ArrayAdapter<TechChannel>{

        private int resourceId;
        public ChannelAdapter(Context context, int resource, List<TechChannel> objects) {
            super(context, resource, objects);
            this.resourceId = resource;
        }


        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TechChannel techChannel = getItem(position);
            View view = LayoutInflater.from(getContext()).inflate(resourceId, null);
            TextView tvChannelName = (TextView) view.findViewById(R.id.tv_channel_name);
            tvChannelName.setText(techChannel.name);
            return view;
        }
    }


}
