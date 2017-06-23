/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.easemob.helpdesk.activity.chat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.BaseActivity;
import com.easemob.helpdesk.utils.CommonUtils;
import com.easemob.helpdesk.widget.photoview.PhotoView;
import com.easemob.helpdesk.widget.photoview.PhotoViewAttacher;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.entity.HDMessage;
import com.hyphenate.kefusdk.entity.HDImageMessageBody;

import java.io.File;
import java.lang.ref.SoftReference;

/**
 * 下载显示大图
 */
public class ShowBigImageActivity extends BaseActivity {

	private PhotoView image;
	private int default_res = R.drawable.default_image;
	private String localFilePath;
	private ProgressBar loadLocalPb;
	private SoftReference<Bitmap> softBitmap = null;
	private String remoteURL = null;
	private HDMessage message;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_big_image);
		image = (PhotoView) findViewById(R.id.image);
		loadLocalPb = (ProgressBar) findViewById(R.id.pb_load_local);
		Intent gIntent = getIntent();
		message = gIntent.getParcelableExtra("message");
		HDImageMessageBody HDImageMessageBody = (HDImageMessageBody) message.getBody();
		localFilePath = HDImageMessageBody.getLocalPath();
		remoteURL = HDImageMessageBody.getRemoteUrl();
		//本地存在，直接显示本地的图片
		if (localFilePath != null && new File(localFilePath).exists()) {
			Bitmap bitmap = CommonUtils.getScaleBitmap(ShowBigImageActivity.this, localFilePath);
			softBitmap = new SoftReference<Bitmap>(bitmap);
			if (softBitmap.get() != null) {
				image.setImageBitmap(softBitmap.get());
			} else {
				image.setImageResource(default_res);
			}
		} else if (remoteURL != null) {
			downloadAttachement();
		} else {
			image.setImageResource(default_res);
		}
		image.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {

			@Override
			public void onPhotoTap(View view, float x, float y) {
				finish();
			}
		});
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (softBitmap != null) {
			softBitmap.clear();
			softBitmap = null;
		}
	}

	private void downloadAttachement() {
		loadLocalPb.setVisibility(View.VISIBLE);
		message.setMessageCallback(new HDDataCallBack() {
			@Override
			public void onSuccess(Object value) {
				if (isFinishing()) {
					return;
				}
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						loadLocalPb.setVisibility(View.GONE);
						Bitmap bitmap = CommonUtils.getScaleBitmap(ShowBigImageActivity.this, localFilePath);
						softBitmap = new SoftReference<Bitmap>(bitmap);
						if (softBitmap.get() != null) {
							image.setImageBitmap(softBitmap.get());
						} else {
							image.setImageResource(default_res);
						}
					}
				});
			}

			@Override
			public void onError(int error, String errorMsg) {
				File localFile = new File(localFilePath);
				if (localFile.exists()) {
					localFile.delete();
				}
				if (isFinishing()) {
					return;
				}
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						loadLocalPb.setVisibility(View.GONE);
						image.setImageResource(default_res);

					}
				});
			}

			@Override
			public void onProgress(final int progress) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						loadLocalPb.setProgress(progress);
					}
				});
			}
		});
		HDClient.getInstance().chatManager().downloadAttachment(message);
	}

	@Override
	public void onBackPressed() {
		finish();
	}
}
