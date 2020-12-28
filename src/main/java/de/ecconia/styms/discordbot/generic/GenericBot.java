package de.ecconia.styms.discordbot.generic;

import java.util.HashMap;
import java.util.Map;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class GenericBot extends ListenerAdapter
{
	protected final JDA jda;
	
	private final Map<String, GenericGuild> guilds = new HashMap<>();
	
	public GenericBot(String token) throws BootException
	{
		try
		{
			jda = JDABuilder.create(token,
					GatewayIntent.GUILD_MESSAGES, //Messages received by members.
					GatewayIntent.GUILD_MEMBERS, //For caching and join/leaves
					GatewayIntent.DIRECT_MESSAGES //Receive private messages.
					)
					.build();
			jda.awaitReady();
			jda.addEventListener(this);
		}
		catch(LoginException e)
		{
			e.printStackTrace();
			throw new BootException("Could not login as bot.");
		}
		catch(InterruptedException e)
		{
			e.printStackTrace();
			throw new BootException("Login interrupted.");
		}
	}
	
	public void registerGuild(String id, GenericGuild guild)
	{
		guilds.put(id, guild);
	}
	
	private GenericGuild getGuild(String id)
	{
		return guilds.get(id);
	}
	
	@Override
	public void onPrivateMessageReceived(PrivateMessageReceivedEvent event)
	{
		//TBI: Hmmm....
	}
	
	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event)
	{
		GenericGuild guild = getGuild(event.getGuild().getId());
		if(guild != null)
		{
			guild.onMessage(event.getMessage());
		}
		else
		{
			//Report event from unregistered guild!
		}
	}
	
	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event)
	{
		GenericGuild guild = getGuild(event.getGuild().getId());
		if(guild != null)
		{
			guild.onMemberJoin(event.getMember());
		}
		else
		{
			//Report event from unregistered guild!
		}
	}
	
	@Override
	public void onGuildMemberLeave(GuildMemberLeaveEvent event)
	{
		GenericGuild guild = getGuild(event.getGuild().getId());
		if(guild != null)
		{
			guild.onMemberLeave(event.getMember());
		}
		else
		{
			//Report event from unregistered guild!
		}
	}
}
