package com.easemob.helpdesk.widget.recyclerview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by liyuzhao on 31/01/2018.
 */

public class DividerItemDecoration2 extends RecyclerView.ItemDecoration {
	private Context mContext;
	private Drawable mDivider;
	private int mOrientation;
	public static final int HORIZONTAL_LIST = LinearLayoutManager.HORIZONTAL;
	public static final int VERTICAL_LIST = LinearLayoutManager.VERTICAL;

	public static final int[] ATRRS = new int[]{android.R.attr.listDivider};

	public DividerItemDecoration2(Context context, int orientation){
		this(context, orientation, null);
	}

	public DividerItemDecoration2(Context context, int orientation, Drawable divider){
		this.mContext = context;
		if (divider == null) {
			final TypedArray ta = context.obtainStyledAttributes(ATRRS);
			this.mDivider = ta.getDrawable(0);
			ta.recycle();
		} else {
			this.mDivider = divider;
		}
		setOrientation(orientation);
	}

	public void setOrientation(int orientation){
		if (orientation != HORIZONTAL_LIST && orientation != VERTICAL_LIST){
			throw new IllegalArgumentException("invalid orientation");
		}
	}

	@Override
	public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
		super.onDraw(c, parent, state);
		if (mOrientation == HORIZONTAL_LIST){
			drawVerticalLine(c, parent, state);
		}else{
			drawHorizontalLine(c, parent, state);
		}
	}

	public void drawHorizontalLine(Canvas c, RecyclerView parent, RecyclerView.State state){
		int left = parent.getPaddingLeft();
		int right = parent.getWidth() -parent.getPaddingRight();
		final int childCount = parent.getChildCount();
		for (int i = 0; i < childCount - 1; i++) {
			final View child = parent.getChildAt(i);

			final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
			final int top = child.getBottom() + params.bottomMargin;
			final int bottom = top + mDivider.getIntrinsicHeight();
			mDivider.setBounds(left, top, right, bottom);
			mDivider.draw(c);
		}
	}

	public void drawVerticalLine(Canvas c, RecyclerView parent, RecyclerView.State state){
		int top = parent.getPaddingTop();
		int bottom = parent.getHeight() - parent.getPaddingBottom();
		final int childCount = parent.getChildCount();
		for (int i = 0; i < childCount - 1; i++) {
			final View child = parent.getChildAt(i);

			final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
			final int left = child.getRight() + params.rightMargin;
			final int right = left + mDivider.getIntrinsicWidth();
			mDivider.setBounds(left, top, right, bottom);
			mDivider.draw(c);
		}
	}

	@Override
	public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
		if (mOrientation == HORIZONTAL_LIST){
			// 画横线，就是往下偏移一个分割线的高度
			outRect.set(0, 0, mDivider.getIntrinsicWidth(), 0);
		}else{
			// 画竖线， 就是往右偏移一个分割线的宽度
			outRect.set(0, 0, 0, mDivider.getIntrinsicHeight());
		}
	}
}
