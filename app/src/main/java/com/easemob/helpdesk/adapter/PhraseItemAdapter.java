package com.easemob.helpdesk.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.helpdesk.HDApplication;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.chat.AddShortCutActivity;
import com.easemob.helpdesk.activity.chat.PhraseItemActivity;
import com.easemob.helpdesk.utils.DialogUtils;
import com.hyphenate.kefusdk.bean.HDPhrase;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.entity.HDUser;
import com.hyphenate.kefusdk.utils.HDLog;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by liyuzhao on 16/3/17.
 */
public class PhraseItemAdapter extends RecyclerView.Adapter<PhraseItemAdapter.MyViewHolder> implements Filterable {

    private static final String TAG = "PhraseItemAdapter";
    private Context mContext;
    private List<HDPhrase> mList;
    private Filter mFilter;
    private HDPhrase[] entities;
    private Dialog dialog;
    private static final int HANDLER_MESSAGE_REFRESH_LIST = 0;
    private boolean isEditable = false;
    /**
     * 弱引用刷新UI
     */
    private WeakHandler handler;

    public PhraseItemAdapter(Context context, List<HDPhrase> list) {
        this.mList = list;
        this.mContext = context;
        handler = new WeakHandler(this);
    }

    public void setEditable(boolean isEditable){
        this.isEditable = isEditable;
    }

    private static class WeakHandler extends Handler {
        WeakReference<PhraseItemAdapter> weakReference;

        public WeakHandler(PhraseItemAdapter adapter) {
            this.weakReference = new WeakReference<PhraseItemAdapter>(adapter);
        }

        private void refreshList() {
            PhraseItemAdapter adapter = weakReference.get();
            if (null != adapter) {
                adapter.entities = adapter.mList.toArray(new HDPhrase[adapter.mList.size()]);
                adapter.notifyDataSetChanged();
            }
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case HANDLER_MESSAGE_REFRESH_LIST:
                    refreshList();
                    break;
            }
        }
    }


    public void refresh(){
        if(handler.hasMessages(HANDLER_MESSAGE_REFRESH_LIST)){
            return;
        }
        Message msg = handler.obtainMessage(HANDLER_MESSAGE_REFRESH_LIST);
        handler.sendMessage(msg);
    }



    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        @SuppressLint("InflateParams") View convertView = LayoutInflater.from(mContext).inflate(R.layout.row_phrase_item, null);
        return new MyViewHolder(convertView);
    }

    @SuppressLint("RecyclerView")
    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        if(entities == null || position >= entities.length){
            return;
        }
        final HDPhrase entty = entities[position];
        if (entty == null){
            return;
        }
        holder.tvMessage.setText(entty.phrase);
        holder.ivIconRight.setVisibility(entty.leaf ?View.GONE:View.VISIBLE);
        if (isEditable) {
            holder.ivLeft.setVisibility(View.VISIBLE);
        } else {
            holder.ivLeft.setVisibility(View.GONE);
        }

        holder.ivLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HDUser loginUser = HDClient.getInstance().getCurrentUser();
                if (loginUser == null){
                    return;
                }
                dialog = DialogUtils.getLoadingDialog(mContext, "正在删除...");
                dialog.show();
                HDClient.getInstance().phraseManager().deleteCommonPhrase(entty, new HDDataCallBack<String>() {
                    @Override
                    public void onSuccess(String value) {
                        ((Activity) mContext).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                closeDialog();
                                mList.remove(position);
                                notifyItemRemoved(position);
                                refresh();
                            }
                        });
                    }

                    @Override
                    public void onError(int error, String errorMsg) {
                        HDLog.e(TAG, "deleteCommonPhrase-erro:" + error + ";errorMsg:" + errorMsg);
                        ((Activity) mContext).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                closeDialog();
                                Toast.makeText(mContext, "删除失败!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onAuthenticationException() {
                        ((Activity) mContext).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                closeDialog();
                                HDApplication.getInstance().logout();
                            }
                        });
                    }
                });
            }
        });


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEditable) {
                    Intent intent = new Intent();
                    intent.putExtra("phraseId", entty.id);
                    intent.putExtra("leaf", entty.leaf);
                    intent.setClass(mContext, AddShortCutActivity.class);
                    ((Activity) mContext).startActivityForResult(intent, PhraseItemActivity.REQUEST_CODE_UPDATE_SHORTCUT);
                } else {
                    if(entty.leaf){
                        Intent intent = new Intent();
                        intent.putExtra("content", entty.phrase);
                        ((Activity) mContext).setResult(Activity.RESULT_OK, intent);
                        ((Activity) mContext).finish();
                    }else{
                        ((PhraseItemActivity)mContext).setOnItemClick(v, position);
                    }
                }
                ((PhraseItemActivity)mContext).queryClear();
            }
        });


    }

    private void closeDialog(){
        if(dialog != null && dialog.isShowing()){
            dialog.dismiss();
        }
    }


    @Override
    public void onViewDetachedFromWindow(MyViewHolder holder) {
        super.onViewDetachedFromWindow(holder);

        closeDialog();
    }

    public HDPhrase getItem(int position){
        if (position < 0 || entities == null || position > entities.length){
            return null;
        }
        return entities[position];
    }


    @Override
    public int getItemCount() {
        return entities == null ? 0 : entities.length;
    }


    @Override
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new PhraseItemFilter(mList);
        }
        return mFilter;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private ImageView ivLeft;
        private ImageView ivIconRight;
        private TextView tvMessage;

        public MyViewHolder(View itemView) {
            super(itemView);
            ivLeft = (ImageView) itemView.findViewById(R.id.iv_left);
            ivIconRight = (ImageView) itemView.findViewById(R.id.iv_icon_right);
            tvMessage = (TextView) itemView.findViewById(R.id.message);
        }


    }


    public class PhraseItemFilter extends Filter {

        List<HDPhrase> mOriginalValues = null;

        public PhraseItemFilter(List<HDPhrase> list) {
            this.mOriginalValues = list;
        }

        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();
            if (mOriginalValues == null) {
                mOriginalValues = new ArrayList<>();
            }

            if (prefix == null || prefix.length() == 0) {
                results.values = mOriginalValues;
                results.count = mOriginalValues.size();
            } else {
                String prefixString = prefix.toString().toLowerCase();
                final int count = mOriginalValues.size();
                final ArrayList<HDPhrase> newValues = new ArrayList<>();
                for (int i = 0; i < count; i++) {
                    final HDPhrase value = mOriginalValues.get(i);
                    String valueText;
                    valueText = value.phrase;
                    if (valueText.startsWith(prefixString)) {
                        newValues.add(value);
                    } else if (valueText.contains(prefixString)) {
                        newValues.add(value);
                    }
                }
                results.values = newValues;
                results.count = newValues.size();
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mList = (List<HDPhrase>) results.values;
            refresh();
        }
    }


}
