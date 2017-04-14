package com.easemob.helpdesk.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.utils.HDLog;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.FileNameMap;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FileUtils {
	private static final String TAG = "FileUtis";
	/**
	 * 读取表情配置文件
	 * 
	 * @param context
	 * @return
	 */
	public static List<String> getEmojiFile(Context context) {
		try {
			List<String> list = new ArrayList<String>();
			InputStream in = context.getResources().getAssets().open("com/sj/emoji");//
			BufferedReader br = new BufferedReader(new InputStreamReader(in,
					"UTF-8"));
			String str;
			while ((str = br.readLine()) != null) {
				list.add(str);
			}

			return list;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Determine the type of file
	 * @param f
	 * @return
	 */
	public static String getMIMEType(File f) {
		String fName = f.getName();
		String end = fName.substring(fName.lastIndexOf(".") + 1, fName.length()).toLowerCase();
		return MimeTypeMap.getSingleton().getMimeTypeFromExtension(end);
	}

	public static String getMIMEType(String fileName) {
		String end = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length()).toLowerCase();
		return MimeTypeMap.getSingleton().getMimeTypeFromExtension(end);
	}

	/**
	 * open file
	 * @param f
	 * @param context
	 */
	public static void openFile(File f, Activity context) {
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(android.content.Intent.ACTION_VIEW);
        /* get MimeType */
		String type = FileUtils.getMIMEType(f);
        /* set intent's file and MimeType */
		intent.setDataAndType(Uri.fromFile(f), type);
		try {
			context.startActivity(intent);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Toast.makeText(context, "没有找到打开此类文件的程序", Toast.LENGTH_SHORT).show();
		}
	}



	public static void asyncformUpload(final String urlStr, final Map<String, String> textMap, final Map<String, String> fileMap, final HDDataCallBack callBack){
		new Thread(new Runnable() {
			@Override
			public void run() {
				formUpload(urlStr,textMap,fileMap,callBack);
			}
		}).start();
	}




	/**
	 * 上传图片
	 * @param urlStr
	 * @param textMap
	 * @param fileMap
	 * @return
	 */
	public static void formUpload(String urlStr, Map<String, String> textMap, Map<String, String> fileMap, HDDataCallBack callBack) {
		String res;
		HttpURLConnection conn = null;
//		String BOUNDARY = "---------------------------123821742118716"; //boundary就是request头和上传文件内容的分隔符
		String BOUNDARY = UUID.randomUUID().toString();
		String PREFIX = "--",LINED = "\r\n";
		try {
			URL url = new URL(urlStr);
			conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(30000);
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestProperty("Charset","UTF-8");
			conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);

			OutputStream out = new DataOutputStream(conn.getOutputStream());
			// text
			if (textMap != null) {
				StringBuilder strBuf = new StringBuilder();
				for (Map.Entry<String, String> entry : textMap.entrySet()) {
					String inputName = entry.getKey();
					String inputValue = entry.getValue();
					if (inputValue == null) {
						continue;
					}
					strBuf.append(PREFIX).append(BOUNDARY).append(LINED);
					strBuf.append("Content-Disposition: form-data; name=\"").append(inputName).append("\"").append(LINED);
//					strBuf.append("Content-Type:text/plain; charset=utf-8" + LINED);
//					strBuf.append("Content-Transfer-Encoding:binary" + LINED);
					strBuf.append(LINED);
					strBuf.append(inputValue);
					strBuf.append(LINED);
				}
				out.write(strBuf.toString().getBytes());
			}

			// file
			if (fileMap != null) {
				for (Map.Entry<String, String> entry : fileMap.entrySet()) {
					String inputName = entry.getKey();
					String inputValue = entry.getValue();
					if (inputValue == null) {
						continue;
					}
					File file = new File(inputValue);
					String filename = file.getName();
//					MagicMatch match = Magic.getMagicMatch(file, false, true);
//					String contentType = match.getMimeType();
//					String contentType="image/jpeg";

					String strBuf = PREFIX + BOUNDARY + LINED +
							"Content-Disposition: form-data; name=\"" + inputName + "\"; filename=\"" + filename + "\"" + LINED +
							"Content-Type: " + guessMimeType(file.getPath()) + LINED +
							LINED;
//					strBuf.append(LINED);
					out.write(strBuf.getBytes());

					DataInputStream in = new DataInputStream(new FileInputStream(file));
					int bytes;
					byte[] bufferOut = new byte[1024];
					while ((bytes = in.read(bufferOut)) != -1) {
						out.write(bufferOut, 0, bytes);
					}
					in.close();
				}
			}

			byte[] endData = (LINED + PREFIX + BOUNDARY + PREFIX + LINED).getBytes();
			out.write(endData);
			out.flush();
			out.close();
			int responseCode = conn.getResponseCode();
			HDLog.d(TAG, "responseCode:" + responseCode);
			InputStream inputStream;
			if(responseCode >= 200 && responseCode <= 204){
				inputStream = conn.getInputStream();
			}else{
				inputStream = conn.getErrorStream();
			}

			// 读取返回数据
			StringBuilder strBuf = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			String line;
			while ((line = reader.readLine()) != null) {
				strBuf.append(line).append("\n");
			}
			res = strBuf.toString();
			HDLog.d(TAG,"res:" + res);
			reader.close();

			if(responseCode == 200){
				if(callBack != null){
					callBack.onSuccess(res);
				}
			}else{
				if(callBack != null){
					callBack.onError(responseCode, res);
				}
			}


		} catch (Exception e) {
			HDLog.e(TAG,"发送POST请求出错。" + urlStr);
			e.printStackTrace();
			if(callBack != null){
				callBack.onError(-1,e.getMessage());
			}
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
	}


	public static String guessMimeType(String path) {
		FileNameMap fileNameMap = URLConnection.getFileNameMap();
		String contentTypeFor = fileNameMap.getContentTypeFor(path);
		if (contentTypeFor == null) {
			contentTypeFor = "application/octet-stream";
		}
		return contentTypeFor;
	}


	public static String getPath(Context context, Uri uri) {

		if ("content".equalsIgnoreCase(uri.getScheme())) {
			String[] projection = {"_data"};
			Cursor cursor = null;
			try {
				cursor = context.getContentResolver().query(uri, projection, null, null, null);
				if (cursor == null){
					return null;
				}
				int column_index = cursor.getColumnIndexOrThrow("_data");
				if (cursor.moveToFirst()) {
					return cursor.getString(column_index);
				}
			} catch (Exception e) {
				// Eat it
			}finally {
				if (cursor != null){
					cursor.close();
				}
			}
		} else if ("file".equalsIgnoreCase(uri.getScheme())) {
			return uri.getPath();
		}

		return null;
	}

}
