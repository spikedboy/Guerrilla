package net.spikedboy.guerrilla.landclaim;

import com.google.inject.Inject;
import net.spikedboy.guerrilla.configuration.GuerrillaConfigurations;
import net.spikedboy.guerrilla.guerrilla.Guerrilla;
import net.spikedboy.guerrilla.guerrilla.GuerrillaManager;
import net.spikedboy.guerrilla.guerrilla.Messager;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class DelayedClaimRunner implements Runnable {
    private Player claimer;
    private Guerrilla gowner;
    private ArrayList<Integer> clist;
    private Guerrilla gclaimer;

    @Inject
    private GuerrillaManager guerrillaManager;

    public void run() {
        DelayedClaimData dcd = guerrillaManager.getDelayedClaimDataQueue().search(claimer.getName());

        if (claimer.getInventory().contains(GuerrillaConfigurations.itemid, (GuerrillaConfigurations.chunkprice * GuerrillaConfigurations.conqmulti))) {
            guerrillaManager.removeInventoryItems(claimer.getInventory(), Material.getMaterial(GuerrillaConfigurations.itemid),
                    (GuerrillaConfigurations.chunkprice * GuerrillaConfigurations.conqmulti));
        } else {
            claimer.sendMessage(Messager.GUERRILLA_MESSAGE_PREFIX + "You pig! You dropped the payment? you don't get the chunk!");
            guerrillaManager.getDelayedClaimDataQueue().removeNode(dcd);
            return;
        }
        if (GuerrillaManager.isStateWon()) {
            guerrillaManager.getDelayedClaimDataQueue().removeNode(dcd);
            return;
        }
        gowner.setNumberOfClaimedChunks(gowner.getNumberOfClaimedChunks() - 1);
        gowner.getTerritories().remove(clist);
        gclaimer.setNumberOfClaimedChunks(gclaimer.getNumberOfClaimedChunks() + 1);
        gclaimer.getTerritories().add(clist);
        guerrillaManager.getDelayedClaimDataQueue().removeNode(dcd);
        new Messager().sendMessage(gclaimer.getName() + " took a part of territory from " + gowner.getName() + "!");
    }

    public void setGuerrillaManager(GuerrillaManager guerrillaManager) {
        this.guerrillaManager = guerrillaManager;
    }

    public void setClaimer(Player claimer) {
        this.claimer = claimer;
    }

    public void setGowner(Guerrilla gowner) {
        this.gowner = gowner;
    }

    public void setClist(ArrayList<Integer> clist) {
        this.clist = clist;
    }

    public void setGclaimer(Guerrilla gclaimer) {
        this.gclaimer = gclaimer;
    }
}
