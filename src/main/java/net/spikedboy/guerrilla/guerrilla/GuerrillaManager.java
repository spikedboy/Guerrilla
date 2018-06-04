package net.spikedboy.guerrilla.guerrilla;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.spikedboy.guerrilla.configuration.GuerrillaConfigurations;
import net.spikedboy.guerrilla.landclaim.DelayedClaimData;
import net.spikedboy.guerrilla.landclaim.DelayedClaimDataQueue;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@Singleton
public class GuerrillaManager {

    private static final Logger LOGGER = Logger.getLogger("Minecraft");

    private static final String GUERRILLA_SAFE_BIN = "GuerrillaSafe.bin";
    private static String winnerGuerrillaName;
    private static boolean stateWon;
    private static final String SAVED_GUERRILLAS_FILENAME = "Guerrilla.bin";

    @Inject
    private Messager messager;

    private final Map<String, Boolean> playerSetsSafe = new HashMap<>();
    private final DelayedClaimDataQueue delayedClaimDataQueue = new DelayedClaimDataQueue();
    private final Map<String, Boolean> togglePlayerChat = new HashMap<>();
    private final Map<String, Boolean> playerSetsBlock = new HashMap<>();

    private ArrayList<Guerrilla> guerrillaList = new ArrayList<>();
    private ArrayList<ArrayList<Integer>> safeChunks = new ArrayList<>();

    public static String getGuerrillaSafeBin() {
        return GUERRILLA_SAFE_BIN;
    }

    public static String getWinnerGuerrillaName() {
        return winnerGuerrillaName;
    }

    public static void setWinnerGuerrillaName(String winnerGuerrillaName) {
        GuerrillaManager.winnerGuerrillaName = winnerGuerrillaName;
    }

    public static boolean isStateWon() {
        return stateWon;
    }

    public static void setStateWon(boolean stateWon) {
        GuerrillaManager.stateWon = stateWon;
    }

    public Guerrilla getGuerrillaByName(String name) {
        for (Guerrilla guerrilla : guerrillaList) {
            if (guerrilla.getName().equals(name)) {
                return guerrilla;
            }
        }
        return null;
    }

    public Guerrilla checkWinners() {
        for (Guerrilla guerrilla : guerrillaList) {
            if (guerrilla.getNumberOfClaimedChunks() >= GuerrillaConfigurations.objectiveChunks) {
                return guerrilla;
            }
        }
        return null;
    }

    public void printmap() throws IOException {
        FileWriter fstream = new FileWriter("coords.txt");
        BufferedWriter out = new BufferedWriter(fstream);
        int contador = 0;
        for (Guerrilla guerrilla : guerrillaList) {
            for (ArrayList<Integer> clist : guerrilla.getTerritories()) {
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

    public void who(Player sender, String args) {
        Date now = new Date();
        if (args == null) {
            Guerrilla guerrilla = getPlayerGuerrilla(sender);
            if (guerrilla == null) {
                sender.sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY + "You have no guerrilla");
                return;
            }
            if (guerrilla.getDate() == null) guerrilla.setDate(now);
            sender.sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY + guerrilla.getName() + "'s info:");
            sender.sendMessage(ChatColor.DARK_AQUA + " Chunks claimed: " + ChatColor.WHITE + guerrilla.getNumberOfClaimedChunks());
            sender.sendMessage(ChatColor.DARK_AQUA + " Payment chests: " + ChatColor.WHITE + guerrilla.getPaymentChests().size());
            sender.sendMessage(ChatColor.DARK_AQUA + " Last login: " + ChatColor.WHITE
                    + ((now.getTime() - guerrilla.getDate().getTime()) / 3600000) + ChatColor.DARK_AQUA + "h ago");

            sender.sendMessage(ChatColor.DARK_AQUA + " Members:");

            for (String playername : guerrilla.getPlayers()) {
                if (guerrilla.getLeader() == playername) {
                    sender.sendMessage("  " + playername + (ChatColor.LIGHT_PURPLE + " (leader)"));
                } else {
                    sender.sendMessage("  " + playername);
                }
            }
        } else {
            Guerrilla guerrilla = getGuerrillaByName(args);
            if (guerrilla == null) {
                sender.sendMessage(args + " doesn't exist");
                return;
            } else {
                if (guerrilla.getDate() == null) guerrilla.setDate(now);
                sender.sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY + guerrilla.getName() + "'s info:");
                sender.sendMessage(ChatColor.DARK_AQUA + " Chunks claimed: " + ChatColor.WHITE + guerrilla.getNumberOfClaimedChunks());
                sender.sendMessage(ChatColor.DARK_AQUA + " Payment chests: " + ChatColor.WHITE + guerrilla.getPaymentChests().size());
                sender.sendMessage(ChatColor.DARK_AQUA + " Last login: " + ChatColor.WHITE
                        + ((now.getTime() - guerrilla.getDate().getTime()) / 3600000) + ChatColor.DARK_AQUA + "h ago");
                if (guerrilla.getQuitPunishmentDate() != null) {
                    sender.sendMessage(ChatColor.DARK_AQUA + " Punished: " + ChatColor.WHITE
                            + (((now.getTime() - guerrilla.getQuitPunishmentDate().getTime()) < GuerrillaConfigurations.delay)));
                }
                sender.sendMessage(ChatColor.DARK_AQUA + " Members:");
                for (String playername : guerrilla.getPlayers()) {
                    if (guerrilla.getLeader().equals(playername)) {
                        sender.sendMessage("  " + playername + (ChatColor.LIGHT_PURPLE + " (leader)"));
                    } else {
                        sender.sendMessage("  " + playername);
                    }
                }
            }
        }
    }

    public Guerrilla getGuerrillaChunk(Chunk chunk) {
        ArrayList<Integer> clist = new ArrayList<>(2);
        Integer chunkX = chunk.getX();
        Integer chunkZ = chunk.getZ();
        clist.add(chunkX);
        clist.add(chunkZ);
        for (Guerrilla guerrilla : guerrillaList) {
            if (guerrilla.getTerritories().contains(clist)) {
                return guerrilla;
            }
        }
        return null;
    }

    public Guerrilla getPlayerGuerrilla(Player player) {
        for (Guerrilla guerrilla : guerrillaList) {
            if (guerrilla.getPlayers().contains(player.getName())) {
                return guerrilla;
            }
        }
        return null;
    }

    Guerrilla getPlayerGuerrilla(String player) {
        for (Guerrilla guerrilla : guerrillaList) {
            if (guerrilla.getPlayers().contains(player)) {
                return guerrilla;
            }
        }
        return null;
    }

    public boolean isLeader(Player player) {
        Guerrilla guerrilla = getPlayerGuerrilla(player);
        //GuerrillaPlugin.LOGGER.info(player.getName() + " " + guerrilla.leader + " " + guerrilla.getLeader() + " " + guerrilla.leader.equals(player.getName()));
        return guerrilla.getLeader().equals(player.getName());
    }

    public boolean leave(Player sender) {
        if (isLeader(sender)) {
            sender.sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY + "The leader can't leave! Disband!");
            return true;
        }
        Guerrilla guerrilla = getPlayerGuerrilla(sender);
        if ((guerrilla != null) && ((!isLeader(sender)))) {
            guerrilla.getPlayers().remove(sender.getName());
            sender.sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY + "You left the "
                    + guerrilla.getName() + " guerrilla!");
            return true;
        }
        sender.sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY + "You can't do that!");
        return false;
    }

    public boolean inviteDecline(Player sender, String args) {
        Guerrilla guerrilla = getGuerrillaByName(args);
        if (guerrilla == null) {
            sender.sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY + "That guerrilla doesn't exist");
            return false;
        }
        if (guerrilla.getInvites().contains(sender.getName())) {
            guerrilla.msggue(sender.getName() + "Declined his invite");
            guerrilla.getInvites().remove(sender.getName());
            sender.sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY + "Invitation declined");
            return true;
        }
        sender.sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY + "You have no pending invites");
        return false;
    }

    void saveFunc(Object o, String path) throws Exception {
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path));
        oos.writeObject(o);
        oos.flush();
        oos.close();
    }

    public Object loadFunc(String path) throws Exception {
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path));
        Object result = ois.readObject();
        ois.close();
        return result;
    }

    public void removeInventoryItems(Inventory inv, Material type, int amount) {
        LOGGER.info("cantidad total: " + amount);
        for (ItemStack is : inv.getContents()) {
            if ((is != null) && (is.getType() == type)) {
                int newamount = is.getAmount() - amount;
                LOGGER.info("stack - total: " + newamount);
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

    ArrayList<Integer> ChestToList(Chest chest) {
        ArrayList<Integer> chlist = new ArrayList<>(3);
        Integer ChX = chest.getX();
        Integer ChY = chest.getY();
        Integer ChZ = chest.getZ();
        chlist.add(ChX);
        chlist.add(ChY);
        chlist.add(ChZ);
        return chlist;
    }

    public void save() throws Exception {
        saveFunc(guerrillaList, SAVED_GUERRILLAS_FILENAME);
        saveFunc(safeChunks, "GuerrillaSafe.bin");
    }

    public void map(Block block, Player sender) {
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

    public void setSafeChunk(Chunk chunk) {
        Guerrilla owner = getGuerrillaChunk(chunk);

        ArrayList<Integer> clist = new ArrayList<>(2);
        Integer chunkX = chunk.getX();
        Integer chunkZ = chunk.getZ();
        clist.add(chunkX);
        clist.add(chunkZ);
        if (owner != null) {
            owner.getTerritories().remove(clist);
        }
        if (safeChunks.contains(clist)) {
            return;
        }
        safeChunks.add(clist);
    }

    public boolean isSafeChunk(Chunk chunk) {
        ArrayList<Integer> clist = new ArrayList<>(2);
        Integer chunkX = chunk.getX();
        Integer chunkZ = chunk.getZ();
        clist.add(chunkX);
        clist.add(chunkZ);
        return safeChunks.contains(clist);
    }

    public void removeSafeChunk(Chunk chunk) {
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

    public int getClaimingID(String pname) {
        DelayedClaimData dcd = delayedClaimDataQueue.search(pname);
        if (dcd != null) {
            return (dcd.getThreadID());
        } else {
            return -1;
        }
    }

    public void List(Player sender, int page) {
        sortGList();

        if (page <= 0) page = 1;
        int index = (page - 1) * 7;
        sender.sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.WHITE + "Listing Guerrillas:" + ChatColor.GRAY
                + " (Chunk objective: " + GuerrillaConfigurations.objectiveChunks + ") " + ChatColor.WHITE + "PAGE " + page);
        for (int i = index; i < (guerrillaList.size()); i++) {
            if (i > (guerrillaList.size() - 1)) break;
            Guerrilla guerrilla = guerrillaList.get(i);
            sender.sendMessage("- " + guerrilla.getName() + ChatColor.LIGHT_PURPLE + " Leader: " + ChatColor.WHITE
                    + guerrilla.getLeader() + ChatColor.LIGHT_PURPLE + " Chunks: " + ChatColor.WHITE + guerrilla.getNumberOfClaimedChunks()
                    + guerrilla.getOnlinePlayersText());
        }
    }

    private void sortGList() {
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

    public Guerrilla getGuerrillaSafeChest(Chest chest) {
        for (Guerrilla g1 : guerrillaList) {
            if (g1.getSafeChest() == null) continue;
            if (g1.getSafeChest().isEmpty()) continue;
            ArrayList<Integer> chlist = ChestToList(chest);
            if (g1.getSafeChest().get(0).equals(chlist)) {
                return getGuerrillaChunk(chest.getBlock().getChunk());
            }
        }
        return null;
    }


    public ArrayList<Guerrilla> getGuerrillaList() {
        return guerrillaList;
    }

    public void setGuerrillaList(ArrayList<Guerrilla> guerrillaList) {
        this.guerrillaList = guerrillaList;
    }

    public ArrayList<ArrayList<Integer>> getSafeChunks() {
        return safeChunks;
    }

    public void setSafeChunks(ArrayList<ArrayList<Integer>> safeChunks) {
        this.safeChunks = safeChunks;
    }

    public void load() throws Exception {
        File gFile = new File(SAVED_GUERRILLAS_FILENAME);
        File sChunks = new File("GuerrillaSafe.bin");

        if (gFile.exists()) {
            setGuerrillaList((ArrayList<Guerrilla>) loadFunc(SAVED_GUERRILLAS_FILENAME));
        } else {
            gFile.createNewFile();
        }

        if (sChunks.exists()) {
            setSafeChunks((ArrayList<ArrayList<Integer>>) loadFunc(GUERRILLA_SAFE_BIN));
        } else {
            sChunks.createNewFile();
        }
    }

    public void chargeGuerrillasMaintenance() {
        messager.sendMessage("Issuing payments...");

        for (Guerrilla guerrilla : getGuerrillaList()) {
            guerrilla.removePayment();
        }

        LOGGER.info("[Guerrilla] Done");

        try {
            save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setMessager(Messager messager) {
        this.messager = messager;
    }

    public Map<String, Boolean> getPlayerSetsSafe() {
        return playerSetsSafe;
    }

    public DelayedClaimDataQueue getDelayedClaimDataQueue() {
        return delayedClaimDataQueue;
    }

    public Map<String, Boolean> getTogglePlayerChat() {
        return togglePlayerChat;
    }

    public Map<String, Boolean> getPlayerSetsBlock() {
        return playerSetsBlock;
    }
}
