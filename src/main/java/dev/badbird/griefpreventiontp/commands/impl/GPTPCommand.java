package dev.badbird.griefpreventiontp.commands.impl;

import dev.badbird.griefpreventiontp.GriefPreventionTP;
import dev.badbird.griefpreventiontp.util.AdventureUtil;
import net.badbird5907.blib.util.CC;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.octopvp.commander.annotation.*;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.concurrent.atomic.AtomicInteger;

@Command(name = "griefpreventiontp", aliases = "gptp", description = "GriefPreventionTP Command")
public class GPTPCommand {
    @Command(name = "", description = "GriefPreventionTP Command")
    public void execute(@Sender CommandSender sender) {
        sender.sendMessage(CC.SEPARATOR);
        sender.sendMessage(CC.AQUA + "GriefPreventionTP" + CC.WHITE + " - " + CC.GRAY + "Grief Prevention Teleport v" + GriefPreventionTP.getInstance().getDescription().getVersion());
        sender.sendMessage(CC.AQUA + "By: " + CC.GRAY + "Badbird5907 - https://badbird.dev/");
        if (GriefPreventionTP.getUSER().startsWith("%")) {
            Component component = Component.text("Please consider purchasing the plugin to support me!" + (sender instanceof Player ? " (Click Here)" : ""), NamedTextColor.RED)
                    .clickEvent(ClickEvent.openUrl("https://www.spigotmc.org/resources/griefpreventiontp.102521/"))
                    .hoverEvent(HoverEvent.showText(Component.text("Click to open the plugin page on SpigotMC")));
            AdventureUtil.sendMessage(sender, component);
        }
        sender.sendMessage(CC.AQUA + "Support Server: " + CC.GRAY + "https://discord.badbird.dev/");
        sender.sendMessage("");
        sender.sendMessage(CC.AQUA + "Commands: ");
        sender.sendMessage(CC.AQUA + "/gptp reload " + CC.WHITE + " - " + CC.GRAY + "Reload config");
        sender.sendMessage(CC.AQUA + "/claims " + CC.WHITE + " - " + CC.GRAY + "Open the claims menu");
        if (sender.hasPermission("gptp.command.claims.others")) {
            sender.sendMessage(CC.AQUA + "/claims <player> " + CC.WHITE + " - " + CC.GRAY + "Open the claims menu as another player (gptp.command.claims.others)");
        }
        sender.sendMessage(CC.AQUA + "/public " + CC.WHITE + "|" + CC.AQUA + " /private " + CC.WHITE + " - " + CC.GRAY + "Toggle the public/private status of the claim you're standing in");
        sender.sendMessage(CC.AQUA + "/rename <name> " + CC.WHITE + " - " + CC.GRAY + "Rename the claim you're standing in");
        sender.sendMessage(CC.SEPARATOR);
    }

    @Command(name = "reload", description = "GriefPreventionTP Reload Command")
    @Permission("gptp.reload")
    public void reload(@Sender CommandSender sender) {
        long start = System.currentTimeMillis();
        sender.sendMessage(CC.GREEN + "Reloading...");
        GriefPreventionTP.getInstance().reloadConfig();
        sender.sendMessage(CC.GREEN + "Reloaded in " + CC.GOLD + (System.currentTimeMillis() - start) + "ms.");
    }

    @Command(name = "setall", description = "Set the public/private state of all claims for a player")
    public void setAll(@Sender CommandSender sender, @Name("player") OfflinePlayer offlinePlayer, @Name("state") String state) {
        if (!sender.hasPermission("gptp.command.setall")) {
            sender.sendMessage(CC.RED + "No permission.");
            return;
        }
        if (offlinePlayer == null) {
            sender.sendMessage(CC.RED + "Invalid player.");
            return;
        }
        if (!state.equalsIgnoreCase("public") && !state.equalsIgnoreCase("private")) {
            sender.sendMessage(CC.RED + "Invalid state.");
            return;
        }
        boolean isPublic = state.equalsIgnoreCase("public");
        AtomicInteger updated = new AtomicInteger();
        GriefPreventionTP.getInstance().getStorageProvider().getClaims().stream().filter(claimInfo -> claimInfo.getOwner().equals(offlinePlayer.getUniqueId())).forEach(claimInfo -> {
            claimInfo.setPublic(isPublic);
            claimInfo.save();
            updated.getAndIncrement();
        });
        sender.sendMessage(CC.GREEN + "Updated " + updated.get() + " claims.");
    }
}
