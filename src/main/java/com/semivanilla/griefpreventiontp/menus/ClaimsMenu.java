package com.semivanilla.griefpreventiontp.menus;

import com.semivanilla.griefpreventiontp.GriefPreventionTP;
import com.semivanilla.griefpreventiontp.object.ClaimInfo;
import lombok.RequiredArgsConstructor;
import net.badbird5907.blib.menu.buttons.Button;
import net.badbird5907.blib.menu.buttons.impl.CloseButton;
import net.badbird5907.blib.menu.buttons.impl.FilterButton;
import net.badbird5907.blib.menu.buttons.impl.NextPageButton;
import net.badbird5907.blib.menu.buttons.impl.PreviousPageButton;
import net.badbird5907.blib.menu.menu.PaginatedMenu;
import net.badbird5907.blib.util.CC;
import net.badbird5907.blib.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class ClaimsMenu extends PaginatedMenu {
    private final UUID uuid;
    private boolean privateClaims = true;
    @Override
    public String getPagesTitle(Player player) {
        return "Claims";
    }

    @Override
    public List<Button> getPaginatedButtons(Player player) {
        List<Button> buttons = new ArrayList<>();
        Collection<ClaimInfo> claims = privateClaims ? GriefPreventionTP.getInstance().getClaimManager().getClaims(uuid) : GriefPreventionTP.getInstance().getClaimManager().getAllClaims();
        for (ClaimInfo claim : claims) {
            buttons.add(new ClaimButton(claim));
        }
        return buttons;
    }

    @Override
    public List<Button> getEveryMenuSlots(Player player) {
        return null;
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
                return new ItemBuilder(Material.PAPER).setName(CC.GREEN + "Viewing Private Claims: " + (privateClaims ? "Yes" : CC.RED + "No")).build();
            }

            @Override
            public int getSlot() {
                return 40;
            }
        };
    }

    @Override
    public Button getCloseButton() {
        return new CloseButton(){
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
    private static class ClaimButton extends Button{
        private final ClaimInfo claimInfo;

        @Override
        public ItemStack getItem(Player player) {
            return new ItemBuilder(Material.PLAYER_HEAD).setName(CC.GREEN + claimInfo.getName())
                    .lore(CC.GRAY + "Click to teleport to this claim!").toSkullBuilder().withOwner(claimInfo.getOwner()).buildSkull();
        }

        @Override
        public int getSlot() {
            return 0;
        }

        @Override
        public void onClick(Player player, int slot, ClickType clickType, InventoryClickEvent event) {
            //5 second countdown to teleport the player
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
        return new PreviousPageButton(this){
            @Override
            public int getSlot() {
                return 39;
            }
        };
    }
}
