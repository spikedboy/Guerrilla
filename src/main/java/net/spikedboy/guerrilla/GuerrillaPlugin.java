package net.spikedboy.guerrilla;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import net.spikedboy.guerrilla.commands.CommandExecutor;
import net.spikedboy.guerrilla.configuration.GuerrillaConfigurations;
import net.spikedboy.guerrilla.guice.GuerrillaModule;
import net.spikedboy.guerrilla.landclaim.DelayedClaimData;
import net.spikedboy.guerrilla.landclaim.DelayedClaimDataQueue;
import net.spikedboy.guerrilla.listeners.GuerrillaBlockListener;
import net.spikedboy.guerrilla.listeners.GuerrillaEntityListener;
import net.spikedboy.guerrilla.listeners.GuerrillaPlayerListener;
import net.spikedboy.guerrilla.listeners.GuerrillaWorldListener;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class GuerrillaPlugin extends JavaPlugin {

    public static final Map<String, Boolean> playerSetsBlock = new HashMap<>();

    public static final Map<String, Boolean> playerSetsSafe = new HashMap<>();

    public static final DelayedClaimDataQueue delayedClaimDataQueue = new DelayedClaimDataQueue();

    public static final Map<String, Boolean> togglePlayerChat = new HashMap<>();

    public static final Logger log = Logger.getLogger("Minecraft");

    public static Server serverInstance;

    private static long initFirstPaymentDelay;

    public static String gwinnerName;

    public static final String gCh = ChatColor.DARK_RED + "[GuerrillaPlugin] " + ChatColor.GRAY;

    private static GuerrillaPlugin ginst;

    public static boolean stateWon;

    private static int paymentThreadId;

    private FileConfiguration config;

    private CommandExecutor commandExecutor;

    @Override
    public void onEnable() {
        initialiceGuiceDependencyInjectionSystem();

        initialiceServerandGuerrillaInstances();
        saveDefaultConfig();
        config = getConfig();

        readConfigIntoVars();

        if (!doesConfiguratedMapNameExist()) {
            return;
        }

        initFirstPaymentDelay = GuerrillaConfigurations.delay - serverInstance.getWorld(GuerrillaConfigurations.gworldname).getTime();
        log.info("[GuerrillaPlugin] Time for the first delay: " + initFirstPaymentDelay);

        PluginManager pm = serverInstance.getPluginManager();
        registerListeners(pm);

        loadSavedDataFromFile();

        removeMissingPaymentChestsHandleConcurrentModificationException();

        initPaymentThread();
    }

    private void initialiceGuiceDependencyInjectionSystem() {
        GuerrillaModule guerrillaModule = new GuerrillaModule();
        guerrillaModule.setGuerrillaPluginSingletonInstance(this);

        Injector injector = Guice.createInjector(guerrillaModule);
        injector.injectMembers(this);
    }

    private void initialiceServerandGuerrillaInstances() {
        Server server = getServer();

        if (server != null) {
            serverInstance = server;
            ginst = this;
        } else {
            log.info("Server == null");
        }
    }

    private void readConfigIntoVars() {
        GuerrillaConfigurations.itemid = config.getInt("guerrilla.itemid", 296);
        GuerrillaConfigurations.chunkprice = config.getInt("guerrilla.chunkprice", 16);
        GuerrillaConfigurations.delay = (long) config.getInt("guerrilla.paymenttime", 23999);
        GuerrillaConfigurations.itemidmaint = config.getInt("guerrilla.iditemmaintennance", 296);
        GuerrillaConfigurations.chunkmaintprice = config.getInt("guerrilla.chunkmaintennanceprice", 1);
        GuerrillaConfigurations.gworldname = config.getString("guerrilla.gworldname", "world");
        log.info("[GuerrillaPlugin] World name: " + GuerrillaConfigurations.gworldname);
        GuerrillaConfigurations.conqdelay = (long) config.getInt("guerrilla.conquestdelay", 6000);
        GuerrillaConfigurations.conqmulti = config.getInt("guerrilla.conquestpricemultiplier", 2);
        GuerrillaConfigurations.defbonus = config.getInt("guerrilla.defenderdamagetakendivider", 2);
        GuerrillaConfigurations.atkbonus = config.getInt("guerrilla.defenderdamagedealtmultiplier", 1);
        GuerrillaConfigurations.nchunkpay = config.getInt("guerrilla.nchunksminmaintenance", 10);
        GuerrillaConfigurations.objectiveChunks = config.getInt("guerrilla.chunkobjective", 780);
        GuerrillaConfigurations.maintmaxprice = config.getInt("guerrilla.maxmaintenanceprice", 44);
        GuerrillaConfigurations.minPSC = config.getInt("guerrilla.miniumplayersforasafechest", 5);
        GuerrillaConfigurations.expTime = config.getInt("guerrilla.guerrillaexpirytime", 604800000);
        GuerrillaConfigurations.tntProtection = config.getBoolean("guerrilla.explostionProtection", false);

        gwinnerName = config.getString("guerrilla.winner", "");
        stateWon = config.getBoolean("guerrilla.ismatchwon", false);
    }

    private void registerListeners(PluginManager pm) {
        pm.registerEvents(new GuerrillaBlockListener(this), this);
        pm.registerEvents(new GuerrillaEntityListener(this), this);
        pm.registerEvents(new GuerrillaPlayerListener(this), this);
        pm.registerEvents(new GuerrillaWorldListener(this), this);
    }

    private void loadSavedDataFromFile() {
        try {
            Guerrilla.load();
        } catch (FileNotFoundException e) {
            log.warning("[GuerrillaPlugin] some guerrilla file is missing, bad things might happen...");
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("[GuerrillaPlugin] GuerrillaPlugin loaded");
    }

    private void removeMissingPaymentChestsHandleConcurrentModificationException() {
        try {
            removeMissingPaymentChests();
        } catch (ConcurrentModificationException e) {
            e.printStackTrace();
        }
    }

    private void removeMissingPaymentChests() {
        for (Guerrilla guerrilla : Guerrilla.guerrillaList) {
            for (ArrayList<Integer> chestc : guerrilla.paymentChests) {
                if (serverInstance.getWorld(GuerrillaConfigurations.gworldname).getBlockAt(chestc.get(0).intValue(),
                        chestc.get(1).intValue(), chestc.get(2).intValue()).getType() != Material.CHEST) {

                    guerrilla.paymentChests.remove(chestc);
                    guerrilla.msggue("A missing paymentchest was removed");
                    log.info("[GuerrillaPlugin] A paymentchest was removed because the chest was missing");
                }
            }
        }
    }

    private void initPaymentThread() {
        paymentThreadId = this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {
                if (Guerrilla.checkWinners() != null) {
                    stateWon = true;
                    gwinnerName = Guerrilla.checkWinners().getName();
                    config.set("guerrilla.ismatchwon", true);
                    config.set("guerrilla.winner", gwinnerName);
                    ginst.saveConfig();
                    serverInstance.broadcastMessage(ChatColor.GOLD + "[GuerrillaWrittenInGold] " + gwinnerName + " HAS WON THIS MATCH!!!!!! GOOD DAY TO YOU");
                    return;
                }
                Guerrilla.chargeMaintenance();
                try {
                    Guerrilla.printmap();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, initFirstPaymentDelay, GuerrillaConfigurations.delay);
    }

    @Override
    public void onDisable() {
        cancelPaymentThread();
        saveStateToFile();
        removeMissingPaymentChests();

        log.info("[GuerrillaPlugin] GuerrillaPlugin disabled");

        saveConfig();
    }

    private void cancelPaymentThread() {
        serverInstance.getScheduler().cancelTask(paymentThreadId);
    }

    private void saveStateToFile() {
        try {
            Guerrilla.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean doesConfiguratedMapNameExist() {
        if (serverInstance.getWorld(GuerrillaConfigurations.gworldname) == null) {
            log.info("[GuerrillaPlugin] El nombre del mapa especificado es erroneo! Cambialo y reinicia");
            return false;
        }
        return true;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        return commandExecutor.processCommands(sender, cmd, commandLabel, args);
    }

    public static void delayedClaim(final Guerrilla gclaimer, final Guerrilla gowner, final Chunk chunk, final Player claimer) {

        ArrayList<Integer> clistn = new ArrayList<>(2);

        Integer chunkX = chunk.getX();
        Integer chunkZ = chunk.getZ();

        clistn.add(chunkX);
        clistn.add(chunkZ);

        final ArrayList<Integer> clist = clistn;

        final Integer dclaimid = serverInstance.getScheduler().scheduleSyncDelayedTask(ginst, new Runnable() {
            public void run() {
                DelayedClaimData dcd = delayedClaimDataQueue.search(claimer.getName());

                if (claimer.getInventory().contains(GuerrillaConfigurations.itemid, (GuerrillaConfigurations.chunkprice * GuerrillaConfigurations.conqmulti))) {
                    Guerrilla.removeInventoryItems(claimer.getInventory(), Material.getMaterial(GuerrillaConfigurations.itemid),
                            (GuerrillaConfigurations.chunkprice * GuerrillaConfigurations.conqmulti));
                } else {
                    claimer.sendMessage(gCh + "You pig! You dropped the payment? you don't get the chunk!");
                    delayedClaimDataQueue.removeNode(dcd);
                    return;
                }
                if (stateWon) {
                    delayedClaimDataQueue.removeNode(dcd);
                    return;
                }
                gowner.numberOfClaimedChunks--;
                gowner.territories.remove(clist);
                gclaimer.numberOfClaimedChunks++;
                gclaimer.territories.add(clist);
                delayedClaimDataQueue.removeNode(dcd);
                Guerrilla.gmsgbroadcast(gclaimer.getName() + " took a part of territory from " + gowner.getName() + "!");

            }
        }, GuerrillaConfigurations.conqdelay);

        delayedClaimDataQueue.addNode(new DelayedClaimData(clist, gclaimer, gowner, claimer.getName(), dclaimid));
    }

    @Inject
    public void setCommandExecutor(CommandExecutor commandExecutor) {
        this.commandExecutor = commandExecutor;
    }
}
