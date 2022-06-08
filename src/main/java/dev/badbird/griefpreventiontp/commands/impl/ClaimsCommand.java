package dev.badbird.griefpreventiontp.commands.impl;

import dev.badbird.griefpreventiontp.menus.ClaimsMenu;
import net.octopvp.commander.annotation.Command;
import net.octopvp.commander.annotation.Sender;
import net.octopvp.commander.bukkit.annotation.PlayerOnly;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ItemDespawnEvent;

public class ClaimsCommand {
    @Command(name = "claims")
    @PlayerOnly
    public void execute(@Sender Player sender) {
        new ClaimsMenu(sender.getUniqueId()).open(sender);

    }
}

