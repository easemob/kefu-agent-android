package com.easemob.helpdesk.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.LruCache;
import android.text.TextUtils;
import android.widget.ImageView;

import com.easemob.helpdesk.HDApplication;
import com.easemob.helpdesk.R;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.entity.HDMessage;
import com.hyphenate.kefusdk.entity.user.HDUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * Created by tiancruyff on 2017/7/10.
 */

public class AvatarManager {
	private volatile static AvatarManager instance = null;
	public LruCache<String, Bitmap> mMemoryCache;
	private static final int DISK_CACHE_SIZE = 1024 * 1024 * 20; // 20MB
	private static final String DISK_CACHE_SUBDIR = "diskCache";
	public DiskLruCache mDiskCache;
	/** 图片请求列表，用于存放已发送的请求。 */
	private Hashtable<String ,ImageRef> mRequestQueue = new Hashtable<>();

	@Deprecated
	public static AvatarManager getInstance(Context appContext) {
		return getInstance();
	}

	public static AvatarManager getInstance() {
		if (instance == null){
			synchronized (AvatarManager.class){
				if (instance == null){
					instance = new AvatarManager();
				}
			}
		}
		return instance;
	}

	/**
	 * 私有构造函数，保证单例模式
	 */
	private AvatarManager() {
		Context appContext = HDClient.getInstance().getContext();
		int memClass = ((ActivityManager) appContext
				.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
		memClass = memClass > 32 ? 32 : memClass;
		// 使用可用内存的1/64作为图片缓存
		final int cacheSize = 1024 * 1024 * memClass / 64;

		mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {

			protected int sizeOf(String key, Bitmap bitmap) {
				return bitmap.getRowBytes() * bitmap.getHeight();
			}
		};

		File cacheDir = DiskLruCache
				.getDiskCacheDir(appContext, DISK_CACHE_SUBDIR);
		mDiskCache = DiskLruCache.openCache(appContext, cacheDir, DISK_CACHE_SIZE);

	}

	/**
	 * 存放图片信息
	 */
	class ImageRef {
		/** 显示Image的Activity*/
		Activity activity;
		/** 图片对应ImageView控件 */
		List<ImageView> imageViews = new ArrayList<>();
		/** 图片URL地址 */
		String url;
		/** 图片缓存路径 */
		String filePath;

		/**
		 * 构造函数
		 * @param imageView
		 * @param url
		 * @param filePath
		 */
		ImageRef(ImageView imageView, String url, String filePath, Activity activity) {
			this.imageViews.add(imageView);
			this.url = url;
			this.filePath = filePath;
			this.activity = activity;
		}

		/**
		 * 增加将显示下载中图片的View
		 * @param imageView
		 */
		public synchronized void addImageView(ImageView imageView) {
			this.imageViews.add(imageView);
		}

		/**
		 * 所以的view显示获取的bitmap
		 * @param bitmap
		 */
		public synchronized void setImageViews(Bitmap bitmap) {
			for (ImageView iv : imageViews) {
				iv.setImageBitmap(bitmap);
			}
		}

		/**
		 * 显示默认的图标
		 */
		public synchronized void setDefaultImageViews() {
			for (ImageView iv : imageViews) {
				iv.setImageResource(R.drawable.default_agent_avatar);
			}
		}
	}

	/**
	 * 获取消息中扩展的头像图标
	 * @param message
	 * @param activity
	 * @param imageView
	 * @return
	 */
	public synchronized boolean asyncGetMessageAvatar(HDMessage message, Activity activity, ImageView imageView) {
		if (imageView == null) {
			return false;
		}

		JSONObject msgExt = message.getExtJson();
		if (msgExt == null || !msgExt.has("weichat")) {
			return false;
		}

		try {
			JSONObject weichat = msgExt.getJSONObject("weichat");

			if (weichat == null || !weichat.has("agent") || weichat.isNull("agent")) {
				return false;
			}

			JSONObject agent = weichat.getJSONObject("agent");

			if (agent == null || !agent.has("avatar")) {
				return false;
			}

			String remoteUrl = recombineUrl(agent.getString("avatar"));

			if (TextUtils.isEmpty(remoteUrl)) {
				return false;
			}

			asyncGetAvatar(imageView, remoteUrl, activity);
		} catch (JSONException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public String recombineUrl(String oriUrl) {
		String remoteUrl = oriUrl;
		if (TextUtils.isEmpty(remoteUrl) || remoteUrl.equals("null")) {
			return "";
		}
		if (remoteUrl.startsWith("//")) {
			remoteUrl = "http:" + remoteUrl;
		}

		if(remoteUrl.contains("/images/uikit/")){
			return "";
		}
		if (remoteUrl.startsWith("/v1/Tenant/")) {
			remoteUrl = HDClient.getInstance().getKefuServerAddress() + remoteUrl;
		}

		return remoteUrl;
	}

	/**
	 * 获取当前用户的头像图标
	 * @param activity
	 * @param imageView
	 */
	public synchronized void refreshAgentAvatar(Activity activity, ImageView imageView) {
		if (imageView == null){
			return;
		}

		if (HDApplication.getInstance().avatarBitmap != null && !HDApplication.getInstance().avatarBitmap.isRecycled()) {
			imageView.setImageBitmap(HDApplication.getInstance().avatarBitmap);
			return;
		}

		HDUser loginUser = HDClient.getInstance().getCurrentUser();
		if (loginUser == null) {
			imageView.setImageResource(R.drawable.default_agent_avatar);
			return;
		}

		String remoteUrl = recombineUrl(loginUser.getAvatar());

		if(TextUtils.isEmpty(remoteUrl) || remoteUrl.contains("/images/uikit/")){
			imageView.setImageResource(R.drawable.default_agent_avatar);
			return;
		}

		// 从内存cache获取
		Bitmap bitmap = mMemoryCache.get(remoteUrl);
		if (bitmap != null) {
			HDApplication.getInstance().avatarBitmap = bitmap;
			setImageBitmap(imageView, bitmap, false);
			return;
		}

		// 从DiskCache获取
		bitmap = mDiskCache.get(remoteUrl);
		if (bitmap != null) {
			if (mMemoryCache.get(remoteUrl) == null) {
				mMemoryCache.remove(remoteUrl);
				mMemoryCache.put(remoteUrl, bitmap);
			}
			HDApplication.getInstance().avatarBitmap = bitmap;
			setImageBitmap(imageView, bitmap, false);
			return;
		}

		// 查看请求列表是否含有这个请求
		if (mRequestQueue.containsKey(remoteUrl)) {
			mRequestQueue.get(remoteUrl).addImageView(imageView);
			return;
		}


		// 生成文件名
		String filePath = CommonUtils.getAvatarPath(remoteUrl);
		if (filePath == null) {
			return;
		}

		asyncGetAvatar(new ImageRef(imageView, remoteUrl, filePath, activity));
	}


	/**
	 * 获取头像
	 * @param imageView
	 * @param url
	 * @param activity
	 */
	public void asyncGetAvatar(ImageView imageView, String url, Activity activity) {
		if (url == null || url.equals("")) {
			return;
		}

		// 从内存cache获取
		Bitmap bitmap = mMemoryCache.get(url);
		if (bitmap != null && bitmap.isRecycled()) {
			setImageBitmap(imageView, bitmap, false);
			return;
		}

		// 从DiskCache获取
		bitmap = mDiskCache.get(url);
		if (bitmap != null) {
			if (mMemoryCache.get(url) == null) {
				mMemoryCache.put(url, bitmap);
			}
			setImageBitmap(imageView, bitmap, false);
			return;
		}

		// 查看请求列表是否含有这个请求
		if (mRequestQueue.containsKey(url)) {
			mRequestQueue.get(url).addImageView(imageView);
			return;
		}


		// 生成文件名
		String filePath = CommonUtils.getAvatarPath(url);
		if (filePath == null) {
			return;
		}

		asyncGetAvatar(new ImageRef(imageView, url, filePath, activity));
	}


	private synchronized void asyncGetAvatar(final ImageRef imageRef) {

		mRequestQueue.put(imageRef.url, imageRef);

		final File localFile = new File(imageRef.filePath);

		HDClient.getInstance().visitorManager().downloadFile(imageRef.filePath, imageRef.url, new HDDataCallBack<String>() {
			@Override
			public void onSuccess(String value) {
				mRequestQueue.remove(imageRef.url);
				if (localFile.exists()) {
					if (imageRef.activity == null || imageRef.activity.isFinishing()) {
						return;
					}
					imageRef.activity.runOnUiThread(new Runnable() {
						@Override
						public synchronized void run() {
							Bitmap bitmap = BitmapFactory.decodeFile(imageRef.filePath);
							if(bitmap != null) {
								mMemoryCache.put(imageRef.url, bitmap);
								mDiskCache.put(imageRef.url, bitmap);
								imageRef.setImageViews(bitmap);
								mRequestQueue.remove(imageRef.url);
							}
						}
					});
				}
			}

			@Override
			public void onError(int error, String errorMsg) {
				mRequestQueue.remove(imageRef.url);
				File file = new File(imageRef.filePath);
				if (file.exists()) {
					file.delete();
				}
				if (imageRef.activity == null || imageRef.activity.isFinishing()) {
					return;
				}
				imageRef.activity.runOnUiThread(new Runnable() {
					@Override
					public synchronized void run() {
						imageRef.setDefaultImageViews();
					}
				});
			}

		});

	}


	/**
	 * 添加图片显示渐现动画
	 *
	 */
	private void setImageBitmap(ImageView imageView, Bitmap bitmap,
	                            boolean isTran) {
//		if (isTran) {
//			final TransitionDrawable td = new TransitionDrawable(
//					new Drawable[] {
//							new ColorDrawable(application.getResources().getColor(android.R.color.transparent)),
//							new BitmapDrawable(bitmap) });
//			td.setCrossFadeEnabled(true);
//			imageView.setImageDrawable(td);
//			td.startTransition(300);
//		} else {
			imageView.setImageBitmap(bitmap);
//		}
	}

	public void clearDiskCache() {
		mDiskCache.clearCache();
	}
}
