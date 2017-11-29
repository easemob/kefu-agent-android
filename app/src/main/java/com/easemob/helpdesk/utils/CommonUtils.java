package com.easemob.helpdesk.utils;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.graphics.Point;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.text.TextUtils;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.easemob.helpdesk.R;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.entity.user.HDMessageUser;
import com.hyphenate.kefusdk.entity.user.HDUser;
import com.hyphenate.kefusdk.utils.HDLog;
import com.hyphenate.kefusdk.utils.PathUtil;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@SuppressWarnings("UnnecessaryLocalVariable")
public class CommonUtils {

	private static final String TAG = CommonUtils.class.getSimpleName();
	private static final String[][] MIME_MapTable={
			//{后缀名，MIME类型}
			{".3gp", "video/3gpp"},
			{".apk", "application/vnd.android.package-archive"},
			{".asf", "video/x-ms-asf"},
			{".avi", "video/x-msvideo"},
			{".bin", "application/octet-stream"},
			{".bmp", "image/bmp"},
			{".c", "text/plain"},
			{".class", "application/octet-stream"},
			{".conf", "text/plain"},
			{".cpp", "text/plain"},
			{".doc", "application/msword"},
			{".docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"},
			{".xls", "application/vnd.ms-excel"},
			{".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"},
			{".exe", "application/octet-stream"},
			{".gif", "image/gif"},
			{".gtar", "application/x-gtar"},
			{".gz", "application/x-gzip"},
			{".h", "text/plain"},
			{".htm", "text/html"},
			{".html", "text/html"},
			{".jar", "application/java-archive"},
			{".java", "text/plain"},
			{".jpeg", "image/jpeg"},
			{".jpg", "image/jpeg"},
			{".js", "application/x-JavaScript"},
			{".log", "text/plain"},
			{".m3u", "audio/x-mpegurl"},
			{".m4a", "audio/mp4a-latm"},
			{".m4b", "audio/mp4a-latm"},
			{".m4p", "audio/mp4a-latm"},
			{".m4u", "video/vnd.mpegurl"},
			{".m4v", "video/x-m4v"},
			{".mov", "video/quicktime"},
			{".mp2", "audio/x-mpeg"},
			{".mp3", "audio/x-mpeg"},
			{".mp4", "video/mp4"},
			{".mpc", "application/vnd.mpohun.certificate"},
			{".mpe", "video/mpeg"},
			{".mpeg", "video/mpeg"},
			{".mpg", "video/mpeg"},
			{".mpg4", "video/mp4"},
			{".mpga", "audio/mpeg"},
			{".msg", "application/vnd.ms-outlook"},
			{".ogg", "audio/ogg"},
			{".pdf", "application/pdf"},
			{".png", "image/png"},
			{".pps", "application/vnd.ms-powerpoint"},
			{".ppt", "application/vnd.ms-powerpoint"},
			{".pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation"},
			{".prop", "text/plain"},
			{".rc", "text/plain"},
			{".rmvb", "audio/x-pn-realaudio"},
			{".rtf", "application/rtf"},
			{".sh", "text/plain"},
			{".tar", "application/x-tar"},
			{".tgz", "application/x-compressed"},
			{".txt", "text/plain"},
			{".wav", "audio/x-wav"},
			{".wma", "audio/x-ms-wma"},
			{".wmv", "audio/x-ms-wmv"},
			{".wps", "application/vnd.ms-works"},
			{".xml", "text/plain"},
			{".z", "application/x-compress"},
			{".zip", "application/x-zip-compressed"},
			{"", "*/*"}
	};

	/**
	 * 检测网络是否可用
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isNetWorkConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
			if (mNetworkInfo != null) {
				return mNetworkInfo.isAvailable();
			}
		}

		return false;
	}

	/**
	 * 检测Sdcard是否存在
	 * 
	 * @return
	 */
	public static boolean isExitsSdcard() {
		return android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
	}
	
	public static boolean isAppRunningForeground(Context ctx) {
		ActivityManager activityManager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> tasks = activityManager.getRunningTasks(1);
        return ctx.getPackageName().equalsIgnoreCase(tasks.get(0).baseActivity.getPackageName());
    }

	public static String getTopActivity(Context context) {
		ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1);

		if (runningTaskInfos != null)
			return runningTaskInfos.get(0).topActivity.getClassName();
		else
			return "";
	}

	public static Bitmap convertBitmap(Bitmap oldBitmap, int reqWidth, int reqHeight) {
		// 获取图片的宽高
		int width = oldBitmap.getWidth();
		int height = oldBitmap.getHeight();

		float scaleWidth;
		float scaleHeight;

		if (width < height) {
			if (height < reqHeight) {
				int newHeight = reqHeight;
				float newWidth = width * (((float) reqHeight) / height);
				// 计算缩放比例
				scaleWidth = newWidth / width + 1;
				scaleHeight = ((float) newHeight) / height + 1;
			} else {
				// 设置想要的大小
				 int newWidth = reqWidth;
				float newHeight = height * (((float) reqWidth) / width);
				// 计算缩放比例
				scaleWidth = ((float) newWidth) / width;
				scaleHeight = newHeight / height;
			}
		} else {
			if (width < reqWidth) {
				// 设置想要的大小
				int newWidth = reqWidth;
				float newHeight = height * (((float) reqWidth) / width);
				// 计算缩放比例
				scaleWidth = ((float) newWidth) / width + 1;
				scaleHeight = newHeight / height + 1;
			} else {
				// 设置想要的大小
				int newHeight = reqHeight;
				float newWidth = width * (((float) reqHeight) / height);
				// 计算缩放比例
				scaleWidth = newWidth / width;
				scaleHeight = ((float) newHeight) / height;
			}
		}
		// 取得想要缩放的matrix参数
		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
		try {
			// 得到新的图片
			return Bitmap.createBitmap(oldBitmap, 0, 0, width, height, matrix, true);
		} catch (Exception e) {
			e.printStackTrace();
			HDLog.e(TAG, "newbm:" + e.getMessage());
		}
		return oldBitmap;
	}

	// 转换dip为px
	public static int convertDip2Px(Context context, int dip) {
		float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dip * scale + 0.5f * (dip >= 0 ? 1 : -1));
	}

	public static int convertDip2Px(Context context, float dip) {
		float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dip * scale + 0.5f * (dip >= 0 ? 1 : -1));
	}

	// 转换px为dip
	public static int convertPx2Dip(Context context, int px) {
		float scale = context.getResources().getDisplayMetrics().density;
		return (int) (px / scale + 0.5f * (px >= 0 ? 1 : -1));
	}

	/**
	 * get bitmap options
	 * 
	 * @param imagePath
	 * @return
	 */
	public static Options getBitmapOptions(String imagePath) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(imagePath, options);
		return options;
	}

	/**
	 * 读取图片属性：旋转的角度
	 * 
	 * @param path
	 *            图片绝对路径
	 * @return degree旋转的角度
	 */
	public static int readPictureDegree(String path) {
		int degree = 0;
		try {
			ExifInterface exifInterface = new ExifInterface(path);
			int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);
			switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				degree = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				degree = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				degree = 270;
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return degree;
	}

	/*
	 * 旋转图片
	 * 
	 * @param angle
	 * 
	 * @param bitmap
	 * 
	 * @return Bitmap
	 */
	public static Bitmap rotaingImageView(int angle, Bitmap bitmap) {
		// 旋转图片 动作
		Matrix matrix = new Matrix();
		matrix.postRotate(angle);
		// 创建新的图片
		return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
	}

	public static Bitmap decodeScaleImage(String imagePath, int reqWidth, int reqHeight) {
		// First decode with inJustDecodeBounds=true to check dimensions
		BitmapFactory.Options options = getBitmapOptions(imagePath);

		// Calculate inSampleSize
		int sampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
		HDLog.d("img", "original wid" + options.outWidth + " original height:" + options.outHeight + " sample:"
				+ sampleSize);
		options.inSampleSize = sampleSize;

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		Bitmap bm = BitmapFactory.decodeFile(imagePath, options);
		// 图片旋转角度
		int degree = readPictureDegree(imagePath);
		Bitmap rotateBm;
		if (bm != null && degree != 0) {
			rotateBm = rotaingImageView(degree, bm);
			bm.recycle();
			return rotateBm;
		} else {
			return bm;
		}
		// return BitmapFactory.decodeFile(imagePath, options);
	}

	public static Bitmap decodeScaleImage(Context context, int drawableId, int reqWidth, int reqHeight) {
		Bitmap bitmap;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;

		BitmapFactory.decodeResource(context.getResources(), drawableId, options);
		int sampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
		options.inSampleSize = sampleSize;
		options.inJustDecodeBounds = false;

		bitmap = BitmapFactory.decodeResource(context.getResources(), drawableId, options);
		return bitmap;
	}

	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			// Calculate ratios of height and width to requested height and
			// width
			final int heightRatio = Math.round((float) height / (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);

			// Choose the smallest ratio as inSampleSize value, this will
			// guarantee
			// a final image with both dimensions larger than or equal to the
			// requested height and width.
			inSampleSize = heightRatio > widthRatio ? heightRatio : widthRatio;
		}

		return inSampleSize;
	}

	
	
//	private static boolean isConnectionFast(int type, int subType) {
//	    if(type==ConnectivityManager.TYPE_WIFI){
//            return true;
//        }else if(type==ConnectivityManager.TYPE_MOBILE){
//            switch(subType){
//            case TelephonyManager.NETWORK_TYPE_1xRTT:
//                return false; // ~ 50-100 kbps
//            case TelephonyManager.NETWORK_TYPE_CDMA:
//                return false; // ~ 14-64 kbps
//            case TelephonyManager.NETWORK_TYPE_EDGE:
//                return false; // ~ 50-100 kbps
//            case TelephonyManager.NETWORK_TYPE_EVDO_0:
//                return true; // ~ 400-1000 kbps
//            case TelephonyManager.NETWORK_TYPE_EVDO_A:
//                return true; // ~ 600-1400 kbps
//            case TelephonyManager.NETWORK_TYPE_GPRS:
//                return false; // ~ 100 kbps
//            case TelephonyManager.NETWORK_TYPE_HSDPA:
//                return true; // ~ 2-14 Mbps
//            case TelephonyManager.NETWORK_TYPE_HSPA:
//                return true; // ~ 700-1700 kbps
//            case TelephonyManager.NETWORK_TYPE_HSUPA:
//                return true; // ~ 1-23 Mbps
//            case TelephonyManager.NETWORK_TYPE_UMTS:
//                return true; // ~ 400-7000 kbps
//            }
//
//            if(android.os.Build.VERSION.SDK_INT >= 11) {
//                if (subType == TelephonyManager.NETWORK_TYPE_EHRPD || //1-2Mbps
//                        subType == TelephonyManager.NETWORK_TYPE_LTE) { //10+ Mbps
//                    return true;
//                }
//            }
//            if (android.os.Build.VERSION.SDK_INT >= 9) {
//                if (subType == TelephonyManager.NETWORK_TYPE_EVDO_B) { // 5Mbps
//                    return true;
//                }
//            }
//
//            if (android.os.Build.VERSION.SDK_INT >= 8) {
//                if (subType == TelephonyManager.NETWORK_TYPE_IDEN) { // 25 kbps
//                    return false;
//                }
//            }
//        }
//        return false;
//	}
	
	

//	public static int getDownloadBufSize(Context context) {
//        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo info = cm.getActiveNetworkInfo();
//        if (info != null && info.getType() == ConnectivityManager.TYPE_WIFI) {
//            return 1024*100;
//        }
//        if (info != null && isConnectionFast(info.getType(), info.getSubtype())) {
//            return 1024*30;
//        }
//        return 2024;
//    }
//	public static String getImagePath(String remoteUrl) {
//		String imageName= remoteUrl.substring(remoteUrl.lastIndexOf("/") + 1, remoteUrl.length());
//		String path = PathUtil.getInstance().getImagePath()+"/"+imageName;
//        HDLog.d("msg", " image path:" + path);
//        return path;
//    }
//
//
//	public static String getVoicePath(String remoteUrl){
//		String voiceName= remoteUrl.substring(remoteUrl.lastIndexOf("/") + 1, remoteUrl.length());
//		String path =PathUtil.getInstance().getVoicePath()+"/"+voiceName;
//        HDLog.d(TAG, " voice path:" + path);
//        return path;
//	}

	public static String getFilePath(String remoteUrl, String fileName) {
		String filePath = remoteUrl.substring(remoteUrl.lastIndexOf("/") + 1, remoteUrl.length());
		String path = PathUtil.getInstance().getFilePath() + "/" + filePath;
		if (!TextUtils.isEmpty(fileName) && fileName.contains(".")) {
			path = path + "." + fileName.substring(fileName.lastIndexOf(".") + 1);
		}
		return path;
	}

	public static String getAvatarPath(String remoteUrl) {
		String avatarImageName= remoteUrl.substring(remoteUrl.lastIndexOf("/") + 1, remoteUrl.length());
		String path =PathUtil.getInstance().getAvatarPath()+"/"+avatarImageName;
		path = path.replace(":","-").replace("?","-").replace("|","");
		HDLog.d(TAG, " avatar image path:" + path);
		return path;
	}

	/**
	 * 判断消息是否来自访客
	 * @param fromUser
	 * @return
	 */
	public static boolean isVisiorChat(HDMessageUser fromUser){
		if(fromUser == null || fromUser.getUserType() == null){
			return false;
		}else if(fromUser.getUserType().equals("Visitor")) {
			HDUser loginUser = HDClient.getInstance().getCurrentUser();
			return loginUser != null && !fromUser.getUserId().equals(loginUser.getUserId());
		}else{
			return false;
		}
	}
	
	public static boolean  isSendDirect(HDMessageUser fromUser, boolean isAgentChat){
		if(isAgentChat) {
			if (fromUser == null || fromUser.getUserType() == null) {
				return false;
			}
			HDUser loginUser = HDClient.getInstance().getCurrentUser();
			return loginUser != null && fromUser.getUserId().equals(loginUser.getUserId());
		}else{
			if (fromUser == null || fromUser.getUserType() == null) {
				return false;
			} else if (fromUser.getUserType().equals("Agent")) {
				return true;
			} else if (fromUser.getUserType().equals("Visitor")) {
				HDUser loginUser = HDClient.getInstance().getCurrentUser();
				return loginUser != null && fromUser.getUserId().equals(loginUser.getUserId());
			} else
				return fromUser.getUserType().equals("Scheduler") || fromUser.getUserType().equals("Robot");
		}
	}
	
	public static String convertStringByMessageText(String messageText){
		return messageText.replaceAll("&lt;", "<").replaceAll("&#39;", "'").replaceAll("&amp;","&");
	}
	
//	/**
//     * 获取控件的高度，如果获取的高度为0，则重新计算尺寸后再返回高度
//     *
//     * @param view
//     * @return
//     */
//    public static int getViewMeasuredHeight(View view) {
//        calcViewMeasure(view);
//        return view.getMeasuredHeight();
//    }
    /**
//     * 测量控件的尺寸
//     *
//     * @param view
//     */
//    public static void calcViewMeasure(View view) {
//        int width = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
//        int expandSpec = View.MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, View.MeasureSpec.AT_MOST);
//        view.measure(width, expandSpec);
//    }

//
//    public static String getLogFileName(){
//    	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
//		return format.format(new Date(System.currentTimeMillis()));
//    }
//
//    public static String getDateEN(){
//    	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
//		return format.format(new Date(System.currentTimeMillis()));
//    }
//
//
//    public static int getVersionCode(Context context){
//		try {
//			PackageManager manager = context.getPackageManager();
//			PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
//			return info.versionCode;
//		} catch (Exception ignored) {
//		}
//		return 0;
//    }
//
    
    
    
    
    @SuppressLint("NewApi")
	public static Bitmap getScaleBitmap(Context ctx, String filePath){
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inJustDecodeBounds = true;
		Bitmap bmp = BitmapFactory.decodeFile(filePath, opt);
		
		int bmpWidth = opt.outWidth;
		int bmpHeight = opt.outHeight;
		
		WindowManager windowManager = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
		Display display = windowManager.getDefaultDisplay();
		int screenWidth;
		int screenHeight;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			Point size = new Point();
			display.getSize(size);
			screenWidth = size.x;
			screenHeight = size.y;
		} else {
			screenWidth = display.getWidth();
			screenHeight = display.getHeight();
		}
		opt.inSampleSize = 1;
		if(bmpWidth > bmpHeight){
			if(bmpWidth>screenWidth){
				opt.inSampleSize = bmpWidth / screenWidth;
			} 
		}else{
			if(bmpHeight > screenHeight){
				opt.inSampleSize = bmpHeight / screenHeight;
			}
		}
		opt.inJustDecodeBounds = false;
		try{
			bmp = BitmapFactory.decodeFile(filePath, opt);
		}catch (OutOfMemoryError error){
			bmp = BitmapFactory.decodeFile(filePath);
		}

		try{
			Bitmap rotateBmp = rotateImage(new File(filePath), bmp);
			return rotateBmp;
		}catch (Exception e){
			return bmp;
		}
	}

	private static Bitmap rotateImage(final File file, Bitmap b){
		try {
			ExifInterface ei = new ExifInterface(file.getPath());
			int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
			Matrix matrix = new Matrix();
			switch (orientation){
				case ExifInterface.ORIENTATION_ROTATE_90:
					matrix.postRotate(90);
					b = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
					break;
				case ExifInterface.ORIENTATION_ROTATE_180:
					matrix.postRotate(180);
					b = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
					break;
				case ExifInterface.ORIENTATION_ROTATE_270:
					matrix.postRotate(270);
					b = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
					break;
				default:
					b = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
					break;
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return b;
	}

	/**
	 * 获取apk的版本名称 currentVersionName
	 * @param context
	 * @return
	 */
	public static String getAppVersionNameFromApp(Context context) {
		PackageManager manager = context.getPackageManager();
		try {
			PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
			return info.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}


//	public static boolean isRobotMenuMessage(HDMessage messageEntity){
//		JSONObject jsonExt = messageEntity.extJson;
//		if(jsonExt == null || !jsonExt.has("msgtype")){
//			return false;
//		}
//		try {
//			JSONObject jsonMsgType = jsonExt.getJSONObject("msgtype");
//			if(jsonMsgType == null || !jsonMsgType.has("choice")){
//				return false;
//			}
//			JSONObject jsonChoice = jsonMsgType.getJSONObject("choice");
//			if(jsonChoice == null){
//				return false;
//			}
//			if(jsonChoice.has("items") || jsonChoice.has("list")){
//				return true;
//			}
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//
//		return false;
//	}

	public static String getTitleFromUrlParam(String url) {
		if (TextUtils.isEmpty(url)) {
			return null;
		}
		String paramString = url.substring(url.indexOf("?") + 1, url.length());
		if (TextUtils.isEmpty(paramString)) {
			return null;
		}
		String[] paramPairs = paramString.split("&");
		if (paramPairs.length > 0) {
			for (String item : paramPairs) {
				String key = item.split("=")[0];
				if (key.equalsIgnoreCase("title")) {
					return item.split("=")[1];
				}
			}
		}
		return null;
	}

	public static String getEllipeString(String validateStr) {
		int AllLength = 6;
		StringBuilder sb = new StringBuilder();
		int valueLength = 0;
		String chinese = "[\u0391-\uFFE5]";
		/* 获取字段值的长度，如果含中文字符，则每个中文字符长度为2，否则为1 */
		for (int i = 0; i < validateStr.length(); i++) {
			/* 获取一个字符 */
			String temp = validateStr.substring(i, i + 1);
			/* 判断是否为中文字符 */
			if (temp.matches(chinese)) {
				/* 中文字符长度为2 */
				valueLength += 2;
				AllLength -= 2;
			} else {
				/* 其他字符长度为1 */
				valueLength += 1;
				AllLength -= 1;
			}
			if(AllLength >= 0){
				sb.append(temp);
			}

		}
		if(valueLength > AllLength){
			return sb.append("...").toString();
		}

		return validateStr;
	}

	public static void setAgentStatusView(ImageView imageView, String status){
		if (imageView == null){
			return;
		}
		if (TextUtils.isEmpty(status) || status.equals("Hidden")){
			imageView.setImageResource(R.drawable.hiding);
		}else if (status.equals("Online")){
			imageView.setImageResource(R.drawable.free);
		}else if (status.equals("Busy")){
			imageView.setImageResource(R.drawable.busy);
		}else if (status.equals("Leave")){
			imageView.setImageResource(R.drawable.leave);
		}else if(status.equals("Offline")){
			imageView.setImageResource(R.drawable.state_gray);
		}else {
			imageView.setImageResource(R.drawable.hiding);
		}
	}

	public static void setAgentStatusTextView(TextView textView, String status){
		if (textView == null){
			return;
		}
		if (TextUtils.isEmpty(status) || status.equals("Hidden")){
			textView.setText("隐身");
		}else if (status.equals("Online")){
			textView.setText("空闲");
		}else if (status.equals("Busy")){
			textView.setText("忙碌");
		}else if (status.equals("Leave")){
			textView.setText("离开");
		}else if(status.equals("Offline")){
			textView.setText("离线");
		}else{
			textView.setText("隐身");
		}
	}

	public static int getAgentStatus(String status){
		int intState = 0;
		if (TextUtils.isEmpty(status)){
			return intState;
		}else if (status.equals("Offline")){
			intState = 4;
		}else if (status.equals("Leave")){
			intState = 3;
		}else if (status.equals("Busy")){
			intState = 2;
		}else if (status.equals("Hidden")){
			intState = 1;
		}else if (status.equals("Online")){
			intState = 0;
		}
		return intState;
	}


	/**
	 * 将字符串转成MD5值
	 * @param string
	 * @return
	 */
	public static String stringToMD5(String string) {
		byte[] hash;
		try {
			hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}

		StringBuilder hex = new StringBuilder(hash.length * 2);
		for (byte b : hash) {
			if ((b & 0xFF) < 0x10)
				hex.append(0);
			hex.append(Integer.toHexString(b & 0xFF));
		}
		return hex.toString();
	}


	/**
	 * 根据文件后缀名获得对应的MIME类型。
	 * @param file
	 */
	public static String getMIMEType(File file){
		String type="*/*";
		String fName = file.getName();
		//获取后缀名前的分隔符"."在fName中的位置。
		int dotIndex = fName.lastIndexOf(".");
		if(dotIndex < 0){
			return type;
		}
        /* 获取文件的后缀名*/
		String end=fName.substring(dotIndex,fName.length()).toLowerCase();
		if(end.equals(""))return type;
		//在MIME和文件类型的匹配表中找到对应的MIME类型。
		for(int i=0;i<MIME_MapTable.length;i++){ //MIME_MapTable??在这里你一定有疑问，这个MIME_MapTable是什么？
			if(end.equals(MIME_MapTable[i][0]))
				type = MIME_MapTable[i][1];
		}
		return type;
	}

}



