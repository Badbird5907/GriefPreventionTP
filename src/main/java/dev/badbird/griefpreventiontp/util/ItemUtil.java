package dev.badbird.griefpreventiontp.util;

import dev.badbird.griefpreventiontp.GriefPreventionTP;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class ItemUtil {
    public static void setPersistentData(ItemStack itemStack, String key, PersistentDataType type, Object value) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.getPersistentDataContainer().set(new NamespacedKey(GriefPreventionTP.getInstance(), key), type, value);
        itemStack.setItemMeta(meta);
    }

    public static String getPersistentDataString(ItemStack itemStack, String key) {
        ItemMeta meta = itemStack.getItemMeta();
        return meta.getPersistentDataContainer().get(new NamespacedKey(GriefPreventionTP.getInstance(), key), PersistentDataType.STRING);
    }
}
