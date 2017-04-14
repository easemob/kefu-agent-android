package com.easemob.helpdesk.utils;

import android.app.Dialog;
import android.content.Context;
import android.widget.TextView;

import com.easemob.helpdesk.R;

public class DialogUtils {

	public static Dialog getLoadingDialog(Context context, CharSequence loadingMsg) {
		Dialog mDialog = new Dialog(context, R.style.progress_dialog);
		mDialog.setContentView(R.layout.dialog);
		mDialog.setCanceledOnTouchOutside(false);
		mDialog.setCancelable(true);
		mDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
		TextView msg = (TextView) mDialog.findViewById(R.id.id_tv_loadingmsg);
		msg.setText(loadingMsg);
		return mDialog;
	}

	public static Dialog getLoadingDialog(Context context, int resid) {
		return getLoadingDialog(context, context.getResources().getText(resid));
	}
}
