package com.easemob.helpdesk.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.ashokvarma.bottomnavigation.BadgeItem;
import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.fragment.ContactFragment;
import com.easemob.helpdesk.fragment.CurrentSessionFragment;
import com.easemob.helpdesk.fragment.MoreFragment;
import com.easemob.helpdesk.utils.HDNotifier;
import com.hyphenate.kefusdk.HDEventListener;
import com.hyphenate.kefusdk.HDNotifierEvent;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.entity.HDMessage;
import com.hyphenate.kefusdk.manager.CloseSessionManager;
import com.hyphenate.kefusdk.manager.CurrentSessionManager;
import com.hyphenate.kefusdk.manager.OverTimeSessionManager;
import com.hyphenate.kefusdk.utils.HDLog;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends BaseActivity implements BottomNavigationBar.OnTabSelectedListener {

	private static final String TAG = "MainActivity";

	private ArrayList<Fragment> fragments;
	private CurrentSessionFragment currentSessionFragment;
	private MoreFragment moreFragment;
	private ContactFragment contactFragment;
	private ViewPager mViewPager;
	private BottomNavigationBar bottomNavigationBar;
	private HDEventListener eventListener;
	public static MainActivity instance = null;
	private BottomNavigationItem chatNavItem;
	private BadgeItem chatNavNumberBadge;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		instance = this;
		mViewPager = (ViewPager) findViewById(R.id.viewpager);
		bottomNavigationBar = (BottomNavigationBar) findViewById(R.id.bottom_navbar);
		bottomNavigationBar.setMode(BottomNavigationBar.MODE_FIXED);
		bottomNavigationBar.setBackgroundStyle(BottomNavigationBar.BACKGROUND_STYLE_STATIC);
		chatNavNumberBadge = new BadgeItem().setBorderWidth(4).setBackgroundColor(Color.RED);
		chatNavItem = new BottomNavigationItem(R.drawable.tab_chat_2,"聊天").setBadgeItem(chatNavNumberBadge);
		bottomNavigationBar.addItem(chatNavItem)
			.addItem(new BottomNavigationItem(R.drawable.tab_contact_2,"我的客户"))
			.addItem(new BottomNavigationItem(R.drawable.tab_more_2,"工具箱"))
				.setFirstSelectedPosition(0)
				.initialise();

		fragments = getFragments();
		bottomNavigationBar.setTabSelectedListener(this);
		mViewPager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
		mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

			}

			@Override
			public void onPageSelected(int position) {
				bottomNavigationBar.selectTab(position);
			}

			@Override
			public void onPageScrollStateChanged(int state) {

			}
		});
		registerMessageListener();
	}

	private void registerMessageListener() {
		if (eventListener == null){
			eventListener = new HDEventListener() {
				@Override
				public void onEvent(final HDNotifierEvent event) {
					final Object data = event.getData();
					runOnUiThread(new Runnable() {
						@Override
						public void run() {

							switch (event.getEvent()){
								case EventNewMessage:
									if (data instanceof HDMessage){
										HDMessage HDMessage = (HDMessage) data;
										if (!HDMessage.isAgentChat()){
											if (CurrentSessionFragment.refreshCallback != null) {
												CurrentSessionFragment.refreshCallback.onRefreshView();
												HDNotifier.getInstance().notifiChatMsg(HDMessage);
											}
										}
									}
									break;

								case EventNewSession:
									if (CurrentSessionFragment.callback != null) {
										HDNotifier.getInstance().notifiChatMsg(null);
										CurrentSessionFragment.callback.onFresh(null);
									}
									break;
								case EventCurrentUserDelete:
//									tipCurrentUserDeleted();
									break;
								case EventSessionClosed:
									OverTimeSessionManager.getInstance().notifyListeners((String) data);
									break;
								case EventMaxSessionsChanged:
//									refreshMaxAccessCount();
									break;
								case EventSessionClosedByAdmin:
									if (data instanceof List){
										List<JSONObject> jsonList = (List<JSONObject>) data;
										if(jsonList != null && !jsonList.isEmpty()){
											for (JSONObject jsonObject : jsonList) {
												try {
													String strBody = jsonObject.getString("body");
													String sSessionId = strBody.substring(0, strBody.indexOf(":"));
													if (sSessionId != null) {
														CloseSessionManager.getInstance().notifyListeners(sSessionId);
													}
												} catch (Exception e) {
													e.printStackTrace();
													HDLog.e(TAG, "ServiceSessionClosedByAdmin error:" + e.getMessage());
												}
											}
										}

									}

									break;
								case EventSessionTransfered:
									if (data instanceof List){
										List<JSONObject> jsonList = (List<JSONObject>) data;
										if (jsonList != null && !jsonList.isEmpty()){
											for (JSONObject jsonObject : jsonList) {
												try {
													String strBody = jsonObject.getString("body");
													String [] splitStr = strBody.split(":");
													String sSessionId = splitStr[1];
													if (sSessionId != null) {
														CloseSessionManager.getInstance().notifyListeners(sSessionId);
													}
												} catch (Exception e) {
													e.printStackTrace();
													HDLog.e(TAG, "ServiceSessionTransfered error:" + e.getMessage());
												}
											}
										}
									}
									break;
							}

						}
					});

				}
			};
		}
		HDClient.getInstance().chatManager().addEventListener(eventListener);
	}


	private ArrayList<Fragment> getFragments() {
		ArrayList<Fragment> fragments = new ArrayList<>();
		if (currentSessionFragment == null){
			currentSessionFragment = new CurrentSessionFragment();
		}
		if (contactFragment == null){
			contactFragment = new ContactFragment();
		}
		if (moreFragment == null){
			moreFragment = new MoreFragment();
		}
		fragments.add(currentSessionFragment);
		fragments.add(contactFragment);
		fragments.add(moreFragment);
		return fragments;
	}


	@Override
	public void onTabSelected(int position) {
		mViewPager.setCurrentItem(position);
	}

	@Override
	public void onTabUnselected(int position) {

	}

	@Override
	public void onTabReselected(int position) {

	}

	public void refreshSessionUnreadCount() {
		int unReadCount = CurrentSessionManager.getInstance().getTotalUnReadCount();
		if (unReadCount > 0) {
			if (unReadCount > 99) {
				chatNavNumberBadge.setText("99+");
			} else {
				chatNavNumberBadge.setText(String.valueOf(unReadCount));
			}
			chatNavNumberBadge.show();
		} else {
			chatNavNumberBadge.hide();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		instance = null;
		if (eventListener != null){
			HDClient.getInstance().chatManager().removeEventListener(eventListener);
		}
	}

	private class MyPagerAdapter extends FragmentPagerAdapter {
		public MyPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			return fragments.get(position);
		}

		@Override
		public int getCount() {
			return fragments.size();
		}

	}
}
