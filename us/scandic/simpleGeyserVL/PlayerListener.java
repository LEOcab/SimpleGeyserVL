package us.scandic.simpleGeyserVL;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener
{
	SimpleGeyserVL Plugin;
	
	File DataFileHandle;
	FileConfiguration DataFile;
	
	List<UUID> OnlinePlayers = new ArrayList<UUID>();
	Map<String, UUID> KnownName = new HashMap<String, UUID>();
	Map<UUID, Long> LastLogin = new HashMap<UUID, Long>(),
		LastLogout = new HashMap<UUID, Long>();
	
	public PlayerListener(SimpleGeyserVL plugin)
	{
		Plugin = plugin;
		loadData();
	}
	
	void loadData()
	{
		try
		{			
			DataFileHandle = new File(Plugin.getDataFolder(), "players.yml");
			DataFile = new YamlConfiguration();
			if (!DataFileHandle.exists())
				return;
			
			DataFile.load(DataFileHandle);
		}
		catch (Exception e)
		{
			Plugin.log(
				"[PlayerTracking] ERROR: Failed to read players file");
			e.printStackTrace();
			return;
		}
		
		Set<String> players =
			DataFile.getConfigurationSection("Names").getKeys(true);
		
		for (String k : players)
			KnownName.put(DataFile.getString("Names." + k), UUID.fromString(k));
		
		players =
			DataFile.getConfigurationSection("LastLogout").getKeys(true);
		
		for (String k : players)
		{
			UUID uid = UUID.fromString(k);
			long lastLogout = DataFile.getLong("LastLogout." + k);
			LastLogout.put(uid, lastLogout);
		}
	}
	
	void saveData()
	{
		for (Entry<UUID, Long> e : LastLogout.entrySet())
			DataFile.set("LastLogout." + e.getKey().toString(), e.getValue());

		for (Entry<String, UUID> e : KnownName.entrySet())
			DataFile.set("Names." + e.getValue().toString(), e.getKey());

		for (UUID u : OnlinePlayers)
			DataFile.set("LastLogout." + u.toString(), new Date().getTime());
			
		try {
			DataFile.save(DataFileHandle);
		} catch (Exception e) {
			e.printStackTrace();
			Plugin.log("ERROR: Failed to save players file");
			return;
		}
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		UUID uid = event.getPlayer().getUniqueId();
		String name = event.getPlayer().getName().toLowerCase();
		KnownName.put(name, uid);
		LastLogin.put(uid, new Date().getTime());
		OnlinePlayers.add(uid);
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		UUID uid = event.getPlayer().getUniqueId();
		LastLogout.put(uid, new Date().getTime());
		OnlinePlayers.remove(uid);
	}
	
	UUID getUidFromName(String name)
	{
		name = name.toLowerCase();
		if (KnownName.containsKey(name))
			return KnownName.get(name);
		else
			return null; // player never joined
	}
	
	long getLastLogin(UUID uid)
	{
		if (LastLogin.containsKey(uid)) return LastLogin.get(uid);
		else return -1; // shouldn't happen...
	}

	long getLastLogout(UUID uid)
	{
		if (LastLogout.containsKey(uid)) return LastLogout.get(uid);
		else return -1; // player never joined
	}
	
	boolean isOnline(UUID uid) {
		return OnlinePlayers.contains(uid);
	}
}
