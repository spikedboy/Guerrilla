package net.spikedboy.guerrilla;

import java.util.logging.Logger;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.util.Vector;

public class GuerrillaBlockListener implements Listener {

    public static Guerrilla plugin;
    Logger log = Logger.getLogger("Minecraft");

    GuerrillaBlockListener(Guerrilla inPlug) {
        plugin = inPlug;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockPlace(BlockPlaceEvent event) {
        GuerrillaG guerrilla = GuerrillaG.getPlayerGuerrilla(event.getPlayer());
        Chunk chunk = event.getBlock().getChunk();
        GuerrillaG guerrillao = GuerrillaG.getGuerrillaChunk(chunk);
        Player player = event.getPlayer();
        // Vector vec = player.getVelocity();
        Location loc = player.getLocation();
        float pitch = loc.getPitch();
        float yaw = loc.getYaw();
        Block locBlock = loc.getBlock();
        for (int i = locBlock.getY() - 1; i >= 0; i--) {
            Block aBlock = player.getWorld().getBlockAt(locBlock.getX(), i,
                    locBlock.getZ());
            if (aBlock.getType() != Material.AIR) {
                loc.setY(aBlock.getRelative(BlockFace.UP).getY());
                loc.setPitch(pitch);
                loc.setYaw(yaw);
                break;
            }
        }

        // Vector vn = new Vector(0,-2,0);

        if (event.getBlockReplacedState().getTypeId() == (10)
                || (event.getBlockReplacedState().getTypeId() == 11)) {
            return;
        }
        if (guerrilla != null) {
            if (!(guerrilla.ownsGuerrillaChunk(chunk))) {
                guerrillao
                        .msgguespam("Someone tried to place a block in your territory");
                // player.setVelocity(vn);
                player.teleport(loc);
                event.setCancelled(true);
                player.setVelocity(new Vector(0, 0, 0));
            }
        } else {
            if (guerrillao != null) {
                guerrillao
                        .msgguespam("Someone tried to place a block in your territory");
                // player.setVelocity(vn);
                player.teleport(loc);
                event.setCancelled(true);
                player.setVelocity(new Vector(0, 0, 0));
            }
        }
    }

	/*
     * public void onBlockPistonExtend(BlockPistonExtendEvent event) { if
	 * (GuerrillaG
	 * .getGuerrillaChunk(event.getBlock().getRelative(event.getDirection(),
	 * 1).getChunk()) {
	 * 
	 * } }
	 * 
	 * public void onBlockPistonRetract(BlockPistonRetractEvent event) {
	 * event.getBlock().getRelative(event.getDirection(), 2); }
	 */

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockBreak(BlockBreakEvent event) {
        Block eblock = event.getBlock();
        GuerrillaG guerrilla = GuerrillaG.getPlayerGuerrilla(event.getPlayer()); //GuerrillaG object of the player that has broke the block
        Chunk chunk = event.getBlock().getChunk();
        GuerrillaG guerrillao = GuerrillaG.getGuerrillaChunk(chunk); //GuerrillaG object propietary of the chunk in which the block is in
        Player player = event.getPlayer();

        if (eblock.getType() == Material.CHEST) {

            Chest chest = (Chest) eblock.getState();
            GuerrillaG guchest = GuerrillaG.getGuerrillaSafeChest(chest);
            String leadero = null;

            if (guchest != null) {
                leadero = guchest.getLeader();
            }
            if (leadero != null) {
                if ((!(leadero.equals(player.getName())))) {

                    if (Guerrilla.sinst.getPlayer(leadero) != null)
                        Guerrilla.sinst
                                .getPlayer(leadero)
                                .sendMessage(
                                        Guerrilla.gCh
                                                + "Someone tried to break your safechest!");
                    event.setCancelled(true);

                } else if (leadero.equals(player.getName())) {
                    player.sendMessage(Guerrilla.gCh
                            + "Sorry! You can't do that! Safe chests are safe! From everyone!");
                    event.setCancelled(true);
                }
            }
        }

        if (eblock.getType() == Material.BED_BLOCK) {
            return;
        }

        if (guerrilla != null) {

            if (!(guerrilla.ownsGuerrillaChunk(chunk))) {

                event.setCancelled(true);
                guerrillao
                        .msgguespam("Someone tried to destroy a block in your territory");

            }

        } else {
            if (guerrillao != null) {
                guerrillao
                        .msgguespam("Someone tried to destroy a block in your territory");
                event.setCancelled(true);
            }
        }

    }

}
