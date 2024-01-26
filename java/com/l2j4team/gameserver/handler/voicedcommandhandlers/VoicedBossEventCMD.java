package com.l2j4team.gameserver.handler.voicedcommandhandlers;

import com.l2j4team.events.bossevent.BossEvent;
import com.l2j4team.events.bossevent.BossEvent.EventState;
import com.l2j4team.gameserver.handler.IVoicedCommandHandler;
import com.l2j4team.gameserver.model.actor.instance.Player;

public class VoicedBossEventCMD implements IVoicedCommandHandler
{
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String params)
	{
		if (command.startsWith("bossevent"))
		{
			if (BossEvent.getInstance().getState() != EventState.REGISTRATION)
			{
				activeChar.sendMessage("Boss Event is not running!");
				return false;
			}
			if (!BossEvent.getInstance().isRegistered(activeChar))
			{
				if (BossEvent.getInstance().addPlayer(activeChar))
				{
					activeChar.sendMessage("You have been successfully registered in Boss Event!");
				}

			}
			else
			{
				if (BossEvent.getInstance().removePlayer(activeChar))
				{
					activeChar.sendMessage("You have been successfully removed of Boss Event!");
				}
			}
		}
		return false;
	}

	@Override
	public String[] getVoicedCommandList()
	{

		return new String[]
		{
			"bossevent"
		};
	}

}