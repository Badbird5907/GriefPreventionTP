package dev.badbird.griefpreventiontp.commands.impl;

import dev.badbird.griefpreventiontp.manager.PermissionsManager;
import net.octopvp.commander.annotation.Command;
import net.octopvp.commander.annotation.Cooldown;
import net.octopvp.commander.annotation.Dependency;
import net.octopvp.commander.annotation.Sender;
import net.octopvp.commander.bukkit.annotation.PlayerOnly;
import org.bukkit.entity.Player;

public class SetSpawnCommand {
    @Command(name = "setspawn", description = "Set the spawn location of the claim you're standing in")
    @PlayerOnly
    @Cooldown(3)
    public void execute(@Sender Player player, @Dependency PermissionsManager permissionsManager) {
        new GPTPCommand().setSpawn(player, permissionsManager);
    }
}
