package net.spikedboy.guerrilla.guerrilla;

import com.google.inject.Inject;
import net.spikedboy.guerrilla.GuerrillaPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.util.logging.Logger;

public class Messager {

    private static final Logger LOGGER = Logger.getLogger("Minecraft");

    public static final String GUERRILLA_MESSAGE_PREFIX = ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY;

    @Inject
    private GuerrillaPlugin plugin;

    @Inject
    private Server server;

    public Messager() {
    }

    public void sendMessage(String msg) {
        sendGuerrillaMessageToAllPlayers(msg);
    }

    private void sendGuerrillaMessageToAllPlayers(String msg) {
        LOGGER.info("[Guerrilla] " + msg);
        for (Player player : server.getOnlinePlayers()) {
            player.sendMessage(GUERRILLA_MESSAGE_PREFIX + msg);
        }
    }

    public void setPlugin(GuerrillaPlugin plugin) {
        this.plugin = plugin;
    }

    public void setServer(Server server) {
        this.server = server;
    }
}
