package de.ecconia.styms.discord.gaymerbot;

import de.ecconia.java.json.JSONObject;
import de.ecconia.java.json.JSONParser;
import de.ecconia.styms.discord.gaymerbot.data.DataCategory;
import de.ecconia.styms.discord.gaymerbot.data.DataGame;
import de.ecconia.styms.discord.gaymerbot.data.DataManager;
import de.ecconia.styms.discord.gaymerbot.generic.BootException;
import de.ecconia.styms.discord.gaymerbot.generic.GenericBot;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.jetbrains.annotations.Nullable;

public class GaymerBot extends GenericBot
{
	public static void main(String[] args) throws BootException
	{
		DataManager data;
		try
		{
			File dataFile = new File("data.json");
			System.out.println("Starting data import.");
			JSONObject json = (JSONObject) JSONParser.parse(new String(Files.readAllBytes(dataFile.toPath()), StandardCharsets.UTF_8));
			data = new DataManager(json);
			System.out.println("Imported data.");
		}
		catch(NoSuchFileException e)
		{
			System.out.println("Was not able to find data file, using empty data.");
			data = new DataManager();
		}
		catch(IOException e)
		{
			e.printStackTrace();
			System.exit(1);
			throw new RuntimeException("Yes dear Java, this won't be executed anymore.");
		}
		
		try(BufferedReader reader = new BufferedReader(new InputStreamReader(GaymerBot.class.getClassLoader().getResourceAsStream("botID.txt"))))
		{
			new GaymerBot(data, reader.readLine());
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
	}
	
	private static class LogStream extends PrintStream
	{
		public LogStream(PrintStream stream)
		{
			super(stream);
		}
		
		@Override
		public void println(@Nullable String x)
		{
			print(getCurrentPrefix());
			super.println(x);
		}
		
		private String getCurrentPrefix()
		{
			ZonedDateTime now = Instant.now().atZone(ZoneOffset.UTC);
			
			String year = String.valueOf(now.getYear());
			String month = String.valueOf(now.getMonthValue());
			if(month.length() != 2)
			{
				month = "0" + month;
			}
			String day = String.valueOf(now.getDayOfMonth());
			if(day.length() != 2)
			{
				day = "0" + day;
			}
			String hours = String.valueOf(now.getHour());
			if(hours.length() != 2)
			{
				hours = "0" + hours;
			}
			String minutes = String.valueOf(now.getMinute());
			if(minutes.length() != 2)
			{
				minutes = "0" + minutes;
			}
			String seconds = String.valueOf(now.getSecond());
			if(seconds.length() != 2)
			{
				seconds = "0" + seconds;
			}
			
			return "[" + year + "." + month + "." + day + " " + hours + ":" + minutes + ":" + seconds + "]";
		}
	}
	
	//Non static:
	
	public final Guild guild; //TODO: Move to styms constants.
	public final StymsConstants c;
	
	public final DataManager data;
	
	public GaymerBot(DataManager data, String token) throws BootException
	{
		super(token);
		this.data = data;
		
		System.setOut(new LogStream(System.out)); //Only run here, to get that other boi too.
		System.setErr(new LogStream(System.err));
		
		System.out.println("Connected, ID: " + jda.getSelfUser().getId() + " as " + jda.getSelfUser().getName());
		
		data.setGaymerBot(this);
		
		registerGuild(String.valueOf(StymsConstants.GaymerServerDiscordID), new StymsGuildHandler(this));
		guild = jda.getGuildById(StymsConstants.GaymerServerDiscordID);
		c = new StymsConstants(guild);
		
		sendBotReport("ZOOOOMIN~~~~~");
		
		integration();
	}
	
	private void integration()
	{
		//Validation:
		List<DataCategory> dataCategories = data.getCategories();
		if(dataCategories.isEmpty())
		{
			sendBotReport("There is no category to scan whitelisted :/ (Use: <@" + StymsConstants.GaymerBotID + "> add category <id>)");
			return;
		}
		else
		{
			sendBotReport("Validating data:");
			
			boolean dirty = false;
			List<DataCategory> categoriesToRemove = new ArrayList<>();
			for(DataCategory dataCategory : dataCategories)
			{
				Category category = guild.getCategoryById(dataCategory.getId());
				if(category == null)
				{
					categoriesToRemove.add(dataCategory);
					if(dataCategory.getName() != null)
					{
						sendBotReport("Category '" + dataCategory.getName() + "' doesn't exist anymore. Removing.");
					}
					else
					{
						sendBotReport("Category with ID " + dataCategory.getId() + " doesn't exist anymore. Removing.");
					}
					dirty = true;
				}
				else
				{
					List<DataGame> gamesToRemove = new ArrayList<>();
					
					//Check name.
					{
						String name = category.getName();
						String oldName = dataCategory.getName();
						if(!name.equals(oldName))
						{
							dataCategory.setName(name);
							sendBotReport("Category name '" + oldName + "' was changed to '" + name + "'.");
							dirty = true;
						}
					}
					
					//Check game-channels:
					Set<Long> gameChannels = category
							.getChannels()
							.stream()
							.filter(channel -> channel instanceof TextChannel)
							.map(channel -> channel.getIdLong())
							.collect(Collectors.toSet());
					for(DataGame dataGame : dataCategory.getGames())
					{
						if(!gameChannels.remove(dataGame.getGameChannelID()))
						{
							gamesToRemove.add(dataGame);
							if(dataGame.getName() != null)
							{
								sendBotReport("Game '" + dataGame.getName() + "' doesn't exist anymore. Removing.");
							}
							else
							{
								sendBotReport("Game with ID " + dataGame.getGameChannelID() + " doesn't exist anymore. Removing.");
							}
							dirty = true;
						}
						else
						{
							TextChannel channel = guild.getTextChannelById(dataGame.getGameChannelID()); //Doesn't NP, cause of set check earlier.
							
							//Check name.
							String name = channel.getName();
							String oldName = dataGame.getName();
							if(!name.equals(oldName))
							{
								dataGame.setName(name);
								sendBotReport("Game name '" + oldName + "' was changed to '" + name + "'.");
								dirty = true;
							}
							
							//Check pinned message.
							Message pinnedMessage = channel.retrieveMessageById(dataGame.getPlayerlistMessageID()).complete();
							if(pinnedMessage == null)
							{
								sendBotReport("Playerlist message for game <#" + dataGame.getGameChannelID() + "> got deleted.");
								
								PlayerListMessage plm = new PlayerListMessage();
								Message message = channel.sendMessage(plm.toString()).complete();
								message.pin().complete();
								
								dataGame.setPlayerListMessage(plm);
								dataGame.setPlayerListMessageMessage(message);
								dataGame.setPlayerlistMessageID(message.getIdLong());
								dirty = true;
							}
							else
							{
								dataGame.setPlayerListMessageMessage(pinnedMessage);
								PlayerListMessage plm;
								try
								{
									plm = new PlayerListMessage(pinnedMessage);
									dataGame.setPlayerListMessage(plm);
								}
								catch(Exception e)
								{
									sendBotReport(e.getMessage());
									sendBotReport("Resetting text of game <#" + dataGame.getGameChannelID() + ">");
									plm = new PlayerListMessage();
									pinnedMessage.editMessage(plm.toString()).complete();
									dataGame.setPlayerListMessage(plm);
									continue;
								}
								
								if(plm.getRoleID() == null)
								{
									sendBotReport("No role assigned for <#" + dataGame.getGameChannelID() + ">. Use: <@" + StymsConstants.GaymerBotID + "> add role for <#" + dataGame.getGameChannelID() + "> <role name>");
								}
								else
								{
									Role role = guild.getRoleById(plm.getRoleID());
									if(role == null)
									{
										sendBotReport("Role for <#" + dataGame.getGameChannelID() + "> does not exist anymore.");
										plm.setRoleID(null);
										dataGame.getPlmMessage().editMessage(plm.toString()).complete();
									}
								}
								//Else do nothing for now.
								
								//TODO: Role and message confirmations bla bla.
							}
						}
					}
					
					for(DataGame dataGame : gamesToRemove)
					{
						dataCategory.removeGame(dataGame);
					}
				}
			}
			
			for(DataCategory dataCategory : categoriesToRemove)
			{
				data.removeCategory(dataCategory);
			}
			
			//Save changes.
			if(dirty)
			{
				data.save();
			}
			
			sendBotReport("Done.");
		}
	}
	
	public void addCategory(String stringID)
	{
		Category cat = guild.getCategoryById(stringID);
		if(cat == null)
		{
			sendBotReport("No category with that ID found.");
			return;
		}
		DataCategory dataCategory = new DataCategory(cat.getIdLong());
		dataCategory.setName(cat.getName());
		sendBotReport("Category name: " + cat.getName());
		int gamesFound = 0;
		for(GuildChannel channel : cat.getChannels())
		{
			if(channel instanceof TextChannel)
			{
				TextChannel textChannel = (TextChannel) channel;
				//Assume its not prepared, so prepare it...
				PlayerListMessage plm = new PlayerListMessage();
				Message message = textChannel.sendMessage(plm.toString()).complete();
				message.pin().complete();
				DataGame game = new DataGame(textChannel.getIdLong(), message.getIdLong());
				game.setPlayerListMessage(plm);
				game.setPlayerListMessageMessage(message);
				game.setName(textChannel.getName());
				dataCategory.addGame(game);
				gamesFound++;
			}
		}
		sendBotReport("Found " + gamesFound + " games.");
		
		data.addCategory(dataCategory);
		data.save();
	}
	
	public void sendBotReport(String message)
	{
		c.channelBot.sendMessage(message).complete();
	}
	
	public void exception(String string, Throwable e)
	{
		String formatted = format(string, e);
		System.out.println("[ERROR] " + formatted);
		if(formatted.length() <= 1900)
		{
			sendBotReport("[ERROR] " + formatted);
		}
		else
		{
			String[] lines = formatted.split("\n");
			String tmp = "[ERROR] " + lines[0];
			for(int i = 1; i < lines.length; i++)
			{
				if(tmp.length() + 1 + lines.length < 1900)
				{
					tmp += '\n' + lines[i];
				}
				else
				{
					sendBotReport(tmp);
					tmp = lines[i];
				}
			}
			sendBotReport(tmp);
		}
	}
	
	public String format(String message, Throwable t)
	{
		StackTraceElement catchOrigin = Thread.currentThread().getStackTrace()[3];
		String catcher = " @(" + catchOrigin.getClassName() + ":" + catchOrigin.getLineNumber() + ")";
		String res = "Exception in RS-Bot2: " + message + catcher + "\n";
		
		List<Throwable> issues = new ArrayList<>();
		
		issues.add(t);
		while(t.getCause() != null)
		{
			issues.add(t);
			t = t.getCause();
		}
		
		for(int i = issues.size() - 1; i >= 0; i--)
		{
			t = issues.get(i);
			
			res += "Exception: " + t.getClass().getSimpleName() + ": " + (t.getMessage() != null ? t.getMessage() + " " : "") + "\n";
			for(StackTraceElement el : t.getStackTrace())
			{
				res += "-> " + el.getClassName() + "." + el.getMethodName() + "(" + el.getFileName() + ":" + el.getLineNumber() + ")" + "\n";
			}
		}
		
		return res;
	}
	
	//Events:
	
	@Override
	public void onPrivateMessageReceived(PrivateMessageReceivedEvent event)
	{
	}
}
