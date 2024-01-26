package com.l2j4team.gameserver.network.clientpackets;

import com.l2j4team.gameserver.model.World;
import com.l2j4team.gameserver.model.actor.instance.Player;

/**
 * @author -Wooden-
 */
public final class SnoopQuit extends L2GameClientPacket
{
	private int _snoopID;

	@Override
	protected void readImpl()
	{
		_snoopID = readD();
	}

	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		final Player target = World.getInstance().getPlayer(_snoopID);
		if (target == null)
			return;

		// No use
	}
}