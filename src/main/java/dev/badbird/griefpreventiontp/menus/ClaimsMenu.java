package dev.badbird.griefpreventiontp.menus;

import com.destroystokyo.paper.profile.PlayerProfile;
import dev.badbird.griefpreventiontp.GriefPreventionTP;
import dev.badbird.griefpreventiontp.api.ClaimInfo;
import dev.badbird.griefpreventiontp.manager.MenuManager;
import dev.badbird.griefpreventiontp.manager.MessageManager;
import dev.badbird.griefpreventiontp.object.ComponentQuestionConversation;
import dev.badbird.griefpreventiontp.object.IconWrapper;
import dev.badbird.griefpreventiontp.util.AdventureUtil;
import me.ryanhamshire.GriefPrevention.Claim;
import net.badbird5907.blib.menu.buttons.Button;
import net.badbird5907.blib.menu.buttons.impl.CloseButton;
import net.badbird5907.blib.menu.buttons.impl.FilterButton;
import net.badbird5907.blib.menu.buttons.impl.NextPageButton;
import net.badbird5907.blib.menu.buttons.impl.PreviousPageButton;
import net.badbird5907.blib.menu.menu.PaginatedMenu;
import net.badbird5907.blib.util.CC;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class ClaimsMenu extends PaginatedMenu {
    private final UUID uuid;
    private String searchTerm;
    private boolean privateClaims = true;

    public ClaimsMenu(UUID uuid, String searchTerm) {
        this.uuid = uuid;
        this.searchTerm = searchTerm;
    }

    public ClaimsMenu(UUID uuid) {
        this.uuid = uuid;
        this.searchTerm = null;
    }


    @Override
    public String getPagesTitle(Player player) {
        return CC.translate(MenuManager.getString("claims", "title", "Claims"));
    }

    @Override
    public boolean showPageNumbersInTitle() {
        return false;
    }

    @Override
    public List<Button> getPaginatedButtons(Player player) {
        List<Button> buttons = new ArrayList<>();
        Collection<ClaimInfo> claims = privateClaims ? GriefPreventionTP.getInstance().getClaimManager().getClaims(uuid) : GriefPreventionTP.getInstance().getClaimManager().getAllPublicClaims();
        for (ClaimInfo claim : claims) {
            if (searchTerm != null && !claim.getName().toLowerCase().contains(searchTerm.toLowerCase()) && !claim.getOwnerName().toLowerCase().contains(searchTerm.toLowerCase()))
                continue;
            buttons.add(new ClaimButton(claim, player));
        }
        return buttons;
    }

    @Override
    public List<Button> getEveryMenuSlots(Player player) {
        List<Button> buttons = new ArrayList<>();
        if (GriefPreventionTP.getInstance().getConfig().getBoolean("menu.enable-search", true)) {
            buttons.add(new SearchButton());
        }
        return buttons;
    }

    @Override
    public Button getFilterButton() {
        if (!plugin.getConfig().getBoolean("enable-public")) {
            return null;
        }
        return new FilterButton() {
            @Override
            public void clicked(Player player, ClickType type, int slot, InventoryClickEvent event) {
                privateClaims = !privateClaims;
                update(player);
            }

            @Override
            public ItemStack getItem(Player player) {
                String base = "filter." + (privateClaims ? "disabled" : "enabled");
                // return new ItemBuilder(Material.PAPER).setName(CC.GREEN + "Viewing Public Claims: " + (!privateClaims ? "Yes" : CC.RED + "No"))
                //        .lore(CC.GRAY + "Click to toggle.").build();
                ItemStack itemStack = new ItemStack(Material.PAPER);
                Component name = AdventureUtil.getComponentFromConfig("claims", base + ".name", "<green>Viewing Public Claims: " + (!privateClaims ? "<green>Yes" : "<red>No"));
                List<Component> lore = AdventureUtil.getComponentListFromConfigDef("claims", base + ".lore", List.of("<gray>Click to toggle."));
                AdventureUtil.setItemDisplayName(itemStack, name);
                AdventureUtil.setItemLore(itemStack, lore);
                return itemStack;
            }

            @Override
            public int getSlot() {
                return 40;
            }
        };
    }

    @Override
    public Button getCloseButton() {
        return new CloseButton() {
            @Override
            public ItemStack getItem(Player player) {
                // return new ItemBuilder(Material.valueOf(plugin.getConfig().getString("menu.close-button-type"))).name(CC.RED + "Close").build();
                ItemStack itemStack = new ItemStack(Material.valueOf(plugin.getConfig().getString("menu.close-button.type")));
                Component name = AdventureUtil.getComponentFromConfig("", "menu.close-button.title", "<red>Close");
                AdventureUtil.setItemDisplayName(itemStack, name);
                List<Component> lore = AdventureUtil.getComponentListFromConfig("", "menu.close-button.lore");
                if (!lore.isEmpty())
                    AdventureUtil.setItemLore(itemStack, lore);
                return itemStack;
            }

            @Override
            public int getSlot() {
                return plugin.getConfig().getBoolean("enable-public") ? 36 : 40;
            }
        };
    }

    @Override
    public Button getNextPageButton() {
        return new NextPageButton(this) {
            @Override
            public int getSlot() {
                return 41;
            }

            @Override
            public ItemStack getItem(Player player) {
                Material material = Material.valueOf(plugin.getConfig().getString("menu.next-page.type"));
                ItemStack item = new ItemStack(material);
                Component name = AdventureUtil.getComponentFromConfig("claims", "menu.next-page.name", "<green>Next Page");
                List<Component> lore = AdventureUtil.getComponentListFromConfig("claims", "menu.next-page.lore", List.of(
                        "<gray>Click to go to the next page."
                ));
                AdventureUtil.setItemDisplayName(item, name);
                AdventureUtil.setItemLore(item, lore);
                return item;
            }
        };
    }

    @Override
    public Button getPreviousPageButton() {
        return new PreviousPageButton(this) {
            @Override
            public int getSlot() {
                return 39;
            }

            @Override
            public ItemStack getItem(Player player) {
                Material material = Material.valueOf(plugin.getConfig().getString("menu.previous-page.type"));
                ItemStack item = new ItemStack(material);
                Component name = AdventureUtil.getComponentFromConfig("claims", "menu.previous-page.name", "<green>Previous Page");
                List<Component> lore = AdventureUtil.getComponentListFromConfig("claims", "menu.previous-page.lore", List.of(
                        "<gray>Click to go to the previous page."
                ));
                AdventureUtil.setItemDisplayName(item, name);
                AdventureUtil.setItemLore(item, lore);
                return item;
            }
        };
    }

    private class ClaimButton extends Button {
        private final ClaimInfo claimInfo;
        private final Player player;
        private Claim claim;
        private boolean canEdit;

        public ClaimButton(ClaimInfo claimInfo, Player player) {
            this.claimInfo = claimInfo;
            this.player = player;
            this.claim = claimInfo.getClaim();
            this.canEdit = player.hasPermission("gptp.staff") ||
                    GriefPreventionTP.getInstance().getPermissionsManager()
                            .hasClaimPermission(player, claim);
            claimInfo.checkValid();
        }

        @Override
        public ItemStack getItem(Player player) {
            boolean valid = claimInfo.getSpawn() != null;
            /*
            ItemBuilder builder = new ItemBuilder(Material.PLAYER_HEAD)
                    .setName(CC.GREEN + claimInfo.getName())
                    .lore(CC.GRAY + "Owner: " + claimInfo.getOwnerName())
                    .amount(claimInfo.getPlayerClaimCount());
            builder.lore(CC.GRAY + "ID: " + claimInfo.getClaimID());
            if (showCoords)
                builder.lore(CC.D_GRAY + claimInfo.getSpawn().getX() + ", " + claimInfo.getSpawn().getY() + ", " + claimInfo.getSpawn().getZ());
            if (valid)
                builder.lore(
                        "", CC.GRAY + "Click to teleport."
                );
            else builder.lore("", CC.RED + "No spawn set!");

            if (canEdit) {
                builder.lore(CC.GRAY + "Right Click to manage.");
            }
            ItemStack stack = builder.build();
             */
            IconWrapper setIcon = claimInfo.getIcon();
            ItemStack stack = setIcon != null ? setIcon.getItemStack() : new ItemStack(Material.PLAYER_HEAD);
            Component name = AdventureUtil.getComponentFromConfig("claims", "claim.name", "<green>{name}", "name", claimInfo.getName());
            List<Component> lore1 =
                    new ArrayList<>(AdventureUtil.getComponentListFromConfigDef("claims", "claim.lore", new ArrayList<>(List.of(
                            "<gray>Owner: {owner}",
                            "<gray>ID: {id}",
                            "<gray>{x}, {y}, {z}",
                            "",
                            "<gray>Click to teleport.",
                            "canEdit:<gray>Right click to manage."
                    )), "owner", claimInfo.getOwnerName(), "id", claimInfo.getClaimID(), "x", claimInfo.getSpawn().getX(), "y", claimInfo.getSpawn().getY(), "z", claimInfo.getSpawn().getZ()));
            List<Component> lore = new ArrayList<>();
            boolean bedrock = Bukkit.getPluginManager().isPluginEnabled("floodgate") && FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId());
            for (int i = 0; i < lore1.size(); i++) {
                Component component = lore1.get(i);
                String content = PlainTextComponentSerializer.plainText().serialize(component); // TODO optimize
                if (content.startsWith("canEdit:bedrock:")) {
                    if (canEdit && bedrock) {
                        lore.add(component.replaceText(TextReplacementConfig.builder()
                                .match("canEdit:bedrock:")
                                .replacement("")
                                .build()));
                        continue;
                    }
                    // lore.remove(component);
                } else if (content.startsWith("canEdit:java:")) {
                    if (canEdit && !bedrock) {
                        lore.add(component.replaceText(TextReplacementConfig.builder()
                                .match("canEdit:java:")
                                .replacement("")
                                .build()));
                        continue;
                    }
                    // lore.remove(component);
                } else if (content.startsWith("canEdit:")) {
                    if (canEdit) {
                        lore.add(component.replaceText(TextReplacementConfig.builder()
                                .match("canEdit:")
                                .replacement("")
                                .build()));
                        continue;
                    }
                    // lore.remove(component);
                } else if (content.startsWith("bedrock:")) {
                    if (bedrock) {
                        lore.add(component.replaceText(TextReplacementConfig.builder()
                                .match("bedrock:")
                                .replacement("")
                                .build()));
                        continue;
                    }
                    // lore.remove(component);
                } else lore.add(component);
            }
            AdventureUtil.setItemDisplayName(stack, name);
            AdventureUtil.setItemLore(stack, lore);

            UUID owner = claimInfo.getOwner();
            if (stack.getType() == Material.PLAYER_HEAD && setIcon == null) {
                SkullMeta skullMeta = (SkullMeta) stack.getItemMeta();
                skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(owner));
                stack.setItemMeta(skullMeta);
            }
            return stack;
        }

        @Override
        public int getSlot() {
            return 0;
        }

        @Override
        public void onClick(Player player, int slot, ClickType clickType, InventoryClickEvent event) {
            if (clickType.isRightClick() && canEdit) {
                new ManageClaimMenu(claimInfo, ClaimsMenu.this).open(player);
                return;
            }
            if (claimInfo.getSpawn() == null) {
                MessageManager.sendMessage(player, "messages.no-spawn-set");
                return;
            }
            GriefPreventionTP.getInstance().getTeleportManager().teleport(player, claimInfo.getSpawn().getLocation());
        }
    }

    private class SearchButton extends Button {

        @Override
        public ItemStack getItem(Player player) {
            /*
            return new ItemBuilder(Material.OAK_SIGN)
                    .setName("&aSearch")
                    .lore(CC.GRAY + "Click to search claims!")
                    .build();
             */
            ItemStack item = new ItemStack(
                    Material.valueOf(MenuManager.getString("claims", "search.type", "OAK_SIGN"))
            );
            Component name = AdventureUtil.getComponentFromConfig("claims", "search.name", "<green>Search");
            List<Component> lore = AdventureUtil.getComponentListFromConfig("claims", "search.lore", List.of(
                    "<gray>Click to search claims!"
            ));
            AdventureUtil.setItemDisplayName(item, name);
            AdventureUtil.setItemLore(item, lore);
            return item;
        }

        @Override
        public int getSlot() {
            return 44;
        }

        @Override
        public void onClick(Player player, int slot, ClickType clickType, InventoryClickEvent event) {
            new ComponentQuestionConversation(MessageManager.getComponent("messages.search"), (a) -> {
                String answer = a.toLowerCase();
                List<String> cancelMessages = plugin.getConfig().getStringList("search.cancel-messages");
                /*
                if (answer.equals("cancel")) {
                    searchTerm = null;
                    open(player);
                    return Prompt.END_OF_CONVERSATION;
                }
                 */
                if (cancelMessages.stream().anyMatch(s -> s.equalsIgnoreCase(answer))) {
                    searchTerm = null;
                    open(player);
                    return Prompt.END_OF_CONVERSATION;
                }

                searchTerm = answer;
                open(player);
                return Prompt.END_OF_CONVERSATION;
            }).start(player);
        }
    }
}
