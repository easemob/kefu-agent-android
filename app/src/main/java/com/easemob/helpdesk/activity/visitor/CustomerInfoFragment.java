package com.easemob.helpdesk.activity.visitor;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.DigitsKeyListener;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bigkoo.pickerview.OptionsPickerView;
import com.bigkoo.pickerview.TimePickerView;
import com.bigkoo.pickerview.view.BasePickerView;
import com.easemob.helpdesk.widget.flowlayout.FlowLayout;
import com.easemob.helpdesk.widget.flowlayout.TagAdapter;
import com.easemob.helpdesk.widget.flowlayout.TagFlowLayout;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.ModifyActivity;
import com.easemob.helpdesk.utils.CommonUtils;
import com.easemob.helpdesk.utils.DialogUtils;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.entity.UserTag;
import com.hyphenate.kefusdk.gsonmodel.customer.CustomerInfoResponse;
import com.hyphenate.kefusdk.utils.HDLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Created by liyuzhao on 14/02/2017.
 */

public class CustomerInfoFragment extends Fragment {

    private static final String TAG = "CustomerInfoFragment";
    private String visitorId;
    private long tenantId;

    public LinearLayout llContainer;

    public RelativeLayout rlIFrame;

    public TextView mIframeTitle;


    private CustomerInfoResponse.EntityBean customerInfo;

    private Dialog dialog;
    private BasePickerView pickerView;
    private static final int REQUEST_CODE_CASCADE_SELECT = 0x01;
    private TextView selectedView;
    private String columnName;
    private TextView rightTv;
    private ArrayList<UserTag> userTagList = new ArrayList<>();
    private ArrayList<UserTag> selectedUserTagList = new ArrayList<>();
    private ArrayList<Long> selectedUserTagIdList = new ArrayList<>();
    private Set<Integer> checkedPosition = new HashSet<>();
    private MyTagAdapter tagAdapter;
    private TagFlowLayout tagFlowLayout;
    private TextView labelNum;
    private String customInfoString;

    @Nullable @Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customer_info, container, false);
        return view;
    }

    private void initListener() {
        Bundle bundle = getArguments();
        if (bundle == null) {
            getActivity().finish();
            return;
        }
        visitorId = bundle.getString("userId");
        tenantId = bundle.getLong("tenantId");
    }

    private void drawAllComponent() {
        if (customerInfo == null || customerInfo.getColumnValues() == null || customerInfo.getColumnValues().size() <= 0) {
            return;
        }
        llContainer.removeAllViews();
        for (final CustomerInfoResponse.EntityBean.ColumnValuesBean bean : customerInfo.getColumnValues()) {
            final CustomerInfoResponse.EntityBean.ColumnValuesBean.ColumnTypeBean columnType = bean.getColumnType();
            //            if (columnType.getTypeName().equals("TEXT_STRING")){
            if (!bean.isVisible()) {
                continue;
            }

            RelativeLayout itemLayout = new RelativeLayout(getContext());

            RelativeLayout.LayoutParams tvLayoutParams =
                    new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            TextView leftTv = new TextView(getContext());
            tvLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
            leftTv.setText(bean.getDisplayName());
            leftTv.setTextColor(getResources().getColor(R.color.text_color_4c4c4c));
            leftTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            leftTv.setLayoutParams(tvLayoutParams);
            leftTv.setId(0x01);

            RelativeLayout.LayoutParams arrowLayoutParams =
                    new RelativeLayout.LayoutParams(CommonUtils.convertDip2Px(getContext(), 40), CommonUtils.convertDip2Px(getContext(), 40));
            arrowLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
            arrowLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
            ImageView imageView = new ImageView(getContext());
            imageView.setImageResource(R.drawable.arrow_status_black_right);
            imageView.setLayoutParams(arrowLayoutParams);
            imageView.setId(0x02);

            RelativeLayout.LayoutParams rightlayoutParams =
                    new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            rightlayoutParams.leftMargin =
                    (int) leftTv.getPaint().measureText(leftTv.getText().toString()) + CommonUtils.convertDip2Px(getContext(), 10);
            rightTv = new TextView(getContext());
            rightlayoutParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
            rightTv.setGravity(Gravity.RIGHT);
            rightTv.setTextColor(getResources().getColor(R.color.text_color_4c4c4c));
            rightTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            rightTv.setId(0x03);

            if (bean.getColumnDescribe() != null) {
                rightTv.setHint(bean.getColumnDescribe());
            }

            String valueString = null;
            if (bean.getValues() != null && !bean.getValues().isEmpty()) {
                valueString = bean.getValues().get(0).toString();
            }

            final String tempValueString = valueString;
            if (!bean.isReadonly()) {
                final String defaultTxt = valueString;

                itemLayout.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) {

                        String columnTypeName = columnType.getTypeName();
                        switch (columnTypeName) {
                            case "TEXT_STRING": {
                                final EditText et = new EditText(getContext());
                                et.setLayoutParams(
                                        new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, CommonUtils.convertDip2Px(getContext(), 68)));
                                et.setLines(1);
                                et.setEllipsize(TextUtils.TruncateAt.END);
                                et.setSingleLine(true);
                                if (!TextUtils.isEmpty(defaultTxt)) {
                                    et.setText(defaultTxt);
                                }
                                if (!TextUtils.isEmpty(bean.getColumnDescribe())) {
                                    et.setHint(bean.getColumnDescribe());
                                }
                                et.setSelection(et.getText().length());
                                et.setFilters(new InputFilter[] { new InputFilter.LengthFilter(256) });

                                columnName = bean.getColumnName();

                                int index = ModifyActivity.PROFILE_MODIFY_NICKNAME;
                                if ("nickname".equals(columnName)) {
                                    index = ModifyActivity.PROFILE_MODIFY_NICKNAME;
                                } else if ("truename".equals(columnName)) {
                                    index = ModifyActivity.PROFILE_MODIFY_TRUENAME;
                                } else if ("phone".equals(columnName)) {
                                    index = ModifyActivity.PROFILE_MODIFY_MOBILE;
                                } else if ("qq".equals(columnName)) {
                                    index = ModifyActivity.MODIFY_QQ;
                                } else if ("weixin".equals(columnName)) {
                                    index = ModifyActivity.MODIFY_WEIXIN;
                                } else if ("email".equals(columnName)) {
                                    index = ModifyActivity.MODIFY_EMAIL;
                                } else if ("companyname".equals(columnName)) {
                                    index = ModifyActivity.MODIFY_COMPANY;
                                }
                                startActivityForResult(new Intent(getActivity(), ModifyActivity.class).putExtra("content", et.getText().toString())
                                        .putExtra("index", index), 1000);
                                break;
                            }
                            case "SELECT_STRING":
                                dismissDialog();
                                final ArrayList<String> list = new ArrayList<String>();
                                List<String> datas = bean.getOptions();
                                if (datas != null && !datas.isEmpty()) {
                                    list.addAll(datas);
                                }
                                pickerView = new OptionsPickerView<String>(getContext());
                                ((OptionsPickerView) pickerView).setPicker(list);
                                pickerView.setCancelable(true);
                                ((OptionsPickerView) pickerView).setTitle("请选择");
                                ((OptionsPickerView) pickerView).setCyclic(false);
                                ((OptionsPickerView) pickerView).setOnoptionsSelectListener(new OptionsPickerView.OnOptionsSelectListener() {
                                    @Override public void onOptionsSelect(int options1, int option2, int options3) {
                                        if (!list.isEmpty()) {
                                            String value = list.get(options1);
                                            updateColumnValue(rightTv, bean.getColumnName(), value, customerInfo.getCustomerId());
                                        }
                                    }
                                });
                                pickerView.show();

                                break;
                            case "DATE":
                                dismissDialog();
                                pickerView = new TimePickerView(getContext(), TimePickerView.Type.YEAR_MONTH_DAY);
                                ((TimePickerView) pickerView).setCyclic(false);
                                pickerView.setCancelable(true);
                                String tempString = null;
                                try {
                                    tempString = new BigDecimal(defaultTxt).setScale(0, BigDecimal.ROUND_UNNECESSARY).toString();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                if (TextUtils.isEmpty(tempString)) {
                                    ((TimePickerView) pickerView).setTime(new Date());
                                } else {
                                    ((TimePickerView) pickerView).setTime(new Date(Long.parseLong(tempString)));
                                }

                                ((TimePickerView) pickerView).setOnTimeSelectListener(new TimePickerView.OnTimeSelectListener() {
                                    @Override public void onTimeSelect(Date date) {
                                        updateColumnValue(rightTv, bean.getColumnName(), date.getTime(), customerInfo.getCustomerId(), true);
                                    }
                                });
                                pickerView.show();

                                break;
                            case "TEXTAREA_STRING": {
                                final EditText et = new EditText(getContext());
                                et.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                        CommonUtils.convertDip2Px(getContext(), 200)));
                                et.setLines(3);
                                et.setFilters(new InputFilter[] { new InputFilter.LengthFilter(1000) });

                                if (!TextUtils.isEmpty(defaultTxt)) {
                                    et.setText(defaultTxt);
                                }
                                if (!TextUtils.isEmpty(bean.getColumnDescribe())) {
                                    et.setHint(bean.getColumnDescribe());
                                }
                                et.setSelection(et.getText().length());

                                columnName = bean.getColumnName();

                                startActivityForResult(new Intent(getActivity(), ModifyActivity.class).putExtra("content", et.getText().toString())
                                        .putExtra("index", ModifyActivity.MODIFY_DESCRIPTION), 1000);

                                //new AlertDialog.Builder(getContext()).setTitle("更新" + bean.getDisplayName())
                                //        .setView(et)
                                //        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                //            @Override public void onClick(DialogInterface dialog, int which) {
                                //                updateColumnValue(rightTv, bean.getColumnName(), et.getText().toString(),
                                //                        customerInfo.getCustomerId());
                                //            }
                                //        })
                                //        .setNegativeButton("取消", null)
                                //        .show();

                                break;
                            }
                            case "TEXT_NUMBER": {
                                final EditText et = new EditText(getContext());
                                et.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                        CommonUtils.convertDip2Px(getContext(), 200)));
                                et.setLines(1);
                                et.setEllipsize(TextUtils.TruncateAt.END);
                                et.setSingleLine(true);
                                et.setInputType(InputType.TYPE_CLASS_NUMBER);
                                et.setFilters(new InputFilter[] { new InputFilter.LengthFilter(16) });
                                if (!TextUtils.isEmpty(defaultTxt)) {
                                    String tempString2 = null;
                                    try {
                                        tempString2 = new BigDecimal(defaultTxt).setScale(0, BigDecimal.ROUND_UNNECESSARY).toString();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    if (TextUtils.isEmpty(tempString2)) {
                                        et.setText(defaultTxt);
                                    } else {
                                        et.setText(tempString2);
                                    }
                                }
                                if (!TextUtils.isEmpty(bean.getColumnDescribe())) {
                                    et.setHint(bean.getColumnDescribe());
                                }
                                et.setSelection(et.getText().length());
                                et.setKeyListener(new DigitsKeyListener());
                                new AlertDialog.Builder(getContext()).setTitle("更新" + bean.getDisplayName())
                                        .setView(et)
                                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                            @Override public void onClick(DialogInterface dialog, int which) {
                                                updateColumnValue(rightTv, bean.getColumnName(), et.getText().toString(),
                                                        customerInfo.getCustomerId());
                                            }
                                        })
                                        .setNegativeButton("取消", null)
                                        .show();
                                break;
                            }
                            case "CASCADE_SELECT_STRING": {
                                //第一层/第二层-2/第三层-2-1/第四层-2-1-1
                                String columnName = bean.getColumnName();
                                selectedView = rightTv;
                                startActivityForResult(new Intent(getContext(), CascadeSelectActivity.class).putExtra("name", columnName)
                                        .putExtra("preValue", tempValueString), REQUEST_CODE_CASCADE_SELECT);
                                break;
                            }
                        }
                    }
                });
            }

            if (!TextUtils.isEmpty(valueString)) {
                switch (columnType.getTypeName()) {
                    case "DATE":
                        try {
                            try {
                                valueString = new BigDecimal(valueString).setScale(0, BigDecimal.ROUND_UNNECESSARY).toString();
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                            long millonSecond = Long.parseLong(valueString);
                            String dateValueStr = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(new Date(millonSecond));
                            rightTv.setText(dateValueStr);
                        } catch (Exception e) {
                            rightTv.setText(valueString);
                        }
                        break;
                    case "TEXT_NUMBER":
                        try {
                            valueString = new BigDecimal(valueString).setScale(0, BigDecimal.ROUND_UNNECESSARY).toString();
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                        rightTv.setText(valueString);
                        break;
                    case "CASCADE_SELECT_STRING":
                        int subStartIndex = valueString.lastIndexOf("value=") + "value=".length();
                        rightTv.setText(valueString.substring(subStartIndex, valueString.length() - 1));
                        break;
                    default:
                        rightTv.setText(valueString);
                        break;
                }
            }

            itemLayout.addView(leftTv);
            if (!bean.isReadonly()) {
                itemLayout.addView(imageView);
                rightlayoutParams.addRule(RelativeLayout.LEFT_OF, imageView.getId());
            } else {
                rightlayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                rightlayoutParams.rightMargin = CommonUtils.convertDip2Px(getContext(), 10);
            }
            itemLayout.addView(rightTv, rightlayoutParams);

            llContainer.addView(itemLayout, ViewGroup.LayoutParams.MATCH_PARENT, CommonUtils.convertDip2Px(getContext(), 62));
            View lineView = new View(getContext());
            lineView.setBackgroundColor(getResources().getColor(R.color.line_color_2));
            llContainer.addView(lineView, ViewGroup.LayoutParams.MATCH_PARENT, CommonUtils.convertDip2Px(getContext(), 0.5f));

            //            }
        }
    }

    private void asyncCustomerDetail() {
        HDClient.getInstance().visitorManager().getCustomerDetailInfo(tenantId, visitorId, new HDDataCallBack<CustomerInfoResponse.EntityBean>() {
            @Override public void onSuccess(CustomerInfoResponse.EntityBean value) {
                customerInfo = value;

                if (getActivity() == null) {
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override public void run() {
                        drawAllComponent();
                    }
                });

                getUserTagsFromRemote();
            }

            @Override public void onError(int error, final String errorMsg) {
                if (getActivity() == null) {
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override public void run() {
                        Toast.makeText(getContext(), "" + errorMsg, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void drawLabelLayout() {
        RelativeLayout labelLayout = new RelativeLayout(getContext());

        RelativeLayout.LayoutParams layoutParams =
                new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        TextView customerLabel = new TextView(getContext());
        customerLabel.setText("客户标签");
        customerLabel.setTextColor(getResources().getColor(R.color.text_color_4c4c4c));
        customerLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        customerLabel.setLayoutParams(layoutParams);
        labelLayout.addView(customerLabel);

        RelativeLayout.LayoutParams arrowLayoutParams =
                new RelativeLayout.LayoutParams(CommonUtils.convertDip2Px(getContext(), 40), CommonUtils.convertDip2Px(getContext(), 40));
        arrowLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        arrowLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
        ImageView arrowImage = new ImageView(getContext());
        arrowImage.setImageResource(R.drawable.arrow_status_black_right);
        arrowImage.setLayoutParams(arrowLayoutParams);
        arrowImage.setId(0x10);
        labelLayout.addView(arrowImage);

        RelativeLayout.LayoutParams labelNumLayoutParams =
                new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        labelNumLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        labelNumLayoutParams.addRule(RelativeLayout.LEFT_OF, arrowImage.getId());
        labelNum = new TextView(getContext());
        labelNum.setText(selectedUserTagList.size() + "");
        labelNum.setTextColor(getResources().getColor(R.color.text_color_4c4c4c));
        labelNum.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        labelNum.setLayoutParams(labelNumLayoutParams);
        labelLayout.addView(labelNum);

        LinearLayout showLabelLayout = new LinearLayout(getContext());

        LinearLayout.LayoutParams tagFlowLayoutParams =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tagFlowLayout = new TagFlowLayout(getContext());
        tagFlowLayout.setLayoutParams(tagFlowLayoutParams);
        showLabelLayout.addView(tagFlowLayout);

        tagAdapter = new MyTagAdapter(selectedUserTagList);
        tagFlowLayout.setAdapter(tagAdapter);
        tagFlowLayout.setClickable(false);

        llContainer.addView(labelLayout, ViewGroup.LayoutParams.MATCH_PARENT, CommonUtils.convertDip2Px(getContext(), 62));
        llContainer.addView(showLabelLayout, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        labelLayout.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                startActivityForResult(new Intent(getActivity(), ChooseCustomerLabelActivity.class).putExtra("visitorId", visitorId)
                        .putExtra("userTagList", userTagList), 2000);
            }
        });
    }

    private void getUserTagsFromRemote() {
        //获取用户标签
        HDClient.getInstance().visitorManager().getUserTag(visitorId, new HDDataCallBack<List<UserTag>>() {
            @Override public void onSuccess(List<UserTag> list) {
                userTagList.clear();
                userTagList.addAll(list);
                for (int i = 0; i < userTagList.size(); i++) {
                    UserTag item = userTagList.get(i);
                    if (item.checked) {
                        selectedUserTagList.add(item);
                        selectedUserTagIdList.add(item.tagId);
                    }
                }

                if (getActivity() == null) {
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override public void run() {
                        drawLabelLayout();
                        initTags();
                    }
                });
            }

            @Override public void onError(int error, String errorMsg) {
            }
        });
    }

    private void initTags() {
        for (int i = 0; i < selectedUserTagList.size(); i++) {
            checkedPosition.add(i);
        }
        tagAdapter.setSelectedList(checkedPosition);
    }

    class MyTagAdapter extends TagAdapter<UserTag> {

        public MyTagAdapter(List<UserTag> datas) {
            super(datas);
        }

        @Override public View getView(FlowLayout parent, int position, UserTag userTag) {
            TextView tv = (TextView) LayoutInflater.from(getActivity()).inflate(R.layout.visitor_tag_textview, tagFlowLayout, false);
            if (userTag != null) {
                tv.setText(userTag.tagName);
            }
            return tv;
        }
    }

    @Override public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        llContainer = (LinearLayout) getView().findViewById(R.id.ll_container);
        rlIFrame = getView().findViewById(R.id.rl_iframe);
        mIframeTitle = getView().findViewById(R.id.tv_iframe_title);
        initListener();
        if (visitorId == null) {
            return;
        }
        asyncCheckIFrame();
        asyncCustomerDetail();
    }

    private void asyncCheckIFrame(){
        final String tabTitle = HDClient.getInstance().visitorManager().getIFrameTabTitle();
        if (tabTitle == null){
            rlIFrame.setVisibility(View.GONE);
        }else{
            mIframeTitle.setText(tabTitle);
        }
        rlIFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String iframeUrl = HDClient.getInstance().visitorManager().getIFrameBaseUrl();
                if(TextUtils.isEmpty(iframeUrl)){
                    return;
                }
                if (iframeUrl.startsWith("//")){
                    iframeUrl = "http:" + iframeUrl;
                }
                StringBuilder stringBuilder = new StringBuilder(iframeUrl);
                if (iframeUrl.contains("?")){
                    stringBuilder.append("&");
                }else{
                    stringBuilder.append("?");
                }

                if (customInfoString == null){
                    asyncGetCustomInfo();
                    return;
                }else{
                    try {
                        JSONObject jsonObject = new JSONObject(customInfoString);
                        String visitorIm = jsonObject.optString("visitorIm");
                        String kefuIms = jsonObject.optJSONArray("kefuIms").getString(0);
                        stringBuilder.append("easemobId=").append(kefuIms).append("&");
                        stringBuilder.append("visitorImId=").append(visitorIm);
                        startActivity(new Intent(getActivity(), CustomerIFrameActivity.class).putExtra("url", stringBuilder.toString()));
                    } catch (JSONException e) {
                        HDLog.e(TAG, e.getMessage());
                    }
                }
            }
        });
    }

    private void asyncGetCustomInfo(){
        HDClient.getInstance().visitorManager().asyncGetCustomInfoParam(visitorId, new HDDataCallBack<String>() {
            @Override
            public void onSuccess(String value) {
                customInfoString = value;
            }

            @Override
            public void onError(int error, String errorMsg) {

            }
        });
    }

    private void dismissDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        if (pickerView != null && pickerView.isShowing()) {
            pickerView.dismiss();
        }
    }

    @SuppressWarnings("unchecked") @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_CASCADE_SELECT) {
                String value = data.getStringExtra("value");
                String columnName = data.getStringExtra("name");
                if (selectedView == null) {
                    return;
                }
                updateColumnValue(selectedView, columnName, value, customerInfo.getCustomerId());
            } else if (requestCode == 1000) {
                updateColumnValue(rightTv, columnName, data.getStringExtra("content"), customerInfo.getCustomerId());
            } else if (requestCode == 2000) {

                Map<Long, Boolean> map = (Map<Long, Boolean>) data.getSerializableExtra("isSelectedMap");
                for (Map.Entry<Long, Boolean> entry : map.entrySet()) {
                    if (entry.getValue()) {
                        if (!selectedUserTagIdList.contains(entry.getKey())) {
                            selectedUserTagIdList.add(entry.getKey());
                            for (UserTag userTag : userTagList) {
                                if (userTag.tagId == entry.getKey()) {
                                    selectedUserTagList.add(userTag);
                                    userTag.checked = true;
                                    HDClient.getInstance().visitorManager().setTag(true, visitorId, userTag);
                                }
                            }
                        }
                    } else {
                        if (selectedUserTagIdList.contains(entry.getKey())) {
                            selectedUserTagIdList.remove(entry.getKey());
                            for (UserTag userTag : userTagList) {
                                if (userTag.tagId == entry.getKey()) {
                                    selectedUserTagList.remove(userTag);
                                    userTag.checked = false;
                                    HDClient.getInstance().visitorManager().setTag(false, visitorId, userTag);
                                }
                            }
                        }
                    }
                }
                labelNum.setText(selectedUserTagList.size() + "");
                initTags();
                tagAdapter.notifyDataChanged();
            }
        }
    }

    private void updateColumnValue(final TextView valueTv, String columnName, final Object updateValue, String customerId) {
        updateColumnValue(valueTv, columnName, updateValue, customerId, false);
    }

    private void updateColumnValue(final TextView valueTv, String columnName, final Object updateValue, String customerId, final boolean isDate) {
        dismissDialog();
        dialog = DialogUtils.getLoadingDialog(getContext(), "信息更新中...");

        //putCustomerDetailInfo
        HDClient.getInstance().visitorManager().putCustomerDetailInfo(tenantId, customerId, columnName, updateValue, new HDDataCallBack<String>() {
            @Override public void onSuccess(String value) {
                if (getActivity() == null) {
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override public void run() {
                        dismissDialog();
                        Toast.makeText(getContext(), "信息更新成功！", Toast.LENGTH_SHORT).show();
                        asyncCustomerDetail();
                    }
                });
            }

            @Override public void onError(int error, String errorMsg) {
                if (getActivity() == null) {
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override public void run() {
                        dismissDialog();
                        Toast.makeText(getContext(), "信息更新失败！", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override public void onDestroy() {
        super.onDestroy();
        selectedView = null;
        dismissDialog();
    }
}
