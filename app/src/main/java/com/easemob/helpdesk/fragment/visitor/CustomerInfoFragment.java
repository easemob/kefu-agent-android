package com.easemob.helpdesk.fragment.visitor;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bigkoo.pickerview.OptionsPickerView;
import com.bigkoo.pickerview.TimePickerView;
import com.bigkoo.pickerview.view.BasePickerView;
import com.easemob.helpdesk.HDApplication;
import com.easemob.helpdesk.R;
import com.hyphenate.kefusdk.gsonmodel.customer.CustomerInfoResponse;
import com.easemob.helpdesk.utils.CommonUtils;
import com.easemob.helpdesk.utils.DialogUtils;
import com.google.gson.Gson;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.manager.VisitorManager;
import com.hyphenate.kefusdk.utils.HDLog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


/**
 * Created by liyuzhao on 14/02/2017.
 */

public class CustomerInfoFragment extends Fragment{

    private static final String TAG = "CustomerInfoFragment";
    private String visitorId;
    private long tenantId;

    public LinearLayout llContainer;

    private CustomerInfoResponse customerInfoResponse;

    private Dialog dialog;
    private BasePickerView pickerView;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customer_info, container, false);
        return view;
    }

    private void initListener(){
        Bundle bundle = getArguments();
        if (bundle == null){
            getActivity().finish();
            return;
        }
        visitorId = bundle.getString("userId");
        tenantId = bundle.getLong("tenantId");

    }





    private void drawAllComponent(){
        if (customerInfoResponse == null){
            return;
        }
        List<CustomerInfoResponse.EntityBean.ColumnValuesBean> columnValues = customerInfoResponse.getEntity().getColumnValues();
        llContainer.removeAllViews();
        for (final CustomerInfoResponse.EntityBean.ColumnValuesBean bean : columnValues){
            final CustomerInfoResponse.EntityBean.ColumnValuesBean.ColumnTypeBean columnType = bean.getColumnType();
//            if (columnType.getTypeName().equals("TEXT_STRING")){
                if (!bean.isVisible()){
                    continue;
                }

                LinearLayout itemLayout = new LinearLayout(getContext());
                itemLayout.setOrientation(LinearLayout.HORIZONTAL);

                TextView leftTv = new TextView(getContext());
                leftTv.setGravity(Gravity.CENTER_VERTICAL);
                leftTv.setText(bean.getDisplayName());
                leftTv.setTextColor(getResources().getColor(R.color.text_color_4c4c4c));
                leftTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

                final TextView rightTv = new TextView(getContext());
                rightTv.setGravity(Gravity.CENTER_VERTICAL);
                rightTv.setTextColor(getResources().getColor(R.color.text_color_4c4c4c));
                rightTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                if (bean.getColumnDescribe() != null){
                    rightTv.setHint(bean.getColumnDescribe());
                }

               String valueString = null;
                if (bean.getValues() != null && !bean.getValues().isEmpty()){
                    valueString = bean.getValues().get(0);
                }


            if (!bean.isReadonly()) {
                final String defaultTxt = valueString;
                itemLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String columnTypeName = columnType.getTypeName();
                        if (columnTypeName.equals("TEXT_STRING")) {
                            final EditText et = new EditText(getContext());
                            et.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, CommonUtils.convertDip2Px(getContext(), 68)));
                            et.setLines(1);
                            et.setEllipsize(TextUtils.TruncateAt.END);
                            et.setSingleLine(true);
                            if (!TextUtils.isEmpty(defaultTxt)){
                                et.setText(defaultTxt);
                            }
                            if (!TextUtils.isEmpty(bean.getColumnDescribe())){
                                et.setHint(bean.getColumnDescribe());
                            }
                            et.setSelection(et.getText().length());

                            new AlertDialog.Builder(getContext()).setTitle("更新" + bean.getDisplayName())
                                    .setView(et)
                                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            updateColumnValue(rightTv, bean.getColumnName(), et.getText().toString(), customerInfoResponse.getEntity().getCustomerId());
                                        }
                                    })
                                    .setNegativeButton("取消", null).show();
                        } else if (columnTypeName.equals("SELECT_STRING")) {
                            dismissDialog();
                            final ArrayList<String> list = new ArrayList<String>();
                            List<String> datas = bean.getOptions();
                            if (datas != null && !datas.isEmpty()){
                                list.addAll(datas);
                            }
                            pickerView = new OptionsPickerView<String>(getContext());
                            ((OptionsPickerView)pickerView).setPicker(list);
                            pickerView.setCancelable(true);
                            ((OptionsPickerView)pickerView).setTitle("请选择");
                            ((OptionsPickerView)pickerView).setCyclic(false);
                            ((OptionsPickerView)pickerView).setOnoptionsSelectListener(new OptionsPickerView.OnOptionsSelectListener() {
                                @Override
                                public void onOptionsSelect(int options1, int option2, int options3) {
                                    if (!list.isEmpty()){
                                        String value = list.get(options1);
                                        updateColumnValue(rightTv, bean.getColumnName(), value, customerInfoResponse.getEntity().getCustomerId());
                                    }
                                }
                            });
                            pickerView.show();


                        } else if (columnTypeName.equals("DATE")){
                            dismissDialog();
                            pickerView = new TimePickerView(getContext(), TimePickerView.Type.YEAR_MONTH_DAY);
                            ((TimePickerView) pickerView).setCyclic(false);
                            pickerView.setCancelable(true);
                            if (!TextUtils.isEmpty(defaultTxt)){
                                ((TimePickerView) pickerView).setTime(new Date(Long.parseLong(defaultTxt)));
                            }else{
                                ((TimePickerView) pickerView).setTime(new Date());
                            }

                            ((TimePickerView) pickerView).setOnTimeSelectListener(new TimePickerView.OnTimeSelectListener() {
                                @Override
                                public void onTimeSelect(Date date) {
                                    updateColumnValue(rightTv, bean.getColumnName(), date.getTime(), customerInfoResponse.getEntity().getCustomerId(), true);
                                }
                            });
                            pickerView.show();


                        }else if (columnTypeName.equals("TEXTAREA_STRING")){
                            final EditText et = new EditText(getContext());
                            et.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, CommonUtils.convertDip2Px(getContext(), 200)));
                            et.setLines(3);
                            if (!TextUtils.isEmpty(defaultTxt)){
                                et.setText(defaultTxt);
                            }
                            if (!TextUtils.isEmpty(bean.getColumnDescribe())){
                                et.setHint(bean.getColumnDescribe());
                            }
                            et.setSelection(et.getText().length());
                            new AlertDialog.Builder(getContext()).setTitle("更新" + bean.getDisplayName())
                                    .setView(et)
                                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            updateColumnValue(rightTv, bean.getColumnName(), et.getText().toString(), customerInfoResponse.getEntity().getCustomerId());
                                        }
                                    })
                                    .setNegativeButton("取消", null).show();

                        }


                    }
                });
            }

            if (!TextUtils.isEmpty(valueString)) {
                if (columnType.getTypeName().equals("DATE")) {
                    try {
                        long millonSecond = Long.parseLong(valueString);
                        String dateValueStr = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(new Date(millonSecond));
                        rightTv.setText(dateValueStr);
                    } catch (Exception e) {
                        rightTv.setText(valueString);
                    }
                } else {
                    rightTv.setText(valueString);
                }
            }

                itemLayout.addView(leftTv, CommonUtils.convertDip2Px(getContext(), 90), ViewGroup.LayoutParams.MATCH_PARENT);
                itemLayout.addView(rightTv, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);

                llContainer.addView(itemLayout, ViewGroup.LayoutParams.MATCH_PARENT, CommonUtils.convertDip2Px(getContext(), 44));
                View lineView = new View(getContext());
                lineView.setBackgroundColor(getResources().getColor(R.color.line_color_2));
                llContainer.addView(lineView, ViewGroup.LayoutParams.MATCH_PARENT, CommonUtils.convertDip2Px(getContext(), 0.5f));

//            }
        }
    }

    public String getStringNotNull(String content){
        if (content == null){
            return "";
        }
        return content;
    }


    private void asyncCustomerDetail(){
        VisitorManager.getInstance().getCustomerDetailInfo(String.valueOf(tenantId), visitorId, new HDDataCallBack<String>() {
            @Override
            public void onSuccess(String value) {
                HDLog.d(TAG, "getCustomerDetailInfo:" + value + ", visitorId:" + visitorId);
                if (TextUtils.isEmpty(value)){
                    return;
                }
                Gson gson = new Gson();
                customerInfoResponse = gson.fromJson(value, CustomerInfoResponse.class);
                if (getActivity() == null){
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        drawAllComponent();
                    }
                });

            }

            @Override
            public void onError(int error, final String errorMsg) {
                if (getActivity() == null){
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), "" + errorMsg, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onAuthenticationException() {

            }
        });


    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        llContainer = (LinearLayout) getView().findViewById(R.id.ll_container);
        initListener();
        if (visitorId == null){
            return;
        }
        asyncCustomerDetail();



    }

    private void dismissDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        if (pickerView != null && pickerView.isShowing()) {
            pickerView.dismiss();
        }
    }

    private void updateColumnValue(final TextView valueTv, String columnName, final Object updateValue, String customerId){
        updateColumnValue(valueTv, columnName, updateValue, customerId, false);
    }

    private void updateColumnValue(final TextView valueTv, String columnName, final Object updateValue, String customerId, final boolean isDate){
        dismissDialog();
        dialog = DialogUtils.getLoadingDialog(getContext(), "信息更新中...");

         //putCustomerDetailInfo
        VisitorManager.getInstance().putCustomerDetailInfo(String.valueOf(tenantId), customerId, columnName, updateValue, new HDDataCallBack<String>() {
            @Override
            public void onSuccess(String value) {
                if (getActivity() == null){
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismissDialog();
                        Toast.makeText(getContext(), "信息更新成功！", Toast.LENGTH_SHORT).show();
                        if (isDate){
                            if (updateValue instanceof Long){
                                long millonSecond = (long) updateValue;
                                String dateValueStr = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(new Date(millonSecond));
                                valueTv.setText(String.valueOf(dateValueStr));
                            }else{
                                valueTv.setText(String.valueOf(updateValue));
                            }

                        }else{
                            valueTv.setText(String.valueOf(updateValue));
                        }
                        asyncCustomerDetail();
                    }
                });

            }

            @Override
            public void onError(int error, String errorMsg) {
                if (getActivity() == null){
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismissDialog();
                        Toast.makeText(getContext(), "信息更新失败！", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onAuthenticationException() {
                if (getActivity() == null){
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismissDialog();
                        HDApplication.getInstance().logout();
                    }
                });
            }
        });


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dismissDialog();
    }

}
