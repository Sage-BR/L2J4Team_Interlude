package com.l2j4team.gameserver.network;

import com.l2j4team.gameserver.model.actor.instance.Player;
import com.l2j4team.gameserver.model.zone.ZoneId;
import com.l2j4team.gameserver.network.serverpackets.ExShowScreenMessage;

public class AioProtection implements Runnable
{
	private final Player _activeChar;

	public AioProtection(Player activeChar)
	{
		_activeChar = activeChar;
	}

	@Override
	public void run()
	{
		if (_activeChar.isOnline() && !_activeChar.isInsideZone(ZoneId.PEACE))
		{
			_activeChar.sendPacket(new ExShowScreenMessage("Aio Buffer not allowed outside the peace zone ..", 6000, 2, true));
			_activeChar.teleToLocation(82849, 147948, -3470, 0);
		}
	}
}
