package com.easemob.helpdesk.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.easemob.helpdesk.R;
import com.easemob.helpdesk.entity.ShortCutEntity;
import com.jude.easyrecyclerview.adapter.BaseViewHolder;

/**
 * Created by liyuzhao on 16/3/17.
 */
public class PhraseItemHolder extends BaseViewHolder<ShortCutEntity> {

    private Context mContext;
    private ImageView ivLeft;
    private ImageView ivIconRight;
    private TextView tvMessage;

    public PhraseItemHolder(Context context, ViewGroup parent) {
        super(parent, R.layout.row_phrase_item);
        this.mContext = context;
        ivLeft = $(R.id.iv_left);
        ivIconRight = $(R.id.iv_icon_right);
        tvMessage = $(R.id.message);
    }


    @Override
    public void setData(final ShortCutEntity data) {
        super.setData(data);
        tvMessage.setText(data.message);
        ivIconRight.setVisibility(View.GONE);
        if (data.isEditable) {
            ivLeft.setVisibility(View.VISIBLE);
        } else {
            ivLeft.setVisibility(View.GONE);
        }



    }




}
