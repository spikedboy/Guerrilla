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

    private final GuerrillaPlugin guerrillaPlugin;
    private Logger log = Logger.getLogger("Minecraft");
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
        if (isGuerrillaNotNull(guerrilla)) {
            if (doesTheGuerrillaOwnTheChunk(guerrilla, chunk)) {
                guerrillao
                        .sendMessagePreventingSpam("Someone tried to place a block in your territory");
                // player.setVelocity(vn);
                player.teleport(loc);
                event.setCancelled(true);
                player.setVelocity(new Vector(0, 0, 0));
            }
        } else {
            if (isGuerrillaNotNull(guerrillao)) {
                guerrillao
                        .sendMessagePreventingSpam("Someone tried to place a block in your territory");
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
        Block blockEventHappenedIn = event.getBlock();
        Guerrilla guerrillaOfPlayerBreakingBlock = guerrillaManager.getPlayerGuerrilla(event.getPlayer());

        Chunk chunkTheBlockWasBrokenIn = event.getBlock().getChunk();
        Guerrilla guerrillaOwnerOfTheChunkTheBrokenBlockWasIn = guerrillaManager.getGuerrillaChunk(chunkTheBlockWasBrokenIn);

        Player player = event.getPlayer();

        if (isBrokenBlockAChest(blockEventHappenedIn)) {
            protectChestIfSafeChest(event, blockEventHappenedIn, player);
        }

        if (isBlockBrokenBedType(blockEventHappenedIn)) {
            return;
        }

        if (isGuerrillaNotNull(guerrillaOfPlayerBreakingBlock)) {
            if (doesTheGuerrillaOwnTheChunk(guerrillaOfPlayerBreakingBlock, chunkTheBlockWasBrokenIn)) {
                cancelBlockBreakingEventAndMessageOwner(event, guerrillaOwnerOfTheChunkTheBrokenBlockWasIn);
            }
        } else {
            if (isGuerrillaNotNull(guerrillaOwnerOfTheChunkTheBrokenBlockWasIn)) {
                cancelBlockBreakingEventAndMessageOwner(event, guerrillaOwnerOfTheChunkTheBrokenBlockWasIn);
            }
        }

    }

    private boolean isBlockBrokenBedType(Block blockEventHappenedIn) {
        return Material.BED_BLOCK.equals(blockEventHappenedIn.getType());
    }

    private boolean doesTheGuerrillaOwnTheChunk(Guerrilla guerrillaOfPlayerBreakingBlock, Chunk chunkTheBlockWasBrokenIn) {
        return !(guerrillaOfPlayerBreakingBlock.ownsGuerrillaChunk(chunkTheBlockWasBrokenIn));
    }

    private boolean isGuerrillaNotNull(Guerrilla guerrilla) {
        return guerrilla != null;
    }

    private void cancelBlockBreakingEventAndMessageOwner(BlockBreakEvent event,
                                                         Guerrilla guerrillaOwnerOfTheChunkTheBrokenBlockWasIn) {
        guerrillaOwnerOfTheChunkTheBrokenBlockWasIn
                .sendMessagePreventingSpam("Someone tried to destroy a block in your territory");
        event.setCancelled(true);
    }

    private void protectChestIfSafeChest(BlockBreakEvent event, Block blockEventHappenedIn, Player playerTriggeringEvent) {
        Chest chest = (Chest) blockEventHappenedIn.getState();
        Guerrilla ownerGuerrillaOfSafeChest = guerrillaManager.getGuerrillaSafeChest(chest);

        if (isGuerrillaNotNull(ownerGuerrillaOfSafeChest)) {
            String playerNameLeaderChestOwnerGuerrilla = ownerGuerrillaOfSafeChest.getLeader();

            event.setCancelled(true);

            if (playerNameLeaderChestOwnerGuerrilla != null) {
                if (isPlayerTriggeringEventNotLeaderOfOwnerGuerrilla(playerTriggeringEvent, playerNameLeaderChestOwnerGuerrilla)) {
                    notifyOwnerOfBlockBreakingIfOnline(event, playerNameLeaderChestOwnerGuerrilla);
                } else if (isPlayerTheLeaderOfTheGuerrilla(playerTriggeringEvent, playerNameLeaderChestOwnerGuerrilla)) {
                    playerTriggeringEvent.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX
                            + "Sorry! You can't do that! Safe chests are safe! From everyone!");
                }
            }
        }
    }

    private boolean isPlayerTheLeaderOfTheGuerrilla(Player playerTriggeringEvent, String playerNameLeaderChestOwnerGuerrilla) {
        return playerNameLeaderChestOwnerGuerrilla.equals(playerTriggeringEvent.getName());
    }

    private void notifyOwnerOfBlockBreakingIfOnline(BlockBreakEvent event, String playerNameLeaderChestOwnerGuerrilla) {
        Server server = guerrillaPlugin.getServer();

        if (isChestOwnerOnline(playerNameLeaderChestOwnerGuerrilla, server)) {
            messageOwnerSafeChestBrokenWarning(playerNameLeaderChestOwnerGuerrilla, server);
        }
    }

    private void messageOwnerSafeChestBrokenWarning(String playerNameLeaderChestOwnerGuerrilla, Server server) {
        server.getPlayer(playerNameLeaderChestOwnerGuerrilla).sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX
                                + "Someone tried to break your safechest!");
    }

    private boolean isChestOwnerOnline(String playerNameLeaderChestOwnerGuerrilla, Server server) {
        return server.getPlayer(playerNameLeaderChestOwnerGuerrilla) != null;
    }

    private boolean isPlayerTriggeringEventNotLeaderOfOwnerGuerrilla(Player playerTriggeringEvent, String playerNameLeaderChestOwnerGuerrilla) {
        return !playerNameLeaderChestOwnerGuerrilla.equals(playerTriggeringEvent.getName());
    }

    private boolean isBrokenBlockAChest(Block blockEventHappenedIn) {
        return Material.CHEST.equals(blockEventHappenedIn.getType());
    }

    public void setGuerrillaManager(GuerrillaManager guerrillaManager) {
        this.guerrillaManager = guerrillaManager;
    }
}
