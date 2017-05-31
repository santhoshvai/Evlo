# Guide: http://wiebe-elsinga.com/blog/obfuscating-for-android-with-proguard/

# Just want proguard to strip unused things, don't care about people
# seeing the source
#-dontobfuscate

# remove logcat
-assumenosideeffects class android.util.Log {
    public static *** e(...);
    public static *** w(...);
    public static *** wtf(...);
    public static *** d(...);
    public static *** v(...);
}

# OkHttp
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }
-dontwarn com.squareup.okhttp.**

# Simple-Xml https://stackoverflow.com/a/44152535/3394023
-dontwarn javax.xml.stream.**

-keep public class org.simpleframework.** { *; }
-keep class org.simpleframework.xml.** { *; }
-keep class org.simpleframework.xml.core.** { *; }
-keep class org.simpleframework.xml.util.** { *; }

-keepattributes ElementList, Root

-keepclassmembers class * {
    @org.simpleframework.xml.* *;
}
## official one: https://svn.code.sf.net/p/simple/svn/trunk/download/stream/proguard.pro (not used)
