package com.easemob.helpdesk.emoticon.filter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.TextUtils;
import android.widget.EditText;

import com.easemob.moticons.DefWeChatEmoticons;
import com.sj.emoji.EmojiDisplayListener;
import com.sj.emoji.EmojiSpan;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sj.keyboard.interfaces.EmoticonFilter;
import sj.keyboard.utils.EmoticonsKeyboardUtils;
import sj.keyboard.widget.EmoticonSpan;

/**
 * Created by liyuzhao on 16/9/26.
 */
public class WeChatFilter extends EmoticonFilter {

    public static final int WRAP_DRAWABLE = -1;
    private int emoticonSize = -1;
//    public static final Pattern WeChat_RANGE = Pattern.compile("^((\\[|/)?[/:\\u4e00-\\u9fa5])[^(!/)\\s]+");
    public static final Pattern WeChat_RANGE = Pattern.compile("/::\\)|/::~|/::B|/::\\||/:8-\\)|/::<|/::\\$|/::X|/::Z|/::\'\\(|/::-\\||/::@|"
        + "/::P|/::D|/::O|/::\\(|/::\\+|/:--b|/::Q|/::T|/:,@P|/:,@-D|/::d|/:,@o|/::g|/:\\|-\\)|/::\\!|/::L|/::>|/::,@|/:,@f|"
        + "/::-S|/:\\?|/:,@x|/:,@@|/::8|/:,@\\!|/:!!!|/:xx|/:bye|/:\\wipe|/:dig|/:handclap|/:&-\\(|/:B-\\)|/:<@|"
        + "/:@>|/::\\-O|/:>-\\||/:P-\\(|/::'\\||/:X-\\)|/::\\*|/:@x|/:8\\*|/:pd|/:<W>|/:beer|/:basketb|/:oo|/:coffee|"
        + "/:eat|/:pig|/:rose|/:fade|/:showlove|/:heart|/:break|/:cake|/:li|/:bome|/:kn|/:footb|/:ladybug|/:shit|/:moon|"
        + "/:sun|/:gift|/:hug|/:strong|/:weak|/:share|/:v|/:@\\)|/:jj|/:@@|/:bad|/:lvu|/:no|/:ok|/:love|"
        + "/:<L>|/:jump|/:shake|/:<O>|/:circle|/:kotow|/:turn|/:skip|/:oY");

    public static Matcher getMatcher(CharSequence matchStr){
        return WeChat_RANGE.matcher(matchStr);
    }

    @Override
    public void filter(EditText editText, CharSequence text, int start, int lengthBefore, int lengthAfter) {
        emoticonSize = emoticonSize == -1 ? EmoticonsKeyboardUtils.getFontHeight(editText) : emoticonSize;
        cleanSpan(editText.getText(), start, text.toString().length());
        Matcher m = getMatcher(text.toString().substring(start, text.toString().length()));
        while (m.find()){
            String key = m.group();
            String icon = DefWeChatEmoticons.wxEmoticonHashMap.get(key);
            if (!TextUtils.isEmpty(icon)){
                emoticonDisplay(editText.getContext(), editText.getText(), icon, emoticonSize, start + m.start(), start + m.end());
            }
        }
    }



    public static Spannable spannableFilter(Context context, Spannable spannable, CharSequence text, int fontSize, EmojiDisplayListener emojiDisplayListener){
        Matcher m = getMatcher(text);
        while (m.find()){
            String key = m.group();
            String icon = DefWeChatEmoticons.wxEmoticonHashMap.get(key);
            if (emojiDisplayListener == null){
                if (!TextUtils.isEmpty(icon)){
                    emoticonDisplay(context, spannable, icon, fontSize, m.start(), m.end());
                }
            }
        }
        return spannable;
    }


    private void cleanSpan(Spannable spannable, int start, int end){
        if (start == end){
            return;
        }
        EmoticonSpan[] oldSpans = spannable.getSpans(start, end, EmoticonSpan.class);
        for (int i = 0; i < oldSpans.length; i++){
            spannable.removeSpan(oldSpans[i]);
        }
    }

    public static void emoticonDisplay(Context context, Spannable spannable, String emoticonName, int fontSize, int start, int end){
        Drawable drawable = getDrawableFromAssets(context, emoticonName);
        if (drawable != null){
            int itemHeight;
            int itemWidth;
            if (fontSize == WRAP_DRAWABLE){
                itemHeight = drawable.getIntrinsicHeight();
                itemWidth = drawable.getIntrinsicWidth();
            }else{
                itemHeight = fontSize;
                itemWidth = fontSize;
            }
            drawable.setBounds(0, 0, itemHeight, itemWidth);
            EmojiSpan imageSpan = new EmojiSpan(drawable);
            spannable.setSpan(imageSpan, start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }
    }

}
