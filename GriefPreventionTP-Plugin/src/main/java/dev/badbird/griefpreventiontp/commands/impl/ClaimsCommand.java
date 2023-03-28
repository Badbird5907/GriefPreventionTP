package dev.badbird.griefpreventiontp.commands.impl;

import dev.badbird.griefpreventiontp.manager.PermissionsManager;
import dev.badbird.griefpreventiontp.menus.ClaimsMenu;
import net.badbird5907.blib.util.CC;
import net.octopvp.commander.annotation.*;
import net.octopvp.commander.bukkit.annotation.PlayerOnly;
import net.octopvp.commander.exception.NoPermissionException;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class ClaimsCommand {
    @Command(name = "claims", description = "Open the claims GUI")
    @PlayerOnly
    public void execute(@Sender Player sender, @Optional String target, @Dependency PermissionsManager permissionsManager) {
        if (permissionsManager.isClaimsPerm() && !sender.hasPermission("gptp.command.claims")) throw new NoPermissionException();
        if (target != null && !target.isEmpty()) {
            if (!sender.hasPermission("gptp.command.claims.others")) {
                throw new NoPermissionException();
            }
            OfflinePlayer op = Bukkit.getOfflinePlayer(target);
            if (!op.hasPlayedBefore()) {
                sender.sendMessage(CC.RED + "That player has never played before!");
                return;
            }
            new ClaimsMenu(op.getUniqueId()).open(sender);
            return;
        }
        new ClaimsMenu(sender.getUniqueId()).open(sender);
    }
    @Completer(name = "claims", index = 1)
    public String[] complete() {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).toArray(String[]::new);
    }
}

