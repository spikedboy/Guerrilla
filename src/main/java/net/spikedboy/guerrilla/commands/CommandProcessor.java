package net.spikedboy.guerrilla.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public interface CommandProcessor {
    Boolean processCommand(CommandSender sender, Command cmd, String commandLabel, String[] args);
}
