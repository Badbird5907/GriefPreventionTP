# Ignore dependencies bundled with the server

-keep class org.bukkit.** { *; }
-dontwarn org.bukkit.**

#-keep class dev.badbird.xenonac.dependencies.** { *; }
#-dontwarn dev.badbird.xenonac.dependencies.**

-keep class io.** { *; } # Catch-all for netty, reflections, etc.
-dontwarn io.**

-keep class org.** { *; } # Catch-all for org.bukkit, org.spigotmc, org.jetbrains etc.
-dontwarn org.**

-keep class net.kyori.** { *; }
-dontwarn net.kyori.**

-keep class net.badbird5907.blib.** { *; }
-dontwarn net.badbird5907.blib.**

-keep class lombok.** { *; }
-dontwarn lombok.**

-keep class javax.** { *; }
-dontwarn javax.**

-keep class javassist.** { *; }
-dontwarn javassist.**

-keep class net.minecraft.** { *; }
-dontwarn net.minecraft.**

-keep class com.google.** { *; }
-dontwarn com.google.**

-keep class dev.badbird.griefpreventiontp.dependencies.** { *; }
-dontwarn dev.badbird.griefpreventiontp.dependencies.**
