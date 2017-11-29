package com.easemob.helpdesk.activity.visitor;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.easemob.flowlayout.FlowLayout;
import com.easemob.flowlayout.TagAdapter;
import com.easemob.flowlayout.TagFlowLayout;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.utils.ViewFindUtils;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.entity.user.HDUser;
import com.hyphenate.kefusdk.entity.UserTag;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by liyuzhao on 16/2/29.
 */
public class VisitorTagsFragment extends Fragment {
    private static final String TAG = VisitorTagsFragment.class.getSimpleName();

    private LayoutInflater mInflater;

    private TagFlowLayout mFlowLayout;
//    private TagAdapter<String> mAdapter;
    private String visitorId;

    private List<UserTag> userTagList = new ArrayList<>();
    private Set<Integer> checkedPosition = new HashSet<>();

    private MyTagAdapter tagAdapter;
    private HDUser currentUser;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_visitor_tag, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = getArguments();
        if(bundle != null){
            visitorId = bundle.getString("userId");
        }
        mInflater = LayoutInflater.from(getActivity());
        currentUser = HDClient.getInstance().getCurrentUser();
        initView();
        if(visitorId != null){
            getUserTagsFromRemote();
        }
    }

    private void getUserTagsFromRemote(){
        //获取用户标签
        HDClient.getInstance().visitorManager().getUserTag(visitorId, new HDDataCallBack<List<UserTag>>() {
            @Override
            public void onSuccess(List<UserTag> list) {
                userTagList.clear();
                userTagList.addAll(list);
                for (int i = 0; i < userTagList.size(); i++) {
                    UserTag item = userTagList.get(i);
                    if (item.checked) {
                        checkedPosition.add(i);
                    }
                }

                if (getActivity() == null) {
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        initTags();
                    }
                });

            }

            @Override
            public void onError(int error, String errorMsg) {
            }

            @Override
            public void onAuthenticationException() {
            }
        });

    }


    private void initTags(){
        tagAdapter.setSelectedList(checkedPosition);
    }


    private void initView(){
        mFlowLayout = ViewFindUtils.find(getView(),R.id.id_flowlayout);
        tagAdapter = new MyTagAdapter(userTagList);
        mFlowLayout.setAdapter(tagAdapter);
        mFlowLayout.setOnSelectListener(new TagFlowLayout.OnSelectListener() {
            @Override
            public void onSelected(Set<Integer> selectPosSet) {
                checkedPosition = selectPosSet;
            }
        });

        mFlowLayout.setOnTagClickListener(new TagFlowLayout.OnTagClickListener() {
            @Override
            public boolean onTagClick(View view, int position, FlowLayout parent) {
                UserTag userTag = userTagList.get(position);
                if(checkedPosition.contains(position)){
                    HDClient.getInstance().visitorManager().setTag(true, visitorId, userTag);
                }else {
                    HDClient.getInstance().visitorManager().setTag(false, visitorId, userTag);
                }
                return false;
            }
        });


    }


    class MyTagAdapter extends TagAdapter<UserTag>{


        public MyTagAdapter(List<UserTag> datas) {
            super(datas);
        }

        @Override
        public View getView(FlowLayout parent, int position, UserTag userTag) {
            TextView tv = (TextView) mInflater.inflate(R.layout.visitor_tag_textview,
                    mFlowLayout, false);
            if(userTag != null){
                tv.setText(userTag.tagName);
            }
            return tv;
        }
    }

}
