package net.spikedboy.guerrilla.listeners;

import com.google.inject.Inject;
import net.spikedboy.guerrilla.GuerrillaPlugin;
import net.spikedboy.guerrilla.configuration.GuerrillaConfigurations;
import net.spikedboy.guerrilla.guerrilla.Guerrilla;
import net.spikedboy.guerrilla.guerrilla.GuerrillaManager;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldSaveEvent;

import java.util.ArrayList;
import java.util.logging.Logger;

public class GuerrillaWorldListener implements Listener {

    private static final Logger LOGGER = Logger.getLogger("Minecraft");

    private final GuerrillaPlugin guerrillaPlugin;

    @Inject
    private GuerrillaManager guerrillaManager;

    public GuerrillaWorldListener(GuerrillaPlugin inPlug) {
        guerrillaPlugin = inPlug;
    }

    @EventHandler
    public void onWorldSave(WorldSaveEvent event) {
        try {
            //GuerrillaPlugin.LOGGER.info("[Guerrilla] Saving..");
            for (Guerrilla guerrilla : guerrillaManager.getGuerrillaList()) {
                for (ArrayList<Integer> chestc : guerrilla.getPaymentChests()) {
                    if (guerrillaPlugin.getServer().getWorld(GuerrillaConfigurations.gworldname).getBlockAt(chestc.get(0),
                            chestc.get(1), chestc.get(2)).getType() != Material.CHEST) {

                        guerrilla.getPaymentChests().remove(chestc);
                        guerrilla.msggue("A missing payment chest was removed");
                        LOGGER.info("[Guerrilla] A payment chest was removed because the chest was missing");
                    }
                }
            }

            //Guerrilla.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setGuerrillaManager(GuerrillaManager guerrillaManager) {
        this.guerrillaManager = guerrillaManager;
    }
}
