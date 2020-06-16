package us.scandic.simpleGeyserVL;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;

public class VoteListener implements Listener
{
	SimpleGeyserVL Plugin;
	PlayerListener Players;
	
	// List of commands to execute on vote
	List<String> CommandList = null;
	
	String PlayerMessage = "(null)", BroadcastMessage = "(null)";
	
	Map<UUID, List<QueuedVote>> QueuedVotes = new HashMap<UUID, List<QueuedVote>>();
	
	File VoteQueueFileHandle = null;
	FileConfiguration VoteQueueFile = null;
	
	String GeyserPrefix;
	
	public VoteListener(SimpleGeyserVL plugin)
	{
		Plugin = plugin;
		Players = plugin.Players;
		loadConfig();
		loadQueuedVotes();
	}
	
	void loadConfig()
	{
		File handle;
		FileConfiguration file;
		
		try
		{			
			handle = new File(Plugin.getDataFolder(), "config.yml");
			file = new YamlConfiguration();
			if (!handle.exists())
			{
				Plugin.log("Creating default config.yml");
				Plugin.saveResource("config.yml", false);
			}
			
			file.load(handle);
		}
		catch (Exception e)
		{
			Plugin.log(
				"[PlayerTracking] ERROR: Failed to read config file");
			e.printStackTrace();
			return;
		}
		
		CommandList = file.getStringList("commands");
		PlayerMessage = file.getString("player-message").replace('&', '§');
		BroadcastMessage = file.getString("broadcast-message").replace('&', '§');
		GeyserPrefix = file.getString("geyser-prefix");
		
		for (String c : CommandList)
		{
			Plugin.log("Loaded vote command: " + c.replace('%', '$'));
		}
		Plugin.log("Loaded player msg: " + PlayerMessage);
		Plugin.log("Loaded proadcast msg: " + BroadcastMessage);
	}
	
	void loadQueuedVotes()
	{
		try
		{			
			VoteQueueFileHandle = new File(Plugin.getDataFolder(), "votequeue.yml");
			VoteQueueFile = new YamlConfiguration();
			if (!VoteQueueFileHandle.exists())
				return;
			
			VoteQueueFile.load(VoteQueueFileHandle);
		}
		catch (Exception e)
		{
			Plugin.log(
				"[PlayerTracking] ERROR: Failed to read vote queue file");
			e.printStackTrace();
			return;
		}
		
		int loaded = 0;
		
		Set<String> players =
			VoteQueueFile.getConfigurationSection("votes").getKeys(false);
		
		for (String k : players)
		{
			UUID uid = UUID.fromString(k);
			String prefix = "votes." + k;
			
			Set<String> votes =
				VoteQueueFile.getConfigurationSection(prefix).getKeys(true);

			for (String v : votes)
			{
				long timestamp = Long.parseLong(v);
				String service = VoteQueueFile.getString(prefix + "." + v);
				
				if (!QueuedVotes.containsKey(uid))
					QueuedVotes.put(uid, new ArrayList<QueuedVote>());
				
				QueuedVotes.get(uid).add(new QueuedVote(uid, service, timestamp));
				++loaded;
				//Plugin.log("Loaded a queued vote from player " + Plugin.getPlayerName(uid) + " service " + service + " timestamp " + timestamp);
			}
		}
		
		if (loaded > 0)
			Plugin.log("Loaded " + loaded + " queued votes");
	}
	
	@EventHandler(priority=EventPriority.NORMAL)
	public void onVotifierEvent(VotifierEvent event)
	{
		try
		{
			Vote vote = event.getVote();
			
			String name = vote.getUsername(), service = vote.getServiceName();
			
			UUID uid = getVotingPlayer(name);
			if (uid == null)
			{
				Plugin.log("WARNING: Received a vote for a non-existent player: " + name + " from " + service);
				return;
			}
			
			if (Players.isOnline(uid))
			{
				Player player = Plugin.getServer().getPlayer(uid);
				if (player == null)
				{
					Plugin.log("ERROR: Online voting player's object is null?!: " + name);
					return;
				}
				
				// The player is online! Execute commands now
				executeCommands(player);
				player.sendMessage(PlayerMessage);
				
			}
			else
			{
				// The player is offline! Queue the vote to execute commands later
				long timestamp = new Date().getTime();
				QueuedVote qv = new QueuedVote(uid, service, timestamp);
				
				if (!QueuedVotes.containsKey(uid))
					QueuedVotes.put(uid, new ArrayList<QueuedVote>());
				
				QueuedVotes.get(uid).add(qv);
				VoteQueueFile.set("votes." + uid.toString() + "." + Long.toString(timestamp), service);
				VoteQueueFile.save(VoteQueueFileHandle);
			}

			// Let players know & count the vote in the leaderboard regardless of if online or not
			String bm = BroadcastMessage.replaceAll("%player%", name).replaceAll("%service%", service);
			Plugin.getServer().broadcast(bm, "essentials.spawn");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Plugin.log("ERROR: ^ Failed to process vote event");
		}
	}
	
	UUID getVotingPlayer(String name)
	{
		UUID javaPlayer = Players.getUidFromName(name),
			bedrockPlayer = Players.getUidFromName(GeyserPrefix + name);
		
		if (javaPlayer != null && bedrockPlayer != null)
		{
			// Uh oh! Both players exist on the server...
			boolean javaIsOnline = Players.isOnline(javaPlayer),
				bedrockIsOnline = Players.isOnline(bedrockPlayer);
			
			if (javaIsOnline && bedrockIsOnline)
			{
				long javaLastSeen = Players.getLastLogin(javaPlayer),
					bedrockLastSeen = Players.getLastLogin(bedrockPlayer);

				// Both players are currently online! Pick whoever logged in first
				if (javaLastSeen <= bedrockLastSeen)
					return javaPlayer;
				else
					return bedrockPlayer;
			}
			else if (javaIsOnline)
				return javaPlayer; // the Java player is the only one online!
			else if (bedrockIsOnline)
				return bedrockPlayer; // the Bedrock player is the only one online!
			else
			{
				long javaLastSeen = Players.getLastLogout(javaPlayer),
					bedrockLastSeen = Players.getLastLogout(bedrockPlayer);
				
				// Both players exist, but neither is online - return whoever logged out last
				if (javaLastSeen >= bedrockLastSeen)
					return javaPlayer;
				else
					return bedrockPlayer;
			}
		}
		else if (javaPlayer != null)
			return javaPlayer; // only found a Java player!
		else
			return bedrockPlayer; // return the found Bedrock player, or null if neither exists
	}
	
	void executeCommands(Player p)
	{
		CommandSender console = Plugin.getServer().getConsoleSender();
		String name = p.getName();
		for (String cmd : CommandList)
		{
			cmd = cmd.replaceAll("%player%", name);
			Plugin.getServer().dispatchCommand(console, cmd);
		}
	}
	
	boolean checkQueuedVotes(UUID uid)
	{
		if (!QueuedVotes.containsKey(uid)) return false;
		
		List<QueuedVote> list = QueuedVotes.get(uid);
		Player player = Plugin.getServer().getPlayer(uid);
		int vc = list.size();
		
		Plugin.log("Processing " + vc + " queued votes for player " + player.getName());
		
		for (int v = 0; v < vc; v++)
			executeCommands(player);
		
		QueuedVotes.remove(uid);
		VoteQueueFile.set("votes." + uid.toString(), null);
		
		try { VoteQueueFile.save(VoteQueueFileHandle); }
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return true;
	}
}
