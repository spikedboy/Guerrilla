package net.spikedboy.guerrilla.guerrilla;

import com.google.inject.Inject;
import net.spikedboy.guerrilla.GuerrillaPlugin;
import org.bukkit.ChatColor;

import java.io.IOException;

public class ChargePaymentsRunnable implements Runnable {
    @Inject
    private GuerrillaPlugin guerrillaPlugin;

    @Inject
    private GuerrillaManager guerrillaManager;

    public void run() {
        if (guerrillaManager.checkWinners() != null) {
            GuerrillaManager.setStateWon(true);
            GuerrillaManager.setWinnerGuerrillaName(guerrillaManager.checkWinners().getName());
            guerrillaPlugin.getConfig().set("guerrilla.ismatchwon", true);
            guerrillaPlugin.getConfig().set("guerrilla.winner", GuerrillaManager.getWinnerGuerrillaName());
            guerrillaPlugin.saveConfig();
            guerrillaPlugin.getServer()
                    .broadcastMessage(ChatColor.GOLD + "[GuerrillaWrittenInGold] "
                            + GuerrillaManager.getWinnerGuerrillaName() + " HAS WON THIS MATCH!!!!!! GOOD DAY TO YOU");
            return;
        }
        guerrillaManager.chargeGuerrillasMaintenance();
        try {
            guerrillaManager.printmap();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setGuerrillaPlugin(GuerrillaPlugin guerrillaPlugin) {
        this.guerrillaPlugin = guerrillaPlugin;
    }

    public void setGuerrillaManager(GuerrillaManager guerrillaManager) {
        this.guerrillaManager = guerrillaManager;
    }
}
