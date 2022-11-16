package dev.badbird.griefpreventiontp.object.config;

import dev.badbird.griefpreventiontp.GriefPreventionTP;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.badbird5907.blib.objects.TypeCallback;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

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
        Map<String, String> placeholderMap = convertArr(placeholders);
        return getGuiTitle(menu, defaultTitle, placeholderMap);
    }

    public static Component getGuiTitle(Menu menu, String defaultTitle, Map<String, String> placeholders) {
        FileConfiguration configuration = menu.getConfig();
        return MINI_MESSAGE.deserialize(replacePlaceholders(configuration.getString("title", defaultTitle), placeholders));
    }

    public static ItemStack getItem(Menu menuType, String key, String... placeholders) {
        // convert placeholders to a map
        Map<String, String> placeholderMap = convertArr(placeholders);
        return getItem(menuType, key, placeholderMap);
    }


    private static Map<String, String> convertArr(Object[] arr) {
        if (arr == null || arr.length == 0) {
            return null;
        }
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < arr.length; i += 2) {
            Object key = arr[i];
            Object value = arr[i + 1];
            map.put(key + "", value + "");
        }
        return map;
    }

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    @Getter
    @Setter
    @AllArgsConstructor
    public static class ItemConfig {
        private ItemStack itemStack;
        private String slot;
        private boolean enable = true;

        public String getSlotRaw() {
            return slot;
        }

        public int getSlot(TypeCallback<Integer, String> auto) {
            try {
                return Integer.parseInt(slot);
            } catch (NumberFormatException e) {
                return auto.callback(slot);
            }
        }
    }

    public static ItemConfig getItemConfig(Menu menuType, String key, Object... placeholders) {
        Map<String, String> placeholderMap = convertArr(placeholders);
        return getItemConfig(menuType, key, placeholderMap);
    }
    public static ItemConfig getItemConfig(Menu menuType, String key, Map<String, String> placeholders) {
        FileConfiguration configuration = menuType.getConfig();
        ConfigurationSection section = configuration.getConfigurationSection(key);
        if (section == null) {
            return null;
        }
        ItemStack itemStack = getItem(menuType, section, placeholders);
        String slot = section.getString("slot", "auto");
        boolean enable = section.getBoolean("enable", true);
        return new ItemConfig(itemStack, slot, enable);
    }

    public static ItemStack getItem(Menu menuType, ConfigurationSection section, Map<String, String> placeholders) {
        String material = section.getString("material");
        Material mat = Material.getMaterial(material);
        if (mat == null) {
            throw new IllegalArgumentException("Invalid material: " + material + " in menu: " + menuType.name());
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

    public static ItemStack getItem(Menu menuType, String key, Map<String, String> placeholders) {
        ConfigurationSection section = menuType.getConfig().getConfigurationSection(key);
        if (section == null) {
            return null;
        }
        return getItem(menuType, section, placeholders);
    }

    private static String replacePlaceholders(String in, Map<String, String> placeholders) {
        if (placeholders == null || placeholders.isEmpty()) {
            return in;
        }
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            in = in.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        return in;
    }

    public static String getString(Menu menu, String key, String defaultString) {
        @NotNull FileConfiguration configuration = menu.getConfig();
        return configuration.getString(key, defaultString);
    }
    public static List<String> getStringList(Menu menu, String key) {
        @NotNull FileConfiguration configuration = menu.getConfig();
        return configuration.getStringList(key);
    }


    public enum Menu {
        CLAIMS("claims.yml");
        private final String fileName;

        Menu(String fileName) {
            this.fileName = fileName;
        }

        public String getFileName() {
            return fileName;
        }

        public @NotNull FileConfiguration getConfig() {
            //noinspection SwitchStatementWithTooFewBranches
            switch (this) {
                case CLAIMS -> {
                    return claimsConfig;
                }
            }
            throw new IllegalStateException("Unexpected value: " + this);
        }
    }
}
