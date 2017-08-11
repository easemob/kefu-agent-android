package com.easemob.helpdesk.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import com.easemob.helpdesk.R;
import com.easemob.helpdesk.mvp.ChatActivity;

/**
 * 右键菜单，文本菜单
 */
public class ContextMenu extends BaseActivity {

    public static final int TYPE_CONTEXT_MENU_SHORTCUT = 1;
    public static final int TYPE_CONTEXT_MENU_CREATE_SESSION = 2;
    public static final int TYPE_CONTEXT_MENU_TXT = 3;
    public static final int TYPE_CONTEXT_MENU_TXT_WITH_RECALL = 4;
//    public static final int TYPE_CONTEXT_MENU_IMAGE = 5;
    public static final int TYPE_CONTEXT_MENU_IMAGE_WITH_RECALL = 6;
//    public static final int TYPE_CONTEXT_MENU_VOICE_WITH_RECALL = 6;


    private int parentPosition;
    private int childPosition;
    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int type = getIntent().getIntExtra("type", -1);
        if (type == TYPE_CONTEXT_MENU_SHORTCUT) {
            setContentView(R.layout.context_menu_for_shorcut);
        } else if (type == TYPE_CONTEXT_MENU_CREATE_SESSION) {
            setContentView(R.layout.context_menu_for_create_session);
        } else if (type == TYPE_CONTEXT_MENU_TXT) {
            setContentView(R.layout.context_menu_for_txt);
        } else if (type == TYPE_CONTEXT_MENU_TXT_WITH_RECALL){
            setContentView(R.layout.context_menu_for_txt_withrecall);
        } else if (type == TYPE_CONTEXT_MENU_IMAGE_WITH_RECALL){
            setContentView(R.layout.context_menu_for_recall);
        }
        Intent gIntent = getIntent();
        position = gIntent.getIntExtra("position", -1);
        parentPosition = gIntent.getIntExtra("parentPosition", -1);
        childPosition = gIntent.getIntExtra("childPosition", -1);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        finish();
        return true;
    }

    public void edit(View view) {
        setResult(RESULT_OK, new Intent().putExtra("parentPosition", parentPosition).putExtra("childPosition", childPosition).putExtra("select", 0));
        finish();
    }

    public void delete(View view) {
        setResult(RESULT_OK, new Intent().putExtra("parentPosition", parentPosition).putExtra("childPosition", childPosition).putExtra("select", 1));
        finish();
    }

    public void createSession(View view) {
        setResult(RESULT_OK, new Intent().putExtra("position", position).putExtra("select", 1));
        finish();
    }

    public void cancel(View view) {
        finish();
    }

    public void detail(View view) {
        setResult(RESULT_OK, new Intent().putExtra("position", position).putExtra("select", 0));
        finish();
    }

    public void copy(View view) {
        setResult(ChatActivity.RESULT_CODE_COPY_AND_PASTE, new Intent().putExtra("position", position));
        finish();
    }


    public void recall(View view){
        setResult(ChatActivity.RESULT_CODE_RECALL, new Intent().putExtra("position", position));
        finish();
    }

}
