package dev.badbird.griefpreventiontp.menus;

import dev.badbird.griefpreventiontp.api.ClaimInfo;
import lombok.RequiredArgsConstructor;
import net.badbird5907.blib.menu.buttons.Button;
import net.badbird5907.blib.menu.menu.Menu;
import net.badbird5907.blib.util.CC;
import net.badbird5907.blib.util.ItemBuilder;
import net.badbird5907.blib.util.QuestionConversation;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@RequiredArgsConstructor
public class ManageClaimMenu extends Menu {
    private final ClaimInfo claimInfo;

    @Override
    public List<Button> getButtons(Player player) {
        return null;
    }

    @Override
    public String getName(Player player) {
        String title = "Manage Claim - " + claimInfo.getName();
        if (title.length() > 29)
            title = title.substring(0, 29) + "...";
        return title;
    }


    private final class DeleteButton extends Button {

        @Override
        public ItemStack getItem(Player player) {
            return null;
        }

        @Override
        public int getSlot() {
            return 0;
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
            return 0;
        }

        @Override
        public void onClick(Player player, int slot, ClickType clickType, InventoryClickEvent event) {
            new QuestionConversation()
        }
    }
}
