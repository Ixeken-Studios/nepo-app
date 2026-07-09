# Keep the nested JSON parsing data classes of ThemeParser to prevent Gson mapping failures
-keep class com.ixeken.nepo.core.designsystem.theme.ThemeParser$** { *; }
-keepclassmembers class com.ixeken.nepo.core.designsystem.theme.ThemeParser$** {
    <fields>;
    <methods>;
}

# Keep the public models
-keep class com.ixeken.nepo.core.designsystem.models.** { *; }
-keepclassmembers class com.ixeken.nepo.core.designsystem.models.** {
    <fields>;
    <methods>;
}
