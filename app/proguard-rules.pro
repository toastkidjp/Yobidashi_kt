# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\toastkidjp\AppData\Local\Android\Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

## For OkHttp
-dontwarn java.nio.file.*
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

-dontwarn javax.annotation.**
-keep class javax.annotation.** { *; }

## For Moshi
-dontwarn okio.**
-keepclasseswithmembers class * {
    @com.squareup.moshi.* <methods>;
}
-keep @com.squareup.moshi.JsonQualifier interface *

-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

## For Retrolambda
-dontwarn java.lang.invoke.*
-dontwarn **$$Lambda$*

## For use AdMob
-keep public class com.google.android.gms.ads.** {
   public *;
}

-keep public class com.google.ads.** {
   public *;
}
