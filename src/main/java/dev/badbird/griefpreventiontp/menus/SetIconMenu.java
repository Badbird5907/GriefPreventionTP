package dev.badbird.griefpreventiontp.menus;

import dev.badbird.griefpreventiontp.GriefPreventionTP;
import dev.badbird.griefpreventiontp.api.ClaimInfo;
import dev.badbird.griefpreventiontp.object.IconWrapper;
import dev.badbird.griefpreventiontp.util.AdventureUtil;
import lombok.RequiredArgsConstructor;
import net.badbird5907.blib.menu.buttons.Button;
import net.badbird5907.blib.menu.menu.Menu;
import net.badbird5907.blib.menu.menu.PaginatedMenu;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
public class SetIconMenu extends PaginatedMenu {
    private final ClaimInfo claimInfo;
    private final Menu previousMenu;

    @Override
    public String getPagesTitle(Player player) {
        return "Set Icon";
    }

    @Override
    public List<Button> getPaginatedButtons(Player player) {
        return new ArrayList<>(GriefPreventionTP.getAllowedIcons().stream().map(IconButton::new).toList());
    }

    @RequiredArgsConstructor
    private class IconButton extends Button {
        private final IconWrapper item;

        @Override
        public ItemStack getItem(Player player) {
            IconWrapper icon = claimInfo.getIcon();
            boolean isSelected = icon != null && icon.equals(item);
            String name;
            if (icon == null) {
                // name = StringUtils.capitalize(item.isMaterial() ? item.getMaterial().name().toLowerCase().replace("_", " ") : item.getIcon().getName());
                name = item.getName();
            } else {
                // name = StringUtils.capitalize(icon.isMaterial() ? icon.getMaterial().name().toLowerCase().replace("_", " ") : icon.getIcon().getName());
                name = icon.getName();
            }
            List<Component> lore = AdventureUtil.getComponentListFromConfigDef("set-icon", "icon.lore." + (isSelected ? "select" : "deselect"),
                    !isSelected ? Arrays.asList(
                            "",
                            "<gray>Click to select this icon."
                    ) : Arrays.asList(
                            "",
                            "<gray>Click to deselect this icon."
                    ),
                    "name", name
            );
            ItemStack stack = item.getItemStack();
            AdventureUtil.setItemLore(stack, lore);
            Component displayName = AdventureUtil.getComponentFromConfig("set-icon", "icon.name",
                    "{name}",
                    "name", name
            );
            AdventureUtil.setItemDisplayName(stack, displayName);
            return stack;
        }

        @Override
        public int getSlot() {
            return 0;
        }

        @Override
        public void onClick(Player player, int slot, ClickType clickType, InventoryClickEvent event) {
            boolean isSelected = claimInfo.getIcon() != null && claimInfo.getIcon().equals(item);
            if (!isSelected) {
                claimInfo.setIcon(item);
                claimInfo.save();
                Component message = AdventureUtil.getComponentFromConfig("set-icon", "icon.message.set",
                        "{name}",
                        "name", item.getName()
                );
                AdventureUtil.sendMessage(player, message);
                player.closeInventory();
            } else {
                claimInfo.setIcon(null);
                claimInfo.save();
                Component message = AdventureUtil.getComponentFromConfig("set-icon", "icon.message.unset",
                        "{name}",
                        "name", item.getName()
                );
                AdventureUtil.sendMessage(player, message);
                player.closeInventory();
            }
            if (previousMenu != null)
                previousMenu.open(player);
        }
    }
}
