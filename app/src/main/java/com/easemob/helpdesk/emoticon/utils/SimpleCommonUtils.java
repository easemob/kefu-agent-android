package com.easemob.helpdesk.emoticon.utils;

import android.content.Context;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.easemob.helpdesk.R;
import com.easemob.helpdesk.emoticon.filter.EaseMobFilter;
import com.easemob.helpdesk.emoticon.filter.WeBoFilter;
import com.easemob.helpdesk.emoticon.filter.WeChatFilter;
import com.easemob.helpdesk.mvp.BaseChatActivity;
import com.easemob.moticons.DefEaseEmoticons;
import com.easemob.moticons.DefWeChatEmoticons;
import com.easemob.moticons.DefWeiBoEmoticons;
import com.hyphenate.kefusdk.chat.EmojiconManager.EmojiconPackage;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.entity.CustomEmojIconEntity;
import com.sj.emoji.DefEmoticons;
import com.sj.emoji.EmojiBean;
import com.sj.emoji.EmojiDisplay;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import sj.keyboard.adapter.EmoticonsAdapter;
import sj.keyboard.adapter.PageSetAdapter;
import sj.keyboard.data.EmoticonEntity;
import sj.keyboard.data.EmoticonPageEntity;
import sj.keyboard.data.EmoticonPageSetEntity;
import sj.keyboard.interfaces.EmoticonClickListener;
import sj.keyboard.interfaces.EmoticonDisplayListener;
import sj.keyboard.interfaces.PageViewInstantiateListener;
import sj.keyboard.utils.EmoticonsKeyboardUtils;
import sj.keyboard.utils.imageloader.ImageBase;
import sj.keyboard.utils.imageloader.ImageLoader;
import sj.keyboard.widget.EmoticonPageView;
import sj.keyboard.widget.EmoticonsAutoEditText;

public class SimpleCommonUtils {

    public static void initEmoticonsEditText(EmoticonsAutoEditText etContent){
        etContent.addEmoticonFilter(new EaseMobFilter());
        etContent.addEmoticonFilter(new WeChatFilter());
        etContent.addEmoticonFilter(new WeBoFilter());
    }

    public static EmoticonClickListener getCommonEmoticonClickListener(final EditText editText) {
        return new EmoticonClickListener() {
            @Override
            public void onEmoticonClick(Object o, int actionType, boolean isDelBtn) {
                if (isDelBtn) {
                    SimpleCommonUtils.delClick(editText);
                } else {
                    if (o == null) {
                        return;
                    }
                    if (actionType == Constants.EMOTICON_CLICK_TEXT) {
                        String content = null;
                        if (o instanceof EmojiBean) {
                            content = ((EmojiBean) o).emoji;
                        } else if (o instanceof EmoticonEntity) {
                            content = ((EmoticonEntity) o).getContent();
                        }

                        if (TextUtils.isEmpty(content)) {
                            return;
                        }
                        int index = editText.getSelectionStart();
                        Editable editable = editText.getText();
                        editable.insert(index, content);
                    }
                }
            }
        };
    }

    public static PageSetAdapter sCommonPageSetAdapter;

    public static PageSetAdapter getCommonAdapter(Context context, EmoticonClickListener emoticonClickListener) {

        if(sCommonPageSetAdapter != null){
            return sCommonPageSetAdapter;
        }

        PageSetAdapter pageSetAdapter = new PageSetAdapter();

        if (context instanceof BaseChatActivity){
            String originType = ((BaseChatActivity)context).getOriginType();
            if (originType == null){
                addEasemobPageSetEntity(pageSetAdapter, context, emoticonClickListener);
            }else if (originType.equals("weibo")){
                addWeBoPageSetEntity(pageSetAdapter, context, emoticonClickListener);
            }else if (originType.equals("weixin")){
                addWeChatPageSetEntity(pageSetAdapter, context, emoticonClickListener);
            }else{
                addEasemobPageSetEntity(pageSetAdapter, context, emoticonClickListener);
            }
            addCustomEmojiconSetEntity(pageSetAdapter, context, emoticonClickListener);
        }
        return pageSetAdapter;
    }

    /**
     * 插入emoji表情集
     *
     * @param pageSetAdapter
     * @param context
     * @param emoticonClickListener
     */
    public static void addEmojiPageSetEntity(PageSetAdapter pageSetAdapter, Context context, final EmoticonClickListener emoticonClickListener) {
        ArrayList<EmojiBean> emojiArray = new ArrayList<>();
        Collections.addAll(emojiArray, DefEmoticons.sEmojiArray);
        EmoticonPageSetEntity emojiPageSetEntity
                = new EmoticonPageSetEntity.Builder()
                .setLine(3)
                .setRow(7)
                .setEmoticonList(emojiArray)
                .setIPageViewInstantiateItem(getDefaultEmoticonPageViewInstantiateItem(new EmoticonDisplayListener<Object>() {
                    @Override
                    public void onBindView(int position, ViewGroup parent, EmoticonsAdapter.ViewHolder viewHolder, Object object, final boolean isDelBtn) {
                        final EmojiBean emojiBean = (EmojiBean) object;
                        if (emojiBean == null && !isDelBtn) {
                            return;
                        }

                        viewHolder.ly_root.setBackgroundResource(sj.keyboard.R.drawable.bg_emoticon);

                        if (isDelBtn) {
                            viewHolder.iv_emoticon.setImageResource(R.mipmap.icon_del);
                        } else {
                            viewHolder.iv_emoticon.setImageResource(emojiBean.icon);
                        }

                        viewHolder.rootView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (emoticonClickListener != null) {
                                    emoticonClickListener.onEmoticonClick(emojiBean, Constants.EMOTICON_CLICK_TEXT, isDelBtn);
                                }
                            }
                        });
                    }
                }))
                .setShowDelBtn(EmoticonPageEntity.DelBtnStatus.LAST)
                .setIconUri(ImageBase.Scheme.DRAWABLE.toUri("icon_emoji"))
                .build();
        pageSetAdapter.add(emojiPageSetEntity);
    }

    public static void addEasemobPageSetEntity(PageSetAdapter pageSetAdapter, Context context, EmoticonClickListener emoticonClickListener){
        EmoticonPageSetEntity easePageSetEntity
                = new EmoticonPageSetEntity.Builder()
                .setLine(3)
                .setRow(7)
                .setEmoticonList(ParseDataUtils.ParseXhsData(DefEaseEmoticons.easeEmoticonArray, ImageBase.Scheme.ASSETS))
                .setIPageViewInstantiateItem(getDefaultEmoticonPageViewInstantiateItem(getCommonEmoticonDisplayListener(emoticonClickListener, Constants.EMOTICON_CLICK_TEXT)))
                .setShowDelBtn(EmoticonPageEntity.DelBtnStatus.LAST)
                .setIconUri(ImageBase.Scheme.ASSETS.toUri("ee_1.png"))
                .build();
        pageSetAdapter.add(easePageSetEntity);
    }

    public static void addWeChatPageSetEntity(PageSetAdapter pageSetAdapter, Context context, EmoticonClickListener emoticonClickListener){
        EmoticonPageSetEntity easePageSetEntity
                = new EmoticonPageSetEntity.Builder()
                .setLine(3)
                .setRow(7)
                .setEmoticonList(ParseDataUtils.ParseXhsData(DefWeChatEmoticons.wxEmoticonArray, ImageBase.Scheme.ASSETS))
                .setIPageViewInstantiateItem(getDefaultEmoticonPageViewInstantiateItem(getCommonEmoticonDisplayListener(emoticonClickListener, Constants.EMOTICON_CLICK_TEXT)))
                .setShowDelBtn(EmoticonPageEntity.DelBtnStatus.LAST)
                .setIconUri(ImageBase.Scheme.ASSETS.toUri("wx_0.png"))
                .build();
        pageSetAdapter.add(easePageSetEntity);
    }

    public static void addWeBoPageSetEntity(PageSetAdapter pageSetAdapter, Context context, EmoticonClickListener emoticonClickListener){
        EmoticonPageSetEntity easePageSetEntity
                = new EmoticonPageSetEntity.Builder()
                .setLine(3)
                .setRow(7)
                .setEmoticonList(ParseDataUtils.ParseXhsData(DefWeiBoEmoticons.wbEmoticonArray, ImageBase.Scheme.ASSETS))
                .setIPageViewInstantiateItem(getDefaultEmoticonPageViewInstantiateItem(getCommonEmoticonDisplayListener(emoticonClickListener, Constants.EMOTICON_CLICK_TEXT)))
                .setShowDelBtn(EmoticonPageEntity.DelBtnStatus.LAST)
                .setIconUri(ImageBase.Scheme.ASSETS.toUri("wb_1.gif"))
                .build();
        pageSetAdapter.add(easePageSetEntity);
    }

    public static void addCustomEmojiconSetEntity(PageSetAdapter pageSetAdapter, Context context, EmoticonClickListener emoticonClickListener) {
        List<EmojiconPackage> emojiconPackages = HDClient.getInstance().emojiManager().getEmojiPackagesList();
        if (emojiconPackages.size() > 0) {
            synchronized (emojiconPackages){
                for (EmojiconPackage aPackage : emojiconPackages) {
                    EmoticonPageSetEntity easePageSetEntity
                            = new EmoticonPageSetEntity.Builder()
                            .setLine(3)
                            .setRow(7)
                            .setEmoticonList(HDClient.getInstance().emojiManager().getEmojiconList(aPackage))
                            .setIPageViewInstantiateItem(getDefaultEmoticonPageViewInstantiateItem(getCustomEmoticonDisplayListener(emoticonClickListener, Constants.EMOTICON_CLICK_BIGIMAGE)))
                            .setSetName(aPackage.packageName)
                            .build();
                    pageSetAdapter.add(easePageSetEntity);
                }
            }
        }
    }


    @SuppressWarnings("unchecked")
    public static Object newInstance(Class _Class, Object... args) throws Exception {
        return newInstance(_Class, 0, args);
    }

    @SuppressWarnings("unchecked")
    public static Object newInstance(Class _Class, int constructorIndex, Object... args) throws Exception {
        Constructor cons = _Class.getConstructors()[constructorIndex];
        return cons.newInstance(args);
    }

    public static PageViewInstantiateListener<EmoticonPageEntity> getDefaultEmoticonPageViewInstantiateItem(final EmoticonDisplayListener<Object> emoticonDisplayListener) {
        return getEmoticonPageViewInstantiateItem(EmoticonsAdapter.class, null, emoticonDisplayListener);
    }

    public static PageViewInstantiateListener<EmoticonPageEntity> getEmoticonPageViewInstantiateItem(final Class _class, EmoticonClickListener onEmoticonClickListener) {
        return getEmoticonPageViewInstantiateItem(_class, onEmoticonClickListener, null);
    }

    public static PageViewInstantiateListener<EmoticonPageEntity> getEmoticonPageViewInstantiateItem(final Class _class, final EmoticonClickListener onEmoticonClickListener, final EmoticonDisplayListener<Object> emoticonDisplayListener) {
        return new PageViewInstantiateListener<EmoticonPageEntity>() {
            @Override
            public View instantiateItem(ViewGroup container, int position, EmoticonPageEntity pageEntity) {
                if (pageEntity.getRootView() == null) {
                    EmoticonPageView pageView = new EmoticonPageView(container.getContext());
                    pageView.setNumColumns(pageEntity.getRow());
                    pageEntity.setRootView(pageView);
                    try {
                        EmoticonsAdapter adapter = (EmoticonsAdapter) newInstance(_class, container.getContext(), pageEntity, onEmoticonClickListener);
                        if (emoticonDisplayListener != null) {
                            adapter.setOnDisPlayListener(emoticonDisplayListener);
                        }
                        pageView.getEmoticonsGridView().setAdapter(adapter);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return pageEntity.getRootView();
            }
        };
    }

    public static EmoticonDisplayListener<Object> getCommonEmoticonDisplayListener(final EmoticonClickListener onEmoticonClickListener, final int type) {
        return new EmoticonDisplayListener<Object>() {
            @Override
            public void onBindView(int position, ViewGroup parent, EmoticonsAdapter.ViewHolder viewHolder, Object object, final boolean isDelBtn) {

                final EmoticonEntity emoticonEntity = (EmoticonEntity) object;
                if (emoticonEntity == null && !isDelBtn) {
                    return;
                }
                viewHolder.ly_root.setBackgroundResource(sj.keyboard.R.drawable.bg_emoticon);

                if (isDelBtn) {
                    viewHolder.iv_emoticon.setImageResource(R.mipmap.icon_del);
                } else {
                    try {
                        ImageLoader.getInstance(viewHolder.iv_emoticon.getContext()).displayImage(emoticonEntity.getIconUri(), viewHolder.iv_emoticon);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                viewHolder.rootView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onEmoticonClickListener != null) {
                            onEmoticonClickListener.onEmoticonClick(emoticonEntity, type, isDelBtn);
                        }
                    }
                });
            }
        };
    }

    public static EmoticonDisplayListener<Object> getCustomEmoticonDisplayListener(final EmoticonClickListener onEmoticonClickListener, final int type) {
        return new EmoticonDisplayListener<Object>() {
            @Override
            public void onBindView(int position, ViewGroup parent, EmoticonsAdapter.ViewHolder viewHolder, Object object, final boolean isDelBtn) {

                final CustomEmojIconEntity emojicon = (CustomEmojIconEntity) object;
                if (emojicon == null && !isDelBtn) {
                    return;
                }
                viewHolder.ly_root.setBackgroundResource(sj.keyboard.R.drawable.bg_emoticon);

                if (isDelBtn) {
                    viewHolder.iv_emoticon.setImageResource(R.mipmap.icon_del);
                } else {
                    File localIcon = null;
                    File localBigIcon = null;
                    if(!TextUtils.isEmpty(emojicon.getIconPath())) {
                        localIcon = new File(emojicon.getIconPath());
                    }
                    if (!TextUtils.isEmpty(emojicon.getBigIconPath())) {
                        localBigIcon = new File(emojicon.getBigIconPath());
                    }
                    if (viewHolder.iv_emoticon != null){
                        if (localIcon != null && localIcon.exists()) {
                            Glide.with(viewHolder.iv_emoticon.getContext()).load(emojicon.getIconPath())
                                    .apply(RequestOptions.placeholderOf(R.drawable.default_image))
                                    .into(viewHolder.iv_emoticon);
                        } else if (localBigIcon != null && localBigIcon.exists()) {
                            Glide.with(viewHolder.iv_emoticon.getContext()).load(emojicon.getBigIconPath())
                                    .apply(RequestOptions.placeholderOf(R.drawable.default_image))
                                    .into(viewHolder.iv_emoticon);
                        } else if (!TextUtils.isEmpty(emojicon.getIconRemotePath())) {
                            Glide.with(viewHolder.iv_emoticon.getContext()).load(emojicon.getIconRemotePath())
                                    .apply(RequestOptions.placeholderOf(R.drawable.default_image))
                                    .into(viewHolder.iv_emoticon);
                        } else if (!TextUtils.isEmpty(emojicon.getBigIconRemotePath())) {
                            Glide.with(viewHolder.iv_emoticon.getContext()).load(emojicon.getBigIconRemotePath())
                                    .apply(RequestOptions.placeholderOf(R.drawable.default_image))
                                    .into(viewHolder.iv_emoticon);
                        } else {
                            viewHolder.iv_emoticon.setImageResource(R.drawable.default_image);
                        }
                    }
                }

                viewHolder.rootView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onEmoticonClickListener != null) {
                            onEmoticonClickListener.onEmoticonClick(emojicon, type, isDelBtn);
                        }
                    }
                });
            }
        };
    }

    public static void delClick(EditText editText) {
        int action = KeyEvent.ACTION_DOWN;
        int code = KeyEvent.KEYCODE_DEL;
        KeyEvent event = new KeyEvent(action, code);
        editText.onKeyDown(KeyEvent.KEYCODE_DEL, event);
    }

    public static void spannableEmoticonFilter(TextView tv_content, String content) {
        if (tv_content == null) {
            return;
        }

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(content);

        Spannable spannable = EmojiDisplay.spannableFilter(tv_content.getContext(),
                spannableStringBuilder,
                content,
                EmoticonsKeyboardUtils.getFontHeight(tv_content));

        spannable = EaseMobFilter.spannableFilter(tv_content.getContext(),
                spannable,
                content,
                EmoticonsKeyboardUtils.getFontHeight(tv_content),
                null);

        spannable = WeChatFilter.spannableFilter(tv_content.getContext(),
                spannable,
                content,
                EmoticonsKeyboardUtils.getFontHeight(tv_content),
                null);

        spannable = WeBoFilter.spannableFilter(tv_content.getContext(),
                spannable,
                content,
                EmoticonsKeyboardUtils.getFontHeight(tv_content),
                null);


        tv_content.setText(spannable);
    }

}
