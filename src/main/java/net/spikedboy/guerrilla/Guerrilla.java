package net.spikedboy.guerrilla;

import net.spikedboy.guerrilla.configuration.GuerrillaConfigurations;
import net.spikedboy.guerrilla.landclaim.DelayedClaimData;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

public class Guerrilla implements Serializable {

    private static final long serialVersionUID = 5277397633585310503L;

    public static ArrayList<Guerrilla> guerrillaList = new ArrayList<>();

    private static ArrayList<ArrayList<Integer>> safeChunks = new ArrayList<>();

    public final ArrayList<ArrayList<Integer>> territories = new ArrayList<>();
    public final ArrayList<String> players = new ArrayList<>();
    public final ArrayList<ArrayList<Integer>> paymentChests = new ArrayList<>();

    public String leader;
    public Date date;
    public Date quitPunishmentDate;

    int numberOfClaimedChunks = 0;

    private final ArrayList<String> invites = new ArrayList<>();

    private ArrayList<ArrayList<Integer>> safeChest = new ArrayList<>(); //chest block xzy

    private String name;
    private Date antiSpam;

    public Guerrilla(Player player, String args) {
        players.add(player.getName());
        leader = player.getName();
        name = args;
        Guerrilla.gmsgbroadcast(player.getName() + " created the " + name + " guerrilla!");
        try {
            Guerrilla.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isBeingClaimed(Guerrilla guerrilla) {
        return GuerrillaPlugin.delayedClaimDataQueue.search(guerrilla, 1) != null;
    }

    public void changeLeader(Player sender, String nleader) {
        Guerrilla guerrillas = Guerrilla.getPlayerGuerrilla(sender);
        if (guerrillas == null) {
            sender.sendMessage(GuerrillaPlugin.gCh + "You have no guerrilla");
            return;
        }
        String gsleader = guerrillas.getLeader();
        String sname = sender.getName();
        Guerrilla nlg = Guerrilla.getPlayerGuerrilla(GuerrillaPlugin.serverInstance.getPlayerExact(nleader));
        if (nlg == null) {
            sender.sendMessage(GuerrillaPlugin.gCh + "That player has no guerrilla!");
            return;
        } else if (!(nlg.equals(guerrillas))) {
            sender.sendMessage(GuerrillaPlugin.gCh + "That player is not in your guerrilla");
            return;
        }
        if (sname.equals(gsleader)) {
            guerrillas.leader = nleader;
            msggue("The new leader is " + nleader + "!");
        } else {
            sender.sendMessage(GuerrillaPlugin.gCh + "Only the leader can do that");
        }
    }

    public void addSafeChest(Chest chest, Player sender) {
        ArrayList<Integer> chlist = Guerrilla.ChestToList(chest);

        if (Guerrilla.getGuerrillaChunk(chest.getBlock().getChunk()) != Guerrilla.getPlayerGuerrilla(sender)) {
            sender.sendMessage(GuerrillaPlugin.gCh + "You can't make a safechest outside of your territory");
            return;
        }

        if (!Guerrilla.isLeader(sender)) {
            sender.sendMessage(GuerrillaPlugin.gCh + "You are not leader");
            GuerrillaPlugin.playerSetsSafe.remove(sender.getName());
            return;
        }
        if (safeChest == null) safeChest = new ArrayList<>();
        int np = countPlayers();
        if (GuerrillaConfigurations.minPSC > np) {
            sender.sendMessage(GuerrillaPlugin.gCh + "You can't have a safe chest if your guerrilla has less than "
                    + GuerrillaConfigurations.minPSC + " members");
            return;
        }
        if (safeChest.isEmpty()) {
            safeChest.add(chlist);
            sender.sendMessage(GuerrillaPlugin.gCh + "Safe chest added!");
            GuerrillaPlugin.playerSetsSafe.remove(sender.getName());
            return;
        }
        sender.sendMessage(GuerrillaPlugin.gCh + "You already have a safechest");
        GuerrillaPlugin.playerSetsSafe.remove(sender.getName());
    }

    public void removeSafeChest(Chest chest, Player sender) {
        ArrayList<Integer> chlist = Guerrilla.ChestToList(chest);
        if (safeChest.isEmpty()) {
            sender.sendMessage(GuerrillaPlugin.gCh + "You have no chests!");
            GuerrillaPlugin.playerSetsSafe.remove(sender.getName());
            return;
        } else if (safeChest.contains(chlist)) {
            sender.sendMessage(GuerrillaPlugin.gCh + "Safe chest removed");
            GuerrillaPlugin.playerSetsSafe.remove(sender.getName());
            safeChest.remove(chlist);
            return;
        }
        sender.sendMessage(GuerrillaPlugin.gCh + "That is not your chest");
        GuerrillaPlugin.playerSetsSafe.remove(sender.getName());
    }

    private int countPlayers() {
        return players.size();
    }

    private boolean isThereAtLeastGuerrillaMemberOnline() {
        for (String pname : players) {
            Collection<? extends Player> onlinePlayers = GuerrillaPlugin.serverInstance.getOnlinePlayers();
            for (Player p1 : onlinePlayers) {
                if (p1.getName().equals(pname)) {
                    return true;
                }
            }

        }
        return false;
    }

    public int howManyGuerrillaMembersOnline() {
        int i = 0;
        for (String pname : players) {
            Collection<? extends Player> onlinePlayers = GuerrillaPlugin.serverInstance.getOnlinePlayers();
            for (Player p1 : onlinePlayers) {
                if (p1.getName().equals(pname)) {
                    i++;
                }
            }

        }
        return i;
    }

    private String getOnlinePlayersText() {
        int n = howManyGuerrillaMembersOnline();
        String answ = "";

        if (n != 0) {
            answ = (ChatColor.LIGHT_PURPLE + " Online: " + ChatColor.WHITE + "(" + n + ")");
        }

        return answ;
    }

    private Chest getdchest(Chest chest) {
        if (chest.getBlock().getRelative(BlockFace.NORTH).getType() == Material.CHEST)
            return (Chest) chest.getBlock().getRelative(BlockFace.NORTH).getState();
        else if (chest.getBlock().getRelative(BlockFace.SOUTH).getType() == Material.CHEST)
            return (Chest) chest.getBlock().getRelative(BlockFace.SOUTH).getState();
        else if (chest.getBlock().getRelative(BlockFace.EAST).getType() == Material.CHEST)
            return (Chest) chest.getBlock().getRelative(BlockFace.EAST).getState();
        else if (chest.getBlock().getRelative(BlockFace.WEST).getType() == Material.CHEST)
            return (Chest) chest.getBlock().getRelative(BlockFace.WEST).getState();
        return null;
    }


    public boolean removePaymentChest(Chest chest) {
        ArrayList<Integer> al1 = ChestToList(chest);
        if (!paymentChests.contains(al1)) {
            return false;
        }
        paymentChests.remove(al1);
        if (getdchest(chest) != null) {
            paymentChests.remove(ChestToList(getdchest(chest)));
        }
        return true;
    }

    public boolean addPaymentChest(Chest chest) {
        ArrayList<Integer> al1 = ChestToList(chest);
        if (paymentChests.contains(al1)) {
            return false;
        }
        paymentChests.add(al1);
        if (getdchest(chest) != null) {
            paymentChests.add(ChestToList(getdchest(chest)));
        }
        return true;
    }

    private void removePayment() {
        int price = (territories.size() / GuerrillaConfigurations.nchunkpay) * GuerrillaConfigurations.chunkmaintprice;
        boolean paid = false;
        for (ArrayList<Integer> chestc : paymentChests) {
            if (GuerrillaPlugin.serverInstance.getWorld(GuerrillaConfigurations.gworldname).getBlockAt(chestc.get(0),
                    chestc.get(1), chestc.get(2)).getType() != Material.CHEST) {
                msggue("A payment chest is missing and has been marked for removal");
                continue;
            }
            Chest chest = ((Chest) (GuerrillaPlugin.serverInstance.getWorld(GuerrillaConfigurations.gworldname).getBlockAt(chestc.get(0), chestc.get(1), chestc.get(2)).getState()));
            if ((chest != null) && (chest.getInventory().contains(GuerrillaConfigurations.itemidmaint, GuerrillaConfigurations.chunkmaintprice))) {
                if (price > GuerrillaConfigurations.maintmaxprice) price = GuerrillaConfigurations.maintmaxprice;
                Guerrilla.removeInventoryItems(chest.getInventory(), Material.getMaterial(GuerrillaConfigurations.itemidmaint), (price));
                paid = true;
                msggue("Payments issued, cost: " + price + ", thank you: Att. Tom Nook");
                break;
            }
        }
        if (!paid) {
            if (territories.isEmpty() || price == 0) {
                msggue("Payments issued, but you had nothing to pay");
                return;
            }
            territories.remove((territories.size() - 1));
            numberOfClaimedChunks--;
            msggue("You have lost a chunk because you have no payment chests");
            return;
        }
        return;
    }

    private boolean somebodyHome(Chunk chunk) {
        for (String playern : players) {
            if (GuerrillaPlugin.serverInstance.getPlayer(playern) == null) continue;
            if (GuerrillaPlugin.serverInstance.getPlayer(playern).getLocation().getBlock().getChunk().equals(chunk)) {
                return true;
            }
        }
        return false;
    }

    public boolean claim(Chunk chunk, Player claimer) {
        if (Guerrilla.isSafeChunk(chunk)) {
            claimer.sendMessage(GuerrillaPlugin.gCh + "Can't claim a safe chunk");
            return false;
        }
        if (Guerrilla.getPlayerGuerrilla(claimer) != null) {
            ArrayList<Integer> clist = new ArrayList<>(2);
            int chunkX = chunk.getX();
            int chunkZ = chunk.getZ();
            clist.add(chunkX);
            clist.add(chunkZ);
            if ((territories.contains(clist))) {
                claimer.sendMessage(ChatColor.DARK_RED + "[GuerrillaPlugin] " + ChatColor.GRAY + "That chunk is already claimed");
                return false;
            } else if (!(claimer.getInventory().contains(GuerrillaConfigurations.itemid, GuerrillaConfigurations.chunkprice))) {
                claimer.sendMessage(ChatColor.DARK_RED + "[GuerrillaPlugin] " + ChatColor.GRAY + "The required price is "
                        + GuerrillaConfigurations.chunkprice + " of " + (Material.getMaterial(GuerrillaConfigurations.itemid).name())
                        + " and you don't have it");
                return false;
            } else {
                if (canClaim(chunk, claimer)) {
                    if (Guerrilla.getGuerrillaChunk(chunk) != this && Guerrilla.getGuerrillaChunk(chunk) != null) {
                        if (!claimer.getInventory().contains(GuerrillaConfigurations.itemid, GuerrillaConfigurations.chunkprice * GuerrillaConfigurations.conqmulti)) {
                            claimer.sendMessage(ChatColor.DARK_RED + "[GuerrillaPlugin] " + ChatColor.GRAY + "The required price is "
                                    + (GuerrillaConfigurations.chunkprice * GuerrillaConfigurations.conqmulti) + " of "
                                    + (Material.getMaterial(GuerrillaConfigurations.itemid).name()) + " and you don't have it");

                            return false;
                        }

                        if (GuerrillaPlugin.delayedClaimDataQueue.search(claimer.getName()) != null) {
                            claimer.sendMessage(ChatColor.DARK_RED + "[GuerrillaPlugin] " + ChatColor.GRAY
                                    + "Your faction is already claiming an enemy chunk! You can't do that twice");
                            return true;
                        }
                        GuerrillaPlugin.delayedClaim(this, Guerrilla.getGuerrillaChunk(chunk), chunk, claimer);
                        Guerrilla.getGuerrillaChunk(chunk).msggue(this.getName() + " is claiming part of your territory! Coords: "
                                + chunk.getBlock(0, 0, 0).getX() + "," + chunk.getBlock(0, 0, 0).getZ() + " (x,z)");
                        msggue("Claiming enemy area, if you leave the chunk you will loose this dispute!");
                        return true;
                    }
                    Guerrilla.removeInventoryItems(claimer.getInventory(), Material.getMaterial(GuerrillaConfigurations.itemid), GuerrillaConfigurations.chunkprice);
                    numberOfClaimedChunks++;
                    territories.add(clist);
                    gmsgbroadcast(this.getName() + " claimed some terrain");
                    //claimer.sendMessage("Chunk claimed");
                    return true;
                } else {
                    claimer.sendMessage(ChatColor.DARK_RED + "[GuerrillaPlugin] " + ChatColor.GRAY + "You can't claim this chunk");
                    return false;
                }
            }
        }
        return false;
    }

    private boolean canClaim(Chunk chunk, Player claimer) {
        Guerrilla owner = Guerrilla.getGuerrillaChunk(chunk);
        Guerrilla gclaimer = Guerrilla.getPlayerGuerrilla(claimer);
        Date now = new Date();
        if ((owner != this) && (owner != null)) {
            if (owner.date == null) owner.date = now;
            if (((now.getTime() - owner.date.getTime()) >= GuerrillaConfigurations.expTime) && ((adyChunks(chunk, owner) <= 2)
                    && (adyChunks(chunk, gclaimer) >= 1))) {
                return true;
            }
            if ((owner.quitPunishmentDate != null) && ((now.getTime() - owner.quitPunishmentDate.getTime()) < GuerrillaConfigurations.delay)
                    && ((adyChunks(chunk, owner) <= 2) && (adyChunks(chunk, gclaimer) >= 1))) {
                return true;
            }
            if (!owner.isThereAtLeastGuerrillaMemberOnline()) return false;
            if (owner.somebodyHome(chunk)) return false;
            if ((adyChunks(chunk, owner) <= 2) && (adyChunks(chunk, gclaimer) >= 1)) {
                return true;
            }
        } else if (owner == null) {
            if (adyChunks(chunk, this) >= 1) {
                return true;
            } else if (adyChunks(chunk, this) == 0) {
                return this.numberOfClaimedChunks == 0;
            }
        } else if (owner == this) {
            return false;
        }
        return false;
    }

    public boolean unclaim(Chunk chunk, Player claimer) {
        ArrayList<Integer> clist = new ArrayList<>(2);
        int chunkX = chunk.getX();
        int chunkZ = chunk.getZ();
        clist.add(chunkX);
        clist.add(chunkZ);
        if (territories.contains(clist)) {
            numberOfClaimedChunks--;
            territories.remove(clist);
            claimer.sendMessage(ChatColor.DARK_RED + "[GuerrillaPlugin] " + ChatColor.GRAY + "Chunk unclaimed");
            return true;
        }
        claimer.sendMessage(ChatColor.DARK_RED + "[GuerrillaPlugin] " + ChatColor.GRAY + "You don't own this chunk");
        return false;
    }

    public boolean unclaimall(Player sender) {
        if (sender.getName().equals(leader)) {
            numberOfClaimedChunks = 0;
            territories.clear();
            msggue("All territories have been unclaimed!");
            return true;
        }
        sender.sendMessage(ChatColor.DARK_RED + "[GuerrillaPlugin] " + ChatColor.GRAY + "You are not leader");
        return true;
    }

    public void msggue(String msg) {
        for (String playername : players) {

            if (GuerrillaPlugin.serverInstance.getPlayer(playername) != null) {
                GuerrillaPlugin.serverInstance.getPlayer(playername).sendMessage(ChatColor.DARK_RED + "[GuerrillaPlugin] " + ChatColor.GRAY + msg);
            }
        }
    }

    public void msgguespam(String msg) {
        Date now = new Date();
        if ((this.antiSpam != null) && ((now.getTime() - this.antiSpam.getTime()) < 5000)) {
            return;
        }
        this.antiSpam = new Date();
        for (String playername : players) {
            Player player = GuerrillaPlugin.serverInstance.getPlayer(playername);
            //GuerrillaPlugin.log.info("deb " + antiSpam.getTime());
            if (player != null) {
                player.sendMessage(ChatColor.DARK_RED + "[GuerrillaPlugin] " + ChatColor.GRAY + msg);
            }
        }
    }

    public boolean join(Player player) {
        if (invites.contains(player.getName())) {
            msggue(player.getName() + " has joined your GuerrillaPlugin");
            players.add(player.getName());
            invites.remove(player.getName());
            player.sendMessage(ChatColor.DARK_RED + "[GuerrillaPlugin] " + ChatColor.GRAY + "You are now part of the "
                    + this.getName() + " guerrilla");
            return true;
        } else {
            player.sendMessage(ChatColor.DARK_RED + "[GuerrillaPlugin] " + ChatColor.GRAY + "You have not been invited to this faction");
            return false;
        }
    }

    public boolean kick(String player, Player sender) {
        if (Guerrilla.isLeader(sender)) {
            if (Guerrilla.getPlayerGuerrilla(player) == Guerrilla.getPlayerGuerrilla(sender)) {
                players.remove(player);
                msggue(player + " has been kicked");
                if (GuerrillaPlugin.serverInstance.getPlayer(player) != null) {
                    GuerrillaPlugin.serverInstance.getPlayer(player).sendMessage(ChatColor.DARK_RED + "[GuerrillaPlugin] " + ChatColor.GRAY
                            + sender.getName() + " has kicked you");
                }
                return true;
            }
            sender.sendMessage(ChatColor.DARK_RED + "[GuerrillaPlugin] " + ChatColor.GRAY + "That player is not in your guerrilla");
            return false;
        }
        sender.sendMessage(ChatColor.DARK_RED + "[GuerrillaPlugin] " + ChatColor.GRAY + "You are not a guerrilla leader");
        return false;
    }

    public boolean invite(Player player, CommandSender sender) {
        if (!(invites.contains(player.getName())) && ((Guerrilla.getPlayerGuerrilla(player)) == null)) {
            invites.add(player.getName());
            sender.sendMessage(ChatColor.DARK_RED + "[GuerrillaPlugin] " + ChatColor.GRAY + player.getName() + " has been invited to your guerrilla");
            player.sendMessage(ChatColor.DARK_RED + "[GuerrillaPlugin] " + ChatColor.GRAY + "You have been invited to the " + this.getName() + " guerrilla");
            return true;
        }
        sender.sendMessage(ChatColor.DARK_RED + "[GuerrillaPlugin] " + ChatColor.GRAY + "That player can't get invited");
        return false;
    }

    public boolean inviteCancel(Player player, Player sender) {
        if ((invites.contains(player.getName())) && (Guerrilla.isLeader(sender))) {
            invites.remove(player.getName());
            sender.sendMessage(ChatColor.DARK_RED + "[GuerrillaPlugin] " + ChatColor.GRAY + "You canceled that invite");
            return true;
        }
        sender.sendMessage(ChatColor.DARK_RED + "[GuerrillaPlugin] " + ChatColor.GRAY + "You can't do that");
        return false;
    }

    public boolean ownsGuerrillaChunk(Chunk chunk) {
        ArrayList<Integer> clist = new ArrayList<>(2);
        Integer chunkX = chunk.getX();
        Integer chunkZ = chunk.getZ();
        clist.add(chunkX);
        clist.add(chunkZ);
        if (Guerrilla.getGuerrillaChunk(chunk) == null) {
            return true;
        } else {
            return (territories.contains(clist));
        }
    }

    public boolean disband(Player player) {
        if (this.getLeader().equals(player.getName())) {
            msggue("Your guerrilla has been disbanded!");
            GuerrillaPlugin.log.info(this.getName() + " disbanded");
            players.clear();
            territories.clear();
            invites.clear();
            name = "";
            leader = "";
            numberOfClaimedChunks = 0;
            guerrillaList.remove(this);
            return true;
        }
        player.sendMessage(ChatColor.DARK_RED + "[GuerrillaPlugin] " + ChatColor.GRAY + "You do not own this guerrilla");
        return false;
    }

    public String getLeader() {
        return leader;
    }

    public String getName() {
        return name;
    }

    public static Guerrilla getGuerrillaSafeChest(Chest chest) {
        for (Guerrilla g1 : guerrillaList) {
            if (g1.safeChest == null) continue;
            if (g1.safeChest.isEmpty()) continue;
            ArrayList<Integer> chlist = Guerrilla.ChestToList(chest);
            if (g1.safeChest.get(0).equals(chlist)) {
                return Guerrilla.getGuerrillaChunk(chest.getBlock().getChunk());
            }
        }
        return null;
    }

    private static void sortGList() {
        //TODO
        if (guerrillaList.isEmpty()) return;
        for (int i = 0; i < (guerrillaList.size()); i++) {

            int n = guerrillaList.get(i).howManyGuerrillaMembersOnline(), maxIn = i;

            for (int j = i; j < (guerrillaList.size()); j++) {
                if (n <= guerrillaList.get(j).howManyGuerrillaMembersOnline()) maxIn = j;
            }

            Guerrilla gAu = guerrillaList.get(i);
            guerrillaList.set(i, guerrillaList.get(maxIn));
            guerrillaList.set(maxIn, gAu);

        }
    }

    public static void List(Player sender, int page) {
        sortGList();
        if (page <= 0) page = 1;
        int index = (page - 1) * 7;
        sender.sendMessage(ChatColor.DARK_RED + "[GuerrillaPlugin] " + ChatColor.WHITE + "Listing Guerrillas:" + ChatColor.GRAY
                + " (Chunk objective: " + GuerrillaConfigurations.objectiveChunks + ") " + ChatColor.WHITE + "PAGE " + page);
        for (int i = index; i < (guerrillaList.size()); i++) {
            if (i > (guerrillaList.size() - 1)) break;
            Guerrilla guerrilla = guerrillaList.get(i);
            sender.sendMessage("- " + guerrilla.name + ChatColor.LIGHT_PURPLE + " Leader: " + ChatColor.WHITE
                    + guerrilla.getLeader() + ChatColor.LIGHT_PURPLE + " Chunks: " + ChatColor.WHITE + guerrilla.numberOfClaimedChunks
                    + guerrilla.getOnlinePlayersText());
        }
    }

    public static Guerrilla getGuerrillaByName(String name) {
        for (Guerrilla guerrilla : guerrillaList) {
            if (guerrilla.getName().equals(name)) {
                return guerrilla;
            }
        }
        return null;
    }

    public static Guerrilla checkWinners() {
        for (Guerrilla guerrilla : guerrillaList) {
            if (guerrilla.numberOfClaimedChunks >= GuerrillaConfigurations.objectiveChunks) {
                return guerrilla;
            }
        }
        return null;
    }

    public static void printmap() throws IOException {
        FileWriter fstream = new FileWriter("coords.txt");
        BufferedWriter out = new BufferedWriter(fstream);
        int contador = 0;
        for (Guerrilla guerrilla : guerrillaList) {
            for (ArrayList<Integer> clist : guerrilla.territories) {
                Integer ClistX = clist.get(0);
                Integer ClistZ = clist.get(1);
                String gname = guerrilla.getName();
                if (contador != 0) out.newLine();
                out.write(ClistX + "," + ClistZ + "," + gname + ",");
                out.flush();
                contador++;
            }
        }
    }

    public static void who(Player sender, String args) {
        Date now = new Date();
        if (args == null) {
            Guerrilla guerrilla = Guerrilla.getPlayerGuerrilla(sender);
            if (guerrilla == null) {
                sender.sendMessage(ChatColor.DARK_RED + "[GuerrillaPlugin] " + ChatColor.GRAY + "You have no guerrilla");
                return;
            }
            if (guerrilla.date == null) guerrilla.date = now;
            sender.sendMessage(ChatColor.DARK_RED + "[GuerrillaPlugin] " + ChatColor.GRAY + guerrilla.getName() + "'s info:");
            sender.sendMessage(ChatColor.DARK_AQUA + " Chunks claimed: " + ChatColor.WHITE + guerrilla.numberOfClaimedChunks);
            sender.sendMessage(ChatColor.DARK_AQUA + " Payment chests: " + ChatColor.WHITE + guerrilla.paymentChests.size());
            sender.sendMessage(ChatColor.DARK_AQUA + " Last login: " + ChatColor.WHITE
                    + ((now.getTime() - guerrilla.date.getTime()) / 3600000) + ChatColor.DARK_AQUA + "h ago");

            sender.sendMessage(ChatColor.DARK_AQUA + " Members:");

            for (String playername : guerrilla.players) {
                if (guerrilla.getLeader() == playername) {
                    sender.sendMessage("  " + playername + (ChatColor.LIGHT_PURPLE + " (leader)"));
                } else {
                    sender.sendMessage("  " + playername);
                }
            }
        } else {
            Guerrilla guerrilla = Guerrilla.getGuerrillaByName(args);
            if (guerrilla == null) {
                sender.sendMessage(args + " doesn't exist");
                return;
            } else {
                if (guerrilla.date == null) guerrilla.date = now;
                sender.sendMessage(ChatColor.DARK_RED + "[GuerrillaPlugin] " + ChatColor.GRAY + guerrilla.getName() + "'s info:");
                sender.sendMessage(ChatColor.DARK_AQUA + " Chunks claimed: " + ChatColor.WHITE + guerrilla.numberOfClaimedChunks);
                sender.sendMessage(ChatColor.DARK_AQUA + " Payment chests: " + ChatColor.WHITE + guerrilla.paymentChests.size());
                sender.sendMessage(ChatColor.DARK_AQUA + " Last login: " + ChatColor.WHITE
                        + ((now.getTime() - guerrilla.date.getTime()) / 3600000) + ChatColor.DARK_AQUA + "h ago");
                if (guerrilla.quitPunishmentDate != null) {
                    sender.sendMessage(ChatColor.DARK_AQUA + " Punished: " + ChatColor.WHITE
                            + (((now.getTime() - guerrilla.quitPunishmentDate.getTime()) < GuerrillaConfigurations.delay)));
                }
                sender.sendMessage(ChatColor.DARK_AQUA + " Members:");
                for (String playername : guerrilla.players) {
                    if (guerrilla.getLeader().equals(playername)) {
                        sender.sendMessage("  " + playername + (ChatColor.LIGHT_PURPLE + " (leader)"));
                    } else {
                        sender.sendMessage("  " + playername);
                    }
                }
            }
        }
    }

    public static Guerrilla getGuerrillaChunk(Chunk chunk) {
        ArrayList<Integer> clist = new ArrayList<>(2);
        Integer chunkX = chunk.getX();
        Integer chunkZ = chunk.getZ();
        clist.add(chunkX);
        clist.add(chunkZ);
        for (Guerrilla guerrilla : guerrillaList) {
            if (guerrilla.territories.contains(clist)) {
                return guerrilla;
            }
        }
        return null;
    }

    public static Guerrilla getPlayerGuerrilla(Player player) {
        for (Guerrilla guerrilla : guerrillaList) {
            if (guerrilla.players.contains(player.getName())) {
                return guerrilla;
            }
        }
        return null;
    }

    private static Guerrilla getPlayerGuerrilla(String player) {
        for (Guerrilla guerrilla : guerrillaList) {
            if (guerrilla.players.contains(player)) {
                return guerrilla;
            }
        }
        return null;
    }

    public static boolean isLeader(Player player) {
        Guerrilla guerrilla = Guerrilla.getPlayerGuerrilla(player);
        //GuerrillaPlugin.log.info(player.getName() + " " + guerrilla.leader + " " + guerrilla.getLeader() + " " + guerrilla.leader.equals(player.getName()));
        return guerrilla.leader.equals(player.getName());
    }

    public static boolean leave(Player sender) {
        if (Guerrilla.isLeader(sender)) {
            sender.sendMessage(ChatColor.DARK_RED + "[GuerrillaPlugin] " + ChatColor.GRAY + "The leader can't leave! Disband!");
            return true;
        }
        Guerrilla guerrilla = Guerrilla.getPlayerGuerrilla(sender);
        if ((guerrilla != null) && ((!Guerrilla.isLeader(sender)))) {
            guerrilla.players.remove(sender.getName());
            sender.sendMessage(ChatColor.DARK_RED + "[GuerrillaPlugin] " + ChatColor.GRAY + "You left the "
                    + guerrilla.getName() + " guerrilla!");
            return true;
        }
        sender.sendMessage(ChatColor.DARK_RED + "[GuerrillaPlugin] " + ChatColor.GRAY + "You can't do that!");
        return false;
    }

    public static boolean inviteDecline(Player sender, String args) {
        Guerrilla guerrilla = Guerrilla.getGuerrillaByName(args);
        if (guerrilla == null) {
            sender.sendMessage(ChatColor.DARK_RED + "[GuerrillaPlugin] " + ChatColor.GRAY + "That guerrilla doesn't exist");
            return false;
        }
        if (guerrilla.invites.contains(sender.getName())) {
            guerrilla.msggue(sender.getName() + "Declined his invite");
            guerrilla.invites.remove(sender.getName());
            sender.sendMessage(ChatColor.DARK_RED + "[GuerrillaPlugin] " + ChatColor.GRAY + "Invitation declined");
            return true;
        }
        sender.sendMessage(ChatColor.DARK_RED + "[GuerrillaPlugin] " + ChatColor.GRAY + "You have no pending invites");
        return false;
    }

    private static void saveFunc(Object o, String path) throws Exception {
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path));
        oos.writeObject(o);
        oos.flush();
        oos.close();
    }

    private static Object loadFunc(String path) throws Exception {
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path));
        Object result = ois.readObject();
        ois.close();
        return result;
    }

    public static void save() throws Exception {
        Guerrilla.saveFunc(guerrillaList, "GuerrillaPlugin.bin");
        Guerrilla.saveFunc(safeChunks, "GuerrillaSafe.bin");
    }

    public static void removeInventoryItems(Inventory inv, Material type, int amount) {
        GuerrillaPlugin.log.info("cantidad total: " + amount);
        for (ItemStack is : inv.getContents()) {
            if ((is != null) && (is.getType() == type)) {
                int newamount = is.getAmount() - amount;
                GuerrillaPlugin.log.info("stack - total: " + newamount);
                if (newamount > 0) {
                    is.setAmount(newamount);
                    break;
                } else {
                    inv.remove(is);
                    amount = -newamount;
                    if (amount == 0) break;
                }
            }
        }
    }

    public static void load() throws Exception {
        File gFile = new File("GuerrillaPlugin.bin");
        File sChunks = new File("GuerrillaSafe.bin");
        if (gFile.exists()) guerrillaList = (ArrayList<Guerrilla>) Guerrilla.loadFunc("GuerrillaPlugin.bin");
        else gFile.createNewFile();
        if (sChunks.exists()) safeChunks = (ArrayList<ArrayList<Integer>>) Guerrilla.loadFunc("GuerrillaSafe.bin");
        else sChunks.createNewFile();
    }

    private static int adyChunks(Chunk chunk, Guerrilla guerrilla) {
        int n = 0;
        if (Guerrilla.getGuerrillaChunk(chunk.getBlock(1, 1, 1).getRelative(BlockFace.NORTH, 17).getChunk()) == guerrilla) {
            n++;
        }
        if (Guerrilla.getGuerrillaChunk(chunk.getBlock(1, 1, 1).getRelative(BlockFace.SOUTH, 17).getChunk()) == guerrilla) {
            n++;
        }
        if (Guerrilla.getGuerrillaChunk(chunk.getBlock(1, 1, 1).getRelative(BlockFace.EAST, 17).getChunk()) == guerrilla) {
            n++;
        }
        if (Guerrilla.getGuerrillaChunk(chunk.getBlock(1, 1, 1).getRelative(BlockFace.WEST, 17).getChunk()) == guerrilla) {
            n++;
        }
        return n;
    }

    public static void chargeMaintenance() {
        gmsgbroadcast("Issuing payments...");
        for (Guerrilla guerrilla : guerrillaList) {
            guerrilla.removePayment();
        }
        GuerrillaPlugin.log.info("[GuerrillaPlugin] Done");
        try {
            Guerrilla.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void gmsgbroadcast(String msg) {
        GuerrillaPlugin.log.info("[GuerrillaPlugin] " + msg);
        for (Player player : GuerrillaPlugin.serverInstance.getOnlinePlayers()) {
            player.sendMessage(ChatColor.DARK_PURPLE + "[GuerrillaPlugin] " + ChatColor.GRAY + msg);
        }
    }

    private static ArrayList<Integer> ChestToList(Chest chest) {
        ArrayList<Integer> chlist = new ArrayList<>(3);
        Integer ChX = chest.getX();
        Integer ChY = chest.getY();
        Integer ChZ = chest.getZ();
        chlist.add(ChX);
        chlist.add(ChY);
        chlist.add(ChZ);
        return chlist;
    }

    public static Chest ListToChest(ArrayList<Integer> chlist) {
        int chestx = chlist.get(0);
        int chesty = chlist.get(1);
        int chestz = chlist.get(2);
        Chest chest = (Chest) GuerrillaPlugin.serverInstance.getWorld(GuerrillaConfigurations.gworldname).getBlockAt(chestx, chesty, chestz).getState();
        if (chest == null) {
            GuerrillaPlugin.log.info("[GuerrillaPlugin] Oh shit nigga what are you doing");
            return null;
        }
        return chest;
    }

    public static void map(Block block, Player sender) {
        //WIP

        boolean finished = false;
        Chunk center = block.getChunk();
        String line = "";
        char[] lineCh = new char[4];

        World world = center.getWorld();

        Chunk ulcorner = world.getChunkAt(center.getX() + 1, center.getZ() + 1);

        Chunk uside = world.getChunkAt(center.getX(), center.getZ() + 1);
        Chunk urcorner = world.getChunkAt(center.getX() - 1, center.getZ() + 1);

        Chunk lside = world.getChunkAt(center.getX() + 1, center.getZ());
        Chunk rside = world.getChunkAt(center.getX() - 1, center.getZ());

        Chunk dlcorner = world.getChunkAt(center.getX() + 1, center.getZ() - 1);
        Chunk dside = world.getChunkAt(center.getX(), center.getZ() - 1);
        Chunk drcorner = world.getChunkAt(center.getX() - 1, center.getZ() - 1);

        Block first = ulcorner.getBlock(0, 0, 0);


        for (byte j = 0; j < 12; j++) {
            for (byte i = 0; i < 4; i++) {
                if (i == 1) {
                    lineCh[i] = '+';
                }
                if (i > 1 && i < 3) {
                    lineCh[i] = 'G';
                    continue;
                }
                if (i == 3) {
                    lineCh[i] = '+';
                    continue;
                }
            }

            line += lineCh;
        }


    }

    public static void setSafeChunk(Chunk chunk) {
        Guerrilla owner = getGuerrillaChunk(chunk);

        ArrayList<Integer> clist = new ArrayList<>(2);
        Integer chunkX = chunk.getX();
        Integer chunkZ = chunk.getZ();
        clist.add(chunkX);
        clist.add(chunkZ);
        if (owner != null) {
            owner.territories.remove(clist);
        }
        if (safeChunks.contains(clist)) {
            return;
        }
        safeChunks.add(clist);
    }

    public static boolean isSafeChunk(Chunk chunk) {
        ArrayList<Integer> clist = new ArrayList<>(2);
        Integer chunkX = chunk.getX();
        Integer chunkZ = chunk.getZ();
        clist.add(chunkX);
        clist.add(chunkZ);
        return safeChunks.contains(clist);
    }

    public static void removeSafeChunk(Chunk chunk) {
        ArrayList<Integer> clist = new ArrayList<>(2);
        Integer chunkX = chunk.getX();
        Integer chunkZ = chunk.getZ();
        clist.add(chunkX);
        clist.add(chunkZ);

        if (!safeChunks.contains(clist)) {
            return;
        }
        safeChunks.remove(clist);
    }

    public static int getClaimingID(String pname) {
        DelayedClaimData dcd = GuerrillaPlugin.delayedClaimDataQueue.search(pname);
        if (dcd != null) {
            return (dcd.getThreadID());
        } else {
            return -1;
        }
    }

}
