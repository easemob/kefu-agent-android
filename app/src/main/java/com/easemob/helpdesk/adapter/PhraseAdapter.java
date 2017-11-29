package com.easemob.helpdesk.adapter;

import android.content.Context;
import android.view.ViewGroup;

import com.hyphenate.kefusdk.entity.HDPhrase;
import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;

/**
 * Created by liyuzhao on 16/3/16.
 */
public class PhraseAdapter extends RecyclerArrayAdapter<HDPhrase> {

    public PhraseAdapter(Context context) {
        super(context);
    }

    @Override
    public BaseViewHolder OnCreateViewHolder(ViewGroup viewGroup, int i) {
        return new PhraseHolder(viewGroup);
    }
}
