package com.easemob.helpdesk.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.easemob.helpdesk.R;
import com.hyphenate.kefusdk.bean.HDPhrase;
import com.jude.easyrecyclerview.adapter.BaseViewHolder;

/**
 * Created by liyuzhao on 16/3/16.
 */
public class PhraseHolder extends BaseViewHolder<HDPhrase> {
    private TextView tvMessage;
    private ImageView ivRight;

    public PhraseHolder(ViewGroup parent) {
        super(parent, R.layout.row_phrase_item);
        tvMessage = $(R.id.message);
        ivRight = $(R.id.iv_icon_right);
    }

    @Override
    public void setData(HDPhrase data) {
        super.setData(data);
        tvMessage.setText(data.phrase);
        ivRight.setVisibility(data.leaf ? View.GONE : View.VISIBLE);

    }
}
