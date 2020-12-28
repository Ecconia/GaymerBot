package de.ecconia.styms.discord.gaymerbot;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.entities.Message;

public class PlayerListMessage
{
	private final List<Long> members = new ArrayList<>();
	
	private Long roleID;
	
	public PlayerListMessage()
	{
		//Empty message.
	}
	
	public PlayerListMessage(Message message)
	{
		//From message.
		String rawText = message.getContentRaw();
		System.out.println("Raw message text:\n" + rawText + "\n-over-");
		
		//Version 1:
		if(!parseV1(rawText))
		{
			throw new RuntimeException("Could not parse Version 1.");
		}
	}
	
	private boolean parseV1(String text)
	{
		if(text.isEmpty())
		{
			return false;
		}
		
		String[] lines = text.split("\n");
		if(lines.length < 3)
		{
			return false;
		}
		
		//Role:
		if(!lines[0].equals("Role: <unknown>"))
		{
			if(!lines[0].startsWith("Role: <@&"))
			{
				return false;
			}
			String rolePart = lines[0].substring("Role: <@&".length(), lines[0].length() - 1);
			System.out.println("RoleID: '" + rolePart + "'");
			try
			{
				roleID = Long.parseLong(rolePart);
			}
			catch(NumberFormatException e)
			{
				System.out.println("Could not parse role-id.");
				return false;
			}
		}
		
		//Members:
		if(!lines[1].equals("Players:"))
		{
			return false;
		}
		if(lines[2].equals("-none-"))
		{
			if(lines.length != 3)
			{
				return false;
			}
			return true; //No members play this game.
		}
		for(int i = 2; i < lines.length; i++)
		{
			String line = lines[i];
			if(!line.startsWith("- <@"))
			{
				return false;
			}
			String memberID = line.substring("- <@".length(), line.length() - 1);
			try
			{
				members.add(Long.parseLong(memberID));
			}
			catch(NumberFormatException e)
			{
				System.out.println("Could not parse member-id.");
				return false;
			}
		}
		
		return true;
	}
	
	public Long getRoleID()
	{
		return roleID;
	}
	
	public List<Long> getMembers()
	{
		return members;
	}
	
	public String toString()
	{
		return "Role: " + (roleID == null ? "<unknown>" : "<@&" + roleID + ">") + "\n"
				+ "Players:" + "\n"
				+ (members.isEmpty() ? "-none-" : "- <@" + members.stream().map(e -> String.valueOf(e)).collect(Collectors.joining(">\n- <@")) + ">");
	}
	
	public void setRoleID(Long roleID)
	{
		this.roleID = roleID;
	}
}
