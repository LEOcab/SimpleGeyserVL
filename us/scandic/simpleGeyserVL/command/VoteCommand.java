package us.scandic.simpleGeyserVL.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import us.scandic.simpleGeyserVL.SimpleGeyserVL;
import us.scandic.simpleGeyserVL.listener.VoteListener;


public class VoteCommand implements CommandExecutor {
	
	
	SimpleGeyserVL simpleGeyserVL;
	String message;
	

	public VoteCommand(SimpleGeyserVL simpleGeyserVL2) {
		simpleGeyserVL2 = simpleGeyserVL;
	}


	@Override
	public boolean onCommand(CommandSender sender, Command command, String arg2, String[] arg3) {
		message = VoteListener.getVoteCommandMessage();
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw \"" + sender.getName() + "\" " + message);

		return true;
	}

}
