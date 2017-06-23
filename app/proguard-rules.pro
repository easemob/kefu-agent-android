# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in F:\adt_bundle\adt-bundle-windows-x86_64-20140624\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
-optimizationpasses 5                                                           # 指定代码的压缩级别
-dontusemixedcaseclassnames                                                     # 是否使用大小写混合
-dontskipnonpubliclibraryclasses                                                # 是否混淆第三方jar
-dontpreverify                                                                  # 混淆时是否做预校验
-verbose                                                                        # 混淆时是否记录日志
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*        # 混淆时所采用的算法

-keep public class * extends android.app.Activity                               # 保持哪些类不被混淆
-keep public class * extends android.app.Application                            # 保持哪些类不被混淆
-keep public class * extends android.app.Service                                # 保持哪些类不被混淆
-keep public class * extends android.content.BroadcastReceiver                  # 保持哪些类不被混淆
-keep public class * extends android.content.ContentProvider                    # 保持哪些类不被混淆
-keep public class * extends android.app.backup.BackupAgentHelper               # 保持哪些类不被混淆
-keep public class * extends android.preference.Preference                      # 保持哪些类不被混淆
-keep public class com.android.vending.licensing.ILicensingService              # 保持哪些类不被混淆
-keep public class * extends android.webkit.WebView

-dontskipnonpubliclibraryclassmembers

-keepclasseswithmembernames class * {                                           # 保持 native 方法不被混淆
    native <methods>;
}

-keepclasseswithmembers class * {                                               # 保持自定义控件类不被混淆
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);     # 保持自定义控件类不被混淆
}

-keepclassmembers class * extends android.app.Activity {                        # 保持自定义控件类不被混淆
   public void *(android.view.View);
}

-keepclassmembers enum * {                                                      # 保持枚举 enum 类不被混淆
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {                                # 保持 Parcelable 不被混淆
  public static final android.os.Parcelable$Creator *;
}

# AppCompat
-dontwarn android.support.v7.**
-keep class android.support.v7.** { *; }
-keep interface android.support.v7.** { *; }
-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod,RuntimeVisibleAnnotations,AnnotationDefault
#-dontobfuscate
-dontoptimize
-keep class !android.support.v7.internal.view.menu.**, ** { *; }

# Keep line numbers to alleviate debugging stack traces
-renamesourcefileattribute SourceFile

### for api client
-keepclassmembers class * {
  @com.google.api.client.util.Key <fields>;
}

# Fix bug on Samsung, Wiko (and other) devices running Android 4.2
# See also: https://code.google.com/p/android/issues/detail?id=78377
-keepattributes **
-keep class !android.support.v7.view.menu.**,!android.support.design.internal.NavigationMenu,!android.support.design.internal.NavigationMenuPresenter,!android.support.design.internal.NavigationSubMenu,** {*;}

# See: https://stackoverflow.com/questions/30562330/using-appcompat-layout-behavior-with-string-appbar-scrolling-view-behavior
-keep class android.support.design.widget.** { *; }
-keep interface android.support.design.widget.** { *; }
-dontwarn android.support.design.**

# 保持自己定义的类不被混淆
-keep class com.easemob.** {*;}
-keep class org.jivesoftware.** {*;}
-keep class org.apache.** {*;}
-dontwarn  com.easemob.**

-dontwarn ch.imvs.**
-dontwarn org.slf4j.**
-keep class org.ice4j.** {*;}
-keep class net.java.sip.** {*;}
-keep class org.webrtc.voiceengine.** {*;}
-keep class org.bitlet.** {*;}
-keep class org.slf4j.** {*;}
-keep class ch.imvs.** {*;}


-keepclassmembers class * {
   public <init>(org.json.JSONObject);
}
-keep public class com.easemob.helpdesk.R$*{
public static final int *;
}

-keepclasseswithmembernames class * {    native <methods>;}


#okhttp
-dontwarn com.squareup.okhttp.**
-keep class com.squareup.okhttp.** { *;}
-dontwarn okio.**
-keep interface com.squareup.okhttp.** { *; }
-dontwarn com.squareup.okhttp.**



-keepnames class * extends android.view.View

-keep class * extends android.app.Fragment {
 public void setUserVisibleHint(boolean);
 public void onHiddenChanged(boolean);
 public void onResume();
 public void onPause();
}
-keep class android.support.v4.app.Fragment {
 public void setUserVisibleHint(boolean);
 public void onHiddenChanged(boolean);
 public void onResume();
 public void onPause();
}
-keep class * extends android.support.v4.app.Fragment {
 public void setUserVisibleHint(boolean);
 public void onHiddenChanged(boolean);
 public void onResume();
 public void onPause();
}


#mipush
-keep class com.xiaomi.push.** {*;}
-dontwarn com.xiaomi.push.**
#-dontwarn com.xiaomi.push.service.a.a
-keepclasseswithmembernames class com.xiaomi.**{*;}
-keep public class * extends com.xiaomi.mipush.sdk.PushMessageReceiver


#okhttp3
-dontwarn com.squareup.okhttp3.**
-keep class com.squareup.okhttp3.** { *;}
-dontwarn okio.**

#glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}



#butterknife
-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewBinder { *; }
-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}
-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}

#### for support 22
-dontwarn android.support.**

-keep class * implements android.os.Parcelable {
   public static final android.os.Parcelable$Creator *;
}


#iconfont
-keep class .R
-keep class **.R$* {
    <fields>;
}


-keepclassmembers class ** {
    public void onEvent*(**);
}
# Keep GSON stuff
-keep class com.google.gson.** { *; }


#不混淆org.apache.http.legacy.jar
-dontwarn android.net.compatibility.**
-dontwarn android.net.http.**
-dontwarn com.android.internal.http.multipart.**
-dontwarn org.apache.commons.**
-dontwarn org.apache.http.**
-keep class org.apache.http.**{*;}
-keep class android.net.compatibility.**{*;}
-keep class android.net.http.**{*;}
-keep class com.android.internal.http.multipart.**{*;}
-keep class org.apache.commons.**{*;}



#volley
-keep class * extends java.lang.annotation.Annotation { *; }

################### region for xUtils
-keepattributes Signature,*Annotation*
-keep public class org.xutils.** {
    public protected *;
}
-keep public interface org.xutils.** {
    public protected *;
}
-keepclassmembers class * extends org.xutils.** {
    public protected *;
}
-keepclassmembers @org.xutils.db.annotation.* class * {*;}
-keepclassmembers @org.xutils.http.annotation.* class * {*;}
-keepclassmembers class * {
    @org.xutils.view.annotation.Event <methods>;
}
#################### end region

# Keep the pojos used by GSON or Jackson
-keep class com.twnel.android.models.retrofit.** { *; }

# Keep Retrofit
-keep class retrofit.** { *; }
-keepclasseswithmembers class * {
    @retrofit.** *;
}
-keepclassmembers class * {
    @retrofit.** *;
}

-keepclassmembers class **.R$* {
  public static <fields>;
}


#jsbridge
-keep interface com.easemob.jsbridge.** { *; }
-keepclasseswithmembernames class com.easemob.jsbridge.** {                                           # 保持 native 方法不被混淆
    *;
}

#nineoldandroids
-dontwarn com.nineoldandroids.*
-keep class com.nineoldandroids.** { *;}

#gson
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.* { *; }
-keep class com.google.gson.examples.android.model.* { *; }
-keep class com.google.gson.* { *;}

#support-v4
-dontwarn android.support.v4.**
-keep class android.support.v4.** { *; }


-keepclassmembers class com.easemob.helpdesk.** {
        private <fields>;
}

-keepclassmembers class * implements java.io.Serializable {
    *;
}
-keep public class * implements java.io.Serializable {
    public *;
}
-keep class com.easemob.helpdesk.entity.**{*;}
-keep class com.hyphenate.kefusdk.gsonmodel.**{*;}

# keep setters in Views so that animations can still work.
# see http://proguard.sourceforge.net/manual/examples.html#beans
-keepclassmembers public class * extends android.view.View {
   void set*(***);
   *** get*();
}


-dontwarn u.aly.**
-keep class u.aly.** {*;}
-dontwarn u.upd.**
-keep class u.upd.** {*;}

#permissiongen
-keep class kr.co.namee.permissiongen.** {*;}

# easemob 3.x
-keep class com.hyphenate.** {*;}
-dontwarn  com.hyphenate.**

# Huawei push
-keep class com.huawei.android.pushagent.** {*;}
-keep class com.huawei.android.pushselfshow.** {*;}
-keep class com.huawei.android.microkernel.** {*;}
-keep class com.baidu.mapapi.** {*;}
-keep class com.hianalytics.android.** {*;}
-dontwarn com.huawei.android.pushagent.**
-dontwarn com.huawei.android.pushselfshow.**
-dontwarn com.huawei.android.microkernel.**
-dontwarn com.github.mikephil.charting.data.**

# io.realm
-keep class io.realm.** {*;}
-dontwarn io.realm.**

#adapter也不能混淆
-keep public class * extends android.widget.BaseAdapter {*;}

#https://github.com/leolin310148/ShortcutBadger/issues/46
-keep class me.leolin.shortcutbadger.impl.AdwHomeBadger { <init>(...); }
-keep class me.leolin.shortcutbadger.impl.ApexHomeBadger { <init>(...); }
-keep class me.leolin.shortcutbadger.impl.AsusHomeLauncher { <init>(...); }
-keep class me.leolin.shortcutbadger.impl.DefaultBadger { <init>(...); }
-keep class me.leolin.shortcutbadger.impl.NewHtcHomeBadger { <init>(...); }
-keep class me.leolin.shortcutbadger.impl.NovaHomeBadger { <init>(...); }
-keep class me.leolin.shortcutbadger.impl.SolidHomeBadger { <init>(...); }
-keep class me.leolin.shortcutbadger.impl.SonyHomeBadger { <init>(...); }
-keep class me.leolin.shortcutbadger.impl.XiaomiHomeBadger { <init>(...); }

#rxjava start
-dontwarn sun.misc.**
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
 long producerIndex;
 long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
 rx.internal.util.atomic.LinkedQueueNode producerNode;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
 rx.internal.util.atomic.LinkedQueueNode consumerNode;
}
#rxjava end
