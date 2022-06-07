# ----------------------------------------------------------------------------
#                           Obfuscation rules
# ----------------------------------------------------------------------------

#-overloadaggressively
-useuniqueclassmembernames
-keepattributes Signature #Gson

# Keep the public API
-keep class dev.badbird.griefpreventiontp.api.** { *; }
-keepclassmembers class dev.badbird.griefpreventiontp.api.**

# Keep your main class
-keep public class dev.badbird.griefpreventiontp.GriefPreventionTP extends org.bukkit.plugin.java.JavaPlugin {
	public void onLoad();
    public void onEnable();
	public void onDisable();

	public java.io.File getDataFolder();

	public java.util.logging.Logger getLogger();
	public org.bukkit.plugin.PluginDescriptionFile getDescription();
	public org.bukkit.Server getServer();
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

#-obfuscationdictionary 'names.txt'
#-classobfuscationdictionary 'names.txt'
#-packageobfuscationdictionary 'names.txt'

-dontshrink
-dontoptimize
-keepattributes Exceptions,*Annotation*

-libraryjars  <java.home>/jmods/java.base.jmod(!**.jar;!module-info.class)
