package com.l2j4team.gameserver.handler.voicedcommandhandlers;

import com.l2j4team.Config;
import com.l2j4team.gameserver.AutofarmManager;
import com.l2j4team.gameserver.handler.IVoicedCommandHandler;
import com.l2j4team.gameserver.model.actor.instance.Player;

public class AutofarmCommand implements IVoicedCommandHandler
{
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String params)
	{
		if (activeChar.getInstance().getId() != 0)
		{
			activeChar.sendMessage("You can't use this command inside a Instance!");
			return false;
		}
		if (Config.NEED_VIP && !activeChar.isVip())
		{
			activeChar.sendMessage("Auto farm is only for Vip Players!");
			return false;
		}
		
		switch (command)
		{
			case "farm":
				AutofarmManager.INSTANCE.toggleFarm(activeChar);
				break;
			case "farmon":
				AutofarmManager.INSTANCE.startFarm(activeChar);
				break;
			case "farmoff":
				AutofarmManager.INSTANCE.stopFarm(activeChar);
				break;
		}
		return false;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return new String[]
		{
			"farm",
			"farmon",
			"farmoff"
		};
	}
}