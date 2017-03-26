package ds.guerrilla;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldSaveEvent;

public class GuerrillaWorldListener implements Listener {
	
	public static Guerrilla plugin;
	
	GuerrillaWorldListener(Guerrilla inPlug) {
		plugin=inPlug;
	}

		
	@EventHandler
	public void onWorldSave(WorldSaveEvent event) {
		try {
			//Guerrilla.log.info("[Guerrilla] Saving..");
			GuerrillaG.removeMChests();
			//GuerrillaG.save();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
}
