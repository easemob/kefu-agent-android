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
import com.hyphenate.kefusdk.gsonmodel.ticket.TicketStatusResponse;
import com.easemob.helpdesk.mvp.MainActivity;
import com.easemob.helpdesk.utils.CommonUtils;
import com.google.gson.Gson;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.entity.HDUser;
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

	private HDUser loginUser;
	private volatile long mProjectId;
	private TicketStatusResponse ticketStatusResponse;
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
		loginUser = HDClient.getInstance().getCurrentUser();
		getProjectIds();
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
		i.putExtra("projectId", mProjectId);
		i.putExtra("TicketStatusResponse", ticketStatusResponse);
		switch (view.getId()) {
			case R.id.ticket_my_open:
				i.putExtra("Title", "未处理留言");
				i.putExtra("statusIdIndex", 2);
				i.putExtra("assigned", 1);
				break;
			case R.id.ticket_pending:
				i.putExtra("Title", "处理中留言");
				i.putExtra("statusIdIndex", 0);
				i.putExtra("assigned", 1);
				break;
			case R.id.ticket_my_solved:
				i.putExtra("Title", "已解决留言");
				i.putExtra("statusIdIndex", 1);
				i.putExtra("assigned", 1);
				break;
			case R.id.ticket_unassigned:
				i.putExtra("Title", "未分配留言");
				i.putExtra("assigned", 0);
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


	private synchronized  void getProjectIds(){
		if (loginUser == null){
			return;
		}

		LeaveMessageManager.getInstance().getProjectIds(new HDDataCallBack<Long>() {
			@Override
			public void onSuccess(Long value) {
				mProjectId = value;
				if (mProjectId > 0) {
					loadTicketStatus();
				}
			}

			@Override
			public void onError(int error, String errorMsg) {

			}

			@Override
			public void onAuthenticationException() {

			}
		});

	}


	private synchronized void loadTicketStatus(){
		if (loginUser == null || mProjectId == 0){
			return;
		}

		LeaveMessageManager.getInstance().getTicketStatus(mProjectId, new HDDataCallBack<String>() {
			@Override
			public void onSuccess(String value) {
				Gson gson = new Gson();
				ticketStatusResponse = gson.fromJson(value, TicketStatusResponse.class);
			}

			@Override
			public void onError(int error, String errorMsg) {

			}

			@Override
			public void onAuthenticationException() {

			}
		});


	}

	private void refleshTicketsCount() {
		getOpenTicketsCount();
		getPendingTicketsCount();
		getSolvedTicketsCount();
		getUnassigneeTicketsCount();
		getCustomFilterTicketsCount();
	}

    private void getPendingTicketsCount() {
	    if (loginUser == null || mProjectId == 0){
		    return;
	    }

	    if (ticketStatusResponse == null || ticketStatusResponse.getNumberOfElements() < 3) {
		    return;
	    }

	    String pendingTicketsId = String.valueOf(ticketStatusResponse.getEntities().get(0).getId());

	    pendingTicketsLayout.setClickable(false);

	    LeaveMessageManager.getInstance().getTicketCountsByStatusIds(mProjectId, pendingTicketsId, new HDDataCallBack<String>() {
		    @Override
		    public void onSuccess(final String value) {
			    if (getActivity() == null) {
				    return;
			    }
			    getActivity().runOnUiThread(new Runnable() {
				    @Override
				    public void run() {
					    pendingTicketsCounts.setText(value);
					    pendingTicketsLayout.setClickable(true);
				    }
			    });
		    }

		    @Override
		    public void onError(int error, String errorMsg) {

		    }
	    });

    }

    private void getSolvedTicketsCount() {
	    if (loginUser == null || mProjectId == 0){
		    return;
	    }

	    if (ticketStatusResponse == null || ticketStatusResponse.getNumberOfElements() < 3) {
		    return;
	    }

	    String pendingTicketsId = String.valueOf(ticketStatusResponse.getEntities().get(1).getId());

	    mysolvedTicketsLayout.setClickable(false);

	    LeaveMessageManager.getInstance().getTicketCountsByStatusIds(mProjectId, pendingTicketsId, new HDDataCallBack<String>() {
		    @Override
		    public void onSuccess(final String value) {
			    if (getActivity() == null) {
				    return;
			    }
			    getActivity().runOnUiThread(new Runnable() {
				    @Override
				    public void run() {
					    mySolvedTicketsCounts.setText(value);
					    mysolvedTicketsLayout.setClickable(true);
				    }
			    });
		    }

		    @Override
		    public void onError(int error, String errorMsg) {

		    }
	    });
    }

    private void getOpenTicketsCount() {
	    if (loginUser == null || mProjectId == 0){
		    return;
	    }

	    if (ticketStatusResponse == null || ticketStatusResponse.getNumberOfElements() < 3) {
		    return;
	    }

	    String pendingTicketsId = String.valueOf(ticketStatusResponse.getEntities().get(2).getId());

	    myOpenTicketsLayout.setClickable(false);

	    LeaveMessageManager.getInstance().getTicketCountsByStatusIds(mProjectId, pendingTicketsId, new HDDataCallBack<String>() {
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
				        myOpenTicketsCounts.setText(value);
				        myOpenTicketsLayout.setClickable(true);
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
	    if (loginUser == null || mProjectId == 0){
		    return;
	    }

	    unassignedTicketsLayout.setClickable(false);

	    LeaveMessageManager.getInstance().getUnassignedTicketCounts(mProjectId, new HDDataCallBack<String>() {
		    @Override
		    public void onSuccess(final String value) {
			    if (getActivity() == null) {
				    return;
			    }
			    getActivity().runOnUiThread(new Runnable() {
				    @Override
				    public void run() {
					    unassignedTicketsCounts.setText(value);
					    unassignedTicketsLayout.setClickable(true);
				    }
			    });
		    }

		    @Override
		    public void onError(int error, String errorMsg) {

		    }
	    });
    }

    private void getCustomFilterTicketsCount() {
	    if (loginUser == null || mProjectId == 0){
		    return;
	    }

	    if (ticketStatusResponse == null || ticketStatusResponse.getNumberOfElements() < 3) {
		    return;
	    }
	    int count = getActivity().getSharedPreferences("screeningCount", MODE_PRIVATE).getInt("screeningCount", -1);
	    if(count >= 0) {
		   customFilterTicketsCounts.setText(String.valueOf(count));
		   customFilterTicketsLayout.setClickable(true);
	    } else {
		    String pendingTicketsId = ticketStatusResponse.getEntities().get(0).getId() + "," + ticketStatusResponse.getEntities().get(1).getId() +
				    "," + ticketStatusResponse.getEntities().get(2).getId();

		    customFilterTicketsLayout.setClickable(false);
		    LeaveMessageManager.getInstance().getTicketCountsByStatusIds(mProjectId, pendingTicketsId, new HDDataCallBack<String>() {
			    @Override
			    public void onSuccess(final String value) {
				    if (getActivity() == null) {
					    return;
				    }
				    getActivity().runOnUiThread(new Runnable() {
					    @Override
					    public void run() {
						    customFilterTicketsCounts.setText(value);
						    customFilterTicketsLayout.setClickable(true);
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
