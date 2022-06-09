package dev.badbird.griefpreventiontp.menus;

import dev.badbird.griefpreventiontp.GriefPreventionTP;
import dev.badbird.griefpreventiontp.api.ClaimInfo;
import dev.badbird.griefpreventiontp.manager.MessageManager;
import dev.badbird.griefpreventiontp.object.ComponentQuestionConversation;
import lombok.RequiredArgsConstructor;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import net.badbird5907.blib.menu.buttons.Button;
import net.badbird5907.blib.menu.buttons.PlaceholderButton;
import net.badbird5907.blib.menu.buttons.impl.CloseButton;
import net.badbird5907.blib.menu.menu.Menu;
import net.badbird5907.blib.util.CC;
import net.badbird5907.blib.util.ItemBuilder;
import net.badbird5907.blib.util.QuestionConversation;
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
        ArrayList<Button> buttons = new ArrayList<>();
        buttons.add(new DeleteButton());
        buttons.add(new RenameButton());
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
            return 36;
        }

        @Override
        public void onClick(Player player, int slot, ClickType clickType, InventoryClickEvent event) {
            new ConfirmMenu("delete this claim?", (res)-> {
                if (res) {
                    GriefPrevention.instance.dataStore.deleteClaim(claimInfo.getClaim());
                    MessageManager.sendMessage(player, "messages.staff.claim-deleted");
                    player.closeInventory();
                    new ClaimsMenu(player.getUniqueId()).open(player);
                }
            });
        }
    }

    private final class RenameButton extends Button {

        @Override
        public ItemStack getItem(Player player) {
            return new ItemBuilder(Material.ANVIL)
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
            new ComponentQuestionConversation(MessageManager.getComponent("messages.staff.rename-claim"), (response)-> {
                claimInfo.setName(response);
                MessageManager.sendMessage(player, "messages.staff.claim-renamed", claimInfo.getName());
                return Prompt.END_OF_CONVERSATION;
            });
        }
    }
}
