package com.easemob.helpdesk.activity.chat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.easemob.helpdesk.activity.BaseActivity;

/**
 * Created by tiancruyff on 2017/6/2.
 */

public class ImageGridActivity extends BaseActivity {
	private static final String TAG = "ImageGridActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getSupportFragmentManager().findFragmentByTag(TAG) == null) {
			final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.add(android.R.id.content, new ImageGridFragment(), TAG);
			ft.commit();
		}
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

}
