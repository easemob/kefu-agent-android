package com.easemob.helpdesk.widget.pickerview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.bigkoo.pickerview.adapter.ArrayWheelAdapter;
import com.bigkoo.pickerview.lib.WheelView;
import com.bigkoo.pickerview.view.BasePickerView;
import com.easemob.helpdesk.R;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by liyuzhao on 16/3/11.
 */
public class SimplePickerView extends BasePickerView implements View.OnClickListener{

    private Context mContext;
    private View btnSave;
    private WheelView wv;



    public SimplePickerView(Context context, String[] valueArray){
        super(context);
        this.mContext = context;
        LayoutInflater.from(context).inflate(R.layout.popup_picker_online, contentContainer);
        btnSave = findViewById(R.id.tv_save);
        btnSave.setOnClickListener(this);
        wv = (WheelView) findViewById(R.id.wheelview);
        ArrayList<String> list = new ArrayList<>();
        list.addAll(Arrays.asList(valueArray));
        wv.setCyclic(false);
        wv.setTextSize(20);
        wv.setAdapter(new ArrayWheelAdapter(list));
    }

    public SimplePickerView(Context context, ArrayList<String> list){
        super(context);
        this.mContext = context;
        LayoutInflater.from(context).inflate(R.layout.popup_picker_online, contentContainer);
        btnSave = findViewById(R.id.tv_save);
        btnSave.setOnClickListener(this);
        wv = (WheelView) findViewById(R.id.wheelview);
        wv.setCyclic(false);
        wv.setAdapter(new ArrayWheelAdapter(list));
    }

    public SimplePickerView(Context context,ArrayWheelAdapter  adapter){
        super(context);
        this.mContext = context;
        LayoutInflater.from(context).inflate(R.layout.popup_picker_online, contentContainer);
        btnSave = findViewById(R.id.tv_save);
        btnSave.setOnClickListener(this);
        wv = (WheelView) findViewById(R.id.wheelview);
        wv.setCyclic(false);
        wv.setAdapter(adapter);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.tv_save:
                if (mContext instanceof SimplePickSelectItemListener){
                    ((SimplePickSelectItemListener)mContext).simplePickerSelect(wv.getCurrentItem());
                }
//
//                if(mContext instanceof ScreeningActivity){
//                    ((ScreeningActivity)mContext).simplePickerSelect(wv.getCurrentItem());
//                }else if (mContext instanceof HomeFilterActivity){
//                    ((HomeFilterActivity)mContext).simplePickerSelect(wv.getCurrentItem());
//                }else if (mContext instanceof VisitorsFilterActivity){
//                    ((VisitorsFilterActivity)mContext).simplePickerSelect(wv.getCurrentItem());
//                }else if (mContext instanceof WorkloadFilter){
//                    ((WorkloadFilter)mContext).simplePickerSelect(wv.getCurrentItem());
//                }else if (mContext instanceof HistoryFilter){
//                    ((HistoryFilter)mContext).simplePickerSelect(wv.getCurrentItem());
//                }else if (mContext instanceof TicketDetailActivity){
//                    ((TicketDetailActivity)mContext).simplePickerSelect(wv.getCurrentItem());
//                }
                dismiss();
                break;
        }
    }


    public interface SimplePickSelectItemListener{
        void simplePickerSelect(int position);
    }


}
