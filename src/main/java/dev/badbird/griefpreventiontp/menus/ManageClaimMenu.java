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
import net.badbird5907.blib.menu.buttons.impl.CloseButton;
import net.badbird5907.blib.menu.menu.Menu;
import net.badbird5907.blib.util.CC;
import net.badbird5907.blib.util.ItemBuilder;
import net.milkbowl.vault.economy.Economy;
import net.octopvp.commander.command.CommandInfo;
import org.bukkit.Material;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@RequiredArgsConstructor
public class ManageClaimMenu extends Menu {
    private final ClaimInfo claimInfo;

    private static final int[] PLACEHOLDERS;

    static {
        List<Integer> a = new ArrayList<>();
        IntStream.range(0, 36).forEach((i) -> {
            if (i != 11 && i != 15 && i != 36)
                a.add(i);
        });
        PLACEHOLDERS = a.stream().mapToInt(i -> i).toArray();
    }

    @Override
    public List<Button> getButtons(Player player) {
        Claim claim = claimInfo.getClaim();
        ArrayList<Button> buttons = new ArrayList<>();
        if (claim.ownerID.equals(player.getUniqueId()) && GriefPreventionTP.getInstance().getConfig().getBoolean("menu.enable-delete")) //Only allow owner to delete claim
            buttons.add(new DeleteButton());
        buttons.add(new RenameButton());
        buttons.add(new PublicButton(player));
        buttons.add(new Placeholders());
        return buttons;
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
            return PLACEHOLDERS;
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
                }else {
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
                    .lore(CC.GRAY + "Click to rename")
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
            return new ItemBuilder(Material.OAK_DOOR)
                    .name(claimInfo.isPublic() ? CC.GREEN + "Public" : CC.RED + "Private")
                    .lore(CC.GRAY + "Click to toggle public/private.")
                    .build();
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
                        economy.withdrawPlayer(player, cost);
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
}
