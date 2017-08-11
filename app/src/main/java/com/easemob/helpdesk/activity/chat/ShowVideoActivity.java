package com.easemob.helpdesk.activity.chat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.BaseActivity;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.entity.HDMessage;
import com.hyphenate.kefusdk.utils.HDLog;
import com.hyphenate.util.PathUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 展示视频内容
 *
 */
public class ShowVideoActivity extends BaseActivity {
	private static final String TAG = "ShowVideoActivity";

	private RelativeLayout loadingLayout;
	private ProgressBar progressBar;
	private String localFilePath;
	private HDMessage message;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.hd_showvideo_activity);
		loadingLayout = (RelativeLayout) findViewById(R.id.loading_layout);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		localFilePath = getIntent().getStringExtra("localpath");
		String remotepath = getIntent().getStringExtra("remotepath");
		String secret = getIntent().getStringExtra("secret");
		message = getIntent().getParcelableExtra("videomsg");
		HDLog.d(TAG, "show video view file:" + localFilePath
				+ " remotepath:" + remotepath + " secret:" + secret);
		if (localFilePath != null && new File(localFilePath).exists()) {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(new File(localFilePath)),
					"video/mp4");
			startActivity(intent);
			finish();
		} else if (!TextUtils.isEmpty(remotepath) && !remotepath.equals("null")) {
			HDLog.d(TAG, "download remote video file");
			Map<String, String> maps = new HashMap<String, String>();
			if (!TextUtils.isEmpty(secret)) {
				maps.put("share-secret", secret);
			}
			downloadVideo(remotepath, message , maps);
		} else {

		}
	}

	public String getLocalFilePath(String remoteUrl) {
		String localPath;
		if (remoteUrl.contains("/")) {
			localPath = PathUtil.getInstance().getVideoPath().getAbsolutePath()
					+ "/" + remoteUrl.substring(remoteUrl.lastIndexOf("/") + 1)
					+ ".mp4";
		} else {
			localPath = PathUtil.getInstance().getVideoPath().getAbsolutePath()
					+ "/" + remoteUrl + ".mp4";
		}
		return localPath;
	}

	/**
	 * 播放本地视频
	 *
	 * @param localPath 视频路径
	 */
	private void showLocalVideo(String localPath) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(new File(localPath)),
				"video/mp4");
		startActivity(intent);
		finish();
	}


	/**
	 * 下载视频文件
	 */
	private void downloadVideo(final String remoteUrl, HDMessage message,
	                           final Map<String, String> header) {

		if (TextUtils.isEmpty(localFilePath)) {
			localFilePath = getLocalFilePath(remoteUrl);
		}
		if (new File(localFilePath).exists()) {
			showLocalVideo(localFilePath);
			return;
		}
		loadingLayout.setVisibility(View.VISIBLE);

		message.setMessageCallback(new HDDataCallBack() {
			@Override
			public void onSuccess(Object o) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						loadingLayout.setVisibility(View.GONE);
						progressBar.setProgress(0);
						showLocalVideo(localFilePath);
					}
				});
			}

			@Override
			public void onProgress(final int progress) {
				HDLog.d("ease", "video progress:" + progress);
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						progressBar.setProgress(progress);
					}
				});

			}

			@Override
			public void onError(int error, String msg) {
				HDLog.e("###", "offline file transfer error:" + msg);
				File file = new File(localFilePath);
				if (file.exists()) {
					file.delete();
				}
			}
		});
		HDClient.getInstance().chatManager().downloadAttachment(message);
	}

	@Override
	public void onBackPressed() {
		finish();
	}


}
