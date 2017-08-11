package com.easemob.helpdesk.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.easemob.helpdesk.AppConfig;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.widget.imageview.CircleDrawable;
import com.hyphenate.kefusdk.bean.HDCategorySummary;
import com.hyphenate.kefusdk.manager.main.CategorySummaryManager;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 结束标签，模糊搜索界面
 *
 * Created by lyuzhao on 2016/1/6.
 */
public class SearchCategoryActivity extends BaseActivity implements TextWatcher, TextView.OnEditorActionListener, AdapterView.OnItemClickListener {


    @BindView(R.id.btn_cancel)
    protected Button btnCancel;

    @BindView(R.id.listview)
    protected ListView mListView;

    @BindView(R.id.et_search)
    protected EditText etSearch;

    @BindView(R.id.emptyView)
    protected View emptyView;

    private List<HDCategorySummary> categoryTreeEntities = new ArrayList<HDCategorySummary>();
    private SearchAdapter mAdapter;
    private List<HDCategorySummary> tempTreeEntities = new ArrayList<HDCategorySummary>();
    private boolean isChildNode;
    private List<Long> enttyIds;
    private long parentId;
    private CategorySummaryManager manager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppConfig.setFitWindowMode(this);
        setContentView(R.layout.activity_search_category);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        String value = intent.getStringExtra("ids");
        parentId = intent.getLongExtra("parentid", 0L);
        enttyIds = getCategorySummaryIds(value);
        isChildNode = intent.getBooleanExtra("ischild",true);
        String sessionId = intent.getStringExtra("sessionId");
        manager = new CategorySummaryManager(sessionId);

        mAdapter = new SearchAdapter(this, categoryTreeEntities);
        mListView.setAdapter(mAdapter);
        initListener();
        loadData();
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

    /**
     * 获取所有的View，并设置其监听
     */
    private void initListener(){
        mListView.setEmptyView(emptyView);
        etSearch.addTextChangedListener(this);
        etSearch.setOnEditorActionListener(this);
        mListView.setOnItemClickListener(this);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    /**
     * 异步加载所有的标签
     */
    private void loadData() {
        Thread loadThread = new Thread(new Runnable() {
            @Override
            public void run() {
                List<HDCategorySummary> list = null;
                if(parentId == 0){
                    list = manager.getAllCategorySummarys();
                }else{
                    list = manager.getChildCategorySummarys(parentId);
                }
                tempTreeEntities.clear();
                tempTreeEntities.addAll(list);
            }
        });
        loadThread.start();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
            if(s.length() == 0){
                categoryTreeEntities.clear();
                mAdapter.notifyDataSetChanged();
            }else{
                String str = s.toString();
                searchContent(str);
            }
    }


    /**
     * 模糊筛选
     * @param s 填入的文本内容
     */
    public void searchContent(final String s){
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<HDCategorySummary> newValue = new ArrayList<HDCategorySummary>();
                for(HDCategorySummary item: tempTreeEntities){
                    String name = item.name;
                    if(isChildNode&&item.hasChildren){
                        continue;
                    }
                    if(enttyIds.contains(item.id)){
                        continue;
                    }
                    if(name.equalsIgnoreCase(s)){
                        newValue.add(item);
                    }else if(name.contains(s)){
                        newValue.add(item);
                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        categoryTreeEntities.clear();
                        categoryTreeEntities.addAll(newValue);
                        mAdapter.notifyDataSetChanged();
                    }
                });

            }
        }).start();



    }

    /**
     * 销毁UI时调用，关闭键盘
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideKeyboard();
    }

    /**
     * 监控键盘输入，如果点击的是键盘上的搜索按钮或回车按钮，则进行搜索
     *
     * @param v 监控的view
     * @param actionId 触发的事件ID
     * @param event 触发的事件（搜索，按下，回车等）
     * @return 返回值：如果true则有效，如果false则无效
     */
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (v.getId() == R.id.et_search) {
            hideKeyboard();
            String txtContent = etSearch.getText().toString().trim();
            if (TextUtils.isEmpty(txtContent)) {
                return false;
            }
            if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                searchContent(txtContent);
                return true;
            }
        }
        return false;
    }


    /**
     * 列表的Item项点击事件
     *
     * @param parent 父view
     * @param view ItemView
     * @param position 点击的位置
     * @param id 点击的ID
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
     final HDCategorySummary entty = (HDCategorySummary) parent.getItemAtPosition(position);
        setResult(RESULT_OK, new Intent().putExtra("tree", entty));
        finish();
    }


    /**
     * 搜索的适配器，为列表填充数据
     */
    class SearchAdapter extends BaseAdapter {
        private Context context;
        private List<HDCategorySummary> mList;
        public SearchAdapter(Context context, List<HDCategorySummary> list){
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
            if(convertView == null){
                convertView = LayoutInflater.from(context).inflate(R.layout.list_item_search_category, parent, false);
                holder = new ViewHolder();
                holder.itemText = (TextView) convertView.findViewById(R.id.item_text);
                holder.flLayout = (FrameLayout) convertView.findViewById(R.id.fl_layout);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }
            HDCategorySummary entty = getItem(position);
            String rootName = (TextUtils.isEmpty(entty.rootName))?"":entty.rootName+">";
            holder.itemText.setText(rootName + entty.name);
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
                CircleDrawable circleView = new CircleDrawable(context, Color.parseColor(strColor));
                circleView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                holder.flLayout.addView(circleView);
            }
            return convertView;
        }

        class ViewHolder{
            TextView itemText;
            FrameLayout flLayout;
        }

    }


}
