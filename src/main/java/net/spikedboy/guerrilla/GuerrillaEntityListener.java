package net.spikedboy.guerrilla;

import java.util.logging.Logger;

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

public class GuerrillaEntityListener implements Listener {

    Logger log = Logger.getLogger("Minecraft");
    private static Guerrilla plugin;

    GuerrillaEntityListener(Guerrilla inPlug) {
        plugin = inPlug;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityExplode(EntityExplodeEvent event) {
        for (Block block : event.blockList()) {
            Chunk chunk = block.getChunk();
            GuerrillaG owner = GuerrillaG.getGuerrillaChunk(chunk);
            if (block.getType() == Material.CHEST) {
                if (GuerrillaG.getGuerrillaSafeChest((Chest) block.getState()) != null) {
                    event.setCancelled(true);
                    return;
                }
            }
            if (GuerrillaG.isSafeChunk(chunk)) {
                event.setCancelled(true);
                return;
            }
            if (owner == null) continue;
            owner.msgguespam("Something exploded in your territory");
            if (Guerrilla.tntProtection) {
                event.setCancelled(true);
                return;
            }
            if (owner.howManyOnline() == 0) {
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
                GuerrillaG damageechunkguerrilla = GuerrillaG.getGuerrillaChunk(pdamagee.getLocation().getBlock().getChunk());
                GuerrillaG damagerchunkguerrilla = GuerrillaG.getGuerrillaChunk(pdamager.getLocation().getBlock().getChunk());
                GuerrillaG gpdamagee = GuerrillaG.getPlayerGuerrilla(pdamagee);
                GuerrillaG gpdamager = GuerrillaG.getPlayerGuerrilla(pdamager);

                if (GuerrillaG.isSafeChunk(damageechunk)) {
                    event.setCancelled(true);
                    return;
                }

                if ((gpdamagee == null) || (gpdamager == null)) {
                    return;
                }

                if ((damageechunkguerrilla != null) && (damageechunkguerrilla.equals(gpdamagee))) {
                    if ((sub.getDamage() / Guerrilla.defbonus) == 0)
                        pdamager.sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY + "Your weapon does nothing to " + pdamagee.getName() + " here!");
                    sub.setDamage(sub.getDamage() / Guerrilla.defbonus);
                }

                if ((damagerchunkguerrilla != null) && (damagerchunkguerrilla.equals(gpdamager))) {
                    sub.setDamage(sub.getDamage() * Guerrilla.atkbonus);
                }

                if (gpdamagee.equals(gpdamager)) {
                    event.setCancelled(true);
                    pdamager.sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY + "Can't damage a fellow comrade");
                }

            }

        }
    }

}
