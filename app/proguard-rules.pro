# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Firebase Realtime Database model classes
-keepclassmembers class com.formfix.poseexercise.UserAccounts {
    *;
}

# Sun Mail and Activation libraries
-keep class com.sun.mail.** { *; }
-keep class javax.mail.** { *; }
-keep class javax.activation.** { *; }
-keep class com.sun.activation.** { *; }

-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# Firebase-related classes
-keepclassmembers class com.google.firebase.** { *; }
-keep class com.google.firebase.** { *; }

# Prevent removal of no-arg constructors
-keepclassmembers class * {
    <init>();
}

# Keep data model classes
-keepclassmembers class * implements java.io.Serializable {
    <fields>;
    <methods>;
}

# Preserve all annotations
-keepattributes *Annotation*