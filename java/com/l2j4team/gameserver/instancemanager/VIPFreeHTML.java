package com.l2j4team.gameserver.instancemanager;

import com.l2j4team.gameserver.handler.admincommandhandlers.AdminCustom;
import com.l2j4team.gameserver.model.actor.instance.Player;

public class VIPFreeHTML implements Runnable
{
	private final Player _activeChar;
	
	public VIPFreeHTML(Player activeChar)
	{
		_activeChar = activeChar;
	}
	
	@Override
	public void run()
	{
		if (_activeChar.isOnline())
			AdminCustom.showHtml(_activeChar);
	}
}
