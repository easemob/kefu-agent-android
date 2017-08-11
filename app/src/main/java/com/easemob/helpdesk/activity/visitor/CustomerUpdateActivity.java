package com.easemob.helpdesk.activity.visitor;

import com.easemob.helpdesk.activity.BaseActivity;

/**
 * 文本修改界面，主要有title和文本框组成
 */
public class CustomerUpdateActivity extends BaseActivity  {

//    public static final int MODIFY_NICENAME = 1;
//    public static final int MODIFY_TRUENAME = 2;
//    public static final int MODIFY_TELPHONE = 3;
//    public static final int MODIFY_QQ = 4;
//    public static final int MODIFY_EMAIL = 5;
//    public static final int MODIFY_COMPANY = 6;
//    public static final int MODIFY_NOTE = 7;
//
//    //user profile setting
//    public static final int PROFILE_MODIFY_NICKNAME = 8;
//    public static final int PROFILE_MODIFY_TRUENAME = 9;
//    public static final int PROFILE_MODIFY_AGENTNUMBER = 10;
//    public static final int PROFILE_MODIFY_MOBILE = 11;
//    public static final int PROFILE_MODIFY_PWD = 12;
//
//
//    public static final int MODIFY_WEL_CONTENT = 13;
//
//    //screeningactivity visitorname
//    public static final int SCREENING_MODIFY_VISITORNAME = 14;
//
//    private TextView txtTitle;
//    private EditText etContent;
//    private ImageButton btnBack;
//    private Button btnSave;
//    private final String StringExtra = "content";
//    private final String TitleExtra = "title";
//    private String txtContent;
//    private String strTitle;
//    private CustomerInfoResponse.EntityBean.ColumnValuesBean bean;
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        AppConfig.setFitWindowMode(this);
//        setContentView(R.layout.activity_modify);
//        Intent intent = getIntent();
//        strTitle = intent.getStringExtra(TitleExtra);
//        txtContent = intent.getStringExtra(StringExtra);
//        bean = (CustomerInfoResponse.EntityBean.ColumnValuesBean) intent.getSerializableExtra("bean");
//        initView();
//        if (!TextUtils.isEmpty(strTitle)){
//            txtTitle.setText(strTitle);
//        }
//
//        if (!TextUtils.isEmpty(txtContent)) {
//            etContent.setText(txtContent);
//            etContent.setSelection(etContent.getText().length());
//        }
//
//    }
//
//
//    /**
//     * 获取View
//     */
//    private void initView() {
//        btnBack = (ImageButton) findViewById(R.id.btnBack);
//        btnSave = (Button) findViewById(R.id.btnSave);
//        etContent = (EditText) findViewById(R.id.editText);
//        txtTitle = (TextView) findViewById(R.id.txtTitle);
//        btnBack.setOnClickListener(this);
//        btnSave.setOnClickListener(this);
//    }
//
//    private boolean isMobile(String mobile) {
//        String telRegex = "[1]\\d{10,17}";
//        return !TextUtils.isEmpty(mobile) && mobile.matches(telRegex);
//
//    }
//
//    private boolean isQQ(String qq) {
//        String qqRegex = "[0-9]{4,22}";
//        return !TextUtils.isEmpty(qq) && qq.matches(qqRegex);
//    }
//
//    private boolean isEmail(String email) {
//        String emailRegex = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
//        return !TextUtils.isEmpty(email) && Pattern.compile(emailRegex).matcher(email).matches();
//    }
//
//
//    @Override
//    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.btnBack:
//                finish();
//                break;
//            case R.id.btnSave:
//                String txtContent = etContent.getText().toString().trim();
//                Intent intent = new Intent();
//                intent.putExtra(StringExtra, txtContent);
//                intent.putExtra("bean", bean);
//                setResult(RESULT_OK, intent);
//                finish();
//                break;
//            default:
//                break;
//        }
//    }
}
