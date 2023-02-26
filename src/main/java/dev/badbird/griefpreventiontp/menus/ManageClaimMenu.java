package dev.badbird.griefpreventiontp.menus;

import dev.badbird.griefpreventiontp.GriefPreventionTP;
import dev.badbird.griefpreventiontp.api.ClaimInfo;
import dev.badbird.griefpreventiontp.manager.MessageManager;
import dev.badbird.griefpreventiontp.manager.TPClaimManager;
import dev.badbird.griefpreventiontp.object.ComponentQuestionConversation;
import lombok.RequiredArgsConstructor;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import net.badbird5907.blib.menu.buttons.Button;
import net.badbird5907.blib.menu.buttons.PlaceholderButton;
import net.badbird5907.blib.menu.buttons.impl.BackButton;
import net.badbird5907.blib.menu.buttons.impl.CloseButton;
import net.badbird5907.blib.menu.menu.Menu;
import net.badbird5907.blib.util.CC;
import net.badbird5907.blib.util.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.milkbowl.vault.economy.Economy;
import net.octopvp.commander.command.CommandInfo;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static net.badbird5907.blib.util.CC.GRAY;
import static net.badbird5907.blib.util.XMaterial.GRAY_STAINED_GLASS_PANE;

@RequiredArgsConstructor
public class ManageClaimMenu extends Menu {
    private final ClaimInfo claimInfo;
    private final Menu previousMenu;
    private boolean showCoords = GriefPreventionTP.getInstance().getConfig().getBoolean("menu.show-coordinates");

    private static final List<Integer> PLACEHOLDERS = new ArrayList<>();

    private boolean hasPublicPerm = true;

    static {
        IntStream.range(0, 36).forEach((i) -> {
            if (i != 11 && i != 15 && i != 36)
                PLACEHOLDERS.add(i);
        });
    }

    @Override
    public List<Button> getButtons(Player player) {
        hasPublicPerm = player.hasPermission("gptp.command.public");
        Claim claim = claimInfo.getClaim();
        ArrayList<Button> buttons = new ArrayList<>();
        if (claim.ownerID.equals(player.getUniqueId()) && GriefPreventionTP.getInstance().getConfig().getBoolean("menu.enable-delete")) //Only allow owner to delete claim
            buttons.add(new DeleteButton());
        buttons.add(new RenameButton());
        buttons.add(new ClaimButton());
        if (hasPublicPerm) buttons.add(new PublicButton(player));
        buttons.add(new Placeholders());
        return buttons;
    }

    @Override
    public Button getBackButton(Player player) {
        if (previousMenu == null) return null;
        return new BackButton() {
            @Override
            public void clicked(Player player, int slot, ClickType clickType, InventoryClickEvent event) {
                previousMenu.open(player);
            }

            @Override
            public int getSlot() {
                return 27;
            }
        };
    }

    @Override
    public String getName(Player player) {
        String title = "Manage Claim - " + claimInfo.getName();
        if (title.length() > 29)
            title = title.substring(0, 29) + "...";
        return title;
    }

    private class Placeholders extends PlaceholderButton {
        @Override
        public int[] getSlots() {
            List<Integer> a = new ArrayList<>(PLACEHOLDERS);
            if (!hasPublicPerm) {
                a.add(13);
                a.add(15);
            }
            return a.stream().mapToInt(i -> i).toArray();
        }
    }

    @Override
    public Button getCloseButton() {
        return new CloseButton();
    }

    private final class DeleteButton extends Button {
        @Override
        public ItemStack getItem(Player player) {
            return new ItemBuilder(Material.REDSTONE_BLOCK)
                    .setName(CC.RED + "Delete Claim")
                    .lore(CC.GRAY + "Click to delete this claim.")
                    .build();
        }

        @Override
        public int getSlot() {
            return 35;
        }

        @Override
        public void onClick(Player player, int slot, ClickType clickType, InventoryClickEvent event) {
            new ConfirmMenu("delete this claim", (res) -> {
                if (res) {
                    Claim claim = claimInfo.getClaim();
                    if (!claim.getOwnerID().equals(player.getUniqueId())) {
                        MessageManager.sendMessage(player, "messages.no-permission");
                        return;
                    }
                    GriefPrevention.instance.dataStore.deleteClaim(claim);
                    MessageManager.sendMessage(player, "messages.manager-gui.claim-deleted");
                    player.closeInventory();
                    new ClaimsMenu(player.getUniqueId()).open(player);
                } else {
                    player.closeInventory();
                    //new ClaimsMenu(player.getUniqueId()).open(player);
                }
            }).open(player);
        }
    }

    private final class RenameButton extends Button {

        @Override
        public ItemStack getItem(Player player) {
            return new ItemBuilder(Material.NAME_TAG)
                    .setName(CC.GREEN + "Rename")
                    .lore("", CC.GRAY + "Click to rename.")
                    .build();
        }

        @Override
        public int getSlot() {
            return 11;
        }

        @Override
        public void onClick(Player player, int slot, ClickType clickType, InventoryClickEvent event) {
            CommandInfo ci = GriefPreventionTP.getInstance().getCommander().getCommandMap().get("rename");
            if (ci != null) {
                if (ci.isOnCooldown(player.getUniqueId())) {
                    player.sendMessage(CC.RED + "You must wait " + ci.getCooldownSeconds(player.getUniqueId()) + " seconds before using this again.");
                    return;
                }
            }
            new ComponentQuestionConversation(MessageManager.getComponent("messages.manager-gui.rename-claim"), (response) -> {
                if (response.length() > GriefPreventionTP.getInstance().getConfig().getInt("max-claim-name-length")) {
                    MessageManager.sendMessage(player, "messages.name-too-long");
                    return Prompt.END_OF_CONVERSATION;
                }
                claimInfo.setName(response);
                MessageManager.sendMessage(player, "messages.manager-gui.claim-renamed", claimInfo.getName());
                claimInfo.save();
                ci.addCooldown(player.getUniqueId());
                return Prompt.END_OF_CONVERSATION;
            }).start(player);
        }
    }

    private final class PublicButton extends Button {

        private int canMakePublic;

        public PublicButton(Player player) {
            if (claimInfo.isPublic()) {
                canMakePublic = 0;
            } else {
                canMakePublic = GriefPreventionTP.getInstance().getClaimManager().canMakePublic(player);
            }
        }

        @Override
        public ItemStack getItem(Player player) {
            if (!hasPublicPerm) {
                return null;
            }
            boolean isPublic = claimInfo.isPublic();
            int cost = GriefPreventionTP.getInstance().getClaimManager().getCostToMakePublic(player);
            ItemStack item = new ItemStack(Material.OAK_DOOR);
            ItemMeta meta = item.getItemMeta();
            meta.displayName(LegacyComponentSerializer.legacySection().deserialize(CC.translate(claimInfo.isPublic() ? CC.GREEN + "Public" : CC.RED + "Private")).decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            if (!isPublic && cost > 0) {
                Component component = MessageManager.getComponent("messages.verify-public-cost.menu", cost)
                        .decoration(TextDecoration.ITALIC, false);
                lore.add(component);
            }
            lore.add(LegacyComponentSerializer.legacySection().deserialize(CC.GRAY + "Click to toggle public/private.")
                    .decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
            item.setItemMeta(meta);
            return item;
        }

        @Override
        public int getSlot() {
            return 15;
        }

        @Override
        public void onClick(Player player, int slot, ClickType clickType, InventoryClickEvent event) {
            if (!plugin.getConfig().getBoolean("enable-public")) {
                MessageManager.sendMessage(player, "messages.public-disabled");
                return;
            }
            CommandInfo ci = GriefPreventionTP.getInstance().getCommander().getCommandMap().get("public");
            if (ci != null) {
                if (ci.isOnCooldown(player.getUniqueId())) {
                    player.sendMessage(CC.RED + "You must wait " + ci.getCooldownSeconds(player.getUniqueId()) + " seconds before using this again.");
                    return;
                }
            }
            int cost = 0;
            if (claimInfo.isPublic() || canMakePublic == 0) {
                if (claimInfo.isPublic()) {
                    cost = GriefPreventionTP.getInstance().getClaimManager().getCostToMakePublic(player);
                    if (cost > 0) {
                        Economy economy = (Economy) GriefPreventionTP.getInstance().getClaimManager().getVaultEconomy();
                        if (!player.hasPermission("gptp.bypass.public")) economy.withdrawPlayer(player, cost);
                    }
                }
                claimInfo.setPublic(!claimInfo.isPublic());
                claimInfo.save();
            } else {
                if (canMakePublic == 2) {
                    MessageManager.sendMessage(player, "messages.not-enough-money");
                    update(player);
                    return;
                }
                MessageManager.sendMessage(player, "messages.max-public-exceeded");
                update(player);
                return;
            }
            if (claimInfo.isPublic()) MessageManager.sendMessage(player, "messages.public-on", cost);
            else MessageManager.sendMessage(player, "messages.public-off");
            update(player);
            ci.addCooldown(player.getUniqueId());
        }
    }

    private class ClaimButton extends Button {
        public ClaimButton() {
            claimInfo.checkValid();
        }
        @Override
        public ItemStack getItem(Player player) {
            boolean valid = claimInfo.getSpawn() != null;
            ItemBuilder builder = new ItemBuilder(Material.PLAYER_HEAD).setName(CC.GREEN + claimInfo.getName())
                    .lore(CC.GRAY + "Owner: " + claimInfo.getOwnerName())
                    .amount(claimInfo.getPlayerClaimCount());
            builder.lore(CC.GRAY + "ID: " + claimInfo.getClaimID());
            if (showCoords) builder.lore(CC.D_GRAY + claimInfo.getSpawn().getX() + ", " + claimInfo.getSpawn().getY() + ", " + claimInfo.getSpawn().getZ());
            if (!valid) builder.lore("", CC.RED + "No spawn set!");

            ItemStack stack = builder.build();
            UUID owner = claimInfo.getOwner();
            SkullMeta skullMeta = (SkullMeta) stack.getItemMeta();
            skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(owner));
            stack.setItemMeta(skullMeta);
            return stack;
        }

        @Override
        public int getSlot() {
            return hasPublicPerm ? 13 : 15;
        }

        @Override
        public void onClick(Player player, int slot, ClickType clickType, InventoryClickEvent event) {

        }
    }
}
