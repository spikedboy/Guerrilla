package net.spikedboy.guerrilla.listeners;

import net.spikedboy.guerrilla.Guerrilla;
import net.spikedboy.guerrilla.GuerrillaPlugin;
import net.spikedboy.guerrilla.configuration.GuerrillaConfigurations;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldSaveEvent;

import java.util.ArrayList;

public class GuerrillaWorldListener implements Listener {

    public GuerrillaWorldListener(GuerrillaPlugin inPlug) {
        GuerrillaPlugin plugin = inPlug;
    }


    @EventHandler
    public void onWorldSave(WorldSaveEvent event) {
        try {
            //GuerrillaPlugin.log.info("[GuerrillaPlugin] Saving..");
            for (Guerrilla guerrilla : Guerrilla.guerrillaList) {
                for (ArrayList<Integer> chestc : guerrilla.paymentChests) {
                    if (GuerrillaPlugin.serverInstance.getWorld(GuerrillaConfigurations.gworldname).getBlockAt(chestc.get(0).intValue(),
                            chestc.get(1).intValue(), chestc.get(2).intValue()).getType() != Material.CHEST) {

                        guerrilla.paymentChests.remove(chestc);
                        guerrilla.msggue("A missing paymentchest was removed");
                        GuerrillaPlugin.log.info("[GuerrillaPlugin] A paymentchest was removed because the chest was missing");
                    }
                }
            }

            //Guerrilla.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
