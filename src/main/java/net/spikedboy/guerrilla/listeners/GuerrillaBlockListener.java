package net.spikedboy.guerrilla.listeners;

import com.google.inject.Inject;
import net.spikedboy.guerrilla.GuerrillaPlugin;
import net.spikedboy.guerrilla.guerrilla.Guerrilla;
import net.spikedboy.guerrilla.guerrilla.GuerrillaManager;
import net.spikedboy.guerrilla.guerrilla.Messager;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
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

import java.util.logging.Logger;

public class GuerrillaBlockListener implements Listener {

    private Logger log = Logger.getLogger("Minecraft");
    private final GuerrillaPlugin guerrillaPlugin;

    @Inject
    private GuerrillaManager guerrillaManager;

    public GuerrillaBlockListener(GuerrillaPlugin inPlug) {
        guerrillaPlugin = inPlug;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockPlace(BlockPlaceEvent event) {
        Guerrilla guerrilla = guerrillaManager.getPlayerGuerrilla(event.getPlayer());
        Chunk chunk = event.getBlock().getChunk();
        Guerrilla guerrillao = guerrillaManager.getGuerrillaChunk(chunk);
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
	 * (Guerrilla
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
        Guerrilla guerrilla = guerrillaManager.getPlayerGuerrilla(event.getPlayer()); //Guerrilla object of the player that has broke the block
        Chunk chunk = event.getBlock().getChunk();
        Guerrilla guerrillao = guerrillaManager.getGuerrillaChunk(chunk); //Guerrilla object propietary of the chunk in which the block is in
        Player player = event.getPlayer();

        if (eblock.getType() == Material.CHEST) {

            Chest chest = (Chest) eblock.getState();
            Guerrilla guchest = guerrillaManager.getGuerrillaSafeChest(chest);
            String leadero = null;

            if (guchest != null) {
                leadero = guchest.getLeader();
            }
            if (leadero != null) {
                if ((!(leadero.equals(player.getName())))) {
                    Server server = guerrillaPlugin.getServer();

                    if (server.getPlayer(leadero) != null)
                        server
                                .getPlayer(leadero)
                                .sendMessage(
                                        Messager.GUERRILLA_MESSAGE_PREFIX
                                                + "Someone tried to break your safechest!");
                    event.setCancelled(true);

                } else if (leadero.equals(player.getName())) {
                    player.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX
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

    public void setGuerrillaManager(GuerrillaManager guerrillaManager) {
        this.guerrillaManager = guerrillaManager;
    }
}
