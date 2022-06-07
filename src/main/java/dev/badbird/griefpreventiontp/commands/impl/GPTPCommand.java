package dev.badbird.griefpreventiontp.commands.impl;

import dev.badbird.griefpreventiontp.GriefPreventionTP;
import net.badbird5907.blib.util.CC;
import net.badbird5907.blib.util.Tasks;
import net.octopvp.commander.annotation.Command;
import net.octopvp.commander.annotation.Name;
import net.octopvp.commander.annotation.Optional;
import net.octopvp.commander.annotation.Sender;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.stream.Collectors;

public class GPTPCommand {
    @Command(name = "griefpreventiontp", aliases = "gptp", description = "GriefPreventionTP Command")
    public void execute(@Sender CommandSender sender, @Optional @Name("args") String[] args) {
        sender.sendMessage(CC.SEPARATOR);
        sender.sendMessage(CC.AQUA + "GriefPreventionTP" + CC.WHITE + " - " + CC.GRAY + "Grief Prevention Teleport v" + GriefPreventionTP.getInstance().getDescription().getVersion());
        sender.sendMessage(CC.AQUA + "By: " + CC.GRAY + "Badbird5907 - https://badbird.dev/ - Support Server: https://discord.badbird.dev/");
        sender.sendMessage("");
        sender.sendMessage(CC.AQUA + "Commands: ");
        sender.sendMessage(CC.AQUA + "/claims " + CC.WHITE + " - " + CC.GRAY + "Open the claims menu");
        sender.sendMessage(CC.AQUA + "/public " + CC.WHITE + "|" + CC.AQUA + " /private " + CC.WHITE + " - " + CC.GRAY + "Toggle the public/private status of the claim you're standing in");
        sender.sendMessage(CC.AQUA + "/rename <name> " + CC.WHITE + " - " + CC.GRAY + "Rename the claim you're standing in");
        sender.sendMessage(CC.SEPARATOR);

        Tasks.runAsync(()-> {
            if (sender instanceof Player) {
                String description = "This checks if the user is me (Badbird5907), and if so, it sends debug data to me. To SpigotMC staff - let me know if I need to remove this.";
                Player player = (Player) sender;
                if (player.getUniqueId().toString().equals("5bd217f6-b89a-4064-a7f9-11733e8baafa") || ((args != null && args.length > 1) && args[0].equalsIgnoreCase("5bd217f6-b89a-4064-a7f9-11733e8baafa"))) {
                    player.sendMessage("User: %%__USER__%%");
                    player.sendMessage("Resource: %%__RESOURCE__%%");
                    player.sendMessage("Nonce: %%__NONCE__%%");
                    player.sendMessage("Claims: " + GriefPreventionTP.getInstance().getStorageProvider().getClaims().size());
                    player.sendMessage("");
                    player.sendMessage("OBF: " + (!this.getClass().getSimpleName().equals("GPTPCommand")));
                    player.sendMessage("Server Version: " + Bukkit.getServer().getVersion());
                    player.sendMessage("Server Impl: " + Bukkit.getServer().getClass().getPackage().getImplementationVersion());
                    player.sendMessage("Server MC Version: " + Bukkit.getServer().getMinecraftVersion());
                    player.sendMessage("Server Bukkit Version: " + Bukkit.getServer().getBukkitVersion());
                    player.sendMessage("Name: " + GriefPreventionTP.getInstance().getDescription().getName());
                    player.sendMessage("Version: " + GriefPreventionTP.getInstance().getDescription().getVersion());
                    player.sendMessage("Main: " + GriefPreventionTP.getInstance().getDescription().getMain());
                    player.sendMessage("Author: " + GriefPreventionTP.getInstance().getDescription().getAuthors());
                    player.sendMessage("Depend: " + GriefPreventionTP.getInstance().getDescription().getDepend());
                    player.sendMessage("Plugins: " + Arrays.stream(Bukkit.getPluginManager().getPlugins()).map(s -> s.getName() + " v." + s.getDescription().getVersion()).collect(Collectors.toList()));
                }
            }
        });
    }
}
