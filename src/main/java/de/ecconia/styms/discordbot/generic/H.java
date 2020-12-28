package de.ecconia.styms.discordbot.generic;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;

public class H
{
	public static void answer(PrivateMessageReceivedEvent m, String content)
	{
		answer(m.getMessage(), content);
	}
	
	public static void answer(Message m, String content)
	{
		answer(m.getChannel(), content);
	}
	
	public static void answer(MessageChannel c, String content)
	{
		c.sendMessage(content).complete();
	}
	
	public static void setTopic(TextChannel channel, String content)
	{
		channel.getManager().setTopic(content).complete();
	}
	
	public static String discordEscape(String input)
	{
		if(input == null)
		{
			return null;
		}
		
		input = input.replace("\\", "\\\\");
		input = input.replace("_", "\\_");
		input = input.replace("*", "\\*");
		input = input.replace("~", "\\~");
		input = input.replace("`", "\\`");
		input = input.replace("|", "\\|");
		input = input.replace(":", "\\:");
		
		return input;
	}
}
