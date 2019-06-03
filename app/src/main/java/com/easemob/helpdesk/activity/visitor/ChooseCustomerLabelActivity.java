package com.easemob.helpdesk.activity.visitor;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.easemob.helpdesk.widget.flowlayout.FlowLayout;
import com.easemob.helpdesk.widget.flowlayout.TagAdapter;
import com.easemob.helpdesk.widget.flowlayout.TagFlowLayout;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.BaseActivity;
import com.hyphenate.kefusdk.entity.UserTag;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by benson on 2018/4/19.
 */

public class ChooseCustomerLabelActivity extends BaseActivity {

    @BindView(R.id.iv_back) ImageView back;
    @BindView(R.id.tv_save) TextView save;
    public String visitorId;

    private LayoutInflater mInflater;

    private TagFlowLayout mFlowLayout;

    @BindView(R.id.query) public EditText query;
    @BindView(R.id.search_clear) public ImageButton clearSearch;

    private List<UserTag> userTagList = new ArrayList<>();
    private Set<Integer> checkedPosition = new HashSet<>();
    private Map<Long, Boolean> map = new HashMap<>();

    private MyTagAdapter tagAdapter;
    private Intent intent;
    private InputMethodManager inputMethodManager;

    @SuppressWarnings("unchecked") @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_customer_label);
        ButterKnife.bind(this);

        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        visitorId = getIntent().getExtras().getString("visitorId");
        userTagList = (List<UserTag>) getIntent().getSerializableExtra("userTagList");

        mInflater = LayoutInflater.from(this);
        for (int i = 0; i < userTagList.size(); i++) {
            UserTag item = userTagList.get(i);
            if (item.checked) {
                checkedPosition.add(i);
            }
        }
        initView();
        initTags();

        back.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                finish();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (map.keySet().size() > 0) {
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                } else {
                    Toast.makeText(ChooseCustomerLabelActivity.this, "no change", Toast.LENGTH_SHORT).show();
                }
            }
        });

        query.setHint("搜索");

        query.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tagAdapter.getFilter().filter(s);
                if (s.length() > 0) {
                    clearSearch.setVisibility(View.VISIBLE);
                } else {
                    clearSearch.setVisibility(View.INVISIBLE);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
            }
        });
        clearSearch.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                query.getText().clear();
                hideSoftKeyboard();
            }
        });
    }

    void hideSoftKeyboard() {
        if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getCurrentFocus() != null) {
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    private void initTags() {
        tagAdapter.setSelectedList(checkedPosition);
    }

    private void initView() {
        mFlowLayout = findViewById(R.id.id_flowlayout);
        tagAdapter = new MyTagAdapter(userTagList);
        mFlowLayout.setAdapter(tagAdapter);
        mFlowLayout.setOnSelectListener(new TagFlowLayout.OnSelectListener() {
            @Override public void onSelected(Set<Integer> selectPosSet) {
                checkedPosition = selectPosSet;
            }
        });

        mFlowLayout.setOnTagClickListener(new TagFlowLayout.OnTagClickListener() {
            @Override public boolean onTagClick(View view, int position, FlowLayout parent) {
                UserTag userTag = userTagList.get(position);
                map.put(userTag.tagId, checkedPosition.contains(position));
                intent = new Intent();
                intent.putExtra("isSelectedMap", (Serializable) map);
                return false;
            }
        });
    }

    class MyTagAdapter extends TagAdapter<UserTag> implements Filterable {

        MyTagAdapter.ContentFilter filter;

        public MyTagAdapter(List<UserTag> datas) {
            super(datas);
        }

        @Override public View getView(FlowLayout parent, int position, UserTag userTag) {
            TextView tv = (TextView) mInflater.inflate(R.layout.visitor_tag_textview, mFlowLayout, false);
            if (userTag != null) {
                tv.setText(userTag.tagName);
            }
            return tv;
        }

        @Override public Filter getFilter() {
            if (filter == null) {
                filter = new ContentFilter();
            }
            return filter;
        }

        public class ContentFilter extends Filter {

            @Override protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();

                if (constraint == null || constraint.length() == 0) {
                    List<UserTag> list = new ArrayList<>(userTagList);
                    results.values = list;
                    results.count = list.size();
                } else {

                    String content = constraint.toString().toLowerCase();
                    List<UserTag> values = new ArrayList<>(userTagList);
                    List<UserTag> newValues = new ArrayList<>();

                    for (int i = 0; i < values.size(); i++) {
                        if (values.get(i).tagName.contains(content)) {
                            newValues.add(values.get(i));
                        }
                    }
                    results.values = newValues;
                    results.count = newValues.size();
                }
                return results;
            }

            @Override protected void publishResults(CharSequence constraint, FilterResults results) {

                List<UserTag> list = (List<UserTag>) results.values;
                tagAdapter = new MyTagAdapter(list);
                mFlowLayout.setAdapter(tagAdapter);
                checkedPosition.clear();
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).checked) {
                        checkedPosition.add(i);
                    }
                }
                initTags();
            }
        }
    }
}
