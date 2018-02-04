# Guide: http://wiebe-elsinga.com/blog/obfuscating-for-android-with-proguard/

-dontobfuscate

# remove logcat
-assumenosideeffects class android.util.Log {
    public static *** e(...);
    public static *** w(...);
    public static *** wtf(...);
    public static *** d(...);
    public static *** v(...);
}

# protobuf
#-keep class com.google.protobuf.** { *; }
#-keep class info.santhosh.evlo.model.CommodityProtos.** { *; }
-dontwarn sun.misc.Unsafe
-dontwarn com.google.protobuf.**

# OkHttp
-dontwarn okio.**
-dontwarn javax.annotation.Nullable
-dontwarn javax.annotation.ParametersAreNonnullByDefault
-dontwarn okhttp3.**

# evernote android job
-dontwarn com.evernote.android.job.gcm.**
-dontwarn com.evernote.android.job.util.GcmAvailableHelper.**
-keep public class com.evernote.android.job.v21.PlatformJobService
-keep public class com.evernote.android.job.v14.PlatformAlarmService
-keep public class com.evernote.android.job.v14.PlatformAlarmReceiver
-keep public class com.evernote.android.job.JobBootReceiver
-keep public class com.evernote.android.job.JobRescheduleService

# crashlytics
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
-keep class com.crashlytics.** { *; }
-dontwarn com.crashlytics.**

# unncessary warnings
-dontnote android.net.http.*
-dontnote org.apache.commons.codec.**
-dontnote org.apache.http.**
-dontnote okhttp3.**
-dontnote com.evernote.android.job.**
-dontnote info.santhosh.evlo.ui.search.Searchbar*
-dontnote info.santhosh.evlo.widget.EmptyRecyclerView*

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
