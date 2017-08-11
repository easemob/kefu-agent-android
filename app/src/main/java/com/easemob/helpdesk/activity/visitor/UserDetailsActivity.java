package com.easemob.helpdesk.activity.visitor;

import com.easemob.helpdesk.activity.BaseActivity;

public class UserDetailsActivity extends BaseActivity {
//	private static final String TAG = UserDetailsActivity.class.getSimpleName();
//
//
//	private ImageButton imageBtn;
////	private UserDetail userDetail;
//	private TextView tvName,tvNick,tvPhone,tvQQ,tvEmail,tvCompany,tvNote;
//	private TextView txtTitle;
//	private List<UserTag> UserTagList;
//	private CheckBox btnTags[];
//	private HDUser user;
//	private View nicenameLayout,truenameLayout,telphoneLayout,qqLayout,emailLayout,companyLayout,noteLayout;
//	private final int REQUEST_CODE_MODIFY = 1;
//	private Map<String,Object> oldUserMap;
//	private Map<String, Object> tempUserMap;
//	private String historyNiceName;
//
//
//	private void initView() {
//		tvName = (TextView) findViewById(R.id.visitor_name);
//		tvNick = (TextView) findViewById(R.id.visitor_nickname);
//		tvPhone = (TextView) findViewById(R.id.visitor_phone);
//		tvQQ = (TextView) findViewById(R.id.visitor_qq);
//		tvEmail = (TextView) findViewById(R.id.visitor_email);
//		tvCompany = (TextView) findViewById(R.id.visitor_company);
//		tvNote = (TextView) findViewById(R.id.visitor_description);
//		txtTitle = (TextView) findViewById(R.id.txtTitle);
//		user = (HDUser) getIntent().getSerializableExtra("user");
//
//		nicenameLayout = findViewById(R.id.nicename_layout);
//		truenameLayout = findViewById(R.id.truename_layout);
//		telphoneLayout = findViewById(R.id.telphone_layout);
//		qqLayout = findViewById(R.id.qq_layout);
//		emailLayout = findViewById(R.id.email_layout);
//		companyLayout = findViewById(R.id.company_layout);
//		noteLayout = findViewById(R.id.note_layout);
//
//		nicenameLayout.setOnClickListener(this);
//		truenameLayout.setOnClickListener(this);
//		telphoneLayout.setOnClickListener(this);
//		qqLayout.setOnClickListener(this);
//		emailLayout.setOnClickListener(this);
//		companyLayout.setOnClickListener(this);
//		noteLayout.setOnClickListener(this);
//		imageBtn = (ImageButton) findViewById(R.id.ib_back_userdetail);
//
//	}
//
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		AppConfig.setFitWindowMode(this);
//		setContentView(R.layout.activity_user_details);
//		initView();
//		initListener();
//
//		HelpDeskManager.getInstance().getUserDetails(user.getUserId(), new HDDataCallBack<String>() {
//			@Override
//			public void onSuccess(String value) {
//				HDLog.d(TAG, "getUserDetail :" + value + ",user.userId:" + user.getUserId());
//				if (TextUtils.isEmpty(value)) {
//					return;
//				}
//				tempUserMap = oldUserMap = CommonUtils.getMapFromJson(value);
////				userDetail = JsonUtils.getUserDetails(value);
//				if (oldUserMap == null) {
//					return;
//				}
//				UserDetailsActivity.this.runOnUiThread(new Runnable() {
//					@Override
//					public void run() {
//						String oldTrueName = null;
//						if (oldUserMap.containsKey("trueName")) {
//							oldTrueName = oldUserMap.get("trueName").toString();
//						}
//						if (!TextUtils.isEmpty(oldTrueName) && !oldTrueName.equals("null")) {
//							tvName.setText(oldTrueName);
//						}
//						String oldNiceName = null;
//						if (oldUserMap.containsKey("nicename")) {
//							historyNiceName = oldNiceName = oldUserMap.get("nicename").toString();
//						}
//						if (!TextUtils.isEmpty(oldNiceName) && !oldNiceName.equals("null")) {
//							tvNick.setText(oldNiceName);
//							txtTitle.setText(oldNiceName);
//						}
//						String oldQQ = "";
//						if (oldUserMap.containsKey("qq")) {
//							oldQQ = oldUserMap.get("qq").toString();
//						}
//						if (!TextUtils.isEmpty(oldQQ) && !oldQQ.equals("null")) {
//							tvQQ.setText(oldQQ);
//						}
//						String oldCompany = "";
//						if (oldUserMap.containsKey("companyName")) {
//							oldCompany = oldUserMap.get("companyName").toString();
//						}
//						if (!TextUtils.isEmpty(oldCompany) && !oldCompany.equals("null")) {
//							tvCompany.setText(oldCompany);
//						}
//						String oldEmail = "";
//						if (oldUserMap.containsKey("email")) {
//							oldEmail = oldUserMap.get("email").toString();
//						}
//						if (!TextUtils.isEmpty(oldEmail) && !oldEmail.equals("null")) {
//							tvEmail.setText(oldEmail);
//						}
//						String oldPhone = "";
//						if (oldUserMap.containsKey("phone")) {
//							oldPhone = oldUserMap.get("phone").toString();
//						}
//						if (oldPhone != null && !oldPhone.equals("null")) {
//							tvPhone.setText(oldPhone);
//						}
//						String oldDescription = "";
//						if (oldUserMap.containsKey("description")) {
//							oldDescription = oldUserMap.get("description").toString();
//						}
//						if (oldDescription != null && !oldDescription.equals("null")) {
//							tvNote.setText(oldDescription);
//						}
//					}
//				});
//			}
//
//			@Override
//			public void onError(int error, String errorMsg) {
//			}
//
//			@Override
//			public void onAuthenticationException() {
//				// TODO Auto-generated method stub
//
//			}
//		});
//
//
//
//		//获取用户标签
//		HelpDeskManager.getInstance().getUserTag(user.getTenantId(), user.getUserId(),new HDDataCallBack<String>() {
//			@Override
//			public void onSuccess(String value) {
//				UserTagList = JsonUtils.getUserTags(value);
//				 runOnUiThread(new Runnable() {
//
//					@Override
//					public void run() {
//						setCustomeView(UserTagList);
//					}
//				});
//			}
//
//			@Override
//			public void onError(int error, String errorMsg) {
//			}
//
//			@Override
//			public void onAuthenticationException() {
//				// TODO Auto-generated method stub
//
//			}
//		});
//
//
//	}
//
//	private void initListener() {
//		imageBtn.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				if (oldUserMap == null || oldUserMap.size() == 0) {
//					finish();
//				} else if (oldUserMap.containsKey("nicename")) {
//					String newNiceName = oldUserMap.get("nicename").toString();
//					if (!TextUtils.isEmpty(historyNiceName) && !historyNiceName.equals(newNiceName)) {
//						setResult(RESULT_OK, new Intent().putExtra("nicename", newNiceName));
//						UserDetailsActivity.this.finish();
//					} else {
//						UserDetailsActivity.this.finish();
//					}
//				} else {
//					finish();
//				}
//
//
//			}
//		});
//	}
//
//
//	private void setCustomeView(List<UserTag> mList){
//		TableLayout  tablelayout = (TableLayout) findViewById(R.id.tablelayout);
//		int btnNum = mList.size();
//		btnTags = new CheckBox[btnNum];
//		int rowCount = btnNum%3==0?(btnNum/3):(btnNum/3)+1;
//		TableRow[] tableRow = new TableRow[rowCount];
//		for (int i = 0; i < rowCount; i++) {
//			tableRow[i] = new TableRow(getBaseContext());
//			TableLayout.LayoutParams layoutParams = new TableLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
////			TableLayout.LayoutParams layoutParams = new TableLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, CommonUtils.convertDip2Px(getBaseContext(), 35));
//			layoutParams.bottomMargin = CommonUtils.convertDip2Px(getBaseContext(), 5);
//			layoutParams.topMargin = CommonUtils.convertDip2Px(getBaseContext(), 5);
//			tableRow[i].setLayoutParams(layoutParams);
//			tableRow[i].setOrientation(TableRow.HORIZONTAL);
//			tablelayout.addView(tableRow[i]);
//		}
//		for (int i = 0; i < btnNum; i++) {
//			int countCheck = i/3;
//			btnTags[i] = new CheckBox(getBaseContext());
//			TableRow.LayoutParams tableRowLp = new TableRow.LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, CommonUtils.convertDip2Px(this, 35));
//			tableRowLp.leftMargin = CommonUtils.convertDip2Px(getBaseContext(), 10);
//			tableRowLp.rightMargin = CommonUtils.convertDip2Px(getBaseContext(), 10);
//			tableRowLp.weight = 1;
//			btnTags[i].setLayoutParams(tableRowLp);
//			btnTags[i].setBackgroundResource(R.drawable.bg_radiobtn);
//			btnTags[i].setEllipsize(TruncateAt.END);
//			btnTags[i].setSingleLine(true);
//			btnTags[i].setMaxWidth(CommonUtils.convertDip2Px(getBaseContext(), 70));
//			btnTags[i].setButtonDrawable(getResources().getDrawable(android.R.color.transparent));
//			btnTags[i].setGravity(Gravity.CENTER);
//			btnTags[i].setPadding(CommonUtils.convertDip2Px(getBaseContext(), 10), 0, CommonUtils.convertDip2Px(getBaseContext(), 10), 0);
//			btnTags[i].setTextSize(14);
//			btnTags[i].setChecked(mList.get(i).checked);
//			if(btnTags[i].isChecked()){
//				btnTags[i].setTextColor(Color.WHITE);
//			}else{
//				btnTags[i].setTextColor(Color.BLACK);
//			}
//			btnTags[i].setText(mList.get(i).tagName);
//			btnTags[i].setOnCheckedChangeListener(new CheckBoxCheckListener());
//			tableRow[countCheck].addView(btnTags[i]);
//		}
//
//	}
//
//
//
//	class CheckBoxCheckListener implements OnCheckedChangeListener {
//
//		@Override
//		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//
//			for (int j = 0; j < UserTagList.size(); j++) {
//				if (btnTags[j] == buttonView) {
//					UserTag uTag = UserTagList.get(j);
//					if (isChecked) {
//						btnTags[j].setTextColor(Color.WHITE);
//						HelpDeskManager.getInstance().putUserTagTrue(uTag.visitorUserId, uTag.userTagId,
//								String.valueOf(uTag.tenantId), new HDDataCallBack<String>() {
//									@Override
//									public void onSuccess(String value) {
//									}
//
//									@Override
//									public void onError(int error, String errorMsg) {
//									}
//
//									@Override
//									public void onAuthenticationException() {
//									}
//								});
//					} else {
//						btnTags[j].setTextColor(Color.BLACK);
//						HelpDeskManager.getInstance().putUserTagFalse(uTag.visitorUserId, uTag.userTagId,
//								String.valueOf(uTag.tenantId), new HDDataCallBack<String>() {
//									@Override
//									public void onSuccess(String value) {
//									}
//
//									@Override
//									public void onError(int error, String errorMsg) {
//									}
//
//									@Override
//									public void onAuthenticationException() {
//
//									}
//								});
//					}
//
//				}
//			}
//		}
//
//	}
//
//	private boolean isMobile(String mobile) {
//		String telRegex = "[1]\\d{10,17}";
//		return !TextUtils.isEmpty(mobile) && mobile.matches(telRegex);
//
//	}
//
//	private boolean isQQ(String qq) {
//		String qqRegex = "[0-9]{4,22}";
//		return !TextUtils.isEmpty(qq) && qq.matches(qqRegex);
//	}
//
//	private boolean isEmail(String email) {
//		String emailRegex = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
//		return !TextUtils.isEmpty(email) && Pattern.compile(emailRegex).matcher(email).matches();
//	}
//
//	private boolean isNumber(String str){
//		Pattern pattern = Pattern.compile("[0-9]*");
//		Matcher isNum = pattern.matcher(str);
//		return isNum.matches();
//	}
//
//
//
//	@Override
//	public void onClick(View v) {
//		switch (v.getId()) {
//		case R.id.nicename_layout:
//			startActivityForResult(
//					new Intent(this, ModifyActivity.class)
//					.putExtra("index", ModifyActivity.MODIFY_NICENAME)
//					.putExtra("content", tvNick.getText().toString()), REQUEST_CODE_MODIFY);
//			break;
//		case R.id.truename_layout:
//			startActivityForResult(
//					new Intent(this, ModifyActivity.class)
//					.putExtra("index", ModifyActivity.MODIFY_TRUENAME)
//					.putExtra("content", tvName.getText().toString()), REQUEST_CODE_MODIFY);
//			break;
//		case R.id.telphone_layout:
//			startActivityForResult(
//					new Intent(this, ModifyActivity.class)
//					.putExtra("index", ModifyActivity.MODIFY_TELPHONE)
//					.putExtra("content", tvPhone.getText().toString()), REQUEST_CODE_MODIFY);
//			break;
//		case R.id.qq_layout:
//			startActivityForResult(
//					new Intent(this, ModifyActivity.class)
//					.putExtra("index", ModifyActivity.MODIFY_QQ)
//					.putExtra("content", tvQQ.getText().toString()), REQUEST_CODE_MODIFY);
//			break;
//		case R.id.email_layout:
//			startActivityForResult(
//					new Intent(this, ModifyActivity.class)
//					.putExtra("index", ModifyActivity.MODIFY_EMAIL)
//					.putExtra("content", tvEmail.getText().toString()), REQUEST_CODE_MODIFY);
//			break;
//		case R.id.company_layout:
//			startActivityForResult(
//					new Intent(this, ModifyActivity.class)
//					.putExtra("index", ModifyActivity.MODIFY_COMPANY)
//					.putExtra("content", tvCompany.getText().toString()), REQUEST_CODE_MODIFY);
//			break;
//		case R.id.note_layout:
//			startActivityForResult(
//					new Intent(this, ModifyActivity.class)
//					.putExtra("index", ModifyActivity.MODIFY_NOTE)
//					.putExtra("content", tvNote.getText().toString()), REQUEST_CODE_MODIFY);
//			break;
//		default:
//			break;
//		}
//
//	}
//
//
//	@Override
//	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//		super.onActivityResult(requestCode, resultCode, data);
//		if(resultCode == RESULT_OK){
//			if(requestCode == REQUEST_CODE_MODIFY){
//				final String txtContent = data.getStringExtra("content");
//				final int index = data.getIntExtra("index", 0);
//				switch (index) {
//				case ModifyActivity.MODIFY_NICENAME:
//					if(!TextUtils.isEmpty(txtContent)&&txtContent.length()>22){
//						Toast.makeText(this, "客户名最大长度22位！", Toast.LENGTH_SHORT).show();
//						return;
//					}
//					tempUserMap.put("nicename",txtContent);
//					break;
//				case ModifyActivity.MODIFY_TRUENAME:
//					if(!TextUtils.isEmpty(txtContent)&&txtContent.length()>22){
//						Toast.makeText(this, "姓名最大长度22位！", Toast.LENGTH_SHORT).show();
//						return;
//					}
//					tempUserMap.put("trueName",txtContent);
//					break;
//				case ModifyActivity.MODIFY_TELPHONE:
//					if(!TextUtils.isEmpty(txtContent)&&!isMobile(txtContent)){
//						Toast.makeText(this, "手机号长度为11-18位数字！", Toast.LENGTH_SHORT).show();
//						return;
//					}
//					tempUserMap.put("phone",txtContent);
//					break;
//				case ModifyActivity.MODIFY_QQ:
//					if(!TextUtils.isEmpty(txtContent)&&!isQQ(txtContent)){
//						Toast.makeText(this, "QQ有效长度为4-22位数字！", Toast.LENGTH_SHORT).show();
//						return;
//					}
//					tempUserMap.put("qq",txtContent);
//					break;
//				case ModifyActivity.MODIFY_EMAIL:
//					if(!TextUtils.isEmpty(txtContent)&&!isEmail(txtContent)){
//						Toast.makeText(this, "email格式不正确！", Toast.LENGTH_SHORT).show();
//						return;
//					}
//					tempUserMap.put("email",txtContent);
//					break;
//				case ModifyActivity.MODIFY_COMPANY:
//					tempUserMap.put("companyName",txtContent);
//					break;
//				case ModifyActivity.MODIFY_NOTE:
//					tempUserMap.put("description",txtContent);
//					break;
//				default:
//					return;
//				}
//
//				HelpDeskManager.getInstance().putUserDetails(HDUser.getUserId(), tempUserMap, new HDDataCallBack<String>() {
//
//					@Override
//					public void onSuccess(String value) {
//						if(UserDetailsActivity.this.isFinishing()){
//							return;
//						}
//						 runOnUiThread(new Runnable() {
//
//							@Override
//							public void run() {
//								oldUserMap = tempUserMap;
//								Toast.makeText(getBaseContext(), "修改成功！", Toast.LENGTH_SHORT).show();
//								switch (index) {
//								case ModifyActivity.MODIFY_NICENAME:
//									txtTitle.setText(txtContent);
//									tvNick.setText(txtContent);
//									break;
//								case ModifyActivity.MODIFY_TRUENAME:
//									tvName.setText(txtContent);
//									break;
//								case ModifyActivity.MODIFY_TELPHONE:
//									tvPhone.setText(txtContent);
//									break;
//								case ModifyActivity.MODIFY_QQ:
//									tvQQ.setText(txtContent);
//									break;
//								case ModifyActivity.MODIFY_EMAIL:
//									tvEmail.setText(txtContent);
//									break;
//								case ModifyActivity.MODIFY_COMPANY:
//									tvCompany.setText(txtContent);
//									break;
//								case ModifyActivity.MODIFY_NOTE:
//									tvNote.setText(txtContent);
//									break;
//								}
//							}
//						});
//					}
//
//					@Override
//					public void onError(int error, String errorMsg) {
//						if(isFinishing()){
//							return;
//						}
//						runOnUiThread(new Runnable() {
//
//							@Override
//							public void run() {
//								tempUserMap = oldUserMap;
//								Toast.makeText(getBaseContext(), "修改失败", Toast.LENGTH_SHORT).show();
//							}
//						});
//					}
//
//					@Override
//					public void onAuthenticationException() {
//					}
//				});
//
//			}
//
//		}
//
//
//
//	}
//
	
	
}
