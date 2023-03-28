package dev.badbird.griefpreventiontp.util;

import dev.badbird.griefpreventiontp.GriefPreventionTP;
import net.badbird5907.blib.util.Logger;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.platform.bukkit.BukkitComponentSerializer;
import net.kyori.adventure.platform.bukkit.MinecraftComponentSerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

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
    @SuppressWarnings("deprecation")
    public static void setItemDisplayName(ItemStack item, Component component) {
        if (runningOnPaper)
            item.editMeta(meta -> meta.displayName(component));
        else {
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(LegacyComponentSerializer.legacySection().serialize(component));
            item.setItemMeta(meta);
        }
    }
    @SuppressWarnings("deprecation")
    public static void setItemLore(ItemStack item, List<Component> component) {
        if (runningOnPaper)
            item.editMeta(meta -> meta.lore(component));
        else {
            ItemMeta meta = item.getItemMeta();
            List<String> lore = new ArrayList<>();
            for (Component c : component)
                lore.add(LegacyComponentSerializer.legacySection().serialize(c));
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
    }
}
