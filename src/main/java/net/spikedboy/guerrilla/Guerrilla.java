package net.spikedboy.guerrilla;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Guerrilla extends JavaPlugin {

    public static Map<String, Boolean> PlayerSetsBlock = new HashMap<String, Boolean>();
    public static Map<String, Boolean> PlayerSetsSafe = new HashMap<String, Boolean>();
    public static DelayedClaimDataQueue delayedClaimDataQueue = new DelayedClaimDataQueue();
    public static Map<String, Boolean> TogglePlayerChat = new HashMap<String, Boolean>();
    public static Logger log = Logger.getLogger("Minecraft");
    public static Server sinst;
    public static long delay, initdelay, conqdelay;
    public static String gworldname, gwinnerName, gCh = ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY;
    public static Guerrilla ginst;
    public static boolean stateWon, tntProtection;
    public static int conqmulti, defbonus, atkbonus, nchunkpay, itemid, chunkprice, itemidmaint, chunkmaintprice, objectiveChunks, maintmaxprice, minPSC, expTime, paymentThreadId;

    public void onEnable() {
        //set a variable to send to GuerrillaG for the server instance
        if (this.getServer() != null && this != null) {
            sinst = this.getServer();
            ginst = this;
        } else {
            log.info("Server == null");
        }

        //getConfig() set up
        this.saveDefaultConfig();
        itemid = getConfig().getInt("guerrilla.itemid", 296);
        chunkprice = getConfig().getInt("guerrilla.chunkprice", 16);
        delay = (long) getConfig().getInt("guerrilla.paymenttime", 23999);
        itemidmaint = getConfig().getInt("guerrilla.iditemmaintennance", 296);
        chunkmaintprice = getConfig().getInt("guerrilla.chunkmaintennanceprice", 1);
        gworldname = getConfig().getString("guerrilla.gworldname", "world");
        log.info("[Guerrilla] Mundo: " + gworldname);
        conqdelay = (long) getConfig().getInt("guerrilla.conquestdelay", 6000);
        conqmulti = getConfig().getInt("guerrilla.conquestpricemultiplier", 2);
        defbonus = getConfig().getInt("guerrilla.defenderdamagetakendivider", 2);
        atkbonus = getConfig().getInt("guerrilla.defenderdamagedealtmultiplier", 1);
        nchunkpay = getConfig().getInt("guerrilla.nchunksminmaintenance", 10);
        stateWon = getConfig().getBoolean("guerrilla.ismatchwon", false);
        objectiveChunks = getConfig().getInt("guerrilla.chunkobjective", 780);
        maintmaxprice = getConfig().getInt("guerrilla.maxmaintenanceprice", 44);
        gwinnerName = getConfig().getString("guerrilla.winner", "");
        minPSC = getConfig().getInt("guerrilla.miniumplayersforasafechest", 5);
        expTime = getConfig().getInt("guerrilla.guerrillaexpirytime", 604800000);
        tntProtection = getConfig().getBoolean("guerrilla.explostionProtection", false);

        //check getConfig()

        if (sinst.getWorld(gworldname) == null) {
            log.info("[Guerrilla] El nombre del mapa especificado es erroneo! Cambialo y reinicia");
            return;
        }

        //get the first payment delay

        initdelay = delay - sinst.getWorld(gworldname).getTime();
        log.info("[Guerrilla] Time for the first delay: " + initdelay);

        //listeners and pluginmanager retrieving
        PluginManager pm = sinst.getPluginManager();
        pm.registerEvents(new GuerrillaBlockListener(this), this);
        pm.registerEvents(new GuerrillaEntityListener(this), this);
        pm.registerEvents(new GuerrillaPlayerListener(this), this);
        pm.registerEvents(new GuerrillaWorldListener(this), this);

        //load saved data
        try {
            GuerrillaG.load();
        } catch (FileNotFoundException e) {
            log.warning("[Guerrilla] some guerrilla file is missing, bad things might happen...");
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("[Guerrilla] Guerrilla loaded");

        //remove missing payment chests
        try {
            GuerrillaG.removeMChests();
        } catch (ConcurrentModificationException e) {
            e.printStackTrace();
        }

        //payment thread
        paymentThreadId = this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {
                if (GuerrillaG.checkWinners() != null) {
                    stateWon = true;
                    gwinnerName = GuerrillaG.checkWinners().getName();
                    getConfig().set("guerrilla.ismatchwon", true);
                    getConfig().set("guerrilla.winner", gwinnerName);
                    ginst.saveConfig();
                    sinst.broadcastMessage(ChatColor.GOLD + "[GuerrillaWrittenInGold] " + gwinnerName + " HAS WON THIS MATCH!!!!!! GOOD DAY TO YOU");
                    return;
                }
                GuerrillaG.ChargueMaintenance();
                try {
                    GuerrillaG.printmap();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, initdelay, delay);
    }

    public void onDisable() {
        sinst.getScheduler().cancelTask(paymentThreadId);
        try {
            GuerrillaG.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
        GuerrillaG.removeMChests();
        log.info("[Guerrilla] Guerrilla disabled");
        this.saveConfig();
    }

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {


        if (!(sender instanceof Player)) {
            if (cmd.getLabel().equalsIgnoreCase("gmakemap")) {
                try {
                    GuerrillaG.printmap();
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
        GuerrillaG guerrilla = GuerrillaG.getPlayerGuerrilla(playurd);
        Chunk chunk = playurd.getLocation().getBlock().getChunk();

        if (!playurd.getWorld().equals(sinst.getWorld(gworldname))) {
            playurd.sendMessage(gCh + "You can't do that in this world");
            return true;
        }
        if (stateWon == true) {
            playurd.sendMessage(gCh + "This round has ended, wait for an admin to start a new match! The winner was the "
                    + gwinnerName + "'s guerrilla");
            return true;
        }

        if (commandLabel.equalsIgnoreCase("gc")) {
            if (guerrilla != null) {
                if (args.length < 1) {
                    if (TogglePlayerChat.get(playurd.getName()) == null || TogglePlayerChat.get(playurd.getName()) == false) {
                        TogglePlayerChat.put(playurd.getName(), true);
                        playurd.sendMessage(gCh + "Guerrilla chat enabled");
                    } else if (TogglePlayerChat.get(playurd.getName()) == true) {
                        TogglePlayerChat.put(playurd.getName(), false);
                        playurd.sendMessage(gCh + "Guerrilla chat disabled");
                    }
                    return true;
                } else {
                    return false;
                }
            } else {
                playurd.sendMessage(gCh + "You have no guerrilla");
                return true;
            }

        }

        if ((commandLabel.equalsIgnoreCase("g")) || (commandLabel.equalsIgnoreCase("guerrilla"))) {

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
                        playurd.sendMessage(gCh + "You have no Guerrilla");
                        return true;
                    }
                    //if false deletechest, if true addchest
                    if (!GuerrillaG.isLeader(playurd)) {
                        playurd.sendMessage(gCh + "You are not leader");
                        return true;
                    }
                    if (PlayerSetsSafe.get(playurd.getName()) == null || PlayerSetsSafe.get(playurd.getName()) == false) {
                        playurd.sendMessage(gCh + "Setting safe chest");
                        PlayerSetsSafe.put(playurd.getName(), Boolean.TRUE);
                        return true;
                    } else if (PlayerSetsSafe.get(playurd.getName()) == true) {
                        playurd.sendMessage(gCh + "Removing safe chest");
                        PlayerSetsSafe.put(playurd.getName(), Boolean.FALSE);
                        return true;
                    }
                } else if ((args[0].equalsIgnoreCase("help"))) {
                    if (args.length == 2) {
                        if (args[1].equalsIgnoreCase("prices")) {

                            playurd.sendMessage(gCh + chunkprice + " " + Material.getMaterial(itemid).toString()
                                    + " for each unclaimed chunk claim, and " + chunkprice * conqmulti
                                    + " for every conquest");

                            playurd.sendMessage(gCh + "Maintenance price: " + chunkmaintprice + " "
                                    + Material.getMaterial(itemidmaint).toString() + " every " + nchunkpay
                                    + " chunks, with a max of: " + maintmaxprice);
                            if (guerrilla != null) {
                                int price = (int) (guerrilla.Territories.size() / Guerrilla.nchunkpay) * Guerrilla.chunkmaintprice;
                                playurd.sendMessage(gCh + "You are currently paying: " + price
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
                                playurd.sendMessage(gCh + "This is the Guerrilla help :) Plugin made by DS");
                                playurd.sendMessage(gCh + "COMMANDS: *you may also type /guerrilla instead of /g*");
                                playurd.sendMessage(gCh + "/g disband - deletes your guerrilla (only leader)");
                                playurd.sendMessage(gCh + "/g claim - claims the chunk you are standing on");
                                playurd.sendMessage(gCh + "/g join <name> - joins the guerrilla you have been invited to");
                                playurd.sendMessage(gCh + "/g invite <player> - invites the player to your guerrilla");
                                playurd.sendMessage(gCh + "/g kick <player> - kicks a player");
                                return true;

                            }
                            case 2: {
                                playurd.sendMessage(ChatColor.DARK_RED + "[Page 2/4]");
                                playurd.sendMessage(gCh + "/g unclaim - unclaims the chunk you are standing on");
                                playurd.sendMessage(gCh + "/g unclaimall - unclaims all the chunks (leaders only)");
                                playurd.sendMessage(gCh + "/g list [page]- lists guerrillas");
                                playurd.sendMessage(gCh + "/g who [guerrilla] - gives guerrilla info");
                                playurd.sendMessage(gCh + "/g pchestset - sets payment chest (then open it)");
                                playurd.sendMessage(gCh + "/g pchestremove - removes payment chest (then open it)");
                                return true;
                            }
                            case 3: {
                                playurd.sendMessage(ChatColor.DARK_RED + "[Page 3/4]");
                                playurd.sendMessage(gCh + "/g leave - leaves the guerrilla (not leaders)");
                                playurd.sendMessage(gCh + "/g invitec <player> - cancel the invite for a player you've invited");
                                playurd.sendMessage(gCh + "/g decline - cancels a invitation you've been send");
                                playurd.sendMessage(gCh + "/gc - toggles intern guerrilla chat");
                                playurd.sendMessage(gCh + "/g help prices - see current Guerrilla prices");
                                playurd.sendMessage(gCh + "/g safec (leader only) - sets a safe chest only you can open or destroy (toggles set/remove)");
                                return true;
                            }
                            case 4: {
                                playurd.sendMessage(ChatColor.DARK_RED + "[Page 4/4]");
                                playurd.sendMessage(gCh + "/g changeleader [playername] - Changes the guerrilla leader");
                                return true;
                            }
                        }
                    } else if (args.length == 1) {
                        playurd.sendMessage(ChatColor.DARK_RED + "[Page 1/4]");
                        playurd.sendMessage(gCh + "This is the Guerrilla help :) Plugin made by DS");
                        playurd.sendMessage(gCh + "COMMANDS: *you may also type /guerrilla instead of /g*");
                        playurd.sendMessage(gCh + "/g disband - deletes your guerrilla (only leader)");
                        playurd.sendMessage(gCh + "/g claim - claims the chunk you are standing on");
                        playurd.sendMessage(gCh + "/g join <name> - joins the guerrilla you have been invited to");
                        playurd.sendMessage(gCh + "/g invite <player> - invites the player to your guerrilla");
                        playurd.sendMessage(gCh + "/g kick <player> - kicks a player");
                        return true;
                    } else {
                        playurd.sendMessage(gCh + "type /guerrilla help [page] for more info");
                        return true;
                    }
                } else if ((args[0].equalsIgnoreCase("pchestset"))) {
                    if (guerrilla.getLeader().equals(playurd.getName())) {
                        Guerrilla.PlayerSetsBlock.put(playurd.getName(), new Boolean(true));
                        sender.sendMessage(gCh + "Payment chest waiting to be set, please open it");
                        return true;
                    }
                    sender.sendMessage(gCh + "You are not leader");
                    return true;
                } else if ((args[0].equalsIgnoreCase("adminsetatkbonus"))) {
                    if (playurd.isOp()) {
                        getConfig().set("guerrilla.defenderdamagedealtmultiplier", Integer.parseInt(args[1]));
                        this.saveConfig();
                        atkbonus = getConfig().getInt("guerrilla.defenderdamagedealtmultiplier", 1);
                        playurd.sendMessage(String.valueOf(atkbonus));
                        return true;
                    } else {
                        return false;
                    }
                } else if ((args[0].equalsIgnoreCase("adminsetleader"))) {
                    if (playurd.isOp()) {
                        GuerrillaG guerrillz = GuerrillaG.getGuerrillaByName(args[1]);
                        guerrillz.leader = args[2];
                        guerrillz.Players.add(playurd.getName());
                        if (guerrilla != null) guerrilla.Players.remove(playurd.getName());
                        sender.sendMessage(" Leader: " + guerrillz.leader);
                        return true;
                    }
                } else if ((args[0].equalsIgnoreCase("setnminmaintprice"))) {
                    if (playurd.isOp()) {
                        getConfig().set("guerrilla.nchunksminmaintenance", Integer.parseInt(args[1]));
                        this.saveConfig();
                        nchunkpay = getConfig().getInt("guerrilla.nchunksminmaintenance", 4);
                        playurd.sendMessage("" + nchunkpay);
                        return true;
                    }
                    return false;
                } else if ((args[0].equalsIgnoreCase("adminsetsafechunk"))) {
                    if (playurd.isOp()) {
                        GuerrillaG.setSafeChunk(chunk);
                        playurd.sendMessage(gCh + "Set");
                        return true;
                    }
                } else if ((args[0].equalsIgnoreCase("adminremovesafechunk"))) {
                    if (playurd.isOp()) {
                        GuerrillaG.removeSafeChunk(chunk);
                        playurd.sendMessage(gCh + "Removed");
                        return true;
                    }
                } else if ((args[0].equalsIgnoreCase("pchestremove"))) {
                    if (guerrilla.getLeader().equals(playurd.getName())) {
                        Guerrilla.PlayerSetsBlock.put(sender.getName(), new Boolean(false));
                        sender.sendMessage(gCh + "Payment chest waiting to be removed, please open it");
                        return true;
                    }
                    sender.sendMessage(gCh + "You are not leader");
                    return true;
                } else if ((args[0].equalsIgnoreCase("who"))) {
                    if (args.length == 1) {
                        GuerrillaG.who((Player) sender, null);
                        return true;
                    } else if (args.length == 2) {
                        GuerrillaG.who((Player) sender, (String) args[1]);
                        return true;
                    } else {
                        return false;
                    }
                } else if ((args[0].equalsIgnoreCase("unclaimall"))) {
                    if (guerrilla != null) {
                        guerrilla.unclaimall((Player) sender);
                        return true;
                    }
                    sender.sendMessage(gCh + "You have no guerrilla");
                    return true;
                } else if ((args[0].equalsIgnoreCase("kick"))) {
                    if (args.length == 2) {
                        if (guerrilla != null) {
                            guerrilla.kick((String) args[1], playurd);
                            return true;
                        } else {
                            sender.sendMessage(gCh + "Can't do that! You must have a guerrilla");
                            return true;
                        }
                    }
                    sender.sendMessage("/guerrilla kick [player]");
                    return true;
                } else if ((args[0].equalsIgnoreCase("leave"))) {
                    GuerrillaG.leave((Player) sender);
                    return true;
                } else if ((args[0].equalsIgnoreCase("list"))) {
                    if (args.length == 1) {
                        GuerrillaG.List((Player) sender, 1);
                        return true;
                    } else if (args.length == 2) {
                        int page;
                        try {
                            page = Integer.parseInt(args[1]);
                        } catch (NumberFormatException e) {
                            return false;
                        }
                        GuerrillaG.List(playurd, page);
                        return true;
                    }
                    return false;
                } else if ((args[0].equalsIgnoreCase("invite"))) {
                    if (!GuerrillaG.getPlayerGuerrilla(playurd).getLeader().equals(playurd.getName())) {
                        playurd.sendMessage(gCh + "You are not leader");
                        return true;
                    }
                    if (args.length == 2) {
                        Player player = getServer().getPlayer((String) args[1]);
                        if (player != null) {
                            guerrilla.invite(player, sender);
                            return true;
                        }
                        sender.sendMessage(gCh + "Player not found");
                        return true;
                    }
                    sender.sendMessage("/guerrilla invite [playername]");
                    return true;
                } else if ((args[0].equalsIgnoreCase("invitec"))) {
                    if (args.length == 2) {
                        Player player = getServer().getPlayer((String) args[1]);
                        if (player != null) {
                            if (guerrilla != null) {
                                guerrilla.inviteCancel(player, (Player) sender);
                                return true;
                            }
                            sender.sendMessage(gCh + "You can't do that");
                            return true;
                        }
                        sender.sendMessage(gCh + "Player not found");
                        return true;
                    }
                } else if ((args[0].equalsIgnoreCase("decline"))) {
                    if (args.length == 2) {
                        GuerrillaG.inviteDecline((Player) sender, (String) args[1]);
                        return true;
                    }
                    sender.sendMessage(gCh + "/g decline [name]");
                    return true;
                } else if ((args[0].equalsIgnoreCase("join"))) {
                    if (args.length == 2) {
                        GuerrillaG guerrillan = GuerrillaG.getGuerrillaByName((String) args[1]);
                        if (guerrillan != null) {
                            guerrillan.join((Player) sender);
                            return true;
                        }
                        sender.sendMessage(gCh + "That guerrilla doesn't exist");
                        return true;

                    }
                    sender.sendMessage(gCh + "/guerrilla join name");
                    return true;
                } else if ((args[0].equalsIgnoreCase("claim"))) {
                    Player player = (Player) sender;
                    if (guerrilla != null) {
                        guerrilla.Claim(chunk, player);
                        return true;
                    }
                    player.sendMessage(gCh + "You have no guerrilla");
                    return true;
                } else if ((args[0].equalsIgnoreCase("unclaim"))) {
                    Player player = (Player) sender;
                    if (guerrilla != null) {
                        guerrilla.unclaim(chunk, player);
                        return true;
                    }
                    player.sendMessage(gCh + "You can't do that");
                    return true;
                } else if ((args[0].equalsIgnoreCase("create"))) {
                    if (args.length == 2) {
                        if (GuerrillaG.getPlayerGuerrilla((Player) sender) == null) {
                            if (GuerrillaG.getGuerrillaByName((String) args[1]) != null) {
                                sender.sendMessage(gCh + "That guerrilla already exists! Please choose another name");
                                return true;
                            }
                            if (args[1].length() > 10) {
                                playurd.sendMessage(gCh + "The name is too long, max. 10 characters");
                                return true;
                            }
                            GuerrillaG guerrillan = new GuerrillaG((Player) sender, (String) args[1]);
                            GuerrillaG.GuerrillaList.add(guerrillan);
                            //sender.sendMessage(gCh + "You created the "+ guerrilla.getName() +" guerrilla");
                            return true;
                        }
                        sender.sendMessage(gCh + "You have already joined a guerrilla");
                        return true;

                    }
                    sender.sendMessage(gCh + "/guerrilla create [guerrilla name]");
                    return true;
                } else if (args[0].equalsIgnoreCase("disband")) {
                    if (args.length == 2) {
                        Player player = (Player) sender;
                        if ((GuerrillaG.getGuerrillaByName((String) args[1])) == (guerrilla)) {
                            guerrilla.disband(player);
                            return true;
                        }
                        sender.sendMessage(gCh + "You can't disband other guerrilla than your own!");
                        return true;
                    }
                    sender.sendMessage(gCh + "Are you sure? type /guerrilla disband [yourguerrillaname] to disband");
                    return true;
                }

            }
        }
        return false;
    }

    public static void delayedClaim(final GuerrillaG gclaimer, final GuerrillaG gowner, final Chunk chunk, final Player claimer) {

        ArrayList<Double> clistn = new ArrayList<Double>(2);
        Double chunkX = new Double(chunk.getX());
        Double chunkZ = new Double(chunk.getZ());
        clistn.add(chunkX);
        clistn.add(chunkZ);
        final ArrayList<Double> clist = clistn;

        final Integer dclaimid = sinst.getScheduler().scheduleSyncDelayedTask(ginst, new Runnable() {
            public void run() {
                DelayedClaimData dcd = delayedClaimDataQueue.search(claimer.getName());

                if (claimer.getInventory().contains(Guerrilla.itemid, (Guerrilla.chunkprice * Guerrilla.conqmulti))) {
                    GuerrillaG.removeInventoryItems(claimer.getInventory(), Material.getMaterial(Guerrilla.itemid),
                            (Guerrilla.chunkprice * Guerrilla.conqmulti));
                } else {
                    claimer.sendMessage(gCh + "You pig! You dropped the payment? you don't get the chunk!");
                    delayedClaimDataQueue.removeNode(dcd);
                    return;
                }
                if (stateWon == true) {
                    delayedClaimDataQueue.removeNode(dcd);
                    return;
                }
                gowner.claimedchunks--;
                gowner.Territories.remove(clist);
                gclaimer.claimedchunks++;
                gclaimer.Territories.add(clist);
                delayedClaimDataQueue.removeNode(dcd);
                GuerrillaG.gmsgbroadcast(gclaimer.getName() + " took a part of territory from " + gowner.getName() + "!");

            }
        }, conqdelay);

        delayedClaimDataQueue.addNode(new DelayedClaimData(clist, gclaimer, gowner, claimer.getName(), dclaimid.intValue()));
    }

}
