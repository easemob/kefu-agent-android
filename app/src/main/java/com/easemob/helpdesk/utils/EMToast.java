package com.easemob.helpdesk.utils;

import android.content.Context;
import android.graphics.Color;
import android.widget.Toast;

import com.easemob.helpdesk.R;
import com.easemob.helpdesk.styleabletoastlibrary.StyleableToast;

public class EMToast {

	public static StyleableToast makeLoginFailStyleableToast(Context context, String txtError){
		StyleableToast st = new StyleableToast(context, txtError, Toast.LENGTH_SHORT);
		st.setBackgroundColor(Color.parseColor("#fd662b"));
		st.setTextColor(Color.WHITE);
//		st.setCornerRadius(5);
		st.setIcon(R.drawable.icon_attention);
//		st.spinIcon();
		st.setMaxAlpha();
		return st;
	}

	public static StyleableToast makeStyleableToast(Context context, String txtMsg){
		StyleableToast st = new StyleableToast(context, txtMsg, Toast.LENGTH_SHORT);
		st.setBackgroundColor(Color.parseColor("#64c749"));
		st.setTextColor(Color.WHITE);
		st.setIcon(R.drawable.icon_attention);
//		st.spinIcon();
		st.setMaxAlpha();
		return st;
	}

}
