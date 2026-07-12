# Keep default rules

# Room
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase {
	*;
}
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }

# Accessibility/VPN services
-keep class com.alhaq.amnshield.guardian.service.** { *; }
