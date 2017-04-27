package com.easemob.helpdesk.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.helpdesk.R;
import com.easemob.helpdesk.utils.CommonUtils;
import com.easemob.helpdesk.widget.imageview.CircleDrawable;
import com.hyphenate.kefusdk.bean.HDCategorySummary;
import com.hyphenate.kefusdk.db.HDDBManager;
import com.hyphenate.kefusdk.manager.CategorySummaryManager;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SelectCategoryTreeActivity extends BaseActivity{

    private static final int REQUEST_CODE_SEARCH = 0x01;

    @BindView(R.id.left)
    protected ImageButton leftBtn;

    @BindView(R.id.title)
    protected TextView txtTitle;

    @BindView(R.id.listView)
    protected ListView mListView;

    private List<HDCategorySummary> treeEntities = Collections.synchronizedList(new ArrayList<HDCategorySummary>());

    private SelectAdapter mAdapter;

    private LinkedList<HDCategorySummary> mLinkList = new LinkedList<HDCategorySummary>();
    private List<Long> enttyIds;

    private float y;

    @BindView(R.id.root)
    protected LinearLayout rootLayout;

    @BindView(R.id.iv_search)
    protected ImageView ivSearch;

    private boolean isChildNode;
    private CategorySummaryManager manager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_categorytree);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        isChildNode = intent.getBooleanExtra("ischild", true);
        String sessionId = intent.getStringExtra("sessionId");
        String value = intent.getStringExtra("ids");
        enttyIds = getCategorySummaryIds(value);
        manager = new CategorySummaryManager(sessionId);
        mAdapter = new SelectAdapter(this, treeEntities);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HDCategorySummary entty = (HDCategorySummary) parent.getItemAtPosition(position);
                if (entty.hasChildren) {
                    mLinkList.addLast(entty);
                    leftBtn.setVisibility(View.VISIBLE);
                    txtTitle.setText(entty.name);
                    treeEntities = manager.getChildCategorySummarys(entty.id);
                    mAdapter = new SelectAdapter(SelectCategoryTreeActivity.this, treeEntities);
                    mListView.setAdapter(mAdapter);
                    mAdapter.notifyDataSetChanged();
                } else {
                    if (!enttyIds.contains(entty.id)) {
                        setResult(RESULT_OK, new Intent().putExtra("tree", entty));
                        finish();
                        overridePendingTransition(0, R.anim.activity_close);
                    } else {
                        Toast.makeText(SelectCategoryTreeActivity.this, "此分类已存在！", Toast.LENGTH_SHORT).show();
                    }

                }

            }
        });

        loadData();
    }

    private void loadData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                treeEntities.clear();
                treeEntities.addAll(HDDBManager.getInstance().getCategoryTreeByParentId(0));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();
    }

    private List<Long> getCategorySummaryIds(String value){
        List<Long> mList = new ArrayList<Long>();
        try {
            JSONArray jsonArray = new JSONArray(value);
            for (int i = 0; i < jsonArray.length();i++){
                long id = jsonArray.getLong(i);
                mList.add(id);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return mList;
    }


    @OnClick(R.id.left)
    public void onClickByLeft(){
        if (!mLinkList.isEmpty()) {
            treeEntities = manager.getChildCategorySummarys(mLinkList.getLast().parentId);
            mAdapter = new SelectAdapter(SelectCategoryTreeActivity.this, treeEntities);
            mListView.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
            mLinkList.removeLast();
            if(mLinkList.size() == 0){
                leftBtn.setVisibility(View.GONE);
                txtTitle.setText("选择会话标签");
            }
        }
    }

    @OnClick(R.id.right)
    public void onClickByRight(){
        finish();
        overridePendingTransition(0, R.anim.activity_close);
    }

    @OnClick(R.id.search_layout)
    public void onClickBySearchLayout(){
        y = ivSearch.getY() + CommonUtils.convertDip2Px(this, 20);
        TranslateAnimation animation = new TranslateAnimation(0, 0, 0, -y);
        animation.setDuration(500);
        animation.setFillAfter(true);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), SearchCategoryActivity.class);
                intent.putExtra("sessionId", manager.getSessionId());
                intent.putExtra("ids", enttyIds.toString());
                intent.putExtra("ischild", isChildNode);
                if (mLinkList != null && mLinkList.size() > 0) {
                    intent.putExtra("parentid", mLinkList.getLast().id);
                } else {
                    intent.putExtra("parentid", 0L);
                }
                startActivityForResult(intent, REQUEST_CODE_SEARCH);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        rootLayout.startAnimation(animation);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        TranslateAnimation animation = new TranslateAnimation(0, 0, -y, 0);
        animation.setDuration(500);
        animation.setFillAfter(true);
        rootLayout.startAnimation(animation);
        if(resultCode == RESULT_OK){
            if(requestCode == REQUEST_CODE_SEARCH){
                setResult(RESULT_OK, data);
                finish();
            }
        }
    }

    class SelectAdapter extends BaseAdapter {
        private Context context;
        private List<HDCategorySummary> mList;
        public SelectAdapter(Context context, List<HDCategorySummary> list) {
            this.context = context;
            this.mList = list;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public HDCategorySummary getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.list_item_selectcategory, parent, false);
                holder = new ViewHolder();
                holder.itemText = (TextView) convertView.findViewById(R.id.item_text);
                holder.rightIcon = (ImageView) convertView.findViewById(R.id.iv_icon_right);
                holder.flLayout = (FrameLayout) convertView.findViewById(R.id.fl_layout);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            HDCategorySummary entty = getItem(position);
            holder.itemText.setText(entty.name);
            int color = (int) entty.color;
            String strColor;
            if(color == 0){
                strColor = "#000000";
            }else if(color == 255){
                strColor = "#ffffff";
            }else{
                strColor = "#"+Integer.toHexString(color);
                strColor = strColor.substring(0,7);
            }
            int childCount = holder.flLayout.getChildCount();
            if (childCount == 0){
                CircleDrawable circleDrawable = new CircleDrawable(context, Color.parseColor(strColor));
                circleDrawable.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                holder.flLayout.addView(circleDrawable);
            }
            if (entty.hasChildren) {
                holder.rightIcon.setVisibility(View.VISIBLE);
            } else {
                holder.rightIcon.setVisibility(View.INVISIBLE);
            }

            return convertView;
        }

        class ViewHolder {
            TextView itemText;
            ImageView rightIcon;
            FrameLayout flLayout;
        }
    }


}
