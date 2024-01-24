package com.l2j4team.gameserver.network.serverpackets;

import com.l2j4team.gameserver.model.actor.Summon;

/**
 * @author Yme
 */
public class PetStatusShow extends L2GameServerPacket
{
	private final int _summonType;
	
	public PetStatusShow(Summon summon)
	{
		_summonType = summon.getSummonType();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xB0);
		writeD(_summonType);
	}
}