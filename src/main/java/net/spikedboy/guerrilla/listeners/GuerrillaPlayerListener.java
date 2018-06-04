package net.spikedboy.guerrilla.listeners;

import com.google.inject.Inject;
import net.spikedboy.guerrilla.GuerrillaPlugin;
import net.spikedboy.guerrilla.guerrilla.Guerrilla;
import net.spikedboy.guerrilla.guerrilla.GuerrillaManager;
import net.spikedboy.guerrilla.guerrilla.Messager;
import net.spikedboy.guerrilla.landclaim.DelayedClaimData;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Date;
import java.util.logging.Logger;

public class GuerrillaPlayerListener implements Listener {

    private static final Logger LOGGER = Logger.getLogger("Minecraft");

    private final GuerrillaPlugin plugin;

    private final Server server;

    @Inject
    private GuerrillaManager guerrillaManager;

    public GuerrillaPlayerListener(GuerrillaPlugin inPlug) {
        plugin = inPlug;
        server = plugin.getServer();
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (guerrillaManager.getTogglePlayerChat().get(event.getPlayer().getName()) != null && guerrillaManager.getTogglePlayerChat().get(event.getPlayer().getName()) == true) {
            guerrillaManager.getPlayerGuerrilla(event.getPlayer()).msggue(ChatColor.WHITE + "<" + event.getPlayer().getName() + "> " + event.getMessage());
            LOGGER.info("[Gchat] " + event.getPlayer().getName() + " " + event.getMessage());
            event.setCancelled(true);
        }
        if (event.isCancelled()) return;
        Guerrilla guerrilla = guerrillaManager.getPlayerGuerrilla(event.getPlayer());
        if (guerrilla == null) return;

        event.setMessage(ChatColor.GRAY + "<" + ChatColor.DARK_RED + guerrilla.getName() + ChatColor.GRAY + "> " + ChatColor.WHITE + event.getMessage());

    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {
        Guerrilla guerrillao = guerrillaManager.getGuerrillaChunk(event.getBlockClicked().getChunk());
        Player player = event.getPlayer();
        Guerrilla guerrillap = guerrillaManager.getPlayerGuerrilla(player);
        if ((guerrillao != null) && (guerrillao != guerrillap) && (event.getBlockClicked().getTypeId() == 8 || event.getBlockClicked().getTypeId() == 9)) {
            event.setCancelled(true);
        }

    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {

        final Player player = event.getPlayer();
        final Guerrilla guerrilla = guerrillaManager.getPlayerGuerrilla(player);
        if (guerrilla == null) return;
        guerrilla.setDate(new Date());

        //GuerrillaPlugin.LOGGER.info("" + guerrilla.howManyGuerrillaMembersOnline() + " " + Guerrilla.isBeingClaimed(guerrilla));

        if (guerrilla.isBeingClaimed() && (guerrilla.howManyGuerrillaMembersOnline() == 1)) {
            guerrilla.setQuitPunishmentDate(new Date());
        }
        int idClaim = guerrillaManager.getClaimingID(player.getName());
        if (idClaim != (-1)) {
            DelayedClaimData dcd = guerrillaManager.getDelayedClaimDataQueue().search(idClaim);
            dcd.getGuerrillaOwner().msggue("Your atacker quit! You're no longer being attacked");
            server.getScheduler().cancelTask(idClaim);
            guerrillaManager.getDelayedClaimDataQueue().removeNode(dcd);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        Chunk chunk = event.getBlockClicked().getRelative(event.getBlockFace()).getChunk();
        Guerrilla guerrillao = guerrillaManager.getGuerrillaChunk(chunk);
        Guerrilla guerrillap = guerrillaManager.getPlayerGuerrilla(player);
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

        if (!guerrillaManager.getDelayedClaimDataQueue().isEmpty()) {
            if (tchunk != fchunk) {
                DelayedClaimData dcd = guerrillaManager.getDelayedClaimDataQueue().search(player.getName());
                //claimerleave
                if (dcd != null) {
                    guerrillaManager.getDelayedClaimDataQueue().removeNode(dcd);
                    Guerrilla gowner = guerrillaManager.getGuerrillaChunk(fchunk);
                    player.sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY + "You left the claim area! You loose! Good day sir");
                    gowner.msggue("Your atacker left!");
                    int id = dcd.getThreadID();
                    server.getScheduler().cancelTask(id);
                }
            }
        }

        Guerrilla fguerrilla = guerrillaManager.getGuerrillaChunk(from.getBlock().getChunk());
        Guerrilla tguerrilla = guerrillaManager.getGuerrillaChunk(to.getBlock().getChunk());

        if (guerrillaManager.isSafeChunk(tchunk) && (guerrillaManager.isSafeChunk(tchunk) != guerrillaManager.isSafeChunk(fchunk))) {
            player.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "You're entering a safe area");
        }

        if (guerrillaManager.isSafeChunk(fchunk) && (guerrillaManager.isSafeChunk(tchunk) != guerrillaManager.isSafeChunk(fchunk))) {
            player.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "You left the safe area");
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
        DelayedClaimData dcd = guerrillaManager.getDelayedClaimDataQueue().search(player.getName());
        if (dcd != null) {
            server.getScheduler().cancelTask(dcd.getThreadID());
            player.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "You lost the territory dispute!");
            guerrillaManager.getDelayedClaimDataQueue().removeNode(dcd);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerLogin(PlayerLoginEvent event) {
        Guerrilla guerrilla = guerrillaManager.getPlayerGuerrilla(event.getPlayer());
        if ((guerrilla != null) && (guerrilla.getQuitPunishmentDate() != null)) {
            guerrilla.setQuitPunishmentDate(null);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.hasBlock()) return;

        Player player = event.getPlayer();
        String pname = player.getName();
        Location loc = player.getLocation();
        Guerrilla guerrillap = guerrillaManager.getPlayerGuerrilla(player);
        Block block = event.getClickedBlock();
        Guerrilla guerrillao = guerrillaManager.getGuerrillaChunk(block.getChunk());

        //GuerrillaPlugin.LOGGER.info("event " + block.getType());

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

            Guerrilla guerrillac = guerrillaManager.getGuerrillaSafeChest(chest);

            if (guerrillac != null) {
                //GuerrillaPlugin.LOGGER.info(guerrillac.getName());
                if (guerrillap == null) {
                    event.setCancelled(true);
                    player.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "That is a Safe Chest! and it doesn't belong to you!");
                    return;
                }

                if (!(guerrillac.getLeader().equals(pname))) {
                    event.setCancelled(true);
                    player.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "That is a Safe Chest! and it doesn't belong to you!");
                    return;
                }
            }

            //setsafechests code

            if (guerrillaManager.getPlayerSetsSafe().containsKey(pname)) {
                Boolean b2 = guerrillaManager.getPlayerSetsSafe().get(pname);
                Guerrilla guerrilla = guerrillaManager.getPlayerGuerrilla(player);
                if ((b2) && (event.getClickedBlock()).getTypeId() == 54) {
                    guerrilla.addSafeChest(chest, player);
                    guerrillaManager.getPlayerSetsSafe().remove(event.getPlayer().getName());
                    return;
                } else if ((!b2)) {
                    guerrilla.removeSafeChest(chest, player);
                    guerrillaManager.getPlayerSetsSafe().remove(event.getPlayer().getName());
                    return;
                }
            }

            //setpaymentchests code

            if (guerrillaManager.getPlayerSetsBlock().containsKey(pname)) {
                Boolean b2 = guerrillaManager.getPlayerSetsBlock().get(pname);
                if (b2) {
                    if (guerrillaManager.getPlayerGuerrilla(event.getPlayer()).addPaymentChest(chest)) {
                        guerrillaManager.getPlayerSetsBlock().remove(event.getPlayer().getName());
                        event.getPlayer().sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY + "Payment chest set");
                    } else {
                        guerrillaManager.getPlayerSetsBlock().remove(event.getPlayer().getName());
                        event.getPlayer().sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY + "That chest is already set");
                    }
                } else {
                    if (guerrillaManager.getPlayerGuerrilla(event.getPlayer()).removePaymentChest(chest)) {
                        guerrillaManager.getPlayerSetsBlock().remove(event.getPlayer().getName());
                        event.getPlayer().sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY + "Payment chest removed");
                    } else {
                        guerrillaManager.getPlayerSetsBlock().remove(event.getPlayer().getName());
                        event.getPlayer().sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY + "That is not a payment chest");
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

    public void setGuerrillaManager(GuerrillaManager guerrillaManager) {
        this.guerrillaManager = guerrillaManager;
    }
}
