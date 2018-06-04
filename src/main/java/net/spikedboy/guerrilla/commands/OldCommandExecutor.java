package net.spikedboy.guerrilla.commands;

import com.google.inject.Inject;
import com.google.inject.Injector;
import net.spikedboy.guerrilla.GuerrillaPlugin;
import net.spikedboy.guerrilla.configuration.GuerrillaConfigurations;
import net.spikedboy.guerrilla.guerrilla.Guerrilla;
import net.spikedboy.guerrilla.guerrilla.GuerrillaManager;
import net.spikedboy.guerrilla.guerrilla.Messager;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;

public class OldCommandExecutor implements CommandExecutor {

    @Inject
    private GuerrillaPlugin guerrillaPlugin;

    @Inject
    private GuerrillaManager guerrillaManager;

    @Inject
    private Injector injector;

    @Override
    public boolean processCommands(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        return processCommandsWithFuckedUpIfs(sender, cmd, commandLabel, args);
    }

    private boolean processCommandsWithFuckedUpIfs(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (!(sender instanceof Player)) {
            if (cmd.getLabel().equalsIgnoreCase("gmakemap")) {
                try {
                    guerrillaManager.printmap();
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                    return true;
                }
            }
            return true;
        }

        //definitions
        Player playurd = (Player) sender;
        Guerrilla guerrilla = guerrillaManager.getPlayerGuerrilla(playurd);
        Chunk chunk = playurd.getLocation().getBlock().getChunk();

        if (!playurd.getWorld().equals(guerrillaPlugin.getServer().getWorld(GuerrillaConfigurations.gworldname))) {
            playurd.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "You can't do that in this world");
            return true;
        }
        if (GuerrillaManager.isStateWon() == true) {
            playurd.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "This round has ended, wait for an admin to start a new match! The winner was the "
                    + GuerrillaManager.getWinnerGuerrillaName() + "'s guerrillaPlugin");
            return true;
        }

        if (commandLabel.equalsIgnoreCase("gc")) {
            if (guerrilla != null) {
                if (args.length < 1) {
                    if (guerrillaManager.getTogglePlayerChat().get(playurd.getName()) == null || guerrillaManager.getTogglePlayerChat().get(playurd.getName()) == false) {
                        guerrillaManager.getTogglePlayerChat().put(playurd.getName(), true);
                        playurd.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "GuerrillaPlugin chat enabled");
                    } else if (guerrillaManager.getTogglePlayerChat().get(playurd.getName()) == true) {
                        guerrillaManager.getTogglePlayerChat().put(playurd.getName(), false);
                        playurd.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "GuerrillaPlugin chat disabled");
                    }
                    return true;
                } else {
                    return false;
                }
            } else {
                playurd.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "You have no guerrillaPlugin");
                return true;
            }

        }

        if ((commandLabel.equalsIgnoreCase("g")) || (commandLabel.equalsIgnoreCase("guerrillaPlugin"))) {

            if (args.length >= 1) {
                if ((args[0].equalsIgnoreCase("changeleader"))) {
                    if (args.length == 2) {
                        String nleader = args[1];
                        guerrilla.changeLeader(playurd, nleader);
                        return true;
                    } else {
                        return false;
                    }
                } else if ((args[0].equalsIgnoreCase("safec"))) {
                    if (guerrilla == null) {
                        playurd.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "You have no GuerrillaPlugin");
                        return true;
                    }
                    //if false deletechest, if true addchest
                    if (!guerrillaManager.isLeader(playurd)) {
                        playurd.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "You are not leader");
                        return true;
                    }
                    if (guerrillaManager.getPlayerSetsSafe().get(playurd.getName()) == null || guerrillaManager.getPlayerSetsSafe().get(playurd.getName()) == false) {
                        playurd.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "Setting safe chest");
                        guerrillaManager.getPlayerSetsSafe().put(playurd.getName(), Boolean.TRUE);
                        return true;
                    } else if (guerrillaManager.getPlayerSetsSafe().get(playurd.getName()) == true) {
                        playurd.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "Removing safe chest");
                        guerrillaManager.getPlayerSetsSafe().put(playurd.getName(), Boolean.FALSE);
                        return true;
                    }
                } else if ((args[0].equalsIgnoreCase("help"))) {
                    if (args.length == 2) {
                        if (args[1].equalsIgnoreCase("prices")) {

                            playurd.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + GuerrillaConfigurations.chunkprice + " " + Material.getMaterial(GuerrillaConfigurations.itemid).toString()
                                    + " for each unclaimed chunk claim, and " + GuerrillaConfigurations.chunkprice * GuerrillaConfigurations.conqmulti
                                    + " for every conquest");

                            playurd.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "Maintenance price: " + GuerrillaConfigurations.chunkmaintprice + " "
                                    + Material.getMaterial(GuerrillaConfigurations.itemidmaint).toString() + " every " + GuerrillaConfigurations.nchunkpay
                                    + " chunks, with a max of: " + GuerrillaConfigurations.maintmaxprice);
                            if (guerrilla != null) {
                                int price = guerrilla.getTerritories().size() / GuerrillaConfigurations.nchunkpay * GuerrillaConfigurations.chunkmaintprice;
                                playurd.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "You are currently paying: " + price
                                        + " per minecraft day. \nEach real day: " + (price * 72));
                            }
                            return true;
                        }
                        int pnumber = 0;
                        try {
                            pnumber = Integer.parseInt(args[1]);
                        } catch (NumberFormatException e) {
                            return false;
                        }
                        switch (pnumber) {
                            case 1: {
                                playurd.sendMessage(ChatColor.DARK_RED + "[Page 1/4]");
                                playurd.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "This is the GuerrillaPlugin help :) Plugin made by DS");
                                playurd.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "COMMANDS: *you may also type /guerrillaPlugin instead of /g*");
                                playurd.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "/g disband - deletes your guerrillaPlugin (only leader)");
                                playurd.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "/g claim - claims the chunk you are standing on");
                                playurd.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "/g join <name> - joins the guerrillaPlugin you have been invited to");
                                playurd.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "/g invite <player> - invites the player to your guerrillaPlugin");
                                playurd.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "/g kick <player> - kicks a player");
                                return true;

                            }
                            case 2: {
                                playurd.sendMessage(ChatColor.DARK_RED + "[Page 2/4]");
                                playurd.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "/g unclaim - unclaims the chunk you are standing on");
                                playurd.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "/g unclaimall - unclaims all the chunks (leaders only)");
                                playurd.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "/g list [page]- lists guerrillas");
                                playurd.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "/g who [guerrillaPlugin] - gives guerrillaPlugin info");
                                playurd.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "/g pchestset - sets payment chest (then open it)");
                                playurd.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "/g pchestremove - removes payment chest (then open it)");
                                return true;
                            }
                            case 3: {
                                playurd.sendMessage(ChatColor.DARK_RED + "[Page 3/4]");
                                playurd.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "/g leave - leaves the guerrillaPlugin (not leaders)");
                                playurd.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "/g invitec <player> - cancel the invite for a player you've invited");
                                playurd.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "/g decline - cancels a invitation you've been send");
                                playurd.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "/gc - toggles intern guerrillaPlugin chat");
                                playurd.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "/g help prices - see current GuerrillaPlugin prices");
                                playurd.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "/g safec (leader only) - sets a safe chest only you can open or destroy (toggles set/remove)");
                                return true;
                            }
                            case 4: {
                                playurd.sendMessage(ChatColor.DARK_RED + "[Page 4/4]");
                                playurd.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "/g changeleader [playername] - Changes the guerrillaPlugin leader");
                                return true;
                            }
                        }
                    } else if (args.length == 1) {
                        playurd.sendMessage(ChatColor.DARK_RED + "[Page 1/4]");
                        playurd.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "This is the GuerrillaPlugin help :) Plugin made by DS");
                        playurd.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "COMMANDS: *you may also type /guerrillaPlugin instead of /g*");
                        playurd.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "/g disband - deletes your guerrillaPlugin (only leader)");
                        playurd.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "/g claim - claims the chunk you are standing on");
                        playurd.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "/g join <name> - joins the guerrillaPlugin you have been invited to");
                        playurd.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "/g invite <player> - invites the player to your guerrillaPlugin");
                        playurd.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "/g kick <player> - kicks a player");
                        return true;
                    } else {
                        playurd.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "type /guerrillaPlugin help [page] for more info");
                        return true;
                    }
                } else if ((args[0].equalsIgnoreCase("pchestset"))) {
                    if (guerrilla.getLeader().equals(playurd.getName())) {
                        guerrillaManager.getPlayerSetsBlock().put(playurd.getName(), new Boolean(true));
                        sender.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "Payment chest waiting to be set, please open it");
                        return true;
                    }
                    sender.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "You are not leader");
                    return true;
                } else if ((args[0].equalsIgnoreCase("adminsetatkbonus"))) {
                    if (playurd.isOp()) {
                        guerrillaPlugin.getConfig().set("guerrillaPlugin.defenderdamagedealtmultiplier", Integer.parseInt(args[1]));
                        guerrillaPlugin.saveConfig();
                        GuerrillaConfigurations.atkbonus = guerrillaPlugin.getConfig().getInt("guerrillaPlugin.defenderdamagedealtmultiplier", 1);
                        playurd.sendMessage(String.valueOf(GuerrillaConfigurations.atkbonus));
                        return true;
                    } else {
                        return false;
                    }
                } else if ((args[0].equalsIgnoreCase("adminsetleader"))) {
                    if (playurd.isOp()) {
                        Guerrilla guerrillz = guerrillaManager.getGuerrillaByName(args[1]);
                        guerrillz.setLeader(args[2]);
                        guerrillz.getPlayers().add(playurd.getName());
                        if (guerrilla != null) guerrilla.getPlayers().remove(playurd.getName());
                        sender.sendMessage(" Leader: " + guerrillz.getLeader());
                        return true;
                    }
                } else if ((args[0].equalsIgnoreCase("setnminmaintprice"))) {
                    if (playurd.isOp()) {
                        guerrillaPlugin.getConfig().set("guerrillaPlugin.nchunksminmaintenance", Integer.parseInt(args[1]));
                        guerrillaPlugin.saveConfig();
                        GuerrillaConfigurations.nchunkpay = guerrillaPlugin.getConfig().getInt("guerrillaPlugin.nchunksminmaintenance", 4);
                        playurd.sendMessage("" + GuerrillaConfigurations.nchunkpay);
                        return true;
                    }
                    return false;
                } else if ((args[0].equalsIgnoreCase("adminsetsafechunk"))) {
                    if (playurd.isOp()) {
                        guerrillaManager.setSafeChunk(chunk);
                        playurd.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "Set");
                        return true;
                    }
                } else if ((args[0].equalsIgnoreCase("adminremovesafechunk"))) {
                    if (playurd.isOp()) {
                        guerrillaManager.removeSafeChunk(chunk);
                        playurd.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "Removed");
                        return true;
                    }
                } else if ((args[0].equalsIgnoreCase("pchestremove"))) {
                    if (guerrilla.getLeader().equals(playurd.getName())) {
                        guerrillaManager.getPlayerSetsBlock().put(sender.getName(), new Boolean(false));
                        sender.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "Payment chest waiting to be removed, please open it");
                        return true;
                    }
                    sender.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "You are not leader");
                    return true;
                } else if ((args[0].equalsIgnoreCase("who"))) {
                    if (args.length == 1) {
                        guerrillaManager.who((Player) sender, null);
                        return true;
                    } else if (args.length == 2) {
                        guerrillaManager.who((Player) sender, args[1]);
                        return true;
                    } else {
                        return false;
                    }
                } else if ((args[0].equalsIgnoreCase("unclaimall"))) {
                    if (guerrilla != null) {
                        guerrilla.unclaimall((Player) sender);
                        return true;
                    }
                    sender.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "You have no guerrillaPlugin");
                    return true;
                } else if ((args[0].equalsIgnoreCase("kick"))) {
                    if (args.length == 2) {
                        if (guerrilla != null) {
                            guerrilla.kick(args[1], playurd);
                            return true;
                        } else {
                            sender.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "Can't do that! You must have a guerrillaPlugin");
                            return true;
                        }
                    }
                    sender.sendMessage("/guerrillaPlugin kick [player]");
                    return true;
                } else if ((args[0].equalsIgnoreCase("leave"))) {
                    guerrillaManager.leave((Player) sender);
                    return true;
                } else if ((args[0].equalsIgnoreCase("list"))) {
                    if (args.length == 1) {
                        guerrillaManager.List((Player) sender, 1);
                        return true;
                    } else if (args.length == 2) {
                        int page;
                        try {
                            page = Integer.parseInt(args[1]);
                        } catch (NumberFormatException e) {
                            return false;
                        }
                        guerrillaManager.List(playurd, page);
                        return true;
                    }
                    return false;
                } else if ((args[0].equalsIgnoreCase("invite"))) {
                    if (!guerrillaManager.getPlayerGuerrilla(playurd).getLeader().equals(playurd.getName())) {
                        playurd.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "You are not leader");
                        return true;
                    }
                    if (args.length == 2) {
                        Player player = guerrillaPlugin.getServer().getPlayer(args[1]);
                        if (player != null) {
                            guerrilla.invite(player, sender);
                            return true;
                        }
                        sender.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "Player not found");
                        return true;
                    }
                    sender.sendMessage("/guerrillaPlugin invite [playername]");
                    return true;
                } else if ((args[0].equalsIgnoreCase("invitec"))) {
                    if (args.length == 2) {
                        Player player = guerrillaPlugin.getServer().getPlayer(args[1]);
                        if (player != null) {
                            if (guerrilla != null) {
                                guerrilla.inviteCancel(player, (Player) sender);
                                return true;
                            }
                            sender.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "You can't do that");
                            return true;
                        }
                        sender.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "Player not found");
                        return true;
                    }
                } else if ((args[0].equalsIgnoreCase("decline"))) {
                    if (args.length == 2) {
                        guerrillaManager.inviteDecline((Player) sender, args[1]);
                        return true;
                    }
                    sender.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "/g decline [name]");
                    return true;
                } else if ((args[0].equalsIgnoreCase("join"))) {
                    if (args.length == 2) {
                        Guerrilla guerrillan = guerrillaManager.getGuerrillaByName(args[1]);
                        if (guerrillan != null) {
                            guerrillan.join((Player) sender);
                            return true;
                        }
                        sender.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "That guerrillaPlugin doesn't exist");
                        return true;

                    }
                    sender.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "/guerrillaPlugin join name");
                    return true;
                } else if ((args[0].equalsIgnoreCase("claim"))) {
                    Player player = (Player) sender;
                    if (guerrilla != null) {
                        guerrilla.claim(chunk, player);
                        return true;
                    }
                    player.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "You have no guerrillaPlugin");
                    return true;
                } else if ((args[0].equalsIgnoreCase("unclaim"))) {
                    Player player = (Player) sender;
                    if (guerrilla != null) {
                        guerrilla.unclaim(chunk, player);
                        return true;
                    }
                    player.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "You can't do that");
                    return true;
                } else if ((args[0].equalsIgnoreCase("create"))) {
                    if (args.length == 2) {
                        if (guerrillaManager.getPlayerGuerrilla((Player) sender) == null) {
                            if (guerrillaManager.getGuerrillaByName(args[1]) != null) {
                                sender.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "That guerrillaPlugin already exists! Please choose another name");
                                return true;
                            }
                            if (args[1].length() > 10) {
                                playurd.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "The name is too long, max. 10 characters");
                                return true;
                            }
//                            Guerrilla guerrillan = new Guerrilla((Player) sender, args[1]);
                            Guerrilla guerrillan = injector.getInstance(Guerrilla.class);
                            guerrillan.initiateNewGuerrilla((Player) sender, args[1]);
                            guerrillaManager.getGuerrillaList().add(guerrillan);
                            //sender.sendMessage(GUERRILLA_MESSAGE_PREFIX + "You created the "+ guerrillaPlugin.getName() +" guerrillaPlugin");
                            return true;
                        }
                        sender.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "You have already joined a guerrillaPlugin");
                        return true;

                    }
                    sender.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "/guerrillaPlugin create [guerrillaPlugin name]");
                    return true;
                } else if (args[0].equalsIgnoreCase("disband")) {
                    if (args.length == 2) {
                        Player player = (Player) sender;
                        if ((guerrillaManager.getGuerrillaByName(args[1])) == (guerrilla)) {
                            guerrilla.disband(player);
                            return true;
                        }
                        sender.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "You can't disband other guerrillaPlugin than your own!");
                        return true;
                    }
                    sender.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "Are you sure? type /guerrillaPlugin disband [yourguerrillaname] to disband");
                    return true;
                }

            }
        }
        return false;
    }

    public void setGuerrillaPlugin(GuerrillaPlugin guerrillaPlugin) {
        this.guerrillaPlugin = guerrillaPlugin;
    }

    public void setGuerrillaManager(GuerrillaManager guerrillaManager) {
        this.guerrillaManager = guerrillaManager;
    }
}
