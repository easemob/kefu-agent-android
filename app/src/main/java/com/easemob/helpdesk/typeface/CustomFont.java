package com.easemob.helpdesk.typeface;

import android.content.Context;
import android.graphics.Typeface;

import com.mikepenz.iconics.typeface.IIcon;
import com.mikepenz.iconics.typeface.ITypeface;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by liyuzhao on 16/4/26.
 */
public class CustomFont implements ITypeface {

    private static final String TTF_FILE = "iconfont.ttf";

    private static Typeface typeface = null;

    private static HashMap<String, Character> mChars;

    @Override
    public IIcon getIcon(String key) {
        return Icon.valueOf(key);
    }

    @Override
    public HashMap<String, Character> getCharacters() {
        if(mChars == null){
            HashMap<String, Character> aChars = new HashMap<String, Character>();
            for (Icon v : Icon.values()){
                aChars.put(v.name(), v.character);
            }
            mChars = aChars;
        }
        return mChars;
    }

    @Override
    public String getMappingPrefix() {
        return "fon";
    }

    @Override
    public String getFontName() {
        return "CustomFont";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public int getIconCount() {
        return mChars == null ? 0 : mChars.size();
    }

    @Override
    public Collection<String> getIcons() {
        Collection<String> icons = new LinkedList<String>();
        for (Icon value : Icon.values()){
            icons.add(value.name());
        }
        return icons;
    }

    @Override
    public String getAuthor() {
        return "liyuzhao";
    }

    @Override
    public String getUrl() {
        return "";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public String getLicense() {
        return "";
    }

    @Override
    public String getLicenseUrl() {
        return "";
    }

    @Override
    public Typeface getTypeface(Context context) {
        if(typeface == null){
            try{
                typeface = Typeface.createFromAsset(context.getAssets(), "fonts/" + TTF_FILE);
            }catch (Exception e){
                return null;
            }
        }
        return typeface;
    }


    public enum Icon implements IIcon{
        fon_arrow_left('\u0031'),
        fon_arrow_right('\u0032'),
        fon_channel_app('\u0077'),
        fon_channel_wechat('\u0078'),
        fon_channel_weibo('\u007a'),
        fon_eval_star_normal('\u006c'),
        fon_eval_star_select('\u004c'),
        fon_image_break('\u0028'),
        fon_channel_web('\u0079'),
        fon_nav_session_select('\u0041'),
        fon_nav_session_normal('\u0061'),
        fon_nav_waitting_normal('\u0062'),
        fon_nav_waitting_select('\u0042'),
        fon_nav_leave_select('\u0043'),
        fon_nav_leave_normal('\u0063'),
        fon_nav_history_normal('\u0064'),
        fon_nav_history_select('\u0044'),
        fon_nav_setting_normal('\u0033'),
        fon_icon_search('\u003f'),
        fon_icon_transfer('\u0021'),
        fon_icon_sessiontag('\u0023'),
        fon_icon_flower('\u002a'),
        fon_icon_face_normal('\u0035'),
        fon_icon_file('\u0034'),
        fon_nav_notice_select('\u0045'),
        fon_nav_notice_normal('\u0065'),
        fon_nav_visitor_normal('\u0066'),
        fon_nav_visitor_select('\u0046'),
        fon_nav_friend_normal('\u0067'),
        fon_nav_friend_select('\u0047'),
        fon_nav_friend2_normal('\u0068'),
        fon_nav_friend2_select('\u0048'),
        fon_nav_agent_normal('\u0069'),
        fon_nav_agent_select('\u0049'),
        fon_icon_file2_select('\u004a'),
        fon_icon_file2_normal('\u006a'),
        fon_icon_ring_normal('\u006b'),
        fon_icon_ring_select('\u004b'),
        fon_icon_expand('\u0036'),
        fon_icon_keyboard('\u0037'),
        fon_icon_less('\u0038'),
        fon_icon_share('\u0030'),
        fon_camera('\u0076'),
        fon_more('\u004d'),
        fon_phrase('\u003a'),
        fon_links('\u002f'),
        fon_history('\u003b'),
        fon_arrow_up('\u0074'),
        fon_audio1('\u005b'),
        fon_audio2('\u007d'),
        fon_audio3('\u007b'),
        fon_file_big('\u0039'),
        fon_ai('\u005d'),
        fon_closed('\u0058'),
        fon_shai('\u0073'),
        fon_setting_limit('\u0072'),
        fon_profile('\u0071'),
        fon_profile_full('\u0051'),
        fon_audio('\u0070'),
        fon_icon_update('\u0053'),
        fon_icon_update2('\u0054'),
        //icon_false 005c不好显示,用\\(两个向右的斜线替换,因为他是转义字符)
        fon_icon_exit('\\'),
        //下面是管理员部分
        fon_nav_data('\u003d'),
        fon_icon_workload('\u006f'),
        fon_icon_workmanship('\u0067'),
        fon_nav_visitors('\u0068'),
        fon_icon_arrow_down('\ue600'),
        fon_icon_arrow_up('\ue601'),
        fon_nav_agent('\u0069'),
        //注册部分
        fon_icon_lock('\ua001'),
        fon_icon_right('\ua002'),
        fon_icon_picture('\ua003'),
        fon_icon_jieru('\ua004');


        char character;
        Icon(char character){
            this.character = character;
        }

        public String getFormattedName(){
            return "{" + name() + "}";
        }

        public char getCharacter(){
            return character;
        }

        public String getName(){
            return name();
        }

        //remember the typeface so we can use it later
        private static ITypeface typeface;

        public ITypeface getTypeface(){
            if(typeface == null){
                typeface = new CustomFont();
            }
            return typeface;
        }
    }





}
