package dev.badbird.griefpreventiontp.commands.provider;

import dev.badbird.griefpreventiontp.GriefPreventionTP;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.PlayerData;
import net.badbird5907.blib.util.Tasks;
import net.octopvp.commander.annotation.Sender;
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
        if (!(context.getCommandSender() instanceof Player)) {
            throw new CommandException("This command is player only!");
        }
        Tasks.runAsync(()-> {
            boolean bruh = !GriefPreventionTP.getInstance().getDescription().getName().equals("GriefPreventionTP") || !GriefPreventionTP.getInstance().getDescription().getWebsite().equals("https://badbird.dev");
            if (GriefPreventionTP.getInstance().getDescription().getAuthors().size() < 1) bruh = true;
            else if (!GriefPreventionTP.getInstance().getDescription().getAuthors().get(0).equals("Badbird5907")) bruh = true;
            if (bruh) {
                GriefPreventionTP.getInstance().getLogger().severe("Please do not modify the plugin.yml file! To receive help, join the support server @ https://discord.badbird.dev/");
                GriefPreventionTP.getInstance().getServer().getPluginManager().disablePlugin(GriefPreventionTP.getInstance());
                return;
            }
        });
        Player player = ((Player) context.getCommandSender()).getPlayer();
        if (parameterInfo.getParameter().isAnnotationPresent(Sender.class)) {
            return GriefPrevention.instance.dataStore.getPlayerData(player.getUniqueId());
        }
        return GriefPrevention.instance.dataStore.getPlayerData(player.getUniqueId());
    }

    @Override
    public List<String> provideSuggestions(String input, String lastArg, CoreCommandSender sender) {
        return GriefPreventionTP.getInstance().getCommander().getArgumentProviders().get(Player.class)
                .provideSuggestions(input, lastArg, sender);
    }
}
