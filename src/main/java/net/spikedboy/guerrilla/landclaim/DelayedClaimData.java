package net.spikedboy.guerrilla.landclaim;

import net.spikedboy.guerrilla.Guerrilla;

import java.util.ArrayList;

public class DelayedClaimData {

    private ArrayList<Double> chunk;
    private Guerrilla guerrillaClaimer;
    private Guerrilla guerrillaOwner;
    private String claimerName;
    private int threadID;
    private DelayedClaimData subsequentNode;

    public DelayedClaimData getSubsequentNode() {
        return subsequentNode;
    }

    public void setSubsequentNode(DelayedClaimData subsequentNode) {
        this.subsequentNode = subsequentNode;
    }

    public DelayedClaimData(ArrayList<Integer> iChunk, Guerrilla iGuerrillaClaimer,
                            Guerrilla iGuerrillaOwner, String iClaimerName, int iThreadID) {
        chunk = iChunk;
        guerrillaClaimer = iGuerrillaClaimer;
        guerrillaOwner = iGuerrillaOwner;
        claimerName = iClaimerName;
        threadID = iThreadID;
        subsequentNode = null;
    }

    public ArrayList<Double> getChunk() {
        return chunk;
    }

    public Guerrilla getGuerrillaClaimer() {
        return guerrillaClaimer;
    }

    public Guerrilla getGuerrillaOwner() {
        return guerrillaOwner;
    }

    public String getClaimerName() {
        return claimerName;
    }

    public int getThreadID() {
        return threadID;
    }

    public void setChunk(ArrayList<Double> chunk) {
        this.chunk = chunk;
    }

    public void setGuerrillaClaimer(Guerrilla guerrillaClaimer) {
        this.guerrillaClaimer = guerrillaClaimer;
    }

    public void setGuerrillaOwner(Guerrilla guerrillaOwner) {
        this.guerrillaOwner = guerrillaOwner;
    }

    public void setClaimerName(String claimerName) {
        this.claimerName = claimerName;
    }

    public void setThreadID(int threadID) {
        this.threadID = threadID;
    }

    public boolean equals(DelayedClaimData dcd) {
        return (dcd == null) || (dcd.getChunk().equals(chunk) &&
                dcd.getClaimerName().equals(claimerName) &&
                dcd.getGuerrillaOwner().equals(guerrillaOwner) &&
                dcd.getGuerrillaClaimer().equals(guerrillaClaimer) &&
                (dcd.getThreadID() == threadID));
    }

}
