package de.ecconia.styms.discordbot.data;

import de.ecconia.java.json.JSONArray;
import de.ecconia.java.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class DataCategory
{
	private final long id;
	private final List<DataGame> games = new ArrayList<>();
	
	private String name;
	
	public DataCategory(long id)
	{
		this.id = id;
	}
	
	public DataCategory(JSONObject json)
	{
		id = json.getLong("id");
		JSONArray games = json.getArray("games");
		for(Object o : games.getEntries())
		{
			JSONObject object = JSONArray.asObject(o);
			this.games.add(new DataGame(object));
		}
		this.name = json.getStringOrNull("name");
	}
	
	public void addGame(DataGame dataGame)
	{
		games.add(dataGame);
	}
	
	public long getId()
	{
		return id;
	}
	
	public String getName()
	{
		return name;
	}
	
	public List<DataGame> getGames()
	{
		return games;
	}
	
	public JSONObject toJSON()
	{
		JSONObject json = new JSONObject();
		json.put("id", id);
		
		JSONArray games = new JSONArray();
		for(DataGame game : this.games)
		{
			games.add(game.toJSON());
		}
		json.put("games", games);
		
		if(name != null)
		{
			json.put("name", name);
		}
		
		return json;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public void removeGame(DataGame dataGame)
	{
		games.remove(dataGame);
	}
	
	public DataGame getGameByChannel(long channelID)
	{
		for(DataGame game : games)
		{
			if(game.getGameChannelID() == channelID)
			{
				return game;
			}
		}
		return null;
	}
}
