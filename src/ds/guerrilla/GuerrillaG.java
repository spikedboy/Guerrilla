package ds.guerrilla;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;

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

public class GuerrillaG implements Serializable {

	private static final long serialVersionUID = 5277397633585310503L;
	public static ArrayList<GuerrillaG> GuerrillaList = new ArrayList<GuerrillaG>();
	public static ArrayList<ArrayList<Integer>> SafeChunks = new ArrayList<ArrayList<Integer>>();
	public ArrayList<String> Players = new ArrayList<String>();
	public ArrayList<ArrayList<Double>> Territories = new ArrayList<ArrayList<Double>>();
	public ArrayList<String> Invites = new ArrayList<String>();
	public ArrayList<ArrayList<Integer>> PaymentChests = new ArrayList<ArrayList<Integer>>();
	public ArrayList<ArrayList<Integer>> SafeChest = new ArrayList<ArrayList<Integer>>(); //chest block xzy
	public String name, leader;
	public int claimedchunks=0;
	public Date date, antiSpam, quitPunishmentDate;
	
	public GuerrillaG ( Player player, String args ) {
		Players.add(player.getName());
		leader = player.getName();
		name = args;
		GuerrillaG.gmsgbroadcast(player.getName() + " created the " + name + " guerrilla!");
		try {
			GuerrillaG.save();
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	public static boolean isBeingClaimed (GuerrillaG guerrilla) {
		if (Guerrilla.delayedClaimDataQueue.search(guerrilla, 1)!=null) {
			return true;
		}
		return false;
	}
	
	public void changeLeader (Player sender, String nleader) {
		GuerrillaG guerrillas = GuerrillaG.getPlayerGuerrilla(sender);
		if (guerrillas == null) {
			sender.sendMessage(Guerrilla.gCh + "You have no guerrilla");
			return;
		}
		String gsleader = guerrillas.getLeader();
		String sname = sender.getName();
		GuerrillaG nlg = GuerrillaG.getPlayerGuerrilla(Guerrilla.sinst.getPlayerExact(nleader));
		if (nlg==null) {
			sender.sendMessage(Guerrilla.gCh + "That player has no guerrilla!");
			return;
		}
		else if (!(nlg.equals(guerrillas))) {
			sender.sendMessage(Guerrilla.gCh + "That player is not in your guerrilla");
			return;
		}
		if (sname.equals(gsleader)) {
			guerrillas.leader = nleader;
			msggue("The new leader is " + nleader + "!");
		} else {
			sender.sendMessage(Guerrilla.gCh + "Only the leader can do that");
		}
	}
	
	public void addSafeChest(Chest chest, Player sender) {
		ArrayList<Integer> chlist = GuerrillaG.ChestToList(chest);
		
		if (GuerrillaG.getGuerrillaChunk(chest.getBlock().getChunk()) != GuerrillaG.getPlayerGuerrilla(sender)) {
			sender.sendMessage(Guerrilla.gCh + "You can't make a safechest outside of your territory");
			return;
		}
		
		if (!GuerrillaG.isLeader(sender)) {
			sender.sendMessage(Guerrilla.gCh + "You are not leader");
			Guerrilla.PlayerSetsSafe.remove(sender.getName());
			return;
		}
		if (SafeChest == null) SafeChest = new ArrayList<ArrayList<Integer>>();
		int np = countPlayers();
		if (Guerrilla.minPSC > np ) {
			sender.sendMessage(Guerrilla.gCh + "You can't have a safe chest if your guerrilla has less than " + Guerrilla.minPSC + " members");
			return;
		}
		if (SafeChest.isEmpty()){
			SafeChest.add(chlist);
			sender.sendMessage(Guerrilla.gCh + "Safe chest added!");
			Guerrilla.PlayerSetsSafe.remove(sender.getName());
			return;
		}
		sender.sendMessage(Guerrilla.gCh + "You already have a safechest");
		Guerrilla.PlayerSetsSafe.remove(sender.getName());
		return;
	}
	
	public void removeSafeChest(Chest chest, Player sender) {
		ArrayList<Integer> chlist = GuerrillaG.ChestToList(chest);
		if (SafeChest.isEmpty()) {
			sender.sendMessage(Guerrilla.gCh + "You have no chests!");
			Guerrilla.PlayerSetsSafe.remove(sender.getName());
			return;
		}
		else if (SafeChest.contains(chlist)){
			sender.sendMessage(Guerrilla.gCh + "Safe chest removed");
			Guerrilla.PlayerSetsSafe.remove(sender.getName());
			SafeChest.remove(chlist);
			return;
		}	
		sender.sendMessage(Guerrilla.gCh+"That is not your chest");
		Guerrilla.PlayerSetsSafe.remove(sender.getName());
		return;
	}
	
	@SuppressWarnings("unused")
	public int countChunks() {
		int i=0;
		for (ArrayList<Double> a : Territories) i++;
		return i;
	}
	
	@SuppressWarnings("unused")
	public int countPlayers() {
		int i=0;
		for (String a : Players) i++;
		//Guerrilla.log.info(" " + i);
		return i;
	}
	
	public boolean sbonline () {
		for (String pname : Players) {
			Player[] OLlist = Guerrilla.sinst.getOnlinePlayers();
			for (Player p1:OLlist) {
				if (p1.getName().equals(pname)) {
					return true;
				}
			}
			
		}
		return false;
	}
	
	public int howManyOnline () {
		int i=0;
		for (String pname : Players) {
			Player[] OLlist = Guerrilla.sinst.getOnlinePlayers();
			for (Player p1:OLlist) {
				if (p1.getName().equals(pname)) {
					i++;
				}
			}
			
		}
		return i;
	}
	
	public String nonline () {
		int n=howManyOnline();
		String answ = "";
		/*for (String pname : Players) {
			Player[] OLlist = Guerrilla.sinst.getOnlinePlayers();
			for (Player p1:OLlist) {
				if (p1.getName().equals(pname)) {
					n++;
				}
			}
			
		}*/
		if (n==0) return answ;
		answ = (ChatColor.LIGHT_PURPLE + " Online: "+ ChatColor.WHITE +"("+n+")");
		return answ;
	}
	
	public Chest getdchest(Chest chest){
		if(chest.getBlock().getRelative(BlockFace.NORTH).getType() == Material.CHEST)
	        return (Chest)chest.getBlock().getRelative(BlockFace.NORTH).getState();
	   else if(chest.getBlock().getRelative(BlockFace.SOUTH).getType() == Material.CHEST)
	        return (Chest)chest.getBlock().getRelative(BlockFace.SOUTH).getState();
	   else if(chest.getBlock().getRelative(BlockFace.EAST).getType() == Material.CHEST)
	        return (Chest)chest.getBlock().getRelative(BlockFace.EAST).getState();
	   else if(chest.getBlock().getRelative(BlockFace.WEST).getType() == Material.CHEST)
	        return (Chest)chest.getBlock().getRelative(BlockFace.WEST).getState();
	   return null;
	}

	
	public boolean removePaymentChest(Chest chest) {
		ArrayList<Integer> al1 = ChestToList(chest);
		if (!PaymentChests.contains(al1)){
			return false;
		}
		PaymentChests.remove(al1);
		if (getdchest(chest)!=null) { PaymentChests.remove(ChestToList(getdchest(chest))); }
		return true;
	}

	public boolean addPaymentChest(Chest chest) {
		ArrayList<Integer> al1 = ChestToList(chest);
		if (PaymentChests.contains(al1)){
			return false;
		}
		PaymentChests.add(al1);
		if (getdchest(chest)!=null) { PaymentChests.add(ChestToList(getdchest(chest))); }
		return true;
	}
	
	private void removePayment() {
		int price = (int) (Territories.size()/Guerrilla.nchunkpay)*Guerrilla.chunkmaintprice;
		boolean paid=false;
		for (ArrayList<Integer> chestc : PaymentChests) {	
			if (Guerrilla.sinst.getWorld(Guerrilla.gworldname).getBlockAt(chestc.get(0).intValue(), chestc.get(1).intValue(), chestc.get(2).intValue()).getType() != Material.CHEST) {
				msggue("A payment chest is missing and has been marked for removal");
				continue;
			}
			Chest chest=((Chest) (Guerrilla.sinst.getWorld(Guerrilla.gworldname).getBlockAt(chestc.get(0).intValue(), chestc.get(1).intValue(), chestc.get(2).intValue()).getState()));			
			if ((chest!=null) && (chest.getInventory().contains(Guerrilla.itemidmaint, Guerrilla.chunkmaintprice))) {				
				if (price>Guerrilla.maintmaxprice) price = Guerrilla.maintmaxprice;
				GuerrillaG.removeInventoryItems(chest.getInventory(), Material.getMaterial(Guerrilla.itemidmaint), (price));
				paid=true;
				msggue("Payments issued, cost: "+ price +", thank you: Att. Tom Nook");
				break;
			} 
		}
		if (paid==false){
			if (Territories.isEmpty()||price==0){
				msggue("Payments issued, but you had nothing to pay");
				return;
			}
			Territories.remove((Territories.size()-1));
			claimedchunks --;
			msggue("You have lost a chunk because you have no payment chests");
			return;
		}
		return;
	}
	
	public boolean somebodyHome(Chunk chunk) {
		for (String playern : Players) {
			if (Guerrilla.sinst.getPlayer(playern)==null) continue;
			if (Guerrilla.sinst.getPlayer(playern).getLocation().getBlock().getChunk().equals(chunk)) {
				return true;
			}
		}
		return false;
	}
		
	public boolean Claim(Chunk chunk, Player claimer) {
		if (GuerrillaG.isSafeChunk(chunk)) {
			claimer.sendMessage(Guerrilla.gCh + "Can't claim a safe chunk");
			return false;
		}
		if (GuerrillaG.getPlayerGuerrilla(claimer)!=null) {
			ArrayList<Double> clist = new ArrayList<Double>(2);
			Double chunkX = new Double(chunk.getX());
			Double chunkZ = new Double(chunk.getZ());			
			clist.add(chunkX);
			clist.add(chunkZ);
				if ( (Territories.contains(clist))) {
					claimer.sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY + "That chunk is already claimed");
					return false;
				}	
				else if (!(claimer.getInventory().contains(Guerrilla.itemid, Guerrilla.chunkprice))){
					claimer.sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY + "The required price is " + Guerrilla.chunkprice + " of " + (Material.getMaterial(Guerrilla.itemid).name()).toString() + " and you don't have it");
					return false;
				} 
				else {
					if (canClaim(chunk, claimer)) {
						if (GuerrillaG.getGuerrillaChunk(chunk)!=this && GuerrillaG.getGuerrillaChunk(chunk)!=null) {
							if (!claimer.getInventory().contains(Guerrilla.itemid, Guerrilla.chunkprice*Guerrilla.conqmulti)) {
								claimer.sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY + "The required price is " + (Guerrilla.chunkprice*Guerrilla.conqmulti) + " of " + (Material.getMaterial(Guerrilla.itemid).name()).toString() + " and you don't have it");
								return false;
							}
							
							if (Guerrilla.delayedClaimDataQueue.search(claimer.getName())!=null) {
								claimer.sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY + "Your faction is already claiming an enemy chunk! You can't do that twice");
								return true;
							}								
							Guerrilla.delayedClaim(this, GuerrillaG.getGuerrillaChunk(chunk), chunk, claimer);
							GuerrillaG.getGuerrillaChunk(chunk).msggue(this.getName() + " is claiming part of your territory! Coords: " + chunk.getBlock(0, 0, 0).getX() + "," + chunk.getBlock(0, 0, 0).getZ() + " (x,z)");
							msggue("Claiming enemy area, if you leave the chunk you will loose this dispute!");
							return true;
						}
						GuerrillaG.removeInventoryItems(claimer.getInventory(), Material.getMaterial(Guerrilla.itemid), Guerrilla.chunkprice);
						claimedchunks++;
						Territories.add(clist);	
						gmsgbroadcast(this.getName() + " claimed some terrain");
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
	
	public boolean canClaim(Chunk chunk, Player claimer){
		GuerrillaG owner = GuerrillaG.getGuerrillaChunk(chunk);	
		GuerrillaG gclaimer = GuerrillaG.getPlayerGuerrilla(claimer);
		Date now = new Date();
		if ((owner!=this)&&(owner!=null)) {
			if (owner.date==null) owner.date=now;
			if (((now.getTime() - owner.date.getTime())>=Guerrilla.expTime) && ((adyChunks(chunk, owner)<=2)&&(adyChunks(chunk, gclaimer)>=1))) {
				return true;
			}
			if ((owner.quitPunishmentDate != null) && ((now.getTime()-owner.quitPunishmentDate.getTime())<Guerrilla.delay) && ((adyChunks(chunk, owner)<=2)&&(adyChunks(chunk, gclaimer)>=1))) {
				return true;
			}
			if (!owner.sbonline()) return false;
			if (owner.somebodyHome(chunk)) return false;
			if ((adyChunks(chunk, owner)<=2)&&(adyChunks(chunk, gclaimer)>=1)){	
				return true;
			}
		}
		else if (owner==null){
			if (adyChunks(chunk, this)>=1) {
				return true;
			}
			else if (adyChunks(chunk, this)==0){
				if (this.claimedchunks==0) {
					return true;
				} else {
					return false;
				}
			}
		}
		else if (owner==this){
			return false;
		}
		return false;
	}
	
	public boolean unclaim(Chunk chunk, Player claimer){
		ArrayList<Double> clist = new ArrayList<Double>(2);
		Double chunkX = new Double(chunk.getX());
		Double chunkZ = new Double(chunk.getZ());			
		clist.add(chunkX);
		clist.add(chunkZ);
		if (Territories.contains(clist)) {
			claimedchunks--;
			Territories.remove(clist);
			claimer.sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY + "Chunk unclaimed");
			return true;
		}
		claimer.sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY + "You don't own this chunk");
		return false;
	}
	
	public boolean unclaimall(Player sender){
		if (sender.getName().equals(leader)){
			claimedchunks=0;
			Territories.clear();
			msggue("All territories have been unclaimed!");
			return true;
		}
		sender.sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY + "You are not leader");
		return true;
	}
	
	public void msggue(String msg){
		for (String playername : Players) {
			
			if (Guerrilla.sinst.getPlayer(playername) != null) { Guerrilla.sinst.getPlayer(playername).sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY + msg); }
		}
	}
	
	public void msgguespam(String msg){
		Date now = new Date();
		if ((this.antiSpam!=null) && (( now.getTime()-this.antiSpam.getTime()) < 5000)) {
			return;
		}
		this.antiSpam = new Date();
		for (String playername : Players) {
			Player player = Guerrilla.sinst.getPlayer(playername);			
			//Guerrilla.log.info("deb " + antiSpam.getTime());				
			if (player != null) { 
				player.sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY + msg);				
			}
		}
	}
	
	public boolean join(Player player) {
		if (Invites.contains(player.getName())) {
			msggue(player.getName() + " has joined your Guerrilla");
			Players.add(player.getName());
			Invites.remove(player.getName());
			player.sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY + "You are now part of the " + this.getName() + " guerrilla");						
			return true;
		} else {
			player.sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY + "You have not been invited to this faction");
			return false;
		}
	}
	
	public boolean kick(String player, Player sender) {
		if (GuerrillaG.isLeader(sender)) {
			if (GuerrillaG.getPlayerGuerrilla(player)==GuerrillaG.getPlayerGuerrilla(sender)) {
				Players.remove(player);
				msggue(player + " has been kicked");
				if (Guerrilla.sinst.getPlayer(player)!=null){
					Guerrilla.sinst.getPlayer(player).sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY + sender.getName() + " has kicked you");
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
		if (!(Invites.contains(player.getName())) && ((GuerrillaG.getPlayerGuerrilla(player)) == null)) {
			Invites.add(player.getName());
			sender.sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY + player.getName() + " has been invited to your guerrilla");
			player.sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY + "You have been invited to the " + this.getName() + " guerrilla");
			return true;
		}
		sender.sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY + "That player can't get invited");
		return false;
	}
	
	public boolean inviteCancel(Player player, Player sender) {
		if ((Invites.contains(player.getName())) && (GuerrillaG.isLeader(sender))){
			Invites.remove(player.getName());
			sender.sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY + "You canceled that invite");
			return true;
		}
		sender.sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY + "You can't do that");
		return false;
	}
		
	public boolean ownsGuerrillaChunk(Chunk chunk) {
		ArrayList<Double> clist = new ArrayList<Double>(2);
		Double chunkX = new Double(chunk.getX());
		Double chunkZ = new Double(chunk.getZ());			
		clist.add(chunkX);
		clist.add(chunkZ);
		if (GuerrillaG.getGuerrillaChunk(chunk)==null){
			return true;
		} else {
			return (Territories.contains(clist));
		}
	}
	
	public boolean disband(Player player){
		if (this.getLeader().equals(player.getName())) {
			msggue("Your guerrilla has been disbanded!");
			Guerrilla.log.info(this.getName()+ " disbanded");
			Players.clear();
			Territories.clear();
			Invites.clear();
			name = "";
			leader = "";
			claimedchunks = 0;
			GuerrillaList.remove(this);	
			return true;			
		}
		player.sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY + "You do not own this guerrilla");
		return false;
	}
	
	public String getLeader(){
		return leader;
	}
	
	public String getName(){
		return name;
	}
	
	public static GuerrillaG getGuerrillaSafeChest(Chest chest) {
		for (GuerrillaG g1 : GuerrillaList) {
			if (g1.SafeChest==null) continue;
			if (g1.SafeChest.isEmpty()) continue;
			ArrayList<Integer> chlist = GuerrillaG.ChestToList(chest);
			if (g1.SafeChest.get(0).equals(chlist)) {
				return GuerrillaG.getGuerrillaChunk(chest.getBlock().getChunk());
			}
		}
		return null;		
	}
	
	public static void sortGList () {
		//TODO
		if (GuerrillaList.isEmpty()) return;
		for (int i = 0; i<(GuerrillaList.size()); i++) {
			
			int n = GuerrillaList.get(i).howManyOnline(), maxIn=i;
			
			for (int j = i; j<(GuerrillaList.size()); j++) {
				if (n<=GuerrillaList.get(j).howManyOnline()) maxIn = j;
			}
			
			GuerrillaG gAu = GuerrillaList.get(i);
			GuerrillaList.set(i, GuerrillaList.get(maxIn));
			GuerrillaList.set(maxIn, gAu);
			
		}
	}
	
	public static void List(Player sender, int page) {
		sortGList();
		if (page<=0) page=1;
		int index=(page-1)*7;
		sender.sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.WHITE + "Listing Guerrillas:" + ChatColor.GRAY +" (Chunk objective: " + Guerrilla.objectiveChunks + ") " + ChatColor.WHITE + "PAGE " + page);
		for (int i = index; i<(GuerrillaList.size()); i++) {
			if (i>(GuerrillaList.size()-1)) break;
			GuerrillaG guerrilla = GuerrillaList.get(i);
			sender.sendMessage("- " + guerrilla.name + ChatColor.LIGHT_PURPLE + " Leader: " + ChatColor.WHITE +guerrilla.getLeader() + ChatColor.LIGHT_PURPLE + " Chunks: " + ChatColor.WHITE + guerrilla.claimedchunks + guerrilla.nonline());	
		}
		return;
	}
	
	public static GuerrillaG getGuerrillaByName(String name){
		for (GuerrillaG guerrilla : GuerrillaList) {
			if (guerrilla.getName().equals(name)){ return guerrilla; }
		} 
		return null;
	}
	
	public static GuerrillaG checkWinners() {
		for (GuerrillaG guerrilla : GuerrillaList) {
			if (guerrilla.claimedchunks>=Guerrilla.objectiveChunks) {
				return guerrilla;
			}
		}
		return null;
	}
	
	public static void printmap() throws IOException {
		//ArrayList<Double> clist = new ArrayList<Double>(2);
		//Double chunkX = new Double(chunk.getX());
		//Double chunkZ = new Double(chunk.getZ());			
		//clist.add(chunkX);
		//clist.add(chunkZ);
		FileWriter fstream = new FileWriter("coords.txt");
		BufferedWriter out = new BufferedWriter(fstream);
		int contador=0;
		for (GuerrillaG guerrilla : GuerrillaList) {
			for ( ArrayList<Double> clist : guerrilla.Territories) {
				Double ClistX = clist.get(0);
				Double ClistZ = clist.get(1);
				String gname = guerrilla.getName();
				if (contador != 0) out.newLine();
				out.write(ClistX + "," + ClistZ + "," + gname + ",");					
				out.flush();
				contador ++;
			}
		}
	}
	
	public static void who(Player sender, String args) {
		Date now = new Date();
		if (args == null) {
			GuerrillaG guerrilla = GuerrillaG.getPlayerGuerrilla(sender);
			if (guerrilla == null){
				sender.sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY + "You have no guerrilla");
				return;
			}
			if (guerrilla.date==null) guerrilla.date=now;
			sender.sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY + guerrilla.getName() + "'s info:");				
			sender.sendMessage(ChatColor.DARK_AQUA+" Chunks claimed: " + ChatColor.WHITE + guerrilla.claimedchunks);
			sender.sendMessage(ChatColor.DARK_AQUA+" Payment chests: " + ChatColor.WHITE + guerrilla.PaymentChests.size());
			sender.sendMessage(ChatColor.DARK_AQUA+" Last login: " + ChatColor.WHITE + ((now.getTime()-guerrilla.date.getTime())/3600000) + ChatColor.DARK_AQUA + "h ago");
			sender.sendMessage(ChatColor.DARK_AQUA + " Members:");
						
			for (String playername : guerrilla.Players){
				if (guerrilla.getLeader()==playername){
					sender.sendMessage("  " + playername + (ChatColor.LIGHT_PURPLE + " (leader)"));
				} else {
					sender.sendMessage("  " + playername);
				}						
			}
		} else {
			GuerrillaG guerrilla = GuerrillaG.getGuerrillaByName(args);		
			if (guerrilla == null) {
				sender.sendMessage(args + " doesn't exist");
				return;
			} else {
				if (guerrilla.date==null) guerrilla.date=now;
				sender.sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY + guerrilla.getName() + "'s info:");				
				sender.sendMessage(ChatColor.DARK_AQUA+" Chunks claimed: " + ChatColor.WHITE + guerrilla.claimedchunks);
				sender.sendMessage(ChatColor.DARK_AQUA+" Payment chests: " + ChatColor.WHITE + guerrilla.PaymentChests.size());
				sender.sendMessage(ChatColor.DARK_AQUA+" Last login: " + ChatColor.WHITE + ((now.getTime()-guerrilla.date.getTime())/3600000) + ChatColor.DARK_AQUA + "h ago");
				if (guerrilla.quitPunishmentDate!=null) {
					sender.sendMessage(ChatColor.DARK_AQUA + " Punished: " + ChatColor.WHITE + (((now.getTime()-guerrilla.quitPunishmentDate.getTime())<Guerrilla.delay)));
				}
				sender.sendMessage(ChatColor.DARK_AQUA +" Members:");
				for (String playername : guerrilla.Players){					
					if (guerrilla.getLeader().equals(playername)){
						sender.sendMessage("  " + playername + (ChatColor.LIGHT_PURPLE + " (leader)"));
					} else {
						sender.sendMessage("  " + playername);
					}				
					}				
				}
			}
		}		
	
	public static GuerrillaG getGuerrillaChunk(Chunk chunk){
		ArrayList<Double> clist = new ArrayList<Double>(2);
		Double chunkX = new Double(chunk.getX());
		Double chunkZ = new Double(chunk.getZ());			
		clist.add(chunkX);
		clist.add(chunkZ);
		for (GuerrillaG guerrilla : GuerrillaList) {
			if (guerrilla.Territories.contains(clist)) {
				return guerrilla;
			}
		}
		return null;
	}
	
	public static GuerrillaG getPlayerGuerrilla(Player player){
		for (GuerrillaG guerrilla : GuerrillaList) {
			if (guerrilla.Players.contains(player.getName())){
				return guerrilla;
			}
		}
		return null;
	}
	
	public static GuerrillaG getPlayerGuerrilla(String player){
		for (GuerrillaG guerrilla : GuerrillaList) {
			if (guerrilla.Players.contains(player)){
				return guerrilla;
			}
		}
		return null;
	}
	
	public static boolean isLeader(Player player){
		GuerrillaG guerrilla = GuerrillaG.getPlayerGuerrilla(player);
		//Guerrilla.log.info(player.getName() + " " + guerrilla.leader + " " + guerrilla.getLeader() + " " + guerrilla.leader.equals(player.getName()));
		return guerrilla.leader.equals(player.getName());
	}
	
	public static boolean leave(Player sender) {
		if (GuerrillaG.isLeader(sender)){
			sender.sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY + "The leader can't leave! Disband!");
			return true;
		}
		GuerrillaG guerrilla = GuerrillaG.getPlayerGuerrilla(sender);
		if ((guerrilla != null) && ((! GuerrillaG.isLeader(sender)))) {
			guerrilla.Players.remove(sender.getName());
			sender.sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY + "You left the "+guerrilla.getName()+" guerrilla!");
			return true;
		}
		sender.sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY +"You can't do that!");
		return false;
	}
	
	public static boolean inviteDecline(Player sender, String args){
		GuerrillaG guerrilla = GuerrillaG.getGuerrillaByName(args);
		if (guerrilla == null) {
			sender.sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY + "That guerrilla doesn't exist");
			return false;
		}
		if (guerrilla.Invites.contains(sender.getName())) {
			guerrilla.msggue(sender.getName() + "Declined his invite");
			guerrilla.Invites.remove(sender.getName());
			sender.sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY +"Invitation declined");
			return true;
		}
		sender.sendMessage(ChatColor.DARK_RED + "[Guerrilla] " + ChatColor.GRAY + "You have no pending invites");
		return false;
	}
	
	public static void saveFunc(Object o, String path) throws Exception
	{
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path));
		oos.writeObject(o);
		oos.flush();
		oos.close();
	}
	
	public static Object loadFunc(String path) throws Exception
	{
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path));
		Object result = ois.readObject();
		ois.close();
		return result;
	}
	
	public static void save() throws Exception {
		
		GuerrillaG.saveFunc(GuerrillaList, "Guerrilla.bin");
		GuerrillaG.saveFunc(SafeChunks, "GuerrillaSafe.bin");
		
	}
	
    public static void removeInventoryItems(Inventory inv, Material type, int amount) {
    	Guerrilla.log.info("cantidad total: " + amount);
        for (ItemStack is : inv.getContents()) {
            if ( (is != null) && (is.getType() == type)) {
                int newamount = is.getAmount() - amount;
            	Guerrilla.log.info("stack - total: " + newamount);
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
	
	@SuppressWarnings("unchecked")
	public static void load() throws Exception {
		
		File gFile = new File("Guerrilla.bin");
		File sChunks = new File("GuerrillaSafe.bin");
		if (gFile.exists())	GuerrillaList = (ArrayList<GuerrillaG>) GuerrillaG.loadFunc("Guerrilla.bin");
		else gFile.createNewFile();
		if (sChunks.exists()) SafeChunks = (ArrayList<ArrayList<Integer>>) GuerrillaG.loadFunc("GuerrillaSafe.bin");	
		else sChunks.createNewFile();
			
	}
	
	public static int adyChunks(Chunk chunk, GuerrillaG guerrilla){
		int n=0;
		if (GuerrillaG.getGuerrillaChunk(chunk.getBlock(1, 1, 1).getRelative(BlockFace.NORTH, 17).getChunk())==guerrilla){
			n++;
		}
		if (GuerrillaG.getGuerrillaChunk(chunk.getBlock(1, 1, 1).getRelative(BlockFace.SOUTH, 17).getChunk())==guerrilla){
			n++;
		}
		if (GuerrillaG.getGuerrillaChunk(chunk.getBlock(1, 1, 1).getRelative(BlockFace.EAST, 17).getChunk())==guerrilla){
			n++;
		}
		if (GuerrillaG.getGuerrillaChunk(chunk.getBlock(1, 1, 1).getRelative(BlockFace.WEST, 17).getChunk())==guerrilla){
			n++;
		}
		return n;
	}

	public static void ChargueMaintenance() {
		gmsgbroadcast("Issuing payments...");
		for (GuerrillaG guerrilla : GuerrillaList) {
			guerrilla.removePayment();
		}
		Guerrilla.log.info("[Guerrilla] Done");
		try {
			GuerrillaG.save();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void removeMChests() {
		for (GuerrillaG guerrilla : GuerrillaList){
			for (ArrayList<Integer> chestc : guerrilla.PaymentChests) {	
				if (Guerrilla.sinst.getWorld(Guerrilla.gworldname).getBlockAt(chestc.get(0).intValue(), chestc.get(1).intValue(), chestc.get(2).intValue()).getType() != Material.CHEST) {
					guerrilla.PaymentChests.remove(chestc);
					guerrilla.msggue("A missing paymentchest was removed");
					Guerrilla.log.info("[Guerrilla] A paymentchest was removed because the chest was missing");
				}
			}		
		}

	}
	
	public static void gmsgbroadcast(String msg) {
		Guerrilla.log.info("[Guerrilla] " + msg);
		for (Player player : Guerrilla.sinst.getOnlinePlayers()) {
			player.sendMessage(ChatColor.DARK_PURPLE + "[Guerrilla] " + ChatColor.GRAY + msg);
		}
	}
	
	public static ArrayList<Integer> ChestToList(Chest chest) {
		ArrayList<Integer> chlist = new ArrayList<Integer>(3);
		Integer ChX = new Integer(chest.getX());
		Integer ChY = new Integer(chest.getY());
		Integer ChZ = new Integer(chest.getZ());
		chlist.add(ChX);
		chlist.add(ChY);
		chlist.add(ChZ);
		return chlist;
	}
	
	public static Chest ListToChest(ArrayList<Integer> chlist) {
		int chestx = chlist.get(0);
		int chesty = chlist.get(1);
		int chestz = chlist.get(2);
		Chest chest = (Chest) Guerrilla.sinst.getWorld(Guerrilla.gworldname).getBlockAt(chestx, chesty, chestz).getState();
		if (chest==null) {
			Guerrilla.log.info("[Guerrilla] Oh shit nigga what are you doing");
			return null;
		}
		return chest;
	}
	
	@SuppressWarnings("unused")
	public static void map (Block block, Player sender) {
		//WIP
		
		boolean finished=false;
		Chunk center = block.getChunk();
		String line = "";
		char[] lineCh = new char[4];
		
		World world = center.getWorld();
		
		Chunk ulcorner = world.getChunkAt(center.getX()+1, center.getZ()+1);
		
		Chunk uside = world.getChunkAt(center.getX(), center.getZ()+1);
		Chunk urcorner = world.getChunkAt(center.getX()-1, center.getZ()+1);
		
		Chunk lside = world.getChunkAt(center.getX()+1, center.getZ());
		Chunk rside = world.getChunkAt(center.getX()-1, center.getZ());
		
		Chunk dlcorner = world.getChunkAt(center.getX()+1, center.getZ()-1);
		Chunk dside = world.getChunkAt(center.getX(), center.getZ()-1);
		Chunk drcorner = world.getChunkAt(center.getX()-1, center.getZ()-1);
		
		Block first = ulcorner.getBlock(0, 0, 0);
		
		
		for (byte j=0; j<12; j++) {
			for (byte i=0; i<4; i++) {
				if (i==1) {
					lineCh[i]='+';
				}
				if (i>1 && i<3) {
					lineCh[i]='G';
					continue;
				}
				if (i==3) {
					lineCh[i]='+';
					continue;
				}
			}
			
			line += lineCh; 
		}
		
		
	}
	
	public static void setSafeChunk (Chunk chunk) {
		GuerrillaG owner = getGuerrillaChunk(chunk);
		
		ArrayList<Integer> clist = new ArrayList<Integer>(2);
		Integer chunkX = new Integer(chunk.getX());
		Integer chunkZ = new Integer(chunk.getZ());			
		clist.add(chunkX);
		clist.add(chunkZ);
		if (owner!=null){
			owner.Territories.remove(clist);
		}
		if (SafeChunks.contains(clist)) return;
		SafeChunks.add(clist);
	}
	
	public static boolean isSafeChunk (Chunk chunk) {
		ArrayList<Integer> clist = new ArrayList<Integer>(2);
		Integer chunkX = new Integer(chunk.getX());
		Integer chunkZ = new Integer(chunk.getZ());			
		clist.add(chunkX);
		clist.add(chunkZ);
		if (SafeChunks.contains(clist)) {
			return true;
		} else {
			return false;
		}
	}
	
	public static void removeSafeChunk (Chunk chunk) {
		ArrayList<Integer> clist = new ArrayList<Integer>(2);
		Integer chunkX = new Integer(chunk.getX());
		Integer chunkZ = new Integer(chunk.getZ());			
		clist.add(chunkX);
		clist.add(chunkZ);
		
		if (!SafeChunks.contains(clist)) return;
		SafeChunks.remove(clist);
	}

	public static int getClaimingID(String pname) {
		DelayedClaimData dcd = Guerrilla.delayedClaimDataQueue.search(pname);
		if (dcd!=null)
			return (dcd.getThreadID());
		else
			return -1;
	}
	
}
