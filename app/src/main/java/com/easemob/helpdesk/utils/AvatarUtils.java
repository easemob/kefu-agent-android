package com.easemob.helpdesk.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.widget.ImageView;

import com.easemob.helpdesk.HDApplication;
import com.easemob.helpdesk.R;
import com.hyphenate.kefusdk.chat.HDClient;
import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.entity.HDUser;
import com.hyphenate.kefusdk.chat.OkHttpClientManager;
import com.hyphenate.kefusdk.utils.HDLog;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by liyuzhao on 16/3/4.
 */
public class AvatarUtils {

    private static final String TAG = "AvatarUtils";
    private static boolean isDownloading = false;

    private static Set<ImageView> agentViews = new HashSet<>();

    public synchronized static void refreshAgentAvatar(Activity activity, ImageView imageView) {
        if (imageView == null){
            return;
        }
        if (HDApplication.getInstance().avatarBitmap != null && !HDApplication.getInstance().avatarBitmap.isRecycled()) {
            if(imageView != null){
                imageView.setImageBitmap(HDApplication.getInstance().avatarBitmap);
            }
            return;
        }
        HDUser loginUser = HDClient.getInstance().getCurrentUser();
        if (loginUser == null) {
            return;
        }
        String remoteUrl = loginUser.getAvatar();
        if (TextUtils.isEmpty(remoteUrl)) {
            imageView.setImageResource(R.drawable.default_agent_avatar);
            return;
        }
        if (remoteUrl.contains("/ossimages/null")){
            imageView.setImageResource(R.drawable.default_agent_avatar);
            return;
        }

        if (remoteUrl.startsWith("//")) {
            remoteUrl = "http:" + remoteUrl;
        }
        if(remoteUrl.contains("/images/uikit/")){
            imageView.setImageResource(R.drawable.default_agent_avatar);
            return;
        }
        asyncGetAndSetAvatar(remoteUrl, activity, imageView);
    }

    public static void asyncGetAndSetAvatar(String remoteUrl, final Activity activity, final ImageView imageView) {
        if(imageView == null){
            HDLog.e("AvatarUtils","imageView is null  activity:" + activity);
            return;
        }

        if (HDApplication.getInstance().avatarBitmap != null && !HDApplication.getInstance().avatarBitmap.isRecycled()) {
            imageView.setImageBitmap(HDApplication.getInstance().avatarBitmap);
            return;
        }
        final String localPath = CommonUtils.getAvatarPath(remoteUrl);
        final File localFile = new File(localPath);
        if (localFile.exists()) {
            HDApplication.getInstance().avatarBitmap = BitmapFactory.decodeFile(localPath);
            if(HDApplication.getInstance().avatarBitmap != null){
                imageView.setImageBitmap(HDApplication.getInstance().avatarBitmap);
                return;
            }
        }

        agentViews.add(imageView);
        if (isDownloading){
            return;
        }
        isDownloading = true;
        HDLog.e(TAG, "downloadAsync start");
        OkHttpClientManager.getInstance().downloadAsync(remoteUrl, localPath, new HDDataCallBack<String>() {
            @Override
            public void onSuccess(String value) {
                isDownloading = false;
                HDLog.e(TAG, "downloadAsync onSuccess");
                if (localFile.exists()) {
                    if (activity == null || activity.isFinishing()) {
                        return;
                    }
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Bitmap bitmap = BitmapFactory.decodeFile(localPath);
                            if(bitmap !=null){
                                HDApplication.getInstance().avatarBitmap = bitmap;
//                                imageView.setImageBitmap(bitmap);
                                HDLog.e(TAG, "downloadAsync agentViews size:" + agentViews.size());
                                if (agentViews.size() > 0){
                                    for (ImageView iv : agentViews){
                                        iv.setImageBitmap(bitmap);
                                    }
                                    agentViews.clear();
                                }

                            }
                        }
                    });
                }
            }

            @Override
            public void onError(int error, String errorMsg) {
                isDownloading = false;
                HDLog.e(TAG, "downloadAsync onError");
                File file = new File(localPath);
                if (file.exists()) {
                    file.delete();
                }
            }

            @Override
            public void onAuthenticationException() {
                isDownloading = false;
                File file = new File(localPath);
                if (file.exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    file.delete();
                }
            }
        });


    }


}
