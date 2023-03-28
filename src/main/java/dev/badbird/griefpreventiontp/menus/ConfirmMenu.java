package dev.badbird.griefpreventiontp.menus;

import dev.badbird.griefpreventiontp.GriefPreventionTP;
import dev.badbird.griefpreventiontp.util.AdventureUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.badbird5907.blib.menu.buttons.Button;
import net.badbird5907.blib.menu.buttons.PlaceholderButton;
import net.badbird5907.blib.menu.menu.Menu;
import net.badbird5907.blib.util.CC;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;

@RequiredArgsConstructor
public class ConfirmMenu extends Menu {
    private final String action;

    private final Consumer<Boolean> callback;

    @Getter
    @Setter
    private boolean permanent = false;

    private boolean done = false;

    private static final int[] PLACEHOLDERS;

    static {
        List<Integer> a = new ArrayList<>();
        IntStream.range(0, 27).forEach((i) -> {
            if (i != 11 && i != 15 && i != 13)
                a.add(i);
        });
        PLACEHOLDERS = a.stream().mapToInt(i -> i).toArray();
    }

    @Override
    public List<Button> getButtons(Player player) {
        return Arrays.asList(
                new YesButton(),
                new NoButton(),
                new InfoButton(),
                new Placeholders()
        );
    }

    @Override
    public String getName(Player player) {
        return CC.translate(GriefPreventionTP.getInstance().getConfig().getString("menu.confirm.title", "Are you sure?"));
    }

    @Override
    public void onClose(Player player) {
        if (done) {
            return;
        }
        done = true;
        callback.accept(false);
    }

    private static class Placeholders extends PlaceholderButton {
        @Override
        public int[] getSlots() {
            return PLACEHOLDERS;
        }
    }

    private class YesButton extends Button {
        @Override
        public ItemStack getItem(Player player) {
            /*
            return new ItemBuilder(Material.GREEN_CONCRETE)
                    .name(CC.GREEN + "Yes")
                    .build();
             */
            Material material = Material.valueOf(GriefPreventionTP.getInstance().getConfig().getString("menu.confirm.items.confirm.type", "GREEN_STAINED_GLASS_PANE"));
            Component name = AdventureUtil.getComponentFromConfig("menu.confirm.items.confirm.name", "<green>Yes");
            List<Component> lore = AdventureUtil.getComponentListFromConfig("menu.confirm.items.confirm.lore", Arrays.asList("<gray>Click to confirm"));
            ItemStack item = new ItemStack(material);
            AdventureUtil.setItemDisplayName(item, name);
            AdventureUtil.setItemLore(item, lore);
            return item;
        }

        @Override
        public int getSlot() {
            return 11;
        }

        @Override
        public void onClick(Player player, int slot, ClickType clickType, InventoryClickEvent event) {
            if (done) {
                return;
            }
            done = true;
            callback.accept(true);
        }
    }

    private class NoButton extends Button {
        @Override
        public ItemStack getItem(Player player) {
            /*
            return new ItemBuilder(Material.RED_CONCRETE)
                    .name(CC.RED + "No")
                    .build();
             */
            Material material = Material.valueOf(GriefPreventionTP.getInstance().getConfig().getString("menu.confirm.items.cancel.type", "RED_STAINED_GLASS_PANE"));
            Component name = AdventureUtil.getComponentFromConfig("menu.confirm.items.cancel.name", "<red>No");
            List<Component> lore = AdventureUtil.getComponentListFromConfig("menu.confirm.items.cancel.lore", Arrays.asList("<gray>Click to cancel"));
            ItemStack item = new ItemStack(material);
            AdventureUtil.setItemDisplayName(item, name);
            AdventureUtil.setItemLore(item, lore);
            return item;
        }

        @Override
        public int getSlot() {
            return 15;
        }

        @Override
        public void onClick(Player player, int slot, ClickType clickType, InventoryClickEvent event) {
            if (done) return;
            done = true;
            callback.accept(false);
        }
    }

    private class InfoButton extends Button {
        @Override
        public ItemStack getItem(Player player) {
            /*
            ItemBuilder builder = new ItemBuilder(Material.PAPER)
                    .name(CC.GREEN + "Are you sure you want to " + action + "?")
                    .lore(CC.GRAY + "Click &aYes&7 to confirm, or &cNo&7 to cancel.");
            if (permanent) {
                builder.lore(CC.GRAY + "This action cannot be undone.");
            }
            return builder.build();
             */
            Material material = Material.valueOf(GriefPreventionTP.getInstance().getConfig().getString("menu.confirm.items.info.type", "PAPER"));
            Component name = AdventureUtil.getComponentFromConfig("menu.confirm.items.info.name", "<green>Are you sure you want to {action}?", "action", action);
            List<Component> lore = AdventureUtil.getComponentListFromConfigDef("menu.confirm.items.info.lore", Arrays.asList("<gray>Click <green>yes</green> to confirm, or <red>no</red> to cancel.",
                    "permanent:<gray>This action cannot be undone."
            ), "action", action);
            for (int i = 0; i < lore.size(); i++) {
                Component component = lore.get(i);
                if (component instanceof TextComponent tc) {
                    if (tc.content().startsWith("permanent:")) {
                        if (!permanent) {
                            lore.set(i, tc.content(tc.content().substring(10)));
                            continue;
                        }
                        lore.remove(i);
                    }
                }
            }
            ItemStack item = new ItemStack(material);
            AdventureUtil.setItemDisplayName(item, name);
            AdventureUtil.setItemLore(item, lore);
            return item;
        }

        @Override
        public int getSlot() {
            return 13;
        }
    }

}
