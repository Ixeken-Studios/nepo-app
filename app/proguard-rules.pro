# Regla de Preservación de Modelos de Datos para Deserialización JSON
# Evita que R8 optimice o renombre las clases destinadas al mapeo de propiedades de temas
-keepattributes Signature,InnerClasses,EnclosingMethod

-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.ixeken.nepo.core.designsystem.models.** { *; }

# Keep HistoryEntry from being obfuscated by R8 to preserve local storage compatibility
-keep class com.ixeken.nepo.features.calculator.data.HistoryEntry { *; }
-keepclassmembers class com.ixeken.nepo.features.calculator.data.HistoryEntry {
    <fields>;
    <methods>;
}

# Evitar que R8 elimine las firmas de los TypeTokens de Gson
-keep public class * extends com.google.gson.reflect.TypeToken
-keepclassmembers class * extends com.google.gson.reflect.TypeToken {
    <init>(...);
}

