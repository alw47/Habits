# ============================================================
# Crash reporting — preserve file names and line numbers so
# stack traces in Play Console are human-readable.
# ============================================================
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ============================================================
# Annotations, generics, and inner classes
# Required by Hilt (component generation) and Room (schema
# reflection) to function correctly after shrinking.
# ============================================================
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# ============================================================
# Kotlin metadata
# Needed so Kotlin reflection (used by Hilt and coroutines)
# can read class/function metadata at runtime.
# ============================================================
-keep class kotlin.Metadata { *; }

# ============================================================
# Kotlin Coroutines — R8 compatibility
# These internal classes are accessed via reflection in some
# coroutine paths; keeping their names prevents NoSuchField.
# ============================================================
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# ============================================================
# Room
# Room ships its own consumer ProGuard rules inside the AAR,
# but these are a belt-and-suspenders safety net for entities
# and DAOs in case R8 full-mode removes their no-arg ctors.
# ============================================================
-keep @androidx.room.Entity class *
-keepclassmembers @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class *
-keep @androidx.room.Database class *

# ============================================================
# Hilt / Dagger
# The Hilt Gradle plugin injects its own rules automatically.
# These keep the app-level entry points as an extra safeguard.
# ============================================================
-keep @dagger.hilt.android.HiltAndroidApp class *
-keep @dagger.hilt.android.AndroidEntryPoint class *
-keep @dagger.hilt.InstallIn class *
