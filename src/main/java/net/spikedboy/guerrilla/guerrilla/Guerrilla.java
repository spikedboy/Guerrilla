package net.spikedboy.guerrilla.guerrilla;

import com.google.inject.Inject;
import com.google.inject.Injector;
import net.spikedboy.guerrilla.GuerrillaPlugin;
import net.spikedboy.guerrilla.configuration.GuerrillaConfigurations;
import net.spikedboy.guerrilla.landclaim.DelayedClaimData;
import net.spikedboy.guerrilla.landclaim.DelayedClaimRunner;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Logger;

public class Guerrilla implements Serializable {

    private static final Logger LOGGER = Logger.getLogger("Minecraft");

    private static final long serialVersionUID = 5277397633585310503L;

    @Inject
    private transient GuerrillaManager guerrillaManager;

    @Inject
    private transient GuerrillaPlugin guerrillaPlugin;

    @Inject
    private transient Messager messager;

    @Inject
    private transient Server server;

    @Inject
    private transient Injector injector;

    private final ArrayList<ArrayList<Integer>> territories = new ArrayList<>();
    private final ArrayList<String> players = new ArrayList<>();
    private final ArrayList<ArrayList<Integer>> paymentChests = new ArrayList<>();

    private final ArrayList<String> invites = new ArrayList<>();

    private String leader;
    private Date date;
    private Date quitPunishmentDate;

    private int numberOfClaimedChunks = 0;

    private ArrayList<ArrayList<Integer>> safeChest = new ArrayList<>(); //chest block xzy

    private String name;
    private Date antiSpam;


    public Guerrilla() {
    }

    public Guerrilla(Player player, String args) {
        initiateNewGuerrilla(player, args);
    }

    public void initiateNewGuerrilla(Player player, String args) {
        players.add(player.getName());
        leader = player.getName();
        name = args;
        messager.sendMessage(player.getName() + " created the " + name + " guerrilla!");
        try {
            guerrillaManager.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isBeingClaimed() {
        return guerrillaManager.getDelayedClaimDataQueue().search(this, 1) != null;
    }

    int adyChunks(Chunk chunk) {
        int n = 0;
        if (guerrillaManager.getGuerrillaChunk(chunk.getBlock(1, 1, 1).getRelative(BlockFace.NORTH, 17).getChunk()) == this) {
            n++;
        }
        if (guerrillaManager.getGuerrillaChunk(chunk.getBlock(1, 1, 1).getRelative(BlockFace.SOUTH, 17).getChunk()) == this) {
            n++;
        }
        if (guerrillaManager.getGuerrillaChunk(chunk.getBlock(1, 1, 1).getRelative(BlockFace.EAST, 17).getChunk()) == this) {
            n++;
        }
        if (guerrillaManager.getGuerrillaChunk(chunk.getBlock(1, 1, 1).getRelative(BlockFace.WEST, 17).getChunk()) == this) {
            n++;
        }
        return n;
    }

    public void changeLeader(Player sender, String nleader) {
        Guerrilla guerrillas = guerrillaManager.getPlayerGuerrilla(sender);
        if (guerrillas == null) {
            sender.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "You have no guerrilla");
            return;
        }
        String gsleader = guerrillas.getLeader();
        String sname = sender.getName();
        Guerrilla nlg = guerrillaManager.getPlayerGuerrilla(server.getPlayerExact(nleader));
        if (nlg == null) {
            sender.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "That player has no guerrilla!");
            return;
        } else if (!(nlg.equals(guerrillas))) {
            sender.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "That player is not in your guerrilla");
            return;
        }
        if (sname.equals(gsleader)) {
            guerrillas.leader = nleader;
            msggue("The new leader is " + nleader + "!");
        } else {
            sender.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "Only the leader can do that");
        }
    }

    public void addSafeChest(Chest chest, Player sender) {
        ArrayList<Integer> chlist = guerrillaManager.ChestToList(chest);

        if (guerrillaManager.getGuerrillaChunk(chest.getBlock().getChunk()) != guerrillaManager.getPlayerGuerrilla(sender)) {
            sender.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "You can't make a safechest outside of your territory");
            return;
        }

        if (!guerrillaManager.isLeader(sender)) {
            sender.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "You are not leader");
            guerrillaManager.getPlayerSetsSafe().remove(sender.getName());
            return;
        }
        if (safeChest == null) safeChest = new ArrayList<>();
        int np = countPlayers();
        if (GuerrillaConfigurations.minimumPlayersNeededToAllowASafeChest > np) {
            sender.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "You can't have a safe chest if your guerrilla has less than "
                    + GuerrillaConfigurations.minimumPlayersNeededToAllowASafeChest + " members");
            return;
        }
        if (safeChest.isEmpty()) {
            safeChest.add(chlist);
            sender.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "Safe chest added!");
            guerrillaManager.getPlayerSetsSafe().remove(sender.getName());
            return;
        }
        sender.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "You already have a safechest");
        guerrillaManager.getPlayerSetsSafe().remove(sender.getName());
    }

    public void removeSafeChest(Chest chest, Player sender) {
        ArrayList<Integer> chlist = guerrillaManager.ChestToList(chest);
        if (safeChest.isEmpty()) {
            sender.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "You have no chests!");
            guerrillaManager.getPlayerSetsSafe().remove(sender.getName());
            return;
        } else if (safeChest.contains(chlist)) {
            sender.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "Safe chest removed");
            guerrillaManager.getPlayerSetsSafe().remove(sender.getName());
            safeChest.remove(chlist);
            return;
        }
        sender.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "That is not your chest");
        guerrillaManager.getPlayerSetsSafe().remove(sender.getName());
    }

    private int countPlayers() {
        return players.size();
    }

    private boolean isThereAtLeastGuerrillaMemberOnline() {
        for (String pname : players) {
            Collection<? extends Player> onlinePlayers = server.getOnlinePlayers();
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
            Collection<? extends Player> onlinePlayers = server.getOnlinePlayers();
            for (Player p1 : onlinePlayers) {
                if (p1.getName().equals(pname)) {
                    i++;
                }
            }

        }
        return i;
    }

    public String getOnlinePlayersText() {
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
        ArrayList<Integer> al1 = guerrillaManager.ChestToList(chest);
        if (!paymentChests.contains(al1)) {
            return false;
        }
        paymentChests.remove(al1);
        if (getdchest(chest) != null) {
            paymentChests.remove(guerrillaManager.ChestToList(getdchest(chest)));
        }
        return true;
    }

    public boolean addPaymentChest(Chest chest) {
        ArrayList<Integer> al1 = guerrillaManager.ChestToList(chest);
        if (paymentChests.contains(al1)) {
            return false;
        }
        paymentChests.add(al1);
        if (getdchest(chest) != null) {
            paymentChests.add(guerrillaManager.ChestToList(getdchest(chest)));
        }
        return true;
    }

    public void removePayment() {
        int price = (territories.size() / GuerrillaConfigurations.nchunkpay) * GuerrillaConfigurations.chunkmaintprice;
        boolean paid = false;
        for (ArrayList<Integer> chestc : paymentChests) {
            if (server.getWorld(GuerrillaConfigurations.gworldname).getBlockAt(chestc.get(0),
                    chestc.get(1), chestc.get(2)).getType() != Material.CHEST) {
                msggue("A payment chest is missing and has been marked for removal");
                continue;
            }
            Chest chest = ((Chest) (server.getWorld(GuerrillaConfigurations.gworldname).getBlockAt(chestc.get(0), chestc.get(1), chestc.get(2)).getState()));
            if ((chest != null) && (chest.getInventory().contains(GuerrillaConfigurations.itemidmaint, GuerrillaConfigurations.chunkmaintprice))) {
                if (price > GuerrillaConfigurations.maintmaxprice) price = GuerrillaConfigurations.maintmaxprice;
                guerrillaManager.removeInventoryItems(chest.getInventory(), Material.getMaterial(GuerrillaConfigurations.itemidmaint), (price));
                paid = true;
                msggue("Payments issued, cost: " + price + ", thank you");
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
            if (server.getPlayer(playern) == null) continue;
            if (server.getPlayer(playern).getLocation().getBlock().getChunk().equals(chunk)) {
                return true;
            }
        }
        return false;
    }

    public boolean claim(Chunk chunk, Player claimer) {
        if (guerrillaManager.isSafeChunk(chunk)) {
            claimer.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "Can't claim a safe chunk");
            return false;
        }
        if (guerrillaManager.getPlayerGuerrilla(claimer) != null) {
            ArrayList<Integer> clist = new ArrayList<>(2);
            int chunkX = chunk.getX();
            int chunkZ = chunk.getZ();
            clist.add(chunkX);
            clist.add(chunkZ);
            if ((territories.contains(clist))) {
                claimer.sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY + "That chunk is already claimed");
                return false;
            } else if (!(claimer.getInventory().contains(GuerrillaConfigurations.itemid, GuerrillaConfigurations.chunkprice))) {
                claimer.sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY + "The required price is "
                        + GuerrillaConfigurations.chunkprice + " of " + (Material.getMaterial(GuerrillaConfigurations.itemid).name())
                        + " and you don't have it");
                return false;
            } else {
                if (canClaim(chunk, claimer)) {
                    if (guerrillaManager.getGuerrillaChunk(chunk) != this && guerrillaManager.getGuerrillaChunk(chunk) != null) {
                        if (!claimer.getInventory().contains(GuerrillaConfigurations.itemid, GuerrillaConfigurations.chunkprice * GuerrillaConfigurations.conqmulti)) {
                            claimer.sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY + "The required price is "
                                    + (GuerrillaConfigurations.chunkprice * GuerrillaConfigurations.conqmulti) + " of "
                                    + (Material.getMaterial(GuerrillaConfigurations.itemid).name()) + " and you don't have it");

                            return false;
                        }

                        if (guerrillaManager.getDelayedClaimDataQueue().search(claimer.getName()) != null) {
                            claimer.sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY
                                    + "Your faction is already claiming an enemy chunk! You can't do that twice");
                            return true;
                        }
                        delayedClaim(guerrillaManager.getGuerrillaChunk(chunk), chunk, claimer, guerrillaPlugin);
                        guerrillaManager.getGuerrillaChunk(chunk).msggue(this.getName() + " is claiming part of your territory! Coords: "
                                + chunk.getBlock(0, 0, 0).getX() + "," + chunk.getBlock(0, 0, 0).getZ() + " (x,z)");
                        msggue("Claiming enemy area, if you leave the chunk you will loose this dispute!");
                        return true;
                    }
                    guerrillaManager.removeInventoryItems(claimer.getInventory(), Material.getMaterial(GuerrillaConfigurations.itemid), GuerrillaConfigurations.chunkprice);
                    numberOfClaimedChunks++;
                    territories.add(clist);
                    messager.sendMessage(this.getName() + " claimed some terrain");
                    //claimer.sendMessage("Chunk claimed");
                    return true;
                } else {
                    claimer.sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY + "You can't claim this chunk");
                    return false;
                }
            }
        }
        return false;
    }

    private boolean canClaim(Chunk chunk, Player claimer) {
        Guerrilla owner = guerrillaManager.getGuerrillaChunk(chunk);
        Guerrilla gclaimer = guerrillaManager.getPlayerGuerrilla(claimer);
        Date now = new Date();
        if ((owner != this) && (owner != null)) {
            if (owner.date == null) owner.date = now;
            if (((now.getTime() - owner.date.getTime()) >= GuerrillaConfigurations.expTime) && ((owner.adyChunks(chunk) <= 2)
                    && (gclaimer.adyChunks(chunk) >= 1))) {
                return true;
            }
            if ((owner.quitPunishmentDate != null) && ((now.getTime() - owner.quitPunishmentDate.getTime()) < GuerrillaConfigurations.delay)
                    && ((owner.adyChunks(chunk) <= 2) && (gclaimer.adyChunks(chunk) >= 1))) {
                return true;
            }
            if (!owner.isThereAtLeastGuerrillaMemberOnline()) return false;
            if (owner.somebodyHome(chunk)) return false;
            if ((owner.adyChunks(chunk) <= 2) && (gclaimer.adyChunks(chunk) >= 1)) {
                return true;
            }
        } else if (owner == null) {
            if (this.adyChunks(chunk) >= 1) {
                return true;
            } else if (this.adyChunks(chunk) == 0) {
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
            claimer.sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY + "Chunk unclaimed");
            return true;
        }
        claimer.sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY + "You don't own this chunk");
        return false;
    }

    public boolean unclaimall(Player sender) {
        if (sender.getName().equals(leader)) {
            numberOfClaimedChunks = 0;
            territories.clear();
            msggue("All territories have been unclaimed!");
            return true;
        }
        sender.sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY + "You are not leader");
        return true;
    }

    public void msggue(String msg) {
        for (String playername : players) {

            if (server.getPlayer(playername) != null) {
                server.getPlayer(playername).sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY + msg);
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
            Player player = server.getPlayer(playername);
            //GuerrillaPlugin.LOGGER.info("deb " + antiSpam.getTime());
            if (player != null) {
                player.sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY + msg);
            }
        }
    }

    public boolean join(Player player) {
        if (invites.contains(player.getName())) {
            msggue(player.getName() + " has joined your [Guerrilla]");
            players.add(player.getName());
            invites.remove(player.getName());
            player.sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY + "You are now part of the "
                    + this.getName() + " guerrilla");
            return true;
        } else {
            player.sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY + "You have not been invited to this faction");
            return false;
        }
    }

    public boolean kick(String player, Player sender) {
        if (guerrillaManager.isLeader(sender)) {
            if (guerrillaManager.getPlayerGuerrilla(player) == guerrillaManager.getPlayerGuerrilla(sender)) {
                players.remove(player);
                msggue(player + " has been kicked");
                if (server.getPlayer(player) != null) {
                    server.getPlayer(player).sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY
                            + sender.getName() + " has kicked you");
                }
                return true;
            }
            sender.sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY + "That player is not in your guerrilla");
            return false;
        }
        sender.sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY + "You are not a guerrilla leader");
        return false;
    }

    public boolean invite(Player player, CommandSender sender) {
        if (!(invites.contains(player.getName())) && ((guerrillaManager.getPlayerGuerrilla(player)) == null)) {
            invites.add(player.getName());
            sender.sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY + player.getName() + " has been invited to your guerrilla");
            player.sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY + "You have been invited to the " + this.getName() + " guerrilla");
            return true;
        }
        sender.sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY + "That player can't get invited");
        return false;
    }

    public boolean inviteCancel(Player player, Player sender) {
        if ((invites.contains(player.getName())) && (guerrillaManager.isLeader(sender))) {
            invites.remove(player.getName());
            sender.sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY + "You canceled that invite");
            return true;
        }
        sender.sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY + "You can't do that");
        return false;
    }

    public boolean ownsGuerrillaChunk(Chunk chunk) {
        ArrayList<Integer> clist = new ArrayList<>(2);
        Integer chunkX = chunk.getX();
        Integer chunkZ = chunk.getZ();
        clist.add(chunkX);
        clist.add(chunkZ);
        if (guerrillaManager.getGuerrillaChunk(chunk) == null) {
            return true;
        } else {
            return (territories.contains(clist));
        }
    }

    public boolean disband(Player player) {
        if (this.getLeader().equals(player.getName())) {
            msggue("Your guerrilla has been disbanded!");
            LOGGER.info(this.getName() + " disbanded");
            players.clear();
            territories.clear();
            invites.clear();
            name = "";
            leader = "";
            numberOfClaimedChunks = 0;
            guerrillaManager.getGuerrillaList().remove(this);
            return true;
        }
        player.sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY + "You do not own this guerrilla");
        return false;
    }

    private void delayedClaim(final Guerrilla gowner, final Chunk chunk, final Player claimer, GuerrillaPlugin guerrillaPlugin) {
        ArrayList<Integer> clistn = new ArrayList<>(2);

        Integer chunkX = chunk.getX();
        Integer chunkZ = chunk.getZ();

        clistn.add(chunkX);
        clistn.add(chunkZ);

        final ArrayList<Integer> clist = clistn;

        DelayedClaimRunner task = injector.getInstance(DelayedClaimRunner.class);

        task.setClaimer(claimer);
        task.setGowner(gowner);
        task.setClist(clist);
        task.setGclaimer(this);

        final Integer dclaimid = server.getScheduler()
                .scheduleSyncDelayedTask(guerrillaPlugin, task,
                        GuerrillaConfigurations.conqdelay);

        guerrillaManager.getDelayedClaimDataQueue().addNode(new DelayedClaimData(clist, this, gowner, claimer.getName(), dclaimid));
    }

    public String getLeader() {
        return leader;
    }

    public String getName() {
        return name;
    }

    public void setGuerrillaPlugin(GuerrillaPlugin guerrillaPlugin) {
        this.guerrillaPlugin = guerrillaPlugin;
    }

    public void setGuerrillaManager(GuerrillaManager guerrillaManager) {
        this.guerrillaManager = guerrillaManager;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public ArrayList<ArrayList<Integer>> getTerritories() {
        return territories;
    }

    public ArrayList<String> getPlayers() {
        return players;
    }

    public ArrayList<ArrayList<Integer>> getPaymentChests() {
        return paymentChests;
    }

    public ArrayList<String> getInvites() {
        return invites;
    }

    public void setLeader(String leader) {
        this.leader = leader;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getQuitPunishmentDate() {
        return quitPunishmentDate;
    }

    public void setQuitPunishmentDate(Date quitPunishmentDate) {
        this.quitPunishmentDate = quitPunishmentDate;
    }

    public int getNumberOfClaimedChunks() {
        return numberOfClaimedChunks;
    }

    public void setNumberOfClaimedChunks(int numberOfClaimedChunks) {
        this.numberOfClaimedChunks = numberOfClaimedChunks;
    }

    public ArrayList<ArrayList<Integer>> getSafeChest() {
        return safeChest;
    }

    public void setSafeChest(ArrayList<ArrayList<Integer>> safeChest) {
        this.safeChest = safeChest;
    }

    public void setName(String name) {
        this.name = name;
    }
}
