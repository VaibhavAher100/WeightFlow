# ── Debugging: keep source info for crash reports ─────────────────────────────
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ── Kotlin ────────────────────────────────────────────────────────────────────
-keep class kotlin.Metadata { *; }
-keepclassmembers class **$WhenMappings { <fields>; }
-dontwarn kotlin.**

# ── Kotlin Coroutines ─────────────────────────────────────────────────────────
-keepclassmembernames class kotlinx.** { volatile <fields>; }
-dontwarn kotlinx.coroutines.**

# ── Room ──────────────────────────────────────────────────────────────────────
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-dontwarn androidx.room.**

# ── DataStore ─────────────────────────────────────────────────────────────────
-keep class androidx.datastore.** { *; }
-dontwarn androidx.datastore.**

# ── WorkManager ───────────────────────────────────────────────────────────────
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-dontwarn androidx.work.**

# ── Vico 1.13.1 charts ────────────────────────────────────────────────────────
-keep class com.patrykandpatrick.vico.** { *; }
-dontwarn com.patrykandpatrick.vico.**

# ── SQLCipher 4.x (net.zetetic) ───────────────────────────────────────────────
-keep class net.zetetic.database.** { *; }
-dontwarn net.zetetic.**

# ── WeightFlow: keep serialized/reflected types only ─────────────────────────
# Room @Entity and @Dao already kept above via annotation rules.
# No additional broad keeps needed — R8 traces all usages statically.
