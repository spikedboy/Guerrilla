package net.spikedboy.guerrilla.listeners;

import com.google.inject.Inject;
import net.spikedboy.guerrilla.GuerrillaPlugin;
import net.spikedboy.guerrilla.configuration.GuerrillaConfigurations;
import net.spikedboy.guerrilla.guerrilla.Guerrilla;
import net.spikedboy.guerrilla.guerrilla.GuerrillaManager;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.logging.Logger;

public class GuerrillaEntityListener implements Listener {

    private Logger log = Logger.getLogger("Minecraft");

    @Inject
    private GuerrillaManager guerrillaManager;

    public GuerrillaEntityListener(GuerrillaPlugin inPlug) {
        GuerrillaPlugin plugin = inPlug;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityExplode(EntityExplodeEvent event) {
        for (Block block : event.blockList()) {
            Chunk chunk = block.getChunk();
            Guerrilla owner = guerrillaManager.getGuerrillaChunk(chunk);
            if (block.getType() == Material.CHEST) {
                if (guerrillaManager.getGuerrillaSafeChest((Chest) block.getState()) != null) {
                    event.setCancelled(true);
                    return;
                }
            }
            if (guerrillaManager.isSafeChunk(chunk)) {
                event.setCancelled(true);
                return;
            }
            if (owner == null) continue;
            owner.msgguespam("Something exploded in your territory");
            if (GuerrillaConfigurations.tntProtection) {
                event.setCancelled(true);
                return;
            }
            if (owner.howManyGuerrillaMembersOnline() == 0) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamage(EntityDamageEvent event) {

        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent sub = (EntityDamageByEntityEvent) event;
            Entity damager = sub.getDamager();
            Entity damagee = sub.getEntity();


            if ((damagee instanceof Player) && (damager instanceof Player)) {
                Player pdamagee = (Player) damagee;
                Player pdamager = (Player) damager;
                Chunk damageechunk = pdamagee.getLocation().getBlock().getChunk();
                //Chunk damagerchunk = pdamager.getLocation().getBlock().getChunk();
                Guerrilla damageechunkguerrilla = guerrillaManager.getGuerrillaChunk(pdamagee.getLocation().getBlock().getChunk());
                Guerrilla damagerchunkguerrilla = guerrillaManager.getGuerrillaChunk(pdamager.getLocation().getBlock().getChunk());
                Guerrilla gpdamagee = guerrillaManager.getPlayerGuerrilla(pdamagee);
                Guerrilla gpdamager = guerrillaManager.getPlayerGuerrilla(pdamager);

                if (guerrillaManager.isSafeChunk(damageechunk)) {
                    event.setCancelled(true);
                    return;
                }

                if ((gpdamagee == null) || (gpdamager == null)) {
                    return;
                }

                if ((damageechunkguerrilla != null) && (damageechunkguerrilla.equals(gpdamagee))) {
                    if ((sub.getDamage() / GuerrillaConfigurations.defbonus) == 0)
                        pdamager.sendMessage(ChatColor.DARK_RED + "[GuerrillaPlugin] " + ChatColor.GRAY + "Your weapon does nothing to " + pdamagee.getName() + " here!");
                    sub.setDamage(sub.getDamage() / GuerrillaConfigurations.defbonus);
                }

                if ((damagerchunkguerrilla != null) && (damagerchunkguerrilla.equals(gpdamager))) {
                    sub.setDamage(sub.getDamage() * GuerrillaConfigurations.atkbonus);
                }

                if (gpdamagee.equals(gpdamager)) {
                    event.setCancelled(true);
                    pdamager.sendMessage(ChatColor.DARK_RED + "[GuerrillaPlugin] " + ChatColor.GRAY + "Can't damage a fellow comrade");
                }

            }

        }
    }

    public void setGuerrillaManager(GuerrillaManager guerrillaManager) {
        this.guerrillaManager = guerrillaManager;
    }
}
