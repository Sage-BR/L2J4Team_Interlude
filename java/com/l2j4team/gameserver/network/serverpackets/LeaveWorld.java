package com.l2j4team.gameserver.network.serverpackets;

public class LeaveWorld extends L2GameServerPacket
{
	public static final LeaveWorld STATIC_PACKET = new LeaveWorld();

	public LeaveWorld()
	{
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x7e);
	}
}