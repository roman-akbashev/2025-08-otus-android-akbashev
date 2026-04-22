# Keep Kotlin metadata
-keep class kotlin.Metadata { *; }

# Keep data classes
-keep class com.linguacards.core.model.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }