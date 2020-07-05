package us.scandic.simpleGeyserVL.listener;

import java.util.UUID;

public class QueuedVote
{
	UUID PlayerID;
	String Service;
	long Timestamp;
	
	public QueuedVote(UUID uid, String service, long ts)
	{
		PlayerID = uid;
		Service = service;
		Timestamp = ts;
	}
}
