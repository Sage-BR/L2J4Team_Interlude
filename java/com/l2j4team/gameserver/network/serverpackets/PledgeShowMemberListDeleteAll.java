package com.l2j4team.gameserver.network.serverpackets;

public class PledgeShowMemberListDeleteAll extends L2GameServerPacket
{
	public static final PledgeShowMemberListDeleteAll STATIC_PACKET = new PledgeShowMemberListDeleteAll();

	private PledgeShowMemberListDeleteAll()
	{
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x82);
	}
}