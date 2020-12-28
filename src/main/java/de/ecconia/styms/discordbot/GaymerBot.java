package de.ecconia.styms.discordbot;

import de.ecconia.java.json.JSONObject;
import de.ecconia.java.json.JSONParser;
import de.ecconia.styms.discordbot.data.DataCategory;
import de.ecconia.styms.discordbot.data.DataGame;
import de.ecconia.styms.discordbot.data.DataManager;
import de.ecconia.styms.discordbot.generic.BootException;
import de.ecconia.styms.discordbot.generic.GenericBot;
import de.ecconia.styms.discordbot.generic.H;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateNameEvent;
import net.dv8tion.jda.api.exceptions.HierarchyException;
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

//		Thread saveThread = new Thread(() -> {
//			try
//			{
//				while(!Thread.currentThread().isInterrupted())
//				{
//					if(users.isDirty())
//					{
//						users.save();
//					}
//
//					try
//					{
//						Thread.sleep(1000 * 60 * 5); //Every 5 minutes.
//					}
//					catch(InterruptedException e)
//					{
//						break;
//					}
//				}
//			}
//			catch(Throwable e)
//			{
//				exception("**Please fix and restart!**", e);
//			}
//			System.out.println("Turned off auto-save thread.");
//		}, "Inactive timer thread.");
//		saveThread.start();

//		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//			System.out.println("Waiting for auto-saver to shut down...");
//			try
//			{
//				saveThread.interrupt();
//				saveThread.join(5000);
//			}
//			catch(InterruptedException e)
//			{
//				e.printStackTrace();
//			}
//			System.out.println("Saving users...");
//			try
//			{
//				users.save();
//				System.out.println("Done saving users.");
//			}
//			catch(Exception e)
//			{
//				exception("**Could not save userfile**", e);
//			}
//		}));
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

//	public void checkMemberList()
//	{
//		sendBotReport("Doing user check...");
//		Set<String> usersByDID = getUsers().getUserDIDCopy();
//		for(Member member : guild.getMembers())
//		{
//			BotUser user = users.getUser(member.getUser().getId());
//			boolean shouldBeLinked = user != null;
//			boolean relayAccess = false;
//			boolean linked = false;
//			String skillrank = null;
//
//			if(shouldBeLinked)
//			{
//				usersByDID.remove(user.getDiscordID());
//			}
//
//			List<Role> rolesToRemove = new ArrayList<>();
//			List<Role> rolesToAdd = new ArrayList<>();
//
//			List<Role> rolesList = member.getRoles();
//			if(!rolesList.isEmpty())
//			{
//				//Roles (infrastructure):
//				//- Inactive
//				//- Linked
//				//- Chat-Relay
//				//- Visitors, Learners, Regulars, Builders, Seniors
//
//				for(Role role : rolesList)
//				{
//					String roleName = role.getName();
//					if("Linked".equals(roleName))
//					{
//						linked = true;
//					}
//					else if("Chat-Relay".equals(roleName))
//					{
//						relayAccess = true;
//					}
//					else if(c.isSkillRank(roleName))
//					{
//						if(skillrank != null)
//						{
//							String realRank = null;
//							if(shouldBeLinked)
//							{
//								realRank = user.getRank();
//							}
//							warn("Discord user <@" + member.getUser().getId() + "> has more than one skillrank: " + skillrank + " - " + roleName
//									+ (realRank != null ? "\nRank should be: **" + realRank + "**" : ""));
//
//							if(!shouldBeLinked)
//							{
//								rolesToRemove.add(c.getSkillRank(roleName));
//								sendBotReport("[INFO] Removed the **" + roleName + "** role from <@" + member.getUser().getId() + ">, since he is not linked.");
//							}
//							else
//							{
//								if(realRank.equals(roleName))
//								{
//									rolesToRemove.add(c.getSkillRank(skillrank));
//									sendBotReport("[INFO] Removed the **" + skillrank + "** role from <@" + member.getUser().getId() + ">.");
//									skillrank = roleName;
//								}
//								else
//								{
//									rolesToRemove.add(c.getSkillRank(roleName));
//									sendBotReport("[INFO] Removed the **" + roleName + "** role from <@" + member.getUser().getId() + ">.");
//								}
//							}
//						}
//						else
//						{
//							skillrank = roleName;
//						}
//					}
//				}
//			}
//
//			if(shouldBeLinked)
//			{
//				if(!linked)
//				{
//					warn("Discord user <@" + member.getUser().getId() + "> misses the Linked role.");
//					rolesToAdd.add(c.roleLinked);
//					sendBotReport("[INFO] Assigned the **Linked** role to <@" + member.getUser().getId() + ">");
//				}
//
//				if(!relayAccess)
//				{
//					warn("Discord user <@" + member.getUser().getId() + "> misses the Chat-Relay role, intentional? Please fix asap or ignore this message.");
//				}
//
//				{
//					String nickname = member.getNickname();
//					String username = user.getUsername();
//					String name = member.getUser().getName();
//
//					if(username.equals(name))
//					{
//						if(member.getNickname() != null)
//						{
//							sendBotReport("Removed nickname of <@" + member.getUser().getId() + "> cause username matches ign-name.");
//							rename(member, null);
//						}
//					}
//					else if(!username.equals(nickname))
//					{
//						sendBotReport("Changed/Applied nickname to <@" + member.getUser().getId() + "> to match ign-name.");
//						rename(member, username);
//					}
//				}
//
//				if(skillrank == null)
//				{
//					String realRank = user.getRank();
//					warn("Discord user <@" + member.getUser().getId() + "> is linked but has no skill rank!\nRank should be: **" + realRank + "**");
//					rolesToAdd.add(c.getSkillRank(realRank));
//					sendBotReport("[INFO] Assigned the **" + realRank + "** role to <@" + member.getUser().getId() + ">");
//				}
//			}
//			else
//			{
//				if(linked)
//				{
//					warn("Discord user <@" + member.getUser().getId() + "> is not linked, but has the Linked role.");
//					rolesToRemove.add(c.roleLinked);
//					sendBotReport("[INFO] Removed the **Linked** role from <@" + member.getUser().getId() + ">");
//				}
//
//				if(relayAccess)
//				{
//					warn("Discord user <@" + member.getUser().getId() + "> is not linked, but has Chat-Relay access role.");
//					rolesToRemove.add(c.roleChatAccess);
//					sendBotReport("[INFO] Removed the **Chat-Relay** role from <@" + member.getUser().getId() + ">");
//				}
//
//				if(skillrank != null)
//				{
//					warn("Discord user <@" + member.getUser().getId() + "> is not linked but has a skill rank!");
//					rolesToRemove.add(c.getSkillRank(skillrank));
//					sendBotReport("[INFO] Removed the **" + skillrank + "** role from <@" + member.getUser().getId() + ">");
//				}
//			}
//
//			if(!rolesToAdd.isEmpty())
//			{
//				for(Role role : rolesToAdd)
//				{
//					guild.addRoleToMember(member, role).complete();
//				}
//			}
//
//			if(!rolesToRemove.isEmpty())
//			{
//				for(Role role : rolesToRemove)
//				{
//					guild.removeRoleFromMember(member, role).complete();
//				}
//			}
//		}
//
//		if(usersByDID.isEmpty())
//		{
//			sendBotReport("No discord user had to be removed from storage.");
//		}
//		for(String didToRemove : usersByDID)
//		{
//			BotUser user = getUsers().getUser(didToRemove);
//			getUsers().unlink(didToRemove);
//			sendBotReport("Minecraft player **" + user.getUsername() + "** (`" + user.getUuid() + "`) left the server and is now unlinked.");
//		}
//
//		sendBotReport("All okay.");
//		System.out.println("Validation done.");
//	}
	
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
