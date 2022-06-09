package dev.badbird.griefpreventiontp.menus;

import lombok.RequiredArgsConstructor;
import net.badbird5907.blib.menu.buttons.Button;
import net.badbird5907.blib.menu.buttons.PlaceholderButton;
import net.badbird5907.blib.menu.menu.Menu;
import net.badbird5907.blib.util.CC;
import net.badbird5907.blib.util.ItemBuilder;
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
                new YesButton(callback),
                new NoButton(callback),
                new InfoButton(action),
                new Placeholders()
        );
    }

    @Override
    public String getName(Player player) {
        return "Are you sure?";
    }

    @Override
    public void onClose(Player player) {
        callback.accept(false);
    }

    private static class Placeholders extends PlaceholderButton {
        @Override
        public int[] getSlots() {
            return PLACEHOLDERS;
        }
    }
    @RequiredArgsConstructor
    private static class YesButton extends Button {
        private final Consumer<Boolean> callback;
        @Override
        public ItemStack getItem(Player player) {
            return new ItemBuilder(Material.GREEN_CONCRETE)
                    .name(CC.GREEN + "Yes")
                    .build();
        }

        @Override
        public int getSlot() {
            return 11;
        }

        @Override
        public void onClick(Player player, int slot, ClickType clickType, InventoryClickEvent event) {
            callback.accept(true);
        }
    }
    @RequiredArgsConstructor
    private static class NoButton extends Button {
        private final Consumer<Boolean> callback;

        @Override
        public ItemStack getItem(Player player) {
            return new ItemBuilder(Material.RED_CONCRETE)
                    .name(CC.RED + "No")
                    .build();
        }

        @Override
        public int getSlot() {
            return 15;
        }

        @Override
        public void onClick(Player player, int slot, ClickType clickType, InventoryClickEvent event) {
            callback.accept(false);
        }
    }

    @RequiredArgsConstructor
    private static class InfoButton extends Button {
        private final String action;
        @Override
        public ItemStack getItem(Player player) {
            return new ItemBuilder(Material.PAPER)
                    .name(CC.GREEN + "Are you sure you want to " + action + "?")
                    .lore(CC.GRAY + "Click &aYes&7 to confirm, or &cNo&7 to cancel.")
                    .build();
        }

        @Override
        public int getSlot() {
            return 13;
        }
    }

}
