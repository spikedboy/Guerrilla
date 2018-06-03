package net.spikedboy.guerrilla;

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

import java.util.ArrayList;
import java.util.Date;

public class GuerrillaPlayerListener implements Listener {

    public static Guerrilla plugin;

    GuerrillaPlayerListener(Guerrilla inPlug) {
        plugin = inPlug;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerChat(AsyncPlayerChatEvent event) {

        if (Guerrilla.TogglePlayerChat.get(event.getPlayer().getName()) != null && Guerrilla.TogglePlayerChat.get(event.getPlayer().getName()) == true) {
            GuerrillaG.getPlayerGuerrilla(event.getPlayer()).msggue(ChatColor.WHITE + "<" + event.getPlayer().getName() + "> " + event.getMessage());
            Guerrilla.log.info("[Gchat] " + event.getPlayer().getName() + " " + event.getMessage());
            event.setCancelled(true);
        }
        if (event.isCancelled()) return;
        GuerrillaG guerrilla = GuerrillaG.getPlayerGuerrilla(event.getPlayer());
        if (guerrilla == null) return;

        event.setMessage(ChatColor.GRAY + "<" + ChatColor.DARK_RED + guerrilla.getName() + ChatColor.GRAY + "> " + ChatColor.WHITE + event.getMessage());

    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {
        GuerrillaG guerrillao = GuerrillaG.getGuerrillaChunk(event.getBlockClicked().getChunk());
        Player player = event.getPlayer();
        GuerrillaG guerrillap = GuerrillaG.getPlayerGuerrilla(player);
        if ((guerrillao != null) && (guerrillao != guerrillap) && (event.getBlockClicked().getTypeId() == 8 || event.getBlockClicked().getTypeId() == 9)) {
            event.setCancelled(true);
        }

    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {

        final Player player = event.getPlayer();
        final GuerrillaG guerrilla = GuerrillaG.getPlayerGuerrilla(player);
        if (guerrilla == null) return;
        guerrilla.date = new Date();

        //Guerrilla.log.info("" + guerrilla.howManyOnline() + " " + GuerrillaG.isBeingClaimed(guerrilla));

        if (GuerrillaG.isBeingClaimed(guerrilla) && (guerrilla.howManyOnline() == 1)) {
            guerrilla.quitPunishmentDate = new Date();
        }
        int idClaim = GuerrillaG.getClaimingID(player.getName());
        if (idClaim != (-1)) {
            DelayedClaimData dcd = Guerrilla.delayedClaimDataQueue.search(idClaim);
            dcd.getGuerrillaOwner().msggue("Your atacker quit! You're no longer being attacked");
            Guerrilla.sinst.getScheduler().cancelTask(idClaim);
            Guerrilla.delayedClaimDataQueue.removeNode(dcd);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        Chunk chunk = event.getBlockClicked().getRelative(event.getBlockFace()).getChunk();
        GuerrillaG guerrillao = GuerrillaG.getGuerrillaChunk(chunk);
        GuerrillaG guerrillap = GuerrillaG.getPlayerGuerrilla(player);
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

        if (!Guerrilla.delayedClaimDataQueue.isEmpty()) {
            if (tchunk != fchunk) {

                //to ChunkList
                ArrayList<Double> tclist = new ArrayList<Double>(2);
                Double tchunkX = new Double(tchunk.getX());
                Double tchunkZ = new Double(tchunk.getZ());
                tclist.add(tchunkX);
                tclist.add(tchunkZ);

                //from ChunkList
                ArrayList<Double> fclist = new ArrayList<Double>(2);
                Double fchunkX = new Double(fchunk.getX());
                Double fchunkZ = new Double(fchunk.getZ());
                fclist.add(fchunkX);
                fclist.add(fchunkZ);

                DelayedClaimData dcd = Guerrilla.delayedClaimDataQueue.search(player.getName());

                //thread id


                //claimerleave
                if (dcd != null) {
                    Guerrilla.delayedClaimDataQueue.removeNode(dcd);
                    GuerrillaG gowner = GuerrillaG.getGuerrillaChunk(fchunk);
                    player.sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY + "You left the claim area! You loose! Good day sir");
                    gowner.msggue("Your atacker left!");
                    int id = dcd.getThreadID();
                    Guerrilla.sinst.getScheduler().cancelTask(id);
                }

                //cancel claim on defender enter
                //Integer id2 = Guerrilla.DclaimH.get(tclist);
                /*if (id2 != null && gplayer==GuerrillaG.getGuerrillaChunk(tchunk)){
					Guerrilla.sinst.getScheduler().cancelTask(id2.intValue());											
					Guerrilla.sinst.getPlayer(Guerrilla.DclaimH2.get(id2)).sendMessage("Your enemy interrupted your claim!");
					gplayer.msggue(player.getName() + " succesfully stopped a claim attempt!");
					Guerrilla.DclaimH2.remove(id2);
					Guerrilla.DclaimH.remove(tclist);
				}*/
            }
        }

        GuerrillaG fguerrilla = GuerrillaG.getGuerrillaChunk(from.getBlock().getChunk());
        GuerrillaG tguerrilla = GuerrillaG.getGuerrillaChunk(to.getBlock().getChunk());

        if (GuerrillaG.isSafeChunk(tchunk) && (GuerrillaG.isSafeChunk(tchunk) != GuerrillaG.isSafeChunk(fchunk))) {
            player.sendMessage(Guerrilla.gCh + "You're entering a safe area");
        }

        if (GuerrillaG.isSafeChunk(fchunk) && (GuerrillaG.isSafeChunk(tchunk) != GuerrillaG.isSafeChunk(fchunk))) {
            player.sendMessage(Guerrilla.gCh + "You left the safe area");
        }

        if ((fguerrilla != tguerrilla) && tguerrilla != null) {
            player.sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY + "You're entering " + tguerrilla.getName() + "'s territory");
        }
        if ((fguerrilla != tguerrilla) && tguerrilla == null) {
            player.sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY + "You left " + fguerrilla.getName() + "'s territory");
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity().getPlayer();
        DelayedClaimData dcd = Guerrilla.delayedClaimDataQueue.search(player.getName());
        if (dcd != null) {
            Guerrilla.sinst.getScheduler().cancelTask(dcd.getThreadID());
            player.sendMessage(Guerrilla.gCh + "You lost the territory dispute!");
            Guerrilla.delayedClaimDataQueue.removeNode(dcd);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerLogin(PlayerLoginEvent event) {
        GuerrillaG guerrilla = GuerrillaG.getPlayerGuerrilla(event.getPlayer());
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
        GuerrillaG guerrillap = GuerrillaG.getPlayerGuerrilla(player);
        Block block = event.getClickedBlock();
        GuerrillaG guerrillao = GuerrillaG.getGuerrillaChunk(block.getChunk());

        //Guerrilla.log.info("event " + block.getType());

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

            GuerrillaG guerrillac = GuerrillaG.getGuerrillaSafeChest(chest);

            if (guerrillac != null) {
                //Guerrilla.log.info(guerrillac.getName());
                if (guerrillap == null) {
                    event.setCancelled(true);
                    player.sendMessage(Guerrilla.gCh + "That is a Safe Chest! and it doesn't belong to you!");
                    return;
                }

                if (!(guerrillac.getLeader().equals(pname))) {
                    event.setCancelled(true);
                    player.sendMessage(Guerrilla.gCh + "That is a Safe Chest! and it doesn't belong to you!");
                    return;
                }
            }

            //setsafechests code

            if (Guerrilla.PlayerSetsSafe.containsKey(pname)) {
                boolean b1 = Guerrilla.PlayerSetsSafe.get(pname);
                Boolean b2 = new Boolean(b1);
                GuerrillaG guerrilla = GuerrillaG.getPlayerGuerrilla(player);
                if ((b2 == true) && (event.getClickedBlock()).getTypeId() == 54) {
                    guerrilla.addSafeChest(chest, player);
                    Guerrilla.PlayerSetsSafe.remove(event.getPlayer().getName());
                    return;
                } else if ((b2 == false)) {
                    guerrilla.removeSafeChest(chest, player);
                    Guerrilla.PlayerSetsSafe.remove(event.getPlayer().getName());
                    return;
                }
            }

            //setpaymentchests code

            if (Guerrilla.PlayerSetsBlock.containsKey(pname)) {
                boolean b1 = Guerrilla.PlayerSetsBlock.get(pname);
                Boolean b2 = new Boolean(b1);
                if (b2 == true) {
                    if (GuerrillaG.getPlayerGuerrilla(event.getPlayer()).addPaymentChest(chest)) {
                        Guerrilla.PlayerSetsBlock.remove(event.getPlayer().getName());
                        event.getPlayer().sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY + "Payment chest set");
                    } else {
                        Guerrilla.PlayerSetsBlock.remove(event.getPlayer().getName());
                        event.getPlayer().sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY + "That chest is already set");
                    }
                    return;
                } else if (b2 == false) {
                    if (GuerrillaG.getPlayerGuerrilla(event.getPlayer()).removePaymentChest(chest)) {
                        Guerrilla.PlayerSetsBlock.remove(event.getPlayer().getName());
                        event.getPlayer().sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY + "Payment chest removed");
                    } else {
                        Guerrilla.PlayerSetsBlock.remove(event.getPlayer().getName());
                        event.getPlayer().sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY + "That is not a payment chest");
                    }
                    return;
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
