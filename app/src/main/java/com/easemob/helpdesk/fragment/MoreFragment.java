package com.easemob.helpdesk.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.LoginActivity;
import com.easemob.helpdesk.utils.DialogUtils;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.chat.HDClient;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by liyuzhao on 05/04/2017.
 */

public class MoreFragment extends Fragment {

	private Dialog dialog;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_more, null);
		ButterKnife.bind(this, view);
		return view;
	}

	@OnClick(R.id.exit)
	public void onClickByExit(){
		if (dialog == null){
			dialog = DialogUtils.getLoadingDialog(getContext(), "");
			dialog.setCanceledOnTouchOutside(false);
		}
		dialog.show();
		HDClient.getInstance().logout(new HDDataCallBack() {
			@Override
			public void onSuccess(Object o) {
				if (getActivity() == null){
					return;
				}
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						hideDialog();
						Intent intent = new Intent();
						intent.setClass(getContext(), LoginActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(intent);
						getActivity().finish();
					}
				});
			}

			@Override
			public void onError(int i, String s) {
				if (getActivity() == null){
					return;
				}
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						hideDialog();
						Toast.makeText(getContext(), "退出失败", Toast.LENGTH_SHORT).show();
					}
				});
			}

			@Override
			public void onAuthenticationException() {
				if (getActivity() == null){
					return;
				}
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						hideDialog();
						Toast.makeText(getContext(), "退出失败", Toast.LENGTH_SHORT).show();
					}
				});
			}
		});

	}

	private void hideDialog(){
		if (dialog != null && dialog.isShowing()){
			dialog.dismiss();
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		hideDialog();
	}
}
