package dev.badbird.griefpreventiontp.util;

import dev.badbird.griefpreventiontp.GriefPreventionTP;
import net.badbird5907.blib.util.Logger;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public class AdventureUtil {
    private static BukkitAudiences adventure;
    private static boolean runningOnPaper = false;

    public static void init() {
        adventure();
        try {
            Class<? extends Player> playerClass = Player.class;
            playerClass.getMethod("sendMessage", Component.class);
            runningOnPaper = true;
            Logger.info("Paper detected, Using it's implementation of Adventure");
        } catch (NoSuchMethodException e) {
            runningOnPaper = false;
            Logger.info("Paper not detected, Using adapter.");
        }
    }
    public static BukkitAudiences adventure() {
        if (adventure == null)
            adventure = BukkitAudiences.create(GriefPreventionTP.getInstance());
        return adventure;
    }

    public static void sendMessage(Player player, Component component) {
        if (runningOnPaper)
            player.sendMessage(component);
        else
            adventure().player(player).sendMessage(component);
    }
}
