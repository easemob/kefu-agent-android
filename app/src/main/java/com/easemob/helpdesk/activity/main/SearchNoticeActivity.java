package com.easemob.helpdesk.activity.main;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.BaseActivity;
import com.easemob.helpdesk.adapter.NoticeListAdapter;
import com.easemob.helpdesk.entity.NoticeEntity;
import com.easemob.helpdesk.widget.recyclerview.DividerLine;
import com.jude.easyrecyclerview.EasyRecyclerView;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liyuzhao on 16/3/18.
 */
public class SearchNoticeActivity extends BaseActivity {

    private static final String TAG = SearchNoticeActivity.class.getSimpleName();
    private EasyRecyclerView recyclerView;
    private ImageButton btnClear;
    private EditText query;
    private List<NoticeEntity> mList = new ArrayList<>();
    private NoticeListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_history_session);
        initView();
        ArrayList<NoticeEntity> list = getIntent().getParcelableArrayListExtra("list");
        if (list != null && list.size() > 0) {
            mList.addAll(list);
        }
        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        //设置分割线
        DividerLine dividerLine = new DividerLine(DividerLine.VERTICAL);
        dividerLine.setSize(1);
        dividerLine.setColor(0xFFDDDDDD);
        recyclerView.addItemDecoration(dividerLine);

        recyclerView.setAdapterWithProgress(mAdapter = new NoticeListAdapter(this));
        mAdapter.setOnItemClickListener(new RecyclerArrayAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int i) {
//                NoticeEntity entity = mAdapter.getItem(i);
//                if (entity == null){
//                    return;
//                }
//                Intent intent = new Intent();
//                intent.putExtra("activity_id", entity.activity_id);
//                setResult(RESULT_OK, intent);
//                finish();
            }
        });
//        mAdapter.addAll(list);
        query.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
//                mAdapter.clear();
//                mAdapter.addAll(filter(mList, s.toString()));
            }
        });

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                query.getText().clear();
                finish();
            }
        });
    }

    private void initView() {
        recyclerView = $(R.id.recyclerView);
        btnClear = $(R.id.search_clear);
        query = $(R.id.query);
        query.setHint("搜索");
    }

    private List<NoticeEntity> filter(List<NoticeEntity> models, String query) {
        query = query.toLowerCase();
        final List<NoticeEntity> filteredModelList = new ArrayList<>();
        for (NoticeEntity model : models) {
            String text = model.contentDetail;
            if (TextUtils.isEmpty(text)) {
                text = model.contentSummary;
            }
            if (TextUtils.isEmpty(text)) {
                continue;
            }
            text = text.toLowerCase();
            if (text.startsWith(query)) {
                filteredModelList.add(model);
            } else if (text.contains(query)) {
                filteredModelList.add(model);
            }
        }
        return filteredModelList;
    }


}
