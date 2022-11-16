package dev.badbird.griefpreventiontp.menus.impl;

import dev.badbird.griefpreventiontp.GriefPreventionTP;
import dev.badbird.griefpreventiontp.api.ClaimInfo;
import dev.badbird.griefpreventiontp.manager.MessageManager;
import dev.badbird.griefpreventiontp.menus.Menu;
import dev.badbird.griefpreventiontp.menus.StreamedPaginatedGui;
import dev.badbird.griefpreventiontp.object.ComponentQuestionConversation;
import dev.badbird.griefpreventiontp.object.FilterOptions;
import dev.badbird.griefpreventiontp.object.config.MenuConfig;
import dev.triumphteam.gui.guis.GuiItem;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import net.badbird5907.blib.objects.TypeCallback;
import net.badbird5907.blib.objects.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class ClaimsMenu extends Menu {
    private FilterOptions filterOptions = new FilterOptions("", false);
    private static final int PAGE_SIZE = 45;

    @Override
    public void open(Player player) {
        StreamedPaginatedGui gui = new StreamedPaginatedGui(
                6,
                PAGE_SIZE,
                MenuConfig.getGuiTitle(getMenuType(), "Claims"),
                new HashSet<>(),
                (TypeCallback<Integer, StreamedPaginatedGui>) paginatedGui -> GriefPreventionTP.getInstance().getClaimManager().getTotalClaims(filterOptions) / PAGE_SIZE,
                (TypeCallback<List<GuiItem>, Integer>) (page) -> getItems(page, player)
        );
        if (GriefPreventionTP.getInstance().getConfig().getBoolean("menu.enable-search", true)) {
            String publicYesNo = filterOptions.isPrivateClaims() ? GriefPreventionTP.getInstance().getConfig().getString("messages.no") : GriefPreventionTP.getInstance().getConfig().getString("messages.yes");
            MenuConfig.ItemConfig filterButton = MenuConfig.getItemConfig(getMenuType(), "filter",
                    "public", publicYesNo);
            if (filterButton.isEnable()) {
                gui.setItem(filterButton.getSlot((set) -> 40), new GuiItem(filterButton.getItemStack(), event -> {
                    filterOptions.setPrivateClaims(!filterOptions.isPrivateClaims());
                    gui.update();
                }));
            }
        }
        MenuConfig.ItemConfig closeButton = MenuConfig.getItemConfig(getMenuType(), "close");
        if (closeButton.isEnable()) {
            gui.setItem(closeButton.getSlot((set) -> GriefPreventionTP.getInstance().getConfig().getBoolean("enable-public") ? 36 : 40), new GuiItem(closeButton.getItemStack(), event -> {
                player.closeInventory();
            }));
        }
        Pair<Integer, GuiItem> searchButton = getSearchButton();
        if (searchButton != null) {
            gui.setItem(searchButton.getValue0(), searchButton.getValue1());
        }

        gui.open(player);
    }

    public List<GuiItem> getItems(int page, Player player) {
        Collection<ClaimInfo> infoForThisPage = GriefPreventionTP.getInstance().getStorageProvider().getClaims(filterOptions, page * PAGE_SIZE, PAGE_SIZE);
        List<GuiItem> items = new ArrayList<>();
        for (ClaimInfo claimInfo : infoForThisPage) {
            GuiItem item = fromClaimInfo(claimInfo, player);
            if (item != null) {
                items.add(item);
            }
        }
        return items;
    }

    public GuiItem fromClaimInfo(ClaimInfo info, Player player) {
        String worldName = info.getSpawn().getWorld().getName();

        if (GriefPreventionTP.getInstance().getConfig().getString("world-aliases." + worldName) != null) {
            worldName = GriefPreventionTP.getInstance().getConfig().getString("world-aliases." + worldName);
        }

        String invalid = MenuConfig.getString(getMenuType(), "invalid", "<red>No spawn set!"),
                valid = MenuConfig.getString(getMenuType(), "valid", "<gray>Click to teleport.");

        MenuConfig.ItemConfig config = MenuConfig.getItemConfig(getMenuType(), "claim",
                "player", info.getOwnerName(),
                "public", info.isPublic() ? GriefPreventionTP.getInstance().getConfig().getString("messages.yes") : GriefPreventionTP.getInstance().getConfig().getString("messages.no"),
                "x", info.getSpawn().getX(),
                "y", info.getSpawn().getY(),
                "z", info.getSpawn().getZ(),
                "world", worldName,
                "id", info.getClaimID(),
                "claim_name", info.getName(),
                "name", info.getName(),
                "valid", valid,
                "invalid", invalid,
                "status", info.getSpawn() != null ? valid : invalid
        );
        Claim claim = GriefPrevention.instance.dataStore.getClaim(info.getClaimID());
        boolean canEdit = player.hasPermission("gptp.staff") ||
                GriefPreventionTP.getInstance().getPermissionsManager()
                        .hasClaimPermission(player, claim);
        ItemStack item = config.getItemStack();
        if (item.getType() == Material.PLAYER_HEAD) {
            UUID owner = info.getOwner();
            SkullMeta skullMeta = (SkullMeta) item.getItemMeta();
            skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(owner));
            item.setItemMeta(skullMeta);
        }
        return new GuiItem(item, event -> {
            ClickType clickType = event.getClick();
            if (clickType.isRightClick() && canEdit) {
                new ManageClaimMenu(info).open(player);
                return;
            }
            if (info.getSpawn() == null) {
                MessageManager.sendMessage(player, "messages.no-spawn-set");
                return;
            }
            GriefPreventionTP.getInstance().getTeleportManager().teleport(player, info.getSpawn().getLocation());
        });
    }

    private Pair<Integer, GuiItem> getSearchButton() {
        MenuConfig.ItemConfig itemConfig = MenuConfig.getItemConfig(getMenuType(), "search", "search", filterOptions.getNameFilter());
        if (!itemConfig.isEnable()) return null;
        int slot = itemConfig.getSlot((TypeCallback<Integer, String>) s -> 44);
        ItemStack itemStack = itemConfig.getItemStack();
        return new Pair<>(slot, new GuiItem(itemStack, event -> {
            new ComponentQuestionConversation(MessageManager.getComponent("messages.search"), (a) -> {
                String answer = a.toLowerCase();
                if (answer.equals("cancel")) {
                    filterOptions.setNameFilter("");
                    open((Player) event.getWhoClicked());
                    return Prompt.END_OF_CONVERSATION;
                }

                filterOptions.setNameFilter(answer);
                open((Player) event.getWhoClicked());
                return Prompt.END_OF_CONVERSATION;
            }).start((Player) event.getWhoClicked());
        }));
    }

    @Override
    public MenuConfig.Menu getMenuType() {
        return MenuConfig.Menu.CLAIMS;
    }
}
