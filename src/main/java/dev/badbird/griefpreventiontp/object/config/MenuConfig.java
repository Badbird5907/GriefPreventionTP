package dev.badbird.griefpreventiontp.object.config;

import dev.badbird.griefpreventiontp.GriefPreventionTP;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MenuConfig {
    private static FileConfiguration claimsConfig;

    public static void init(GriefPreventionTP plugin) {
        File claimsFile = new File(plugin.getDataFolder(), "menu/claims.yml");
        if (!claimsFile.exists()) {
            plugin.saveResource("menu/claims.yml", false);
        }
        claimsConfig = YamlConfiguration.loadConfiguration(claimsFile);
    }

    public static Component getGuiTitle(Menu menu, String defaultTitle, String... placeholders) {
        Map<String, String> placeholderMap = convertStringArray(placeholders);
        return getGuiTitle(menu, defaultTitle, placeholderMap);
    }
    public static Component getGuiTitle(Menu menu, String defaultTitle, Map<String, String> placeholders) {
        FileConfiguration configuration =  menu.getConfig();
        return MINI_MESSAGE.deserialize(replacePlaceholders(configuration.getString("title", defaultTitle), placeholders));
    }

    public static ItemStack getItem(Menu menuType, String key, String... placeholders) {
        // convert placeholders to a map
        Map<String, String> placeholderMap = convertStringArray(placeholders);
        return getItem(menuType, key, placeholderMap);
    }

    private static Map<String, String> convertStringArray(String[] arr) {
        if (arr == null || arr.length == 0) {
            return null;
        }
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < arr.length; i += 2) {
            map.put(arr[i], arr[i + 1]);
        }
        return map;
    }

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    public static ItemStack getItem(Menu menuType, String key, Map<String, String> placeholders) {
        ConfigurationSection section = menuType.getConfig().getConfigurationSection(key);
        if (section == null) {
            return null;
        }
        String material = section.getString("material");
        Material mat = Material.getMaterial(material);
        if (mat == null) {
            throw new IllegalArgumentException("Invalid material: " + material + " for menu item: " + key + " in menu: " + menuType.name());
        }
        int amount = section.getInt("amount", 1);
        String name = replacePlaceholders(section.getString("name"), placeholders);
        List<String> lore = section.getStringList("lore").stream().map(
                str -> replacePlaceholders(str, placeholders)
        ).toList();
        ItemStack item = new ItemStack(mat, amount);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(MINI_MESSAGE.deserialize(name));
        meta.lore(lore.stream().map(MINI_MESSAGE::deserialize).collect(Collectors.toList()));
        item.setItemMeta(meta);
        return item;
    }

    private static String replacePlaceholders(String in, Map<String, String> placeholders) {
        if (placeholders == null || placeholders.isEmpty()) {
            return in;
        }
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            in = in.replace("%" +entry.getKey() + "%", entry.getValue());
        }
        return in;
    }

    public enum Menu {
        CLAIMS("claims.yml");
        private String fileName;

        Menu(String fileName) {
            this.fileName = fileName;
        }

        public String getFileName() {
            return fileName;
        }

        public FileConfiguration getConfig() {
            //noinspection SwitchStatementWithTooFewBranches
            switch (this) {
                case CLAIMS -> {
                    return claimsConfig;
                }
            }
            return null;
        }
    }
}
