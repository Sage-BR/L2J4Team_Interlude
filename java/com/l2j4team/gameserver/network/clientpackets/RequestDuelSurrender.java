package com.l2j4team.gameserver.network.clientpackets;

import com.l2j4team.gameserver.instancemanager.DuelManager;

/**
 * Format:(ch)
 * @author -Wooden-
 */
public final class RequestDuelSurrender extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		DuelManager.getInstance().doSurrender(getClient().getActiveChar());
	}
}