package net.spikedboy.guerrilla;

import java.util.ArrayList;

public class DelayedClaimData {

    private ArrayList<Double> chunk;
    private GuerrillaG guerrillaClaimer;
    private GuerrillaG guerrillaOwner;
    private String claimerName;
    private int threadID;
    private DelayedClaimData subsequentNode;

    public DelayedClaimData getSubsequentNode() {
        return subsequentNode;
    }

    public void setSubsequentNode(DelayedClaimData subsequentNode) {
        this.subsequentNode = subsequentNode;
    }

    DelayedClaimData(ArrayList<Double> iChunk, GuerrillaG iGuerrillaClaimer,
                     GuerrillaG iGuerrillaOwner, String iClaimerName, int iThreadID) {
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

    public GuerrillaG getGuerrillaClaimer() {
        return guerrillaClaimer;
    }

    public GuerrillaG getGuerrillaOwner() {
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

    public void setGuerrillaClaimer(GuerrillaG guerrillaClaimer) {
        this.guerrillaClaimer = guerrillaClaimer;
    }

    public void setGuerrillaOwner(GuerrillaG guerrillaOwner) {
        this.guerrillaOwner = guerrillaOwner;
    }

    public void setClaimerName(String claimerName) {
        this.claimerName = claimerName;
    }

    public void setThreadID(int threadID) {
        this.threadID = threadID;
    }

    public boolean equals(DelayedClaimData dcd) {
        if ((dcd == null) || (dcd.getChunk().equals(chunk) &&
                dcd.getClaimerName().equals(claimerName) &&
                dcd.getGuerrillaOwner().equals(guerrillaOwner) &&
                dcd.getGuerrillaClaimer().equals(guerrillaClaimer) &&
                (dcd.getThreadID() == threadID)))
            return true;
        else return false;
    }

}
