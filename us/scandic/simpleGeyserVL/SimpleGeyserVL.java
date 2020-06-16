package us.scandic.simpleGeyserVL;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class SimpleGeyserVL extends JavaPlugin implements Listener
{
	Logger Log;
	PlayerListener Players;
	VoteListener Votes;
	
	@Override
	public void onEnable()
	{
		Log = getLogger();
		
		Players = new PlayerListener(this);
		Votes = new VoteListener(this);
		
		getServer().getPluginManager().registerEvents(Players, this);
		getServer().getPluginManager().registerEvents(Votes, this);
	}
	
	@Override
	public void onDisable() {
		Players.saveData();
	}
	
	void log(String s) {
		Log.log(Level.INFO, "§3[SimpleGeyserVL] §a" + s);
	}
}
