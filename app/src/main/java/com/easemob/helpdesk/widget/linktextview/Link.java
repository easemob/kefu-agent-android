package com.easemob.helpdesk.widget.linktextview;

/**
 * Created by liyuzhao on 16/9/13.
 */
public class Link {
    public static String email = "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
            "\\@" +
            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,6}" +
            "(\\.[a-zA-Z][a-zA-Z\\-]{0,6})+";

    public static String phone = "(\\+[0-9]+[\\- \\.]*)?"
            + "(\\([0-9]+\\)[\\- \\.]*)?"
            + "([0-9][0-9\\- \\.]+[0-9])";

    public static String web = "((http|https|Http|Https|rtsp|Rtsp|http|www|ftp)://)?(([a-zA-Z0-9\\._-]+\\.[a-zA-Z]{2,6})|([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}))(:[0-9]{1,4})*(/[a-zA-Z0-9\\&%_\\./-~-]*)?";

}
