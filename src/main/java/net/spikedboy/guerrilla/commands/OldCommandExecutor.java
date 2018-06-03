package net.spikedboy.guerrilla.commands;

import com.google.inject.Inject;
import net.spikedboy.guerrilla.Guerrilla;
import net.spikedboy.guerrilla.GuerrillaPlugin;
import net.spikedboy.guerrilla.configuration.GuerrillaConfigurations;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;

public class OldCommandExecutor implements CommandExecutor {

    private GuerrillaPlugin guerrillaPlugin;

    @Override
    public boolean processCommands(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        return processCommandsWithFuckedUpIfs(sender, cmd, commandLabel, args);
    }

    private boolean processCommandsWithFuckedUpIfs(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (!(sender instanceof Player)) {
            if (cmd.getLabel().equalsIgnoreCase("gmakemap")) {
                try {
                    Guerrilla.printmap();
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
        Guerrilla guerrilla = Guerrilla.getPlayerGuerrilla(playurd);
        Chunk chunk = playurd.getLocation().getBlock().getChunk();

        if (!playurd.getWorld().equals(GuerrillaPlugin.serverInstance.getWorld(GuerrillaConfigurations.gworldname))) {
            playurd.sendMessage(GuerrillaPlugin.gCh + "You can't do that in this world");
            return true;
        }
        if (GuerrillaPlugin.stateWon == true) {
            playurd.sendMessage(GuerrillaPlugin.gCh + "This round has ended, wait for an admin to start a new match! The winner was the "
                    + GuerrillaPlugin.gwinnerName + "'s guerrillaPlugin");
            return true;
        }

        if (commandLabel.equalsIgnoreCase("gc")) {
            if (guerrilla != null) {
                if (args.length < 1) {
                    if (GuerrillaPlugin.togglePlayerChat.get(playurd.getName()) == null || GuerrillaPlugin.togglePlayerChat.get(playurd.getName()) == false) {
                        GuerrillaPlugin.togglePlayerChat.put(playurd.getName(), true);
                        playurd.sendMessage(GuerrillaPlugin.gCh + "GuerrillaPlugin chat enabled");
                    } else if (GuerrillaPlugin.togglePlayerChat.get(playurd.getName()) == true) {
                        GuerrillaPlugin.togglePlayerChat.put(playurd.getName(), false);
                        playurd.sendMessage(GuerrillaPlugin.gCh + "GuerrillaPlugin chat disabled");
                    }
                    return true;
                } else {
                    return false;
                }
            } else {
                playurd.sendMessage(GuerrillaPlugin.gCh + "You have no guerrillaPlugin");
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
                        playurd.sendMessage(GuerrillaPlugin.gCh + "You have no GuerrillaPlugin");
                        return true;
                    }
                    //if false deletechest, if true addchest
                    if (!Guerrilla.isLeader(playurd)) {
                        playurd.sendMessage(GuerrillaPlugin.gCh + "You are not leader");
                        return true;
                    }
                    if (GuerrillaPlugin.playerSetsSafe.get(playurd.getName()) == null || GuerrillaPlugin.playerSetsSafe.get(playurd.getName()) == false) {
                        playurd.sendMessage(GuerrillaPlugin.gCh + "Setting safe chest");
                        GuerrillaPlugin.playerSetsSafe.put(playurd.getName(), Boolean.TRUE);
                        return true;
                    } else if (GuerrillaPlugin.playerSetsSafe.get(playurd.getName()) == true) {
                        playurd.sendMessage(GuerrillaPlugin.gCh + "Removing safe chest");
                        GuerrillaPlugin.playerSetsSafe.put(playurd.getName(), Boolean.FALSE);
                        return true;
                    }
                } else if ((args[0].equalsIgnoreCase("help"))) {
                    if (args.length == 2) {
                        if (args[1].equalsIgnoreCase("prices")) {

                            playurd.sendMessage(GuerrillaPlugin.gCh + GuerrillaConfigurations.chunkprice + " " + Material.getMaterial(GuerrillaConfigurations.itemid).toString()
                                    + " for each unclaimed chunk claim, and " + GuerrillaConfigurations.chunkprice * GuerrillaConfigurations.conqmulti
                                    + " for every conquest");

                            playurd.sendMessage(GuerrillaPlugin.gCh + "Maintenance price: " + GuerrillaConfigurations.chunkmaintprice + " "
                                    + Material.getMaterial(GuerrillaConfigurations.itemidmaint).toString() + " every " + GuerrillaConfigurations.nchunkpay
                                    + " chunks, with a max of: " + GuerrillaConfigurations.maintmaxprice);
                            if (guerrilla != null) {
                                int price = guerrilla.territories.size() / GuerrillaConfigurations.nchunkpay * GuerrillaConfigurations.chunkmaintprice;
                                playurd.sendMessage(GuerrillaPlugin.gCh + "You are currently paying: " + price
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
                                playurd.sendMessage(GuerrillaPlugin.gCh + "This is the GuerrillaPlugin help :) Plugin made by DS");
                                playurd.sendMessage(GuerrillaPlugin.gCh + "COMMANDS: *you may also type /guerrillaPlugin instead of /g*");
                                playurd.sendMessage(GuerrillaPlugin.gCh + "/g disband - deletes your guerrillaPlugin (only leader)");
                                playurd.sendMessage(GuerrillaPlugin.gCh + "/g claim - claims the chunk you are standing on");
                                playurd.sendMessage(GuerrillaPlugin.gCh + "/g join <name> - joins the guerrillaPlugin you have been invited to");
                                playurd.sendMessage(GuerrillaPlugin.gCh + "/g invite <player> - invites the player to your guerrillaPlugin");
                                playurd.sendMessage(GuerrillaPlugin.gCh + "/g kick <player> - kicks a player");
                                return true;

                            }
                            case 2: {
                                playurd.sendMessage(ChatColor.DARK_RED + "[Page 2/4]");
                                playurd.sendMessage(GuerrillaPlugin.gCh + "/g unclaim - unclaims the chunk you are standing on");
                                playurd.sendMessage(GuerrillaPlugin.gCh + "/g unclaimall - unclaims all the chunks (leaders only)");
                                playurd.sendMessage(GuerrillaPlugin.gCh + "/g list [page]- lists guerrillas");
                                playurd.sendMessage(GuerrillaPlugin.gCh + "/g who [guerrillaPlugin] - gives guerrillaPlugin info");
                                playurd.sendMessage(GuerrillaPlugin.gCh + "/g pchestset - sets payment chest (then open it)");
                                playurd.sendMessage(GuerrillaPlugin.gCh + "/g pchestremove - removes payment chest (then open it)");
                                return true;
                            }
                            case 3: {
                                playurd.sendMessage(ChatColor.DARK_RED + "[Page 3/4]");
                                playurd.sendMessage(GuerrillaPlugin.gCh + "/g leave - leaves the guerrillaPlugin (not leaders)");
                                playurd.sendMessage(GuerrillaPlugin.gCh + "/g invitec <player> - cancel the invite for a player you've invited");
                                playurd.sendMessage(GuerrillaPlugin.gCh + "/g decline - cancels a invitation you've been send");
                                playurd.sendMessage(GuerrillaPlugin.gCh + "/gc - toggles intern guerrillaPlugin chat");
                                playurd.sendMessage(GuerrillaPlugin.gCh + "/g help prices - see current GuerrillaPlugin prices");
                                playurd.sendMessage(GuerrillaPlugin.gCh + "/g safec (leader only) - sets a safe chest only you can open or destroy (toggles set/remove)");
                                return true;
                            }
                            case 4: {
                                playurd.sendMessage(ChatColor.DARK_RED + "[Page 4/4]");
                                playurd.sendMessage(GuerrillaPlugin.gCh + "/g changeleader [playername] - Changes the guerrillaPlugin leader");
                                return true;
                            }
                        }
                    } else if (args.length == 1) {
                        playurd.sendMessage(ChatColor.DARK_RED + "[Page 1/4]");
                        playurd.sendMessage(GuerrillaPlugin.gCh + "This is the GuerrillaPlugin help :) Plugin made by DS");
                        playurd.sendMessage(GuerrillaPlugin.gCh + "COMMANDS: *you may also type /guerrillaPlugin instead of /g*");
                        playurd.sendMessage(GuerrillaPlugin.gCh + "/g disband - deletes your guerrillaPlugin (only leader)");
                        playurd.sendMessage(GuerrillaPlugin.gCh + "/g claim - claims the chunk you are standing on");
                        playurd.sendMessage(GuerrillaPlugin.gCh + "/g join <name> - joins the guerrillaPlugin you have been invited to");
                        playurd.sendMessage(GuerrillaPlugin.gCh + "/g invite <player> - invites the player to your guerrillaPlugin");
                        playurd.sendMessage(GuerrillaPlugin.gCh + "/g kick <player> - kicks a player");
                        return true;
                    } else {
                        playurd.sendMessage(GuerrillaPlugin.gCh + "type /guerrillaPlugin help [page] for more info");
                        return true;
                    }
                } else if ((args[0].equalsIgnoreCase("pchestset"))) {
                    if (guerrilla.getLeader().equals(playurd.getName())) {
                        GuerrillaPlugin.playerSetsBlock.put(playurd.getName(), new Boolean(true));
                        sender.sendMessage(GuerrillaPlugin.gCh + "Payment chest waiting to be set, please open it");
                        return true;
                    }
                    sender.sendMessage(GuerrillaPlugin.gCh + "You are not leader");
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
                        Guerrilla guerrillz = Guerrilla.getGuerrillaByName(args[1]);
                        guerrillz.leader = args[2];
                        guerrillz.players.add(playurd.getName());
                        if (guerrilla != null) guerrilla.players.remove(playurd.getName());
                        sender.sendMessage(" Leader: " + guerrillz.leader);
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
                        Guerrilla.setSafeChunk(chunk);
                        playurd.sendMessage(GuerrillaPlugin.gCh + "Set");
                        return true;
                    }
                } else if ((args[0].equalsIgnoreCase("adminremovesafechunk"))) {
                    if (playurd.isOp()) {
                        Guerrilla.removeSafeChunk(chunk);
                        playurd.sendMessage(GuerrillaPlugin.gCh + "Removed");
                        return true;
                    }
                } else if ((args[0].equalsIgnoreCase("pchestremove"))) {
                    if (guerrilla.getLeader().equals(playurd.getName())) {
                        GuerrillaPlugin.playerSetsBlock.put(sender.getName(), new Boolean(false));
                        sender.sendMessage(GuerrillaPlugin.gCh + "Payment chest waiting to be removed, please open it");
                        return true;
                    }
                    sender.sendMessage(GuerrillaPlugin.gCh + "You are not leader");
                    return true;
                } else if ((args[0].equalsIgnoreCase("who"))) {
                    if (args.length == 1) {
                        Guerrilla.who((Player) sender, null);
                        return true;
                    } else if (args.length == 2) {
                        Guerrilla.who((Player) sender, args[1]);
                        return true;
                    } else {
                        return false;
                    }
                } else if ((args[0].equalsIgnoreCase("unclaimall"))) {
                    if (guerrilla != null) {
                        guerrilla.unclaimall((Player) sender);
                        return true;
                    }
                    sender.sendMessage(GuerrillaPlugin.gCh + "You have no guerrillaPlugin");
                    return true;
                } else if ((args[0].equalsIgnoreCase("kick"))) {
                    if (args.length == 2) {
                        if (guerrilla != null) {
                            guerrilla.kick(args[1], playurd);
                            return true;
                        } else {
                            sender.sendMessage(GuerrillaPlugin.gCh + "Can't do that! You must have a guerrillaPlugin");
                            return true;
                        }
                    }
                    sender.sendMessage("/guerrillaPlugin kick [player]");
                    return true;
                } else if ((args[0].equalsIgnoreCase("leave"))) {
                    Guerrilla.leave((Player) sender);
                    return true;
                } else if ((args[0].equalsIgnoreCase("list"))) {
                    if (args.length == 1) {
                        Guerrilla.List((Player) sender, 1);
                        return true;
                    } else if (args.length == 2) {
                        int page;
                        try {
                            page = Integer.parseInt(args[1]);
                        } catch (NumberFormatException e) {
                            return false;
                        }
                        Guerrilla.List(playurd, page);
                        return true;
                    }
                    return false;
                } else if ((args[0].equalsIgnoreCase("invite"))) {
                    if (!Guerrilla.getPlayerGuerrilla(playurd).getLeader().equals(playurd.getName())) {
                        playurd.sendMessage(GuerrillaPlugin.gCh + "You are not leader");
                        return true;
                    }
                    if (args.length == 2) {
                        Player player = guerrillaPlugin.getServer().getPlayer(args[1]);
                        if (player != null) {
                            guerrilla.invite(player, sender);
                            return true;
                        }
                        sender.sendMessage(GuerrillaPlugin.gCh + "Player not found");
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
                            sender.sendMessage(GuerrillaPlugin.gCh + "You can't do that");
                            return true;
                        }
                        sender.sendMessage(GuerrillaPlugin.gCh + "Player not found");
                        return true;
                    }
                } else if ((args[0].equalsIgnoreCase("decline"))) {
                    if (args.length == 2) {
                        Guerrilla.inviteDecline((Player) sender, args[1]);
                        return true;
                    }
                    sender.sendMessage(GuerrillaPlugin.gCh + "/g decline [name]");
                    return true;
                } else if ((args[0].equalsIgnoreCase("join"))) {
                    if (args.length == 2) {
                        Guerrilla guerrillan = Guerrilla.getGuerrillaByName(args[1]);
                        if (guerrillan != null) {
                            guerrillan.join((Player) sender);
                            return true;
                        }
                        sender.sendMessage(GuerrillaPlugin.gCh + "That guerrillaPlugin doesn't exist");
                        return true;

                    }
                    sender.sendMessage(GuerrillaPlugin.gCh + "/guerrillaPlugin join name");
                    return true;
                } else if ((args[0].equalsIgnoreCase("claim"))) {
                    Player player = (Player) sender;
                    if (guerrilla != null) {
                        guerrilla.claim(chunk, player);
                        return true;
                    }
                    player.sendMessage(GuerrillaPlugin.gCh + "You have no guerrillaPlugin");
                    return true;
                } else if ((args[0].equalsIgnoreCase("unclaim"))) {
                    Player player = (Player) sender;
                    if (guerrilla != null) {
                        guerrilla.unclaim(chunk, player);
                        return true;
                    }
                    player.sendMessage(GuerrillaPlugin.gCh + "You can't do that");
                    return true;
                } else if ((args[0].equalsIgnoreCase("create"))) {
                    if (args.length == 2) {
                        if (Guerrilla.getPlayerGuerrilla((Player) sender) == null) {
                            if (Guerrilla.getGuerrillaByName(args[1]) != null) {
                                sender.sendMessage(GuerrillaPlugin.gCh + "That guerrillaPlugin already exists! Please choose another name");
                                return true;
                            }
                            if (args[1].length() > 10) {
                                playurd.sendMessage(GuerrillaPlugin.gCh + "The name is too long, max. 10 characters");
                                return true;
                            }
                            Guerrilla guerrillan = new Guerrilla((Player) sender, args[1]);
                            Guerrilla.guerrillaList.add(guerrillan);
                            //sender.sendMessage(gCh + "You created the "+ guerrillaPlugin.getName() +" guerrillaPlugin");
                            return true;
                        }
                        sender.sendMessage(GuerrillaPlugin.gCh + "You have already joined a guerrillaPlugin");
                        return true;

                    }
                    sender.sendMessage(GuerrillaPlugin.gCh + "/guerrillaPlugin create [guerrillaPlugin name]");
                    return true;
                } else if (args[0].equalsIgnoreCase("disband")) {
                    if (args.length == 2) {
                        Player player = (Player) sender;
                        if ((Guerrilla.getGuerrillaByName(args[1])) == (guerrilla)) {
                            guerrilla.disband(player);
                            return true;
                        }
                        sender.sendMessage(GuerrillaPlugin.gCh + "You can't disband other guerrillaPlugin than your own!");
                        return true;
                    }
                    sender.sendMessage(GuerrillaPlugin.gCh + "Are you sure? type /guerrillaPlugin disband [yourguerrillaname] to disband");
                    return true;
                }

            }
        }
        return false;
    }

    @Inject
    public void setGuerrillaPlugin(GuerrillaPlugin guerrillaPlugin) {
        this.guerrillaPlugin = guerrillaPlugin;
    }
}
