package com.easemob.helpdesk.activity.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easemob.helpdesk.R;
import com.easemob.helpdesk.utils.AvatarManager;
import com.easemob.helpdesk.mvp.MainActivity;
import com.easemob.helpdesk.utils.CommonUtils;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.entity.user.HDUser;
import com.hyphenate.kefusdk.manager.main.LeaveMessageManager;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by tiancruyff on 2017/5/16.
 */

public class LeaveMessageGroupFragment extends Fragment {

	@BindView(R.id.iv_avatar)
	protected ImageView ivAvatar;
	@BindView(R.id.iv_status)
	protected ImageView ivStatus;

	@BindView(R.id.my_open_item_count)
	protected TextView myOpenTicketsCounts;
	@BindView(R.id.pending_item_count)
	protected TextView pendingTicketsCounts;
	@BindView(R.id.my_solved_item_count)
	protected TextView mySolvedTicketsCounts;
	@BindView(R.id.unassigned_item_count)
	protected TextView unassignedTicketsCounts;
	@BindView(R.id.custom_fitter_item_count)
	protected TextView customFilterTicketsCounts;

	@BindView(R.id.ticket_my_open)
	protected LinearLayout myOpenTicketsLayout;
	@BindView(R.id.ticket_pending)
	protected LinearLayout pendingTicketsLayout;
	@BindView(R.id.ticket_my_solved)
	protected LinearLayout mysolvedTicketsLayout;
	@BindView(R.id.ticket_unassigned)
	protected LinearLayout unassignedTicketsLayout;
	@BindView(R.id.ticket_custom_fitter)
	protected LinearLayout customFilterTicketsLayout;

	private int openedLeaveMessageCount;

	private Unbinder unbinder;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_ticket_new, container, false);
		unbinder = ButterKnife.bind(this, view);
		return view;
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		loadFirstStatus();
		refreshAgentAvatar();
	}

	@Override
	public void onResume() {
		super.onResume();
		refleshTicketsCount();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		if (unbinder != null){
			unbinder.unbind();
		}
	}

	@OnClick({R.id.ticket_my_open, R.id.ticket_pending, R.id.ticket_my_solved, R.id.ticket_unassigned, R.id.ticket_custom_fitter})
	public void layoutClicks(View view) {
		Intent i = new Intent(getActivity(), LeaveMessageActivity.class);
		switch (view.getId()) {
			case R.id.ticket_pending:
				i.putExtra("Title", "处理中留言");
				i.putExtra("statusIdIndex", LeaveMessageManager.pendingTicketStatusId);
				break;
			case R.id.ticket_my_solved:
				i.putExtra("Title", "已解决留言");
				i.putExtra("statusIdIndex", LeaveMessageManager.solvedTicketStatusId);
				break;
			case R.id.ticket_my_open:
				i.putExtra("Title", "未处理留言");
				i.putExtra("statusIdIndex", LeaveMessageManager.openedTicketStatusId);
				break;
			case R.id.ticket_unassigned:
				i.putExtra("Title", "未分配留言");
				i.putExtra("statusIdIndex", LeaveMessageManager.noTicketStatusId);
				break;
			case R.id.ticket_custom_fitter:
				i.putExtra("Title", "自定义留言筛选");
				i.putExtra("CustomMode", true);
				break;
		}
		startActivity(i);
	}


	public void refreshAgentAvatar() {
		if(ivAvatar != null)
			AvatarManager.getInstance(getContext()).refreshAgentAvatar(getActivity(), ivAvatar);
	}

	public void refreshOnline(String status) {
		CommonUtils.setAgentStatusView(ivStatus, status);
	}

	private void loadFirstStatus() {
		HDUser loginUser = HDClient.getInstance().getCurrentUser();
		if (loginUser != null) {
			refreshOnline(loginUser.getOnLineState());
		}
	}

	private void refleshTicketsCount() {
		getOpenTicketsCount();
		getPendingTicketsCount();
		getSolvedTicketsCount();
		getUnassigneeTicketsCount();
		getCustomFilterTicketsCount();
	}

    private void getPendingTicketsCount() {


	    pendingTicketsLayout.setClickable(false);

	    HDClient.getInstance().leaveMessageManager().getPendingTicketsCount(new HDDataCallBack<String>() {
		    @Override
		    public void onSuccess(final String value) {
			    if (getActivity() == null) {
				    return;
			    }
			    getActivity().runOnUiThread(new Runnable() {
				    @Override
				    public void run() {
					    if (pendingTicketsCounts != null) {
						    pendingTicketsCounts.setText(value);
					    }
					    if (pendingTicketsLayout != null) {
						    pendingTicketsLayout.setClickable(true);
					    }
				    }
			    });
		    }

		    @Override
		    public void onError(int error, String errorMsg) {

		    }
	    });

    }

    private void getSolvedTicketsCount() {
	    mysolvedTicketsLayout.setClickable(false);

	    HDClient.getInstance().leaveMessageManager().getSolvedTicketsCount(new HDDataCallBack<String>() {
		    @Override
		    public void onSuccess(final String value) {
			    if (getActivity() == null) {
				    return;
			    }
			    getActivity().runOnUiThread(new Runnable() {
				    @Override
				    public void run() {
					    if (mySolvedTicketsCounts != null) {
						    mySolvedTicketsCounts.setText(value);
					    }
					    if (mysolvedTicketsLayout != null) {
						    mysolvedTicketsLayout.setClickable(true);
					    }
				    }
			    });
		    }

		    @Override
		    public void onError(int error, String errorMsg) {

		    }
	    });
    }

    private void getOpenTicketsCount() {

	    myOpenTicketsLayout.setClickable(false);

	    HDClient.getInstance().leaveMessageManager().getOpenTicketsCount(new HDDataCallBack<String>() {
		    @Override
		    public void onSuccess(final String value) {
			    openedLeaveMessageCount = Integer.parseInt(value);
			    if (getActivity() == null) {
				    return;
			    }
		        getActivity().runOnUiThread(new Runnable() {
			        @Override
			        public void run() {
				        ((MainActivity)getActivity()).refreshOpenedLeaveMessageCount();
				        if (myOpenTicketsCounts != null) {
					        myOpenTicketsCounts.setText(value);
				        }
				        if (myOpenTicketsLayout != null) {
					        myOpenTicketsLayout.setClickable(true);
				        }
			        }
		        });
		    }

		    @Override
		    public void onError(int error, String errorMsg) {

		    }
	    });
    }

    public int getOpenTicketsCountResult() {
	    return openedLeaveMessageCount;
    }

    private void getUnassigneeTicketsCount() {
	    unassignedTicketsLayout.setClickable(false);

	    HDClient.getInstance().leaveMessageManager().getUnassignedTicketCounts(new HDDataCallBack<String>() {
		    @Override
		    public void onSuccess(final String value) {
			    if (getActivity() == null) {
				    return;
			    }
			    getActivity().runOnUiThread(new Runnable() {
				    @Override
				    public void run() {
					    if (unassignedTicketsCounts != null) {
						    unassignedTicketsCounts.setText(value);
					    }
					    if (unassignedTicketsLayout != null) {
						    unassignedTicketsLayout.setClickable(true);
					    }
				    }
			    });
		    }

		    @Override
		    public void onError(int error, String errorMsg) {

		    }
	    });
    }

    private void getCustomFilterTicketsCount() {
	    int count = getActivity().getSharedPreferences("screeningCount", MODE_PRIVATE).getInt("screeningCount", -1);
	    if(count >= 0) {
		   customFilterTicketsCounts.setText(String.valueOf(count));
		   customFilterTicketsLayout.setClickable(true);
	    } else {
		    customFilterTicketsLayout.setClickable(false);
		    HDClient.getInstance().leaveMessageManager().getAllCurrentAgentTicketsCount(new HDDataCallBack<String>() {
			    @Override
			    public void onSuccess(final String value) {
				    if (getActivity() == null) {
					    return;
				    }
				    getActivity().runOnUiThread(new Runnable() {
					    @Override
					    public void run() {
						    if (customFilterTicketsCounts != null) {
							    customFilterTicketsCounts.setText(value);
						    }
						    if (customFilterTicketsLayout != null) {
							    customFilterTicketsLayout.setClickable(true);
						    }
					    }
				    });
			    }

			    @Override
			    public void onError(int error, String errorMsg) {

			    }
		    });
	    }
    }
}
