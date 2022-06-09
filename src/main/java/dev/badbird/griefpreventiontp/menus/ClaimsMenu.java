package dev.badbird.griefpreventiontp.menus;

import dev.badbird.griefpreventiontp.GriefPreventionTP;
import dev.badbird.griefpreventiontp.manager.MessageManager;
import dev.badbird.griefpreventiontp.api.ClaimInfo;
import lombok.RequiredArgsConstructor;
import net.badbird5907.blib.menu.buttons.Button;
import net.badbird5907.blib.menu.buttons.impl.CloseButton;
import net.badbird5907.blib.menu.buttons.impl.FilterButton;
import net.badbird5907.blib.menu.buttons.impl.NextPageButton;
import net.badbird5907.blib.menu.buttons.impl.PreviousPageButton;
import net.badbird5907.blib.menu.menu.PaginatedMenu;
import net.badbird5907.blib.util.CC;
import net.badbird5907.blib.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

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
        return "Claims";
    }

    @Override
    public List<Button> getPaginatedButtons(Player player) {
        List<Button> buttons = new ArrayList<>();
        Collection<ClaimInfo> claims = privateClaims ? GriefPreventionTP.getInstance().getClaimManager().getClaims(uuid) : GriefPreventionTP.getInstance().getClaimManager().getAllPublicClaims();
        for (ClaimInfo claim : claims) {
            if (searchTerm != null && !claim.getName().toLowerCase().contains(searchTerm.toLowerCase()) && !claim.getOwnerName().toLowerCase().contains(searchTerm.toLowerCase())) continue;
            buttons.add(new ClaimButton(claim));
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
        return new FilterButton() {
            @Override
            public void clicked(Player player, ClickType type, int slot, InventoryClickEvent event) {
                privateClaims = !privateClaims;
                update(player);
            }

            @Override
            public ItemStack getItem(Player player) {
                return new ItemBuilder(Material.PAPER).setName(CC.GREEN + "Viewing Public Claims: " + (!privateClaims ? "Yes" : CC.RED + "No"))
                        .lore(CC.GRAY + "Click to toggle.").build();
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
                return new ItemBuilder(Material.RED_STAINED_GLASS_PANE).name(CC.RED + "Close").build();
            }

            @Override
            public int getSlot() {
                return 36;
            }
        };
    }

    @RequiredArgsConstructor
    private static class ClaimButton extends Button {
        private final ClaimInfo claimInfo;

        @Override
        public ItemStack getItem(Player player) {
            ItemStack stack = new ItemBuilder(Material.PLAYER_HEAD).setName(CC.GREEN + claimInfo.getName())
                    .lore(CC.GRAY + "Owner: " + claimInfo.getOwnerName(), "", CC.D_GRAY + "Click to teleport.")
                    .amount(claimInfo.getPlayerClaimCount())
                    .build();
            UUID owner = claimInfo.getOwner();
            SkullMeta skullMeta = (SkullMeta) stack.getItemMeta();
            skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(owner));
            stack.setItemMeta(skullMeta);
            return stack;
        }

        @Override
        public int getSlot() {
            return 0;
        }

        @Override
        public void onClick(Player player, int slot, ClickType clickType, InventoryClickEvent event) {
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
            return new ItemBuilder(Material.OAK_SIGN)
                    .setName("&aSearch")
                    .lore(CC.GRAY + "Click to search claims!")
                    .build();
        }

        @Override
        public int getSlot() {
            return 44;
        }

        @Override
        public void onClick(Player player, int slot, ClickType clickType, InventoryClickEvent event) {
            new QuestionConversation(MessageManager.getComponent("messages.search"), (a)-> {
                String answer = a.toLowerCase();
                if (answer.equals("cancel")) {
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

    @Override
    public Button getNextPageButton() {
        return new NextPageButton(this) {
            @Override
            public int getSlot() {
                return 41;
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
        };
    }
}
