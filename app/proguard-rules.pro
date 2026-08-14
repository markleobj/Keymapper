-keepattributes *Annotation*
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
-keepclassmembers class com.keymapper.app.model.** { *; }
