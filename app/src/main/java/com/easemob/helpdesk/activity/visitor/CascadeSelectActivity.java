package com.easemob.helpdesk.activity.visitor;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.helpdesk.widget.flowlayout.FlowLayout;
import com.easemob.helpdesk.widget.flowlayout.TagAdapter;
import com.easemob.helpdesk.widget.flowlayout.TagFlowLayout;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.activity.BaseActivity;
import com.easemob.helpdesk.adapter.HorizontalRecyclerViewAdapter;
import com.easemob.helpdesk.widget.TextDrawable;
import com.easemob.helpdesk.widget.recyclerview.DividerItemDecoration2;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.entity.user.HDUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CascadeSelectActivity extends BaseActivity {

	@BindView(R.id.id_flowlayout)
	protected TagFlowLayout mFlowLayout;

	@BindView(R.id.progressBar)
	protected ProgressBar mPb;

	@BindView(R.id.btn_ok)
	protected Button btnOk;

	@BindView(R.id.btn_close)
	protected Button btnClose;

	@BindView(R.id.selected_recyclerview)
	protected RecyclerView horizontalRecyclerView;

	private List<NodeEntity> currentNodes = Collections.synchronizedList(new ArrayList<NodeEntity>());
	private LinkedList<NodeEntity> parentNodeIds = new LinkedList<>();
	private List<NodeEntity> horizontalNodes = Collections.synchronizedList(new ArrayList<NodeEntity>());
	private Set<Integer> checkedPosition = new HashSet<>();
	private HDUser currentUser;
	private WeakHandler weakHandler;
	private String columnDifinitionId;
	private NodeEntity selectedEntity;
	private HorizontalRecyclerViewAdapter horizontalAdapter;
	private String preValue;

	private MyTagAdapter tagAdapter;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cascade_select);
		ButterKnife.bind(this);
		weakHandler = new WeakHandler(this);
		Intent gIntent = getIntent();
		columnDifinitionId = gIntent.getStringExtra("name");
		preValue = gIntent.getStringExtra("preValue");
		initView();
		tagAdapter = new MyTagAdapter(currentNodes);
		mFlowLayout.setAdapter(tagAdapter);
		mFlowLayout.setMaxSelectCount(1);
		mFlowLayout.setOnSelectListener(new TagFlowLayout.OnSelectListener() {
			@Override
			public void onSelected(Set<Integer> selectPosSet) {
				checkedPosition = selectPosSet;
				if(!checkedPosition.isEmpty()){
					Integer position = checkedPosition.iterator().next();
					NodeEntity itemNode = currentNodes.get(position);
					if (itemNode.hasChildren){
						selectedEntity = null;
						parentNodeIds.addLast(itemNode);
						loadColumnNodeDatas(itemNode.nodeId);
					}else{
						selectedEntity = itemNode;
					}
				}else{
					selectedEntity = null;
				}
				notifyHorizontalViewDatas();
			}
		});
		currentUser = HDClient.getInstance().getCurrentUser();
		mPb.setVisibility(View.VISIBLE);
		try{
			String idsStr = preValue.substring(preValue.indexOf("ids") + 4, preValue.indexOf(","));
			loadColumnNodeDatasBySelected(idsStr);
		}catch (Exception e){
			loadColumnNodeDatas("0");
		}
	}


	private void initView(){
		LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
		horizontalRecyclerView.setLayoutManager(layoutManager);
		horizontalAdapter = new HorizontalRecyclerViewAdapter(this, horizontalNodes);
		horizontalRecyclerView.setAdapter(horizontalAdapter);
		TextDrawable drawable = TextDrawable.builder().beginConfig().textColor(Color.BLACK).fontSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 18, getResources().getDisplayMetrics())).endConfig().buildRect("/", Color.WHITE);
		horizontalRecyclerView.addItemDecoration(new DividerItemDecoration2(this, DividerItemDecoration2.HORIZONTAL_LIST, drawable));
		horizontalAdapter.setOnItemClickListener(new HorizontalRecyclerViewAdapter.OnItemClickListener() {
			@Override
			public void onItemClick(View view, int position) {
				NodeEntity nodeEntity = (NodeEntity) horizontalAdapter.getItemAtPosition(position);
				horizontalAdapter.setSelectedIndex(position-1);
				if (selectedEntity == null || !selectedEntity.nodeId.equals(nodeEntity.nodeId)){
					while(!parentNodeIds.isEmpty()){
						NodeEntity parentItem = parentNodeIds.removeLast();
						if (parentItem.nodeId.equals(nodeEntity.nodeId)){
							selectedEntity = null;
							checkedPosition.clear();
							break;
						}
					}
				}
				loadColumnNodeDatas(nodeEntity.parentNodeId);
			}
		});
	}

	public void notifyHorizontalViewDatas(){
		horizontalNodes.clear();
		horizontalNodes.addAll(parentNodeIds);
		if (selectedEntity != null){
			horizontalNodes.add(selectedEntity);
		}
		tagAdapter.setSelectedList(checkedPosition);
		horizontalAdapter.notifyDataSetChanged();
	}



	private void loadColumnNodeDatasBySelected(String cascadeColumnNodesIds){
		if (currentUser == null){
			return;
		}
		mPb.setVisibility(View.VISIBLE);
		HDClient.getInstance().visitorManager().getCrmColumnDefinitions(currentUser.getTenantId(), columnDifinitionId, cascadeColumnNodesIds, new HDDataCallBack<String>() {
			@Override
			public void onSuccess(String value) {

				try{
					JSONObject jsonObj = new JSONObject(value);
					JSONArray jsonEntities = jsonObj.getJSONArray("entities");
					LinkedList<ArrayList<NodeEntity>> linkedNodes = new LinkedList<>();
					for (int i = 0; i < jsonEntities.length(); i++) {
						JSONObject jsonEntity = jsonEntities.getJSONObject(i);
						JSONArray jsonNodeList = jsonEntity.getJSONArray("nodeList");
						ArrayList<NodeEntity> arrayList = new ArrayList<>();
						for (int j = 0; j < jsonNodeList.length(); j++) {
							JSONObject jsonNode = jsonNodeList.getJSONObject(j);
							String nodeId = jsonNode.optString("nodeId");
							String parentNodeId = jsonNode.optString("parentNodeId");
							long score = jsonNode.optLong("score");
							String displayName = jsonNode.optString("displayName");
							boolean selected = jsonNode.optBoolean("selected", false);
							NodeEntity nodeEntity = new NodeEntity();
							nodeEntity.nodeId = nodeId;
							nodeEntity.parentNodeId = parentNodeId;
							nodeEntity.score = score;
							nodeEntity.displayName = displayName;
							nodeEntity.selected = selected;
							arrayList.add(nodeEntity);
						}
						linkedNodes.addLast(arrayList);
					}
					Message message = Message.obtain();
					message.what = 2;
					message.obj = linkedNodes;
					weakHandler.sendMessage(message);
				}catch (Exception e){
					e.printStackTrace();
					weakHandler.sendEmptyMessage(3);
				}
			}

			@Override
			public void onError(int error, String errorMsg) {
				weakHandler.sendEmptyMessage(1);
			}
		});
	}

	private void loadColumnNodeDatas(String cascadeColumnNodeId){
		if (currentUser == null){
			return;
		}
		mPb.setVisibility(View.VISIBLE);
		HDClient.getInstance().visitorManager().getCrmColumnDefinitionChildren(currentUser.getTenantId(), columnDifinitionId, cascadeColumnNodeId, new HDDataCallBack<String>() {
			@Override
			public void onSuccess(String value) {
				ArrayList<NodeEntity> arrayList = new ArrayList<>();

				try {
					JSONObject jsonObj = new JSONObject(value);
					JSONArray jsonEntities = jsonObj.getJSONArray("entities");
					for (int i = 0; i < jsonEntities.length(); i++){
						JSONObject jsonNode = jsonEntities.getJSONObject(i);
						String nodeId = jsonNode.optString("nodeId");
						String parentNodeId = jsonNode.optString("parentNodeId");
						long score = jsonNode.optLong("score");
						String displayName = jsonNode.optString("displayName");
						boolean hasChildren = jsonNode.optBoolean("hasChildren", false);
						NodeEntity nodeEntity = new NodeEntity();
						nodeEntity.displayName = displayName;
						nodeEntity.hasChildren = hasChildren;
						nodeEntity.nodeId = nodeId;
						nodeEntity.parentNodeId = parentNodeId;
						nodeEntity.score = score;
						arrayList.add(nodeEntity);
					}

					Message message = Message.obtain();
					message.what = 0;
					message.obj = arrayList;
					weakHandler.sendMessage(message);
				} catch (JSONException e) {
					e.printStackTrace();
					weakHandler.sendEmptyMessage(3);
				}
			}

			@Override
			public void onError(int error, String errorMsg) {
				weakHandler.sendEmptyMessage(1);
			}
		});

	}


	@OnClick(R.id.btn_close)
	public void onClickByClose(){
		finish();
	}

	@OnClick(R.id.btn_ok)
	public void onClickByOk(){
		if (parentNodeIds.isEmpty() || selectedEntity == null){
			Toast.makeText(this, "请选择子标签或选择关闭按钮取消", Toast.LENGTH_SHORT).show();
			return;
		}
		StringBuilder stringBuilder = new StringBuilder();
		while (!parentNodeIds.isEmpty()){
			NodeEntity entity = parentNodeIds.removeFirst();
			stringBuilder.append(entity.nodeId);
			stringBuilder.append("_");
		}
		stringBuilder.append(selectedEntity.nodeId);
		setResult(Activity.RESULT_OK, getIntent().putExtra("value", stringBuilder.toString()));
		finish();
	}


	static class WeakHandler extends android.os.Handler {
		WeakReference<CascadeSelectActivity> weakReference;

		public WeakHandler(CascadeSelectActivity activity) {
			weakReference = new WeakReference<CascadeSelectActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			CascadeSelectActivity activity = weakReference.get();
			switch (msg.what) {
				case 0:
					if (activity != null) {
						activity.currentNodes.clear();
						activity.currentNodes.addAll((Collection<? extends NodeEntity>) msg.obj);
						activity.notifyHorizontalViewDatas();
						activity.mPb.setVisibility(View.GONE);
					}
					break;
				case 1:
					if (activity != null) {
						activity.mPb.setVisibility(View.GONE);
						Toast.makeText(activity, "请求失败，请检查网络！", Toast.LENGTH_SHORT).show();
					}
					break;
				case 2:
					if (activity != null){
						LinkedList<ArrayList<NodeEntity>> allNodes = (LinkedList<ArrayList<NodeEntity>>) msg.obj;
						activity.currentNodes.clear();
						if (!allNodes.isEmpty()){
							activity.currentNodes.addAll(allNodes.getLast());
						}
						activity.asyncLoadSelectedContent(allNodes);
						activity.mPb.setVisibility(View.GONE);
					}

					break;
				case 3:
					if (activity != null) {
						activity.mPb.setVisibility(View.GONE);
						Toast.makeText(activity, "数据解析失败，请重试！", Toast.LENGTH_SHORT).show();
					}
					break;
			}
		}
	}


	public void asyncLoadSelectedContent(final LinkedList<ArrayList<NodeEntity>> allNodes){
		new Thread(new Runnable() {
			@Override
			public void run() {
				parentNodeIds.clear();
				horizontalNodes.clear();
				while(!allNodes.isEmpty()){
					ArrayList<NodeEntity> nodeEntities = allNodes.removeFirst();
					NodeEntity selectedNode = null;
					int selectedPostion = -1;
					for (int i = 0; i < nodeEntities.size(); i++) {
						NodeEntity nodeItem = nodeEntities.get(i);
						if (nodeItem.selected){
							selectedNode = nodeItem;
							selectedPostion = i;
							break;
						}
					}


					if (selectedNode != null){
						if (!allNodes.isEmpty()){
							selectedNode.hasChildren = true;
							parentNodeIds.add(selectedNode);
						}else{
							selectedEntity = selectedNode;
							checkedPosition.clear();
							checkedPosition.add(selectedPostion);
						}
					}
				}
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						notifyHorizontalViewDatas();
					}
				});
			}
		}).start();
	}


	class MyTagAdapter extends TagAdapter<NodeEntity> {


		public MyTagAdapter(List<NodeEntity> datas) {
			super(datas);
		}

		@Override
		public View getView(FlowLayout parent, int position, NodeEntity userTag) {
			TextView tv = (TextView) LayoutInflater.from(parent.getContext()).inflate(R.layout.visitor_tag_textview2,
					mFlowLayout, false);
			if (userTag != null) {
				tv.setText(userTag.displayName);
				if (userTag.hasChildren) {
					TextDrawable drawable = TextDrawable.builder().beginConfig().textColor(Color.GREEN).fontSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 18, getResources().getDisplayMetrics())).endConfig().buildRect(">", Color.WHITE);
					tv.setCompoundDrawables(null, null, drawable, null);
					tv.setCompoundDrawablePadding((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics()));
				} else {
					tv.setCompoundDrawables(null, null, null, null);
				}

			}
			return tv;
		}
	}




	private class NodeEntity {
		private String nodeId;
		private String parentNodeId;
		private long score;
		private String displayName;
		private boolean hasChildren;
		private boolean selected;

		@Override
		public String toString() {
			if (displayName != null){
				return displayName;
			}
			return super.toString();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return keyCode == KeyEvent.KEYCODE_BACK || super.onKeyDown(keyCode, event);

	}
}
