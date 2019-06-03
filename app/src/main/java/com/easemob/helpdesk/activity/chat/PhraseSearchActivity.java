package com.easemob.helpdesk.activity.chat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;

import com.easemob.helpdesk.AppConfig;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.BaseActivity;
import com.easemob.helpdesk.utils.DisplayUtil;
import com.easemob.helpdesk.widget.recyclerview.DividerLine;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.entity.HDPhrase;
import com.jude.easyrecyclerview.EasyRecyclerView;

import java.util.ArrayList;
import java.util.List;

public class PhraseSearchActivity extends BaseActivity {
    private static final String TAG = PhraseSearchActivity.class.getSimpleName();

    private EasyRecyclerView recyclerView;

    private List<HDPhrase> phraseList = new ArrayList<>();

    private EditText query;
    public ImageButton clearSearch;
    private PhraseSearchAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppConfig.setFitWindowMode(this);
        setContentView(R.layout.activity_phrase_search);
        initView();
        initListener();
        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        //设置
        DividerLine dividerLine = new DividerLine(DividerLine.VERTICAL);
        dividerLine.setSize(1);
        dividerLine.setColor(0xFFDDDDDD);
        recyclerView.addItemDecoration(dividerLine);
        recyclerView.setAdapterWithProgress(mAdapter = new PhraseSearchAdapter(this, phraseList));
        loadDatas();
        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(HDPhrase phrase) {
                Intent intent = new Intent();
                intent.putExtra("content", phrase.phrase);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
    }

    private void loadDatas(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<HDPhrase> rootList = HDClient.getInstance().phraseManager().getPhrashsLikeKey(null);
                phraseList.addAll(rootList);
                mAdapter.setDatas(phraseList);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();
    }

    private void initView() {
        recyclerView = findViewById(R.id.recyclerView);
        query = findViewById(R.id.query);
        clearSearch = findViewById(R.id.search_clear);
        query.setHint("搜索");
        query.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mAdapter.getFilter().filter(s);
                if (s.length() > 0) {
                    clearSearch.setVisibility(View.VISIBLE);
                } else {
                    clearSearch.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        clearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                query.getText().clear();
                hideKeyboard();
            }
        });
    }

    private void initListener() {
        findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PhraseSearchActivity.this.finish();
            }
        });
    }


    class PhraseSearchAdapter extends RecyclerView.Adapter<PhraseSearchAdapter.ViewHolder> implements Filterable {

        private List<HDPhrase> allPhraseList = new ArrayList<>();
        private List<HDPhrase> showPhraseList = new ArrayList<>();
        private PhraseSearchFilter filter;
        private OnItemClickListener listener;
        private Context context;

        public PhraseSearchAdapter(Context context, List<HDPhrase> phrases) {
            this.context = context;
            allPhraseList.addAll(phrases);
            showPhraseList.addAll(phrases);
        }

        public void setDatas(List<HDPhrase> phrases) {
            this.allPhraseList.clear();
            allPhraseList.addAll(phrases);
            showPhraseList.clear();
            showPhraseList.addAll(phrases);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public PhraseSearchAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
            TextView textView = new TextView(context);
            int leftPadding = DisplayUtil.dip2px(context, 15);
            int topPadding = DisplayUtil.dip2px(context, 10);
            textView.setPadding(leftPadding, topPadding, leftPadding, topPadding);
            textView.setTextSize(16);
            return new ViewHolder(textView);
        }

        @Override
        public void onBindViewHolder(@NonNull PhraseSearchAdapter.ViewHolder viewHolder, int position) {
            final HDPhrase phrase = showPhraseList.get(position);
            if (viewHolder.itemView instanceof TextView) {
                TextView textView = (TextView) viewHolder.itemView;
                textView.setText(phrase.phrase);
            }
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onItemClick(phrase);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return showPhraseList.size();
        }

        @Override
        public Filter getFilter() {
            if (filter == null) {
                filter = new PhraseSearchFilter();
            }
            return filter;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            public ViewHolder(View itemView) {
                super(itemView);
            }
        }

        public void setOnItemClickListener(OnItemClickListener listener) {
            this.listener = listener;
        }


        class PhraseSearchFilter extends Filter {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                if (constraint == null || constraint.length() == 0) {
                    results.values = allPhraseList;
                    results.count = allPhraseList.size();
                } else {
                    String content = constraint.toString().toLowerCase();
                    List<HDPhrase> newPhrases = new ArrayList<>();
                    for (HDPhrase phrase : allPhraseList) {
                        if (phrase.phrase.contains(content)) {
                            newPhrases.add(phrase);
                        }
                    }
                    results.values = newPhrases;
                    results.count = newPhrases.size();
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                showPhraseList.clear();
                showPhraseList.addAll((List<HDPhrase>) results.values);
                notifyDataSetChanged();
            }
        }

    }

    public interface OnItemClickListener {
        void onItemClick(HDPhrase phrase);
    }


}
