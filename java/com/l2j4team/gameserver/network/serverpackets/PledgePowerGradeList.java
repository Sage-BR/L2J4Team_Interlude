package com.l2j4team.gameserver.network.serverpackets;

import com.l2j4team.gameserver.model.pledge.ClanMember;

import java.util.Set;

public class PledgePowerGradeList extends L2GameServerPacket
{
	private final Set<Integer> _ranks;
	private final ClanMember[] _members;
	
	public PledgePowerGradeList(Set<Integer> ranks, ClanMember[] members)
	{
		_ranks = ranks;
		_members = members;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xFE);
		writeH(0x3b);
		writeD(_ranks.size());
		for (int rank : _ranks)
		{
			writeD(rank);
			int count = 0;
			for (ClanMember member : _members)
			{
				if (member.getPowerGrade() == rank)
					count++;
			}
			writeD(count);
		}
	}
}