package de.ecconia.styms.discord.gaymerbot;

import de.ecconia.styms.discord.gaymerbot.data.DataGame;
import de.ecconia.styms.discord.gaymerbot.generic.GenericGuild;
import de.ecconia.styms.discord.gaymerbot.generic.H;
import java.util.List;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

public class StymsGuildHandler extends GenericGuild
{
	private final GaymerBot bot;
	
	public StymsGuildHandler(GaymerBot bot)
	{
		this.bot = bot;
	}
	
	@Override
	public void onMessage(Message message)
	{
		User author = message.getAuthor();
		if(author == message.getJDA().getSelfUser())
		{
			return; //Never talk to yourself.
		}
		
		MessageChannel channel = message.getChannel();
		String raw = message.getContentRaw();
		
		if(channel == bot.c.channelBot)
		{
			if(raw.contains("<@!" + StymsConstants.GaymerBotID + ">") || raw.contains("<@" + StymsConstants.GaymerBotID + ">"))
			{
				String copiedRaw = raw;
				if(copiedRaw.startsWith("<@!" + StymsConstants.GaymerBotID + ">"))
				{
					copiedRaw = copiedRaw.substring(("<@!" + StymsConstants.GaymerBotID + ">").length());
				}
				else if(copiedRaw.startsWith("<@" + StymsConstants.GaymerBotID + ">"))
				{
					copiedRaw = copiedRaw.substring(("<@" + StymsConstants.GaymerBotID + ">").length());
				}
				else
				{
					bot.sendBotReport("I don't understand what you mean. (You can make a Pull-Request on my source-code on github to improve my language skills).");
					return;
				}
				
				copiedRaw = copiedRaw.trim();
				
				if(copiedRaw.startsWith("add category "))
				{
					if(!isManager(message.getMember()))
					{
						bot.sendBotReport("Only (at)Manager can use this command.");
						return;
					}
					
					copiedRaw = copiedRaw.substring("add category ".length());
					if(!copiedRaw.matches("[0-9]+"))
					{
						bot.sendBotReport("Please only supply the ID of the category. Got: '" + copiedRaw + "'");
						return;
					}
					try
					{
						Long.parseLong(copiedRaw);
					}
					catch(NumberFormatException e)
					{
						bot.sendBotReport("Was not able to parse category ID. Got: '" + copiedRaw + "'");
						return;
					}
					
					bot.addCategory(copiedRaw);
					return;
				}
				else if(copiedRaw.startsWith("add role for "))
				{
					if(!isManager(message.getMember()))
					{
						bot.sendBotReport("Only (at)Manager can use this command.");
						return;
					}
					
					copiedRaw = copiedRaw.substring("add role for ".length()).trim();
					
					String[] arguments = copiedRaw.split(" +", 2);
					if(arguments.length < 2)
					{
						bot.sendBotReport("Nope cause: format is `add role for <#channelID> <roleName>`");
						return;
					}
					
					if(!arguments[0].matches("<#[0-9]+>"))
					{
						bot.sendBotReport("Expected channel but got: `" + arguments[0] + "`");
						return;
					}
					long channelID;
					try
					{
						channelID = Long.parseLong(arguments[0].substring(2, arguments[0].length() - 1));
					}
					catch(NumberFormatException e)
					{
						bot.sendBotReport("Not able to parse channel ID.");
						return;
					}
					
					DataGame game = bot.data.getGameByChannel(channelID);
					if(game == null)
					{
						bot.sendBotReport("That channel/game is not know by the bot.");
						return;
					}
					
					if(game.getPlm().getRoleID() != null)
					{
						bot.sendBotReport("Game already has a role assigned.");
						return;
					}
					
					List<Role> possibleMatches = bot.guild.getRolesByName(arguments[1], true);
					if(!possibleMatches.isEmpty())
					{
						bot.sendBotReport("Other roles match this name: <@&" + possibleMatches.stream().map(role -> role.getId()).collect(Collectors.joining("> <@&")) + ">");
						return;
					}
					
					Role newRole = bot.guild.createRole().setName(arguments[1]).complete();
					game.getPlm().setRoleID(newRole.getIdLong());
					game.getPlmMessage().editMessage(game.getPlm().toString()).complete();
					bot.sendBotReport("Done.");
					
					return;
				}
				else if(copiedRaw.startsWith("set role for "))
				{
					if(!isManager(message.getMember()))
					{
						bot.sendBotReport("Only (at)Manager can use this command.");
						return;
					}
					
					copiedRaw = copiedRaw.substring("set role for ".length()).trim();
					
					String[] arguments = copiedRaw.split(" +");
					if(arguments.length != 2)
					{
						bot.sendBotReport("Nope cause: format is `set role for <#channelID> <@&roleID>`");
						return;
					}
					
					if(!arguments[0].matches("<#[0-9]+>"))
					{
						bot.sendBotReport("Expected channel but got: `" + arguments[0] + "`");
						return;
					}
					long channelID;
					try
					{
						channelID = Long.parseLong(arguments[0].substring(2, arguments[0].length() - 1));
					}
					catch(NumberFormatException e)
					{
						bot.sendBotReport("Not able to parse channel ID.");
						return;
					}
					
					DataGame game = bot.data.getGameByChannel(channelID);
					if(game == null)
					{
						bot.sendBotReport("That channel/game is not know by the bot.");
						return;
					}
					
					if(game.getPlm().getRoleID() != null)
					{
						bot.sendBotReport("Game already has a role assigned.");
						return;
					}
					
					if(!arguments[1].matches("<@&[0-9]+>"))
					{
						bot.sendBotReport("Expected role but got: `" + arguments[1] + "`");
						return;
					}
					long roleID;
					try
					{
						roleID = Long.parseLong(arguments[1].substring(3, arguments[1].length() - 1));
					}
					catch(NumberFormatException e)
					{
						bot.sendBotReport("Not able to parse role ID.");
						return;
					}
					
					System.out.println(roleID);
					Role role = bot.guild.getRoleById(roleID);
					if(role == null)
					{
						bot.sendBotReport("Cannot get bot by ID, ¿?");
						return;
					}
					
					game.getPlm().setRoleID(role.getIdLong());
					game.getPlmMessage().editMessage(game.getPlm().toString()).complete();
					bot.sendBotReport("Done.");
					
					return;
				}
				else if(copiedRaw.startsWith("add "))
				{
					copiedRaw = copiedRaw.substring("add ".length()).trim();
					
					if(!copiedRaw.matches("<#[0-9]+>"))
					{
						bot.sendBotReport("Expected channel but got: `" + copiedRaw + "`");
						return;
					}
					long channelID;
					try
					{
						channelID = Long.parseLong(copiedRaw.substring(2, copiedRaw.length() - 1));
					}
					catch(NumberFormatException e)
					{
						bot.sendBotReport("Not able to parse channel ID.");
						return;
					}
					
					DataGame game = bot.data.getGameByChannel(channelID);
					if(game == null)
					{
						bot.sendBotReport("That channel/game is not know by the bot.");
						return;
					}
					
					Long roleID = game.getPlm().getRoleID();
					if(roleID == null)
					{
						bot.sendBotReport("Game doesn't have a role yet. :( Annoy Manager to fix it.");
						return;
					}
					
					Role role = bot.guild.getRoleById(roleID);
					if(role == null)
					{
						bot.sendBotReport("Could not get the role, is it deleted?");
						return;
					}
					
					Member member = message.getMember();
					bot.guild.addRoleToMember(member, role).complete();
					if(!game.getPlm().getMembers().contains(member.getIdLong()))
					{
						game.getPlm().getMembers().add(member.getIdLong());
						game.getPlmMessage().editMessage(game.getPlm().toString()).complete();
					}
					
					bot.sendBotReport("Added <@" + member.getId() + "> to <#" + channelID + ">.");
					
					return;
				}
				
				bot.sendBotReport("I am not sure what you mean, try these:\n"
						+ "`@GaymerBot add #<game-channel>` Adds you to the list of that game.\n"
						+ "Management only:\n"
						+ "`@GaymerBot add category <category-id>` Whitelists a category for games.\n"
						+ "`@GaymerBot add role for #<game-channel> <role name>` Creates a new role for a game."
						+ "`@GaymerBot set role for #<game-channel> <@&roleID>` Uses an existing role for a game.");
			}
			System.out.println("'" + raw + "'");
		}
	}
	
	private boolean isManager(Member member)
	{
		for(Role role : member.getRoles())
		{
			if(role.getIdLong() == bot.c.managerRoleID)
			{
				return true;
			}
		}
		return false;
	}
	
	//Use?
	private static String escapeString(String s)
	{
		//TBI: Regex, or different method?
		s = s.replace("\\t", "\t");
		s = s.replace("\\r", "\r");
		s = s.replace("\\n", "\n");
		s = s.replace("\\f", "\f");
		s = s.replace("\\b", "\b");
		s = s.replace("\\/", "/");
		s = s.replace("\\\"", "\"");
		s = s.replace("\\\\", "\\");
		//TBI: Unicode chars?
		
		return s;
	}
	
	@Override
	public void onMemberJoin(Member member)
	{
		String nickname = member.getNickname();
		User user = member.getUser();
		String name = user.getName();
		long id = user.getIdLong();
		
		bot.sendBotReport("JOIN: **" + H.discordEscape(name) + "** (" + H.discordEscape(nickname) + ") " + id);
	}
	
	@Override
	public void onMemberLeave(Member member)
	{
		String nickname = member.getNickname();
		User user = member.getUser();
		String name = user.getName();
		long id = user.getIdLong();
		
		bot.sendBotReport("LEAVE: **" + H.discordEscape(name) + "** (" + H.discordEscape(nickname) + ") " + id);
	}
}
