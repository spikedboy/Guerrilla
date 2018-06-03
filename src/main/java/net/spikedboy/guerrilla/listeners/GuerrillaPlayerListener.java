package net.spikedboy.guerrilla.listeners;

import net.spikedboy.guerrilla.Guerrilla;
import net.spikedboy.guerrilla.GuerrillaPlugin;
import net.spikedboy.guerrilla.landclaim.DelayedClaimData;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;

import java.util.Date;

public class GuerrillaPlayerListener implements Listener {

    public GuerrillaPlayerListener(GuerrillaPlugin inPlug) {
        GuerrillaPlugin plugin = inPlug;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerChat(AsyncPlayerChatEvent event) {

        if (GuerrillaPlugin.togglePlayerChat.get(event.getPlayer().getName()) != null && GuerrillaPlugin.togglePlayerChat.get(event.getPlayer().getName()) == true) {
            Guerrilla.getPlayerGuerrilla(event.getPlayer()).msggue(ChatColor.WHITE + "<" + event.getPlayer().getName() + "> " + event.getMessage());
            GuerrillaPlugin.log.info("[Gchat] " + event.getPlayer().getName() + " " + event.getMessage());
            event.setCancelled(true);
        }
        if (event.isCancelled()) return;
        Guerrilla guerrilla = Guerrilla.getPlayerGuerrilla(event.getPlayer());
        if (guerrilla == null) return;

        event.setMessage(ChatColor.GRAY + "<" + ChatColor.DARK_RED + guerrilla.getName() + ChatColor.GRAY + "> " + ChatColor.WHITE + event.getMessage());

    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {
        Guerrilla guerrillao = Guerrilla.getGuerrillaChunk(event.getBlockClicked().getChunk());
        Player player = event.getPlayer();
        Guerrilla guerrillap = Guerrilla.getPlayerGuerrilla(player);
        if ((guerrillao != null) && (guerrillao != guerrillap) && (event.getBlockClicked().getTypeId() == 8 || event.getBlockClicked().getTypeId() == 9)) {
            event.setCancelled(true);
        }

    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {

        final Player player = event.getPlayer();
        final Guerrilla guerrilla = Guerrilla.getPlayerGuerrilla(player);
        if (guerrilla == null) return;
        guerrilla.date = new Date();

        //GuerrillaPlugin.log.info("" + guerrilla.howManyGuerrillaMembersOnline() + " " + Guerrilla.isBeingClaimed(guerrilla));

        if (Guerrilla.isBeingClaimed(guerrilla) && (guerrilla.howManyGuerrillaMembersOnline() == 1)) {
            guerrilla.quitPunishmentDate = new Date();
        }
        int idClaim = Guerrilla.getClaimingID(player.getName());
        if (idClaim != (-1)) {
            DelayedClaimData dcd = GuerrillaPlugin.delayedClaimDataQueue.search(idClaim);
            dcd.getGuerrillaOwner().msggue("Your atacker quit! You're no longer being attacked");
            GuerrillaPlugin.serverInstance.getScheduler().cancelTask(idClaim);
            GuerrillaPlugin.delayedClaimDataQueue.removeNode(dcd);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        Chunk chunk = event.getBlockClicked().getRelative(event.getBlockFace()).getChunk();
        Guerrilla guerrillao = Guerrilla.getGuerrillaChunk(chunk);
        Guerrilla guerrillap = Guerrilla.getPlayerGuerrilla(player);
        if (guerrillao == null) return;
        if (guerrillap == null) {
            event.setCancelled(true);
            return;
        }
        if (guerrillap != guerrillao) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        Chunk fchunk = from.getBlock().getChunk();
        Chunk tchunk = to.getBlock().getChunk();
        if (fchunk == tchunk) return;
        Player player = event.getPlayer();

        if (!GuerrillaPlugin.delayedClaimDataQueue.isEmpty()) {
            if (tchunk != fchunk) {
                DelayedClaimData dcd = GuerrillaPlugin.delayedClaimDataQueue.search(player.getName());
                //claimerleave
                if (dcd != null) {
                    GuerrillaPlugin.delayedClaimDataQueue.removeNode(dcd);
                    Guerrilla gowner = Guerrilla.getGuerrillaChunk(fchunk);
                    player.sendMessage(ChatColor.DARK_RED + "[GuerrillaPlugin] " + ChatColor.GRAY + "You left the claim area! You loose! Good day sir");
                    gowner.msggue("Your atacker left!");
                    int id = dcd.getThreadID();
                    GuerrillaPlugin.serverInstance.getScheduler().cancelTask(id);
                }
            }
        }

        Guerrilla fguerrilla = Guerrilla.getGuerrillaChunk(from.getBlock().getChunk());
        Guerrilla tguerrilla = Guerrilla.getGuerrillaChunk(to.getBlock().getChunk());

        if (Guerrilla.isSafeChunk(tchunk) && (Guerrilla.isSafeChunk(tchunk) != Guerrilla.isSafeChunk(fchunk))) {
            player.sendMessage(GuerrillaPlugin.gCh + "You're entering a safe area");
        }

        if (Guerrilla.isSafeChunk(fchunk) && (Guerrilla.isSafeChunk(tchunk) != Guerrilla.isSafeChunk(fchunk))) {
            player.sendMessage(GuerrillaPlugin.gCh + "You left the safe area");
        }

        if ((fguerrilla != tguerrilla) && tguerrilla != null) {
            player.sendMessage(ChatColor.DARK_RED + "[GuerrillaPlugin] " + ChatColor.GRAY + "You're entering " + tguerrilla.getName() + "'s territory");
        }
        if ((fguerrilla != tguerrilla) && tguerrilla == null) {
            player.sendMessage(ChatColor.DARK_RED + "[GuerrillaPlugin] " + ChatColor.GRAY + "You left " + fguerrilla.getName() + "'s territory");
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity().getPlayer();
        DelayedClaimData dcd = GuerrillaPlugin.delayedClaimDataQueue.search(player.getName());
        if (dcd != null) {
            GuerrillaPlugin.serverInstance.getScheduler().cancelTask(dcd.getThreadID());
            player.sendMessage(GuerrillaPlugin.gCh + "You lost the territory dispute!");
            GuerrillaPlugin.delayedClaimDataQueue.removeNode(dcd);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerLogin(PlayerLoginEvent event) {
        Guerrilla guerrilla = Guerrilla.getPlayerGuerrilla(event.getPlayer());
        if ((guerrilla != null) && (guerrilla.quitPunishmentDate != null)) {
            guerrilla.quitPunishmentDate = null;
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.hasBlock()) return;

        Player player = event.getPlayer();
        String pname = player.getName();
        Location loc = player.getLocation();
        Guerrilla guerrillap = Guerrilla.getPlayerGuerrilla(player);
        Block block = event.getClickedBlock();
        Guerrilla guerrillao = Guerrilla.getGuerrillaChunk(block.getChunk());

        //GuerrillaPlugin.log.info("event " + block.getType());

        if (block.getType() == Material.TRAP_DOOR) {
            if ((guerrillao != guerrillap) && (guerrillao != null)) {
                event.setCancelled(true);
                player.teleport(loc);
                return;
            }
        }

        if (block.getType() == Material.STONE_BUTTON) {
            if ((guerrillao != guerrillap) && (guerrillao != null)) {
                event.setCancelled(true);
                player.teleport(loc);
                return;
            }
        }

		/*if (block.getType()==Material.WOODEN_DOOR) {
			if ((guerrillao != guerrillap) && (guerrillao != null)) {
				event.setCancelled(true);
				player.teleport(loc);
				return;
			}
		}*/

        if (block.getType() == Material.FENCE_GATE) {
            if ((guerrillao != guerrillap) && (guerrillao != null)) {
                event.setCancelled(true);
                player.teleport(loc);
                return;
            }
        }

        if (block.getType() == Material.CHEST) {

            Chest chest = (Chest) event.getClickedBlock().getState();

            //safechests safe code

            Guerrilla guerrillac = Guerrilla.getGuerrillaSafeChest(chest);

            if (guerrillac != null) {
                //GuerrillaPlugin.log.info(guerrillac.getName());
                if (guerrillap == null) {
                    event.setCancelled(true);
                    player.sendMessage(GuerrillaPlugin.gCh + "That is a Safe Chest! and it doesn't belong to you!");
                    return;
                }

                if (!(guerrillac.getLeader().equals(pname))) {
                    event.setCancelled(true);
                    player.sendMessage(GuerrillaPlugin.gCh + "That is a Safe Chest! and it doesn't belong to you!");
                    return;
                }
            }

            //setsafechests code

            if (GuerrillaPlugin.playerSetsSafe.containsKey(pname)) {
                Boolean b2 = GuerrillaPlugin.playerSetsSafe.get(pname);
                Guerrilla guerrilla = Guerrilla.getPlayerGuerrilla(player);
                if ((b2) && (event.getClickedBlock()).getTypeId() == 54) {
                    guerrilla.addSafeChest(chest, player);
                    GuerrillaPlugin.playerSetsSafe.remove(event.getPlayer().getName());
                    return;
                } else if ((!b2)) {
                    guerrilla.removeSafeChest(chest, player);
                    GuerrillaPlugin.playerSetsSafe.remove(event.getPlayer().getName());
                    return;
                }
            }

            //setpaymentchests code

            if (GuerrillaPlugin.playerSetsBlock.containsKey(pname)) {
                Boolean b2 = GuerrillaPlugin.playerSetsBlock.get(pname);
                if (b2) {
                    if (Guerrilla.getPlayerGuerrilla(event.getPlayer()).addPaymentChest(chest)) {
                        GuerrillaPlugin.playerSetsBlock.remove(event.getPlayer().getName());
                        event.getPlayer().sendMessage(ChatColor.DARK_RED + "[GuerrillaPlugin] " + ChatColor.GRAY + "Payment chest set");
                    } else {
                        GuerrillaPlugin.playerSetsBlock.remove(event.getPlayer().getName());
                        event.getPlayer().sendMessage(ChatColor.DARK_RED + "[GuerrillaPlugin] " + ChatColor.GRAY + "That chest is already set");
                    }
                } else {
                    if (Guerrilla.getPlayerGuerrilla(event.getPlayer()).removePaymentChest(chest)) {
                        GuerrillaPlugin.playerSetsBlock.remove(event.getPlayer().getName());
                        event.getPlayer().sendMessage(ChatColor.DARK_RED + "[GuerrillaPlugin] " + ChatColor.GRAY + "Payment chest removed");
                    } else {
                        GuerrillaPlugin.playerSetsBlock.remove(event.getPlayer().getName());
                        event.getPlayer().sendMessage(ChatColor.DARK_RED + "[GuerrillaPlugin] " + ChatColor.GRAY + "That is not a payment chest");
                    }
                }
            }
            //end setpayments
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        //
    }

}
