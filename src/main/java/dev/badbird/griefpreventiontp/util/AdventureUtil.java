package dev.badbird.griefpreventiontp.util;

import dev.badbird.griefpreventiontp.GriefPreventionTP;
import net.badbird5907.blib.util.Logger;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
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

    public static Component getComponentFromConfig(String key, String def, Object... placeholders) {
        String raw = GriefPreventionTP.getInstance().getConfig().getString(key, def);
        if (placeholders != null && placeholders.length >= 2) {
            // placeholders used like this: ["key1", "value1", "key2", "value2"]
            for (int i = 0; i < placeholders.length; i += 2) {
                String key1 = "{" + placeholders[i] + "}";
                String value = placeholders[i + 1].toString();
                raw = raw.replace(key1, value);
            }
        }
        return MiniMessage.miniMessage().deserialize(raw);
    }

    public static List<Component> getComponentListFromConfig(String key, Object... placeholders) {
        List<String> raw = GriefPreventionTP.getInstance().getConfig().getStringList(key);
        List<Component> components = new ArrayList<>();
        for (String s : raw) {
            if (placeholders != null && placeholders.length >= 2) {
                // placeholders used like this: ["key1", "value1", "key2", "value2"]
                for (int i = 0; i < placeholders.length; i += 2) {
                    String key1 = "{" + placeholders[i] + "}";
                    String value = placeholders[i + 1].toString();
                    s = s.replace(key1, value);
                }
            }
            components.add(MiniMessage.miniMessage().deserialize(s));
        }
        return components;
    }

    public static List<Component> getComponentListFromConfigDef(String key, List<String> defaults, Object... placeholders) {
        List<String> raw = GriefPreventionTP.getInstance().getConfig().getStringList(key);
        if (placeholders != null && placeholders.length >= 2) {
            // placeholders used like this: ["key1", "value1", "key2", "value2"]
            for (int i = 0; i < placeholders.length; i += 2) {
                String key1 = "{" + placeholders[i] + "}";
                String value = placeholders[i + 1].toString();
                for (int i1 = 0; i1 < raw.size(); i1++) {
                    String s = raw.get(i1);
                    s = s.replace(key1, value);
                    raw.set(i1, s);
                }
                for (int i1 = 0; i1 < defaults.size(); i1++) {
                    String s = defaults.get(i1);
                    s = s.replace(key1, value);
                    defaults.set(i1, s);
                }
            }
        }
        List<Component> components = new ArrayList<>();
        if (raw.isEmpty())
            for (String s : defaults)
                components.add(MiniMessage.miniMessage().deserialize(s));
        else
            for (String s : raw)
                components.add(MiniMessage.miniMessage().deserialize(s));
        return components;
    }
}
