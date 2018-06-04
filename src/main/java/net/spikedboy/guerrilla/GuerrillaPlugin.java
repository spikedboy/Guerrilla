package net.spikedboy.guerrilla;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import net.spikedboy.guerrilla.commands.CommandExecutor;
import net.spikedboy.guerrilla.configuration.GuerrillaConfigurations;
import net.spikedboy.guerrilla.guerrilla.ChargePaymentsRunnable;
import net.spikedboy.guerrilla.guerrilla.Guerrilla;
import net.spikedboy.guerrilla.guerrilla.GuerrillaManager;
import net.spikedboy.guerrilla.guerrilla.Messager;
import net.spikedboy.guerrilla.guice.GuerrillaModule;
import net.spikedboy.guerrilla.listeners.GuerrillaBlockListener;
import net.spikedboy.guerrilla.listeners.GuerrillaEntityListener;
import net.spikedboy.guerrilla.listeners.GuerrillaPlayerListener;
import net.spikedboy.guerrilla.listeners.GuerrillaWorldListener;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.logging.Logger;

public class GuerrillaPlugin extends JavaPlugin {

    private static final Logger LOGGER = Logger.getLogger("Minecraft");

    private static long initFirstPaymentDelay;
    private static int paymentThreadId;

    private FileConfiguration config;

    @Inject
    private Server serverInstance;

    @Inject
    private CommandExecutor commandExecutor;

    @Inject
    private GuerrillaBlockListener guerrillaBlockListener;

    @Inject
    private GuerrillaEntityListener guerrillaEntityListener;

    @Inject
    private GuerrillaPlayerListener guerrillaPlayerListener;

    @Inject
    private GuerrillaWorldListener guerrillaWorldListener;

    @Inject
    private Messager messager;

    @Inject
    private GuerrillaManager guerrillaManager;

    @Inject
    private Injector injector;

    @Override
    public void onEnable() {
        initialiceGuiceDependencyInjectionSystem();

        saveDefaultConfig();
        config = getConfig();

        readConfigIntoVars();

        if (!doesConfiguratedMapNameExist()) {
            return;
        }

        initFirstPaymentDelay = GuerrillaConfigurations.delay - serverInstance.getWorld(GuerrillaConfigurations.gworldname).getTime();
        LOGGER.info("[Guerrilla] Time for the first delay: " + initFirstPaymentDelay);

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

    private void readConfigIntoVars() {
        GuerrillaConfigurations.itemid = config.getInt("guerrilla.itemid", 296);
        GuerrillaConfigurations.chunkprice = config.getInt("guerrilla.chunkprice", 16);
        GuerrillaConfigurations.delay = (long) config.getInt("guerrilla.paymenttime", 23999);
        GuerrillaConfigurations.itemidmaint = config.getInt("guerrilla.iditemmaintennance", 296);
        GuerrillaConfigurations.chunkmaintprice = config.getInt("guerrilla.chunkmaintennanceprice", 1);
        GuerrillaConfigurations.gworldname = config.getString("guerrilla.gworldname", "world");
        LOGGER.info("[Guerrilla] World name: " + GuerrillaConfigurations.gworldname);
        GuerrillaConfigurations.conqdelay = (long) config.getInt("guerrilla.conquestdelay", 6000);
        GuerrillaConfigurations.conqmulti = config.getInt("guerrilla.conquestpricemultiplier", 2);
        GuerrillaConfigurations.defbonus = config.getInt("guerrilla.defenderdamagetakendivider", 2);
        GuerrillaConfigurations.atkbonus = config.getInt("guerrilla.defenderdamagedealtmultiplier", 1);
        GuerrillaConfigurations.nchunkpay = config.getInt("guerrilla.nchunksminmaintenance", 10);
        GuerrillaConfigurations.objectiveChunks = config.getInt("guerrilla.chunkobjective", 780);
        GuerrillaConfigurations.maintmaxprice = config.getInt("guerrilla.maxmaintenanceprice", 44);
        GuerrillaConfigurations.minimumPlayersNeededToAllowASafeChest = config.getInt("guerrilla.miniumplayersforasafechest", 5);
        GuerrillaConfigurations.expTime = config.getInt("guerrilla.guerrillaexpirytime", 604800000);
        GuerrillaConfigurations.tntProtection = config.getBoolean("guerrilla.explostionProtection", false);

        GuerrillaManager.setWinnerGuerrillaName(config.getString("guerrilla.winner", ""));
        GuerrillaManager.setStateWon(config.getBoolean("guerrilla.ismatchwon", false));
    }

    private void registerListeners(PluginManager pm) {
        pm.registerEvents(guerrillaBlockListener, this);
        pm.registerEvents(guerrillaEntityListener, this);
        pm.registerEvents(guerrillaPlayerListener, this);
        pm.registerEvents(guerrillaWorldListener, this);
    }

    private void loadSavedDataFromFile() {
        try {
            guerrillaManager.load();
        } catch (FileNotFoundException e) {
            LOGGER.warning("[Guerrilla] some guerrilla file is missing, bad things might happen...");
        } catch (Exception e) {
            e.printStackTrace();
        }
        LOGGER.info("[Guerrilla] guerrilla loaded");
    }

    private void removeMissingPaymentChestsHandleConcurrentModificationException() {
        try {
            removeMissingPaymentChests();
        } catch (ConcurrentModificationException e) {
            e.printStackTrace();
        }
    }

    private void removeMissingPaymentChests() {
        for (Guerrilla guerrilla : guerrillaManager.getGuerrillaList()) {
            for (ArrayList<Integer> chestc : guerrilla.getPaymentChests()) {
                if (serverInstance.getWorld(GuerrillaConfigurations.gworldname).getBlockAt(chestc.get(0),
                        chestc.get(1), chestc.get(2)).getType() != Material.CHEST) {

                    guerrilla.getPaymentChests().remove(chestc);
                    guerrilla.msggue("A missing payment chest was removed");
                    LOGGER.info("[Guerrilla] A payment chest was removed because the chest was missing");
                }
            }
        }
    }

    private void initPaymentThread() {
        ChargePaymentsRunnable task = injector.getInstance(ChargePaymentsRunnable.class);
        paymentThreadId = this.getServer().getScheduler().scheduleSyncRepeatingTask(this, task, initFirstPaymentDelay, GuerrillaConfigurations.delay);
    }

    @Override
    public void onDisable() {
        cancelPaymentThread();
        saveStateToFile();
        removeMissingPaymentChests();

        LOGGER.info("[Guerrilla] Guerrilla disabled");

        saveConfig();
    }

    private void cancelPaymentThread() {
        serverInstance.getScheduler().cancelTask(paymentThreadId);
    }

    private void saveStateToFile() {
        try {
            guerrillaManager.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean doesConfiguratedMapNameExist() {
        if (serverInstance.getWorld(GuerrillaConfigurations.gworldname) == null) {
            LOGGER.info("[Guerrilla] El nombre del mapa especificado es erroneo! Cambialo y reinicia");
            return false;
        }
        return true;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        return commandExecutor.processCommands(sender, cmd, commandLabel, args);
    }

    public void setCommandExecutor(CommandExecutor commandExecutor) {
        this.commandExecutor = commandExecutor;
    }

    public void setGuerrillaBlockListener(GuerrillaBlockListener guerrillaBlockListener) {
        this.guerrillaBlockListener = guerrillaBlockListener;
    }

    public void setGuerrillaEntityListener(GuerrillaEntityListener guerrillaEntityListener) {
        this.guerrillaEntityListener = guerrillaEntityListener;
    }

    public void setGuerrillaPlayerListener(GuerrillaPlayerListener guerrillaPlayerListener) {
        this.guerrillaPlayerListener = guerrillaPlayerListener;
    }

    public void setGuerrillaWorldListener(GuerrillaWorldListener guerrillaWorldListener) {
        this.guerrillaWorldListener = guerrillaWorldListener;
    }

    public void setMessager(Messager messager) {
        this.messager = messager;
    }

    public void setServerInstance(Server serverInstance) {
        this.serverInstance = serverInstance;
    }

}
