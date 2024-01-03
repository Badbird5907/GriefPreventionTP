# Ignore dependencies bundled with the server

-keep class org.bukkit.** { *; }
-dontwarn org.bukkit.**

#-keep class dev.badbird.xenonac.dependencies.** { *; }
#-dontwarn dev.badbird.xenonac.dependencies.**

-keep class io.** { *; } # Catch-all for netty, reflections, etc.
-dontwarn io.**

-keep class org.** { *; } # Catch-all for org.bukkit, org.spigotmc, org.jetbrains etc.
-dontwarn org.**

-keep class net.minecraft.** { *; }
-dontwarn net.minecraft.**

-keep class net.milkbowl.** { *; }
-dontwarn net.milkbowl.**

-keep class com.google.** { *; }
-dontwarn com.google.**
-keepclassmembers class com.google.**

#-keep class dev.badbird.griefpreventiontp.dependencies.** { *; }
#-dontwarn dev.badbird.griefpreventiontp.dependencies.**

-keep class net.kyori.** { *; }
-dontwarn net.kyori.**

-keep class net.badbird5907.blib.** { *; }
-dontwarn net.badbird5907.blib.**

-keep class lombok.** { *; }
-dontwarn lombok.**

-keep class javax.** { *; }
-dontwarn javax.**

-keep class me.ryanhamshire.** { *; }
-dontwarn me.ryanhamshire.**

-keep class javassist.** { *; }
-dontwarn javassist.**

-keep class sun.** { *; }
-dontwarn sun.**

-keep class jdk.** { *; }
-dontwarn jdk.**

-keep class java.util.** { *; }
-dontwarn java.util.**

#-dontwarn dev.badbird.griefpreventiontp.GriefPreventionTP
#-dontwarn dev.badbird.griefpreventiontp.manager.TeleportManager
-keep class org.bukkit.plugin.java.JavaPlugin { *; }
-dontwarn org.bukkit.plugin.java.JavaPlugin

-keep class org.bukkit.plugin.PluginDescriptionFile { *; }
-dontwarn org.bukkit.plugin.PluginDescriptionFile

-dontwarn java.util.logging.Logger

-dontwarn dev.badbird.griefpreventiontp.data.impl.FlatFileStorageProvider