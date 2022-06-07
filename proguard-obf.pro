# ----------------------------------------------------------------------------
#                           Obfuscation rules
# ----------------------------------------------------------------------------

-overloadaggressively
-useuniqueclassmembernames
-dontpreverify

# Keep the public API
-keep class dev.badbird.griefpreventiontp.api.** { *; }
-keepclassmembers class my.package.api.**

# Keep your main class
-keep public class dev.badbird.griefpreventiontp.GriefPreventionTP {
	public void onLoad();
    public void onEnable();
	public void onDisable();
}

-keepclassmembers class * extends java.lang.Enum {
    <fields>;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep,allowobfuscation class * extends org.bukkit.event.Listener {
    @org.bukkit.event.EventHandler <methods>;
}
-repackageclasses 'dev.badbird.griefpreventiontp'

-obfuscationdictionary 'names.txt'
-classobfuscationdictionary 'names.txt'
-packageobfuscationdictionary 'names.txt'

-dontshrink
-dontoptimize
-keepattributes Exceptions,*Annotation*

-libraryjars  <java.home>/jmods/java.base.jmod(!**.jar;!module-info.class)
