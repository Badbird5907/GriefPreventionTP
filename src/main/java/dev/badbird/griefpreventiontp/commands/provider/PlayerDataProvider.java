package dev.badbird.griefpreventiontp.commands.provider;

import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.PlayerData;
import net.octopvp.commander.annotation.Sender;
import net.octopvp.commander.bukkit.BukkitCommandSender;
import net.octopvp.commander.bukkit.providers.PlayerProvider;
import net.octopvp.commander.command.CommandContext;
import net.octopvp.commander.command.CommandInfo;
import net.octopvp.commander.command.ParameterInfo;
import net.octopvp.commander.exception.CommandException;
import net.octopvp.commander.provider.Provider;
import net.octopvp.commander.sender.CoreCommandSender;
import org.bukkit.entity.Player;

import java.util.Deque;
import java.util.List;

public class PlayerDataProvider implements Provider<PlayerData> {
    @Override
    public PlayerData provide(CommandContext context, CommandInfo commandInfo, ParameterInfo parameterInfo, Deque<String> args) {
        BukkitCommandSender sender = (BukkitCommandSender) context.getCommandSender();
        if (!(sender.getSender() instanceof Player player))
            throw new CommandException("This command is player only!");
        return GriefPrevention.instance.dataStore.getPlayerData(player.getUniqueId());
    }

    @Override
    public List<String> provideSuggestions(String input, String lastArg, CoreCommandSender sender) {
        return PlayerProvider.suggest(sender);
    }
}
