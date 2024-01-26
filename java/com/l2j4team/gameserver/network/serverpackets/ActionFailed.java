package com.l2j4team.gameserver.network.serverpackets;

public final class ActionFailed extends L2GameServerPacket
{
	public static final ActionFailed STATIC_PACKET = new ActionFailed();

	private ActionFailed()
	{
	}

	@Override
	protected void writeImpl()
	{
		writeC(0x25);
	}
}