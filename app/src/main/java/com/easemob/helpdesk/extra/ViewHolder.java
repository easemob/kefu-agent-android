package com.easemob.helpdesk.extra;

import android.util.SparseArray;
import android.view.View;

/**
 * Created by liyuzhao on 16/8/9.
 * <p/>
 * 用法:
 * ImageView bananaView = ViewHolder.get(convertView, R.id.banana);
 * TextView phoneView = ViewHolder.get(convertView, R.id.phone);
 * BananaPhone bananaPhone = getItem(position);
 * phoneView.setText(bananaPhone.getPhone());
 */
public class ViewHolder {
    public static <T extends View> T get(View view, int id) {
        SparseArray<View> viewHolder = (SparseArray<View>) view.getTag();
        if (viewHolder == null) {
            viewHolder = new SparseArray<View>();
            view.setTag(viewHolder);
        }

        View childView = viewHolder.get(id);
        if (childView == null) {
            childView = view.findViewById(id);
            viewHolder.put(id, childView);
        }
        return (T) childView;
    }


}
