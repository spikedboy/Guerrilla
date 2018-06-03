package net.spikedboy.guerrilla.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public interface CommandExecutor {
    boolean processCommands(CommandSender sender, Command cmd, String commandLabel, String[] args);
}
