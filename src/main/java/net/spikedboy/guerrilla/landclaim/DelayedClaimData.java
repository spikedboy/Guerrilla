package net.spikedboy.guerrilla.landclaim;

import net.spikedboy.guerrilla.guerrilla.Guerrilla;

import java.util.ArrayList;

public class DelayedClaimData {

    private ArrayList<Integer> chunk;
    private Guerrilla guerrillaClaimer;
    private Guerrilla guerrillaOwner;
    private String claimerName;
    private int threadID;
    private DelayedClaimData subsequentNode;

    public DelayedClaimData(ArrayList<Integer> iChunk, Guerrilla iGuerrillaClaimer,
                            Guerrilla iGuerrillaOwner, String iClaimerName, int iThreadID) {
        chunk = iChunk;
        guerrillaClaimer = iGuerrillaClaimer;
        guerrillaOwner = iGuerrillaOwner;
        claimerName = iClaimerName;
        threadID = iThreadID;
        subsequentNode = null;
    }

    public DelayedClaimData getSubsequentNode() {
        return subsequentNode;
    }

    public void setSubsequentNode(DelayedClaimData subsequentNode) {
        this.subsequentNode = subsequentNode;
    }

    public ArrayList<Integer> getChunk() {
        return chunk;
    }

    public void setChunk(ArrayList<Integer> chunk) {
        this.chunk = chunk;
    }

    public Guerrilla getGuerrillaClaimer() {
        return guerrillaClaimer;
    }

    public void setGuerrillaClaimer(Guerrilla guerrillaClaimer) {
        this.guerrillaClaimer = guerrillaClaimer;
    }

    public Guerrilla getGuerrillaOwner() {
        return guerrillaOwner;
    }

    public void setGuerrillaOwner(Guerrilla guerrillaOwner) {
        this.guerrillaOwner = guerrillaOwner;
    }

    public String getClaimerName() {
        return claimerName;
    }

    public void setClaimerName(String claimerName) {
        this.claimerName = claimerName;
    }

    public int getThreadID() {
        return threadID;
    }

    public void setThreadID(int threadID) {
        this.threadID = threadID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DelayedClaimData that = (DelayedClaimData) o;

        if (threadID != that.threadID) return false;
        if (chunk != null ? !chunk.equals(that.chunk) : that.chunk != null) return false;
        if (guerrillaClaimer != null ? !guerrillaClaimer.equals(that.guerrillaClaimer) : that.guerrillaClaimer != null)
            return false;
        if (guerrillaOwner != null ? !guerrillaOwner.equals(that.guerrillaOwner) : that.guerrillaOwner != null)
            return false;
        if (claimerName != null ? !claimerName.equals(that.claimerName) : that.claimerName != null) return false;
        return subsequentNode != null ? subsequentNode.equals(that.subsequentNode) : that.subsequentNode == null;
    }

    @Override
    public int hashCode() {
        int result = chunk != null ? chunk.hashCode() : 0;
        result = 31 * result + (guerrillaClaimer != null ? guerrillaClaimer.hashCode() : 0);
        result = 31 * result + (guerrillaOwner != null ? guerrillaOwner.hashCode() : 0);
        result = 31 * result + (claimerName != null ? claimerName.hashCode() : 0);
        result = 31 * result + threadID;
        result = 31 * result + (subsequentNode != null ? subsequentNode.hashCode() : 0);
        return result;
    }
}
