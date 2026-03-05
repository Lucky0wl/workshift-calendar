# ═══════════════════════════════════════════════════════════
# ProGuard Rules for WorkshiftCalendar
# ═══════════════════════════════════════════════════════════

# ─── Gson ───────────────────────────────────────────────────
# Keep generic signature of Call, Response (R8 full mode strips signatures from non-kept items)
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response

# Keep Gson serialization for DTOs
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.example.workshiftcalendar.**Dto { *; }
-keep class com.example.workshiftcalendar.AppDataDto { *; }
-keep class com.example.workshiftcalendar.ShiftDetailsDto { *; }
-keep class com.example.workshiftcalendar.ShiftTemplateDto { *; }
-keep class com.example.workshiftcalendar.ExpenseEntryDto { *; }

# Keep enum fields for Gson
-keepclassmembers,allowobfuscation class * extends java.lang.Enum {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ─── Kotlin ─────────────────────────────────────────────────
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}

# Keep Kotlin metadata
-keep class kotlin.Metadata { *; }

# ─── Compose ────────────────────────────────────────────────
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }

# Keep Compose compiler generated classes
-keepclassmembers class * {
    *** androidx.compose.runtime.compositionLocal.*;
}

# ─── Material Icons ─────────────────────────────────────────
-keep class androidx.compose.material.icons.** { *; }

# ─── DataStore ──────────────────────────────────────────────
-dontwarn androidx.datastore.**
-keep class androidx.datastore.** { *; }

# ─── OSMDroid ───────────────────────────────────────────────
-dontwarn org.osmdroid.**
-keep class org.osmdroid.** { *; }

# ─── WorkManager ────────────────────────────────────────────
-dontwarn androidx.work.**
-keep class androidx.work.** { *; }

# ─── Glance (Widgets) ───────────────────────────────────────
-dontwarn androidx.glance.**
-keep class androidx.glance.** { *; }

# ─── MPAndroidChart ─────────────────────────────────────────
-dontwarn com.github.mikephil.**
-keep class com.github.mikephil.** { *; }

# ─── YCharts ────────────────────────────────────────────────
-dontwarn co.yml.**
-keep class co.yml.** { *; }

# ─── Accompanist ────────────────────────────────────────────
-dontwarn com.google.accompanist.**
-keep class com.google.accompanist.** { *; }

# ─── Google Play Services ───────────────────────────────────
-dontwarn com.google.android.gms.**
-keep class com.google.android.gms.** { *; }

# ─── Keep model classes ─────────────────────────────────────
-keep class com.example.workshiftcalendar.domain.model.** { *; }
-keep class com.example.workshiftcalendar.data.model.** { *; }

# ─── Keep ShiftKind enum ────────────────────────────────────
-keep class com.example.workshiftcalendar.domain.model.ShiftKind { *; }
-keep class com.example.workshiftcalendar.domain.model.ExpenseCategory { *; }
-keep class com.example.workshiftcalendar.ui.theme.AppStyle { *; }

# ─── R8 Full Mode ───────────────────────────────────────────
# Allow R8 to optimize further
-allowaccessmodification
-optimizations !code/simplification/coding,!field/*,!class/merging/*
