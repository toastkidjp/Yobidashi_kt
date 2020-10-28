# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\toastkidjp\AppData\Local\Android\Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

-dontwarn jp.toastkid.yobidashi.browser.BrowserFragment**
-keep class jp.toastkid.yobidashi.tab.model.** { *; }
-keepclassmembers enum jp.toastkid.yobidashi.** { *; }

# For Jsoup
-keep public class org.jsoup.** {
    public *;
}

# For enumeration classes, see http://proguard.sourceforge.net/manual/examples.html#enumerations
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

## For OkHttp
-dontwarn java.nio.file.*
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn okhttp3.internal.platform.ConscryptPlatform
-dontwarn javax.annotation.**
-keep class javax.annotation.** { *; }

### A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

## For Moshi
-dontwarn okio.**
-keepclasseswithmembers class * {
    @com.squareup.moshi.* <methods>;
}
-keep @com.squareup.moshi.JsonQualifier interface *

-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# Retrofit does reflection on generic parameters. InnerClasses is required to use Signature and
# EnclosingMethod is required to use InnerClasses.
-keepattributes Signature, InnerClasses, EnclosingMethod

# Retrofit does reflection on method and parameter annotations.
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# Retain service method parameters when optimizing.
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Ignore annotation used for build tooling.
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# Ignore JSR 305 annotations for embedding nullability information.
-dontwarn javax.annotation.**

# Guarded by a NoClassDefFoundError try/catch and only used when on the classpath.
-dontwarn kotlin.Unit

# For Retrofit
-keepnames @kotlin.Metadata class jp.toastkid.yobidashi.wikipedia.**
-keep class jp.toastkid.yobidashi.wikipedia.** { *; }
-keepclassmembers class jp.toastkid.yobidashi.wikipedia.** { *; }