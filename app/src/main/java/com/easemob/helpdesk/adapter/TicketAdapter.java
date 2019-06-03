package com.easemob.helpdesk.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.easemob.helpdesk.R;
import com.hyphenate.kefusdk.gsonmodel.ticket.LeaveMessageResponse;
import com.hyphenate.kefusdk.utils.ISO8601DateFormat;
import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Created by liyuzhao on 16/8/17.
 */
public class TicketAdapter extends RecyclerArrayAdapter<LeaveMessageResponse.EntitiesBean> {

    private List checkRecordList = Collections.synchronizedList(new ArrayList());
    private int selectedCount;
    private CallBack callBack;
    private boolean isSelectionMode;

    public void selectAllItem() {
        for (int i = 0; i < checkRecordList.size(); i++) {
            checkRecordList.set(i, true);
        }
        selectedCount = checkRecordList.size();
        notifyDataSetChanged();
    }

    public void unselectAllItem() {
        for (int i = 0; i < checkRecordList.size(); i++) {
            checkRecordList.set(i, false);
        }
        selectedCount = 0;
        notifyDataSetChanged();
    }

    @Override public void clear() {
        super.clear();
        checkRecordList.clear();
        selectedCount = 0;
    }

    public void setIsSelectionMode(boolean selectionMode) {
        isSelectionMode = selectionMode;
    }

    public void setOnCheckBoxClick(CallBack cb) {
        callBack = cb;
    }

    public int getSelectedCount() {
        return selectedCount;
    }

    public List getCheckRecordList() {
        return checkRecordList;
    }

    public TicketAdapter(Context context) {
        super(context);
        checkRecordList.clear();
        selectedCount = 0;
        isSelectionMode = false;
    }

    @Override public BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {
        return new TicketHolder(parent);
    }

    private class TicketHolder extends BaseViewHolder<LeaveMessageResponse.EntitiesBean> {

        CheckBox checkBox;
        TextView tvNotice;
        TextView tvSubject;
        TextView tvTimeStamp;
        TextView tvContent;
        TextView tvStatus;
        TextView tvNickname;

        ISO8601DateFormat dateFormat = new ISO8601DateFormat();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

        TicketHolder(ViewGroup parent) {
            super(parent, R.layout.row_fragment_ticket);
            checkBox = $(R.id.check);
            tvNotice = $(R.id.notice);
            tvTimeStamp = $(R.id.timestamp);
            tvSubject = $(R.id.subjectId);
            tvStatus = $(R.id.status);
            tvNickname = $(R.id.nickname);
            tvContent = $(R.id.content);

            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    if (checkBox.isChecked()) {
                        selectedCount++;
                        checkRecordList.set(getLayoutPosition(), true);
                    } else {
                        selectedCount--;
                        checkRecordList.set(getLayoutPosition(), false);
                    }
                    if (callBack != null) {
                        callBack.callBack();
                    }
                }
            });
        }

        @Override public void setData(LeaveMessageResponse.EntitiesBean data) {
            super.setData(data);
            if (data == null) {
                return;
            }

            tvNotice.setText(data.getSubject());

            try {
                tvTimeStamp.setText(simpleDateFormat.format(dateFormat.parse(data.getCreated_at())));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            tvSubject.setText("No." + data.getId());

            if (data.getStatus() != null) {
                tvStatus.setText(data.getStatus().getName());
            }

            if (data.getCreator() != null) {
                tvNickname.setText(data.getCreator().getName() + ":");
            }

            tvContent.setText(data.getContent());

            if (isSelectionMode) {
                checkBox.setVisibility(View.VISIBLE);
            } else {
                checkBox.setVisibility(View.GONE);
            }

            int itemPosition = getPosition();

            if (itemPosition == checkRecordList.size()) {
                checkRecordList.add(itemPosition, false);
                checkBox.setChecked(false);
                if (callBack != null) {
                    callBack.callBack();
                }
            } else if (itemPosition < checkRecordList.size()) {
                checkBox.setChecked((boolean) checkRecordList.get(itemPosition));
            }
        }
    }

    /** Activity (Fragment) 回调 在check状态改变时调用 **/
    public interface CallBack {
        void callBack();
    }
}
