package com.easemob.helpdesk.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

public class PasteEditText extends EditText{
	
	public PasteEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;
		init();
	}

	private Context context;
	
	public PasteEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		init();
	}
	
    private void init() {
//    	setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
//        setMaxLines(5);
//    	setSingleLine(false);
	}

	public PasteEditText(Context context) {
		super(context);
		this.context = context;
		init();
	}
	

    @SuppressLint("NewApi")
	public boolean onTextContextMenuItem(int id) {
//        if(id == android.R.id.paste){
//            ClipboardManager clip = (ClipboardManager)getContext().getSystemService(Context.CLIPBOARD_SERVICE);
//            if (clip == null || clip.getText() == null) {
//                return false;
//            }
//            String text = clip.getText().toString();
//            if(text.startsWith(ChatActivity.COPY_IMAGE)){
////                intent.setDataAndType(Uri.fromFile(new File("/sdcard/mn1.jpg")), "image/*");
//                text = text.replace(ChatActivity.COPY_IMAGE, "");
//                Intent intent = new Intent(context,AlertDialog.class);
//                String str = "发送以下图片";
//                intent.putExtra("title", str);
//                intent.putExtra("forwardImage", text);
//                intent.putExtra("cancel", true);
//                ((Activity)context).startActivityForResult(intent, ChatActivity.RESULT_CODE_COPY_AND_PASTE);
////                clip.setText("");
//            }
//        }
        return super.onTextContextMenuItem(id);
    }
    
    
    
    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
//        if(!TextUtils.isEmpty(text) && text.toString().startsWith(ChatActivity.COPY_IMAGE)){
//            setText("");
//        }
//        else if(!TextUtils.isEmpty(text)){
//        	setText(SmileUtils.getSmiledText(getContext(), text),BufferType.SPANNABLE);
//        }
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
    }
}
