package com.easemob.helpdesk.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.main.TicketDetailActivity;
import com.hyphenate.kefusdk.entity.FileEntity;

import java.util.List;


/**
 * Created by liyuzhao on 16/8/19.
 */
public class CommentFileAdapter extends RecyclerView.Adapter<CommentFileAdapter.MyViewHolder> {

    private List<FileEntity> mList;
    private Context mContext;
    private LayoutInflater layoutInflater;

    public CommentFileAdapter(Context context, List<FileEntity> list){
        this.mList = list;
        this.mContext = context;
        layoutInflater = LayoutInflater.from(context);
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View captureView = layoutInflater.inflate(R.layout.em_comment_file_with_delete, parent, false);
        return new MyViewHolder(captureView);
    }

    @SuppressLint("RecyclerView")
    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        if (mList == null || mList.isEmpty() || position > mList.size()){
            return;
        }
        String data = mList.get(position).name;
        holder.tvFileName.setText(data);
        holder.ivDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((TicketDetailActivity)mContext).delFileClick(v, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder{
        TextView tvFileName;
        ImageView ivDel;
        public MyViewHolder(View itemView) {
            super(itemView);
            tvFileName = (TextView) itemView.findViewById(R.id.tv_file_name);
            ivDel = (ImageView) itemView.findViewById(R.id.delete);
        }
    }


}
