package com.semivanilla.griefpreventiontp.commands;

import com.semivanilla.griefpreventiontp.menus.ClaimsMenu;
import net.badbird5907.blib.command.BaseCommand;
import net.badbird5907.blib.command.Command;
import net.badbird5907.blib.command.CommandResult;
import net.badbird5907.blib.command.Sender;

public class ClaimsCommand extends BaseCommand {
    @Command(name = "claims", playerOnly = true)
    public CommandResult execute(Sender sender, String[] args) {
        new ClaimsMenu(sender.getPlayer().getUniqueId()).open(sender.getPlayer());
        return CommandResult.SUCCESS;
    }
}
