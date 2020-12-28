package de.ecconia.styms.discordbot.data;

import de.ecconia.java.json.JSONArray;
import de.ecconia.java.json.JSONObject;
import de.ecconia.styms.discordbot.GaymerBot;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class DataManager
{
	private GaymerBot gaymerBot;
	
	private final List<DataCategory> categories = new ArrayList<>();
	
	public DataManager()
	{
	}
	
	public DataManager(JSONObject json)
	{
		JSONArray array = json.getArray("categories");
		for(Object o : array.getEntries())
		{
			JSONObject object = JSONArray.asObject(o);
			categories.add(new DataCategory(object));
		}
	}
	
	public void setGaymerBot(GaymerBot gaymerBot)
	{
		this.gaymerBot = gaymerBot;
	}
	
	public List<DataCategory> getCategories()
	{
		return categories;
	}
	
	public void addCategory(DataCategory dataCategory)
	{
		categories.add(dataCategory);
	}
	
	public void save()
	{
		JSONObject root = new JSONObject();
		
		JSONArray categories = new JSONArray();
		for(DataCategory cat : this.categories)
		{
			categories.add(cat.toJSON());
		}
		root.put("categories", categories);
		
		String jsonPlain = root.printJSON();
		
		File file = new File("data.json");
		try
		{
			Files.write(file.toPath(), jsonPlain.getBytes(StandardCharsets.UTF_8));
		}
		catch(IOException e)
		{
			e.printStackTrace();
			gaymerBot.exception("Could not save data.", e);
		}
	}
	
	public void removeCategory(DataCategory dataCategory)
	{
		categories.remove(dataCategory);
	}
	
	public DataGame getGameByChannel(long channelID)
	{
		for(DataCategory cat : categories)
		{
			DataGame game = cat.getGameByChannel(channelID);
			if(game != null)
			{
				return game;
			}
		}
		return null;
	}
}
