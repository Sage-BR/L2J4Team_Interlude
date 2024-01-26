package com.l2j4team.gameserver.network.serverpackets;

import com.l2j4team.gameserver.model.pledge.ClanMember;

/**
 * Format : (ch) dSd
 * @author -Wooden-
 */
public class PledgeReceivePowerInfo extends L2GameServerPacket
{
	private final ClanMember _member;

	public PledgeReceivePowerInfo(ClanMember member)
	{
		_member = member;
	}

	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x3c);

		writeD(_member.getPowerGrade());
		writeS(_member.getName());
		writeD(_member.getClan().getPriviledgesByRank(_member.getPowerGrade()));
	}
}