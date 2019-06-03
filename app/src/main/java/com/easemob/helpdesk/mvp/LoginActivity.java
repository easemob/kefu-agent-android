package com.easemob.helpdesk.mvp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.helpdesk.AppConfig;
import com.easemob.helpdesk.ChannelConfig;
import com.easemob.helpdesk.HDApplication;
import com.easemob.helpdesk.HMSPushHelper;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.BaseActivity;
import com.easemob.helpdesk.activity.ForgetPwdActivity;
import com.easemob.helpdesk.databinding.ActivityLoginBinding;
import com.easemob.helpdesk.mvp.view.IUserLoginView;
import com.easemob.helpdesk.utils.CommonUtils;
import com.easemob.helpdesk.utils.DialogUtils;
import com.easemob.helpdesk.utils.EMToast;
import com.easemob.helpdesk.utils.IntentWrapper;
import com.easemob.helpdesk.utils.PreferenceUtils;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.HDErrorCode;
import com.hyphenate.kefusdk.chat.HDClient;

import kr.co.namee.permissiongen.PermissionFail;
import kr.co.namee.permissiongen.PermissionGen;
import kr.co.namee.permissiongen.PermissionSuccess;

import static com.easemob.helpdesk.utils.IntentWrapper.COOLPAD;
import static com.easemob.helpdesk.utils.IntentWrapper.DOZE;
import static com.easemob.helpdesk.utils.IntentWrapper.GIONEE;
import static com.easemob.helpdesk.utils.IntentWrapper.INTENT_WRAPPER_LIST;
import static com.easemob.helpdesk.utils.IntentWrapper.LENOVO;
import static com.easemob.helpdesk.utils.IntentWrapper.LENOVO_GOD;
import static com.easemob.helpdesk.utils.IntentWrapper.LETV;
import static com.easemob.helpdesk.utils.IntentWrapper.LETV_GOD;
import static com.easemob.helpdesk.utils.IntentWrapper.MEIZU;
import static com.easemob.helpdesk.utils.IntentWrapper.MEIZU_GOD;
import static com.easemob.helpdesk.utils.IntentWrapper.MEIZU_WHITE;
import static com.easemob.helpdesk.utils.IntentWrapper.OPPO;
import static com.easemob.helpdesk.utils.IntentWrapper.SAMSUNG_L;
import static com.easemob.helpdesk.utils.IntentWrapper.SAMSUNG_M;
import static com.easemob.helpdesk.utils.IntentWrapper.VIVO_GOD;
import static com.easemob.helpdesk.utils.IntentWrapper.ZTE;
import static com.easemob.helpdesk.utils.IntentWrapper.ZTE_GOD;
import static com.easemob.helpdesk.utils.IntentWrapper.getApplicationName;

/**
 * 登录界面
 * Created by liyuzhao on 16/8/24.
 */
public class LoginActivity extends BaseActivity implements IUserLoginView, CompoundButton.OnCheckedChangeListener {
	private static final int REQUEST_CODE_REQUEST_PERMISSIONS = 1;

	/**
	 * 加载提示Dialog
	 */
	public Dialog pd = null;
	/**
	 * 账号 密码暂存变量
	 */
	private String strName, strPwd;

	ActivityLoginBinding mBinding;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AppConfig.setFitWindowMode(this);
		mBinding = DataBindingUtil.setContentView(this, R.layout.activity_login);
		mBinding.tvRegister.setVisibility(View.VISIBLE);
		//检测是否登录过此应用
		if (HDClient.getInstance().isLoggedInBefore()) {
			toMainActivity();
			return;
		}
		String preUName = PreferenceUtils.getInstance().getUserName();
		initView();
		initListener();

		mBinding.etAccount.setText(preUName);
		mBinding.etPwd.setText("");

		HMSPushHelper.getInstance().connectHMS(this);
	}

	private void initView() {
		mBinding.logoImageview.setImageResource(ChannelConfig.getInstance().getLoginlogo());
		mBinding.cbInputHide.setOnCheckedChangeListener(this);
		mBinding.etAccount.addTextChangedListener(new AccountTextWatch());
		mBinding.etAccount.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEND || actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
					mBinding.etPwd.requestFocus();
				}
				return false;
			}
		});
		//默认英文
		mBinding.etAccount.setInputType(EditorInfo.TYPE_TEXT_VARIATION_URI);
		mBinding.etPwd.addTextChangedListener(new PwdTextWatch());
		mBinding.etPwd.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEND || actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
					loginClickMethod();
					return true;
				}
				return false;
			}
		});
		//设置版本号
		String versionName = CommonUtils.getAppVersionNameFromApp(this);
		if (!TextUtils.isEmpty(versionName)) {
			mBinding.tvVersion.setText("v" + versionName);
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			String sLastVersion = prefs.getString("S_VERSION_KEY", "");
			if ("".equals(sLastVersion)) {
				whiteListMatters();
				prefs.edit().putString("S_VERSION_KEY", versionName).apply();
			}
		}
	}


	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()) {
			case R.id.cb_input_hide:
				if (isChecked) {
					mBinding.etPwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
				} else {
					mBinding.etPwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
				}
				break;

			default:
				break;
		}
	}

	/**
	 * 监控账号输入
	 */
	class AccountTextWatch implements TextWatcher {

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			if (s.length() > 0) {
				mBinding.ivAccountClear.setVisibility(View.VISIBLE);
			} else {
				mBinding.ivAccountClear.setVisibility(View.INVISIBLE);
			}
		}

		@Override
		public void afterTextChanged(Editable s) {
		}
	}

	/**
	 * 监控密码输入
	 */
	class PwdTextWatch implements TextWatcher {

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			if (s.length() > 0) {
				mBinding.ivPwdClear.setVisibility(View.VISIBLE);
				mBinding.cbInputHide.setVisibility(View.VISIBLE);
			} else {
				mBinding.ivPwdClear.setVisibility(View.INVISIBLE);
				mBinding.cbInputHide.setVisibility(View.INVISIBLE);
			}
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			if (s.length() > 0) {
				mBinding.ivPwdClear.setVisibility(View.VISIBLE);
				mBinding.cbInputHide.setVisibility(View.VISIBLE);
			} else {
				mBinding.ivPwdClear.setVisibility(View.INVISIBLE);
				mBinding.cbInputHide.setVisibility(View.INVISIBLE);
			}
		}

		@Override
		public void afterTextChanged(Editable s) {

		}
	}


	private void loginClickMethod() {
		int hasWritePermission = ContextCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
		if (hasWritePermission != PackageManager.PERMISSION_GRANTED) {
			PermissionGen.with(LoginActivity.this)
					.addRequestCode(REQUEST_CODE_REQUEST_PERMISSIONS)
					.permissions(Manifest.permission.WRITE_EXTERNAL_STORAGE
					).request();
		} else {
			login();
		}
	}

	private void initListener() {
		mBinding.btnLogin.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				loginClickMethod();
			}
		});
		//账号清除按钮
		mBinding.ivAccountClear.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mBinding.etAccount.getText().clear();
			}
		});
		mBinding.ivPwdClear.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//密码清除按钮
				mBinding.etPwd.getText().clear();
			}
		});

		mBinding.tvRegister.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(LoginActivity.this, RegisterActivity.class);
				startActivity(intent);
			}
		});
		mBinding.tvForgetPwd.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(LoginActivity.this, ForgetPwdActivity.class);
				startActivity(intent);
			}
		});
	}


	private void login() {
		if (!checkInputVaid()) {
			return;
		}
		showLoading();
		final String uName = getUsername();
		final String uPwd = getPassword();
		HDClient.getInstance().login(uName, uPwd, isHiddenLogin(), new HDDataCallBack<String>() {
			@Override
			public void onSuccess(String value) {
				if (isFinishing()) {
					return;
				}
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						hideLoading();
						PreferenceUtils.getInstance().setUserName(uName);
						HDApplication.getInstance().putGrowingIO(HDClient.getInstance().getCurrentUser());
//                        IMHelper.getInstance().setGlobalListener();
						HMSPushHelper.getInstance().getHMSPushToken();
						toMainActivity();
					}
				});
			}

			@Override
			public void onError(final int error, final String errorMsg) {
				if (isFinishing()) {
					return;
				}
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						hideLoading();
						final String errorDescription;
						if (error == HDErrorCode.USER_ACCOUNT_DISABLED) {
							errorDescription = "您的账号已被禁用，请联系您的管理员";
						} else if (error == HDErrorCode.USER_AUTHENTICATION_FAILED) {
							errorDescription = "用户名或密码错误";
						} else if (error == HDErrorCode.USER_ACCOUNT_NOT_FOUND) {
							errorDescription = "用户不存在";
						} else {
							errorDescription = "请检查网络";
						}
						showFailedError("登录失败:" + errorDescription);
					}
				});
			}
		});


	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
		hideLoading();
	}

	@Override
	public String getUsername() {
		return mBinding.etAccount.getText().toString().trim();
	}

	@Override
	public String getPassword() {
		return mBinding.etPwd.getText().toString().trim();
	}


	/**
	 * 检测是否为隐身登录
	 *
	 * @return
	 */
	@Override
	public boolean isHiddenLogin() {
		return mBinding.cbHiddenLogin.isChecked();
	}

	@Override
	public void showLoading() {
		//显示登录提示对话框
		if (pd == null) {
			pd = DialogUtils.getLoadingDialog(LoginActivity.this, R.string.loading_login);
		} else {
			hideLoading();
		}
		pd.show();
	}

	@Override
	public void hideLoading() {
		if (pd != null && pd.isShowing()) {
			pd.dismiss();
		}
	}

	/**
	 * 跳转到主界面,并关闭当前界面
	 */
	@Override
	public void toMainActivity() {
		hideLoading();
		Intent intent = new Intent();
		intent.putExtra("displayExpireInfo", true);
		intent.setClass(LoginActivity.this, MainActivity.class);
		startActivity(intent);
		finish();

	}

	@Override
	public void showFailedError(final String errorMsg) {
		EMToast.makeLoginFailStyleableToast(LoginActivity.this, errorMsg).show();
	}


	/**
	 * 检测输入是否合法
	 */
	@Override
	public boolean checkInputVaid() {
		//获取输入的账号和密码
		strName = mBinding.etAccount.getText().toString().trim();
		strPwd = mBinding.etPwd.getText().toString().trim();
		if (TextUtils.isEmpty(strName)) {//检测账号是否为空
			Toast.makeText(getApplicationContext(), R.string.toast_account_notIsNull, Toast.LENGTH_SHORT).show();
			return false;
		}
		if (TextUtils.isEmpty(strPwd)) {//检测密码是否为空
			Toast.makeText(getApplicationContext(), R.string.toast_pwd_notIsNull, Toast.LENGTH_SHORT).show();
			return false;
		}
		if (!CommonUtils.isNetWorkConnected(this)) {//检测当前是否有网
			Toast.makeText(getApplicationContext(), R.string.toast_network_isnot_available, Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		PermissionGen.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}

	@PermissionSuccess(requestCode = REQUEST_CODE_REQUEST_PERMISSIONS)
	public void storageAuthSuccess() {
		login();
	}

	@PermissionFail(requestCode = REQUEST_CODE_REQUEST_PERMISSIONS)
	public void storageAuthFail() {
		new AlertDialog.Builder(this).setMessage("app需要读写手机存储权限 \n请在权限管理->读写手机存储->设为允许!").setPositiveButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
				Uri uri = Uri.fromParts("package", getPackageName(), null);
				intent.setData(uri);
				startActivity(intent);
			}
		}).create().show();

	}

	/**
	 * 处理白名单
	 */
	void whiteListMatters() {
		for (final IntentWrapper intentWrapper : INTENT_WRAPPER_LIST) {
			// 如果本机上没有处理这个Intent的Activity, 说明不是对应的机型，直接忽略进入下一个循环。
			if (!intentWrapper.doesActivityExist()) continue;
			switch (intentWrapper.mType) {
				case DOZE:
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
						try {
							PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
							if (pm.isIgnoringBatteryOptimizations(getPackageName())) break;
							new AlertDialog.Builder(this)
									.setCancelable(false)
									.setTitle("需要忽略 " + getApplicationName() + " 的电池优化")
									.setMessage("锁屏收取消息需要 " + getApplicationName() + " 加入到电池优化的忽略名单。\n\n" +
											"请点击『确定』，在弹出的『忽略电池优化』对话框中，选择『是』。")
									.setPositiveButton("确定", new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which) {
											intentWrapper.startActivity(mActivity);
										}
									}).show();
						} catch (Exception ignored) {
							//锤子手机会报找不到这个activity
						}
					}
					break;
				case ZTE_GOD:
					new AlertDialog.Builder(this)
							.setCancelable(false)
							.setTitle(getApplicationName() + " 需要加入锁屏清理白名单")
							.setMessage("锁屏收取消息需要 " + getApplicationName() + " 加入到锁屏清理白名单。\n\n" +
									"请点击『确定』，在弹出的『锁屏清理』列表中，将 " + getApplicationName() + " 对应的开关打开。")
							.setPositiveButton("确定", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									intentWrapper.startActivity(mActivity);
								}
							}).show();
					break;
				case SAMSUNG_L:
					new AlertDialog.Builder(this)
							.setCancelable(false)
							.setTitle("需要允许 " + getApplicationName() + " 的自启动")
							.setMessage("锁屏收取消息需要 " + getApplicationName() + " 在屏幕关闭时继续运行。\n\n" +
									"请点击『确定』，在弹出的『智能管理器』中，点击『内存』，选择『自启动应用程序』选项卡，将 " + getApplicationName() + " 对应的开关打开。")
							.setPositiveButton("确定", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									intentWrapper.startActivity(mActivity);
								}
							}).show();
					break;
				case SAMSUNG_M:
					new AlertDialog.Builder(this)
							.setCancelable(false)
							.setTitle("需要允许 " + getApplicationName() + " 的自启动")
							.setMessage("锁屏收取消息需要 " + getApplicationName() + " 在屏幕关闭时继续运行。\n\n" +
									"请点击『确定』，在弹出的『电池』页面中，点击『未监视的应用程序』->『添加应用程序』，勾选 " + getApplicationName() + "，然后点击『完成』。")
							.setPositiveButton("确定", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									intentWrapper.startActivity(mActivity);
								}
							}).show();
					break;
				case MEIZU:
					new AlertDialog.Builder(this)
							.setCancelable(false)
							.setTitle("需要允许 " + getApplicationName() + " 的自启动")
							.setMessage("锁屏收取消息需要允许 " + getApplicationName() + " 的自启动。\n\n" +
									"请点击『确定』，在弹出的应用信息界面中，将『自启动』开关打开。")
							.setPositiveButton("确定", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									intentWrapper.startActivity(mActivity);
								}
							}).show();
					break;
				case MEIZU_GOD:
					new AlertDialog.Builder(this)
							.setCancelable(false)
							.setTitle(getApplicationName() + " 需要在待机时保持运行")
							.setMessage("锁屏收取消息需要 " + getApplicationName() + " 在待机时保持运行。\n\n" +
									"请点击『确定』，在弹出的『待机耗电管理』中，将 " + getApplicationName() + " 对应的开关打开。")
							.setPositiveButton("确定", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									intentWrapper.startActivity(mActivity);
								}
							}).show();
					break;
				case MEIZU_WHITE:
					new AlertDialog.Builder(this)
							.setCancelable(false)
							.setTitle(getApplicationName() + " 需要添加到加速白名单中")
							.setMessage("请点击右上角的设置，然后选择『手机加速白名单』进入白名单列表。\n\n" +
									"点击添加 『" + getApplicationName() + "』 到列表项。")
							.setPositiveButton("确定", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									intentWrapper.startActivity(mActivity);
								}
							}).show();
					break;
				case ZTE:
				case LETV:
				case OPPO:
					new AlertDialog.Builder(this)
							.setCancelable(false)
							.setTitle("需要允许 " + getApplicationName() + " 的自启动")
							.setMessage("锁屏收取消息需要 " + getApplicationName() + " 加入到自启动白名单。\n\n" +
									"请点击『确定』，在弹出的『自启动管理』中，将 " + getApplicationName() + " 对应的开关打开。")
							.setPositiveButton("确定", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									intentWrapper.startActivity(mActivity);
								}
							}).show();
					break;
				case COOLPAD:
					new AlertDialog.Builder(this)
							.setCancelable(false)
							.setTitle("需要允许 " + getApplicationName() + " 的自启动")
							.setMessage("锁屏收取消息需要允许 " + getApplicationName() + " 的自启动。\n\n" +
									"请点击『确定』，在弹出的『酷管家』中，找到『软件管理』->『自启动管理』，取消勾选 " + getApplicationName() + "，将 " + getApplicationName() + " 的状态改为『已允许』。")
							.setPositiveButton("确定", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									intentWrapper.startActivity(mActivity);
								}
							}).show();
					break;
				case VIVO_GOD:
					new AlertDialog.Builder(this)
							.setCancelable(false)
							.setTitle("" + getApplicationName() + " 需要在后台高耗电时允许运行")
							.setMessage("锁屏收取消息需要允许 " + getApplicationName() + " 在后台高耗电时运行。\n\n" +
									"请点击『确定』，在弹出的『后台高耗电』中，将 " + getApplicationName() + " 对应的开关打开。")
							.setPositiveButton("确定", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									intentWrapper.startActivity(mActivity);
								}
							}).show();
					break;
				case GIONEE:
					new AlertDialog.Builder(this)
							.setCancelable(false)
							.setTitle("" + getApplicationName() + " 需要加入应用自启和绿色后台白名单")
							.setMessage("锁屏收取消息需要允许 " + getApplicationName() + " 的自启动和后台运行。\n\n" +
									"请点击『确定』，在弹出的『系统管家』中，分别找到『应用管理』->『应用自启』和『绿色后台』->『清理白名单』，将 " + getApplicationName() + " 添加到白名单。")
							.setPositiveButton("确定", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									intentWrapper.startActivity(mActivity);
								}
							}).show();
					break;
				case LETV_GOD:
					new AlertDialog.Builder(this)
							.setCancelable(false)
							.setTitle("需要禁止 " + getApplicationName() + " 被自动清理")
							.setMessage("锁屏收取消息需要禁止 " + getApplicationName() + " 被自动清理。\n\n" +
									"请点击『确定』，在弹出的『应用保护』中，将 " + getApplicationName() + " 对应的开关关闭。")
							.setPositiveButton("确定", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									intentWrapper.startActivity(mActivity);
								}
							}).show();
					break;
				case LENOVO:
					new AlertDialog.Builder(this)
							.setCancelable(false)
							.setTitle("需要允许 " + getApplicationName() + " 的后台运行")
							.setMessage("锁屏收取消息需要允许 " + getApplicationName() + " 的后台自启和后台运行。\n\n" +
									"请点击『确定』，在弹出的『后台管理』中，分别找到『后台自启』和『后台运行』，将 " + getApplicationName() + " 对应的开关打开。")
							.setPositiveButton("确定", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									intentWrapper.startActivity(mActivity);
								}
							}).show();
					break;
				case LENOVO_GOD:
					new AlertDialog.Builder(this)
							.setCancelable(false)
							.setTitle("需要关闭 " + getApplicationName() + " 的后台耗电优化")
							.setMessage("锁屏收取消息需要关闭 " + getApplicationName() + " 的后台耗电优化。\n\n" +
									"请点击『确定』，在弹出的『后台耗电优化』中，将 " + getApplicationName() + " 对应的开关关闭。")
							.setPositiveButton("确定", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									intentWrapper.startActivity(mActivity);
								}
							}).show();
					break;

			}
		}
	}

	/**
	 * 传入EditText的id
	 * 没有传入的EditText不做处理
	 *
	 * @return id 数组
	 */
	public int[] hideSoftByEditViewIds() {
		int ids[] = {R.id.etAccount, R.id.etPwd};
		return ids;
	}

	/**
	 * 传入要过滤的View
	 * 过滤之后点击将不会有隐藏软键盘的操作
	 *
	 * @return id 数组
	 */
	public View[] filterViewByIds() {
		return null;
	}

	// 是否触摸在指定view上面，对某个控件过滤
	public boolean isTouchView(View[] views, MotionEvent ev) {
		if (views == null || views.length == 0) return false;
		int[] location = new int[2];
		for (View view : views) {
			view.getLocationOnScreen(location);
			int x = location[0];
			int y = location[1];
			if (ev.getX() > x && ev.getX() < (x + view.getWidth())
					&& ev.getY() > y && ev.getY() < (y + view.getHeight())) {
				return true;
			}
		}
		return false;
	}

	// 是否触摸在指定view上面，对某个控件过滤
	public boolean isTouchView(int[] ids, MotionEvent ev) {
		int[] location = new int[2];
		for (int id : ids) {
			View view = findViewById(id);
			if (view == null) continue;
			view.getLocationOnScreen(location);
			int x = location[0];
			int y = location[1];
			if (ev.getX() > x && ev.getX() < (x + view.getWidth())
					&& ev.getY() > y && ev.getY() < (y + view.getHeight())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 隐藏键盘
	 *
	 * @param v   焦点所在View
	 * @param ids 输入框
	 * @return true 代表焦点在edit上
	 */
	public boolean isFousEditText(View v, int... ids) {
		if (v instanceof EditText) {
			EditText tmp_et = (EditText) v;
			for (int id : ids) {
				if (tmp_et.getId() == id) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 清除editText的焦点
	 *
	 * @param v   焦点所在View
	 * @param ids 输入框
	 */
	public void clearViewFocus(View v, int... ids) {
		if (null != v && null != ids && ids.length > 0) {
			for (int id : ids) {
				if (v.getId() == id) {
					v.clearFocus();
					break;
				}
			}
		}
	}


	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (ev.getAction() == MotionEvent.ACTION_DOWN) {
			if (isTouchView(filterViewByIds(), ev)) return super.dispatchTouchEvent(ev);
			if (hideSoftByEditViewIds() == null || hideSoftByEditViewIds().length == 0)
				return super.dispatchTouchEvent(ev);
			View v = getCurrentFocus();
			if (isFousEditText(v, hideSoftByEditViewIds())) {
				if (isTouchView(hideSoftByEditViewIds(), ev))
					return super.dispatchTouchEvent(ev);
				//隐藏键盘
				hideKeyboard();
				clearViewFocus(v, hideSoftByEditViewIds());
			}

		}

		return super.dispatchTouchEvent(ev);
	}


}
