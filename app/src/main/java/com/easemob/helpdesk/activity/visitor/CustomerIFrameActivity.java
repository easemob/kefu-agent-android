package com.easemob.helpdesk.activity.visitor;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import com.easemob.helpdesk.AppConfig;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.BaseActivity;
import com.easemob.jsbridge.BridgeHandler;
import com.easemob.jsbridge.BridgeWebView;
import com.easemob.jsbridge.CallBackFunction;
import com.easemob.jsbridge.DefaultHandler;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.utils.HDLog;

/**
 * author liyuzhao
 * email:liyuzhao@easemob.com
 * date: 13/06/2018
 */

public class CustomerIFrameActivity extends BaseActivity {

	private static String TAG = CustomerIFrameActivity.class.getSimpleName();

	private TextView tvTitle;
	/**
	 * 文件选择请求码 (android5.0以下)
	 */
	public final static int FILECHOOSER_RESULTCODE = 1;
	/**
	 * 文件选择请求码 (android5.0包括及以上)
	 */
	public final static int FILECHOOSER_RESULTCODE_FOR_ANDROID_5 = 2;
	/**
	 * WebView 自定义支持更多的功能
	 */
	private BridgeWebView mWebView;

	/**
	 * 选择文件数据集（5.0以下）
	 */
	public ValueCallback<Uri> mUploadMessage;
	/**
	 * 选择文件数据集(5.0包含及以下)
	 */
	public ValueCallback<Uri[]> mUploadMessageForAndroid5;

	/**
	 * 请求的远程URL
	 */
	private String url;

	private View mBack;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AppConfig.setFitWindowMode(this);
		setContentView(R.layout.activity_iframe);
		Intent getIntent = getIntent();
		url = getIntent.getStringExtra("url");
		initViews();
		initListeners();
		initData();
	}

	@SuppressLint("SetJavaScriptEnabled")
	private void initViews(){
		tvTitle = findViewById(R.id.tv_title);
		mBack = findViewById(R.id.iv_back);
		mWebView = (BridgeWebView) findViewById(R.id.webView);
		//设置编码
		mWebView.getSettings().setDefaultTextEncodingName("utf-8");
		//支持js
		mWebView.getSettings().setJavaScriptEnabled(true);
		//设置背景颜色  透明
		mWebView.setBackgroundColor(Color.argb(0, 0, 0, 0));
		//设置本地调用对象及其接口
//        mWebView.addJavascriptInterface(new JavaScriptObject(this),"demo");

		//载入js
//        mWebView.loadUrl("file:///android_asset/demo.html");
		mWebView.getSettings().setAppCacheEnabled(false);
		mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
		mWebView.getSettings().setDomStorageEnabled(true);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
			mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		}

		mWebView.loadUrl(url);
		//点击调用js中方法
		mWebView.setDefaultHandler(new DefaultHandler());

		mWebView.setWebChromeClient(new WebChromeClient() {
			@SuppressWarnings("unused")
			public void openFileChooser(ValueCallback<Uri> uploadMsg, String AcceptType, String capture) {
				this.openFileChooser(uploadMsg);
			}

			@SuppressWarnings("unused")
			public void openFileChooser(ValueCallback<Uri> uploadMsg, String AcceptType) {
				this.openFileChooser(uploadMsg);
			}

			public void openFileChooser(ValueCallback<Uri> uploadMsg) {
				mUploadMessage = uploadMsg;
				pickFile();
			}

			//For Android > 5.0
			public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> uploadMsg, WebChromeClient.FileChooserParams fileChooserParams) {
				openFileChooserImplForAndroid5(uploadMsg);
				return true;
			}

			@Override
			public void onReceivedTitle(WebView view, String title) {
				super.onReceivedTitle(view, title);
			}
		});

		mWebView.registerHandler("sendExtMsg", new BridgeHandler() {

			@Override
			public void handler(String data, CallBackFunction function) {
				HDLog.d(TAG, "handler = sendExtMsg, data from web = " + data);
				if (data != null && data.equals("recoveryTitle")) {
					return;
				}
				setResult(RESULT_OK, new Intent().putExtra("ext", data));
				finish();
			}

		});

		mWebView.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if(event.getAction() == KeyEvent.ACTION_DOWN){
					if(keyCode == KeyEvent.KEYCODE_BACK && mWebView.canGoBack()){
						mWebView.goBack();
						return true;
					}
				}
				return false;
			}
		});
	}

	private void initListeners(){
		mBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}


	private void initData(){
		String iframeTitle = HDClient.getInstance().visitorManager().getIFrameTabTitle();
		if (iframeTitle != null){
			tvTitle.setText(iframeTitle);
		}
	}

	/**
	 * 筛选文件 （5.0以下）
	 */
	public void pickFile() {
		Intent chooserIntent = new Intent(Intent.ACTION_GET_CONTENT);
		chooserIntent.setType("image/*");
		startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);
	}

	/**
	 * 筛选文件 (5.0包含及以上)
	 * @param uploadMsg
	 */
	private void openFileChooserImplForAndroid5(ValueCallback<Uri[]> uploadMsg){
		mUploadMessageForAndroid5 = uploadMsg;
		Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
		contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
		contentSelectionIntent.setType("image/*");

		Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
		chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
		chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");

		startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE_FOR_ANDROID_5);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == FILECHOOSER_RESULTCODE) {
			if (null == mUploadMessage) {
				return;
			}
			Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
			mUploadMessage.onReceiveValue(result);
			mUploadMessage = null;
		}else if(requestCode == FILECHOOSER_RESULTCODE_FOR_ANDROID_5){
			if(null == mUploadMessageForAndroid5)
				return;
			Uri result = (intent == null || resultCode != RESULT_OK) ? null : intent.getData();
			if(result != null){
				mUploadMessageForAndroid5.onReceiveValue(new Uri[]{result});
			}else{
				mUploadMessageForAndroid5.onReceiveValue(new Uri[]{});
			}
			mUploadMessageForAndroid5 = null;
		}
	}


}
