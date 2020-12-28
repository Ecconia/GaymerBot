package de.ecconia.styms.discordbot;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;

public class StymsConstants
{
	public static final long GaymerServerDiscordID = 792889414455132190L;
	public static final String GaymerBotID = "793035696846798888";
	public long managerRoleID = 792894466599944203L;
	
	public final TextChannel channelBot; //Channel the bot talks in.
	
	public StymsConstants(Guild styms)
	{
		channelBot = styms.getTextChannelById("793146695965081620");
	}
}
