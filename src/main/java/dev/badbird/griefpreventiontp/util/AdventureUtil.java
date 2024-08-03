package dev.badbird.griefpreventiontp.util;

import dev.badbird.griefpreventiontp.GriefPreventionTP;
import dev.badbird.griefpreventiontp.manager.MenuManager;
import net.badbird5907.blib.util.Logger;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
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

    public static void sendMessage(CommandSender sender, Component component) {
        if (runningOnPaper)
            sender.sendMessage(component);
        else
            adventure().sender(sender).sendMessage(component);
    }

    @SuppressWarnings("deprecation")
    public static void setItemDisplayName(ItemStack item, Component component) {
        if (runningOnPaper)
            item.editMeta(meta -> meta.displayName(cleanItalics(component)));
        else {
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(LegacyComponentSerializer.legacySection().serialize(cleanItalics(component)));
            item.setItemMeta(meta);
        }
    }

    @SuppressWarnings("deprecation")
    public static void setItemLore(ItemStack item, List<Component> component) {
        if (runningOnPaper)
            item.editMeta(meta -> meta.lore(component.stream().map(AdventureUtil::cleanItalics).toList()));
        else {
            ItemMeta meta = item.getItemMeta();
            List<String> lore = new ArrayList<>();
            for (Component c : component)
                lore.add(LegacyComponentSerializer.legacySection().serialize(c));
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
    }

    public static String getMiniMessageFromConfig(String menu, String key, String def, Object... placeholders) {
        String raw = MenuManager.getMenu(menu).getString(key, def);
        raw = replaceVariables(raw, placeholders);
        return raw;
    }
    public static Component getComponentFromConfig(String menu, String key, String def, Object... placeholders) {
        return cleanItalics(MiniMessage.miniMessage().deserialize(
                getMiniMessageFromConfig(menu, key, def, placeholders)
        ));
    }


    public static List<Component> getComponentListFromConfig(String menu, String key, Object... placeholders) {
        List<String> raw = MenuManager.getMenu(menu).getStringList(key);
        List<Component> components = new ArrayList<>();
        for (String s : raw) {
            s = replaceVariables(s, placeholders);
            components.add(cleanItalics(MiniMessage.miniMessage().deserialize(s)));
        }
        return components;
    }

    public static List<String> getMiniMessageListFromConfig(String menu, String key, Object... placeholders) {
        List<String> raw = MenuManager.getMenu(menu).getStringList(key);
        raw.replaceAll(s -> replaceVariables(s, placeholders));
        return raw;
    }

    private static String replaceVariables(String s, Object[] placeholders) {
        if (placeholders != null && placeholders.length >= 2) {
            // placeholders used like this: ["key1", "value1", "key2", "value2"]
            for (int i = 0; i < placeholders.length; i += 2) {
                String key1 = "{" + placeholders[i] + "}";
                String value = placeholders[i + 1].toString();
                s = s.replace(key1, value);
            }
        }
        return s;
    }

    public static List<String> getMiniMessageListFromConfigDef(String menu, String key, List<String> defaults, Object... placeholders) {
        List<String> raw = MenuManager.getMenu(menu).getStringList(key);
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
        if (raw.isEmpty()) return defaults;
        else return raw;
    }

    public static List<Component> getComponentListFromConfigDef(String menu, String key, List<String> defaults, Object... placeholders) {
        return new ArrayList<>(getMiniMessageListFromConfigDef(menu, key, defaults, placeholders).stream().map(str -> cleanItalics(MiniMessage.miniMessage().deserialize(str))).toList());
    }

    public static Component cleanItalics(Component in) {
        return Component.empty().decoration(TextDecoration.ITALIC, false).append(in);
    }
}
