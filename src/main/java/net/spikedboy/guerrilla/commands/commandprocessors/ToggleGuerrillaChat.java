package net.spikedboy.guerrilla.commands.commandprocessors;

import com.google.inject.Inject;
import net.spikedboy.guerrilla.GuerrillaPlugin;
import net.spikedboy.guerrilla.commands.CommandProcessor;
import net.spikedboy.guerrilla.guerrilla.GuerrillaManager;
import net.spikedboy.guerrilla.guerrilla.Messager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class ToggleGuerrillaChat implements CommandProcessor {

    @Inject
    private GuerrillaPlugin guerrillaPlugin;

    @Inject
    private GuerrillaManager guerrillaManager;

    @Override
    public Boolean processCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        //definitions
        Player playurd = (Player) sender;

        if (commandLabel.equalsIgnoreCase("gc")) {
            if (guerrillaPlugin != null) {
                if (args.length < 1) {
                    Map<String, Boolean> togglePlayerChat = guerrillaManager.getTogglePlayerChat();
                    if (togglePlayerChat.get(playurd.getName()) == null || togglePlayerChat.get(playurd.getName()) == false) {
                        togglePlayerChat.put(playurd.getName(), true);
                        playurd.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "GuerrillaPlugin chat enabled");
                    } else if (togglePlayerChat.get(playurd.getName()) == true) {
                        togglePlayerChat.put(playurd.getName(), false);
                        playurd.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "GuerrillaPlugin chat disabled");
                    }
                    return true;
                } else {
                    return false;
                }
            } else {
                playurd.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "You have no guerrillaPlugin");
                return true;
            }
        }

        return null;
    }

    public void setGuerrillaPlugin(GuerrillaPlugin guerrillaPlugin) {
        this.guerrillaPlugin = guerrillaPlugin;
    }

    public void setGuerrillaManager(GuerrillaManager guerrillaManager) {
        this.guerrillaManager = guerrillaManager;
    }
}
