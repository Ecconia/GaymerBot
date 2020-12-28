package de.ecconia.styms.discord.gaymerbot.data;

import de.ecconia.java.json.JSONObject;
import de.ecconia.styms.discord.gaymerbot.PlayerListMessage;
import net.dv8tion.jda.api.entities.Message;

public class DataGame
{
	private final long gameChannelID;
	
	private long playerlistMessageID;
	private String name;
	
	private PlayerListMessage plm;
	private Message plmMessage;
	
	public DataGame(long gameChannelID, long playerlistMessageID)
	{
		this.gameChannelID = gameChannelID;
		this.playerlistMessageID = playerlistMessageID;
	}
	
	public DataGame(JSONObject json)
	{
		gameChannelID = json.getLong("channelID");
		playerlistMessageID = json.getLong("playerlistID");
		name = json.getStringOrNull("name");
	}
	
	public long getGameChannelID()
	{
		return gameChannelID;
	}
	
	public long getPlayerlistMessageID()
	{
		return playerlistMessageID;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public void setPlayerlistMessageID(long playerlistMessageID)
	{
		this.playerlistMessageID = playerlistMessageID;
	}
	
	public String getName()
	{
		return name;
	}
	
	public JSONObject toJSON()
	{
		JSONObject json = new JSONObject();
		
		json.put("channelID", gameChannelID);
		json.put("playerlistID", playerlistMessageID);
		if(name != null)
		{
			json.put("name", name);
		}
		
		return json;
	}
	
	public void setPlayerListMessage(PlayerListMessage plm)
	{
		System.out.println("Adding plm: " + plm.hashCode());
		this.plm = plm;
	}
	
	public void setPlayerListMessageMessage(Message plmMessage)
	{
		this.plmMessage = plmMessage;
	}
	
	public Message getPlmMessage()
	{
		return plmMessage;
	}
	
	public PlayerListMessage getPlm()
	{
		return plm;
	}
}
