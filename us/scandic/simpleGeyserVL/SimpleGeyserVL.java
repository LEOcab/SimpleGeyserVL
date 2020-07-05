package us.scandic.simpleGeyserVL;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import us.scandic.simpleGeyserVL.command.VoteCommand;
import us.scandic.simpleGeyserVL.listener.PlayerListener;
import us.scandic.simpleGeyserVL.listener.VoteListener;

public class SimpleGeyserVL extends JavaPlugin implements Listener
{
	Logger Log;
	private PlayerListener Players;
	VoteListener Votes;
	VoteCommand votecommand = new VoteCommand(this);
	
	@Override
	public void onEnable()
	{
		Log = getLogger();
		
		setPlayers(new PlayerListener(this));
		Votes = new VoteListener(this);

		getServer().getPluginManager().registerEvents(getPlayers(), this);
		getServer().getPluginManager().registerEvents(Votes, this);
		if (VoteListener.isVoteCommand()) {
	        this.getCommand("vote").setExecutor(votecommand);
			log("Registered \"/vote\" command");
		}
	}
	
	@Override
	public void onDisable() {
		getPlayers().saveData();
	}
	
	public void log(String s) {
		Log.log(Level.INFO, "§3[SimpleGeyserVL] §a" + s);
	}

	public PlayerListener getPlayers() {
		return Players;
	}

	public void setPlayers(PlayerListener players) {
		Players = players;
	}
	
}
