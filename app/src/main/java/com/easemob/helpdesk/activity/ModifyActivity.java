package com.easemob.helpdesk.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.helpdesk.AppConfig;
import com.easemob.helpdesk.R;

import java.util.regex.Pattern;

/**
 * 文本修改界面，主要有title和文本框组成
 */
public class ModifyActivity extends BaseActivity implements OnClickListener {

    public static final int MODIFY_NICENAME = 1;
    public static final int MODIFY_TRUENAME = 2;
    public static final int MODIFY_TELPHONE = 3;
    public static final int MODIFY_QQ = 4;
    public static final int MODIFY_EMAIL = 5;
    public static final int MODIFY_COMPANY = 6;
    public static final int MODIFY_NOTE = 7;

    //user profile setting
    public static final int PROFILE_MODIFY_NICKNAME = 8;
    public static final int PROFILE_MODIFY_TRUENAME = 9;
    public static final int PROFILE_MODIFY_AGENTNUMBER = 10;
    public static final int PROFILE_MODIFY_MOBILE = 11;
    public static final int PROFILE_MODIFY_PWD = 12;


    public static final int MODIFY_WEL_CONTENT = 13;

    //screeningactivity visitorname
    public static final int SCREENING_MODIFY_VISITORNAME = 14;

    private TextView txtTitle;
    private EditText etContent;
    private ImageButton btnBack;
    private Button btnSave;
    private int index = 0;
    private final String IndexExtra = "index";
    private final String StringExtra = "content";
    private final String TitleExtra = "title";
    private String txtContent;
    private String strTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppConfig.setFitWindowMode(this);
        setContentView(R.layout.activity_modify);
        Intent intent = getIntent();
        index = intent.getIntExtra(IndexExtra, 0);
        strTitle = intent.getStringExtra(TitleExtra);
        txtContent = intent.getStringExtra(StringExtra);
        initView();

        switch (index) {
            case MODIFY_NICENAME:
                txtTitle.setText("客户名");
                break;
            case MODIFY_TRUENAME:
                txtTitle.setText("真实姓名");
                break;
            case MODIFY_TELPHONE:
                txtTitle.setText("手机");
                break;
            case MODIFY_QQ:
                txtTitle.setText("QQ");
                break;
            case MODIFY_EMAIL:
                txtTitle.setText("邮箱");
                break;
            case MODIFY_COMPANY:
                txtTitle.setText("公司");
                break;
            case MODIFY_NOTE:
                txtTitle.setText("备注");
                break;
            case MODIFY_WEL_CONTENT:
                txtTitle.setText("问候语");
                break;
            case PROFILE_MODIFY_NICKNAME:
                txtTitle.setText("昵称");
                etContent.setFilters(new InputFilter[]{new InputFilter.LengthFilter(22)});
                etContent.setInputType(InputType.TYPE_CLASS_TEXT);
                break;
            case PROFILE_MODIFY_TRUENAME:
                txtTitle.setText("姓名");
                etContent.setFilters(new InputFilter[]{new InputFilter.LengthFilter(24)});
                etContent.setInputType(InputType.TYPE_CLASS_TEXT);
                break;
            case PROFILE_MODIFY_AGENTNUMBER:
                txtTitle.setText("编号");
                etContent.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
                etContent.setInputType(InputType.TYPE_CLASS_NUMBER);
                break;
            case PROFILE_MODIFY_MOBILE:
                txtTitle.setText("手机");
                etContent.setFilters(new InputFilter[]{new InputFilter.LengthFilter(18)});
                etContent.setInputType(InputType.TYPE_CLASS_PHONE);
                break;
            case PROFILE_MODIFY_PWD:
                txtTitle.setText("密码");
                etContent.setFilters(new InputFilter[]{new InputFilter.LengthFilter(22)});
                etContent.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                break;
            case SCREENING_MODIFY_VISITORNAME:
                txtTitle.setText("访客名称");
                etContent.setFilters(new InputFilter[]{new InputFilter.LengthFilter(256)});
                etContent.setInputType(InputType.TYPE_CLASS_TEXT);
                break;
            default:
                break;
        }

        if (!TextUtils.isEmpty(strTitle)){
            txtTitle.setText(strTitle);
        }

        if (!TextUtils.isEmpty(txtContent)) {
            etContent.setText(txtContent);
            etContent.setSelection(etContent.getText().length());
        }

    }


    /**
     * 获取View
     */
    private void initView() {
        btnBack = (ImageButton) findViewById(R.id.btnBack);
        btnSave = (Button) findViewById(R.id.btnSave);
        etContent = (EditText) findViewById(R.id.editText);
        txtTitle = (TextView) findViewById(R.id.txtTitle);
        btnBack.setOnClickListener(this);
        btnSave.setOnClickListener(this);
    }

    /**
     * 检测输入的内容是否合法
     * @param content
     * @return
     */
    private boolean checkInputError(String content) {
        switch (index) {
            case PROFILE_MODIFY_PWD:
                if (!content.matches("^[\\d\\D]{6,22}$")){
                    Toast.makeText(this, "密码有效长度6~22位!", Toast.LENGTH_SHORT).show();
                    return true;
                }

                if (!content.matches("^(?![A-Z]+$)(?![a-z]+$)(?!\\d+$)(?![\\W_]+$)\\S+$")){
                    Toast.makeText(this, "密码至少包含大写字母,小写字母,数字,符号中两种!", Toast.LENGTH_SHORT).show();
                    return true;
                }
                break;
            case PROFILE_MODIFY_MOBILE:
                if (!isMobile(content)) {
                    Toast.makeText(this, "有效长度11～18位数字", Toast.LENGTH_SHORT).show();
                    return true;
                }
                break;
            case MODIFY_NICENAME:
                if (content.length() > 22){
                    Toast.makeText(this, "客户名最大长度22位！", Toast.LENGTH_SHORT).show();
                    return true;
                }
                break;
            case MODIFY_TRUENAME:
                if (content.length() > 22){
                    Toast.makeText(this, "姓名最大长度22位！", Toast.LENGTH_SHORT).show();
                    return true;
                }
                break;
            case MODIFY_TELPHONE:
                if (!isMobile(content)){
                    Toast.makeText(this, "手机号长度为11-18位数字！", Toast.LENGTH_SHORT).show();
                    return true;
                }
                break;
            case MODIFY_QQ:
                if (!isQQ(content)){
                    Toast.makeText(this, "QQ有效长度为4-22位数字！", Toast.LENGTH_SHORT).show();
                    return true;
                }
                break;
            case MODIFY_EMAIL:
                if (!isEmail(content)){
                    Toast.makeText(this, "email格式不正确！", Toast.LENGTH_SHORT).show();
                    return true;
                }

                break;
        }

        return false;
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnBack:
                finish();
                break;
            case R.id.btnSave:
                String txtContent = etContent.getText().toString().trim();
                if (index != SCREENING_MODIFY_VISITORNAME) {
                    if (TextUtils.isEmpty(txtContent)) {
                        Toast.makeText(ModifyActivity.this, "不能为空！", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (checkInputError(txtContent)) {
                        return;
                    }
                }

                Intent intent = new Intent();
                intent.putExtra(StringExtra, txtContent);
                intent.putExtra(IndexExtra, index);
                setResult(RESULT_OK, intent);
                finish();
                break;
            default:
                break;
        }
    }
}
