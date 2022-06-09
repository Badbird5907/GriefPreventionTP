package dev.badbird.griefpreventiontp.commands.impl;

import dev.badbird.griefpreventiontp.manager.PermissionsManager;
import dev.badbird.griefpreventiontp.menus.ClaimsMenu;
import net.octopvp.commander.annotation.Command;
import net.octopvp.commander.annotation.Dependency;
import net.octopvp.commander.annotation.Sender;
import net.octopvp.commander.bukkit.annotation.PlayerOnly;
import net.octopvp.commander.exception.NoPermissionException;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ItemDespawnEvent;

public class ClaimsCommand {
    @Command(name = "claims")
    @PlayerOnly
    public void execute(@Sender Player sender, @Dependency PermissionsManager permissionsManager) {
        if (permissionsManager.isClaimsPerm() && !sender.hasPermission("gptp.command.claims")) throw new NoPermissionException();
        new ClaimsMenu(sender.getUniqueId()).open(sender);
    }
}
