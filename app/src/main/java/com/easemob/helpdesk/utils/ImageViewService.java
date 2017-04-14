package com.easemob.helpdesk.utils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by liyuzhao on 16/8/25.
 */
public class ImageViewService {
    public static byte[] getImage(String remoteUrl){
        byte[] data = null;
        try{
            //建立URL
            URL url = new URL(remoteUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setReadTimeout(5000);
            InputStream input = conn.getInputStream();
            data = readInputStream(input);
            input.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return data;
    }

    public static byte[] readInputStream(InputStream input){
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try{
            byte[] buffer = new byte[1024];
            int len;
            while ((len = input.read(buffer)) != -1){
                output.write(buffer, 0, len);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return output.toByteArray();
    }

}
