package dev.badbird.griefpreventiontp.object;

import dev.badbird.griefpreventiontp.GriefPreventionTP;
import dev.badbird.griefpreventiontp.util.AdventureUtil;
import dev.badbird.griefpreventiontp.util.ItemUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.badbird5907.blib.util.ItemBuilder;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;

@Getter
@Setter
public class IconWrapper {
    private CustomIcon icon;
    private Material material;

    public IconWrapper(CustomIcon icon) {
        this.icon = icon;
    }

    public IconWrapper(Material material) {
        this.material = material;
    }

    public boolean isMaterial() {
        return material != null;
    }

    public boolean isCustomIcon() {
        return icon != null;
    }

    public String getName() {
        if (isMaterial()) {
            return StringUtils.capitalize(material.name().toLowerCase().replace("_", " "));
        } else if (isCustomIcon()) {
            return StringUtils.capitalize(icon.getName());
        }
        return null;
    }

    public boolean equalsStack(ItemStack stack) {
        if (isMaterial()) {
            return stack.getType() == material;
        } else if (isCustomIcon()) {
            return Objects.equals(ItemUtil.getPersistentDataString(stack, "gptp-icon-id"), icon.getId());
        }
        return false;
    }
    public boolean equals(IconWrapper wrapper) {
        if (isMaterial()) {
            return wrapper.isMaterial() && wrapper.getMaterial() == material;
        } else if (isCustomIcon()) {
            return wrapper.isCustomIcon() && wrapper.getIcon().getId().equals(icon.getId());
        }
        return false;
    }

    public ItemStack getItemStack() {
        if (isMaterial()) {
            return new ItemStack(material);
        }
        ItemStack stack = new ItemBuilder(Material.PLAYER_HEAD)
                .toSkullBuilder()
                .base64Skin(icon.getTexture())
                .buildSkull();
        ItemUtil.setPersistentData(stack, "gptp-icon-id", PersistentDataType.STRING, icon.getId());
        AdventureUtil.setItemDisplayName(stack, MiniMessage.miniMessage().deserialize(icon.getName()));
        return stack;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class CustomIcon {
        private String id;
        private String name; // mini message
        private String texture; // base64
    }
}
