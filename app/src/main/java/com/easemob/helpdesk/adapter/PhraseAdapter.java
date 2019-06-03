package com.easemob.helpdesk.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.entity.PhraseItem;
import com.easemob.helpdesk.utils.CommonUtils;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.entity.HDPhrase;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by liyuzhao on 16/3/16.
 */
public class PhraseAdapter extends RecyclerView.Adapter<PhraseAdapter.ViewHolder> implements Filterable {

    private List<PhraseItem> phraseList = new ArrayList<>();
    private List<PhraseItem> childPhraseList = new ArrayList<>();
    private List<PhraseItem> allPhraseList = new ArrayList<>();
    private Context context;
    private OnItemClickListener listener;
    private PhraseFilter filter;

    public PhraseAdapter(Context context) {
        this.context = context;
    }

    @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RelativeLayout.LayoutParams layoutParams =
                new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, CommonUtils.convertDip2Px(context, 44));
        RelativeLayout layout = new RelativeLayout(context);
        layout.setLayoutParams(layoutParams);

        RelativeLayout.LayoutParams tvParams =
                new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        TextView textView = new TextView(context);
        textView.setTextSize(16);
        tvParams.addRule(RelativeLayout.CENTER_VERTICAL);
        tvParams.leftMargin = CommonUtils.convertDip2Px(context, 24);
        textView.setLayoutParams(tvParams);
        layout.addView(textView);

        RelativeLayout.LayoutParams ivParams =
                new RelativeLayout.LayoutParams(CommonUtils.convertDip2Px(context, 40), CommonUtils.convertDip2Px(context, 40));
        ivParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        ivParams.addRule(RelativeLayout.CENTER_VERTICAL);
        ImageView imageView = new ImageView(context);
        imageView.setImageResource(R.drawable.arrow_status_icon_up);
        imageView.setLayoutParams(ivParams);
        layout.addView(imageView);
        return new ViewHolder(layout);
    }

    @Override public void onBindViewHolder(ViewHolder holder, final int position) {

        final PhraseItem phrase = getItem(position);
        holder.textView.setText(phrase.getPhrase().phrase);
        final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.textView.getLayoutParams();

        if (phrase.getLevel() == 1) {
            holder.imageView.setVisibility(View.VISIBLE);
            params.leftMargin = CommonUtils.convertDip2Px(context, 24);
        } else if (phrase.getLevel() == 2) {
            holder.imageView.setVisibility(View.GONE);
            params.leftMargin = CommonUtils.convertDip2Px(context, 40);
        } else {
            holder.imageView.setVisibility(View.GONE);
            params.leftMargin = CommonUtils.convertDip2Px(context, 60);
        }
        if (phrase.getPhrase().hasChildren && phrase.getLevel() == 2) {
            holder.textView.setGravity(Gravity.CENTER_VERTICAL);
            holder.textView.setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(R.drawable.arrow_status_black_right), null,
                    null, null);
        }
        if (phrase.isExpand()) {
            holder.imageView.setImageResource(R.drawable.arrow_status_icon_down);
        } else {
            holder.imageView.setImageResource(R.drawable.arrow_status_icon_up);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                //if (listener != null) {
                //    listener.onItemClick(position);
                //}
                if (phrase.getPhrase().hasChildren || phrase.getLevel() == 1) {
                    setExpand(!phrase.isExpand(), position, phrase);
                } else {
                    Intent intent = new Intent();
                    intent.putExtra("content", phrase.getPhrase().phrase);
                    ((Activity) context).setResult(Activity.RESULT_OK, intent);
                    ((Activity) context).finish();
                }
            }
        });
    }

    @Override public int getItemCount() {
        return phraseList.size();
    }

    public PhraseItem getItem(int position) {
        return phraseList.get(position);
    }

    @Override public Filter getFilter() {
        if (filter == null) {
            filter = new PhraseFilter();
        }
        return filter;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) ((RelativeLayout) itemView).getChildAt(0);
            imageView = (ImageView) ((RelativeLayout) itemView).getChildAt(1);
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public List<PhraseItem> createPhraseItemList(List<HDPhrase> phraseList) {
        this.phraseList.clear();
        for (HDPhrase phrase : phraseList) {
            PhraseItem item = new PhraseItem();
            item.setPhrase(phrase);
            item.setExpand(false);
            item.setLevel(1);
            this.phraseList.add(item);
        }
        return this.phraseList;
    }

    private List<PhraseItem> createChildPhraseItemList(List<HDPhrase> phraseList, int level, long sameId) {
        for (HDPhrase phrase : phraseList) {
            PhraseItem item = new PhraseItem();
            item.setPhrase(phrase);
            item.setExpand(false);
            item.setLevel(level);
            item.setBelongAncestor(sameId);
            this.childPhraseList.add(item);
        }
        return this.childPhraseList;
    }

    /**
     * 设置为传入
     */
    public final void setExpand(boolean isExpand, int position, PhraseItem phraseItem) {
        if (isExpand) {
            childPhraseList.clear();
            List<HDPhrase> list = HDClient.getInstance().phraseManager().getPhrasesByParentId(phraseItem.getPhrase().id);
            if (phraseItem.getLevel() == 1) {
                createChildPhraseItemList(list, 2, phraseItem.getPhrase().id);
            } else if (phraseItem.getLevel() == 2) {
                createChildPhraseItemList(list, 3, phraseItem.getBelongAncestor());
            }
            onExpand(position, phraseItem);
        } else {
            onCollapse(phraseItem);
        }
    }

    /**
     * 展开
     */
    protected void onExpand(int itemPosition, PhraseItem phraseItem) {
        phraseItem.setExpand(true);
        phraseList.addAll(itemPosition + 1, childPhraseList);
        notifyDataSetChanged();
    }

    /**
     * 折叠
     */
    private void onCollapse(PhraseItem phraseItem) {

        phraseItem.setExpand(false);
        childPhraseList.clear();
        for (PhraseItem item : phraseList) {
            if (phraseItem.getLevel() == 1) {
                if (item.getBelongAncestor() == phraseItem.getPhrase().id) {
                    childPhraseList.add(item);
                }
            } else {
                if (item.getPhrase().parentId == phraseItem.getPhrase().id) {
                    childPhraseList.add(item);
                }
            }
        }
        phraseList.removeAll(childPhraseList);
        notifyDataSetChanged();
    }

    private class PhraseFilter extends Filter {

        @Override protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            if (constraint == null || constraint.length() == 0) {
                List<PhraseItem> list = new ArrayList<>(phraseList);
                results.values = list;
                results.count = list.size();
            } else {
                String content = constraint.toString().toLowerCase();
                List<PhraseItem> values = new ArrayList<>(allPhraseList);
                List<PhraseItem> newValues = new ArrayList<>();

                for (int i = 0; i < values.size(); i++) {
                    if (values.get(i).getPhrase().phrase.contains(content)) {
                        newValues.add(values.get(i));
                    }
                }
                results.values = newValues;
                results.count = newValues.size();
            }
            return results;
        }

        @Override protected void publishResults(CharSequence constraint, FilterResults results) {
            childPhraseList.clear();
            childPhraseList.addAll(phraseList);
            phraseList.clear();
            phraseList.addAll((List<PhraseItem>) results.values);
            notifyDataSetChanged();
        }
    }
}
