package dev.badbird.griefpreventiontp.menus;

import dev.badbird.griefpreventiontp.GriefPreventionTP;
import dev.badbird.griefpreventiontp.api.ClaimInfo;
import dev.badbird.griefpreventiontp.manager.MenuManager;
import dev.badbird.griefpreventiontp.manager.MessageManager;
import dev.badbird.griefpreventiontp.object.ComponentQuestionConversation;
import dev.badbird.griefpreventiontp.util.AdventureUtil;
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
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.milkbowl.vault.economy.Economy;
import net.octopvp.commander.command.CommandInfo;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@RequiredArgsConstructor
public class ManageClaimMenu extends Menu {
    private static final List<Integer> PLACEHOLDERS = new ArrayList<>();

    static {
        IntStream.range(0, 36).forEach((i) -> {
            if (i != 11 && i != 15 && i != 36) PLACEHOLDERS.add(i);
        });
    }

    private final ClaimInfo claimInfo;
    private final Menu previousMenu;
    private boolean hasPublicPerm = true, hasPrivatePerm = true;

    @Override
    public List<Button> getButtons(Player player) {
        hasPublicPerm = player.hasPermission("gptp.command.public");
        hasPrivatePerm = player.hasPermission("gptp.command.private");
        Claim claim = claimInfo.getClaim();
        ArrayList<Button> buttons = new ArrayList<>();
        if (claim.getOwnerID().equals(player.getUniqueId()) && MenuManager.getBoolean("manage-claim", "enable-delete", true)) // Only allow owner to delete claim
            buttons.add(new DeleteButton());
        buttons.add(new RenameButton());
        buttons.add(new ClaimButton());
        if (hasPublicPerm || hasPrivatePerm) buttons.add(new PublicButton(player));
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
            public ItemStack getItem(Player player) {
                ItemStack item = new ItemBuilder(Material.valueOf(plugin.getConfig().getString("menu.back-button.type"))).build();
                Component component = MessageManager.getComponent("menu.back-button.name");
                AdventureUtil.setItemDisplayName(item, component);
                return item;
            }

            @Override
            public int getSlot() {
                return 27;
            }
        };
    }

    @Override
    public String getName(Player player) {
        String title = MenuManager.getString("manage-claim", "title", "Manage Claim - {claim}").replace("{claim}", claimInfo.getName());
        if (title.length() > 29) title = title.substring(0, 29) + "...";
        return title;
    }

    @Override
    public Button getCloseButton() {
        return new CloseButton() {
            @Override
            public ItemStack getItem(Player player) {
                ItemStack itemStack = new ItemStack(Material.valueOf(plugin.getConfig().getString("menu.close-button.type")));
                Component name = AdventureUtil.getComponentFromConfig("claims", "menu.close-button.title", "<red>Close");
                AdventureUtil.setItemDisplayName(itemStack, name);
                List<Component> lore = AdventureUtil.getComponentListFromConfig("claims", "menu.close-button.lore");
                if (!lore.isEmpty()) AdventureUtil.setItemLore(itemStack, lore);
                return itemStack;
            }
        };
    }

    private class Placeholders extends PlaceholderButton {
        @Override
        public int[] getSlots() {
            List<Integer> a = new ArrayList<>(PLACEHOLDERS);
            if (!hasPublicPerm && !hasPrivatePerm) {
                a.add(13);
                a.add(15);
            }
            return a.stream().mapToInt(i -> i).toArray();
        }
    }

    private final class DeleteButton extends Button {
        @Override
        public ItemStack getItem(Player player) {
            ItemStack item = new ItemStack(
                    Material.valueOf(MenuManager.getString("manage-claim", "delete-claim.type", "REDSTONE_BLOCK"))
            );
            Component name = AdventureUtil.getComponentFromConfig("manage-claim", "delete-claim.name", "<red>Delete Claim");
            List<Component> lore = AdventureUtil.getComponentListFromConfig("manage-claim", "delete-claim.lore", Arrays.asList("<gray>Click to delete this claim."));
            AdventureUtil.setItemDisplayName(item, name);
            AdventureUtil.setItemLore(item, lore);
            return item;
        }

        @Override
        public int getSlot() {
            return 35;
        }

        @Override
        public void onClick(Player player, int slot, ClickType clickType, InventoryClickEvent event) {
            new ConfirmMenu(MenuManager.getString("manage-claim", "action-delete-this-claim", "delete this claim"), (res) -> {
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
            /*
            return new ItemBuilder(Material.NAME_TAG)
                    .setName(CC.GREEN + "Rename")
                    .lore("", CC.GRAY + "Click to rename.")
                    .build();
             */
            ItemStack itemStack = new ItemStack(
                    Material.valueOf(MenuManager.getString("manage-claim", "rename-claim.type", "NAME_TAG"))
            );
            Component name = AdventureUtil.getComponentFromConfig("manage-claim", "rename-claim.title", "<green>Rename Claim");
            AdventureUtil.setItemDisplayName(itemStack, name);
            List<Component> lore = AdventureUtil.getComponentListFromConfigDef("manage-claim", "rename-claim.lore", Arrays.asList("", "<gray>Click to rename."));
            if (!lore.isEmpty()) AdventureUtil.setItemLore(itemStack, lore);
            return itemStack;
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
            if (!hasPublicPerm && !hasPrivatePerm) {
                return null;
            }
            boolean isPublic = claimInfo.isPublic();
            int cost = GriefPreventionTP.getInstance().getClaimManager().getCostToMakePublic(player);
            String base = "toggle-public." + (isPublic ? "public" : "private") + ".";
            ItemStack item = new ItemStack(
                    Material.valueOf(MenuManager.getString("manage-claim", base + "type", "OAK_DOOR"))
            );
            // AdventureUtil.setItemDisplayName(item, LegacyComponentSerializer.legacySection().deserialize(CC.translate(claimInfo.isPublic() ? CC.GREEN + "Public" : CC.RED + "Private")).decoration(TextDecoration.ITALIC, false));
            Component name = AdventureUtil.getComponentFromConfig("manage-claim", base + "name", claimInfo.isPublic() ? "<green>Public" : "<red>Private");
            AdventureUtil.setItemDisplayName(item, name);
            /*
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            if (!isPublic && cost > 0) {
                Component component = MessageManager.getComponent("messages.verify-public-cost.menu", cost).decoration(TextDecoration.ITALIC, false);
                lore.add(component);
            }
            lore.add(LegacyComponentSerializer.legacySection().deserialize(CC.GRAY + "Click to toggle public/private.").decoration(TextDecoration.ITALIC, false));
             */
            List<Component> lore = AdventureUtil.getComponentListFromConfigDef("manage-claim", base + "lore",
                    isPublic ? Arrays.asList(
                            "cost:<gray>Cost: <green>${cost}",
                            "<gray>Click to toggle to public."
                    ) : Arrays.asList(
                            "<gray>Click to toggle to private."
                    ),
                    "cost", cost
            );
            for (int i = 0; i < lore.size(); i++) {
                Component component = lore.get(i);
                String content = PlainTextComponentSerializer.plainText().serialize(component); // TODO optimize
                if (content.startsWith("cost:")) {
                    if (!isPublic && cost > 0) {
                        lore.set(i, component.replaceText(TextReplacementConfig.builder()
                                .match("cost:")
                                .replacement("")
                                .build()));
                        continue;
                    }
                    lore.remove(component);
                }
            }
            if (!lore.isEmpty()) AdventureUtil.setItemLore(item, lore);
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
            /*
            ItemBuilder builder = new ItemBuilder(Material.PLAYER_HEAD).setName(CC.GREEN + claimInfo.getName())
                    .lore(CC.GRAY + "Owner: " + claimInfo.getOwnerName())
                    .amount(claimInfo.getPlayerClaimCount());
            builder.lore(CC.GRAY + "ID: " + claimInfo.getClaimID());
            if (showCoords)
                builder.lore(CC.D_GRAY + claimInfo.getSpawn().getX() + ", " + claimInfo.getSpawn().getY() + ", " + claimInfo.getSpawn().getZ());
            if (!valid) builder.lore("", CC.RED + "No spawn set!");

            ItemStack stack = builder.build();
             */
            ItemStack stack = new ItemStack(Material.PLAYER_HEAD);
            Component name = AdventureUtil.getComponentFromConfig("manage-claim", "claim.name", "<green>{name}", "name", claimInfo.getName(), "owner", claimInfo.getOwnerName());
            AdventureUtil.setItemDisplayName(stack, name);
            List<Component> lore = AdventureUtil.getComponentListFromConfigDef("manage-claim", "claim.lore", Arrays.asList("<gray>Owner: {owner}", "<gray>ID: {id}", "<gray>{x}, {y}, {z}"), "id", claimInfo.getClaimID(), "x", claimInfo.getSpawn().getX(), "y", claimInfo.getSpawn().getY(), "z", claimInfo.getSpawn().getZ(), "owner", claimInfo.getOwnerName(), "name", claimInfo.getName());
            AdventureUtil.setItemLore(stack, lore);
            UUID owner = claimInfo.getOwner();
            SkullMeta skullMeta = (SkullMeta) stack.getItemMeta();
            skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(owner));
            stack.setItemMeta(skullMeta);
            return stack;
        }

        @Override
        public int getSlot() {
            return hasPublicPerm || hasPrivatePerm ? 13 : 15;
        }

        @Override
        public void onClick(Player player, int slot, ClickType clickType, InventoryClickEvent event) {

        }
    }
}
