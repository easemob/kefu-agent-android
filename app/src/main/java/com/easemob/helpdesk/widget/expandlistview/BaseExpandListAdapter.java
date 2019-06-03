package com.easemob.helpdesk.widget.expandlistview;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;

import com.easemob.helpdesk.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class BaseExpandListAdapter extends BaseAdapter {

    private Context mContext;
    private BaseViewHolder mHolder;
    private Set<Integer> mPositionSet;
    private View.OnClickListener mChildClickListener = null;
    private View.OnLongClickListener mChildChildLongListener = null;

    private int mStatusArrowViewId = 0;
    private SparseArray<View> viewMap;

    public BaseExpandListAdapter(Context context) {
        this.mContext = context;
        mPositionSet = new HashSet<>();
        viewMap = new SparseArray<>();
    }

    @Override
    public int getCount() {
        return getParentCount();
    }

    @Override
    public Object getItem(int position) {
        return viewMap != null ? viewMap.get(position) : position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {

            mHolder = new BaseViewHolder();

            // 创建baseLayout
            mHolder.baseLayout = new LinearLayout(getContext());
            AbsListView.LayoutParams baseLP = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.WRAP_CONTENT);
            mHolder.baseLayout.setOrientation(LinearLayout.VERTICAL);
            mHolder.baseLayout.setLayoutParams(baseLP);

            // 创建parenLayout
            mHolder.parentLayout = new LinearLayout(getContext());
            AbsListView.LayoutParams parentLP = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.WRAP_CONTENT);
            mHolder.parentLayout.setOrientation(LinearLayout.VERTICAL);
            mHolder.parentLayout.setLayoutParams(parentLP);
            mHolder.parentLayout.setId(R.id.expandlistview_parent_layout);

            // 创建parentView
            mHolder.parentView = getParentView(position, null, mHolder.baseLayout);
            mHolder.parentLayout.addView(mHolder.parentView);

            // 创建childrenLayout
            mHolder.childrenLayout = new LinearLayout(getContext());
            mHolder.childrenLayout.setId(R.id.expandlistview_children_layout);
            AbsListView.LayoutParams childLP = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.WRAP_CONTENT);
            mHolder.childrenLayout.setOrientation(LinearLayout.VERTICAL);
            mHolder.childrenLayout.setLayoutParams(childLP);

            if (isCanExpand(position)) {
                // 创建childView
                int childCount = getChildCount(position);
                BaseChildViewHolder childHolder = new BaseChildViewHolder();
                childHolder.childrens = new ArrayList<>();
                for (int i = 0 ; i < childCount ; i ++) {
                    View childView = getChildView(position, i, null, mHolder.childrenLayout);
                    childHolder.childrens.add(childView);

                    childView.setTag(R.id.expandlistview_parent_position_tag, position);
                    childView.setTag(R.id.expandlistview_child_position_tag, i);
                    childView.setOnClickListener(mChildClickListener);
                    childView.setOnLongClickListener(mChildChildLongListener);
                    mHolder.childrenLayout.addView(childView);
                }
                mHolder.childrenLayout.setTag(R.id.expandlistview_children_layout_holder_tag, childHolder);
                mHolder.childrenLayout.setOnClickListener(null);
            } else {

                // 当Item不可展开的时候，设置children的个数为0
                BaseChildViewHolder childHolder = new BaseChildViewHolder();
                childHolder.childrens = new ArrayList<>();
                mHolder.childrenLayout.setTag(R.id.expandlistview_children_layout_holder_tag, childHolder);

                // 隐藏childrenLayout
                mHolder.childrenLayout.setVisibility(View.GONE);
                mHolder.childrenLayout.setOnClickListener(null);
            }

            // add view to BaseLayout
            mHolder.baseLayout.addView(mHolder.parentLayout);
            mHolder.baseLayout.addView(mHolder.childrenLayout);
            convertView = mHolder.baseLayout;
            convertView.setTag(mHolder);
        } else {

            // 重用convertView
            mHolder = (BaseViewHolder) convertView.getTag();

            // 重新getParentView，设置parentView的值
            mHolder.parentView = getParentView(position, mHolder.parentView, mHolder.parentLayout);

            if (isCanExpand(position)) {
                // 处理ChildView
                final int childCount = getChildCount(position);
                BaseChildViewHolder childHolder = (BaseChildViewHolder) mHolder.childrenLayout.getTag(R.id.expandlistview_children_layout_holder_tag);
                final int existChildCount = childHolder.childrens.size();

                if (existChildCount > childCount) {
                    for (int i = 0 ; i < existChildCount ; i ++) {
                        // 当前存在的子项比所需要的子项多，则隐藏多余的子项
                        if (i >= childCount) {
                            childHolder.childrens.get(i).setOnClickListener(null);
                            childHolder.childrens.get(i).setVisibility(View.GONE);
                            continue;
                        }

                        childHolder.childrens.get(i).setVisibility(View.VISIBLE);
                        View childView = childHolder.childrens.get(i);
                        childView.setVisibility(View.VISIBLE);
                        childView = getChildView(position, i, childView, mHolder.childrenLayout);
                        childView.setTag(R.id.expandlistview_parent_position_tag, position);
                        childView.setTag(R.id.expandlistview_child_position_tag, i);
                        childView.setOnClickListener(mChildClickListener);
                        childView.setOnLongClickListener(mChildChildLongListener);

                    }
                } else {
                    for (int i = 0 ; i < childCount ; i ++) {
                        // 当前存在的子项比所需要的子项少，则创建缺少的子项
                        if (i >= existChildCount) {
                            View childView = getChildView(position, i, null, mHolder.childrenLayout);
                            childView.setVisibility(View.VISIBLE);
                            childView.setTag(R.id.expandlistview_parent_position_tag, position);
                            childView.setTag(R.id.expandlistview_child_position_tag, i);
                            childView.setOnClickListener(mChildClickListener);
                            childView.setOnLongClickListener(mChildChildLongListener);
                            
                            childHolder.childrens.add(childView);
                            mHolder.childrenLayout.addView(childView);
                            continue;
                        }

                        childHolder.childrens.get(i).setVisibility(View.VISIBLE);
                        View childView = childHolder.childrens.get(i);
                        childView.setVisibility(View.VISIBLE);
                        childView = getChildView(position, i, childView, mHolder.childrenLayout);
                        childView.setTag(R.id.expandlistview_parent_position_tag, position);
                        childView.setTag(R.id.expandlistview_child_position_tag, i);
                        childView.setOnClickListener(mChildClickListener);
                        childView.setOnLongClickListener(mChildChildLongListener);
                    }

                }
                mHolder.childrenLayout.setTag(R.id.expandlistview_children_layout_holder_tag, childHolder);
                mHolder.childrenLayout.setVisibility(View.VISIBLE);
            } else {
                mHolder.childrenLayout.setVisibility(View.GONE);
            }

        }

        mHolder.parentLayout.setTag(R.id.expandlistview_parent_position_tag, position);

        // expand animation value
        int widthSpec = View.MeasureSpec.makeMeasureSpec(
                (int) (getContext().getResources().getDisplayMetrics().widthPixels - 10
                        * getContext().getResources().getDisplayMetrics().density),
                View.MeasureSpec.EXACTLY);
        mHolder.childrenLayout.measure(widthSpec, 0);
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mHolder.childrenLayout.getLayoutParams();
        if (mPositionSet.contains(position)) {
            lp.bottomMargin = 0;
            mHolder.childrenLayout.setVisibility(View.VISIBLE);
        } else {
            lp.bottomMargin = - mHolder.childrenLayout.getMeasuredHeight();
            mHolder.childrenLayout.setVisibility(View.GONE);
        }

        if (mStatusArrowViewId != 0) {
            resetArrowView(convertView, position);
        }

        if (getItemBackgroundResources() != 0) {
            mHolder.baseLayout.setBackgroundResource(getItemBackgroundResources());
        }
        viewMap.put(position, convertView);
        return convertView;
    }

    private static class BaseViewHolder {
        LinearLayout baseLayout;
        LinearLayout parentLayout;
        View parentView;
        LinearLayout childrenLayout;
    }

    private static class BaseChildViewHolder {
        List<View> childrens;
    }

    /**
     * 重置显示状态的箭头
     * @param convertView
     * @param position
     */
    private void resetArrowView(View convertView, int position) {
        if (!isCanExpand(position)) {
            return;
        }
        if (isItemOpening(position)) {
            ViewCompat.setRotation(convertView.findViewById(mStatusArrowViewId), 90.0f);
        } else {
            ViewCompat.setRotation(convertView.findViewById(mStatusArrowViewId), 0.0f);
        }
    }

    /**
     * 获取当前Context
     * @return
     */
    public Context getContext() {
        return mContext;
    }

    /**
     * 当前项是否为打开状态
     * @param position
     * @return
     */
    public boolean isItemOpening(int position) {
        return mPositionSet.contains(position);
    }

    public void setChildClickListener(View.OnClickListener l) {
        this.mChildClickListener = l;
    }
    
    public void setChildLongClickListener(View.OnLongClickListener l){
    	this.mChildChildLongListener = l;
    }

    public void setStatusArrowViewId(int id) {
        this.mStatusArrowViewId = id;
    }

    /**
     * 设置默认打开所有Item
     */
    public void openAllItem() {
        for (int i = 0 ; i < getParentCount() ; i ++) {
            if (isCanExpand(i)) {
                if (!mPositionSet.contains(i)) {
                    mPositionSet.add(i);
                }
            }
        }
        notifyDataSetChanged();
    }

    /**
     * 當允許打開多個Item的時候使用該方法更新Position
     * @param position
     */
    public void updatePositionSet(int position) {
        if (!mPositionSet.contains(position)) {
            mPositionSet.add(position);
        } else {
            mPositionSet.remove(position);
        }
        notifyDataSetChanged();
    }

    /**
     * 當只允許打開一個Item的時候使用該方法更新Position
     * @param position
     * @param oldPosition
     */
    public void updatePositionSet(int position, int oldPosition) {
        if (!mPositionSet.contains(position)) {
            mPositionSet.add(position);
        } else {
            mPositionSet.remove(position);
        }
        if (!mPositionSet.contains(oldPosition)) {
            mPositionSet.add(oldPosition);
        } else {
            mPositionSet.remove(oldPosition);
        }
        notifyDataSetChanged();
    }

    /**
     * 设置Item背景，返回资源id
     * @return
     */
    protected int getItemBackgroundResources() {
        return 0;
    }

    /**
     * 返回父项个数
     *
     * return the parent count
     */
    public abstract int getParentCount();

    /**
     * 根据父项position返回子项个数
     *
     * @param position parent position
     */
    public abstract int getChildCount(int position);

    /**
     * 返回当前父项是否可以展开 <br>
     *
     * @param position the parent item position
     */
    public abstract boolean isCanExpand(int position);

    /**
     * 返回父项的View，原理同BaseAdapter.getView(..)
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    public abstract View getParentView(int position, View convertView, ViewGroup parent);

    /**
     * 返回子项的View，原理同BaseAdapter.getView(..)
     * @param parentPosition
     * @param childPosition
     * @param convertView
     * @param parent
     * @return
     */
    public abstract View getChildView(int parentPosition, int childPosition, View convertView, ViewGroup parent);

}