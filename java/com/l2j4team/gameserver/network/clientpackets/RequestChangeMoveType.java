package com.l2j4team.gameserver.network.clientpackets;

import com.l2j4team.gameserver.model.actor.instance.Player;

public final class RequestChangeMoveType extends L2GameClientPacket
{
	private boolean _typeRun;
	
	@Override
	protected void readImpl()
	{
		_typeRun = readD() == 1;
	}
	
	@Override
	protected void runImpl()
	{
		// Get player.
		final Player player = getClient().getActiveChar();
		// Player is mounted, do not allow to change movement type.
		if ((player == null) || player.isMounted())
			return;
		
		// Change movement type.
		if (_typeRun)
			player.setRunning();
		else
			player.setWalking();
	}
}