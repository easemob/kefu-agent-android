package com.easemob.helpdesk.mvp;

import android.Manifest;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.ClipboardManager;
import android.widget.Toast;

import com.easemob.helpdesk.AppConfig;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.BaseActivity;
import com.easemob.helpdesk.activity.chat.ImageGridActivity;
import com.easemob.helpdesk.adapter.ChatAdapter;
import com.easemob.helpdesk.emoticon.data.AppBean;
import com.easemob.helpdesk.image.ImageHandleUtils;
import com.easemob.helpdesk.recorder.MediaManager;
import com.easemob.helpdesk.utils.CommonUtils;
import com.easemob.helpdesk.utils.DialogUtils;
import com.easemob.helpdesk.utils.FaceConversionUtil;
import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.entity.HDMessage;
import com.hyphenate.kefusdk.entity.HDMessageUser;
import com.hyphenate.kefusdk.entity.HDTextMessageBody;
import com.hyphenate.kefusdk.manager.session.SessionManager;
import com.hyphenate.kefusdk.utils.PathUtil;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import kr.co.namee.permissiongen.PermissionFail;
import kr.co.namee.permissiongen.PermissionGen;
import kr.co.namee.permissiongen.PermissionSuccess;
import me.iwf.photopicker.PhotoPickerActivity;

/**
 * Created by liyuzhao on 02/05/2017.
 */

public abstract class BaseChatActivity extends BaseActivity {

	protected static final String TAG = "BaseChatActivity";

	//打开相册
	private static final int REQUEST_CODE_CHOOSE_PICTURE = 0x001;

	public static final int REQUEST_CODE_SELECT_FILE = 0x002;

	private static final int REQUEST_CODE_PERMISSIONS_CAMERA = 0x003;

	private static final int REQUEST_CODE_PERMISSIONS_RECORD = 0x004;

	private static final int REQUEST_CODE_PERMISSIONS_CAMERA_VIDEO = 0x005;

	private static final int REQUEST_CODE_SELECT_VIDEO = 0x006;

	//重发的RequestCode
	public static final int REQUEST_CODE_RESEND = 0x010;

	//copy message for contextmenu
	public static final int REQUEST_CODE_CONTEXT_MENU = 0x011;

	public static final int RESULT_CODE_COPY_AND_PASTE = 0x012;
	public static final int RESULT_CODE_RECALL = 0x013;


	/**
	 * 会话ID
	 */
	protected String sessionId = null;
	/**
	 * 当前会话是否有未读消息
	 */
	protected boolean hasUnReadMessage;

	/**
	 * 当前会话toUser（目标用户）
	 */
	protected HDMessageUser toUser;
	/**
	 * 渠道来源
	 */
	protected String originType;
	/**
	 * 渠道名称
	 */
	protected String techChannelName;

	/**
	 * 加载中Dialog
	 */
	protected Dialog pd = null;

	protected SessionManager sessionManager;

	public ChatAdapter mAdapter;

	/**
	 * 需要重发的消息位置
	 */
	public static int resendPos;

	protected Unbinder unbinder;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//加载Emoji表情图标
		FaceConversionUtil.getInstace().getFileText(getApplication());
		AppConfig.setFitWindowMode(this);
		setContentView();
		unbinder = ButterKnife.bind(this);
		if (!CommonUtils.isNetWorkConnected(this)) {
			Toast.makeText(this, "当前无网络!", Toast.LENGTH_SHORT).show();
		}

		checkVoicePermission();
	}

	public  abstract void setContentView();

	public abstract ArrayList<AppBean> getExtendAppBeans();

	public String getOriginType(){
		return originType;
	}


	private void checkVoicePermission(){
		int hasRecordPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
		if (hasRecordPermission != PackageManager.PERMISSION_GRANTED){
			PermissionGen.with(this)
					.addRequestCode(REQUEST_CODE_PERMISSIONS_RECORD)
					.permissions(Manifest.permission.RECORD_AUDIO
					).request();
		}
	}

	@PermissionSuccess(requestCode = REQUEST_CODE_PERMISSIONS_RECORD)
	public void recordAuthSuccess(){}

	@PermissionFail(requestCode = REQUEST_CODE_PERMISSIONS_RECORD)
	public void recordAuthFail(){
		new android.app.AlertDialog.Builder(this).setMessage("app需要手机录音权限 \n请在权限管理->麦克风->设为允许!").setPositiveButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
				Uri uri = Uri.fromParts("package", getPackageName(), null);
				intent.setData(uri);
				startActivity(intent);
			}
		}).create().show();

	}

	//============= send message start ================
	protected void sendText(String txtContent) {
		sendText(txtContent, null);
	}

	/**
	 * 发送带有扩展的自定义文本消息
	 *
	 * @param txtContent 文本内容
	 * @param extJson    扩展消息（JSON格式）
	 */
	protected void sendText(String txtContent, JSONObject extJson) {
		HDMessage message = HDMessage.createSendTextMessage(txtContent);
		if (extJson != null) {
			message.setExtJson(extJson);
		}
		sessionManager.sendMessage(message);
		mAdapter.refreshSelectLast();
	}

	protected void sendFileMessage(String filePath){
		if (filePath == null){
			return;
		}
		File file = new File(filePath);
		if (!file.exists()) {
			Toast.makeText(this, getString(R.string.toast_file_no_exist), Toast.LENGTH_SHORT).show();
			return;
		}
		if (file.length() > 10 * 1024 * 1024) {
			Toast.makeText(this, getString(R.string.toast_file_nomore_count), Toast.LENGTH_SHORT).show();
			return;
		}
		//创建一个文件消息
		HDMessage message = HDMessage.createSendFileMessage(filePath);
		sessionManager.sendMessage(message);
		mAdapter.refreshSelectLast();
		setResult(RESULT_OK);

	}

	/**
	 * 发送语音消息
	 */
	protected void sendVoiceMessage(final int seconds, final String filePath) {
		if (isFinishing()){
			return;
		}
		HDMessage message = HDMessage.createSendVoiceMessage(filePath, seconds);
		sessionManager.sendMessage(message);
		mAdapter.refreshSelectLast();
		setResult(RESULT_OK);
	}

	/**
	 * 发送图片消息
	 */
	protected void sendPicture(final String filePath) {
		if (isFinishing()){
			return;
		}
		HDMessage message = HDMessage.createSendImageMessage(filePath);
		sessionManager.sendMessage(message);
		mAdapter.refreshSelectLast();
		setResult(RESULT_OK);
	}

	/**
	 * 发送视频消息
	 */
	protected void sendVideoMessage(String videoPath, String thumbPath, int videoLength) {
		HDMessage message = HDMessage.createVideoSendMessage(videoPath, thumbPath, videoLength);
		sessionManager.sendMessage(message);
		mAdapter.refreshSelectLast();
	}
	/**
	 * 重发消息
	 */
	protected void resendMessage() {
		sessionManager.resendMessage(resendPos);
		mAdapter.refreshSelectLast();
	}

	//============= send sendMessagemessage end ================


	/**
	 * 文字copy
	 * @param context
	 * @param content copy的文字
	 */
	protected  void copyText(Context context, String content) {
		if (Build.VERSION.SDK_INT >= 11) {
			android.text.ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
			clipboardManager.setText(content);
		} else {
			android.content.ClipboardManager clipboardManager = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
			clipboardManager.setPrimaryClip(ClipData.newPlainText(null, content));
		}

	}


	public void selectPicFromLocal() {
		int hasRecordPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
		if (hasRecordPermission != PackageManager.PERMISSION_GRANTED){
			PermissionGen.with(this)
					.addRequestCode(REQUEST_CODE_PERMISSIONS_CAMERA)
					.permissions(Manifest.permission.CAMERA
					).request();
		}else{
			selectPicAuthSuccess();
		}
	}

	public void selectVideoFromLocal() {
		int hasRecordPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
		if (hasRecordPermission != PackageManager.PERMISSION_GRANTED){
			PermissionGen.with(this)
					.addRequestCode(REQUEST_CODE_PERMISSIONS_CAMERA_VIDEO)
					.permissions(Manifest.permission.CAMERA
					).request();
		}else{
			selectVideoAuthSuccess();
		}
	}


	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		PermissionGen.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
//		switch(requestCode){
//			case FilePickerDialog.EXTERNAL_READ_PERMISSION_GRANT:
//			{
//				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED){
////                    if (dialog != null){
////                        // Show dialog if the read permission has been granted.
////                        dialog.show();
////                    }
//				}else{
//					// Permission has not been granted. Notify the user.
//					Toast.makeText(mActivity, "无权限", Toast.LENGTH_SHORT).show();
//				}
//			}
//		}
//		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}

	@PermissionSuccess(requestCode = REQUEST_CODE_PERMISSIONS_CAMERA_VIDEO)
	public void selectVideoAuthSuccess() {
		Intent intent = new Intent(BaseChatActivity.this, ImageGridActivity.class);
		startActivityForResult(intent, REQUEST_CODE_SELECT_VIDEO);
	}

	@PermissionSuccess(requestCode = REQUEST_CODE_PERMISSIONS_CAMERA)
	public void selectPicAuthSuccess(){
		//打开相册新方法
		Intent intent = ImageHandleUtils.pickSingleImage(this, true);
		this.startActivityForResult(intent, REQUEST_CODE_CHOOSE_PICTURE);
	}

	@PermissionFail(requestCode = REQUEST_CODE_PERMISSIONS_CAMERA | REQUEST_CODE_PERMISSIONS_CAMERA_VIDEO)
	public void selectPicAuthFail(){
		new android.app.AlertDialog.Builder(this).setMessage("拍照需要相机权限 \n请在权限管理->相机->设为允许!").setPositiveButton("确定", new DialogInterface.OnClickListener() {
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
	 * 选择文件
	 */
	public void selectFileFromLocal() {
//        Intent intent;
//        if (Build.VERSION.SDK_INT < 19) {
//            intent = new Intent(Intent.ACTION_GET_CONTENT);
//            intent.setType("*/*");
//            intent.addCategory(Intent.CATEGORY_OPENABLE);
//        } else {
//            intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        }
//        startActivityForResult(intent, REQUEST_CODE_SELECT_FILE);

		selectFileFromLocalNew();
	}

	public void selectFileFromLocalNew(){
		DialogProperties properties = new DialogProperties();
		properties.selection_mode = DialogConfigs.SINGLE_MODE;
		properties.selection_type = DialogConfigs.FILE_SELECT;
		properties.root = Environment.getExternalStorageDirectory();
		properties.error_dir = Environment.getExternalStorageDirectory();
		properties.offset = new File(DialogConfigs.DEFAULT_DIR);
		properties.extensions = null;

		FilePickerDialog dialog = new FilePickerDialog(this, properties);
		dialog.setTitle("选择要发送的文件");
		dialog.setDialogSelectionListener(new DialogSelectionListener() {
			@Override
			public void onSelectedFilePaths(String[] files) {
				// files is the array of the paths of files selected by the Application User.
				if (files != null && files.length > 0){
					for (String filePath : files){
						sendFileMessage(filePath);
					}
				}
			}
		});
		dialog.show();
	}



	/**
	 * 发送文件消息
	 */
	protected void sendFile(Uri uri) {
		String filePath = null;
		if ("content".equalsIgnoreCase(uri.getScheme())) {
			String[] projection = {"_data"};
			Cursor cursor = null;
			try {
				cursor = getContentResolver().query(uri, projection, null, null, null);
				if (cursor != null){
					int column_index = cursor.getColumnIndexOrThrow("_data");
					if (cursor.moveToFirst()) {
						filePath = cursor.getString(column_index);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}finally {
				if (cursor != null){
					cursor.close();
				}
			}
		} else if ("file".equalsIgnoreCase(uri.getScheme())) {
			filePath = uri.getPath();
		}

		sendFileMessage(filePath);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CODE_CONTEXT_MENU) {
			if (resultCode == RESULT_CODE_COPY_AND_PASTE) {
				HDMessage copyMsg = mAdapter.getItem(data.getIntExtra("position", -1));
				if (copyMsg.getType() == HDMessage.Type.TXT){//只支持文本消息的Copy
					copyText(mActivity, ((HDTextMessageBody) copyMsg.getBody()).getMessage());
				}
			}else if (resultCode == RESULT_CODE_RECALL){
				closeDialog();
				pd = DialogUtils.getLoadingDialog(this, R.string.info_recalling);
				pd.show();
				final int position = data.getIntExtra("position", -1);
				if (position == -1){
					return;
				}
				final HDMessage recallMsg = mAdapter.getItem(position);
				sessionManager.asyncRecallMessage(recallMsg, new HDDataCallBack<HDMessage>() {
					@Override
					public void onSuccess(HDMessage value) {
						if (isFinishing()) {
							return;
						}
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								closeDialog();
								mAdapter.notifyItemChanged(position);
								Toast.makeText(mActivity, "消息撤回成功！", Toast.LENGTH_SHORT).show();
							}
						});
					}

					@Override
					public void onError(int error, final String errorMsg) {
						if (isFinishing()) {
							return;
						}
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								closeDialog();
								Toast.makeText(mActivity, "消息撤回失败！" + errorMsg, Toast.LENGTH_SHORT).show();
							}
						});
					}
				});
			}
		}
		if (resultCode == RESULT_OK) {
			if (requestCode == REQUEST_CODE_CHOOSE_PICTURE) {
				if (data != null) {
					ArrayList<String> picPathList = data.getStringArrayListExtra(PhotoPickerActivity.KEY_SELECTED_PHOTOS);
					if (picPathList == null || picPathList.size() == 0) {
						return;
					}
					String picPath = picPathList.get(0);
					sendPicture(picPath);
				}
			} else if (requestCode == REQUEST_CODE_RESEND) {
				resendMessage();
			} else if (requestCode == REQUEST_CODE_SELECT_VIDEO) {
				if (data != null) {
					int duration = data.getIntExtra("dur", 0);
					String videoPath = data.getStringExtra("path");
					File file = new File(PathUtil.getInstance().getImagePath(), "thvideo" + System.currentTimeMillis());
					try {
						FileOutputStream fos = new FileOutputStream(file);
						Bitmap ThumbBitmap = ThumbnailUtils.createVideoThumbnail(videoPath, 3);
						ThumbBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
						fos.close();
						sendVideoMessage(videoPath, file.getAbsolutePath(), duration);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * 加载Dialog关闭
	 */
	protected void closeDialog() {
		if (pd != null && pd.isShowing()) {
			pd.dismiss();
		}
	}



	@Override
	protected void onResume() {
		super.onResume();
		MediaManager.resume();
		if (mAdapter != null) {
			mAdapter.refresh();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		MediaManager.pause();
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
		closeDialog();
		if(unbinder != null){
			unbinder.unbind();
		}
		if (sessionManager != null){
			sessionManager.clear();
		}
		MediaManager.release();

	}
}
