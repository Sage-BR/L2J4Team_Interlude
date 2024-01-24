
package com.l2j4team.gameserver.handler;

import com.l2j4team.gameserver.handler.voicedcommandhandlers.VoicedDressMe;
import com.l2j4team.gameserver.model.actor.instance.Player;

import java.util.HashMap;
import java.util.Map;

public class CustomBypassHandler
{
	
	private static CustomBypassHandler _instance = null;
	private final Map<String, ICustomByPassHandler> _handlers;
	
	private CustomBypassHandler()
	{
		_handlers = new HashMap<>();
		
		registerCustomBypassHandler(new VoicedDressMe());
	}
	
	public static CustomBypassHandler getInstance()
	{
		if (_instance == null)
		{
			_instance = new CustomBypassHandler();
		}
		
		return _instance;
	}
	
	public void registerCustomBypassHandler(final ICustomByPassHandler handler)
	{
		for (final String s : handler.getByPassCommands())
		{
			_handlers.put(s, handler);
		}
	}
	
	public void handleBypass(final Player player, final String command)
	{
		String cmd = "";
		String params = "";
		final int iPos = command.indexOf(" ");
		if (iPos != -1)
		{
			cmd = command.substring(7, iPos);
			params = command.substring(iPos + 1);
		}
		else
		{
			cmd = command.substring(7);
		}
		final ICustomByPassHandler ch = _handlers.get(cmd);
		if (ch != null)
		{
			ch.handleCommand(cmd, player, params);
		}
		else
		{
			
		}
	}
}