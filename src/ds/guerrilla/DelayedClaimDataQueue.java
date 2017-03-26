package ds.guerrilla;

import java.util.ArrayList;

public class DelayedClaimDataQueue {
	
	private DelayedClaimData firstNode;
	
	DelayedClaimDataQueue(DelayedClaimData node) {
		firstNode = node;
	}
	
	DelayedClaimDataQueue() {
		firstNode = null;
	}
	
	/**
	 * 
	 * @return Will return a null value if there is no last node
	 */
	
	public DelayedClaimData getLastNode() {
		DelayedClaimData searchPointer = firstNode;
		if (searchPointer!=null) {
			while (firstNode.getSubsequentNode()!=null){
				searchPointer=firstNode.getSubsequentNode();
			}
		}
		return searchPointer;
	}
	
	public void addNode(DelayedClaimData node) {
		DelayedClaimData lastNode = getLastNode();
		if (lastNode==null) {
			firstNode = node ;
		} else {
			lastNode.setSubsequentNode(node);
		}
	}
	
	public void removeNode(DelayedClaimData node) {
		DelayedClaimData searchPointer = firstNode; 		
		if(firstNode!=null) {
			if (searchPointer.equals(node)) {
				firstNode = null;
			}
			else {
				while (searchPointer.getSubsequentNode()!=null && !searchPointer.getSubsequentNode().equals(node)) {
					searchPointer = searchPointer.getSubsequentNode();
				}
				if (searchPointer.getSubsequentNode()!=null) {
					if (searchPointer.getSubsequentNode().equals(node)) {
						DelayedClaimData chainNext = searchPointer.getSubsequentNode().getSubsequentNode();
						if (chainNext!=null) {
							searchPointer.setSubsequentNode(chainNext);
						} else {
							searchPointer.setSubsequentNode(null);
						}
					}
				}
			}
		}	
	}
	
	
	public DelayedClaimData search (ArrayList<Double> clist) {
		DelayedClaimData pointer = firstNode;
		boolean found = false;
		while ((pointer!=null) && (!found)) {
			if (pointer.getChunk().equals(clist))
				found = true;
			else
				pointer=pointer.getSubsequentNode();
		}
		return pointer;
	}
	
	/**
	 * 
	 * @param guerrilla
	 * @param kind 
	 * 0: search claimer
	 * 1: search owner
	 * @return
	 */
	
	public DelayedClaimData search (GuerrillaG guerrilla, int kind) {
		DelayedClaimData pointer = firstNode;
		boolean found = false;
		while ((pointer!=null) && (!found)) {
			switch (kind) {
			case 0:
				if (pointer.getGuerrillaClaimer().equals(guerrilla))
					found = true;
				else
					pointer=pointer.getSubsequentNode();
				break;
			case 1:
				if (pointer.getGuerrillaOwner().equals(guerrilla))
					found = true;
				else
					pointer=pointer.getSubsequentNode();
				break;
			}
			
		}
		return pointer;
	}
	
	public DelayedClaimData search (String claimerName) {
		DelayedClaimData pointer = firstNode;
		boolean found = false;
		while ((pointer!=null) && (!found)) {
			if (pointer.getClaimerName().equals(claimerName))
				found = true;
			else
				pointer=pointer.getSubsequentNode();
		}
		return pointer;
	}
	
	public DelayedClaimData search (int threadId) {
		DelayedClaimData pointer = firstNode;
		boolean found = false;
		while ((pointer!=null) && (!found)) {
			if (pointer.getThreadID() == threadId)
				found = true;
			else
				pointer=pointer.getSubsequentNode();
		}
		return pointer;
	}
	
	public boolean isEmpty() {
		if (firstNode==null) return true;
		else return false;
	}

}
