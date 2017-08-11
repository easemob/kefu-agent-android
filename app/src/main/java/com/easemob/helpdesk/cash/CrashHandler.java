package com.easemob.helpdesk.cash;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.easemob.helpdesk.HDApplication;
import com.hyphenate.kefusdk.utils.HDLog;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Date;

public class CrashHandler implements UncaughtExceptionHandler {

	private static final String TAG = CrashHandler.class.getSimpleName();

	private static CrashHandler instance;

	private Context mContext;

	public static CrashHandler getInstance() {
		if (instance == null) {
			instance = new CrashHandler();
		}
		return instance;
	}

	public void init(Context appContext) {
		HDLog.i(TAG, "init()");
		this.mContext = appContext;
		// 设置该类为线程默认UncatchException的处理器
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	/**
	 * 当UncaughtException发生时会回调该函数处理
	 */
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		HDLog.e(TAG, "system wrong...");
		HDLog.e(TAG, ex.getMessage());
		collectCrashInfoToFile(thread, ex);

		// 判断是否为UI异常， thread.getId()==1 为UI线程
		if (thread.getId() != 1) {
			HDLog.d(TAG, "Exception ThreadId " + thread.getId());
			thread.interrupt();
		} else {
			HDApplication.getInstance().logout();
		}
	}

	private void collectCrashInfoToFile(Thread thread, Throwable ex) {

		// 异常信息收集
		// 应用程序信息收集
		// 保存错误报告文件到文件
		String logDir;
		if (Environment.getExternalStorageDirectory() != null) {
			logDir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "emkefu"
					+ File.separator + "log";

			File file = new File(logDir);
			boolean mkSuccess;
			if (!file.isDirectory()) {
				mkSuccess = file.mkdirs();
				if (!mkSuccess) {
					//noinspection UnusedAssignment
					mkSuccess = file.mkdirs();
				}
			}
			File errFile = new File(logDir + File.separator + "error.log");
			try {
				if (file.exists()) {
					if (file.length() > 5 * 1024 * 1024) {
						file.delete();
					}
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}

			try {
				FileWriter fw = new FileWriter(errFile, true);
				fw.write("-------------------------------------------------------------------");
				fw.write(new Date() + "\n");
				StackTraceElement[] stackTrace = ex.getStackTrace();
				fw.write(ex.getMessage() + "\n");
				for (int i = 0; i < stackTrace.length; i++) {
					fw.write("file:" + stackTrace[i].getFileName() + " class:" + stackTrace[i].getClassName()
							+ " method:" + stackTrace[i].getMethodName() + " line:" + stackTrace[i].getLineNumber()
							+ "\n");
				}
				fw.write("\n");
				fw.close();
			} catch (IOException e) {
				Log.e(TAG, "load file faild...", e.getCause());
			}

		}

	}
}
