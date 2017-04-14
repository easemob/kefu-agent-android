package sj.keyboard.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;

import java.util.ArrayList;
import java.util.List;

import cn.refactor.kmpautotextview.KMPAutoComplTextView;
import sj.keyboard.interfaces.EmoticonFilter;

/**
 */
public class EmoticonsAutoEditText extends KMPAutoComplTextView {

    private List<EmoticonFilter> mFilterList;

    public EmoticonsAutoEditText(Context context) {
        this(context, null);
    }

    public EmoticonsAutoEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EmoticonsAutoEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        try {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } catch (Exception e) {
            setText(getText().toString());
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if(oldh > 0 && onSizeChangedListener != null){
            onSizeChangedListener.onSizeChanged(w, h, oldw, oldh);
        }
    }


    @Override
    public void setGravity(int gravity) {
        try {
            super.setGravity(gravity);
        } catch (Exception e) {
            setText(getText().toString());
            super.setGravity(gravity);
        }
    }


    @Override
    public void setText(CharSequence text, BufferType type) {
        try {
            super.setText(text, type);
        } catch (Exception e) {
            setText(text.toString());
        }
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        if (mFilterList == null){
            return;
        }
        for (EmoticonFilter emoticonFilter : mFilterList){
            emoticonFilter.filter(this, text, start, lengthBefore, lengthAfter);
        }
    }

    public void addEmoticonFilter(EmoticonFilter emoticonFilter){
        if (mFilterList == null){
            mFilterList = new ArrayList<>();
        }
        mFilterList.add(emoticonFilter);
    }

    public void removedEmoticonFilter(EmoticonFilter emoticonFilter){
        if(mFilterList != null && mFilterList.contains(emoticonFilter)){
            mFilterList.remove(emoticonFilter);
        }
    }

    @Override
    public boolean dispatchKeyEventPreIme(KeyEvent event) {
        if(onBackKeyClickListener != null){
            onBackKeyClickListener.onBackKeyClick();
        }
        return super.dispatchKeyEventPreIme(event);
    }

    public interface OnBackKeyClickListener {
        void onBackKeyClick();
    }

    OnBackKeyClickListener onBackKeyClickListener;

    public void setOnBackKeyClickListener(OnBackKeyClickListener i) {
        onBackKeyClickListener = i;
    }

    public interface OnSizeChangedListener {
        void onSizeChanged(int w, int h, int oldw, int oldh);
    }

    OnSizeChangedListener onSizeChangedListener;

    public void setOnSizeChangedListener(OnSizeChangedListener i) {
        onSizeChangedListener = i;
    }

}
