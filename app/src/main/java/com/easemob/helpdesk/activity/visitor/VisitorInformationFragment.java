package com.easemob.helpdesk.activity.visitor;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.ModifyActivity;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.entity.HDVisitorUser;
import com.hyphenate.kefusdk.utils.HDLog;

import java.util.Map;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by liyuzhao on 16/2/29.
 */
public class VisitorInformationFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = VisitorInformationFragment.class.getSimpleName();
    @BindView(R.id.visitor_name)
    public TextView tvName;
    @BindView(R.id.visitor_nickname)
    public TextView tvNick;
    @BindView(R.id.visitor_phone)
    public TextView tvPhone;
    @BindView(R.id.visitor_qq)
    public TextView tvQQ;
    @BindView(R.id.visitor_email)
    public TextView tvEmail;
    @BindView(R.id.visitor_company)
    public TextView tvCompany;
    @BindView(R.id.visitor_description)
    public TextView tvNote;

    @BindView(R.id.nicename_layout)
    public View nicenameLayout;
    @BindView(R.id.truename_layout)
    public View truenameLayout;
    @BindView(R.id.telphone_layout)
    public View telphoneLayout;
    @BindView(R.id.qq_layout)
    public View qqLayout;
    @BindView(R.id.email_layout)
    public View emailLayout;
    @BindView(R.id.company_layout)
    public View companyLayout;
    @BindView(R.id.note_layout)
    public View noteLayout;

    private HDVisitorUser visitorUser;
    private final int REQUEST_CODE_MODIFY = 1;
    private Map<String, Object> oldUserMap;
    private Map<String, Object> tempUserMap;
    private String historyNiceName;


    private void initListener() {
        Bundle bundle = getArguments();
        if (bundle == null){
            getActivity().finish();
            return;
        }
        visitorUser = bundle.getParcelable("user");
        nicenameLayout.setOnClickListener(this);
        truenameLayout.setOnClickListener(this);
        telphoneLayout.setOnClickListener(this);
        qqLayout.setOnClickListener(this);
        emailLayout.setOnClickListener(this);
        companyLayout.setOnClickListener(this);
        noteLayout.setOnClickListener(this);

    }
    private Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_visitor_infomation, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initListener();
        if (visitorUser == null) {
            return;
        }

        HDClient.getInstance().visitorManager().getUserDetails(visitorUser.getUserId(), new HDDataCallBack<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> value) {
                HDLog.d(TAG, "getUserDetail :" + value + ",HDUser.userId:" + visitorUser.getUserId());
                tempUserMap = oldUserMap = value;
                if (oldUserMap == null) {
                    return;
                }
                if (getActivity() == null) {
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String oldTrueName = null;
                        if (oldUserMap.containsKey("trueName")) {
                            oldTrueName = oldUserMap.get("trueName").toString();
                        }
                        if (!TextUtils.isEmpty(oldTrueName) && !oldTrueName.equals("null")) {
                            tvName.setText(oldTrueName);
                        }
                        String oldNiceName = null;
                        if (oldUserMap.containsKey("nicename")) {
                            historyNiceName = oldNiceName = oldUserMap.get("nicename").toString();
                        }
                        if (!TextUtils.isEmpty(oldNiceName) && !oldNiceName.equals("null")) {
                            tvNick.setText(oldNiceName);
                            if (getActivity() instanceof VisitorDetailActivity) {
                                ((VisitorDetailActivity)getActivity()).updateCurrentNick(oldNiceName);
                            }
                        }
                        String oldQQ = "";
                        if (oldUserMap.containsKey("qq")) {
                            oldQQ = oldUserMap.get("qq").toString();
                        }
                        if (!TextUtils.isEmpty(oldQQ) && !oldQQ.equals("null")) {
                            tvQQ.setText(oldQQ);
                        }
                        String oldCompany = "";
                        if (oldUserMap.containsKey("companyName")) {
                            oldCompany = oldUserMap.get("companyName").toString();
                        }
                        if (!TextUtils.isEmpty(oldCompany) && !oldCompany.equals("null")) {
                            tvCompany.setText(oldCompany);
                        }
                        String oldEmail = "";
                        if (oldUserMap.containsKey("email")) {
                            oldEmail = oldUserMap.get("email").toString();
                        }
                        if (!TextUtils.isEmpty(oldEmail) && !oldEmail.equals("null")) {
                            tvEmail.setText(oldEmail);
                        }
                        String oldPhone = "";
                        if (oldUserMap.containsKey("phone")) {
                            oldPhone = oldUserMap.get("phone").toString();
                        }
                        if (oldPhone != null && !oldPhone.equals("null")) {
                            tvPhone.setText(oldPhone);
                        }
                        String oldDescription = "";
                        if (oldUserMap.containsKey("description")) {
                            oldDescription = oldUserMap.get("description").toString();
                        }
                        if (oldDescription != null && !oldDescription.equals("null")) {
                            tvNote.setText(oldDescription);
                        }
                    }
                });
            }

            @Override
            public void onError(int error, String errorMsg) {
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.nicename_layout:
                startActivityForResult(
                        new Intent(getActivity(), ModifyActivity.class)
                                .putExtra("index", ModifyActivity.MODIFY_NICENAME)
                                .putExtra("content", tvNick.getText().toString()), REQUEST_CODE_MODIFY);
                break;
            case R.id.truename_layout:
                startActivityForResult(
                        new Intent(getActivity(), ModifyActivity.class)
                                .putExtra("index", ModifyActivity.MODIFY_TRUENAME)
                                .putExtra("content", tvName.getText().toString()), REQUEST_CODE_MODIFY);
                break;
            case R.id.telphone_layout:
                startActivityForResult(
                        new Intent(getActivity(), ModifyActivity.class)
                                .putExtra("index", ModifyActivity.MODIFY_TELPHONE)
                                .putExtra("content", tvPhone.getText().toString()), REQUEST_CODE_MODIFY);
                break;
            case R.id.qq_layout:
                startActivityForResult(
                        new Intent(getActivity(), ModifyActivity.class)
                                .putExtra("index", ModifyActivity.MODIFY_QQ)
                                .putExtra("content", tvQQ.getText().toString()), REQUEST_CODE_MODIFY);
                break;
            case R.id.email_layout:
                startActivityForResult(
                        new Intent(getActivity(), ModifyActivity.class)
                                .putExtra("index", ModifyActivity.MODIFY_EMAIL)
                                .putExtra("content", tvEmail.getText().toString()), REQUEST_CODE_MODIFY);
                break;
            case R.id.company_layout:
                startActivityForResult(
                        new Intent(getActivity(), ModifyActivity.class)
                                .putExtra("index", ModifyActivity.MODIFY_COMPANY)
                                .putExtra("content", tvCompany.getText().toString()), REQUEST_CODE_MODIFY);
                break;
            case R.id.note_layout:
                startActivityForResult(
                        new Intent(getActivity(), ModifyActivity.class)
                                .putExtra("index", ModifyActivity.MODIFY_NOTE)
                                .putExtra("content", tvNote.getText().toString()), REQUEST_CODE_MODIFY);
                break;
            default:
                break;
        }

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_MODIFY) {
                final String txtContent = data.getStringExtra("content");
                final int index = data.getIntExtra("index", 0);
                switch (index) {
                    case ModifyActivity.MODIFY_NICENAME:
                        if (!TextUtils.isEmpty(txtContent) && txtContent.length() > 22) {
                            Toast.makeText(getActivity(), "客户名最大长度22位！", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        tempUserMap.put("nicename", txtContent);
                        break;
                    case ModifyActivity.MODIFY_TRUENAME:
                        if (!TextUtils.isEmpty(txtContent) && txtContent.length() > 22) {
                            Toast.makeText(getActivity(), "姓名最大长度22位！", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        tempUserMap.put("trueName", txtContent);
                        break;
                    case ModifyActivity.MODIFY_TELPHONE:
                        if (!TextUtils.isEmpty(txtContent) && !isMobile(txtContent)) {
                            Toast.makeText(getActivity(), "手机号长度为11-18位数字！", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        tempUserMap.put("phone", txtContent);
                        break;
                    case ModifyActivity.MODIFY_QQ:
                        if (!TextUtils.isEmpty(txtContent) && !isQQ(txtContent)) {
                            Toast.makeText(getActivity(), "QQ有效长度为4-22位数字！", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        tempUserMap.put("qq", txtContent);
                        break;
                    case ModifyActivity.MODIFY_EMAIL:
                        if (!TextUtils.isEmpty(txtContent) && !isEmail(txtContent)) {
                            Toast.makeText(getActivity(), "email格式不正确！", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        tempUserMap.put("email", txtContent);
                        break;
                    case ModifyActivity.MODIFY_COMPANY:
                        tempUserMap.put("companyName", txtContent);
                        break;
                    case ModifyActivity.MODIFY_NOTE:
                        tempUserMap.put("description", txtContent);
                        break;
                    default:
                        return;
                }

                HDClient.getInstance().visitorManager().putUserDetails(visitorUser.getUserId(), tempUserMap, new HDDataCallBack<String>() {

                    @Override
                    public void onSuccess(String value) {
                        if (getActivity() == null) {
                            return;
                        }
                        getActivity().runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                oldUserMap = tempUserMap;
                                Toast.makeText(getActivity(), "修改成功！", Toast.LENGTH_SHORT).show();
                                switch (index) {
                                    case ModifyActivity.MODIFY_NICENAME:
                                        tvNick.setText(txtContent);
                                        if (getActivity() instanceof VisitorDetailActivity) {
                                            ((VisitorDetailActivity)getActivity()).updateCurrentNick(txtContent);
                                        }
                                        break;
                                    case ModifyActivity.MODIFY_TRUENAME:
                                        tvName.setText(txtContent);
                                        break;
                                    case ModifyActivity.MODIFY_TELPHONE:
                                        tvPhone.setText(txtContent);
                                        break;
                                    case ModifyActivity.MODIFY_QQ:
                                        tvQQ.setText(txtContent);
                                        break;
                                    case ModifyActivity.MODIFY_EMAIL:
                                        tvEmail.setText(txtContent);
                                        break;
                                    case ModifyActivity.MODIFY_COMPANY:
                                        tvCompany.setText(txtContent);
                                        break;
                                    case ModifyActivity.MODIFY_NOTE:
                                        tvNote.setText(txtContent);
                                        break;
                                }
                            }
                        });
                    }

                    @Override
                    public void onError(int error, String errorMsg) {
                        if (getActivity() == null) {
                            return;
                        }
                        getActivity().runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                tempUserMap = oldUserMap;
                                Toast.makeText(getActivity(), "修改失败", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

            }

        }


    }

    private boolean isMobile(String mobile) {
        String telRegex = "[1]\\d{10,17}";
        return !TextUtils.isEmpty(mobile) && mobile.matches(telRegex);

    }

    private boolean isQQ(String qq) {
        String qqRegex = "[0-9]{4,22}";
        return !TextUtils.isEmpty(qq) && qq.matches(qqRegex);
    }

    private boolean isEmail(String email) {
        String emailRegex = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
        return !TextUtils.isEmpty(email) && Pattern.compile(emailRegex).matcher(email).matches();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null){
            unbinder.unbind();
        }
    }
}
