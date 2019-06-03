package com.easemob.helpdesk.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

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
			InputStream in = context.getResources().getAssets().open("emoji");//
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
	public static void openFile(File f,Activity context) {
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
			Toast.makeText(context.getApplicationContext(), "没有找到打开此类文件的程序", Toast.LENGTH_SHORT).show();
		}
	}

//	public static String getPath(Context context, Uri uri) {
//
//		if ("content".equalsIgnoreCase(uri.getScheme())) {
//			String[] projection = {"_data"};
//			Cursor cursor = null;
//			try {
//				cursor = context.getContentResolver().query(uri, projection, null, null, null);
//				if (cursor == null){
//					return null;
//				}
//				int column_index = cursor.getColumnIndexOrThrow("_data");
//				if (cursor.moveToFirst()) {
//					return cursor.getString(column_index);
//				}
//			} catch (Exception e) {
//				// Eat it
//			}finally {
//				if (cursor != null){
//					cursor.close();
//				}
//			}
//		} else if ("file".equalsIgnoreCase(uri.getScheme())) {
//			return uri.getPath();
//		}
//
//		return null;
//	}

	/**
	 * 复制单个文件
	 * @param oldPath String 原文件路径
	 * @param newPath String 复制后路径
	 * @param fileName String 文件名
	 * @return boolean
	 */
	public static Boolean copyFile(String oldPath, String newPath, String fileName) {
		try {
			int bytesum = 0;
			int byteread = 0;
			File oldfile = new File(oldPath);
			if (oldfile.exists()) { //文件存在时
				File newFile = new File(newPath);

				if (!newFile.exists()) {
					newFile.mkdirs();
				}
				InputStream inStream = new FileInputStream(oldPath); //读入原文件
				newFile = new File(newPath + fileName);
				if (!newFile.exists()) {
					newFile.createNewFile();
				}
				FileOutputStream fs = new FileOutputStream(newFile);
				byte[] buffer = new byte[1444];
				while ( (byteread = inStream.read(buffer)) != -1) {
					bytesum += byteread; //字节数 文件大小
					fs.write(buffer, 0, byteread);
				}
				inStream.close();
			}
		}
		catch (Exception e) {
			Log.e("WorkService","Copy file failed.");
			e.printStackTrace();
			return false;
		}
		return true;
	}


}
