package net.spikedboy.guerrilla.commands;

import com.google.inject.Inject;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Iterator;
import java.util.List;

public class MultiCommandProcessorCommandExecutor implements CommandExecutor {

    private CommandListFactory commandListFactory;

    @Override
    public boolean processCommands(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        List<CommandProcessor> commandProcessors = commandListFactory.build();

        boolean processCommands = true;

        Boolean commandResult = false;

        Iterator<CommandProcessor> iterator = commandProcessors.iterator();

        while (processCommands && iterator.hasNext()) {
            CommandProcessor next = iterator.next();

            commandResult = next.processCommand(sender, cmd, commandLabel, args);

            if (commandResult) {
                processCommands = false;
            }
        }

        return commandResult;
    }

    @Inject
    public void setCommandListFactory(CommandListFactory commandListFactory) {
        this.commandListFactory = commandListFactory;
    }
}
