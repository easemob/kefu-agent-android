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
# 混淆时不适用大小写混淆类名
-dontusemixedcaseclassnames
# 不跳过library中的非public类
-dontskipnonpubliclibraryclasses
# 打印混淆的详情信息
-verbose
# Optimization is turned off by default, Dex does not like code run
# through the ProGuard optimize and preverify steps (and performs some
# of these optimizations on its own).
# 关闭优化
-dontoptimize
# 不进行预校验，可加快混淆速度
-dontpreverify
# 保留注解中的参数
-keepattributes *Annotation*
# 保留反射中的不混淆
-keepattributes Signature
# 不混淆Parcelable实现类中的CREATOR字段，以保证Parcelable机制正常工作
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}
# 不混淆R文件中的所有静态字段，以保证正确找到每个资源的id
-keepclassmembers class **.R$* {
    public static <fields>;
}
# 不混淆Keep类
-keep class android.support.annotation.Keep
# 不混淆使用了注解的类及类成员
-keep @android.support.annotation.Keep class *{*;}
# 如果类中有使用了注解的方法，则不混淆类和类成员
-keepclasseswithmembers class * {
    @android.support.annotation.Keep <methods>;
}
# 如果类中使用了注解的字段，则不混淆类和类成员
-keepclasseswithmembers class * {
    @android.support.annotation.Keep <fields>;
}
# 如果类中有使用了注解的构造函数，则不混淆类和类成员
-keepclasseswithmembers class * {
    @android.support.annotation.Keep <init>(...);
}

-keepclasseswithmembers class * {                                               # 保持自定义控件类不被混淆
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);     # 保持自定义控件类不被混淆
}

#tinker multidex keep patterns:
-keep public class * implements com.tencent.tinker.loader.app.ApplicationLifeCycle {
    <init>(...);
    void onBaseContextAttached(android.content.Context);
}

-keep public class * extends com.tencent.tinker.loader.TinkerLoader {
    <init>(...);
}

-keep public class * extends android.app.Application {
     <init>();
     void attachBaseContext(android.content.Context);
}

-keep class com.tencent.tinker.loader.TinkerTestAndroidNClassLoader {
    <init>(...);
}

#your dex.loader patterns here
-keep class tinker.sample.android.app.SampleApplication {
    <init>(...);
}

-keep class com.tencent.tinker.loader.** {
    <init>(...);
}


-keep class com.hyphenate.** {*;}
-dontwarn com.hyphenate.**


# okio
-dontwarn okio.**
-keep class okio.** { *; }

#okhttp3
-dontwarn okhttp3.**
-dontwarn com.squareup.okhttp3.**
-dontwarn javax.annotation.**
-keep class com.squareup.okhttp3.** { *;}
-dontwarn com.squareup.okhttp.internal.huc.**
-dontwarn com.squareup.okhttp.**
-dontwarn com.squareup.okhttp3.**
-dontwarn retrofit2.**
-dontwarn org.codehaus.mojo.**
-keep class retrofit2.** { *; }
-keep interface com.squareup.okhttp3.** { *; }
-keepattributes Exceptions
-dontwarn javax.annotation.Nullable
-dontwarn javax.annotation.ParametersAreNonnullByDefault

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

# Huawei push
-ignorewarning
-keepattributes *Annotation*
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes Signature
-keepattributes SourceFile,LineNumberTable
-keep class com.hianalytics.android.**{*;}
-keep class com.huawei.updatesdk.**{*;}
-keep class com.huawei.hms.**{*;}

-keep class com.huawei.gamebox.plugin.gameservice.**{*;}

-keep public class com.huawei.android.hms.agent.** extends android.app.Activity { public *; protected *; }
-keep interface com.huawei.android.hms.agent.common.INoProguard {*;}
-keep class * extends com.huawei.android.hms.agent.common.INoProguard {*;}



#mipush
-keep class com.xiaomi.push.** {*;}
-dontwarn com.xiaomi.push.**
#-dontwarn com.xiaomi.push.service.a.a
-keepclasseswithmembernames class com.xiaomi.**{*;}
-keep public class * extends com.xiaomi.mipush.sdk.PushMessageReceiver

#tencent bugly
-dontwarn com.tencent.bugly.**
-keep public class com.tencent.bugly.**{*;}

#gson
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.* { *; }
-keep class com.google.gson.examples.android.model.* { *; }
-keep class com.google.gson.* { *;}

-keep interface com.easemob.jsbridge.** { *; }
-keepclasseswithmembernames class com.easemob.jsbridge.** {                                           # 保持 native 方法不被混淆
    *;
}

# bugly
-dontwarn u.aly.**
-keep class u.aly.** {*;}
-dontwarn u.upd.**
-keep class u.upd.** {*;}

#permissiongen
-keep class kr.co.namee.permissiongen.** {*;}


# io.realm
#-keep class io.realm.** {*;}
#-dontwarn io.realm.**


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

-keep class sj.keyboard.** {*;}
-keep class com.hyphenate.kefusdk.gsonmodel.**{*;}
-keep class com.easemob.helpdesk.activity.manager.model.** {*;}
-keep class com.easemob.bsdiff.* {*;}

-keep class com.squareup.leakcanary.** {*;}