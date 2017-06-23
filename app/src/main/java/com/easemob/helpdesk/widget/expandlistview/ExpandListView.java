package com.easemob.helpdesk.widget.expandlistview;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.widget.AdapterView;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.easemob.helpdesk.R;

public class ExpandListView extends ListView {

    private static final long ANIMATOR_START_DELAY = 0L;

    private long mDuration;
    private Interpolator mInterpolator;
    private OnParentItemClickListener mParentItemClickListener;
    private OnChildItemClickListener mChildItemClickListener;
    private OnChildItemLongClickListener mChildItemLongClickListener;
    private boolean mAllItemCanOpen = true;
    private boolean mCanClickClose = true;
    private boolean mOpenAllItem = false;
    private int mStatusArrowViewId = 0;

    // ExpandAnimation use field
    private boolean mRunningAnimation = false;
    private int beforePosition = -1;

    public ExpandListView(Context context) {
        super(context);
    }

    public ExpandListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ExpandListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        super.setAdapter(adapter);
        this.setOnItemClickListener(new ExpandListViewOnItemClickListener());
        getExpandAdapter().setChildClickListener(new ExpandListViewChildOnClickListener());
        getExpandAdapter().setChildLongClickListener(new ExpandListViewChildOnLongClickListener());
        if (mOpenAllItem) {
            getExpandAdapter().openAllItem();
        }
        setSelector(R.color.expandlistview_selector);
        getExpandAdapter().setStatusArrowViewId(mStatusArrowViewId);
        if (beforePosition != -1) {
            getExpandAdapter().updatePositionSet(beforePosition);
        }
    }

    /**
     * 获取ExpandListAdapter
     * @return
     */
    public BaseExpandListAdapter getExpandAdapter() {
        if (getAdapter() != null) {
            if (getAdapter() instanceof BaseExpandListAdapter) {
                return (BaseExpandListAdapter) getAdapter();
            } else if (getAdapter() instanceof HeaderViewListAdapter) {
                return (BaseExpandListAdapter) ((HeaderViewListAdapter)getAdapter()).getWrappedAdapter();
            }
        }
        return null;
    }

    /**
     * 设置展开动画的时间
     * @param duration
     */
    public void setExpandDuration(long duration) {
        this.mDuration = duration;
    }

    /**
     * 设置展开动画的时间
     * @param duration
     */
    public void setExpandDuration(int duration) {
        setExpandDuration((long) duration);
    }

    /**
     * 设置展开动画的Interpolator
     * @param i
     */
    public void setExpandInterpolator(Interpolator i) {
        this.mInterpolator = i;
    }

    /**
     * 设置父项的点击事件Listener
     * @param l
     */
    public void setOnParentItemClickListener(OnParentItemClickListener l) {
        this.mParentItemClickListener = l;
    }

    /**
     * 设置子项的点击事件Listener
     * @param l
     */
    public void setOnChildItemClickListener(OnChildItemClickListener l) {
        this.mChildItemClickListener = l;
    }

    /**
     * 设置子项的长点击事件Listener
     * @param l
     */
    public void setOnChildItemLongClickListener(OnChildItemLongClickListener l){
    	this.mChildItemLongClickListener = l;
    }
    
    /**
     * 设置所有的Item是否可以同时展开。 <br>
     * false的话只允许展开一个Item，之前已经展开的Item会关闭。
     * @param isAllItemCanOpen
     */
    public void setAllItemCanOpen(boolean isAllItemCanOpen) {
        this.mAllItemCanOpen = isAllItemCanOpen;
        if (isAllItemCanOpen) {
            this.mCanClickClose = true;
        }
    }

    /**
     * 设置是否支持点击后关闭Item
     * @param isCanClickClose
     */
    public void setCanClickClose(boolean isCanClickClose) {
        this.mCanClickClose = isCanClickClose;
    }

    /**
     * 设置默认展开所有Item
     * @param isOpenAllItem
     */
    public void setOpenAllItem(boolean isOpenAllItem) {
        this.mOpenAllItem = isOpenAllItem;
        if (isOpenAllItem) {
            if (getExpandAdapter() != null) {
                getExpandAdapter().openAllItem();
            }
        }
    }

    /**
     * 设置用于显示Item状态的箭头的View对应的ID，通过该ID所find到的View必须存在于Item的<b>父项</b>中
     * @param id
     */
    public void setStatusArrowViewId(int id) {
        this.mStatusArrowViewId = id;
        if (getExpandAdapter() != null) {
            getExpandAdapter().setStatusArrowViewId(mStatusArrowViewId);
        }
    }

    /**
     * 设置默认显示打开状态的Item
     * @param position
     */
    public void setDefaultOpenItemPosition(int position) {
        this.beforePosition = position;
        getExpandAdapter().updatePositionSet(beforePosition);
    }

    private void runOpenExpandAnimation(View view, final int index, final int oldIndex) {
        ExpandAnimation animation = new ExpandAnimation(view, mDuration);
        if (mInterpolator != null) {
            animation.setInterpolator(mInterpolator);
        }
        animation.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                mRunningAnimation = true;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (!mAllItemCanOpen) {
                    if (oldIndex != -1 && oldIndex != index && getExpandAdapter().isItemOpening(oldIndex)) {
                        getExpandAdapter().updatePositionSet(index, oldIndex);
                    } else {
                        getExpandAdapter().updatePositionSet(index);
                    }
                } else {
                    getExpandAdapter().updatePositionSet(index);
                }
                mRunningAnimation = false;
            }
        });
        if (!mAllItemCanOpen) {
            if (oldIndex != -1 && oldIndex != index && oldIndex >= getFirstVisiblePosition() && oldIndex <= getLastVisiblePosition()) {
                runCloseExpandAnimation(((View) getExpandAdapter().getItem(oldIndex)).findViewById(R.id.expandlistview_children_layout), oldIndex, false);
                runCloseStatusArrowImageAnimation(((View) getExpandAdapter().getItem(oldIndex)).findViewById(mStatusArrowViewId));
            }
        }
        view.startAnimation(animation);
        new Thread(new ResetAnimationStatus()).start();
    }

    private void runCloseExpandAnimation(View view, final int index, boolean flag) {
        if (getExpandAdapter().isItemOpening(index)) {
            ExpandAnimation animation = new ExpandAnimation(view, mDuration);
            if (mInterpolator != null) {
                animation.setInterpolator(mInterpolator);
            }
            if (flag) {
                animation.setAnimationListener(new Animation.AnimationListener() {

                    @Override
                    public void onAnimationStart(Animation animation) {
                        mRunningAnimation = true;
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        getExpandAdapter().updatePositionSet(index);
                        mRunningAnimation = false;
                    }
                });
            }
            view.startAnimation(animation);
            new Thread(new ResetAnimationStatus()).start();
        }
    }

    private void runOpenStatusArrowImageAnimation(View v) {
        if (mStatusArrowViewId == 0) {
            return;
        }
        ViewPropertyAnimatorCompat animator = ViewCompat.animate(v);
        animator.rotation(90.0f);
        animator.setStartDelay(ANIMATOR_START_DELAY);
        animator.setDuration(mDuration);
        animator.setInterpolator(mInterpolator);
        animator.start();
    }

    private void runCloseStatusArrowImageAnimation(View v) {
        if (mStatusArrowViewId == 0) {
            return;
        }
        ViewPropertyAnimatorCompat animator = ViewCompat.animate(v);
        animator.rotation(0.0f);
        animator.setStartDelay(ANIMATOR_START_DELAY);
        animator.setDuration(mDuration);
        animator.setInterpolator(mInterpolator);
        animator.start();

    }

    private class ExpandListViewOnItemClickListener implements OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            int itemPosition = position;

            // 判断HeaderView的个数
            if (getHeaderViewsCount() > 0) {
                // 重新计算position
                itemPosition = position - getHeaderViewsCount();
                // 排除点击HeaderView的position
                if (itemPosition < 0) {
                    return;
                }
            }

            // 判断FooterView的个数
            if (getFooterViewsCount() > 0) {
//                // 重新计算position
//                itemPosition = position - getFooterViewsCount();
                // 排除点击FooterView的position
                if (itemPosition >= getExpandAdapter().getCount()) {
                    return;
                }
            }

            if (!mRunningAnimation) {
                if (getExpandAdapter().isCanExpand(itemPosition)) { // 判断当前Item是否有子项可以展开
                    // 获得子项所在的Layout
                    View childrenLayout = view.findViewById(R.id.expandlistview_children_layout);

                    if (!getExpandAdapter().isItemOpening(itemPosition)) { // 如果当前Item是关闭状态，则动画展开

                        runOpenExpandAnimation(childrenLayout, itemPosition, beforePosition);
                        runOpenStatusArrowImageAnimation(view.findViewById(mStatusArrowViewId));
                        beforePosition = itemPosition;

                    } else { // 如果当前Item是打开状态

                        if (mCanClickClose) { // 如果允许点击关闭，则动画关闭当前Item
                            runCloseExpandAnimation(childrenLayout, itemPosition, true);
                            runCloseStatusArrowImageAnimation(view.findViewById(mStatusArrowViewId));
                            beforePosition = itemPosition;
                        }

                    }
                } else {
                    if (beforePosition != - 1 && getExpandAdapter().isCanExpand(beforePosition) && !mAllItemCanOpen) { // 如果只允许一个Item打开，则关闭之前所打开的Item
                        runCloseExpandAnimation(((View) getExpandAdapter().getItem(beforePosition)).findViewById(R.id.expandlistview_children_layout), beforePosition, true);
                        runCloseStatusArrowImageAnimation(((View) getExpandAdapter().getItem(beforePosition)).findViewById(mStatusArrowViewId));
                        beforePosition = itemPosition;
                    }
                }
            }

            // 传递父Item的点击事件
            if (mParentItemClickListener != null) {
                ViewGroup parentView = (ViewGroup) view.findViewById(R.id.expandlistview_parent_layout);
                int parentPosition = (Integer) parentView.getTag(R.id.expandlistview_parent_position_tag);
                mParentItemClickListener.onItemClick(parentView.getChildAt(0), parentPosition);
            }
        }
    }

    private class ExpandListViewChildOnClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            // 传递子Item的点击事件
            if (mChildItemClickListener != null) {
                int parentPosition = (Integer) v.getTag(R.id.expandlistview_parent_position_tag);
                int childPosition = (Integer) v.getTag(R.id.expandlistview_child_position_tag);
                mChildItemClickListener.onItemClick(v, parentPosition, childPosition);
            }
        }
    }

    private class ExpandListViewChildOnLongClickListener implements OnLongClickListener{

		@Override
		public boolean onLongClick(View v) {
			//传递子Item的长点击事件
			if(mChildItemLongClickListener != null){
				int parentPosition = (Integer) v.getTag(R.id.expandlistview_parent_position_tag);
                int childPosition = (Integer) v.getTag(R.id.expandlistview_child_position_tag);
                mChildItemLongClickListener.onItemLongClick(v, parentPosition, childPosition);
                return true;
			}
			return false;
		}
    	
    }
    
    
    /**
     * 用于更新当前所执行的动画状态，当展开动画非正常结束时，可以令mRunningAnimation的值设置回false
     */
    private class ResetAnimationStatus implements Runnable {

        @Override
        public void run() {
            try {
                Thread.sleep(mDuration);
                mRunningAnimation = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

}
