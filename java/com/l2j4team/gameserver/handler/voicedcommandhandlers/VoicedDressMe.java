package com.l2j4team.gameserver.handler.voicedcommandhandlers;

import com.l2j4team.Config;
import com.l2j4team.gameserver.data.cache.HtmCache;
import com.l2j4team.gameserver.handler.ICustomByPassHandler;
import com.l2j4team.gameserver.handler.IVoicedCommandHandler;
import com.l2j4team.gameserver.model.actor.instance.Player;
import com.l2j4team.gameserver.network.serverpackets.NpcHtmlMessage;

import java.text.SimpleDateFormat;
import java.util.Date;

public class VoicedDressMe implements IVoicedCommandHandler, ICustomByPassHandler
{
	private static String[] _voicedCommands =
	{
		Config.DRESS_ME_COMMAND
	};
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		
		if (!Config.ALLOW_DRESS_ME_SYSTEM)
		{
			
			activeChar.sendMessage("El Sistema de Skin Esta Desactivado.");
			return false;
		}
		
		if (command.startsWith(Config.DRESS_ME_COMMAND))
		{
			showHtm(activeChar);
		}
		
		return true;
	}
	
	private static void showHtm(Player player)
	{
		NpcHtmlMessage htm = new NpcHtmlMessage(1);
		String text = HtmCache.getInstance().getHtm("data/html/dressme/index.htm");
		
		htm.setHtml(text);
		
		{
			htm.replace("%time%", sdf.format(new Date(System.currentTimeMillis())));
			htm.replace("%dat%", (new SimpleDateFormat("dd/MM/yyyy")).format(new Date(System.currentTimeMillis())));
			
		}
		
		player.sendPacket(htm);
	}
	
	static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
	
	@Override
	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}
	
	@Override
	public String[] getByPassCommands()
	{
		return new String[]
		{
			"dressme_back"
		};
	}
	
	private enum CommandEnum
	{
		dressme_back,
	}
	
	@Override
	public void handleCommand(String command, Player player, String parameters)
	{
		CommandEnum comm = CommandEnum.valueOf(command);
		
		if (comm == null)
		{
			return;
		}
		
		switch (comm)
		{
			case dressme_back:
			{
				showHtm(player);
			}
				break;
		}
	}
}